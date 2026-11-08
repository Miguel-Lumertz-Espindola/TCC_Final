package com.example.Database_TCC.dto;

public class ConquistaDTO {
    private Long id;
    private String nome;
    private String descricao;
    private String iconeUrl;
    private int pontosRecompensa;

    // O campo chave para o Android saber se deve pintar a medalha de colorido ou cinza
    private boolean desbloqueada;

    public ConquistaDTO(Long id, String nome, String descricao, String iconeUrl, int pontosRecompensa, boolean desbloqueada) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.iconeUrl = iconeUrl;
        this.pontosRecompensa = pontosRecompensa;
        this.desbloqueada = desbloqueada;
    }

    // Getters para o Spring Boot converter tudo para JSON
    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public String getIconeUrl() { return iconeUrl; }
    public int getPontosRecompensa() { return pontosRecompensa; }
    public boolean isDesbloqueada() { return desbloqueada; }
    public String getCriterio() { return com.example.Database_TCC.gamificacao.RegrasConquistas.criterio(nome,descricao); }
}
