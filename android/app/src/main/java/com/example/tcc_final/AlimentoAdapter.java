package com.example.tcc_final;
import android.content.res.ColorStateList;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import database.MODEL.StatusGamificacao;
import java.util.*;

public class AlimentoAdapter extends RecyclerView.Adapter<AlimentoAdapter.AlimentoViewHolder> {
    public interface OnCheckin { void registrar(String tipo); }
    private static final String[] TIPOS={"Cafe_da_Manha","Almoco","Jantar"};
    private final OnCheckin listener;
    private final Set<String> feitas=new HashSet<>();
    private boolean pronto,carregando;
    private String emAndamento;
    public AlimentoAdapter(OnCheckin listener){this.listener=listener;}
    public void atualizar(StatusGamificacao status,boolean carregando,String emAndamento){
        pronto=status!=null;this.carregando=carregando;this.emAndamento=emAndamento;
        feitas.clear();if(status!=null)feitas.addAll(status.getRefeicoesRegistradasHoje());notifyDataSetChanged();
    }
    @NonNull @Override public AlimentoViewHolder onCreateViewHolder(@NonNull ViewGroup pai,int tipo){
        return new AlimentoViewHolder(LayoutInflater.from(pai.getContext()).inflate(R.layout.activity_item_alimento,pai,false));
    }
    @Override public void onBindViewHolder(@NonNull AlimentoViewHolder h,int posicao){
        int[] nomes={R.string.home_cafe,R.string.home_almoco,R.string.home_jantar};
        h.nome.setText(nomes[posicao]);
        boolean feita=feitas.contains(TIPOS[posicao]),enviando=TIPOS[posicao].equals(emAndamento);
        int[] descricoes={R.string.home_refeicao_manha,R.string.home_refeicao_almoco,R.string.home_refeicao_noite};
        h.descricao.setText(feita?R.string.home_refeicao_registrada:descricoes[posicao]);
        h.botao.setText(feita?R.string.home_alimentado:enviando?R.string.diario_saving:R.string.home_alimentar);
        h.botao.setEnabled(pronto&&!carregando&&emAndamento==null&&!feita);
        h.botao.setBackgroundTintList(ColorStateList.valueOf(h.itemView.getContext().getColor(feita?R.color.ranking_surface:R.color.ranking_accent)));
        h.botao.setTextColor(h.itemView.getContext().getColor(feita?R.color.ranking_text_secondary:R.color.black));
        h.botao.setOnClickListener(v->{int p=h.getBindingAdapterPosition();if(p!=RecyclerView.NO_POSITION)listener.registrar(TIPOS[p]);});
    }
    @Override public int getItemCount(){return TIPOS.length;}
    public static class AlimentoViewHolder extends RecyclerView.ViewHolder {
        Button botao;TextView nome,descricao;
        AlimentoViewHolder(View v){super(v);botao=v.findViewById(R.id.btnAlimentar);nome=v.findViewById(R.id.txtNomeAlimento);descricao=v.findViewById(R.id.txtDescricaoAlimento);}
    }
}
