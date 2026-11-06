package br.com.cinebox.model.entity;

import jakarta.persistence.*;
import java.util.*;
import java.time.*;
import java.math.BigDecimal;

@Entity
@Table(name = "filmes")
public class Filme {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 140)
    private String titulo;
    @Column(nullable = false, length = 100)
    private String diretor;
    @Column(name = "ano_lancamento", nullable = false)
    private Integer anoLancamento;
    @Column(name = "duracao_minutos", nullable = false)
    private Integer duracaoMinutos;
    @Column(nullable = false, length = 2000)
    private String sinopse;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private Capa capa;
    @Lob
    @Column(name = "imagem")
    private byte[] imagem;
    @Column(name = "imagem_tipo", length = 100)
    private String imagemTipo;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "filme_genero", joinColumns = @JoinColumn(name = "filme_id"), inverseJoinColumns = @JoinColumn(name = "genero_id"), uniqueConstraints = @UniqueConstraint(columnNames = {"filme_id", "genero_id"}))
    private Set<Genero> generos;
    public Filme() { generos = new LinkedHashSet<>(); capa = Capa.PADRAO; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDiretor() { return diretor; }
    public void setDiretor(String diretor) { this.diretor = diretor; }
    public Integer getAnoLancamento() { return anoLancamento; }
    public void setAnoLancamento(Integer anoLancamento) { this.anoLancamento = anoLancamento; }
    public Integer getDuracaoMinutos() { return duracaoMinutos; }
    public void setDuracaoMinutos(Integer duracaoMinutos) { this.duracaoMinutos = duracaoMinutos; }
    public String getSinopse() { return sinopse; }
    public void setSinopse(String sinopse) { this.sinopse = sinopse; }
    public Capa getCapa() { return capa; }
    public void setCapa(Capa capa) { this.capa = capa; }
    public byte[] getImagem() { return imagem; }
    public void setImagem(byte[] imagem) { this.imagem = imagem; }
    public String getImagemTipo() { return imagemTipo; }
    public void setImagemTipo(String imagemTipo) { this.imagemTipo = imagemTipo; }
    public Set<Genero> getGeneros() { return generos; }
    public void setGeneros(Set<Genero> generos) { this.generos = generos; }
}
