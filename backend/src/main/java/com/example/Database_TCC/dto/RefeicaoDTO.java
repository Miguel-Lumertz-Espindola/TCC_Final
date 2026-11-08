package com.example.Database_TCC.dto;
import com.example.Database_TCC.model.RefeicaoRealizada;
import com.example.Database_TCC.model.TipoRefeicao;
import java.time.LocalDateTime;
import java.util.List;
public record RefeicaoDTO(Long id, TipoRefeicao tipoRefeicao, LocalDateTime dataHoraConsumo,
                          String descricao, String observacao, LocalDateTime dataHoraRegistro,
                          List<ItemRefeicaoDTO> itens) {
    public static RefeicaoDTO de(RefeicaoRealizada r) {
        return new RefeicaoDTO(r.getId(), r.getTipoRefeicao(), r.getDataHoraConsumo(),
                r.getDescricao(), r.getObservacao(), r.getDataHoraRegistro(),
                r.getItens().stream().map(ItemRefeicaoDTO::de).toList());
    }
}
