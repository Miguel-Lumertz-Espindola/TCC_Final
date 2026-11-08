package com.example.Database_TCC.dto;
import com.example.Database_TCC.model.TipoRefeicao;
import java.time.LocalDateTime;
import java.util.List;
public record RefeicaoRequest(TipoRefeicao tipoRefeicao, LocalDateTime dataHoraConsumo,
                             String descricao, String observacao, List<ItemRefeicaoDTO> itens) {
    // Mantem compatibilidade com chamadas anteriores sem lista de alimentos.
    public RefeicaoRequest(TipoRefeicao tipo, LocalDateTime consumo, String descricao, String observacao) {
        this(tipo, consumo, descricao, observacao, null);
    }
}
