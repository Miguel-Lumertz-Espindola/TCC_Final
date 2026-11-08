package com.example.tcc_final.dto;

import com.google.gson.annotations.SerializedName;

public class ConquistaDTO {

    @SerializedName("id")
    private Long id;

    @SerializedName("nome")
    private String nome;

    @SerializedName("descricao")
    private String descricao;

    @SerializedName("iconeUrl")
    private String iconeUrl;

    @SerializedName("pontosRecompensa")
    private int pontosRecompensa;

    @SerializedName("desbloqueada")
    private boolean desbloqueada;

    // Construtor vazio para o Gson
    public ConquistaDTO() {}

    // Getters para lermos as informações no Adapter
    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public String getIconeUrl() { return iconeUrl; }
    public int getPontosRecompensa() { return pontosRecompensa; }
    public boolean isDesbloqueada() { return desbloqueada; }

    private String criterio;
    public String getCriterio() { return criterio==null||criterio.trim().isEmpty()?descricao:criterio; }
}
