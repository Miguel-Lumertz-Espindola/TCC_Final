package com.example.Database_TCC.nutricao;

import com.example.Database_TCC.dto.DietaSugeridaDTO;
import com.example.Database_TCC.model.Usuario;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class SeletorDieta {
    private final CatalogoDietas catalogo;
    public SeletorDieta(CatalogoDietas catalogo) { this.catalogo = catalogo; }
    private record Candidato(CatalogoDietas.Perfil perfil, CatalogoDietas.Variante variante,
            double pontos, List<String> criterios) { }

    public DietaSugeridaDTO selecionar(Usuario usuario) {
        Set<TagAlimentar> tags = usuario.getTagsAlimentares();
        List<Candidato> candidatos = new ArrayList<>();
        for (var perfil : catalogo.perfis()) {
            if (!perfil.objetivo().equals(usuario.getObjetivo())) continue;
            for (var variante : perfil.variantes()) {
                // Todas as tags precisam ser atendidas pelo mesmo cardápio completo.
                if (!variante.compatibilidades().containsAll(tags)) continue;
                double pontos = 0;
                List<String> criterios = new ArrayList<>();
                criterios.add("Objetivo: " + nomeObjetivo(usuario.getObjetivo()));
                if (perfil.niveisAtividade().contains(usuario.getNivelAtividade())) {
                    pontos += 20; criterios.add("Nível de atividade compatível");
                }
                if (perfil.praticidade().equals(usuario.getPraticidade())) {
                    pontos += 20; criterios.add("Praticidade de preparo");
                }
                if (perfil.orcamento().equals(usuario.getOrcamento())) {
                    pontos += 20; criterios.add("Preferência de orçamento");
                }
                if (perfil.rigor().equals(usuario.getRigor())) {
                    pontos += 10; criterios.add("Organização da rotina");
                }
                if (usuario.getPesoInicialKg() != null && usuario.getAlturaCm() != null) {
                    // Proximidade matemática entre perfis; não calcula necessidade energética.
                    pontos += proximidade(usuario.getPesoInicialKg(), perfil.pesoReferenciaKg(), 20);
                    pontos += proximidade(usuario.getAlturaCm(), perfil.alturaReferenciaCm(), 15);
                    criterios.add("Peso considerado: " + usuario.getPesoInicialKg().stripTrailingZeros().toPlainString() + " kg");
                    criterios.add("Altura considerada: " + usuario.getAlturaCm().stripTrailingZeros().toPlainString() + " cm");
                }
                candidatos.add(new Candidato(perfil, variante, pontos, List.copyOf(criterios)));
            }
        }
        List<String> nomesTags = tags.stream().sorted().map(TagAlimentar::getNome).toList();
        // Empates usam códigos estáveis; a ordem do JSON não muda o resultado.
        candidatos.sort(Comparator.comparingDouble(Candidato::pontos).reversed()
                .thenComparing(c -> c.perfil().codigo()).thenComparing(c -> c.variante().codigo()));
        if (candidatos.isEmpty())
            return new DietaSugeridaDTO("SEM_COMPATIVEL",
                    "Ainda não há um plano para este objetivo que atenda a todas as suas tags.",
                    catalogo.versao(), null, null, usuario.getObjetivo(), null, null, false,
                    0, List.of(), nomesTags, List.of());
        Candidato c = candidatos.getFirst();
        return new DietaSugeridaDTO("SUGERIDA",
                c.variante().demonstracao()
                        ? "Exemplo para demonstrar a seleção por perfil. As refeições e porções precisam de avaliação nutricional antes de uso como dieta individual."
                        : "Plano selecionado entre as opções previamente cadastradas.",
                catalogo.versao(), c.perfil().codigo(), c.perfil().nome(), usuario.getObjetivo(),
                c.variante().codigo(), c.variante().nome(), c.variante().demonstracao(),
                (int)Math.round(c.pontos()), c.criterios(), nomesTags, c.variante().refeicoes());
    }

    private static double proximidade(java.math.BigDecimal informado, java.math.BigDecimal referencia, double escala) {
        return 15.0 / (1.0 + informado.subtract(referencia).abs().doubleValue() / escala);
    }

    private static String nomeObjetivo(String objetivo) {
        return switch (objetivo) {
            case "MANUTENCAO" -> "Manutenção";
            case "EMAGRECIMENTO" -> "Emagrecimento";
            case "HIPERTROFIA" -> "Hipertrofia";
            default -> objetivo;
        };
    }
}
