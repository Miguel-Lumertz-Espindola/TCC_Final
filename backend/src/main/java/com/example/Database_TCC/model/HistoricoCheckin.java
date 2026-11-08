package com.example.Database_TCC.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "historico_checkin")
public class HistoricoCheckin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "data_hora_registro")
    private LocalDateTime dataHoraRegistro = LocalDateTime.now();

    @Column(name = "tipo_refeicao")
    private String tipoRefeicao; // Ex: "Almoco"

    @Column(name = "pontos_ganhos")
    private Integer pontosGanhos;

    @Column(name = "status_precisao")
    private String statusPrecisao; // Ex: "Exato", "Atrasado"

    public HistoricoCheckin() {}

    public HistoricoCheckin(Usuario usuario, String tipoRefeicao, Integer pontosGanhos, String statusPrecisao) {
        this.usuario = usuario;
        this.tipoRefeicao = tipoRefeicao;
        this.pontosGanhos = pontosGanhos;
        this.statusPrecisao = statusPrecisao;
        this.dataHoraRegistro = LocalDateTime.now();
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public LocalDateTime getDataHoraRegistro() { return dataHoraRegistro; }
    public void setDataHoraRegistro(LocalDateTime dataHoraRegistro) { this.dataHoraRegistro = dataHoraRegistro; }
    public String getTipoRefeicao() { return tipoRefeicao; }
    public void setTipoRefeicao(String tipoRefeicao) { this.tipoRefeicao = tipoRefeicao; }
    public Integer getPontosGanhos() { return pontosGanhos; }
    public void setPontosGanhos(Integer pontosGanhos) { this.pontosGanhos = pontosGanhos; }
    public String getStatusPrecisao() { return statusPrecisao; }
    public void setStatusPrecisao(String statusPrecisao) { this.statusPrecisao = statusPrecisao; }
}