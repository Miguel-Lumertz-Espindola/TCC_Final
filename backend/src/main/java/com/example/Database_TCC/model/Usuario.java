package com.example.Database_TCC.model;

import jakarta.persistence.*;
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha;

    // O JPA exige um construtor vazio por padrão
    public Usuario() {
    }

    public Usuario(String email, String senha) {
        this.email = email;
        this.senha = senha;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    private String nome;
    public String getNome() { return nome; }
    public void setNome(String valor) { this.nome = valor; }
    private java.time.LocalDate dataNascimento;
    public java.time.LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(java.time.LocalDate valor) { this.dataNascimento = valor; }
    private java.math.BigDecimal alturaCm;
    public java.math.BigDecimal getAlturaCm() { return alturaCm; }
    public void setAlturaCm(java.math.BigDecimal valor) { this.alturaCm = valor; }
    private java.math.BigDecimal pesoInicialKg;
    public java.math.BigDecimal getPesoInicialKg() { return pesoInicialKg; }
    public void setPesoInicialKg(java.math.BigDecimal valor) { this.pesoInicialKg = valor; }
    private String objetivo;
    public String getObjetivo() { return objetivo; }
    public void setObjetivo(String valor) { this.objetivo = valor; }
    private String nivelAtividade;
    public String getNivelAtividade() { return nivelAtividade; }
    public void setNivelAtividade(String valor) { this.nivelAtividade = valor; }
    private String restricoesAlimentares;
    public String getRestricoesAlimentares() { return restricoesAlimentares; }
    public void setRestricoesAlimentares(String valor) { this.restricoesAlimentares = valor; }
    private String preferenciasAlimentares;
    public String getPreferenciasAlimentares() { return preferenciasAlimentares; }
    public void setPreferenciasAlimentares(String valor) { this.preferenciasAlimentares = valor; }

    private String praticidade;
    private String rigor;
    private String orcamento;
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "usuario_tags_alimentares", joinColumns = @JoinColumn(name = "usuario_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "tag"}))
    @Enumerated(EnumType.STRING)
    @Column(name = "tag", nullable = false, length = 40)
    private java.util.Set<com.example.Database_TCC.nutricao.TagAlimentar> tagsAlimentares = new java.util.HashSet<>();
    public String getPraticidade() { return praticidade; }
    public void setPraticidade(String valor) { praticidade = valor; }
    public String getRigor() { return rigor; }
    public void setRigor(String valor) { rigor = valor; }
    public String getOrcamento() { return orcamento; }
    public void setOrcamento(String valor) { orcamento = valor; }
    public java.util.Set<com.example.Database_TCC.nutricao.TagAlimentar> getTagsAlimentares() {
        return tagsAlimentares == null ? java.util.Collections.emptySet() : tagsAlimentares;
    }
    public void setTagsAlimentares(java.util.Set<com.example.Database_TCC.nutricao.TagAlimentar> valor) {
        tagsAlimentares = valor == null ? new java.util.HashSet<>() : new java.util.HashSet<>(valor);
    }
}
