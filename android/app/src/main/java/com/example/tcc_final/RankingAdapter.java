package com.example.tcc_final; // Ajuste para o seu pacote

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tcc_final.dto.UsuarioRankingDTO;
import java.util.List;

public class RankingAdapter extends RecyclerView.Adapter<RankingAdapter.RankingViewHolder> {

    private List<UsuarioRankingDTO> listaRanking;
    private String emailLogado; // E-mail da sessão atual para comparação

    public RankingAdapter(List<UsuarioRankingDTO> listaRanking, String emailLogado) {
        this.listaRanking = listaRanking;
        this.emailLogado = emailLogado;
    }

    @NonNull
    @Override
    public RankingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ranking, parent, false);
        return new RankingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RankingViewHolder holder, int position) {
        UsuarioRankingDTO jogador = listaRanking.get(position);

        android.content.Context context = holder.itemView.getContext();
        holder.txtPosicao.setText(context.getString(R.string.ranking_position_value, jogador.getPosicao()));
        holder.txtPontos.setText(context.getString(R.string.ranking_points_value, jogador.getPontuacaoTotal()));
        holder.imgMascote.setImageResource(R.drawable.ic_character_shield_24);
        boolean proprioUsuario = emailLogado != null && !emailLogado.isEmpty()
                && emailLogado.equals(jogador.getEmail());
        holder.layoutLinha.setBackgroundResource(proprioUsuario
                ? R.drawable.bg_ranking_self : R.drawable.bg_ranking_row);
        holder.txtNome.setText(proprioUsuario
                ? context.getString(R.string.ranking_you, jogador.getNomeUsuario())
                : jogador.getNomeUsuario());
    }

    @Override
    public int getItemCount() {
        return listaRanking != null ? listaRanking.size() : 0;
    }

    public static class RankingViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutLinha;
        TextView txtPosicao, txtNome, txtPontos;
        ImageView imgMascote;

        public RankingViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutLinha = itemView.findViewById(R.id.layoutLinha);
            txtPosicao = itemView.findViewById(R.id.txtPosicao);
            txtNome = itemView.findViewById(R.id.txtNomeJogador);
            txtPontos = itemView.findViewById(R.id.txtPontosJogador);
            imgMascote = itemView.findViewById(R.id.imgMascoteRanking);
        }
    }
}