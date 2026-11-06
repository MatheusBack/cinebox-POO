package br.com.cinebox.model.entity;

import jakarta.persistence.*;
import java.util.*;
import java.time.*;
import java.math.BigDecimal;

@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 80)
    private String nome;
    @Column(nullable = false, unique = true, length = 30)
    private String apelido;
    @Column(nullable = false, unique = true, length = 120)
    private String email;
    @Column(length = 300)
    private String bio;
    @Column(nullable = false, updatable = false)
    private LocalDate dataCadastro;
    // Nullable para preservar contas anteriores à autenticação. Conta sem hash não faz login.
    @Column(name = "senha_hash", length = 100)
    private String senhaHash;
    @Lob @Basic(fetch = FetchType.LAZY)
    @Column(name = "foto")
    private byte[] foto;
    @Column(name = "foto_tipo", length = 20)
    private String fotoTipo;

    public String getSenhaHash() { return senhaHash; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }
    public byte[] getFoto() { return foto; }
    public void setFoto(byte[] foto) { this.foto = foto; }
    public String getFotoTipo() { return fotoTipo; }
    public void setFotoTipo(String fotoTipo) { this.fotoTipo = fotoTipo; }
    public boolean isTemFoto() { return foto != null && foto.length > 0; }

    @PrePersist
    void preparar() { if (dataCadastro == null) dataCadastro = LocalDate.now(); }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getApelido() { return apelido; }
    public void setApelido(String apelido) { this.apelido = apelido; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public LocalDate getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDate dataCadastro) { this.dataCadastro = dataCadastro; }
}
