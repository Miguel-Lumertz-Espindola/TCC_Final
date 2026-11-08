package com.example.Database_TCC.dto;
import java.math.BigDecimal;
public record MetasRequest(String objetivo, BigDecimal calorias, BigDecimal proteinas, BigDecimal carboidratos, BigDecimal gorduras) { }
