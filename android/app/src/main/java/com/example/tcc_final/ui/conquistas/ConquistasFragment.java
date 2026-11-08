package com.example.tcc_final.ui.conquistas;
import android.content.Context;
import android.os.Bundle;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import com.example.tcc_final.*;
import com.example.tcc_final.dto.ConquistaDTO;
import java.util.List;
import api.*;
import retrofit2.*;

public class ConquistasFragment extends Fragment {
    private RecyclerView lista;
    private ConquistaAdapter adapter;
    private Call<List<ConquistaDTO>> consulta;
    @Override public View onCreateView(@NonNull LayoutInflater inflater,ViewGroup pai,Bundle estado){
        View raiz=inflater.inflate(R.layout.fragment_conquistas,pai,false);lista=raiz.findViewById(R.id.recyclerViewConquistas);
        lista.setLayoutManager(new GridLayoutManager(requireContext(),3));return raiz;
    }
    @Override public void onResume(){super.onResume();carregar();}
    private void carregar(){
        String email=requireActivity().getSharedPreferences("SessaoUsuario",Context.MODE_PRIVATE).getString("email_ativo","");
        if(email==null||email.isEmpty())return;if(consulta!=null)consulta.cancel();
        consulta=ApiClient.getClient().create(UsuarioService.class).listarConquistas(email);
        consulta.enqueue(new Callback<List<ConquistaDTO>>(){
            @Override public void onResponse(Call<List<ConquistaDTO>> call,Response<List<ConquistaDTO>> r){
                if(lista==null||call!=consulta)return;
                if(r.isSuccessful()&&r.body()!=null){if(adapter!=null)adapter.fecharDetalhes();adapter=new ConquistaAdapter(r.body());lista.setAdapter(adapter);}
                else Toast.makeText(requireContext(),"Erro ao carregar conquistas.",Toast.LENGTH_SHORT).show();
            }
            @Override public void onFailure(Call<List<ConquistaDTO>> call,Throwable erro){if(lista!=null&&!call.isCanceled()&&call==consulta)Toast.makeText(requireContext(),"Falha de conexão ao carregar conquistas.",Toast.LENGTH_SHORT).show();}
        });
    }
    @Override public void onDestroyView(){if(consulta!=null)consulta.cancel();if(adapter!=null)adapter.fecharDetalhes();if(lista!=null)lista.setAdapter(null);adapter=null;lista=null;super.onDestroyView();}
}
