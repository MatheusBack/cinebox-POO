package br.com.cinebox.model.service;

import br.com.cinebox.model.entity.*;
import br.com.cinebox.model.form.*;
import br.com.cinebox.model.repository.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@ConditionalOnProperty(name = "app.dados-exemplo", havingValue = "true")
public class DadosExemploService {
    private final UsuarioRepository usuarios;
    private final AvaliacaoRepository avaliacoes;
    private final ListaFilmesRepository listas;
    private final ItemListaRepository itens;
    public DadosExemploService(UsuarioRepository usuarios, AvaliacaoRepository avaliacoes, ListaFilmesRepository listas, ItemListaRepository itens) {
        this.usuarios=usuarios; this.avaliacoes=avaliacoes; this.listas=listas; this.itens=itens;
    }
    public Usuario pessoa(String nome, String apelido, String bio) {
        Usuario u = new Usuario(); u.setNome(nome); u.setApelido(apelido); u.setEmail(apelido+"@example.com"); u.setBio(bio);
        // Perfil fictício sem senha: nenhum acesso padrão compartilhado é criado.
        return usuarios.save(u);
    }
    public void avaliar(Usuario usuario, Filme filme, AvaliacaoForm f) {
        Avaliacao a=new Avaliacao(); a.setUsuario(usuario); a.setFilme(filme); a.setNota(f.getNota());
        a.setComentario(f.getComentario()); a.setDataAssistido(f.getDataAssistido()); a.setFavorito(f.isFavorito());
        a.setSpoiler(f.isSpoiler()); avaliacoes.save(a);
    }
    public ListaFilmes lista(String nome, String descricao, Usuario usuario) {
        ListaFilmes l=new ListaFilmes(); l.setNome(nome); l.setDescricao(descricao); l.setUsuario(usuario); return listas.save(l);
    }
    public void item(ListaFilmes lista, Filme filme, int posicao) {
        ItemLista i=new ItemLista(); i.setLista(lista); i.setFilme(filme); i.setPosicao(posicao); itens.save(i);
    }
}
