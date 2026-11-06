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
@RequestMapping("/generos")
public class GeneroController {
    private final GeneroService service;

    public GeneroController(GeneroService service) { this.service = service;  }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("generos", service.listar());
        return "generos/index";
    }
    private void preparar(Model model, Long id) {
        model.addAttribute("id", id);
        model.addAttribute("titulo", id == null ? "Novo gênero" : "Editar gênero");

    }
    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("form", new GeneroForm());
        preparar(model, null); return "generos/form";
    }
    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("form", service.formulario(id)); preparar(model, id); return "generos/form";
    }
    @PostMapping({"", "/{id}"})
    public String salvar(@PathVariable(required = false) Long id, @Valid @ModelAttribute("form") GeneroForm form,
                         BindingResult result, Model model, RedirectAttributes flash) {
        if (!result.hasErrors()) {
            try {
                service.salvar(id, form);
                flash.addFlashAttribute("sucesso", "Registro salvo com sucesso."); return "redirect:/generos";
            } catch (RegraNegocioException e) { result.reject("regra", e.getMessage()); }
        }
        preparar(model, id); return "generos/form";
    }
    @GetMapping("/{id}/excluir")
    public String confirmar(@PathVariable Long id, Model model) {
        model.addAttribute("titulo", "Excluir gênero"); model.addAttribute("nome", service.buscar(id).getNome());
        model.addAttribute("explicacao", "A exclusão é definitiva. Registros com vínculos protegidos precisam ter seus vínculos removidos primeiro.");
        model.addAttribute("acao", "/generos/" + id + "/excluir"); model.addAttribute("voltar", "/generos");
        return "compartilhado/excluir";
    }
    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes flash) {
        try { service.excluir(id); flash.addFlashAttribute("sucesso", "Registro excluído."); }
        catch (RegraNegocioException e) { flash.addFlashAttribute("erro", e.getMessage()); }
        return "redirect:/generos";
    }

}
