package com.example.Database_TCC.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "dietas_usuario")
public class DietaUsuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;
    @Column(nullable = false, columnDefinition = "text")
    private String selecaoJson;
    @Column(nullable = false)
    private LocalDateTime criadaEm;

    public DietaUsuario() { }
    public DietaUsuario(Usuario usuario, String selecaoJson) {
        this.usuario = usuario; this.selecaoJson = selecaoJson;
        this.criadaEm = LocalDateTime.now();
    }
    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public String getSelecaoJson() { return selecaoJson; }
    public LocalDateTime getCriadaEm() { return criadaEm; }
}
