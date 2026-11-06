package br.com.cinebox.model.entity;

import jakarta.persistence.*;
import java.util.*;
import java.time.*;
import java.math.BigDecimal;

@Entity
@Table(name = "itens_lista", uniqueConstraints = @UniqueConstraint(columnNames = {"lista_id", "filme_id"}))
public class ItemLista {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "lista_id", nullable = false)
    private ListaFilmes lista;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "filme_id", nullable = false)
    private Filme filme;
    @Column(nullable = false)
    private Integer posicao;
    @Column(length = 300)
    private String observacao;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ListaFilmes getLista() { return lista; }
    public void setLista(ListaFilmes lista) { this.lista = lista; }
    public Filme getFilme() { return filme; }
    public void setFilme(Filme filme) { this.filme = filme; }
    public Integer getPosicao() { return posicao; }
    public void setPosicao(Integer posicao) { this.posicao = posicao; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
}
