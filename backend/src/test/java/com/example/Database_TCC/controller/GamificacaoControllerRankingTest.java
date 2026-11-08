package com.example.Database_TCC.controller;

import com.example.Database_TCC.model.Usuario;
import com.example.Database_TCC.model.StatusGamificacao;
import com.example.Database_TCC.repository.StatusGamificacaoRepository;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class GamificacaoControllerRankingTest {
    private final GamificacaoController controller = new GamificacaoController();
    private void prepararRanking(List<StatusGamificacao> registros) throws Exception {
        var repository = java.lang.reflect.Proxy.newProxyInstance(
                StatusGamificacaoRepository.class.getClassLoader(),
                new Class<?>[]{StatusGamificacaoRepository.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("findTop50ByOrderByPontuacaoTotalDesc")) return registros;
                    throw new UnsupportedOperationException(method.getName());
                });
        var campo = GamificacaoController.class.getDeclaredField("statusRepository");
        campo.setAccessible(true);
        campo.set(controller, repository);
    }

    private StatusGamificacao status(String email, Integer pontos) {
        StatusGamificacao status = new StatusGamificacao(new Usuario(email, "senha-teste"));
        status.setPontuacaoTotal(pontos);
        return status;
    }

    @Test void rankingAceitaContasAntigasEPreservaPosicaoEPontos() throws Exception {
        prepararRanking(List.of(
            status("maria@example.com", 100), status("contaAntiga", 50),
            status("@example.com", 20), status("", 10), status(null, null)));
        var resposta = controller.obterRanking();
        assertEquals(200, resposta.getStatusCode().value());
        var ranking = resposta.getBody();
        assertNotNull(ranking);
        assertEquals(5, ranking.size());
        assertEquals("maria", ranking.get(0).getNomeUsuario());
        assertEquals("maria@example.com", ranking.get(0).getEmail());
        assertEquals("contaAntiga", ranking.get(1).getNomeUsuario());
        assertEquals(50, ranking.get(1).getPontuacaoTotal());
        assertEquals("Jogador", ranking.get(2).getNomeUsuario());
        assertEquals("Jogador", ranking.get(3).getNomeUsuario());
        assertEquals("Jogador", ranking.get(4).getNomeUsuario());
        assertEquals(0, ranking.get(4).getPontuacaoTotal());
        for (int i = 0; i < ranking.size(); i++) assertEquals(i + 1, ranking.get(i).getPosicao());
    }

    @Test void rankingSemJogadoresRetornaListaVazia() throws Exception {
        prepararRanking(List.of());
        var resposta = controller.obterRanking();
        assertEquals(200, resposta.getStatusCode().value());
        assertNotNull(resposta.getBody());
        assertTrue(resposta.getBody().isEmpty());
    }
}
