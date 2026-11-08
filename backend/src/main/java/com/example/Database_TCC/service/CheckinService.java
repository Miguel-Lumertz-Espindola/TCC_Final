package com.example.Database_TCC.service;
import com.example.Database_TCC.dto.*;
import com.example.Database_TCC.model.*;
import com.example.Database_TCC.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.time.*;
import java.util.*;
import java.text.Normalizer;

@Service
public class CheckinService {
    private final UsuarioRepository usuarios;
    private final StatusGamificacaoRepository status;
    private final HistoricoCheckinRepository historico;
    private final GamificacaoService conquistas;
    private final Clock relogio;
    private final com.example.Database_TCC.gamificacao.CatalogoPersonagens personagens;
    private final com.example.Database_TCC.gamificacao.HorariosCheckin horarios;
    @Autowired public CheckinService(UsuarioRepository usuarios,StatusGamificacaoRepository status,HistoricoCheckinRepository historico,GamificacaoService conquistas,com.example.Database_TCC.gamificacao.HorariosCheckin horarios,com.example.Database_TCC.gamificacao.CatalogoPersonagens personagens) {
        this(usuarios,status,historico,conquistas,horarios,Clock.system(horarios.zona()),personagens);
    }
    CheckinService(UsuarioRepository usuarios,StatusGamificacaoRepository status,HistoricoCheckinRepository historico,GamificacaoService conquistas,Clock relogio) {
        this(usuarios,status,historico,conquistas,new com.example.Database_TCC.gamificacao.HorariosCheckin(),relogio);
    }
    CheckinService(UsuarioRepository usuarios,StatusGamificacaoRepository status,HistoricoCheckinRepository historico,GamificacaoService conquistas,com.example.Database_TCC.gamificacao.HorariosCheckin horarios,Clock relogio) {
        this(usuarios,status,historico,conquistas,horarios,relogio,new com.example.Database_TCC.gamificacao.CatalogoPersonagens(30));
    }
    CheckinService(UsuarioRepository usuarios,StatusGamificacaoRepository status,HistoricoCheckinRepository historico,GamificacaoService conquistas,com.example.Database_TCC.gamificacao.HorariosCheckin horarios,Clock relogio,com.example.Database_TCC.gamificacao.CatalogoPersonagens personagens) {
        this.usuarios=usuarios;this.status=status;this.historico=historico;this.conquistas=conquistas;
        this.horarios=horarios;this.relogio=relogio.withZone(horarios.zona());this.personagens=personagens;
    }
    static String tipo(String valor) {
        String chave=valor==null?"":Normalizer.normalize(valor,Normalizer.Form.NFD).replaceAll("\\p{M}","").replace('_',' ').trim().toLowerCase(Locale.ROOT);
        return switch(chave){case "cafe da manha" -> "Cafe_da_Manha";case "almoco" -> "Almoco";case "jantar" -> "Jantar";case "outros" -> "Outros";default -> null;};
    }
    private Usuario usuario(String email,boolean bloquear) {
        if(email==null||email.isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Informe o usuário.");
        return (bloquear?usuarios.buscarParaAtualizarGamificacao(email.trim()):usuarios.findByEmail(email.trim()))
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Usuário não encontrado."));
    }
    // Hoje ainda pode receber um check-in. A sequencia zera apos um dia completo sem pontos.
    static int ofensiva(List<LocalDateTime> registros,LocalDate hoje) {
        Set<LocalDate> datas=new HashSet<>();for(LocalDateTime d:registros)datas.add(d.toLocalDate());
        LocalDate dia=datas.contains(hoje)?hoje:hoje.minusDays(1);int dias=0;
        while(datas.contains(dia)){dias++;dia=dia.minusDays(1);}return dias;
    }
    private StatusGamificacaoDTO snapshot(Usuario usuario,StatusGamificacao atual,LocalDate hoje) {
        List<String> feitas=historico.findByUsuarioIdAndDataHoraRegistroGreaterThanEqualAndDataHoraRegistroLessThan(usuario.getId(),hoje.atStartOfDay(),hoje.plusDays(1).atStartOfDay())
                .stream().map(h->tipo(h.getTipoRefeicao())).filter(Objects::nonNull).distinct().sorted().toList();
        int dias=ofensiva(historico.datasComPontos(usuario.getId(),hoje.plusDays(1).atStartOfDay()),hoje);
        return new StatusGamificacaoDTO(atual.getPontuacaoTotal()==null?0:atual.getPontuacaoTotal(),dias,
                atual.getStatusMascote(),atual.getUltimoCheckin(),hoje,feitas,personagens.selecionado(atual),personagens.listar(atual));
    }
    public com.example.Database_TCC.dto.HorariosCheckinDTO consultarHorarios() { return horarios.consultar(relogio); }
    @Transactional(readOnly=true) public StatusGamificacaoDTO consultar(String email) {
        Usuario usuario=usuario(email,false);
        return snapshot(usuario,status.findByUsuario(usuario).orElseGet(()->new StatusGamificacao(usuario)),LocalDate.now(relogio));
    }
    @Transactional public CheckinRespostaDTO registrar(String email,String solicitado) {
        String tipo=tipo(solicitado);if(tipo==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Tipo de refeição inválido.");
        Usuario usuario=usuario(email,true);LocalDateTime agora=LocalDateTime.now(relogio);LocalDate hoje=agora.toLocalDate();
        StatusGamificacao atual=status.findByUsuario(usuario).orElseGet(()->new StatusGamificacao(usuario));
        boolean repetido=historico.findByUsuarioIdAndDataHoraRegistroGreaterThanEqualAndDataHoraRegistroLessThan(usuario.getId(),hoje.atStartOfDay(),hoje.plusDays(1).atStartOfDay())
                .stream().anyMatch(h->tipo.equals(tipo(h.getTipoRefeicao())));
        if(repetido)return new CheckinRespostaDTO("Esta refeição já foi registrada hoje.",true,snapshot(usuario,atual,hoje));
        var resultado=horarios.avaliar(tipo,agora.toLocalTime());
        int pontos=resultado.pontos();String precisao=resultado.precisao();
        int pontosAntes=atual.getPontuacaoTotal()==null?0:atual.getPontuacaoTotal();
        atual.setPontuacaoTotal(pontosAntes+pontos);
        atual.setStatusMascote("Animado e Alimentado!");atual.setUltimoCheckin(agora);status.save(atual);
        HistoricoCheckin registro=new HistoricoCheckin(usuario,tipo,pontos,precisao);registro.setDataHoraRegistro(agora);historico.save(registro);
        atual.setOfensivaDias(ofensiva(historico.datasComPontos(usuario.getId(),hoje.plusDays(1).atStartOfDay()),hoje));status.save(atual);
        conquistas.verificarDesbloqueioConquistas(usuario);
        String horaConsiderada=agora.atZone(relogio.getZone()).format(java.time.format.DateTimeFormatter.ofPattern("HH:mm XXX"));
        String mensagem="Check-in às "+horaConsiderada+": "+precisao+" | +"+pontos+" pontos";
        var novos=personagens.novosDesbloqueios(pontosAntes,atual.getPontuacaoTotal());
        if(!novos.isEmpty())mensagem+=". Personagem desbloqueado: "+String.join(", ",novos)+"!";
        return new CheckinRespostaDTO(mensagem,false,snapshot(usuario,atual,hoje));
    }
}
