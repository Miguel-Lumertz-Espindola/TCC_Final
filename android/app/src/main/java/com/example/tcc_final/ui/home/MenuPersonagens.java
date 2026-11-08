package com.example.tcc_final.ui.home;

import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.view.*;
import android.widget.*;
import com.example.tcc_final.R;
import com.example.tcc_final.dto.PersonagemDTO;
import database.MODEL.StatusGamificacao;

final class MenuPersonagens extends PopupWindow {
    interface AoSelecionar { void selecionar(String codigo); }
    private final Context contexto;
    private final View conteudo;
    private final AoSelecionar listener;
    private StatusGamificacao status;
    private boolean ocupado;

    MenuPersonagens(Context contexto,AoSelecionar listener){
        super(contexto);this.contexto=contexto;this.listener=listener;
        conteudo=LayoutInflater.from(contexto).inflate(R.layout.popup_personagens,null);
        setContentView(conteudo);
        int margem=dp(32);
        setWidth(Math.min(dp(288),contexto.getResources().getDisplayMetrics().widthPixels-margem));
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);setFocusable(true);setOutsideTouchable(true);
        setInputMethodMode(PopupWindow.INPUT_METHOD_NOT_NEEDED);
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));setElevation(dp(12));
        preparar(R.id.opcaoGuerreiro,R.id.iconeGuerreiro,"GUERREIRO");
        preparar(R.id.opcaoMago,R.id.iconeMago,"MAGO");
    }
    private int dp(int valor){return Math.round(valor*contexto.getResources().getDisplayMetrics().density);}
    private void preparar(int id,int imagemId,String codigo){
        ImageView imagem=conteudo.findViewById(imagemId);imagem.setImageResource(PersonagensVisuais.icone(codigo));
        if(imagem.getDrawable() instanceof BitmapDrawable)((BitmapDrawable)imagem.getDrawable().mutate()).setFilterBitmap(false);
        conteudo.findViewById(id).setOnClickListener(v->{
            if(ocupado)return;
            PersonagemDTO opcao=status==null?null:status.getPersonagem(codigo);
            if(opcao==null){Toast.makeText(contexto,R.string.personagens_indisponiveis,Toast.LENGTH_LONG).show();return;}
            if(opcao.selecionado){dismiss();return;}
            listener.selecionar(codigo);
            if(opcao.desbloqueado)dismiss();
        });
    }
    void atualizar(StatusGamificacao status,boolean ocupado){
        this.status=status;this.ocupado=ocupado;
        atualizarOpcao("GUERREIRO",R.id.opcaoGuerreiro,R.id.iconeGuerreiro,R.id.estadoGuerreiro,R.id.requisitoGuerreiro);
        atualizarOpcao("MAGO",R.id.opcaoMago,R.id.iconeMago,R.id.estadoMago,R.id.requisitoMago);
        TextView ajuda=conteudo.findViewById(R.id.ajudaMenuPersonagens);
        ajuda.setText(ocupado?R.string.personagens_salvando:status==null||status.getPersonagens().isEmpty()?R.string.personagens_indisponiveis:R.string.personagens_ajuda);
    }
    private void atualizarOpcao(String codigo,int cardId,int imagemId,int estadoId,int requisitoId){
        PersonagemDTO p=status==null?null:status.getPersonagem(codigo);
        View card=conteudo.findViewById(cardId);ImageView imagem=conteudo.findViewById(imagemId);
        TextView estado=conteudo.findViewById(estadoId),requisito=conteudo.findViewById(requisitoId);
        boolean livre=p!=null&&p.desbloqueado,selecionado=p!=null&&p.selecionado;
        card.setSelected(selecionado);card.setEnabled(!ocupado);
        if(livre){imagem.clearColorFilter();imagem.setAlpha(1f);}
        else{ColorMatrix cinza=new ColorMatrix();cinza.setSaturation(0);imagem.setColorFilter(new ColorMatrixColorFilter(cinza));imagem.setAlpha(.4f);}
        int situacao=p==null?R.string.personagens_consultando:selecionado?R.string.personagem_em_uso:livre?R.string.personagem_disponivel:R.string.personagem_bloqueado;
        estado.setText(situacao);
        estado.setTextColor(contexto.getColor(selecionado?R.color.ranking_accent:R.color.ranking_text_secondary));
        requisito.setVisibility(p==null||p.pontosNecessarios==0?View.GONE:View.VISIBLE);
        if(p!=null&&p.pontosNecessarios>0){
            String meta=contexto.getString(R.string.personagem_meta,p.pontosNecessarios);
            requisito.setText(livre?meta:meta+"\n"+contexto.getString(R.string.personagem_faltam,Math.max(0,p.pontosNecessarios-status.getPontuacaoTotal())));
        }
        String descricao=PersonagensVisuais.nome(codigo)+". "+estado.getText()+". "+(requisito.getVisibility()==View.VISIBLE?requisito.getText():"");
        card.setContentDescription(descricao);
    }
    void abrir(View ancora){showAsDropDown(ancora,0,dp(4),Gravity.END);}
}
