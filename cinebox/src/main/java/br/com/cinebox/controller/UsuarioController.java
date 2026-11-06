package br.com.cinebox.controller;

import br.com.cinebox.model.service.UsuarioService;
import br.com.cinebox.security.SessaoUsuarioService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/pessoas")
public class UsuarioController {
    private final UsuarioService service;
    private final SessaoUsuarioService sessao;
    public UsuarioController(UsuarioService service, SessaoUsuarioService sessao) { this.service=service; this.sessao=sessao; }
    @GetMapping
    public String index(Model model) { model.addAttribute("pessoas",service.listar()); return "pessoas/index"; }
    @GetMapping("/novo")
    public String novo() { return "redirect:/perfil"; }
    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id) { exigirProprio(id); return "redirect:/perfil/editar"; }
    @GetMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id) { exigirProprio(id); return "redirect:/perfil/excluir"; }
    private void exigirProprio(Long id) {
        if (!id.equals(sessao.exigirId())) throw new AccessDeniedException("Você só pode gerenciar a sua própria conta.");
    }
    // Os POSTs antigos foram retirados: nenhuma rota permite editar outro usuário pelo ID.
}
