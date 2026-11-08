package com.example.Database_TCC.gamificacao;
import java.text.Normalizer;
import java.util.Locale;
public final class RegrasConquistas {
    public static final int DIAS_CHAMA=3, ALMOCOS_MESTRE=7, REGISTROS_RELOGIO=10;
    private RegrasConquistas() { }
    public static String chave(String nome) {
        return nome==null?"":Normalizer.normalize(nome,Normalizer.Form.NFD).replaceAll("\\p{M}","").toLowerCase(Locale.ROOT).trim();
    }
    public static String criterio(String nome,String descricao) {
        return switch(chave(nome)) {
            case "primeiro passo" -> "Faça seu primeiro check-in de refeição na tela do personagem.";
            case "chama viva" -> "Faça pelo menos um check-in com pontos em cada um de "+DIAS_CHAMA+" dias consecutivos. Várias refeições no mesmo dia contam como um único dia.";
            case "mestre do almoco" -> "Acumule "+ALMOCOS_MESTRE+" check-ins de almoço com precisão Exato (30 pontos), dentro da faixa de maior precisão exibida no botão de informação da tela do personagem. Cada almoço conta uma vez por dia.";
            case "relogio suico" -> "Acumule "+REGISTROS_RELOGIO+" check-ins com precisão Exato (30 pontos), no horário correto de café da manhã, almoço ou jantar. Consulte as faixas no botão de informação da tela do personagem.";
            default -> descricao;
        };
    }
}
