package com.example.Database_TCC.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuario_conquistas")
public class UsuarioConquista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relacionamento com a sua classe Usuario já existente
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "conquista_id", nullable = false)
    private com.example.Database_TCC.model.Conquista conquista;

    @Column(nullable = false)
    private LocalDateTime dataDesbloqueio;

    public UsuarioConquista() {}

    public UsuarioConquista(Usuario usuario, com.example.Database_TCC.model.Conquista conquista) {
        this.usuario = usuario;
        this.conquista = conquista;
        this.dataDesbloqueio = LocalDateTime.now(); // Salva a data e hora exata do desbloqueio
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public com.example.Database_TCC.model.Conquista getConquista() { return conquista; }
    public void setConquista(com.example.Database_TCC.model.Conquista conquista) { this.conquista = conquista; }

    public LocalDateTime getDataDesbloqueio() { return dataDesbloqueio; }
    public void setDataDesbloqueio(LocalDateTime dataDesbloqueio) { this.dataDesbloqueio = dataDesbloqueio; }
}