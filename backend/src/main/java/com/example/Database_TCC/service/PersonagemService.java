package com.example.Database_TCC.service;

import com.example.Database_TCC.dto.*;
import com.example.Database_TCC.gamificacao.CatalogoPersonagens;
import com.example.Database_TCC.model.StatusGamificacao;
import com.example.Database_TCC.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.Locale;

@Service
public class PersonagemService {
    private final UsuarioRepository usuarios;
    private final StatusGamificacaoRepository status;
    private final CatalogoPersonagens catalogo;
    private final CheckinService checkins;
    public PersonagemService(UsuarioRepository usuarios,StatusGamificacaoRepository status,
            CatalogoPersonagens catalogo,CheckinService checkins) {
        this.usuarios=usuarios;this.status=status;this.catalogo=catalogo;this.checkins=checkins;
    }
    @Transactional
    public StatusGamificacaoDTO selecionar(String email,PersonagemEscolhaDTO escolha) {
        if(email==null||email.isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Informe o usuário.");
        String codigo=escolha==null||escolha.codigo()==null?"":escolha.codigo().trim().toUpperCase(Locale.ROOT);
        var personagem=catalogo.buscar(codigo);
        if(personagem==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Personagem inválido.");
        // O mesmo bloqueio serializa a troca e o check-in do usuário.
        var usuario=usuarios.buscarParaAtualizarGamificacao(email.trim()).orElseThrow(()->
                new ResponseStatusException(HttpStatus.NOT_FOUND,"Usuário não encontrado."));
        var atual=status.findByUsuario(usuario).orElseGet(()->new StatusGamificacao(usuario));
        int pontos=catalogo.pontos(atual);
        if(pontos<personagem.pontosNecessarios())
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Faltam "+(personagem.pontosNecessarios()-pontos)+" pontos para desbloquear "+personagem.nome()+".");
        atual.setPersonagemCodigo(codigo);status.save(atual);
        return checkins.consultar(email.trim());
    }
}
