package br.com.cinebox.controller;

import br.com.cinebox.model.form.UsuarioForm;
import br.com.cinebox.model.service.*;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/perfil")
public class PerfilController {
    private final UsuarioService usuarios;
    private final AvaliacaoService avaliacoes;
    public PerfilController(UsuarioService usuarios, AvaliacaoService avaliacoes) { this.usuarios=usuarios; this.avaliacoes=avaliacoes; }
    @GetMapping
    public String perfil(Model model) {
        model.addAttribute("perfil",usuarios.perfilAtual());
        model.addAttribute("avaliacoes",avaliacoes.listar()); return "perfil/index";
    }
    @GetMapping("/editar")
    public String editar(Model model) { model.addAttribute("form",usuarios.formularioAtual()); return "perfil/form"; }
    @PostMapping("/editar")
    public String salvar(@Valid @ModelAttribute("form") UsuarioForm form, BindingResult result,
                         @RequestParam(required=false) MultipartFile foto,
                         @RequestParam(defaultValue="false") boolean removerFoto, RedirectAttributes flash) {
        if (!result.hasErrors()) {
            try {
                usuarios.atualizarPerfil(form,foto,removerFoto); flash.addFlashAttribute("sucesso","Perfil atualizado."); return "redirect:/perfil";
            } catch (RegraNegocioException e) { result.reject("regra",e.getMessage()); }
        }
        return "perfil/form";
    }
    @GetMapping("/foto") @ResponseBody
    public ResponseEntity<byte[]> foto() {
        var foto = usuarios.fotoAtual();
        if (foto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).contentType(MediaType.parseMediaType(foto.tipo())).body(foto.bytes());
    }
    @GetMapping("/excluir")
    public String confirmarExclusao() { return "perfil/excluir"; }
    @PostMapping("/excluir")
    public String excluir(@RequestParam String senha, Model model, HttpServletRequest request,
                          HttpServletResponse response, Authentication authentication) {
        try {
            usuarios.excluirConta(senha);
            new SecurityContextLogoutHandler().logout(request,response,authentication);
            return "redirect:/login?contaExcluida";
        } catch (RegraNegocioException e) { model.addAttribute("erro",e.getMessage()); return "perfil/excluir"; }
    }
}
