package br.com.cinebox.model.service;

import br.com.cinebox.model.entity.*;
import br.com.cinebox.model.form.*;
import br.com.cinebox.model.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.math.BigDecimal;
import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(readOnly = true)
public class FilmeService {
    private final FilmeRepository repository;
    private final GeneroRepository generos;
    private final AvaliacaoRepository avaliacoes;
    private final ItemListaRepository itens;
    public FilmeService(FilmeRepository repository, GeneroRepository generos, AvaliacaoRepository avaliacoes, ItemListaRepository itens) {
        this.repository = repository; this.generos = generos; this.avaliacoes = avaliacoes; this.itens = itens;
    }
    public List<Filme> listar(String termo, Long generoId) { return repository.pesquisar(termo == null ? "" : termo.strip(), generoId); }
    public long contar() { return repository.count(); }
    public Filme buscar(Long id) { return repository.findById(id).orElseThrow(() -> new NaoEncontradoException("Filme não encontrado.")); }
    @Transactional
    public Filme salvar(Long id, FilmeForm form) {
        Set<Genero> selecionados = new LinkedHashSet<>(generos.findAllById(form.getGeneroIds()));
        if (selecionados.size() != form.getGeneroIds().size()) throw new RegraNegocioException("Um dos gêneros selecionados não existe mais.");
        if (form.getNovoGenero() != null) {
            for (String parte : form.getNovoGenero().split(",")) {
                String nome = parte.strip();
                if (nome.isEmpty()) continue;
                if (nome.length() > 40) throw new RegraNegocioException("Cada gênero novo deve ter até 40 caracteres.");
                Genero g = generos.findByNomeIgnoreCase(nome).orElseGet(() -> {
                    Genero novo = new Genero(); novo.setNome(nome); return generos.save(novo);
                });
                selecionados.add(g);
            }
        }
        if (selecionados.isEmpty()) throw new RegraNegocioException("Selecione ao menos um gênero ou digite um novo.");
        Filme filme = id == null ? new Filme() : buscar(id);
        filme.setTitulo(form.getTitulo().strip()); filme.setDiretor(form.getDiretor().strip());
        filme.setAnoLancamento(form.getAnoLancamento()); filme.setDuracaoMinutos(form.getDuracaoMinutos());
        filme.setSinopse(form.getSinopse().strip()); filme.setCapa(form.getCapa()); filme.setGeneros(selecionados);
        MultipartFile imagem = form.getImagem();
        if (imagem != null && !imagem.isEmpty()) {
            String tipo = imagem.getContentType();
            if (tipo == null || !List.of("image/jpeg", "image/png", "image/webp").contains(tipo))
                throw new RegraNegocioException("Envie uma imagem JPG, PNG ou WEBP.");
            if (imagem.getSize() > 2 * 1024 * 1024)
                throw new RegraNegocioException("A imagem deve ter no máximo 2 MB.");
            try { filme.setImagem(imagem.getBytes()); filme.setImagemTipo(tipo); }
            catch (IOException e) { throw new RegraNegocioException("Não foi possível ler a imagem enviada."); }
        }
        return repository.save(filme);
    }
    public FilmeForm formulario(Long id) {
        Filme filme = buscar(id); FilmeForm f = new FilmeForm();
        f.setTitulo(filme.getTitulo()); f.setDiretor(filme.getDiretor()); f.setAnoLancamento(filme.getAnoLancamento());
        f.setDuracaoMinutos(filme.getDuracaoMinutos()); f.setSinopse(filme.getSinopse()); f.setCapa(filme.getCapa());
        filme.getGeneros().forEach(g -> f.getGeneroIds().add(g.getId())); return f;
    }
    @Transactional
    public int aplicarPostersPadrao() {
        int aplicados = 0;
        String[][] formatos = {{"jpg", "image/jpeg"}, {"jpeg", "image/jpeg"}, {"png", "image/png"}, {"webp", "image/webp"}};
        for (Filme filme : repository.findAll()) {
            if (filme.getImagem() != null) continue; // nunca sobrescreve imagem já salva ou enviada pelo usuário
            String slug = slug(filme.getTitulo());
            for (String[] formato : formatos) {
                ClassPathResource recurso = new ClassPathResource("posters/" + slug + "." + formato[0]);
                if (!recurso.exists()) continue;
                try (InputStream in = recurso.getInputStream()) {
                    byte[] bytes = in.readAllBytes();
                    if (bytes.length <= 2 * 1024 * 1024) {
                        filme.setImagem(bytes); filme.setImagemTipo(formato[1]); repository.save(filme); aplicados++;
                    }
                } catch (IOException e) { /* pôster ilegível: o filme continua com a capa ilustrada */ }
                break;
            }
        }
        return aplicados;
    }
    static String slug(String titulo) {
        return Normalizer.normalize(titulo, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }
    @Transactional
    public void excluir(Long id) {
        Filme filme = buscar(id);
        if (avaliacoes.existsByFilmeId(id) || itens.existsByFilmeId(id)) throw new RegraNegocioException("Remova as avaliações e os itens de listas deste filme antes de excluí-lo.");
        repository.delete(filme);
    }
}
