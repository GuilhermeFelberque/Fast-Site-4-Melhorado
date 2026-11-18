package com.example.demo.model;

import java.math.BigDecimal;

/**
 * MODEL: peça do estoque da oficina (tabela "pecas").
 */
public class Peca {

    private Long id;
    private String codigo;
    private String nome;
    private String categoria;
    private Integer quantidade;
    private Integer estoqueMinimo;
    private BigDecimal preco;   // preço unitário (BigDecimal: dinheiro sem erro de centavos)

    // Calculado (não é coluna): true quando precisa repor
    public boolean isEstoqueBaixo() {
        return quantidade != null && estoqueMinimo != null && quantidade <= estoqueMinimo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }

    public Integer getEstoqueMinimo() { return estoqueMinimo; }
    public void setEstoqueMinimo(Integer estoqueMinimo) { this.estoqueMinimo = estoqueMinimo; }

    public BigDecimal getPreco() { return preco; }
    public void setPreco(BigDecimal preco) { this.preco = preco; }
}
