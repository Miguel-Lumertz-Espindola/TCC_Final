package com.example.Database_TCC.service;
import com.example.Database_TCC.dto.*;
import com.example.Database_TCC.model.*;
import com.example.Database_TCC.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Service
public class MetasService {
    private final UsuarioRepository usuarios;
    private final MetasUsuarioRepository metas;
    private final Clock relogio;
    @Autowired
    public MetasService(UsuarioRepository usuarios, MetasUsuarioRepository metas) {
        this(usuarios,metas,Clock.systemDefaultZone());
    }
    MetasService(UsuarioRepository usuarios, MetasUsuarioRepository metas, Clock relogio) {
        this.usuarios=usuarios;this.metas=metas;this.relogio=relogio;
    }
    private Usuario usuario(String email, boolean bloquear) {
        if(email==null || email.isBlank()) throw erro("Informe o usuário.");
        return (bloquear ? usuarios.buscarParaAtualizarMetas(email.trim()) : usuarios.findByEmail(email.trim()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Usuário não encontrado."));
    }
    private ResponseStatusException erro(String mensagem) { return new ResponseStatusException(HttpStatus.BAD_REQUEST,mensagem); }
    private void numero(BigDecimal valor) {
        if(valor!=null && (valor.signum()<=0 || valor.compareTo(new BigDecimal("999999999"))>0 || valor.stripTrailingZeros().scale()>2))
            throw erro("Preencha metas positivas, até 999999999, com até duas casas decimais. Deixe em branco o que não deseja acompanhar.");
    }
    private void validar(MetasRequest dados) {
        if(dados==null || !Arrays.asList("MANUTENCAO","EMAGRECIMENTO","HIPERTROFIA").contains(dados.objetivo()))
            throw erro("Selecione um objetivo válido.");
        if(dados.calorias()==null && dados.proteinas()==null && dados.carboidratos()==null && dados.gorduras()==null)
            throw erro("Informe pelo menos uma meta diária.");
        numero(dados.calorias());numero(dados.proteinas());numero(dados.carboidratos());numero(dados.gorduras());
    }
    private boolean igual(BigDecimal a,BigDecimal b) { return a==null ? b==null : b!=null && a.compareTo(b)==0; }
    private boolean semAlteracoes(MetasUsuario atual, MetasRequest d) {
        return atual.getObjetivo().equals(d.objetivo()) && igual(atual.getCalorias(),d.calorias()) && igual(atual.getProteinas(),d.proteinas())
                && igual(atual.getCarboidratos(),d.carboidratos()) && igual(atual.getGorduras(),d.gorduras());
    }
    @Transactional(readOnly=true)
    public MetasConsultaDTO consultar(String email, LocalDate data) {
        Usuario usuario=usuario(email,false);
        LocalDate dia=data==null?LocalDate.now(relogio):data;
        if(dia.isAfter(LocalDate.now(relogio))) throw erro("Consulte metas de hoje ou de dias anteriores.");
        // A ultima revisao do dia representa as metas daquele dia; revisoes posteriores nao o alteram.
        MetasUsuario meta=metas.findFirstByUsuarioIdAndInicioVigenciaBeforeOrderByInicioVigenciaDescIdDesc(usuario.getId(),dia.plusDays(1).atStartOfDay()).orElse(null);
        return new MetasConsultaDTO(meta==null?usuario.getObjetivo():meta.getObjetivo(),meta==null?null:MetasDTO.de(meta));
    }
    @Transactional(readOnly=true)
    public List<MetasDTO> historico(String email) {
        return metas.findTop50ByUsuarioIdOrderByInicioVigenciaDescIdDesc(usuario(email,false).getId()).stream().map(MetasDTO::de).toList();
    }
    @Transactional
    public MetasDTO salvar(String email, MetasRequest dados) {
        validar(dados);
        Usuario usuario=usuario(email,true);
        MetasUsuario atual=metas.findFirstByUsuarioIdOrderByInicioVigenciaDescIdDesc(usuario.getId()).orElse(null);
        if(atual!=null && semAlteracoes(atual,dados)) return MetasDTO.de(atual);
        MetasUsuario nova=new MetasUsuario();nova.setUsuario(usuario);nova.setObjetivo(dados.objetivo());
        nova.setInicioVigencia(LocalDateTime.now(relogio));nova.setCalorias(dados.calorias());nova.setProteinas(dados.proteinas());
        nova.setCarboidratos(dados.carboidratos());nova.setGorduras(dados.gorduras());
        return MetasDTO.de(metas.save(nova));
    }
}
