package com.example.Database_TCC.service;

import com.example.Database_TCC.dto.*;
import com.example.Database_TCC.model.*;
import com.example.Database_TCC.nutricao.*;
import com.example.Database_TCC.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.dao.DataIntegrityViolationException;
import tools.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.*;

@Service
public class CadastroService {
    private final UsuarioRepository usuarios;
    private final StatusGamificacaoRepository status;
    private final DietaUsuarioRepository dietas;
    private final SeletorDieta seletor;
    private final ObjectMapper mapper;

    public CadastroService(UsuarioRepository usuarios, StatusGamificacaoRepository status,
            DietaUsuarioRepository dietas, SeletorDieta seletor, ObjectMapper mapper) {
        this.usuarios = usuarios; this.status = status; this.dietas = dietas;
        this.seletor = seletor; this.mapper = mapper;
    }
    private ResponseStatusException erro(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }
    private String opcao(String valor, String padrao, Set<String> opcoes) {
        // Clientes anteriores continuam aceitos; o novo cadastro exige seleção explícita.
        String resultado = valor == null ? padrao : valor;
        if (!opcoes.contains(resultado)) throw erro("Opção de perfil inválida.");
        return resultado;
    }
    private boolean longo(String valor) { return valor != null && valor.length() > 255; }

    @Transactional
    public DietaSugeridaDTO cadastrar(CadastroRequest d) {
        if (d == null || d.nome() == null || d.nome().isBlank() || d.nome().length() > 100
                || d.email() == null || d.email().length() > 254
                || !d.email().trim().matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")
                || d.senha() == null || d.senha().length() < 8 || d.senha().length() > 255
                || d.dataNascimento() == null || !d.dataNascimento().isBefore(LocalDate.now())
                || d.alturaCm() == null || d.alturaCm().signum() <= 0
                || d.pesoInicialKg() == null || d.pesoInicialKg().signum() <= 0
                || d.alturaCm().compareTo(new java.math.BigDecimal("999.99")) > 0
                || d.pesoInicialKg().compareTo(new java.math.BigDecimal("999.99")) > 0
                || !Arrays.asList("MANUTENCAO", "EMAGRECIMENTO", "HIPERTROFIA").contains(d.objetivo())
                || !Arrays.asList("SEDENTARIO", "LEVE", "MODERADO", "INTENSO").contains(d.nivelAtividade())
                || longo(d.restricoesAlimentares()) || longo(d.preferenciasAlimentares()))
            throw erro("Confira os campos do cadastro. A senha deve ter pelo menos 8 caracteres.");
        Set<TagAlimentar> tags = new HashSet<>();
        if (d.tagsAlimentares() != null) {
            if (d.tagsAlimentares().size() > TagAlimentar.values().length)
                throw erro("Selecione apenas as tags disponíveis.");
            for (String tag : d.tagsAlimentares()) {
                try { tags.add(TagAlimentar.valueOf(tag)); }
                catch (IllegalArgumentException | NullPointerException e) { throw erro("Tag alimentar inválida."); }
            }
        }
        Usuario u = new Usuario(d.email().trim(), d.senha());
        u.setNome(d.nome().trim()); u.setDataNascimento(d.dataNascimento());
        u.setAlturaCm(d.alturaCm()); u.setPesoInicialKg(d.pesoInicialKg());
        u.setObjetivo(d.objetivo()); u.setNivelAtividade(d.nivelAtividade());
        u.setPraticidade(opcao(d.praticidade(), "RAPIDA", Set.of("RAPIDA", "ELABORADA")));
        u.setRigor(opcao(d.rigor(), "FLEXIVEL", Set.of("FLEXIVEL", "ESTRUTURADO")));
        u.setOrcamento(opcao(d.orcamento(), "ECONOMICO", Set.of("ECONOMICO", "FLEXIVEL")));
        u.setTagsAlimentares(tags);
        u.setRestricoesAlimentares(d.restricoesAlimentares());
        u.setPreferenciasAlimentares(d.preferenciasAlimentares());
        if (usuarios.findByEmail(u.getEmail()).isPresent()) throw erro("E-mail já cadastrado.");
        DietaSugeridaDTO selecao;
        if ((d.restricoesAlimentares() != null && !d.restricoesAlimentares().isBlank())
                || (d.preferenciasAlimentares() != null && !d.preferenciasAlimentares().isBlank())) {
            selecao = new DietaSugeridaDTO("OBSERVACOES_PENDENTES",
                    "As observações em texto foram guardadas. Elas precisam de revisão antes de selecionar um plano; no novo cadastro, use as tags alimentares.",
                    null, null, null, u.getObjetivo(), null, null, false, 0, List.of(),
                    tags.stream().sorted().map(TagAlimentar::getNome).toList(), List.of());
        } else {
            selecao = seletor.selecionar(u);
        }
        // O snapshot preserva refeições e motivo da escolha mesmo após ampliar o catálogo.
        String snapshot = mapper.writeValueAsString(selecao);
        try { usuarios.saveAndFlush(u); }
        catch (DataIntegrityViolationException e) { throw erro("Não foi possível cadastrar. Confira o e-mail e os dados informados."); }
        status.save(new StatusGamificacao(u));
        dietas.save(new DietaUsuario(u, snapshot));
        return selecao;
    }
    @Transactional(readOnly = true)
    public DietaSugeridaDTO consultar(String email) {
        if (email == null || email.isBlank()) throw erro("Informe o usuário.");
        Usuario u = usuarios.findByEmail(email.trim()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
        return dietas.findByUsuarioId(u.getId()).map(d -> mapper.readValue(d.getSelecaoJson(), DietaSugeridaDTO.class))
                .orElseGet(() -> new DietaSugeridaDTO("CADASTRO_ANTERIOR",
                        "Esta conta ainda não possui uma dieta sugerida. A seleção automática está disponível nos novos cadastros.",
                        null, null, null, u.getObjetivo(), null, null, false, 0,
                        List.of(), u.getTagsAlimentares().stream().sorted().map(TagAlimentar::getNome).toList(), List.of()));
    }
}
