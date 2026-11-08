package com.example.Database_TCC.ui.ranking; // Ajuste para o seu pacote

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tcc_final.R;
import com.example.tcc_final.RankingAdapter; // Importe o seu Adapter
import com.example.tcc_final.dto.UsuarioRankingDTO;

import java.util.List;

import api.ApiClient;
import api.UsuarioService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RankingFragment extends Fragment {

    private RecyclerView recyclerView;
    private RankingAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Infla o layout que criamos
        View root = inflater.inflate(R.layout.fragment_ranking, container, false);

        // Configura o RecyclerView em formato de lista vertical
        recyclerView = root.findViewById(R.id.recyclerViewRanking);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        carregarRanking();

        return root;
    }

    private void carregarRanking() {
        // 1. Pega o e-mail do usuário logado para destacar a linha dele na tabela
        SharedPreferences preferences = requireActivity().getSharedPreferences("SessaoUsuario", Context.MODE_PRIVATE);
        String emailLogado = preferences.getString("email_ativo", "");

        // 2. Chama a API do Spring Boot
        UsuarioService service = ApiClient.getClient().create(UsuarioService.class);
        Call<List<UsuarioRankingDTO>> call = service.obterRanking();

        call.enqueue(new Callback<List<UsuarioRankingDTO>>() {
            @Override
            public void onResponse(Call<List<UsuarioRankingDTO>> call, Response<List<UsuarioRankingDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<UsuarioRankingDTO> listaRanking = response.body();

                    // 3. Injeta a lista no Adapter, passando também o e-mail logado
                    adapter = new RankingAdapter(listaRanking, emailLogado);
                    recyclerView.setAdapter(adapter);

                } else {
                    Toast.makeText(getContext(), "Erro ao carregar a tabela de classificação.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<UsuarioRankingDTO>> call, Throwable t) {
                Toast.makeText(getContext(), "Falha de conexão: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}