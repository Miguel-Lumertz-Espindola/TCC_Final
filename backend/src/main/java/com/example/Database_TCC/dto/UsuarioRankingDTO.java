package com.example.Database_TCC.dto;

public class UsuarioRankingDTO {
    private int posicao;
    private String email;
    private String nomeUsuario;
    private String statusMascote;
    private int pontuacaoTotal;

    public UsuarioRankingDTO(int posicao, String email, String nomeUsuario, String statusMascote, int pontuacaoTotal) {
        this.posicao = posicao;
        this.email = email;
        this.nomeUsuario = nomeUsuario;
        this.statusMascote = statusMascote;
        this.pontuacaoTotal = pontuacaoTotal;
    }

    // Adicione os Getters e Setters
    public int getPosicao() { return posicao; }
    public String getEmail() { return email; }
    public String getNomeUsuario() { return nomeUsuario; }
    public String getStatusMascote() { return statusMascote; }
    public int getPontuacaoTotal() { return pontuacaoTotal; }
}