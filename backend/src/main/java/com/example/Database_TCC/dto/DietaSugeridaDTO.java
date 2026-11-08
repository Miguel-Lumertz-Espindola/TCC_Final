package com.example.Database_TCC.dto;

import java.util.List;

public record DietaSugeridaDTO(String situacao, String mensagem, String versaoCatalogo,
        String perfilCodigo, String perfilNome, String objetivo, String varianteCodigo,
        String varianteNome, boolean demonstracao, int pontosCompatibilidade,
        List<String> criteriosAtendidos, List<String> tagsAlimentares,
        List<RefeicaoPlanejadaDTO> refeicoes) {
    public record RefeicaoPlanejadaDTO(String tipo, String nome, String horario,
            String descricao, List<String> ingredientes) { }
}
