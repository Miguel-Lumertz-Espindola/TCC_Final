package com.example.tcc_final.ui.home;
import android.content.*;
import android.os.Bundle;
import android.graphics.Paint;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.recyclerview.widget.ConcatAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.view.*;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.tcc_final.*;
import com.bumptech.glide.Glide;
import com.example.tcc_final.databinding.FragmentHomeBinding;
import database.MODEL.StatusGamificacao;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private HomeViewModel modelo;
    private AlimentoAdapter adapter;
    private AlertDialog informacoesPontuacao;
    private MenuPersonagens menuPersonagens;
    private String personagemExibido;
    private retrofit2.Call<com.example.tcc_final.dto.HorariosCheckinDTO> consultaHorarios;
    @Override public View onCreateView(@NonNull LayoutInflater inflater,ViewGroup container,Bundle estado){
        binding=FragmentHomeBinding.inflate(inflater,container,false);
        binding.textStatusMascote.setPaintFlags(binding.textStatusMascote.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        exibirPersonagem("GUERREIRO");
        modelo=new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        binding.btnTrocarPersonagem.setOnClickListener(v->{
            if(menuPersonagens!=null&&menuPersonagens.isShowing()){menuPersonagens.dismiss();return;}
            menuPersonagens=new MenuPersonagens(requireContext(),modelo::selecionarPersonagem);
            atualizarBotoes();menuPersonagens.abrir(binding.btnTrocarPersonagem);
        });
        adapter=new AlimentoAdapter(modelo::registrar);
        binding.recyclerViewAlimentos.setLayoutManager(new LinearLayoutManager(requireContext()));binding.recyclerViewAlimentos.setAdapter(new ConcatAdapter(adapter,new PontuacaoInfoAdapter(this::mostrarPontuacao)));
        binding.btnAbrirDieta.setOnClickListener(v->startActivity(new Intent(requireContext(),DietaActivity.class)));
        binding.btnAbrirDiario.setOnClickListener(v->startActivity(new Intent(requireContext(),DiarioActivity.class)));
        return binding.getRoot();
    }
    @Override public void onViewCreated(@NonNull View view,Bundle estado){
        super.onViewCreated(view,estado);
        modelo.getStatus().observe(getViewLifecycleOwner(),s->{mostrar(s);atualizarBotoes();});
        modelo.getCarregando().observe(getViewLifecycleOwner(),s->atualizarBotoes());
        modelo.getTrocandoPersonagem().observe(getViewLifecycleOwner(),s->atualizarBotoes());
        modelo.getEmAndamento().observe(getViewLifecycleOwner(),s->atualizarBotoes());
        modelo.getMensagem().observe(getViewLifecycleOwner(),texto->{if(texto!=null&&!texto.isEmpty()){Toast.makeText(requireContext(),texto,Toast.LENGTH_LONG).show();modelo.consumirMensagem();}});
    }
    @Override public void onResume(){
        super.onResume();String email=requireActivity().getSharedPreferences("SessaoUsuario",Context.MODE_PRIVATE).getString("email_ativo","");
        modelo.carregar(email);
    }
    private void atualizarBotoes(){
        boolean trocando=Boolean.TRUE.equals(modelo.getTrocandoPersonagem().getValue());
        boolean carregando=Boolean.TRUE.equals(modelo.getCarregando().getValue());
        if(adapter!=null)adapter.atualizar(modelo.getStatus().getValue(),carregando||trocando,modelo.getEmAndamento().getValue());
        if(binding!=null)binding.btnTrocarPersonagem.setEnabled(!trocando);
        if(menuPersonagens!=null)menuPersonagens.atualizar(modelo.getStatus().getValue(),carregando||trocando||modelo.getEmAndamento().getValue()!=null);
    }
    private void exibirPersonagem(String codigo){
        if(binding==null||codigo.equals(personagemExibido))return;
        personagemExibido=codigo;
        int icone=PersonagensVisuais.icone(codigo);
        Glide.with(this).asGif().load(PersonagensVisuais.animacao(codigo))
                .fitCenter().placeholder(icone).error(icone).into(binding.imageViewMascote);
        binding.iconePersonagemAtual.setImageResource(icone);
        if(binding.iconePersonagemAtual.getDrawable() instanceof android.graphics.drawable.BitmapDrawable)
            ((android.graphics.drawable.BitmapDrawable)binding.iconePersonagemAtual.getDrawable().mutate()).setFilterBitmap(false);
        binding.imageViewMascote.setContentDescription(getString(R.string.personagem_ativo,PersonagensVisuais.nome(codigo)));
    }
    private void mostrar(StatusGamificacao s){
        if(binding==null)return;
        if(s==null){binding.textPontuacao.setText(R.string.home_pontos_carregando);binding.textOfensiva.setText(R.string.home_dias_carregando);return;}
        exibirPersonagem(s.getPersonagemSelecionado());
        binding.textPontuacao.setText(getString(R.string.home_pontos,s.getPontuacaoTotal()));
        binding.textOfensiva.setText(getResources().getQuantityString(R.plurals.home_dias,s.getOfensivaDias(),s.getOfensivaDias()));
        binding.textStatusMascote.setText(s.getStatusMascote());
        binding.textNivel.setText(getString(R.string.home_nivel,(s.getPontuacaoTotal()/100)+1));binding.progressBarNivel.setProgress(s.getPontuacaoTotal()%100);
    }
    private void mostrarPontuacao(){
        if(informacoesPontuacao!=null)informacoesPontuacao.dismiss();
        ContextThemeWrapper contexto=new ContextThemeWrapper(requireContext(),R.style.Theme_Tcc_final_Diario);
        View conteudo=LayoutInflater.from(contexto).inflate(R.layout.dialog_pontuacao,null);
        AlertDialog janela=new MaterialAlertDialogBuilder(contexto)
                .setTitle(R.string.home_info_pontuacao).setView(conteudo)
                .setPositiveButton(R.string.conquista_close,null).show();
        informacoesPontuacao=janela;
        janela.setOnDismissListener(d->{
            if(informacoesPontuacao==janela){
                if(consultaHorarios!=null)consultaHorarios.cancel();
                consultaHorarios=null;informacoesPontuacao=null;
            }
        });
        consultaHorarios=api.ApiClient.getClient().create(api.UsuarioService.class).buscarHorariosCheckin();
        consultaHorarios.enqueue(new retrofit2.Callback<com.example.tcc_final.dto.HorariosCheckinDTO>(){
            @Override public void onResponse(retrofit2.Call<com.example.tcc_final.dto.HorariosCheckinDTO> call,retrofit2.Response<com.example.tcc_final.dto.HorariosCheckinDTO> resposta){
                if(consultaHorarios!=call||informacoesPontuacao!=janela)return;consultaHorarios=null;
                var dados=resposta.body();
                if(!resposta.isSuccessful()||dados==null||dados.refeicoes==null||dados.refeicoes.size()!=3){falhaHorarios(conteudo);return;}
                java.util.List<String> exatos=new java.util.ArrayList<>(),parciais=new java.util.ArrayList<>();
                for(var faixa:dados.refeicoes){
                    if(faixa==null||faixa.nome==null||faixa.inicio==null||faixa.fimExato==null||faixa.fimParcial==null){falhaHorarios(conteudo);return;}
                    exatos.add(getString(R.string.home_faixa_checkin,faixa.nome,faixa.inicio,faixa.fimExato));
                    parciais.add(getString(R.string.home_faixa_checkin,faixa.nome,faixa.fimExato,faixa.fimParcial));
                }
                ((android.widget.TextView)conteudo.findViewById(R.id.precisaoExataHorarios)).setText(android.text.TextUtils.join("\n",exatos));
                ((android.widget.TextView)conteudo.findViewById(R.id.precisaoParcialHorarios)).setText(android.text.TextUtils.join("\n",parciais));
                ((android.widget.TextView)conteudo.findViewById(R.id.horarioReferenciaCheckin)).setText(getString(R.string.home_horario_referencia,dados.horaAtual,dados.fusoHorario));
            }
            @Override public void onFailure(retrofit2.Call<com.example.tcc_final.dto.HorariosCheckinDTO> call,Throwable erro){
                if(consultaHorarios!=call||call.isCanceled()||informacoesPontuacao!=janela)return;
                consultaHorarios=null;falhaHorarios(conteudo);
            }
        });
    }
    private void falhaHorarios(View conteudo){
        ((android.widget.TextView)conteudo.findViewById(R.id.precisaoExataHorarios)).setText(R.string.home_horarios_indisponiveis);
        ((android.widget.TextView)conteudo.findViewById(R.id.precisaoParcialHorarios)).setText(R.string.home_horarios_indisponiveis);
        ((android.widget.TextView)conteudo.findViewById(R.id.horarioReferenciaCheckin)).setText(R.string.home_horarios_erro);
    }
    @Override public void onStop(){
        if(menuPersonagens!=null)menuPersonagens.dismiss();super.onStop();
    }
    @Override public void onDestroyView(){
        if(menuPersonagens!=null){menuPersonagens.dismiss();menuPersonagens=null;}
        personagemExibido=null;
        if(consultaHorarios!=null){consultaHorarios.cancel();consultaHorarios=null;}
        if(informacoesPontuacao!=null){informacoesPontuacao.dismiss();informacoesPontuacao=null;}
        if(binding!=null){Glide.with(this).clear(binding.imageViewMascote);binding.recyclerViewAlimentos.setAdapter(null);}
        adapter=null;binding=null;super.onDestroyView();
    }
}
