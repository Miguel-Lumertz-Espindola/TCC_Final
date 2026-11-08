package com.example.tcc_final.dto;
import java.util.List;

public class DietaSugeridaDTO {
    public String situacao, mensagem, versaoCatalogo, perfilCodigo, perfilNome, objetivo;
    public String varianteCodigo, varianteNome;
    public boolean demonstracao;
    public int pontosCompatibilidade;
    public List<String> criteriosAtendidos, tagsAlimentares;
    public List<RefeicaoPlanejadaDTO> refeicoes;
    public static class RefeicaoPlanejadaDTO {
        public String tipo, nome, horario, descricao;
        public List<String> ingredientes;
    }
}
