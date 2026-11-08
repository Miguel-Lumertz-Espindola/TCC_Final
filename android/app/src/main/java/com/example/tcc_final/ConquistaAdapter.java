package com.example.tcc_final;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.example.tcc_final.dto.ConquistaDTO;
import java.util.List;

public class ConquistaAdapter extends RecyclerView.Adapter<ConquistaAdapter.ConquistaViewHolder> {
    private final List<ConquistaDTO> conquistas;
    private AlertDialog detalhes;
    public ConquistaAdapter(List<ConquistaDTO> conquistas){this.conquistas=conquistas;}
    @NonNull @Override public ConquistaViewHolder onCreateViewHolder(@NonNull ViewGroup pai,int tipo){return new ConquistaViewHolder(LayoutInflater.from(pai.getContext()).inflate(R.layout.item_conquista,pai,false));}
    private void imagem(ImageView imagem,ConquistaDTO c){
        imagem.setImageResource(IconesConquista.recurso(c));
        if(c.isDesbloqueada()){imagem.clearColorFilter();imagem.setAlpha(1f);}
        else{ColorMatrix matriz=new ColorMatrix();matriz.setSaturation(0);imagem.setColorFilter(new ColorMatrixColorFilter(matriz));imagem.setAlpha(.4f);}
    }
    @Override public void onBindViewHolder(@NonNull ConquistaViewHolder h,int posicao){
        ConquistaDTO c=conquistas.get(posicao);h.nome.setText(c.getNome());imagem(h.imagem,c);
        String status=h.itemView.getContext().getString(c.isDesbloqueada()?R.string.conquista_unlocked:R.string.conquista_locked);
        h.itemView.setContentDescription(h.itemView.getContext().getString(R.string.conquista_accessibility,c.getNome(),status));
        h.itemView.setOnClickListener(v->abrir(v,c));
    }
    private void abrir(View origem,ConquistaDTO c){
        fecharDetalhes();ContextThemeWrapper contexto=new ContextThemeWrapper(origem.getContext(),R.style.Theme_Tcc_final_Diario);
        View conteudo=LayoutInflater.from(contexto).inflate(R.layout.dialog_conquista,null);
        imagem(conteudo.findViewById(R.id.iconeDetalheConquista),c);
        ((TextView)conteudo.findViewById(R.id.statusDetalheConquista)).setText(c.isDesbloqueada()?R.string.conquista_unlocked:R.string.conquista_locked);
        ((TextView)conteudo.findViewById(R.id.criterioDetalheConquista)).setText(c.getCriterio());
        detalhes=new MaterialAlertDialogBuilder(contexto).setTitle(c.getNome()).setView(conteudo).setPositiveButton(R.string.conquista_close,null).show();
    }
    public void fecharDetalhes(){if(detalhes!=null){detalhes.dismiss();detalhes=null;}}
    @Override public int getItemCount(){return conquistas==null?0:conquistas.size();}
    static class ConquistaViewHolder extends RecyclerView.ViewHolder {
        ImageView imagem;TextView nome;
        ConquistaViewHolder(View v){super(v);imagem=v.findViewById(R.id.imgMedalha);nome=v.findViewById(R.id.txtNomeConquista);}
    }
}
