package com.example.Database_TCC.service;
import com.example.Database_TCC.dto.*;
import com.example.Database_TCC.model.*;
import com.example.Database_TCC.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class MetasServiceTest {
    private final Usuario ana=usuario(1L,"ana@example.test","EMAGRECIMENTO"),bia=usuario(2L,"bia@example.test","HIPERTROFIA");
    private final List<MetasUsuario> banco=new ArrayList<>();
    private long proximoId=1;
    private final UsuarioRepository usuarios=proxy(UsuarioRepository.class,(obj,metodo,args)->{
        if(metodo.getName().equals("findByEmail")||metodo.getName().equals("buscarParaAtualizarMetas"))
            return List.of(ana,bia).stream().filter(u->u.getEmail().equals(args[0])).findFirst();
        throw new UnsupportedOperationException(metodo.getName());
    });
    private final Comparator<MetasUsuario> ordem=Comparator.comparing(MetasUsuario::getInicioVigencia).thenComparing(MetasUsuario::getId).reversed();
    private final MetasUsuarioRepository metas=proxy(MetasUsuarioRepository.class,(obj,metodo,args)->{
        switch(metodo.getName()) {
            case "save": {MetasUsuario m=(MetasUsuario)args[0];m.setId(proximoId++);banco.add(m);return m;}
            case "findFirstByUsuarioIdAndInicioVigenciaBeforeOrderByInicioVigenciaDescIdDesc":
                return banco.stream().filter(m->m.getUsuario().getId().equals(args[0])&&m.getInicioVigencia().isBefore((LocalDateTime)args[1])).sorted(ordem).findFirst();
            case "findFirstByUsuarioIdOrderByInicioVigenciaDescIdDesc":
                return banco.stream().filter(m->m.getUsuario().getId().equals(args[0])).sorted(ordem).findFirst();
            case "findTop50ByUsuarioIdOrderByInicioVigenciaDescIdDesc":
                return banco.stream().filter(m->m.getUsuario().getId().equals(args[0])).sorted(ordem).limit(50).toList();
            default:throw new UnsupportedOperationException(metodo.getName());
        }
    });
    @SuppressWarnings("unchecked")private static <T>T proxy(Class<T> tipo,java.lang.reflect.InvocationHandler h){return (T)Proxy.newProxyInstance(tipo.getClassLoader(),new Class<?>[]{tipo},h);}
    private static Usuario usuario(Long id,String email,String objetivo){Usuario u=new Usuario(email,"teste");u.setId(id);u.setObjetivo(objetivo);return u;}
    private MetasService service(String instante){return new MetasService(usuarios,metas,Clock.fixed(Instant.parse(instante),ZoneId.of("America/Sao_Paulo")));}
    private MetasRequest dados(String calorias){return new MetasRequest("MANUTENCAO",new BigDecimal(calorias),null,null,null);}
    @Test void consultaSemMetaRetornaObjetivoDoCadastro(){
        var r=service("2026-10-21T13:00:00Z").consultar(ana.getEmail(),null);
        assertEquals("EMAGRECIMENTO",r.objetivo());assertNull(r.meta());
    }
    @Test void salvaValoresOpcionaisSemInventarNutrientes(){
        var r=service("2026-10-20T13:00:00Z").salvar(ana.getEmail(),dados("2000"));
        assertEquals(0,r.calorias().compareTo(new BigDecimal("2000")));assertNull(r.proteinas());assertNull(r.gorduras());
        assertEquals("EMAGRECIMENTO",ana.getObjetivo());
    }
    @Test void alteracaoPreservaMetasDosDiasAnteriores(){
        var antigo=service("2026-10-20T13:00:00Z").salvar(ana.getEmail(),dados("2000"));
        var agora=service("2026-10-21T13:00:00Z");var novo=agora.salvar(ana.getEmail(),dados("2200"));
        assertNotEquals(antigo.id(),novo.id());
        assertEquals(antigo.id(),agora.consultar(ana.getEmail(),LocalDate.of(2026,10,20)).meta().id());
        assertEquals(novo.id(),agora.consultar(ana.getEmail(),null).meta().id());
        assertNull(agora.consultar(ana.getEmail(),LocalDate.of(2026,10,19)).meta());
    }
    @Test void variasAlteracoesNoMesmoDiaUsamAUltima(){
        service("2026-10-21T13:00:00Z").salvar(ana.getEmail(),dados("2000"));
        var agora=service("2026-10-21T15:00:00Z");var novo=agora.salvar(ana.getEmail(),dados("2100"));
        assertEquals(novo.id(),agora.consultar(ana.getEmail(),null).meta().id());assertEquals(2,agora.historico(ana.getEmail()).size());
    }
    @Test void limiteDaMeiaNoiteNaoAplicaMetaDoDiaSeguinte(){
        var primeiro=service("2026-10-21T02:59:59Z").salvar(ana.getEmail(),dados("2000"));
        var agora=service("2026-10-21T03:00:00Z");agora.salvar(ana.getEmail(),dados("2100"));
        assertEquals(primeiro.id(),agora.consultar(ana.getEmail(),LocalDate.of(2026,10,20)).meta().id());
    }
    @Test void salvarOsMesmosValoresNaoDuplicaHistorico(){
        var agora=service("2026-10-21T13:00:00Z");var primeiro=agora.salvar(ana.getEmail(),dados("2000"));
        assertEquals(primeiro.id(),agora.salvar(ana.getEmail(),dados("2000.00")).id());assertEquals(1,banco.size());
    }
    @Test void recusaValoresInvalidosEFormularioVazio(){
        var agora=service("2026-10-21T13:00:00Z");
        for(String valor:List.of("0","-1","1.123","1000000000"))assertEquals(400,assertThrows(ResponseStatusException.class,()->agora.salvar(ana.getEmail(),dados(valor))).getStatusCode().value());
        assertEquals(400,assertThrows(ResponseStatusException.class,()->agora.salvar(ana.getEmail(),new MetasRequest("MANUTENCAO",null,null,null,null))).getStatusCode().value());
        assertEquals(400,assertThrows(ResponseStatusException.class,()->agora.salvar(ana.getEmail(),new MetasRequest("OUTRO",BigDecimal.ONE,null,null,null))).getStatusCode().value());
        assertTrue(banco.isEmpty());
    }
    @Test void consultasEHistoricoSaoSeparadosPorUsuario(){
        var agora=service("2026-10-21T13:00:00Z");agora.salvar(ana.getEmail(),dados("2000"));
        assertTrue(agora.historico(bia.getEmail()).isEmpty());assertNull(agora.consultar(bia.getEmail(),null).meta());
        assertEquals(404,assertThrows(ResponseStatusException.class,()->agora.salvar("ausente@example.test",dados("2000"))).getStatusCode().value());
    }
    @Test void recusaConsultaFutura(){
        assertEquals(400,assertThrows(ResponseStatusException.class,()->service("2026-10-21T13:00:00Z").consultar(ana.getEmail(),LocalDate.of(2026,10,22))).getStatusCode().value());
    }
}
