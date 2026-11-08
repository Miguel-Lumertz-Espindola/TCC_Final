package com.example.Database_TCC.gamificacao;

import com.example.Database_TCC.dto.PersonagemDTO;
import com.example.Database_TCC.model.StatusGamificacao;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import java.util.*;

@Component
public class CatalogoPersonagens {
    public static final String PADRAO="GUERREIRO";
    public record Definicao(String codigo,String nome,int pontosNecessarios) { }
    private final List<Definicao> personagens;

    public CatalogoPersonagens(@Value("${gamificacao.personagens.pontos-mago:30}") int pontosMago) {
        if(pontosMago<=0)throw new IllegalArgumentException("O mago deve exigir uma pontuação positiva.");
        personagens=List.of(new Definicao(PADRAO,"Guerreiro",0),new Definicao("MAGO","Mago",pontosMago));
    }
    public Definicao buscar(String codigo) {
        return personagens.stream().filter(p->p.codigo().equals(codigo)).findFirst().orElse(null);
    }
    public int pontos(StatusGamificacao status) {
        return status.getPontuacaoTotal()==null?0:Math.max(0,status.getPontuacaoTotal());
    }
    public String selecionado(StatusGamificacao status) {
        Definicao atual=buscar(status.getPersonagemCodigo());
        return atual!=null&&pontos(status)>=atual.pontosNecessarios()?atual.codigo():PADRAO;
    }
    public List<PersonagemDTO> listar(StatusGamificacao status) {
        String atual=selecionado(status);int pontos=pontos(status);
        return personagens.stream().map(p->new PersonagemDTO(p.codigo(),p.nome(),p.pontosNecessarios(),
                pontos>=p.pontosNecessarios(),p.codigo().equals(atual))).toList();
    }
    public List<String> novosDesbloqueios(int antes,int depois) {
        return personagens.stream().filter(p->p.pontosNecessarios()>0&&antes<p.pontosNecessarios()&&depois>=p.pontosNecessarios())
                .map(Definicao::nome).toList();
    }
}
