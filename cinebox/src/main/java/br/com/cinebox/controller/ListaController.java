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
@RequestMapping("/listas")
public class ListaController {
    private final ListaService service;
    private final FilmeService filmeService;
    public ListaController(ListaService service, FilmeService filmeService) { this.service = service; this.filmeService = filmeService; }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("listas", service.listar());
        return "listas/index";
    }
    private void preparar(Model model, Long id) {
        model.addAttribute("id", id);
        model.addAttribute("titulo", id == null ? "Nova lista" : "Editar lista");
    }
    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("form", new ListaForm());
        preparar(model, null); return "listas/form";
    }
    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("form", service.formulario(id)); preparar(model, id); return "listas/form";
    }
    @PostMapping({"", "/{id}"})
    public String salvar(@PathVariable(required = false) Long id, @Valid @ModelAttribute("form") ListaForm form,
                         BindingResult result, Model model, RedirectAttributes flash) {
        if (!result.hasErrors()) {
            try {
                var salvo = service.salvar(id, form);
                flash.addFlashAttribute("sucesso", "Registro salvo com sucesso."); return "redirect:/listas/" + salvo.getId();
            } catch (RegraNegocioException e) { result.reject("regra", e.getMessage()); }
        }
        preparar(model, id); return "listas/form";
    }
    @GetMapping("/{id}/excluir")
    public String confirmar(@PathVariable Long id, Model model) {
        model.addAttribute("titulo", "Excluir lista"); model.addAttribute("nome", service.buscarPropria(id).getNome());
        model.addAttribute("explicacao", "Todos os itens desta lista também serão removidos. Os filmes e as avaliações serão preservados.");
        model.addAttribute("acao", "/listas/" + id + "/excluir"); model.addAttribute("voltar", "/listas");
        return "compartilhado/excluir";
    }
    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes flash) {
        try { service.excluir(id); flash.addFlashAttribute("sucesso", "Registro excluído."); }
        catch (RegraNegocioException e) { flash.addFlashAttribute("erro", e.getMessage()); }
        return "redirect:/listas";
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        ItemListaForm f = new ItemListaForm();
        int proxima = service.buscar(id).getItens().stream().mapToInt(ItemLista::getPosicao).max().orElse(0) + 1;
        f.setPosicao(Math.min(proxima, 9999));
        model.addAttribute("itemForm", f); prepararDetalhe(id, model); return "listas/detalhe";
    }
    private void prepararDetalhe(Long id, Model model) {
        model.addAttribute("lista", service.buscar(id)); model.addAttribute("filmes", filmeService.listar(null, null));
    }
    @PostMapping("/{id}/itens")
    public String adicionarItem(@PathVariable Long id, @Valid @ModelAttribute("itemForm") ItemListaForm form,
                                BindingResult result, Model model, RedirectAttributes flash) {
        if (!result.hasErrors()) {
            try {
                service.salvarItem(id, null, form); flash.addFlashAttribute("sucesso", "Filme adicionado à lista.");
                return "redirect:/listas/" + id;
            } catch (RegraNegocioException e) { result.reject("regra", e.getMessage()); }
        }
        prepararDetalhe(id, model); return "listas/detalhe";
    }
    @GetMapping("/{id}/itens/{itemId}/editar")
    public String editarItem(@PathVariable Long id, @PathVariable Long itemId, Model model) {
        model.addAttribute("itemForm", service.formularioItem(id, itemId)); model.addAttribute("itemId", itemId);
        prepararDetalhe(id, model); return "listas/item-form";
    }
    @PostMapping("/{id}/itens/{itemId}")
    public String salvarItem(@PathVariable Long id, @PathVariable Long itemId,
                             @Valid @ModelAttribute("itemForm") ItemListaForm form, BindingResult result,
                             Model model, RedirectAttributes flash) {
        if (!result.hasErrors()) {
            try {
                service.salvarItem(id, itemId, form); flash.addFlashAttribute("sucesso", "Item atualizado.");
                return "redirect:/listas/" + id;
            } catch (RegraNegocioException e) { result.reject("regra", e.getMessage()); }
        }
        model.addAttribute("itemId", itemId); prepararDetalhe(id, model); return "listas/item-form";
    }
    @GetMapping("/{id}/itens/{itemId}/excluir")
    public String confirmarItem(@PathVariable Long id, @PathVariable Long itemId, Model model) {
        model.addAttribute("titulo", "Remover filme da lista");
        model.addAttribute("nome", service.buscarItem(id, itemId).getFilme().getTitulo());
        model.addAttribute("explicacao", "Apenas o item será removido. O filme continuará no catálogo.");
        model.addAttribute("acao", "/listas/" + id + "/itens/" + itemId + "/excluir");
        model.addAttribute("voltar", "/listas/" + id); return "compartilhado/excluir";
    }
    @PostMapping("/{id}/itens/{itemId}/excluir")
    public String excluirItem(@PathVariable Long id, @PathVariable Long itemId, RedirectAttributes flash) {
        service.excluirItem(id, itemId); flash.addFlashAttribute("sucesso", "Filme removido da lista.");
        return "redirect:/listas/" + id;
    }

}
