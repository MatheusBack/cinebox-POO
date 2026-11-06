package br.com.cinebox.model.form;

import jakarta.validation.constraints.*;
import java.util.*;
import java.time.LocalDate;
import java.math.BigDecimal;
import br.com.cinebox.model.entity.Capa;
import org.springframework.format.annotation.DateTimeFormat;


public class UsuarioForm {
    @NotBlank(message = "Informe o nome.") @Size(max = 80, message = "Use até 80 caracteres.")
    private String nome;
    @NotBlank(message = "Informe um apelido.") @Pattern(regexp = "[a-zA-Z0-9_]{3,30}", message = "Use de 3 a 30 letras, números ou sublinhados, sem espaços.")
    private String apelido;
    @NotBlank(message = "Informe o e-mail.") @Email(message = "Informe um e-mail válido.") @Size(max = 120, message = "Use até 120 caracteres.")
    private String email;
    @Size(max = 300, message = "Use até 300 caracteres.")
    private String bio;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getApelido() { return apelido; }
    public void setApelido(String apelido) { this.apelido = apelido; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
}
