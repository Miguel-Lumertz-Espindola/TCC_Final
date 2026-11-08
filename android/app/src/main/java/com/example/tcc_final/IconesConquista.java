package com.example.tcc_final;
import com.example.tcc_final.dto.ConquistaDTO;
import java.text.Normalizer;
import java.util.Locale;
public final class IconesConquista {
    private IconesConquista() { }
    public static int recurso(ConquistaDTO c) {
        String nome=c.getNome()==null?"":Normalizer.normalize(c.getNome(),Normalizer.Form.NFD).replaceAll("\\p{M}","").toLowerCase(Locale.ROOT).trim();
        switch(nome) {
            case "primeiro passo":return R.drawable.ic_conquista_primeiro_passo;
            case "chama viva":return R.drawable.ic_conquista_chama_viva;
            case "mestre do almoco":return R.drawable.ic_conquista_mestre_almoco;
            case "relogio suico":return R.drawable.ic_conquista_relogio_suico;
            default:return R.drawable.ic_achievement_trophy_24;
        }
    }
}
