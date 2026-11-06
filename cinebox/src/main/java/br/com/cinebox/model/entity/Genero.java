package br.com.cinebox.model.entity;

import jakarta.persistence.*;
import java.util.*;
import java.time.*;
import java.math.BigDecimal;

@Entity
@Table(name = "generos")
public class Genero {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 40)
    private String nome;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
}
