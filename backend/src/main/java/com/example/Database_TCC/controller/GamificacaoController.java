package com.example.Database_TCC.controller;

import com.example.Database_TCC.model.StatusGamificacao;
import com.example.Database_TCC.model.Usuario;
import com.example.Database_TCC.repository.StatusGamificacaoRepository;
import com.example.Database_TCC.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.Database_TCC.dto.UsuarioRankingDTO;
import java.util.Optional;
import com.example.Database_TCC.dto.ConquistaDTO;
import com.example.Database_TCC.model.Conquista;
import com.example.Database_TCC.model.UsuarioConquista;
import com.example.Database_TCC.repository.ConquistaRepository;
import com.example.Database_TCC.repository.UsuarioConquistaRepository;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/gamificacao")
public class GamificacaoController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private StatusGamificacaoRepository statusRepository;


    @Autowired private com.example.Database_TCC.service.CheckinService checkins;

    @Autowired private com.example.Database_TCC.service.PersonagemService personagens;

    @PostMapping("/personagem")
    public com.example.Database_TCC.dto.StatusGamificacaoDTO selecionarPersonagem(
            @RequestParam("email") String email,@RequestBody com.example.Database_TCC.dto.PersonagemEscolhaDTO escolha) {
        return personagens.selecionar(email,escolha);
    }

    @GetMapping("/horarios")
    public com.example.Database_TCC.dto.HorariosCheckinDTO consultarHorarios() {
        return checkins.consultarHorarios();
    }

    @PostMapping("/checkin")
    public ResponseEntity<?> realizarCheckin(@RequestParam("email") String email,@RequestParam("tipoRefeicao") String tipoRefeicao,
            @RequestParam(value="detalhado",defaultValue="false") boolean detalhado) {
        com.example.Database_TCC.dto.CheckinRespostaDTO resultado=checkins.registrar(email,tipoRefeicao);
        return ResponseEntity.ok(detalhado?resultado:resultado.mensagem());
    }
    @GetMapping("/status")
    public ResponseEntity<com.example.Database_TCC.dto.StatusGamificacaoDTO> buscarStatus(@RequestParam("email") String email) {
        return ResponseEntity.ok(checkins.consultar(email));
    }
    @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
    public ResponseEntity<String> erroGamificacao(org.springframework.web.server.ResponseStatusException erro) {
        return ResponseEntity.status(erro.getStatusCode()).body(erro.getReason());
    }

    @Autowired
    private ConquistaRepository conquistaRepository;

    @Autowired
    private UsuarioConquistaRepository usuarioConquistaRepository;

    @GetMapping("/conquistas")
    public ResponseEntity<List<ConquistaDTO>> listarConquistas(@RequestParam String email) {
        // Verifica se o usuário existe
        Optional<Usuario> optUsuario = usuarioRepository.findByEmail(email);
        if (!optUsuario.isPresent()) {
            return ResponseEntity.badRequest().build();
        }
        Usuario usuario = optUsuario.get();

        // Busca todas as medalhas cadastradas no jogo (catalogo)
        List<Conquista> todasConquistas = conquistaRepository.findAll();

        // Busca apenas as medalhas que ESTE usuário já destravou
        List<UsuarioConquista> conquistasDoUsuario = usuarioConquistaRepository.findByUsuario(usuario);

        // Monta a resposta cruzando os dados
        List<ConquistaDTO> prateleira = new ArrayList<>();

        for (Conquista conquista : todasConquistas) {
            // Checa se a conquista atual do loop está na lista de destravadas do usuário
            boolean possui = conquistasDoUsuario.stream()
                    .anyMatch(uc -> uc.getConquista().getId().equals(conquista.getId()));

            prateleira.add(new ConquistaDTO(
                    conquista.getId(),
                    conquista.getNome(),
                    conquista.getDescricao(),
                    conquista.getIconeUrl(),
                    conquista.getPontosRecompensa(),
                    possui
            ));
        }

        return ResponseEntity.ok(prateleira);
    }

    @GetMapping("/ranking")
    public ResponseEntity<List<UsuarioRankingDTO>> obterRanking() {
        List<StatusGamificacao> topStatus = statusRepository.findTop50ByOrderByPontuacaoTotalDesc();
        List<UsuarioRankingDTO> ranking = new ArrayList<>();
        int posicao = 1;

        for (StatusGamificacao status : topStatus) {
            String emailJogador = status.getUsuario().getEmail();
            // Contas antigas podem ter sido cadastradas sem um e-mail válido.
            int separador = emailJogador == null ? -1 : emailJogador.indexOf('@');
            String nomeExibicao = separador > 0
                    ? emailJogador.substring(0, separador)
                    : emailJogador == null || emailJogador.isBlank() || separador == 0
                            ? "Jogador" : emailJogador;
            int pontuacao = status.getPontuacaoTotal() == null ? 0 : status.getPontuacaoTotal();

            ranking.add(new UsuarioRankingDTO(
                    posicao++,
                    emailJogador,
                    nomeExibicao,
                    status.getStatusMascote(),
                    pontuacao
            ));
        }
        return ResponseEntity.ok(ranking);
    }


}
