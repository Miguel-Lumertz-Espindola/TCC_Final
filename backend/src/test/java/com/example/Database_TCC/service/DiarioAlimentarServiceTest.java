package com.example.Database_TCC.service;
import com.example.Database_TCC.dto.*;
import com.example.Database_TCC.model.*;
import com.example.Database_TCC.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.lang.reflect.Proxy;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class DiarioAlimentarServiceTest {
    private final Usuario ana = usuario(1L, "ana@example.test");
    private final Usuario bia = usuario(2L, "bia@example.test");
    private final Map<Long, RefeicaoRealizada> banco = new HashMap<>();
    private Object[] argumentosConsulta;
    private long proximoId = 1;
    private final UsuarioRepository usuarios = proxy(UsuarioRepository.class, (obj, method, args) -> {
        if (method.getName().equals("findByEmail")) return List.of(ana, bia).stream().filter(u -> u.getEmail().equals(args[0])).findFirst();
        throw new UnsupportedOperationException(method.getName());
    });
    private final RefeicaoRealizadaRepository refeicoes = proxy(RefeicaoRealizadaRepository.class, (obj, method, args) -> {
        switch (method.getName()) {
            case "save": {
                RefeicaoRealizada r = (RefeicaoRealizada)args[0];
                if (r.getId() == null) r.setId(proximoId++);
                banco.put(r.getId(), r); return r;
            }
            case "findByIdAndUsuarioId": {
                RefeicaoRealizada r = banco.get(args[0]);
                return r != null && r.getUsuario().getId().equals(args[1]) ? Optional.of(r) : Optional.empty();
            }
            case "delete": banco.remove(((RefeicaoRealizada)args[0]).getId()); return null;
            case "buscarPorDia": argumentosConsulta = args; return List.of();
            default: throw new UnsupportedOperationException(method.getName());
        }
    });
    private final DiarioAlimentarService diario = new DiarioAlimentarService(usuarios, refeicoes);
    @SuppressWarnings("unchecked") private static <T> T proxy(Class<T> tipo, java.lang.reflect.InvocationHandler h) {
        return (T)Proxy.newProxyInstance(tipo.getClassLoader(), new Class<?>[]{tipo}, h);
    }
    private static Usuario usuario(Long id, String email) { Usuario u=new Usuario(email,"teste");u.setId(id);return u; }
    private RefeicaoRequest dados(String descricao) {
        return new RefeicaoRequest(TipoRefeicao.ALMOCO, LocalDate.now().minusDays(1).atTime(12,30), descricao, "  teste  ");
    }
    @Test void registraConsumoEHorarioDoServidorSeparadamente() {
        RefeicaoDTO r=diario.registrar(ana.getEmail(),dados("  Arroz e feijão  "));
        assertEquals("Arroz e feijão",r.descricao()); assertEquals("teste",r.observacao());
        assertEquals(dados("").dataHoraConsumo(),r.dataHoraConsumo());
        assertTrue(r.dataHoraRegistro().isAfter(r.dataHoraConsumo()));
        assertEquals(ana.getId(),banco.get(r.id()).getUsuario().getId());
    }
    @Test void rejeitaDescricaoVaziaENaoSalva() {
        assertEquals(400,assertThrows(ResponseStatusException.class,()->diario.registrar(ana.getEmail(),dados("  "))).getStatusCode().value());
        assertTrue(banco.isEmpty());
    }
    @Test void rejeitaConsumoFuturo() {
        var pedido=new RefeicaoRequest(TipoRefeicao.JANTAR, LocalDateTime.now().plusDays(1), "teste",null);
        assertEquals(400,assertThrows(ResponseStatusException.class,()->diario.registrar(ana.getEmail(),pedido)).getStatusCode().value());
        assertTrue(banco.isEmpty());
    }
    @Test void consultaUmDiaCompletoSomenteDoUsuario() {
        LocalDate dia=LocalDate.of(2026,1,31);
        assertTrue(diario.listar(ana.getEmail(),dia).isEmpty());
        assertEquals(ana.getId(),argumentosConsulta[0]);
        assertEquals(dia.atStartOfDay(),argumentosConsulta[1]);
        assertEquals(LocalDate.of(2026,2,1).atStartOfDay(),argumentosConsulta[2]);
    }
    @Test void editaSemAlterarRegistroOriginalENaoPermiteOutroUsuario() {
        var criado=diario.registrar(ana.getEmail(),dados("Arroz"));
        assertEquals(404,assertThrows(ResponseStatusException.class,()->diario.editar(bia.getEmail(),criado.id(),dados("Outro"))).getStatusCode().value());
        var editado=diario.editar(ana.getEmail(),criado.id(),dados("Arroz e salada"));
        assertEquals(criado.dataHoraRegistro(),editado.dataHoraRegistro());
        assertEquals("Arroz e salada",editado.descricao());
    }
    @Test void excluiSomenteRefeicaoDoUsuario() {
        var criado=diario.registrar(ana.getEmail(),dados("Arroz"));
        assertEquals(404,assertThrows(ResponseStatusException.class,()->diario.excluir(bia.getEmail(),criado.id())).getStatusCode().value());
        assertEquals(1,banco.size()); diario.excluir(ana.getEmail(),criado.id());assertTrue(banco.isEmpty());
    }
    @Test void usuarioInexistenteNaoPodeRegistrar() {
        assertEquals(404,assertThrows(ResponseStatusException.class,()->diario.registrar("desconhecido@example.test",dados("Teste"))).getStatusCode().value());
        assertTrue(banco.isEmpty());
    }

    private ItemRefeicaoDTO alimento(String nome, String quantidade, UnidadeMedida unidade, String calorias) {
        return new ItemRefeicaoDTO(nome, quantidade == null ? null : new java.math.BigDecimal(quantidade), unidade,
                calorias == null ? null : new java.math.BigDecimal(calorias), null, null, null);
    }
    private RefeicaoRequest comItens(String descricao, List<ItemRefeicaoDTO> itens) {
        return new RefeicaoRequest(TipoRefeicao.ALMOCO, LocalDate.now().minusDays(1).atTime(12,30), descricao, null, itens);
    }
    @Test void registraAlimentoSemNutrientesESemDescricao() {
        var criado=diario.registrar(ana.getEmail(),comItens("",List.of(alimento("  Arroz  ","100",UnidadeMedida.G,null))));
        assertEquals("Arroz",criado.itens().getFirst().nome());
        assertNull(criado.itens().getFirst().calorias());
        assertEquals(criado.id(),banco.get(criado.id()).getItens().getFirst().getRefeicao().getId());
        assertFalse(criado.descricao().isBlank());
    }
    @Test void zeroInformadoNaoViraValorAusente() {
        var criado=diario.registrar(ana.getEmail(),comItens("Bebida",List.of(alimento("Água",null,null,"0"))));
        assertEquals(0,criado.itens().getFirst().calorias().signum());
        assertNull(criado.itens().getFirst().proteinas());assertNull(criado.itens().getFirst().quantidade());
    }
    @Test void validaQuantidadeUnidadeNomeENutrientes() {
        var invalidos=Arrays.asList(alimento("Arroz","100",null,null), alimento("Arroz",null,UnidadeMedida.G,null),
                alimento("Arroz","0",UnidadeMedida.G,null), alimento("Arroz","-1",UnidadeMedida.G,null),
                alimento("Arroz",null,null,"-1"), alimento("Arroz",null,null,"1.123"), alimento(" ",null,null,null),null);
        for (var item:invalidos) {
            assertEquals(400,assertThrows(ResponseStatusException.class,()->diario.registrar(ana.getEmail(),comItens("Teste",Collections.singletonList(item)))).getStatusCode().value());
        }
        assertTrue(banco.isEmpty());
    }
    @Test void rejeitaMaisDeCinquentaAlimentos() {
        var itens=Collections.nCopies(51,alimento("Arroz",null,null,null));
        assertEquals(400,assertThrows(ResponseStatusException.class,()->diario.registrar(ana.getEmail(),comItens("Teste",itens))).getStatusCode().value());
        assertTrue(banco.isEmpty());
    }
    @Test void substituiERemoveAlimentosExplicitamente() {
        var criado=diario.registrar(ana.getEmail(),comItens("Almoço",List.of(alimento("Arroz",null,null,"100"))));
        var editado=diario.editar(ana.getEmail(),criado.id(),comItens("Almoço",List.of(alimento("Feijão",null,null,"50"))));
        assertEquals(1,editado.itens().size());assertEquals("Feijão",editado.itens().getFirst().nome());
        assertEquals(criado.dataHoraRegistro(),editado.dataHoraRegistro());
        assertTrue(diario.editar(ana.getEmail(),criado.id(),comItens("Só descrição",List.of())).itens().isEmpty());
    }
    @Test void clienteSemCampoItensPreservaAlimentosNaEdicao() {
        var criado=diario.registrar(ana.getEmail(),comItens("Almoço",List.of(alimento("Arroz",null,null,"100"))));
        var editado=diario.editar(ana.getEmail(),criado.id(),dados("Descrição atualizada"));
        assertEquals(1,editado.itens().size());assertEquals("Arroz",editado.itens().getFirst().nome());
    }
    @Test void outroUsuarioNaoPodeTrocarAlimentos() {
        var criado=diario.registrar(ana.getEmail(),comItens("Almoço",List.of(alimento("Arroz",null,null,null))));
        assertEquals(404,assertThrows(ResponseStatusException.class,()->diario.editar(bia.getEmail(),criado.id(),comItens("Outro",List.of()))).getStatusCode().value());
        assertEquals(1,banco.get(criado.id()).getItens().size());
    }
}
