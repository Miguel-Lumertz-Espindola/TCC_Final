package com.example.tcc_final;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.example.tcc_final.dto.*;
import com.example.tcc_final.nutricao.TextoNutricional;
import api.ApiClient;
import api.MetasService;
import retrofit2.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

public class MetasActivity extends AppCompatActivity {
    private final String[] objetivos={null,"MANUTENCAO","EMAGRECIMENTO","HIPERTROFIA"};
    private MetasService api;
    private String email;
    private Spinner objetivo;
    private Button salvar,repetir;
    private TextView status;
    private boolean carregado,enviando,preservarCampos;
    private Call<MetasConsultaDTO> consulta;
    private Call<List<MetasDTO>> historico;
    private Call<MetasDTO> alteracao;
    private boolean encerrada(){return isFinishing()||isDestroyed();}
    @Override protected void onCreate(Bundle estado){
        super.onCreate(estado);setContentView(R.layout.activity_metas);
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content),(v,insets)->{
            androidx.core.graphics.Insets b=insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(b.left,b.top,b.right,b.bottom);return insets;
        });
        MaterialToolbar barra=findViewById(R.id.toolbarMetas);
        barra.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        barra.setNavigationIconTint(getColor(R.color.ranking_accent));barra.setNavigationContentDescription(R.string.metas_back);
        barra.setNavigationOnClickListener(v->{if(!enviando)finish();});
        email=getSharedPreferences("SessaoUsuario",MODE_PRIVATE).getString("email_ativo","");
        if(email==null||email.isEmpty()){Toast.makeText(this,R.string.diario_session,Toast.LENGTH_LONG).show();finish();return;}
        api=ApiClient.getClient().create(MetasService.class);
        objetivo=findViewById(R.id.objetivoMetas);
        ArrayAdapter<CharSequence> opcoes=ArrayAdapter.createFromResource(this,R.array.metas_objectives,android.R.layout.simple_spinner_item);
        opcoes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);objetivo.setAdapter(opcoes);
        salvar=findViewById(R.id.salvarMetas);repetir=findViewById(R.id.repetirMetas);status=findViewById(R.id.statusMetas);
        preservarCampos=estado!=null&&estado.getBoolean("carregado",false);
        salvar.setOnClickListener(v->salvar());repetir.setOnClickListener(v->carregar(!preservarCampos));
        carregar(!preservarCampos);
    }
    @Override protected void onSaveInstanceState(Bundle estado){estado.putBoolean("carregado",carregado);super.onSaveInstanceState(estado);}
    private void habilitar(View view,boolean ativo){
        view.setEnabled(ativo);
        if(view instanceof ViewGroup){ViewGroup grupo=(ViewGroup)view;for(int i=0;i<grupo.getChildCount();i++)habilitar(grupo.getChildAt(i),ativo);}
    }
    private void preencher(int id,BigDecimal valor){((TextInputEditText)findViewById(id)).setText(valor==null?"":valor.stripTrailingZeros().toPlainString());}
    private void preencher(MetasConsultaDTO resposta){
        String selecionado=resposta.meta==null?resposta.objetivo:resposta.meta.objetivo;
        objetivo.setSelection(0);for(int i=1;i<objetivos.length;i++)if(objetivos[i].equals(selecionado))objetivo.setSelection(i);
        MetasDTO m=resposta.meta;
        preencher(R.id.caloriasMetas,m==null?null:m.calorias);preencher(R.id.proteinasMetas,m==null?null:m.proteinas);
        preencher(R.id.carboidratosMetas,m==null?null:m.carboidratos);preencher(R.id.gordurasMetas,m==null?null:m.gorduras);
    }
    private String mensagem(Response<?> resposta){
        String padrao=getString(R.string.diario_server_error,resposta.code());
        try{if(resposta.errorBody()==null)return padrao;String texto=resposta.errorBody().string();
            if(texto.startsWith("{")){String detalhe=new org.json.JSONObject(texto).optString("message","");return detalhe.isEmpty()?padrao:detalhe;}
            return texto.isEmpty()?padrao:texto;
        }catch(Exception e){return padrao;}
    }
    private String data(String iso){
        if(iso==null)return "";
        try{Date d=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm",Locale.ROOT).parse(iso);if(d!=null)return new SimpleDateFormat("dd/MM/yyyy HH:mm",Locale.getDefault()).format(d);}
        catch(java.text.ParseException ignored){}return iso;
    }
    private void carregar(boolean preencher){
        if(consulta!=null)consulta.cancel();
        status.setText(R.string.metas_loading);repetir.setVisibility(View.GONE);habilitar(findViewById(R.id.formMetas),false);
        consulta=api.consultar(email,null);consulta.enqueue(new Callback<MetasConsultaDTO>(){
            @Override public void onResponse(Call<MetasConsultaDTO> call,Response<MetasConsultaDTO> resposta){
                if(encerrada()||call!=consulta)return;
                if(!resposta.isSuccessful()||resposta.body()==null){falha(mensagem(resposta));return;}
                if(preencher)preencher(resposta.body());
                carregado=true;preservarCampos=true;habilitar(findViewById(R.id.formMetas),true);
                status.setText(resposta.body().meta==null?getString(R.string.metas_history_empty):getString(R.string.metas_since,data(resposta.body().meta.inicioVigencia)));
            }
            @Override public void onFailure(Call<MetasConsultaDTO> call,Throwable erro){if(!encerrada()&&!call.isCanceled())falha(getString(R.string.diario_connection));}
        });
        carregarHistorico();
    }
    private void falha(String mensagem){status.setText(mensagem);repetir.setVisibility(View.VISIBLE);}
    private void carregarHistorico(){
        if(historico!=null)historico.cancel();
        TextView info=findViewById(R.id.statusHistoricoMetas);info.setText(R.string.metas_loading);
        LinearLayout linhas=findViewById(R.id.historicoMetas);linhas.removeAllViews();
        historico=api.historico(email);historico.enqueue(new Callback<List<MetasDTO>>(){
            @Override public void onResponse(Call<List<MetasDTO>> call,Response<List<MetasDTO>> resposta){
                if(encerrada()||call!=historico)return;
                if(!resposta.isSuccessful()||resposta.body()==null){info.setText(R.string.metas_history_error);return;}
                info.setText(resposta.body().isEmpty()?R.string.metas_history_empty:R.string.metas_vigencia_help);
                String[] nomes=getResources().getStringArray(R.array.metas_objectives);
                for(MetasDTO m:resposta.body()){
                    View linha=getLayoutInflater().inflate(R.layout.item_historico_metas,linhas,false);
                    String nome=m.objetivo;for(int i=1;i<objetivos.length;i++)if(objetivos[i].equals(m.objetivo))nome=nomes[i];
                    ((TextView)linha.findViewById(R.id.tituloHistoricoMetas)).setText(getString(R.string.metas_history_item,data(m.inicioVigencia),nome));
                    ((TextView)linha.findViewById(R.id.valoresHistoricoMetas)).setText(TextoNutricional.metas(MetasActivity.this,m));linhas.addView(linha);
                }
            }
            @Override public void onFailure(Call<List<MetasDTO>> call,Throwable erro){if(!encerrada()&&!call.isCanceled())info.setText(R.string.metas_history_error);}
        });
    }
    private BigDecimal numero(int id){
        TextInputEditText campo=findViewById(id);String texto=campo.getText()==null?"":campo.getText().toString().trim();if(texto.isEmpty())return null;
        try{BigDecimal v=new BigDecimal(texto.replace(',','.'));
            if(v.signum()<=0||v.compareTo(new BigDecimal("999999999"))>0||v.stripTrailingZeros().scale()>2)throw new NumberFormatException();return v;
        }catch(NumberFormatException e){campo.setError(getString(R.string.metas_invalid));campo.requestFocus();throw new IllegalArgumentException();}
    }
    private void salvar(){
        if(!carregado||enviando)return;
        if(objetivo.getSelectedItemPosition()==0){Toast.makeText(this,R.string.metas_objective_required,Toast.LENGTH_LONG).show();return;}
        MetasDTO m=new MetasDTO();m.objetivo=objetivos[objetivo.getSelectedItemPosition()];
        try{m.calorias=numero(R.id.caloriasMetas);m.proteinas=numero(R.id.proteinasMetas);m.carboidratos=numero(R.id.carboidratosMetas);m.gorduras=numero(R.id.gordurasMetas);}catch(IllegalArgumentException e){return;}
        if(m.calorias==null&&m.proteinas==null&&m.carboidratos==null&&m.gorduras==null){Toast.makeText(this,R.string.metas_at_least_one,Toast.LENGTH_LONG).show();return;}
        enviando=true;habilitar(findViewById(R.id.formMetas),false);salvar.setText(R.string.diario_saving);
        alteracao=api.salvar(email,m);alteracao.enqueue(new Callback<MetasDTO>(){
            @Override public void onResponse(Call<MetasDTO> call,Response<MetasDTO> resposta){
                if(encerrada())return;enviando=false;salvar.setText(R.string.metas_save);
                if(resposta.isSuccessful()){Toast.makeText(MetasActivity.this,R.string.metas_saved,Toast.LENGTH_SHORT).show();carregar(true);}
                else{habilitar(findViewById(R.id.formMetas),true);Toast.makeText(MetasActivity.this,mensagem(resposta),Toast.LENGTH_LONG).show();}
            }
            @Override public void onFailure(Call<MetasDTO> call,Throwable erro){
                if(encerrada()||call.isCanceled())return;enviando=false;habilitar(findViewById(R.id.formMetas),true);salvar.setText(R.string.metas_save);
                Toast.makeText(MetasActivity.this,R.string.diario_connection,Toast.LENGTH_LONG).show();
            }
        });
    }
    @Override protected void onDestroy(){if(consulta!=null)consulta.cancel();if(historico!=null)historico.cancel();if(alteracao!=null)alteracao.cancel();super.onDestroy();}
}
