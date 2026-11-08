package com.example.tcc_final;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import com.example.tcc_final.dto.DietaSugeridaDTO;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import api.ApiClient;
import api.UsuarioService;
import retrofit2.*;

public class DietaActivity extends AppCompatActivity {
    private Call<DietaSugeridaDTO> consulta;
    private boolean aposCadastro;
    private TextView estado;
    private Button repetir;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); setContentView(R.layout.activity_dieta);
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v,insets)->{
            androidx.core.graphics.Insets barras=insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(barras.left,barras.top,barras.right,barras.bottom); return insets;
        });
        aposCadastro=getIntent().getBooleanExtra("aposCadastro",false);
        MaterialToolbar toolbar=findViewById(R.id.toolbarDieta);
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        toolbar.setNavigationIconTint(getColor(R.color.ranking_accent));
        toolbar.setNavigationContentDescription(R.string.diario_back);
        toolbar.setNavigationOnClickListener(v->voltar());
        getOnBackPressedDispatcher().addCallback(this,new OnBackPressedCallback(true){
            @Override public void handleOnBackPressed(){voltar();}
        });
        estado=findViewById(R.id.estadoDieta); repetir=findViewById(R.id.repetirDieta);
        repetir.setOnClickListener(v->carregar());
        findViewById(R.id.continuarDieta).setVisibility(aposCadastro?View.VISIBLE:View.GONE);
        findViewById(R.id.continuarDieta).setOnClickListener(v->voltar());
        carregar();
    }
    private void voltar() {
        if(aposCadastro){
            startActivity(new Intent(this,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
        }
        finish();
    }
    private void carregar() {
        if(consulta!=null)consulta.cancel();
        String email=getSharedPreferences("SessaoUsuario",MODE_PRIVATE).getString("email_ativo","");
        if(email==null||email.isEmpty()){estado.setText(R.string.diario_session);repetir.setVisibility(View.GONE);return;}
        estado.setVisibility(View.VISIBLE);estado.setText(R.string.dieta_carregando);
        repetir.setVisibility(View.GONE);findViewById(R.id.conteudoDieta).setVisibility(View.GONE);
        consulta=ApiClient.getClient().create(UsuarioService.class).buscarDieta(email);
        consulta.enqueue(new Callback<DietaSugeridaDTO>(){
            @Override public void onResponse(Call<DietaSugeridaDTO> call,Response<DietaSugeridaDTO> resposta){
                if(call!=consulta||isFinishing()||isDestroyed())return;consulta=null;
                if(resposta.isSuccessful()&&resposta.body()!=null)mostrar(resposta.body());
                else falha();
            }
            @Override public void onFailure(Call<DietaSugeridaDTO> call,Throwable erro){
                if(call!=consulta||call.isCanceled()||isFinishing()||isDestroyed())return;consulta=null;falha();
            }
        });
    }
    private void falha(){estado.setText(R.string.dieta_erro);repetir.setVisibility(View.VISIBLE);}
    private void mostrar(DietaSugeridaDTO dieta){
        estado.setVisibility(View.GONE);findViewById(R.id.conteudoDieta).setVisibility(View.VISIBLE);
        ((TextView)findViewById(R.id.nomePerfilDieta)).setText(dieta.perfilNome==null?getString(R.string.dieta_sem_plano):dieta.perfilNome);
        ((TextView)findViewById(R.id.avisoDieta)).setText(dieta.mensagem);
        TextView variante=findViewById(R.id.varianteDieta);
        variante.setVisibility(dieta.varianteNome==null?View.GONE:View.VISIBLE);variante.setText(dieta.varianteNome);
        TextView criterios=findViewById(R.id.criteriosDieta);
        boolean temCriterios=dieta.criteriosAtendidos!=null&&!dieta.criteriosAtendidos.isEmpty();
        criterios.setVisibility(temCriterios?View.VISIBLE:View.GONE);
        if(temCriterios)criterios.setText(getString(R.string.dieta_criterios)+"\n• "+android.text.TextUtils.join("\n• ",dieta.criteriosAtendidos));
        ChipGroup tags=findViewById(R.id.tagsDieta);tags.removeAllViews();
        if(dieta.tagsAlimentares!=null)for(String nome:dieta.tagsAlimentares){
            Chip chip=new Chip(this);chip.setText(nome);chip.setCheckable(false);chip.setClickable(false);tags.addView(chip);
        }
        tags.setVisibility(tags.getChildCount()>0?View.VISIBLE:View.GONE);
        LinearLayout refeicoes=findViewById(R.id.refeicoesDieta);refeicoes.removeAllViews();
        if(dieta.refeicoes!=null)for(DietaSugeridaDTO.RefeicaoPlanejadaDTO r:dieta.refeicoes){
            View card=getLayoutInflater().inflate(R.layout.item_refeicao_planejada,refeicoes,false);
            ((TextView)card.findViewById(R.id.nomeRefeicaoPlanejada)).setText(r.nome);
            ((TextView)card.findViewById(R.id.horarioRefeicaoPlanejada)).setText(r.horario);
            ((TextView)card.findViewById(R.id.descricaoRefeicaoPlanejada)).setText(r.descricao);
            refeicoes.addView(card);
        }
    }
    @Override protected void onDestroy(){if(consulta!=null)consulta.cancel();super.onDestroy();}
}
