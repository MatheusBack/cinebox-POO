package br.com.cinebox.model.entity;

import jakarta.persistence.*;
import java.util.*;
import java.time.*;
import java.math.BigDecimal;

@Entity
@Table(name = "avaliacoes", uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "filme_id"}))
public class Avaliacao {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "filme_id", nullable = false)
    private Filme filme;
    @Column(nullable = false, precision = 2, scale = 1)
    private BigDecimal nota;
    @Column(length = 3000)
    private String comentario;
    @Column(name = "data_assistido", nullable = false)
    private LocalDate dataAssistido;
    @Column(nullable = false)
    private boolean favorito;
    @Column(nullable = false)
    private boolean spoiler;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Filme getFilme() { return filme; }
    public void setFilme(Filme filme) { this.filme = filme; }
    public BigDecimal getNota() { return nota; }
    public void setNota(BigDecimal nota) { this.nota = nota; }
    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
    public LocalDate getDataAssistido() { return dataAssistido; }
    public void setDataAssistido(LocalDate dataAssistido) { this.dataAssistido = dataAssistido; }
    public boolean isFavorito() { return favorito; }
    public void setFavorito(boolean favorito) { this.favorito = favorito; }
    public boolean isSpoiler() { return spoiler; }
    public void setSpoiler(boolean spoiler) { this.spoiler = spoiler; }
}
