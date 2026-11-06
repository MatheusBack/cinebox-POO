package br.com.cinebox.model.form;

import jakarta.validation.constraints.*;
import java.util.*;
import java.time.LocalDate;
import java.math.BigDecimal;
import br.com.cinebox.model.entity.Capa;
import org.springframework.format.annotation.DateTimeFormat;


public class GeneroForm {
    @NotBlank(message = "Informe o gênero.") @Size(max = 40, message = "Use até 40 caracteres.")
    private String nome;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
}
