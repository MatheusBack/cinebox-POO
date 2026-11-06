package br.com.cinebox.model.service;

import br.com.cinebox.model.entity.*;
import br.com.cinebox.model.form.*;
import br.com.cinebox.model.repository.*;
import br.com.cinebox.security.SessaoUsuarioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.math.BigDecimal;

@Service
@Transactional(readOnly = true)
public class ListaService {
    private final ListaFilmesRepository repository;
    private final ItemListaRepository itens;
    private final UsuarioService usuarios;
    private final FilmeService filmes;
    private final SessaoUsuarioService sessao;
    public ListaService(ListaFilmesRepository repository, ItemListaRepository itens, UsuarioService usuarios, FilmeService filmes, SessaoUsuarioService sessao) {
        this.repository = repository; this.itens = itens; this.usuarios = usuarios; this.filmes = filmes; this.sessao = sessao;
    }
    public List<ListaFilmes> listar() { return repository.findAllByOrderByIdDesc(); }
    public long contar() { return repository.count(); }
    public ListaFilmes buscar(Long id) { return repository.findById(id).orElseThrow(() -> new NaoEncontradoException("Lista não encontrada.")); }
    public ListaFilmes buscarPropria(Long id) {
        ListaFilmes lista = buscar(id);
        if (!lista.getUsuario().getId().equals(sessao.exigirId()))
            throw new NaoEncontradoException("Lista não encontrada entre as suas listas.");
        return lista;
    }
    @Transactional
    public ListaFilmes salvar(Long id, ListaForm form) {
        ListaFilmes lista = id == null ? new ListaFilmes() : buscarPropria(id);
        lista.setNome(form.getNome().strip()); lista.setDescricao(form.getDescricao()); lista.setUsuario(usuarios.buscar(sessao.exigirId()));
        return repository.save(lista);
    }
    public ListaForm formulario(Long id) {
        ListaFilmes lista = buscarPropria(id); ListaForm f = new ListaForm();
        f.setNome(lista.getNome()); f.setDescricao(lista.getDescricao()); return f;
    }
    @Transactional
    public void excluir(Long id) { repository.delete(buscarPropria(id)); }
    public ItemLista buscarItem(Long listaId, Long itemId) {
        return buscarPropria(listaId).getItens().stream().filter(i -> i.getId().equals(itemId)).findFirst()
            .orElseThrow(() -> new NaoEncontradoException("Item não encontrado nesta lista."));
    }
    public ItemListaForm formularioItem(Long listaId, Long itemId) {
        ItemLista item = buscarItem(listaId, itemId); ItemListaForm f = new ItemListaForm();
        f.setFilmeId(item.getFilme().getId()); f.setPosicao(item.getPosicao()); f.setObservacao(item.getObservacao()); return f;
    }
    @Transactional
    public void salvarItem(Long listaId, Long itemId, ItemListaForm form) {
        ListaFilmes lista = buscarPropria(listaId);
        itens.findByListaIdAndFilmeId(listaId, form.getFilmeId()).filter(i -> !Objects.equals(i.getId(), itemId)).ifPresent(i -> { throw new RegraNegocioException("Esse filme já está nesta lista."); });
        ItemLista item = itemId == null ? new ItemLista() : buscarItem(listaId, itemId);
        item.setLista(lista); item.setFilme(filmes.buscar(form.getFilmeId())); item.setPosicao(form.getPosicao()); item.setObservacao(form.getObservacao());
        if (itemId == null) lista.getItens().add(item);
        itens.save(item);
    }
    @Transactional
    public void excluirItem(Long listaId, Long itemId) {
        ListaFilmes lista = buscarPropria(listaId); ItemLista item = buscarItem(listaId, itemId);
        lista.getItens().remove(item); // orphanRemoval remove apenas a associação, preservando o filme.
    }
}
