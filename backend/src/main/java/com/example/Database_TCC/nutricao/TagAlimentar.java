package com.example.Database_TCC.nutricao;

public enum TagAlimentar {
    SEM_LACTOSE("Sem lactose"), SEM_GLUTEN("Sem glúten"),
    VEGETARIANO("Vegetariano"), VEGANO("Vegano");

    private final String nome;
    TagAlimentar(String nome) { this.nome = nome; }
    public String getNome() { return nome; }
}
