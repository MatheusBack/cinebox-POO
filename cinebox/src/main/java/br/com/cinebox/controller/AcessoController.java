package br.com.cinebox.controller;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AcessoController {
    @RequestMapping("/acesso-negado") @ResponseStatus(HttpStatus.FORBIDDEN)
    public String negado(Model model) {
        model.addAttribute("codigo","403"); model.addAttribute("mensagem","Você não tem permissão para alterar este registro.");
        return "compartilhado/erro";
    }
}
