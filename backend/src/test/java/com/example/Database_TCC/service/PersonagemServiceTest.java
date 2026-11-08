package com.example.Database_TCC.service;

import com.example.Database_TCC.dto.*;
import com.example.Database_TCC.gamificacao.CatalogoPersonagens;
import com.example.Database_TCC.model.StatusGamificacao;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class PersonagemServiceTest {
    private final CatalogoPersonagens catalogo=new CatalogoPersonagens(30);
    private CheckinServiceTest.Fixture conta(int pontos) {
        var f=new CheckinServiceTest.Fixture();f.atual=new StatusGamificacao(f.usuario);f.atual.setPontuacaoTotal(pontos);return f;
    }
    private PersonagemService service(CheckinServiceTest.Fixture f) {
        return new PersonagemService(f.usuarios,f.status,catalogo,f.em("2026-11-07T12:30:00"));
    }
    private PersonagemDTO mago(StatusGamificacaoDTO s){return s.personagens().stream().filter(p->p.codigo().equals("MAGO")).findFirst().orElseThrow();}

    @Test void magoSoDesbloqueiaAoAtingirTrintaPontos(){
        for(int pontos:new int[]{0,15,29,30,31,300}){
            var f=conta(pontos);var s=f.em("2026-11-07T12:30:00").consultar(f.usuario.getEmail());
            assertEquals(pontos>=30,mago(s).desbloqueado());assertEquals(30,mago(s).pontosNecessarios());
            assertEquals("GUERREIRO",s.personagemSelecionado());
        }
    }
    @Test void guerreiroDisponivelParaContaNovaEContaAntiga(){
        var f=conta(0);f.atual.setPersonagemCodigo(null);
        var r=service(f).selecionar(f.usuario.getEmail(),new PersonagemEscolhaDTO("GUERREIRO"));
        assertEquals("GUERREIRO",r.personagemSelecionado());assertEquals(0,r.pontuacaoTotal());
    }
    @Test void apiRejeitaMagoBloqueadoSemMudarEstado(){
        var f=conta(29);var erro=assertThrows(ResponseStatusException.class,()->service(f).selecionar(f.usuario.getEmail(),new PersonagemEscolhaDTO("MAGO")));
        assertEquals(403,erro.getStatusCode().value());assertEquals(29,f.atual.getPontuacaoTotal());
        assertEquals("GUERREIRO",f.atual.getPersonagemCodigo());assertTrue(f.registros.isEmpty());
    }
    @Test void escolhaPersisteENaoGastaPontosNemAlteraHistorico(){
        var f=conta(30);var antes=LocalDateTime.of(2026,11,6,12,0);f.atual.setUltimoCheckin(antes);
        var r=service(f).selecionar(f.usuario.getEmail(),new PersonagemEscolhaDTO("MAGO"));
        assertEquals("MAGO",r.personagemSelecionado());assertTrue(mago(r).selecionado());assertEquals(30,r.pontuacaoTotal());
        assertEquals(antes,f.atual.getUltimoCheckin());assertTrue(f.registros.isEmpty());
        assertEquals("MAGO",f.em("2026-11-07T19:30:00").consultar(f.usuario.getEmail()).personagemSelecionado());
    }
    @Test void podeVoltarAoGuerreiroSemCusto(){
        var f=conta(30);service(f).selecionar(f.usuario.getEmail(),new PersonagemEscolhaDTO("MAGO"));
        var r=service(f).selecionar(f.usuario.getEmail(),new PersonagemEscolhaDTO("GUERREIRO"));
        assertEquals("GUERREIRO",r.personagemSelecionado());assertEquals(30,r.pontuacaoTotal());assertTrue(mago(r).desbloqueado());
    }
    @Test void checkinLiberaMagoEProximosCheckinsPreservamEscolha(){
        var f=conta(0);var primeiro=f.em("2026-11-07T07:30:00").registrar(f.usuario.getEmail(),"Cafe_da_Manha");
        assertEquals(30,primeiro.status().pontuacaoTotal());assertTrue(mago(primeiro.status()).desbloqueado());
        assertTrue(primeiro.mensagem().contains("Personagem desbloqueado: Mago"));
        service(f).selecionar(f.usuario.getEmail(),new PersonagemEscolhaDTO("MAGO"));
        var almoco=f.em("2026-11-07T13:15:00").registrar(f.usuario.getEmail(),"Almoco");
        assertEquals(45,almoco.status().pontuacaoTotal());assertEquals("MAGO",almoco.status().personagemSelecionado());
        assertFalse(almoco.mensagem().contains("Personagem desbloqueado"));
    }
    @Test void checkinSemPontosNaoDesbloqueiaMago(){
        var f=conta(0);var r=f.em("2026-11-07T17:00:00").registrar(f.usuario.getEmail(),"Almoco");
        assertFalse(mago(r.status()).desbloqueado());assertEquals("GUERREIRO",r.status().personagemSelecionado());
    }
    @Test void validaPersonagemUsuarioEEmail(){
        var f=conta(300);
        assertEquals(400,assertThrows(ResponseStatusException.class,()->service(f).selecionar(f.usuario.getEmail(),new PersonagemEscolhaDTO("DRAGAO"))).getStatusCode().value());
        assertEquals(400,assertThrows(ResponseStatusException.class,()->service(f).selecionar(f.usuario.getEmail(),null)).getStatusCode().value());
        assertEquals(400,assertThrows(ResponseStatusException.class,()->service(f).selecionar("",new PersonagemEscolhaDTO("MAGO"))).getStatusCode().value());
        assertEquals(404,assertThrows(ResponseStatusException.class,()->service(f).selecionar("ausente@example.invalid",new PersonagemEscolhaDTO("MAGO"))).getStatusCode().value());
    }
    @Test void limitePodeSerConfiguradoNoCatalogo(){
        var outro=new CatalogoPersonagens(150);var f=conta(149);
        assertFalse(outro.listar(f.atual).get(1).desbloqueado());f.atual.setPontuacaoTotal(150);
        assertTrue(outro.listar(f.atual).get(1).desbloqueado());
        assertThrows(IllegalArgumentException.class,()->new CatalogoPersonagens(0));
    }
}
