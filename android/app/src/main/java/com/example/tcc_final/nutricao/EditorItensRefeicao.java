package com.example.tcc_final.nutricao;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.*;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.example.tcc_final.R;
import com.example.tcc_final.dto.ItemRefeicaoDTO;
import java.math.BigDecimal;
import java.util.*;

/** Edita uma copia da lista; as alteracoes so sao enviadas ao salvar a refeicao. */
public final class EditorItensRefeicao {
    private static final String[] UNIDADES = {null,"G","ML","UNIDADE","PORCAO","COLHER_SOPA","XICARA"};
    private final AppCompatActivity activity;
    private final LinearLayout lista;
    private final Button adicionar;
    private final List<ItemRefeicaoDTO> itens = new ArrayList<>();
    private AlertDialog formulario;
    public EditorItensRefeicao(AppCompatActivity activity, View raiz, List<ItemRefeicaoDTO> originais) {
        this.activity=activity; lista=raiz.findViewById(R.id.itensRefeicaoDiario);
        adicionar=raiz.findViewById(R.id.adicionarItemRefeicao);
        if (originais != null) itens.addAll(originais);
        adicionar.setOnClickListener(v -> editar(-1)); atualizar();
    }
    public List<ItemRefeicaoDTO> obterItens() { return new ArrayList<>(itens); }
    public void fechar() { if (formulario != null) formulario.dismiss(); }
    private void atualizar() {
        lista.removeAllViews(); adicionar.setEnabled(itens.size()<50);
        for (int i=0;i<itens.size();i++) {
            final int indice=i; ItemRefeicaoDTO item=itens.get(i);
            View linha=activity.getLayoutInflater().inflate(R.layout.item_alimento_form,lista,false);
            ((TextView)linha.findViewById(R.id.nomeAlimentoForm)).setText(TextoNutricional.nome(activity,item));
            ((TextView)linha.findViewById(R.id.nutrientesAlimentoForm)).setText(TextoNutricional.nutrientes(activity,item));
            linha.findViewById(R.id.editarAlimentoForm).setOnClickListener(v -> editar(indice));
            linha.findViewById(R.id.removerAlimentoForm).setOnClickListener(v -> {itens.remove(indice);atualizar();});
            lista.addView(linha);
        }
    }
    private String texto(View raiz,int id) {
        TextInputEditText campo=raiz.findViewById(id);
        return campo.getText()==null ? "" : campo.getText().toString().trim();
    }
    private BigDecimal decimal(View raiz,int id,int casas) {
        String texto=texto(raiz,id); if (texto.isEmpty()) return null;
        try {
            BigDecimal valor=new BigDecimal(texto.replace(',','.'));
            if (valor.signum()<0 || valor.compareTo(new BigDecimal("999999999"))>0 || valor.stripTrailingZeros().scale()>casas) throw new NumberFormatException();
            return valor;
        } catch (NumberFormatException e) {
            TextInputEditText campo=raiz.findViewById(id);
            campo.setError(activity.getString(R.string.alimentos_number_error,casas)); campo.requestFocus();
            throw new IllegalArgumentException("decimal");
        }
    }
    private void preencher(View raiz,int id,BigDecimal valor) {
        if (valor!=null) ((TextInputEditText)raiz.findViewById(id)).setText(valor.stripTrailingZeros().toPlainString());
    }
    private void editar(int indice) {
        if (indice<0 && itens.size()>=50) { Toast.makeText(activity,R.string.alimentos_limit,Toast.LENGTH_SHORT).show();return; }
        View campos=activity.getLayoutInflater().inflate(R.layout.dialog_item_refeicao,null);
        Spinner unidade=campos.findViewById(R.id.unidadeItem);
        ArrayAdapter<CharSequence> unidades=ArrayAdapter.createFromResource(activity,R.array.alimentos_units,android.R.layout.simple_spinner_item);
        unidades.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);unidade.setAdapter(unidades);
        if(indice>=0) {
            ItemRefeicaoDTO item=itens.get(indice);
            ((TextInputEditText)campos.findViewById(R.id.nomeItem)).setText(item.nome);
            preencher(campos,R.id.quantidadeItem,item.quantidade);preencher(campos,R.id.caloriasItem,item.calorias);
            preencher(campos,R.id.proteinasItem,item.proteinas);preencher(campos,R.id.carboidratosItem,item.carboidratos);preencher(campos,R.id.gordurasItem,item.gorduras);
            for(int i=1;i<UNIDADES.length;i++) if(UNIDADES[i].equals(item.unidade)) unidade.setSelection(i);
        }
        AlertDialog dialogo=new MaterialAlertDialogBuilder(activity).setTitle(indice<0?R.string.alimentos_add:R.string.alimentos_edit)
                .setView(campos).setNegativeButton(R.string.diario_cancel,null).setPositiveButton(R.string.diario_save,null).create();
        formulario=dialogo;
        dialogo.setOnShowListener(v -> dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(botao -> {
            String nome=texto(campos,R.id.nomeItem);
            if(nome.isEmpty()) {
                TextInputEditText campo=campos.findViewById(R.id.nomeItem);campo.setError(activity.getString(R.string.alimentos_name_required));campo.requestFocus();return;
            }
            ItemRefeicaoDTO item=new ItemRefeicaoDTO();item.nome=nome;
            try {
                item.quantidade=decimal(campos,R.id.quantidadeItem,3);item.unidade=UNIDADES[unidade.getSelectedItemPosition()];
                if(item.quantidade!=null && item.quantidade.signum()==0) {
                    ((TextInputEditText)campos.findViewById(R.id.quantidadeItem)).setError(activity.getString(R.string.alimentos_quantity_error));return;
                }
                if((item.quantidade==null)!=(item.unidade==null)) {
                    Toast.makeText(activity,R.string.alimentos_pair_error,Toast.LENGTH_LONG).show();return;
                }
                item.calorias=decimal(campos,R.id.caloriasItem,2);item.proteinas=decimal(campos,R.id.proteinasItem,2);
                item.carboidratos=decimal(campos,R.id.carboidratosItem,2);item.gorduras=decimal(campos,R.id.gordurasItem,2);
            } catch(IllegalArgumentException erro) {return;}
            if(indice<0) itens.add(item);else itens.set(indice,item);
            atualizar();dialogo.dismiss();
        }));
        dialogo.show();
    }
}
