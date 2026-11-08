package com.example.Database_TCC.nutricao;

import com.example.Database_TCC.dto.DietaSugeridaDTO.RefeicaoPlanejadaDTO;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.*;

@Component
public class CatalogoDietas {
    public record Documento(String versao, List<Perfil> perfis) { }
    public record Perfil(String codigo, String nome, String objetivo,
            Set<String> niveisAtividade, String praticidade, String rigor,
            String orcamento, java.math.BigDecimal pesoReferenciaKg,
            java.math.BigDecimal alturaReferenciaCm, List<Variante> variantes) { }
    public record Variante(String codigo, String nome, boolean demonstracao,
            Set<TagAlimentar> compatibilidades, Set<String> componentesAlimentares,
            List<RefeicaoPlanejadaDTO> refeicoes) { }

    private final Documento documento;

    @org.springframework.beans.factory.annotation.Autowired
    public CatalogoDietas(ObjectMapper mapper) {
        try (InputStream entrada = getClass().getResourceAsStream("/nutricao/perfis-dietas-v1.json")) {
            if (entrada == null) throw new IllegalStateException("Catálogo de dietas ausente.");
            documento = mapper.readValue(entrada, Documento.class);
            validar(documento);
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Não foi possível ler o catálogo de dietas.", e);
        }
    }

    CatalogoDietas(Documento documento) { validar(documento); this.documento = documento; }
    public String versao() { return documento.versao(); }
    public List<Perfil> perfis() { return documento.perfis(); }

    private static void validar(Documento documento) {
        if (documento == null || documento.versao() == null || documento.versao().isBlank()
                || documento.perfis() == null || documento.perfis().isEmpty())
            throw new IllegalStateException("Catálogo vazio ou sem versão.");
        Set<String> codigos = new HashSet<>();
        for (Perfil p : documento.perfis()) {
            if (p.codigo() == null || p.codigo().isBlank() || !codigos.add(p.codigo())
                    || p.nome() == null || p.nome().isBlank()
                    || !Set.of("MANUTENCAO", "EMAGRECIMENTO", "HIPERTROFIA").contains(p.objetivo())
                    || p.niveisAtividade() == null || p.niveisAtividade().isEmpty()
                    || !Set.of("SEDENTARIO", "LEVE", "MODERADO", "INTENSO").containsAll(p.niveisAtividade())
                    || !Set.of("RAPIDA", "ELABORADA").contains(p.praticidade())
                    || !Set.of("FLEXIVEL", "ESTRUTURADO").contains(p.rigor())
                    || !Set.of("ECONOMICO", "FLEXIVEL").contains(p.orcamento())
                    || p.pesoReferenciaKg() == null || p.pesoReferenciaKg().signum() <= 0
                    || p.alturaReferenciaCm() == null || p.alturaReferenciaCm().signum() <= 0
                    || p.variantes() == null || p.variantes().isEmpty())
                throw new IllegalStateException("Perfil inválido ou repetido: " + p.codigo());
            Set<String> variantes = new HashSet<>();
            for (Variante v : p.variantes()) {
                if (v.codigo() == null || !variantes.add(v.codigo()) || v.compatibilidades() == null
                        || v.componentesAlimentares() == null
                        || v.refeicoes() == null || v.refeicoes().size() != 3)
                    throw new IllegalStateException("Variante inválida: " + p.codigo());
                Set<String> componentes = v.componentesAlimentares();
                if ((v.compatibilidades().contains(TagAlimentar.SEM_GLUTEN) && componentes.contains("GLUTEN"))
                        || (v.compatibilidades().contains(TagAlimentar.SEM_LACTOSE) && componentes.contains("LACTOSE"))
                        || (v.compatibilidades().contains(TagAlimentar.VEGETARIANO) && componentes.contains("CARNE"))
                        || (v.compatibilidades().contains(TagAlimentar.VEGANO)
                            && !Collections.disjoint(componentes, Set.of("ORIGEM_ANIMAL", "CARNE", "LACTOSE"))))
                    throw new IllegalStateException("Tags contradizem a composição da variante: " + p.codigo());
                Set<String> tipos = new HashSet<>();
                for (RefeicaoPlanejadaDTO r : v.refeicoes()) {
                    if (!Set.of("Cafe_da_Manha", "Almoco", "Jantar").contains(r.tipo()) || !tipos.add(r.tipo())
                            || r.nome() == null || r.descricao() == null || r.ingredientes() == null || r.ingredientes().isEmpty())
                        throw new IllegalStateException("Refeição incompleta: " + p.codigo());
                }
            }
        }
    }
}
