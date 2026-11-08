package com.example.Database_TCC.nutricao;

import com.example.Database_TCC.model.Usuario;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class SeletorDietaTest {
    private final CatalogoDietas catalogo=new CatalogoDietas(JsonMapper.builder().build());
    private Usuario usuario(String objetivo, boolean pratico, Set<TagAlimentar> tags) {
        Usuario u=new Usuario();u.setObjetivo(objetivo);
        u.setNivelAtividade(pratico?"LEVE":"INTENSO");
        u.setPraticidade(pratico?"RAPIDA":"ELABORADA");
        u.setOrcamento(pratico?"ECONOMICO":"FLEXIVEL");
        u.setPesoInicialKg(new java.math.BigDecimal(pratico?"80":"60"));
        u.setAlturaCm(new java.math.BigDecimal(pratico?"175":"160"));
        u.setRigor(pratico?"FLEXIVEL":"ESTRUTURADO");u.setTagsAlimentares(tags);return u;
    }
    @Test void seisPerfisSelecionaveisComTresRefeicoesCada() {
        assertEquals(6,catalogo.perfis().size());
        for(String objetivo:List.of("MANUTENCAO","EMAGRECIMENTO","HIPERTROFIA"))
            for(boolean pratico:List.of(true,false)) {
                var r=new SeletorDieta(catalogo).selecionar(usuario(objetivo,pratico,Set.of()));
                assertEquals(objetivo+"_"+(pratico?"PRATICO":"VARIADO"),r.perfilCodigo());
                assertEquals("A_TRADICIONAL",r.varianteCodigo());assertTrue(r.demonstracao());
                assertEquals(100,r.pontosCompatibilidade());assertEquals(3,r.refeicoes().size());
            }
    }
    @Test void todasAsCombinacoesDeTagsSaoAtendidasJuntas() {
        TagAlimentar[] tags=TagAlimentar.values();
        for(int mascara=1;mascara<(1<<tags.length);mascara++) {
            Set<TagAlimentar> selecao=new HashSet<>();
            for(int i=0;i<tags.length;i++)if((mascara&(1<<i))!=0)selecao.add(tags[i]);
            var r=new SeletorDieta(catalogo).selecionar(usuario("MANUTENCAO",true,selecao));
            assertEquals("B_VEGETAL_SEM_GLUTEN",r.varianteCodigo());
            assertEquals(selecao.size(),r.tagsAlimentares().size());
            var variante=catalogo.perfis().stream().filter(p->p.codigo().equals(r.perfilCodigo())).findFirst().orElseThrow()
                    .variantes().stream().filter(v->v.codigo().equals(r.varianteCodigo())).findFirst().orElseThrow();
            assertTrue(variante.compatibilidades().containsAll(selecao));
        }
    }
    @Test void jamaisIgnoraTagsQuandoNaoExistePlanoCompativel() {
        var p=catalogo.perfis().getFirst();
        var reduzido=new CatalogoDietas(new CatalogoDietas.Documento("teste",List.of(
                new CatalogoDietas.Perfil(p.codigo(),p.nome(),p.objetivo(),p.niveisAtividade(),p.praticidade(),p.rigor(),p.orcamento(),p.pesoReferenciaKg(),p.alturaReferenciaCm(),List.of(p.variantes().getFirst())))));
        var r=new SeletorDieta(reduzido).selecionar(usuario(p.objetivo(),true,Set.of(TagAlimentar.VEGANO)));
        assertEquals("SEM_COMPATIVEL",r.situacao());assertNull(r.perfilCodigo());assertTrue(r.refeicoes().isEmpty());
    }
    @Test void objetivoDiferenteNaoEntraComoAlternativa() {
        var p=catalogo.perfis().getFirst();
        var reduzido=new CatalogoDietas(new CatalogoDietas.Documento("teste",List.of(p)));
        assertEquals("SEM_COMPATIVEL",new SeletorDieta(reduzido).selecionar(usuario("HIPERTROFIA",true,Set.of())).situacao());
    }
    @Test void criteriosIndividuaisAlteramPontuacaoSemConfundirComPrecisaoDoCheckin() {
        Usuario u=usuario("MANUTENCAO",true,Set.of());
        u.setRigor("ESTRUTURADO");
        var r=new SeletorDieta(catalogo).selecionar(u);
        assertEquals("MANUTENCAO_PRATICO",r.perfilCodigo());assertEquals(90,r.pontosCompatibilidade());
        assertFalse(r.criteriosAtendidos().contains("Organização da rotina"));
        u.setNivelAtividade("INTENSO");u.setPraticidade("ELABORADA");u.setPesoInicialKg(new java.math.BigDecimal("60"));u.setAlturaCm(new java.math.BigDecimal("160"));
        assertEquals("MANUTENCAO_VARIADO",new SeletorDieta(catalogo).selecionar(u).perfilCodigo());
    }
    @Test void empateIndependeDaOrdemDoCatalogo() {
        Usuario u=usuario("MANUTENCAO",true,Set.of());u.setPraticidade("ELABORADA");u.setRigor("ESTRUTURADO");
        // Um critério novo pode criar empate; códigos definem uma ordem total estável.
        var esperado=new SeletorDieta(catalogo).selecionar(u);
        var lista=new ArrayList<>(catalogo.perfis());Collections.reverse(lista);
        var invertido=new CatalogoDietas(new CatalogoDietas.Documento("teste",lista));
        assertEquals(esperado.perfilCodigo(),new SeletorDieta(invertido).selecionar(u).perfilCodigo());
    }
    @Test void composicaoContraditoriaNaoPodeReceberTagSemLactose() {
        var p=catalogo.perfis().getFirst();var v=p.variantes().getFirst();
        var invalida=new CatalogoDietas.Variante(v.codigo(),v.nome(),true,Set.of(TagAlimentar.SEM_LACTOSE),Set.of("LACTOSE"),v.refeicoes());
        var perfil=new CatalogoDietas.Perfil(p.codigo(),p.nome(),p.objetivo(),p.niveisAtividade(),p.praticidade(),p.rigor(),p.orcamento(),p.pesoReferenciaKg(),p.alturaReferenciaCm(),List.of(invalida));
        assertThrows(IllegalStateException.class,()->new CatalogoDietas(new CatalogoDietas.Documento("teste",List.of(perfil))));
    }
    private Usuario criteriosDivididos() {
        Usuario u=usuario("MANUTENCAO",true,Set.of());
        u.setOrcamento("FLEXIVEL");u.setRigor("ESTRUTURADO");return u;
    }
    @Test void mudarSomentePesoPodeMudarPerfil() {
        Usuario u=criteriosDivididos();u.setAlturaCm(new java.math.BigDecimal("160"));
        assertEquals("MANUTENCAO_PRATICO",new SeletorDieta(catalogo).selecionar(u).perfilCodigo());
        u.setPesoInicialKg(new java.math.BigDecimal("60"));
        assertEquals("MANUTENCAO_VARIADO",new SeletorDieta(catalogo).selecionar(u).perfilCodigo());
    }
    @Test void mudarSomenteAlturaPodeMudarPerfil() {
        Usuario u=criteriosDivididos();u.setPesoInicialKg(new java.math.BigDecimal("60"));
        assertEquals("MANUTENCAO_PRATICO",new SeletorDieta(catalogo).selecionar(u).perfilCodigo());
        u.setAlturaCm(new java.math.BigDecimal("160"));
        assertEquals("MANUTENCAO_VARIADO",new SeletorDieta(catalogo).selecionar(u).perfilCodigo());
    }
    @Test void idadeNaoParticipaDaClassificacao() {
        Usuario u=usuario("MANUTENCAO",true,Set.of());u.setDataNascimento(java.time.LocalDate.of(1995,1,1));
        var antes=new SeletorDieta(catalogo).selecionar(u);u.setDataNascimento(java.time.LocalDate.of(1970,1,1));
        assertEquals(antes,new SeletorDieta(catalogo).selecionar(u));
    }
}
