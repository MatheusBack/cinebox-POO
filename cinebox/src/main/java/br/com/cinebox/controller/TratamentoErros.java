package br.com.cinebox.controller;

import br.com.cinebox.model.service.NaoEncontradoException;
import br.com.cinebox.model.service.RegraNegocioException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
public class TratamentoErros {
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String acessoNegado(Model model) {
        model.addAttribute("codigo","403"); model.addAttribute("mensagem","Você não tem permissão para alterar este registro.");
        return "compartilhado/erro";
    }
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class) @ResponseStatus(HttpStatus.FORBIDDEN)
    public String semPermissao(Model model) { return acessoNegado(model); }
    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class) @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public String fotoGrande(Model model) {
        model.addAttribute("codigo","413"); model.addAttribute("mensagem","A imagem ultrapassa o limite de 2 MB. Volte à tela anterior e escolha uma imagem menor.");
        return "compartilhado/erro";
    }
    @ExceptionHandler(NaoEncontradoException.class) @ResponseStatus(HttpStatus.NOT_FOUND)
    public String naoEncontrado(NaoEncontradoException e, Model model) {
        model.addAttribute("codigo", "404"); model.addAttribute("mensagem", e.getMessage()); return "compartilhado/erro";
    }
    @ExceptionHandler(RegraNegocioException.class) @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String regra(RegraNegocioException e, Model model) {
        model.addAttribute("codigo", "400"); model.addAttribute("mensagem", e.getMessage()); return "compartilhado/erro";
    }
    @ExceptionHandler(MethodArgumentTypeMismatchException.class) @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String parametro(Model model) {
        model.addAttribute("codigo", "400"); model.addAttribute("mensagem", "O endereço contém um identificador ou filtro inválido."); return "compartilhado/erro";
    }
    @ExceptionHandler(DataIntegrityViolationException.class) @ResponseStatus(HttpStatus.CONFLICT)
    public String conflito(Model model) {
        model.addAttribute("codigo", "409"); model.addAttribute("mensagem", "Não foi possível concluir: existe um registro duplicado ou um vínculo que precisa ser removido. Volte à listagem e confira os dados.");
        return "compartilhado/erro";
    }
}
