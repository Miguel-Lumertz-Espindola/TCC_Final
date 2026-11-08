package com.example.tcc_final.dto;
import java.util.List;

public class HorariosCheckinDTO {
    public String fusoHorario,horaAtual;
    public List<FaixaDTO> refeicoes;
    public static class FaixaDTO {
        public String tipo,nome,inicio,fimExato,fimParcial;
    }
}
