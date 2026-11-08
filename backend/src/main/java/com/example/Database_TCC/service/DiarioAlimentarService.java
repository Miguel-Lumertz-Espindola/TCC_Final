package com.example.Database_TCC.service;
import com.example.Database_TCC.dto.*;
import com.example.Database_TCC.model.*;
import com.example.Database_TCC.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.time.*;
import java.util.List;

@Service
public class DiarioAlimentarService {
    private final UsuarioRepository usuarios;
    private final RefeicaoRealizadaRepository refeicoes;
    public DiarioAlimentarService(UsuarioRepository usuarios, RefeicaoRealizadaRepository refeicoes) {
        this.usuarios = usuarios; this.refeicoes = refeicoes;
    }
    private Usuario usuario(String email) {
        if (email == null || email.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o usuário.");
        return usuarios.findByEmail(email.trim()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
    }
    private void numero(java.math.BigDecimal valor, String campo, int casas, boolean positivo) {
        if (valor == null) return;
        if (valor.signum() < 0 || (positivo && valor.signum() == 0)
                || valor.compareTo(new java.math.BigDecimal("999999999")) > 0
                || valor.stripTrailingZeros().scale() > casas)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    campo + ": informe um valor " + (positivo ? "positivo" : "maior ou igual a zero")
                    + " com até " + casas + " casas decimais (máximo 999999999).");
    }
    private void validar(RefeicaoRequest dados, List<ItemRefeicaoDTO> itens) {
        if (dados == null || dados.tipoRefeicao() == null || dados.dataHoraConsumo() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o tipo, a data e o horário da refeição.");
        if ((dados.descricao() == null || dados.descricao().isBlank()) && itens.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Descreva o consumo ou adicione pelo menos um alimento.");
        if (dados.dataHoraConsumo().isAfter(LocalDateTime.now()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A refeição não pode ter data ou horário futuros.");
        if ((dados.descricao() != null && dados.descricao().trim().length() > 1000)
                || (dados.observacao() != null && dados.observacao().trim().length() > 500))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Use até 1000 caracteres na descrição e 500 na observação.");
        if (itens.size() > 50)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Adicione até 50 alimentos por refeição.");
        for (ItemRefeicaoDTO item : itens) {
            if (item == null || item.nome() == null || item.nome().isBlank() || item.nome().trim().length() > 120)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o nome de cada alimento (até 120 caracteres).");
            if ((item.quantidade() == null) != (item.unidade() == null))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Preencha quantidade e unidade juntas ou deixe ambas vazias.");
            numero(item.quantidade(), "Quantidade", 3, true);
            numero(item.calorias(), "Calorias", 2, false); numero(item.proteinas(), "Proteínas", 2, false);
            numero(item.carboidratos(), "Carboidratos", 2, false); numero(item.gorduras(), "Gorduras", 2, false);
        }
    }
    private void preencher(RefeicaoRealizada refeicao, RefeicaoRequest dados) {
        List<ItemRefeicaoDTO> itens = dados == null || dados.itens() == null
                ? refeicao.getItens().stream().map(ItemRefeicaoDTO::de).toList() : dados.itens();
        validar(dados, itens);
        refeicao.setTipoRefeicao(dados.tipoRefeicao());
        refeicao.setDataHoraConsumo(dados.dataHoraConsumo());
        String descricao = dados.descricao() == null ? "" : dados.descricao().trim();
        refeicao.setDescricao(descricao.isEmpty() ? "Refeição com alimentos registrados" : descricao);
        refeicao.setObservacao(dados.observacao() == null ? null : dados.observacao().trim());
        // Lista ausente preserva itens em clientes antigos; lista vazia os remove explicitamente.
        if (dados.itens() != null) refeicao.substituirItens(itens.stream().map(ItemRefeicaoDTO::entidade).toList());
    }
    @Transactional(readOnly = true)
    public List<RefeicaoDTO> listar(String email, LocalDate data) {
        Usuario usuario = usuario(email);
        if (data == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe a data do diário.");
        return refeicoes.buscarPorDia(usuario.getId(), data.atStartOfDay(), data.plusDays(1).atStartOfDay())
                .stream().map(RefeicaoDTO::de).toList();
    }
    @Transactional
    public RefeicaoDTO registrar(String email, RefeicaoRequest dados) {
        Usuario usuario = usuario(email);
        RefeicaoRealizada refeicao = new RefeicaoRealizada();
        refeicao.setUsuario(usuario); refeicao.setDataHoraRegistro(LocalDateTime.now());
        preencher(refeicao, dados);
        return RefeicaoDTO.de(refeicoes.save(refeicao));
    }
    private RefeicaoRealizada refeicao(Long id, Usuario usuario) {
        return refeicoes.findByIdAndUsuarioId(id, usuario.getId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Refeição não encontrada para este usuário."));
    }
    @Transactional
    public RefeicaoDTO editar(String email, Long id, RefeicaoRequest dados) {
        RefeicaoRealizada refeicao = refeicao(id, usuario(email));
        preencher(refeicao, dados);
        return RefeicaoDTO.de(refeicoes.save(refeicao));
    }
    @Transactional
    public void excluir(String email, Long id) {
        refeicoes.delete(refeicao(id, usuario(email)));
    }
}
