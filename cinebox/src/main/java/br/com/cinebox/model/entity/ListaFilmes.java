package br.com.cinebox.model.entity;

import jakarta.persistence.*;
import java.util.*;
import java.time.*;
import java.math.BigDecimal;

@Entity
@Table(name = "listas")
public class ListaFilmes {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String nome;
    @Column(length = 1000)
    private String descricao;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    @OneToMany(mappedBy = "lista", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("posicao ASC, id ASC")
    private List<ItemLista> itens;
    public ListaFilmes() { itens = new ArrayList<>(); }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public List<ItemLista> getItens() { return itens; }
    public void setItens(List<ItemLista> itens) { this.itens = itens; }
}
