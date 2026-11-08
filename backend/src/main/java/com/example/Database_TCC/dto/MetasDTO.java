package com.example.Database_TCC.dto;
import com.example.Database_TCC.model.MetasUsuario;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public record MetasDTO(Long id, String objetivo, LocalDateTime inicioVigencia,
                       BigDecimal calorias, BigDecimal proteinas, BigDecimal carboidratos, BigDecimal gorduras) {
    public static MetasDTO de(MetasUsuario m) {
        return new MetasDTO(m.getId(),m.getObjetivo(),m.getInicioVigencia(),m.getCalorias(),m.getProteinas(),m.getCarboidratos(),m.getGorduras());
    }
}
