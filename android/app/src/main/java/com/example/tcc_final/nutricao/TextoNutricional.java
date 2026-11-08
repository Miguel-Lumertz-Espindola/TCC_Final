package com.example.tcc_final.nutricao;
import android.content.Context;
import com.example.tcc_final.R;
import com.example.tcc_final.dto.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;

public final class TextoNutricional {
    private static final String[] UNIDADES = {"G", "ML", "UNIDADE", "PORCAO", "COLHER_SOPA", "XICARA"};
    private TextoNutricional() { }
    public static String numero(BigDecimal valor) {
        NumberFormat formato = NumberFormat.getNumberInstance();
        formato.setGroupingUsed(false); formato.setMaximumFractionDigits(3);
        return formato.format(valor);
    }
    private static String valor(Context c, BigDecimal numero, String unidade, boolean parcial) {
        if (numero == null) return c.getString(R.string.alimentos_unknown);
        String texto = c.getString(R.string.alimentos_metric, numero(numero), unidade);
        return parcial ? c.getString(R.string.alimentos_partial, texto) : texto;
    }
    public static String nome(Context c, ItemRefeicaoDTO item) {
        if (item.quantidade == null || item.unidade == null) return item.nome;
        String[] unidades = c.getResources().getStringArray(R.array.alimentos_units);
        for (int i = 0; i < UNIDADES.length; i++) {
            if (UNIDADES[i].equals(item.unidade)) return c.getString(R.string.alimentos_amount, item.nome, numero(item.quantidade), unidades[i+1]);
        }
        return item.nome;
    }
    public static String nutrientes(Context c, ItemRefeicaoDTO item) {
        if (item.calorias == null && item.proteinas == null && item.carboidratos == null && item.gorduras == null)
            return c.getString(R.string.alimentos_no_nutrients);
        return c.getString(R.string.alimentos_metrics, valor(c,item.calorias,"kcal",false),
                valor(c,item.proteinas,"g",false), valor(c,item.carboidratos,"g",false), valor(c,item.gorduras,"g",false));
    }
    public static String alimentos(Context c, List<ItemRefeicaoDTO> itens) {
        if (itens == null || itens.isEmpty()) return c.getString(R.string.alimentos_undetailed);
        StringBuilder texto = new StringBuilder();
        for (ItemRefeicaoDTO item : itens) {
            if (texto.length() > 0) texto.append("\n\n");
            texto.append(nome(c,item)).append('\n').append(nutrientes(c,item));
        }
        return texto.toString();
    }
    public static String resumo(Context c, ResumoNutricional resumo) {
        return c.getString(R.string.alimentos_metrics,
                valor(c,resumo.calorias.conhecido,"kcal",resumo.calorias.ausentes>0),
                valor(c,resumo.proteinas.conhecido,"g",resumo.proteinas.ausentes>0),
                valor(c,resumo.carboidratos.conhecido,"g",resumo.carboidratos.ausentes>0),
                valor(c,resumo.gorduras.conhecido,"g",resumo.gorduras.ausentes>0));
    }

    private static String meta(Context c, BigDecimal numero, String unidade) {
        return numero==null ? c.getString(R.string.metas_unknown) : c.getString(R.string.alimentos_metric,numero(numero),unidade);
    }
    public static String metas(Context c, MetasDTO m) {
        return c.getString(R.string.alimentos_metrics, meta(c,m.calorias,"kcal"),meta(c,m.proteinas,"g"),meta(c,m.carboidratos,"g"),meta(c,m.gorduras,"g"));
    }
    private static String comparar(Context c,int nome, ResumoNutricional.Valor consumo,BigDecimal meta,String unidade) {
        return c.getString(R.string.metas_comparison,c.getString(nome),valor(c,consumo.conhecido,unidade,consumo.ausentes>0),meta(c,meta,unidade));
    }
    public static String comparar(Context c,ResumoNutricional r,MetasDTO m) {
        return comparar(c,R.string.metas_label_calories,r.calorias,m.calorias,"kcal")+"\n"
            +comparar(c,R.string.metas_label_protein,r.proteinas,m.proteinas,"g")+"\n"
            +comparar(c,R.string.metas_label_carbs,r.carboidratos,m.carboidratos,"g")+"\n"
            +comparar(c,R.string.metas_label_fat,r.gorduras,m.gorduras,"g");
    }
}
