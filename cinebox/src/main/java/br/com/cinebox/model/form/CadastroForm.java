package br.com.cinebox.model.form;

import jakarta.validation.constraints.*;
import java.util.Objects;

public class CadastroForm extends UsuarioForm {
    @NotBlank(message = "Informe uma senha.")
    @Size(min = 8, max = 72, message = "A senha precisa ter de 8 a 72 caracteres (no máximo 72 bytes UTF-8).")
    private String senha;
    @NotBlank(message = "Confirme a senha.")
    @Size(max = 72, message = "Use até 72 caracteres.")
    private String confirmarSenha;

    @AssertTrue(message = "As senhas não conferem.")
    public boolean isSenhasIguais() { return Objects.equals(senha, confirmarSenha); }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public String getConfirmarSenha() { return confirmarSenha; }
    public void setConfirmarSenha(String confirmarSenha) { this.confirmarSenha = confirmarSenha; }
}
