package com.example.Database_TCC.model;

import jakarta.persistence.*;

@Entity
@Table(name = "conquistas")
public class Conquista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ATUALIZADO AQUI: 'nome' em vez de 'titulo'
    @Column(nullable = false, unique = true)
    private String nome;

    @Column(nullable = false)
    private String descricao;

    private String iconeUrl;
    private int pontosRecompensa;

    public Conquista() {}

    public Conquista(String nome, String descricao, String iconeUrl, int pontosRecompensa) {
        this.nome = nome;
        this.descricao = descricao;
        this.iconeUrl = iconeUrl;
        this.pontosRecompensa = pontosRecompensa;
    }

    // Getters e Setters Atualizados
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getIconeUrl() { return iconeUrl; }
    public void setIconeUrl(String iconeUrl) { this.iconeUrl = iconeUrl; }

    public int getPontosRecompensa() { return pontosRecompensa; }
    public void setPontosRecompensa(int pontosRecompensa) { this.pontosRecompensa = pontosRecompensa; }
}