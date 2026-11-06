package br.com.cinebox.model.form;

import jakarta.validation.constraints.*;
import java.util.*;
import java.time.LocalDate;
import java.math.BigDecimal;
import br.com.cinebox.model.entity.Capa;
import org.springframework.format.annotation.DateTimeFormat;


public class ListaForm {
    @NotBlank(message = "Dê um nome para a lista.") @Size(max = 100, message = "Use até 100 caracteres.")
    private String nome;
    @Size(max = 1000, message = "Use até 1000 caracteres.")
    private String descricao;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
}
