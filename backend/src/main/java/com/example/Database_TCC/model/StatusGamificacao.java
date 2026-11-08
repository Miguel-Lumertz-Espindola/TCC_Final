package com.example.Database_TCC.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "status_gamificacao")
public class StatusGamificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relacionamento 1 para 1 com a tabela de usuários
    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(name = "pontuacao_total")
    private Integer pontuacaoTotal = 0;

    @Column(name = "ofensiva_dias")
    private Integer ofensivaDias = 0;

    @Column(name = "status_mascote")
    private String statusMascote = "Animado";

    @Column(name = "ultimo_checkin")
    private LocalDateTime ultimoCheckin;

    @Column(name = "personagem_codigo", length = 40)
    private String personagemCodigo = "GUERREIRO";

    public String getPersonagemCodigo() {
        return personagemCodigo == null || personagemCodigo.isBlank() ? "GUERREIRO" : personagemCodigo;
    }
    public void setPersonagemCodigo(String codigo) { personagemCodigo = codigo; }

    // Construtor vazio obrigatório pelo JPA
    public StatusGamificacao() {
    }

    public StatusGamificacao(Usuario usuario) {
        this.usuario = usuario;
        this.pontuacaoTotal = 0;
        this.ofensivaDias = 0;
        this.statusMascote = "Animado";
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Integer getPontuacaoTotal() { return pontuacaoTotal; }
    public void setPontuacaoTotal(Integer pontuacaoTotal) { this.pontuacaoTotal = pontuacaoTotal; }

    public Integer getOfensivaDias() { return ofensivaDias; }
    public void setOfensivaDias(Integer ofensivaDias) { this.ofensivaDias = ofensivaDias; }

    public String getStatusMascote() { return statusMascote; }
    public void setStatusMascote(String statusMascote) { this.statusMascote = statusMascote; }

    public LocalDateTime getUltimoCheckin() { return ultimoCheckin; }
    public void setUltimoCheckin(LocalDateTime ultimoCheckin) { this.ultimoCheckin = ultimoCheckin; }
}
