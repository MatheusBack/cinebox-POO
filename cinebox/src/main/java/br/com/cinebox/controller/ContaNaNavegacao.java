package br.com.cinebox.controller;

import br.com.cinebox.model.service.UsuarioService;
import br.com.cinebox.security.SessaoUsuarioService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class ContaNaNavegacao {
    private final UsuarioService usuarios;
    private final SessaoUsuarioService sessao;
    public ContaNaNavegacao(UsuarioService usuarios, SessaoUsuarioService sessao) { this.usuarios=usuarios; this.sessao=sessao; }
    @ModelAttribute("usuarioLogado")
    public UsuarioService.Perfil usuarioLogado() {
        return sessao.idAtual().isPresent() ? usuarios.perfilAtual() : null;
    }
}
