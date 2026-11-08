package com.example.Database_TCC.service;

import com.example.Database_TCC.dto.*;
import com.example.Database_TCC.model.*;
import com.example.Database_TCC.nutricao.*;
import com.example.Database_TCC.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.json.JsonMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class CadastroServiceTest {
    private final List<Usuario> gravados=new ArrayList<>();
    private final List<DietaUsuario> dietasGravadas=new ArrayList<>();
    private final Map<String,Usuario> existentes=new HashMap<>();
    private DietaUsuario snapshot;
    private int mascotes;
    @SuppressWarnings("unchecked") private static <T>T proxy(Class<T> c,java.lang.reflect.InvocationHandler h){
        return (T)java.lang.reflect.Proxy.newProxyInstance(c.getClassLoader(),new Class<?>[]{c},h);
    }
    private final UsuarioRepository usuarios=proxy(UsuarioRepository.class,(o,m,a)->{
        if(m.getName().equals("findByEmail"))return Optional.ofNullable(existentes.get(a[0]));
        if(m.getName().equals("saveAndFlush")){Usuario u=(Usuario)a[0];u.setId(1L);gravados.add(u);return u;}
        throw new UnsupportedOperationException(m.getName());
    });
    private final StatusGamificacaoRepository status=proxy(StatusGamificacaoRepository.class,(o,m,a)->{
        if(m.getName().equals("save")){mascotes++;return a[0];}throw new UnsupportedOperationException(m.getName());
    });
    private final DietaUsuarioRepository dietas=proxy(DietaUsuarioRepository.class,(o,m,a)->{
        if(m.getName().equals("save")){dietasGravadas.add((DietaUsuario)a[0]);return a[0];}
        if(m.getName().equals("findByUsuarioId"))return Optional.ofNullable(snapshot);
        throw new UnsupportedOperationException(m.getName());
    });
    private final JsonMapper mapper=JsonMapper.builder().build();
    private final CadastroService service=new CadastroService(usuarios,status,dietas,new SeletorDieta(new CatalogoDietas(mapper)),mapper);
    private CadastroRequest dados(List<String> tags,String praticidade) {
        return new CadastroRequest(" Teste ","teste@example.invalid","senha-teste",LocalDate.of(1995,1,1),
                new BigDecimal("170"),new BigDecimal("70"),"MANUTENCAO","LEVE",praticidade,"FLEXIVEL","ECONOMICO",tags,null,null);
    }
    @Test void cadastroSalvaTagsMascoteESnapshotDaDietaJuntos() {
        var resposta=service.cadastrar(dados(List.of("SEM_LACTOSE","SEM_GLUTEN","VEGANO"),"RAPIDA"));
        assertEquals(1,gravados.size());assertEquals(3,gravados.getFirst().getTagsAlimentares().size());
        assertEquals("Teste",gravados.getFirst().getNome());assertEquals(1,mascotes);
        assertEquals(1,dietasGravadas.size());assertSame(gravados.getFirst(),dietasGravadas.getFirst().getUsuario());
        assertEquals(resposta,mapper.readValue(dietasGravadas.getFirst().getSelecaoJson(),DietaSugeridaDTO.class));
    }
    @Test void tagDesconhecidaRejeitaCadastroSemGravarNada() {
        assertThrows(ResponseStatusException.class,()->service.cadastrar(dados(List.of("SEM_ACUCAR"),"RAPIDA")));
        assertTrue(gravados.isEmpty());assertTrue(dietasGravadas.isEmpty());assertEquals(0,mascotes);
    }
    @Test void opcaoInvalidaRejeitaCadastro() {
        assertThrows(ResponseStatusException.class,()->service.cadastrar(dados(List.of(),"INVENTADA")));
        assertTrue(gravados.isEmpty());
    }
    @Test void tagsRepetidasSaoNormalizadas() {
        assertEquals(List.of("Vegano"),service.cadastrar(dados(List.of("VEGANO","VEGANO"),"RAPIDA")).tagsAlimentares());
    }
    @Test void tagsOpcionaisENovoCampoAusentePreservamClienteAnterior() {
        assertEquals("A_TRADICIONAL",service.cadastrar(dados(null,null)).varianteCodigo());
    }
    @Test void emailJaCadastradoNaoGeraOutroMascoteOuDieta() {
        existentes.put("teste@example.invalid",new Usuario());
        assertThrows(ResponseStatusException.class,()->service.cadastrar(dados(List.of(),"RAPIDA")));
        assertTrue(gravados.isEmpty());assertTrue(dietasGravadas.isEmpty());assertEquals(0,mascotes);
    }
    @Test void contaAnteriorNaoRecebeDietaSemRespostasDoNovoPerfil() {
        Usuario u=new Usuario();u.setId(8L);u.setObjetivo("MANUTENCAO");existentes.put("antigo@example.invalid",u);
        assertEquals("CADASTRO_ANTERIOR",service.consultar("antigo@example.invalid").situacao());
        assertTrue(dietasGravadas.isEmpty());
    }
    @Test void consultaPreservaSnapshotMesmoSeCatalogoCrescer() {
        Usuario u=new Usuario();u.setId(8L);existentes.put("teste@example.invalid",u);
        var original=new DietaSugeridaDTO("SUGERIDA","Guardado","v-antiga","PERFIL-ANTIGO","Nome antigo","MANUTENCAO","v1","Menu antigo",true,42,List.of("Original"),List.of(),List.of());
        snapshot=new DietaUsuario(u,mapper.writeValueAsString(original));
        assertEquals(original,service.consultar("teste@example.invalid"));
    }
    @Test void observacaoLegadaNaoPodeSerIgnoradaNaSugestao() {
        CadastroRequest base=dados(List.of(),"RAPIDA");
        CadastroRequest comTexto=new CadastroRequest(base.nome(),base.email(),base.senha(),base.dataNascimento(),base.alturaCm(),base.pesoInicialKg(),base.objetivo(),base.nivelAtividade(),base.praticidade(),base.rigor(),base.orcamento(),base.tagsAlimentares(),"Alergia ao leite",null);
        var resposta=service.cadastrar(comTexto);assertEquals("OBSERVACOES_PENDENTES",resposta.situacao());
        assertTrue(resposta.refeicoes().isEmpty());assertEquals(1,gravados.size());
    }
}
