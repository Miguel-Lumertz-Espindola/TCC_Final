package com.example.Database_TCC.model;
import jakarta.persistence.*;
import java.math.BigDecimal;
@Entity
@Table(name = "itens_refeicao", indexes = @Index(name = "idx_item_refeicao", columnList = "refeicao_id"))
public class ItemRefeicao {
    public ItemRefeicao() { }
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    public Long getId() { return id; }
    public void setId(Long valor) { id = valor; }
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "refeicao_id", nullable = false)
    private RefeicaoRealizada refeicao;
    public RefeicaoRealizada getRefeicao() { return refeicao; }
    public void setRefeicao(RefeicaoRealizada valor) { refeicao = valor; }
    @Column(nullable = false, length = 120)
    private String nome;
    public String getNome() { return nome; }
    public void setNome(String valor) { nome = valor; }
    @Column(precision = 12, scale = 3)
    private BigDecimal quantidade;
    public BigDecimal getQuantidade() { return quantidade; }
    public void setQuantidade(BigDecimal valor) { quantidade = valor; }
    @Enumerated(EnumType.STRING) @Column(length = 20)
    private UnidadeMedida unidade;
    public UnidadeMedida getUnidade() { return unidade; }
    public void setUnidade(UnidadeMedida valor) { unidade = valor; }
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
