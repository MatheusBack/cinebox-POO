package br.com.cinebox.model.service;

import br.com.cinebox.model.entity.*;
import br.com.cinebox.model.form.*;
import br.com.cinebox.model.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.math.BigDecimal;

@Service
@Transactional(readOnly = true)
public class GeneroService {
    private final GeneroRepository repository;
    private final FilmeRepository filmes;
    public GeneroService(GeneroRepository repository, FilmeRepository filmes) { this.repository = repository; this.filmes = filmes; }
    public List<Genero> listar() { return repository.findAllByOrderByNomeAsc(); }
    public Genero buscar(Long id) { return repository.findById(id).orElseThrow(() -> new NaoEncontradoException("Gênero não encontrado.")); }
    @Transactional
    public Genero salvar(Long id, GeneroForm form) {
        String nome = form.getNome().strip();
        repository.findByNomeIgnoreCase(nome).filter(g -> !Objects.equals(g.getId(), id)).ifPresent(g -> { throw new RegraNegocioException("Esse gênero já está cadastrado."); });
        Genero genero = id == null ? new Genero() : buscar(id);
        genero.setNome(nome); return repository.save(genero);
    }
    public GeneroForm formulario(Long id) { GeneroForm f = new GeneroForm(); f.setNome(buscar(id).getNome()); return f; }
    @Transactional
    public void excluir(Long id) {
        Genero genero = buscar(id);
        if (filmes.existsByGenerosId(id)) throw new RegraNegocioException("Esse gênero está associado a filmes. Edite os filmes e remova a associação antes de excluí-lo.");
        repository.delete(genero);
    }
}
