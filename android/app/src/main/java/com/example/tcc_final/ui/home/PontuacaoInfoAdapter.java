package com.example.tcc_final.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.tcc_final.R;

/** Rodapé que acompanha a rolagem da lista de refeições. */
final class PontuacaoInfoAdapter extends RecyclerView.Adapter<PontuacaoInfoAdapter.InfoViewHolder> {
    private final Runnable abrirInformacoes;

    PontuacaoInfoAdapter(Runnable abrirInformacoes) {
        this.abrirInformacoes = abrirInformacoes;
    }

    @NonNull @Override
    public InfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new InfoViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_info_pontuacao, parent, false));
    }

    @Override public void onBindViewHolder(@NonNull InfoViewHolder holder, int position) {
        holder.itemView.findViewById(R.id.btnInfoPontuacao)
                .setOnClickListener(v -> abrirInformacoes.run());
    }

    @Override public int getItemCount() { return 1; }

    static final class InfoViewHolder extends RecyclerView.ViewHolder {
        InfoViewHolder(View view) { super(view); }
    }
}
