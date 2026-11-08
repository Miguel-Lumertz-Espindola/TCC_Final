package com.example.Database_TCC.dto;
import java.util.List;

public record HorariosCheckinDTO(String fusoHorario, String horaAtual,
        List<FaixaDTO> refeicoes) {
    public record FaixaDTO(String tipo, String nome, String inicio,
            String fimExato, String fimParcial) { }
}
