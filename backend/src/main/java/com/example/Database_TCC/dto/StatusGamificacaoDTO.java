package com.example.Database_TCC.dto;
import java.time.*;
import java.util.List;

public record StatusGamificacaoDTO(int pontuacaoTotal,int ofensivaDias,String statusMascote,
        LocalDateTime ultimoCheckin,LocalDate dataReferencia,List<String> refeicoesRegistradasHoje,
        String personagemSelecionado,List<PersonagemDTO> personagens) { }
