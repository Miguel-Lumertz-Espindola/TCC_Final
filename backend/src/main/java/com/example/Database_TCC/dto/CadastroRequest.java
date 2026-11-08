package com.example.Database_TCC.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CadastroRequest(String nome, String email, String senha,
        LocalDate dataNascimento, BigDecimal alturaCm, BigDecimal pesoInicialKg,
        String objetivo, String nivelAtividade, String praticidade, String rigor,
        String orcamento, List<String> tagsAlimentares,
        String restricoesAlimentares, String preferenciasAlimentares) { }
