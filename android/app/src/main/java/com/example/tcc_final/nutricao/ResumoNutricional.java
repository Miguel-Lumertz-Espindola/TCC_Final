package com.example.tcc_final.nutricao;
import com.example.tcc_final.dto.*;
import java.math.BigDecimal;
import java.util.List;

/** Soma valores declarados para a porcao consumida, sem estimar dados ausentes. */
public final class ResumoNutricional {
    public static final class Valor {
        public BigDecimal conhecido;
        public int ausentes;
        private void adicionar(BigDecimal valor) {
            if (valor == null) ausentes++;
            else conhecido = conhecido == null ? valor : conhecido.add(valor);
        }
    }
    public final Valor calorias = new Valor(), proteinas = new Valor(), carboidratos = new Valor(), gorduras = new Valor();
    public ResumoNutricional(List<RefeicaoDTO> refeicoes) {
        for (RefeicaoDTO refeicao : refeicoes) {
            if (refeicao.itens == null || refeicao.itens.isEmpty()) {
                calorias.adicionar(null); proteinas.adicionar(null); carboidratos.adicionar(null); gorduras.adicionar(null);
                continue;
            }
            for (ItemRefeicaoDTO item : refeicao.itens) {
                calorias.adicionar(item.calorias); proteinas.adicionar(item.proteinas);
                carboidratos.adicionar(item.carboidratos); gorduras.adicionar(item.gorduras);
            }
        }
    }
}
