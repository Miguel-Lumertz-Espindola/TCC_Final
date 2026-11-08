package com.example.tcc_final.ui.home;
import com.example.tcc_final.R;

final class PersonagensVisuais {
    private PersonagensVisuais(){ }
    static int animacao(String codigo){return "MAGO".equals(codigo)?R.raw.personagem_mago:R.raw.personagem_padrao;}
    static int icone(String codigo){return "MAGO".equals(codigo)?R.drawable.personagem_mago_icone:R.drawable.personagem_guerreiro_icone;}
    static String nome(String codigo){return "MAGO".equals(codigo)?"Mago":"Guerreiro";}
}
