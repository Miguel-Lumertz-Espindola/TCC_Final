package com.example.tcc_final.nutricao;
import com.example.tcc_final.dto.*;
import java.math.BigDecimal;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;
public class ResumoNutricionalTest {
    private ItemRefeicaoDTO item(String calorias, String proteinas) {
        ItemRefeicaoDTO i=new ItemRefeicaoDTO();i.nome="Alimento";
        i.calorias=calorias==null?null:new BigDecimal(calorias);i.proteinas=proteinas==null?null:new BigDecimal(proteinas);return i;
    }
    private RefeicaoDTO refeicao(ItemRefeicaoDTO... itens) {
        RefeicaoDTO r=new RefeicaoDTO();r.itens=Arrays.asList(itens);return r;
    }
    @Test public void somaDecimalSemErroDePontoFlutuante() {
        var r=new ResumoNutricional(List.of(refeicao(item("0.1","2.5"),item("0.2","3"))));
        assertEquals(0,new BigDecimal("0.3").compareTo(r.calorias.conhecido));
        assertEquals(0,new BigDecimal("5.5").compareTo(r.proteinas.conhecido));assertEquals(0,r.calorias.ausentes);
    }
    @Test public void valorAusenteNaoViraZero() {
        var r=new ResumoNutricional(List.of(refeicao(item(null,null))));
        assertNull(r.calorias.conhecido);assertEquals(1,r.calorias.ausentes);
    }
    @Test public void zeroExplicitoContinuaConhecido() {
        var r=new ResumoNutricional(List.of(refeicao(item("0",null))));
        assertNotNull(r.calorias.conhecido);assertEquals(0,r.calorias.conhecido.signum());
        assertEquals(0,r.calorias.ausentes);assertNull(r.proteinas.conhecido);
    }
    @Test public void cadaNutrienteTemSuaPropriaCompletude() {
        var r=new ResumoNutricional(List.of(refeicao(item("100","5"),item("50",null))));
        assertEquals(0,new BigDecimal("150").compareTo(r.calorias.conhecido));assertEquals(0,r.calorias.ausentes);
        assertEquals(1,r.proteinas.ausentes);assertEquals(2,r.gorduras.ausentes);
    }
    @Test public void refeicaoSemItensTornaOsTotaisParciais() {
        var r=new ResumoNutricional(List.of(refeicao(item("100","5")),new RefeicaoDTO()));
        assertEquals(1,r.calorias.ausentes);assertEquals(1,r.proteinas.ausentes);
        assertEquals(0,new BigDecimal("100").compareTo(r.calorias.conhecido));
    }
    @Test public void diaSemRegistrosNaoInventaTotais() {
        var r=new ResumoNutricional(Collections.emptyList());assertNull(r.calorias.conhecido);assertEquals(0,r.calorias.ausentes);
    }
}
