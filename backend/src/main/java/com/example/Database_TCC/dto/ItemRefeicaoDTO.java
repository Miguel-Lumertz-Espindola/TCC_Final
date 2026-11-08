package com.example.Database_TCC.dto;
import com.example.Database_TCC.model.*;
import java.math.BigDecimal;
public record ItemRefeicaoDTO(String nome, BigDecimal quantidade, UnidadeMedida unidade,
                             BigDecimal calorias, BigDecimal proteinas, BigDecimal carboidratos, BigDecimal gorduras) {
    public static ItemRefeicaoDTO de(ItemRefeicao i) {
        return new ItemRefeicaoDTO(i.getNome(), i.getQuantidade(), i.getUnidade(),
                i.getCalorias(), i.getProteinas(), i.getCarboidratos(), i.getGorduras());
    }
    public ItemRefeicao entidade() {
        ItemRefeicao i = new ItemRefeicao();
        i.setNome(nome.trim()); i.setQuantidade(quantidade); i.setUnidade(unidade);
        i.setCalorias(calorias); i.setProteinas(proteinas); i.setCarboidratos(carboidratos); i.setGorduras(gorduras);
        return i;
    }
}
