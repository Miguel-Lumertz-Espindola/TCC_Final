package com.example.Database_TCC.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity
@Table(name = "refeicoes_realizadas", indexes = @Index(name = "idx_refeicao_usuario_consumo", columnList = "usuario_id,data_hora_consumo"))
public class RefeicaoRealizada {
    public RefeicaoRealizada() { }
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    public Long getId() { return id; }
    public void setId(Long valor) { id = valor; }
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario valor) { usuario = valor; }
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private TipoRefeicao tipoRefeicao;
    public TipoRefeicao getTipoRefeicao() { return tipoRefeicao; }
    public void setTipoRefeicao(TipoRefeicao valor) { tipoRefeicao = valor; }
    @Column(nullable = false)
    private LocalDateTime dataHoraConsumo;
    public LocalDateTime getDataHoraConsumo() { return dataHoraConsumo; }
    public void setDataHoraConsumo(LocalDateTime valor) { dataHoraConsumo = valor; }
    @Column(nullable = false, length = 1000)
    private String descricao;
    public String getDescricao() { return descricao; }
    public void setDescricao(String valor) { descricao = valor; }
    @Column(length = 500)
    private String observacao;
    public String getObservacao() { return observacao; }
    public void setObservacao(String valor) { observacao = valor; }
    @Column(nullable = false, updatable = false)
    private LocalDateTime dataHoraRegistro;
    public LocalDateTime getDataHoraRegistro() { return dataHoraRegistro; }
    public void setDataHoraRegistro(LocalDateTime valor) { dataHoraRegistro = valor; }
    @OneToMany(mappedBy = "refeicao", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private java.util.List<ItemRefeicao> itens = new java.util.ArrayList<>();
    public java.util.List<ItemRefeicao> getItens() { return itens; }
    public void substituirItens(java.util.List<ItemRefeicao> novos) {
        itens.clear();
        for (ItemRefeicao item : novos) { item.setRefeicao(this); itens.add(item); }
    }
}
