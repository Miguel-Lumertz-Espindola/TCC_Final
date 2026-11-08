package com.example.Database_TCC.service;
import com.example.Database_TCC.model.*;
import com.example.Database_TCC.repository.*;
import com.example.Database_TCC.gamificacao.RegrasConquistas;
import org.springframework.stereotype.Service;
import java.util.*;
@Service
public class GamificacaoService {
    private final ConquistaRepository conquistas;
    private final UsuarioConquistaRepository inventario;
    private final StatusGamificacaoRepository status;
    private final HistoricoCheckinRepository historico;
    public GamificacaoService(ConquistaRepository conquistas,UsuarioConquistaRepository inventario,StatusGamificacaoRepository status,HistoricoCheckinRepository historico) {
        this.conquistas=conquistas;this.inventario=inventario;this.status=status;this.historico=historico;
    }
    public void verificarDesbloqueioConquistas(Usuario usuario) {
        StatusGamificacao atual=status.findByUsuario(usuario).orElse(null);if(atual==null)return;
        Set<String> existentes=new HashSet<>();
        for(UsuarioConquista uc:inventario.findByUsuario(usuario))existentes.add(uc.getConquista().getNome());
        if(historico.existsByUsuario(usuario))desbloquear(usuario,existentes,"Primeiro Passo");
        if(atual.getOfensivaDias()!=null&&atual.getOfensivaDias()>=RegrasConquistas.DIAS_CHAMA)desbloquear(usuario,existentes,"Chama Viva");
        if(historico.countByUsuarioAndTipoRefeicaoIgnoreCaseAndStatusPrecisaoStartingWithIgnoreCase(usuario,"Almoco","Exato")>=RegrasConquistas.ALMOCOS_MESTRE)desbloquear(usuario,existentes,"Mestre do Almoço");
        if(historico.countByUsuarioAndStatusPrecisaoStartingWithIgnoreCase(usuario,"Exato")>=RegrasConquistas.REGISTROS_RELOGIO)desbloquear(usuario,existentes,"Relógio Suíço");
    }
    private void desbloquear(Usuario usuario,Set<String> existentes,String nome) {
        if(existentes.contains(nome))return;
        Conquista conquista=conquistas.findByNome(nome);
        if(conquista!=null){inventario.save(new UsuarioConquista(usuario,conquista));existentes.add(nome);}
    }
}
