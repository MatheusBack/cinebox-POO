package br.com.cinebox.controller;

import br.com.cinebox.model.form.CadastroForm;
import br.com.cinebox.model.service.*;
import br.com.cinebox.security.SessaoUsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AutenticacaoController {
    private final UsuarioService usuarios;
    private final SessaoUsuarioService sessao;
    public AutenticacaoController(UsuarioService usuarios, SessaoUsuarioService sessao) { this.usuarios=usuarios; this.sessao=sessao; }
    @GetMapping("/login")
    public String login() { return sessao.idAtual().isPresent() ? "redirect:/perfil" : "auth/login"; }
    @GetMapping("/cadastro")
    public String cadastro(Model model) {
        if (sessao.idAtual().isPresent()) return "redirect:/perfil";
        model.addAttribute("form",new CadastroForm()); return "auth/cadastro";
    }
    @PostMapping("/cadastro")
    public String cadastrar(@Valid @ModelAttribute("form") CadastroForm form, BindingResult result, RedirectAttributes flash) {
        if (sessao.idAtual().isPresent()) return "redirect:/perfil";
        if (!result.hasErrors()) {
            try {
                usuarios.cadastrar(form); flash.addFlashAttribute("sucesso","Conta criada. Entre com seu e-mail e senha.");
                return "redirect:/login";
            } catch (RegraNegocioException e) { result.reject("regra",e.getMessage()); }
        }
        // Inputs de senha não recebem value na view, mesmo em caso de erro.
        return "auth/cadastro";
    }
}
