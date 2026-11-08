package com.example.tcc_final.dto; // Lembre-se de que no seu projeto você usa com.example.Database_TCC.dto

public class UsuarioRankingDTO {

    private int posicao;
    private String email;
    private String nomeUsuario;
    private String statusMascote;
    private int pontuacaoTotal;

    // Construtor vazio (boa prática para frameworks de serialização)
    public UsuarioRankingDTO() {}

    // Construtor completo
    public UsuarioRankingDTO(int posicao, String email, String nomeUsuario, String statusMascote, int pontuacaoTotal) {
        this.posicao = posicao;
        this.email = email;
        this.nomeUsuario = nomeUsuario;
        this.statusMascote = statusMascote;
        this.pontuacaoTotal = pontuacaoTotal;
    }

    // Getters e Setters
    public int getPosicao() {
        return posicao;
    }

    public void setPosicao(int posicao) {
        this.posicao = posicao;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }

    public String getStatusMascote() {
        return statusMascote;
    }

    public void setStatusMascote(String statusMascote) {
        this.statusMascote = statusMascote;
    }

    public int getPontuacaoTotal() {
        return pontuacaoTotal;
    }

    public void setPontuacaoTotal(int pontuacaoTotal) {
        this.pontuacaoTotal = pontuacaoTotal;
    }
}