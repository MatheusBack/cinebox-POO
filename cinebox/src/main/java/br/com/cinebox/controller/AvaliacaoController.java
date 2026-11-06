package br.com.cinebox.controller;

import br.com.cinebox.model.service.*;
import br.com.cinebox.model.form.*;
import br.com.cinebox.model.entity.Capa;
import br.com.cinebox.model.entity.ItemLista;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/avaliacoes")
public class AvaliacaoController {
    private final AvaliacaoService service;
    private final FilmeService filmeService;
    public AvaliacaoController(AvaliacaoService service, FilmeService filmeService) { this.service = service; this.filmeService = filmeService; }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("avaliacoes", service.listar());
        return "avaliacoes/index";
    }
    private void preparar(Model model, Long id) {
        model.addAttribute("id", id);
        model.addAttribute("titulo", id == null ? "Registrar filme" : "Editar avaliação");
        model.addAttribute("filmes", filmeService.listar(null, null));
    }
    @GetMapping("/novo")
    public String novo(Model model, @RequestParam(required = false) Long filmeId) {
        AvaliacaoForm novo = new AvaliacaoForm(); novo.setFilmeId(filmeId); model.addAttribute("form", novo);
        preparar(model, null); return "avaliacoes/form";
    }
    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("form", service.formulario(id)); preparar(model, id); return "avaliacoes/form";
    }
    @PostMapping({"", "/{id}"})
    public String salvar(@PathVariable(required = false) Long id, @Valid @ModelAttribute("form") AvaliacaoForm form,
                         BindingResult result, Model model, RedirectAttributes flash) {
        if (!result.hasErrors()) {
            try {
                service.salvar(id, form);
                flash.addFlashAttribute("sucesso", "Registro salvo com sucesso."); return "redirect:/avaliacoes";
            } catch (RegraNegocioException e) { result.reject("regra", e.getMessage()); }
        }
        preparar(model, id); return "avaliacoes/form";
    }
    @GetMapping("/{id}/excluir")
    public String confirmar(@PathVariable Long id, Model model) {
        model.addAttribute("titulo", "Excluir avaliação"); model.addAttribute("nome", service.buscar(id).getFilme().getTitulo());
        model.addAttribute("explicacao", "A exclusão é definitiva. Registros com vínculos protegidos precisam ter seus vínculos removidos primeiro.");
        model.addAttribute("acao", "/avaliacoes/" + id + "/excluir"); model.addAttribute("voltar", "/avaliacoes");
        return "compartilhado/excluir";
    }
    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes flash) {
        try { service.excluir(id); flash.addFlashAttribute("sucesso", "Registro excluído."); }
        catch (RegraNegocioException e) { flash.addFlashAttribute("erro", e.getMessage()); }
        return "redirect:/avaliacoes";
    }

}
