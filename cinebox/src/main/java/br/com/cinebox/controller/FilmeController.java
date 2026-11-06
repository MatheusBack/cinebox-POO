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
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;

@Controller
@RequestMapping("/filmes")
public class FilmeController {
    private final FilmeService service;
    private final GeneroService generoService;
    private final AvaliacaoService avaliacaoService;
    private final UsuarioService usuarioService;
    private final ListaService listaService;
    public FilmeController(FilmeService service, GeneroService generoService, AvaliacaoService avaliacaoService, UsuarioService usuarioService, ListaService listaService) { this.service = service; this.generoService = generoService; this.avaliacaoService = avaliacaoService; this.usuarioService = usuarioService; this.listaService = listaService; }

    @GetMapping
    public String index(@RequestParam(defaultValue = "") String q, @RequestParam(required = false) Long generoId, Model model) {
        model.addAttribute("filmes", service.listar(q, generoId));
        model.addAttribute("generos", generoService.listar());
        model.addAttribute("q", q); model.addAttribute("generoId", generoId);
        model.addAttribute("notas", avaliacaoService.medias());
        model.addAttribute("totalFilmes", service.contar());
        model.addAttribute("totalAvaliacoes", avaliacaoService.contar());
        model.addAttribute("totalPessoas", usuarioService.contar());
        model.addAttribute("totalListas", listaService.contar());
        return "filmes/index";
    }
    private void preparar(Model model, Long id) {
        model.addAttribute("id", id);
        model.addAttribute("titulo", id == null ? "Novo filme" : "Editar filme");
        model.addAttribute("generos", generoService.listar()); model.addAttribute("capas", Capa.values());
    }
    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("form", new FilmeForm());
        preparar(model, null); return "filmes/form";
    }
    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("form", service.formulario(id)); preparar(model, id); return "filmes/form";
    }
    @PostMapping({"", "/{id}"})
    public String salvar(@PathVariable(required = false) Long id, @Valid @ModelAttribute("form") FilmeForm form,
                         BindingResult result, Model model, RedirectAttributes flash) {
        if (!result.hasErrors()) {
            try {
                service.salvar(id, form);
                flash.addFlashAttribute("sucesso", "Registro salvo com sucesso."); return "redirect:/filmes";
            } catch (RegraNegocioException e) { result.reject("regra", e.getMessage()); }
        }
        preparar(model, id); return "filmes/form";
    }
    @GetMapping("/{id}/excluir")
    public String confirmar(@PathVariable Long id, Model model) {
        model.addAttribute("titulo", "Excluir filme"); model.addAttribute("nome", service.buscar(id).getTitulo());
        model.addAttribute("explicacao", "A exclusão é definitiva. Registros com vínculos protegidos precisam ter seus vínculos removidos primeiro.");
        model.addAttribute("acao", "/filmes/" + id + "/excluir"); model.addAttribute("voltar", "/filmes");
        return "compartilhado/excluir";
    }
    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes flash) {
        try { service.excluir(id); flash.addFlashAttribute("sucesso", "Registro excluído."); }
        catch (RegraNegocioException e) { flash.addFlashAttribute("erro", e.getMessage()); }
        return "redirect:/filmes";
    }

    @GetMapping(value = "/{id}/imagem")
    @ResponseBody
    public ResponseEntity<byte[]> imagem(@PathVariable Long id) {
        var filme = service.buscar(id);
        if (filme.getImagem() == null || filme.getImagemTipo() == null) return ResponseEntity.notFound().build();
        MediaType tipo = MediaType.parseMediaType(filme.getImagemTipo());
        return ResponseEntity.ok().contentType(tipo).body(filme.getImagem());
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        model.addAttribute("filme", service.buscar(id));
        model.addAttribute("avaliacoes", avaliacaoService.doFilme(id));
        model.addAttribute("notas", avaliacaoService.medias());
        return "filmes/detalhe";
    }

}
