package com.example.tcc_final.ui.home;
import androidx.lifecycle.*;
import api.ApiClient;
import api.UsuarioService;
import com.example.tcc_final.dto.CheckinRespostaDTO;
import database.MODEL.StatusGamificacao;
import retrofit2.*;

public class HomeViewModel extends ViewModel {
    private final MutableLiveData<StatusGamificacao> status=new MutableLiveData<>();
    private final MutableLiveData<Boolean> carregando=new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> trocandoPersonagem=new MutableLiveData<>(false);
    private final MutableLiveData<String> emAndamento=new MutableLiveData<>(),mensagem=new MutableLiveData<>();
    private final UsuarioService api=ApiClient.getClient().create(UsuarioService.class);
    private String email;
    private Call<StatusGamificacao> consulta,trocaPersonagem;
    private Call<CheckinRespostaDTO> alteracao;
    public LiveData<StatusGamificacao> getStatus(){return status;}
    public LiveData<Boolean> getCarregando(){return carregando;}
    public LiveData<Boolean> getTrocandoPersonagem(){return trocandoPersonagem;}
    public LiveData<String> getEmAndamento(){return emAndamento;}
    public LiveData<String> getMensagem(){return mensagem;}
    public void consumirMensagem(){mensagem.setValue(null);}
    private void sessao(String usuario){
        if(usuario!=null&&usuario.equals(email))return;
        cancelar();email=usuario;trocandoPersonagem.setValue(false);status.setValue(null);emAndamento.setValue(null);mensagem.setValue(null);
    }
    public void carregar(String usuario){
        sessao(usuario);if(email==null||email.isEmpty()||alteracao!=null||trocaPersonagem!=null)return;
        if(consulta!=null)consulta.cancel();carregando.setValue(true);
        consulta=api.buscarStatus(email);consulta.enqueue(new Callback<StatusGamificacao>(){
            @Override public void onResponse(Call<StatusGamificacao> call,Response<StatusGamificacao> r){
                if(call!=consulta)return;consulta=null;carregando.setValue(false);
                if(r.isSuccessful()&&r.body()!=null)status.setValue(r.body());else mensagem.setValue("Não foi possível atualizar o personagem. Tente abrir a aba novamente.");
            }
            @Override public void onFailure(Call<StatusGamificacao> call,Throwable erro){
                if(call!=consulta||call.isCanceled())return;consulta=null;carregando.setValue(false);mensagem.setValue("Falha de conexão ao atualizar o personagem.");
            }
        });
    }
    public void registrar(String tipo){
        if(alteracao!=null||trocaPersonagem!=null||status.getValue()==null||email==null||email.isEmpty())return;
        if(status.getValue().getRefeicoesRegistradasHoje().contains(tipo))return;
        if(consulta!=null){consulta.cancel();consulta=null;}
        carregando.setValue(false);emAndamento.setValue(tipo);
        alteracao=api.realizarCheckinDetalhado(email,tipo);alteracao.enqueue(new Callback<CheckinRespostaDTO>(){
            @Override public void onResponse(Call<CheckinRespostaDTO> call,Response<CheckinRespostaDTO> r){
                if(call!=alteracao)return;alteracao=null;
                if(r.isSuccessful()&&r.body()!=null&&r.body().status!=null){
                    status.setValue(r.body().status);mensagem.setValue(r.body().mensagem);
                }else mensagem.setValue("Não foi possível registrar a refeição. Tente novamente.");
                emAndamento.setValue(null);
            }
            @Override public void onFailure(Call<CheckinRespostaDTO> call,Throwable erro){
                if(call!=alteracao||call.isCanceled())return;alteracao=null;emAndamento.setValue(null);
                mensagem.setValue("Falha de conexão. Confira a API e tente novamente.");
            }
        });
    }

    public void selecionarPersonagem(String codigo){
        StatusGamificacao atual=status.getValue();
        if(trocaPersonagem!=null||alteracao!=null||atual==null||email==null||email.isEmpty())return;
        var opcao=atual.getPersonagem(codigo);
        if(opcao==null){mensagem.setValue("Não foi possível consultar os personagens. Abra a aba novamente.");return;}
        if(!opcao.desbloqueado){mensagem.setValue("Faltam "+Math.max(0,opcao.pontosNecessarios-atual.getPontuacaoTotal())+" pontos para desbloquear "+opcao.nome+".");return;}
        if(opcao.selecionado)return;
        if(consulta!=null){consulta.cancel();consulta=null;}
        carregando.setValue(false);trocandoPersonagem.setValue(true);
        trocaPersonagem=api.selecionarPersonagem(email,new com.example.tcc_final.dto.PersonagemEscolhaDTO(codigo));
        trocaPersonagem.enqueue(new Callback<StatusGamificacao>(){
            @Override public void onResponse(Call<StatusGamificacao> call,Response<StatusGamificacao> resposta){
                if(call!=trocaPersonagem)return;trocaPersonagem=null;
                if(resposta.isSuccessful()&&resposta.body()!=null){
                    status.setValue(resposta.body());mensagem.setValue(opcao.nome+" selecionado.");
                }else mensagem.setValue(resposta.code()==403?"Este personagem ainda está bloqueado. Atualize a pontuação e tente novamente.":"Não foi possível salvar o personagem. Tente novamente.");
                trocandoPersonagem.setValue(false);
            }
            @Override public void onFailure(Call<StatusGamificacao> call,Throwable erro){
                if(call!=trocaPersonagem||call.isCanceled())return;trocaPersonagem=null;trocandoPersonagem.setValue(false);
                mensagem.setValue("Falha de conexão ao trocar o personagem. Tente novamente.");
            }
        });
    }
    private void cancelar(){
        if(consulta!=null)consulta.cancel();if(alteracao!=null)alteracao.cancel();if(trocaPersonagem!=null)trocaPersonagem.cancel();
        consulta=null;alteracao=null;trocaPersonagem=null;
    }
    @Override protected void onCleared(){cancelar();}
}
