package com.example.Database_TCC.model;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity
@Table(name = "metas_usuario", indexes = @Index(name = "idx_metas_usuario_vigencia", columnList = "usuario_id,inicio_vigencia"))
public class MetasUsuario {
    public MetasUsuario() { }
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    public Long getId() { return id; }
    public void setId(Long valor) { id = valor; }
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario valor) { usuario = valor; }
    @Column(nullable = false, length = 30)
    private String objetivo;
    public String getObjetivo() { return objetivo; }
    public void setObjetivo(String valor) { objetivo = valor; }
    @Column(nullable = false, updatable = false)
    private LocalDateTime inicioVigencia;
    public LocalDateTime getInicioVigencia() { return inicioVigencia; }
    public void setInicioVigencia(LocalDateTime valor) { inicioVigencia = valor; }
    @Column(precision = 12, scale = 2)
    private BigDecimal calorias;
    public BigDecimal getCalorias() { return calorias; }
    public void setCalorias(BigDecimal valor) { calorias = valor; }
    @Column(precision = 12, scale = 2)
    private BigDecimal proteinas;
    public BigDecimal getProteinas() { return proteinas; }
    public void setProteinas(BigDecimal valor) { proteinas = valor; }
    @Column(precision = 12, scale = 2)
    private BigDecimal carboidratos;
    public BigDecimal getCarboidratos() { return carboidratos; }
    public void setCarboidratos(BigDecimal valor) { carboidratos = valor; }
    @Column(precision = 12, scale = 2)
    private BigDecimal gorduras;
    public BigDecimal getGorduras() { return gorduras; }
    public void setGorduras(BigDecimal valor) { gorduras = valor; }
}
