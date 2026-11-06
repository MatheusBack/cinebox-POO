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
public class AvaliacaoService {
    private final AvaliacaoRepository repository;
    private final FilmeService filmes;
    private final UsuarioService usuarios;
    private final SessaoUsuarioService sessao;
    public AvaliacaoService(AvaliacaoRepository repository, FilmeService filmes, UsuarioService usuarios, SessaoUsuarioService sessao) {
        this.repository = repository; this.filmes = filmes; this.usuarios = usuarios; this.sessao = sessao;
    }
    public List<Avaliacao> listar() { return repository.findByUsuarioIdOrderByDataAssistidoDescIdDesc(sessao.exigirId()); }
    public List<Avaliacao> doFilme(Long filmeId) { return repository.findByFilmeIdOrderByDataAssistidoDescIdDesc(filmeId); }
    public long contar() { return repository.count(); }
    public Map<Long, Double> medias() {
        Map<Long, Double> resultado = new HashMap<>();
        repository.medias().forEach(m -> resultado.put(m.getFilmeId(), m.getMedia())); return resultado;
    }
    public Avaliacao buscar(Long id) { return repository.findByIdAndUsuarioId(id, sessao.exigirId()).orElseThrow(() -> new NaoEncontradoException("Avaliação não encontrada.")); }
    @Transactional
    public Avaliacao salvar(Long id, AvaliacaoForm form) {
        Long usuarioId = sessao.exigirId();
        // Verifica a propriedade antes de qualquer alteração, inclusive de campos do filme.
        Avaliacao a = id == null ? new Avaliacao() : buscar(id);
        if (form.getNota().remainder(new BigDecimal("0.5")).compareTo(BigDecimal.ZERO) != 0)
            throw new RegraNegocioException("Escolha uma nota em intervalos de 0,5: 0,5; 1; 1,5; até 5.");

        // Renomeando a variável do lambda de 'a' para 'existente' para evitar conflito de escopo
        repository.findByUsuarioIdAndFilmeId(usuarioId, form.getFilmeId())
                .filter(existente -> !Objects.equals(existente.getId(), id))
                .ifPresent(existente -> { throw new RegraNegocioException("Você já avaliou este filme. Edite a avaliação existente."); });

        a.setUsuario(usuarios.buscar(usuarioId));
        a.setFilme(filmes.buscar(form.getFilmeId()));
        a.setNota(form.getNota());
        a.setComentario(form.getComentario());
        a.setDataAssistido(form.getDataAssistido());
        a.setFavorito(form.isFavorito());
        a.setSpoiler(form.isSpoiler());
        return repository.save(a);
    }
    public AvaliacaoForm formulario(Long id) {
        Avaliacao a = buscar(id); AvaliacaoForm f = new AvaliacaoForm();
        f.setFilmeId(a.getFilme().getId()); f.setNota(a.getNota());
        f.setComentario(a.getComentario()); f.setDataAssistido(a.getDataAssistido()); f.setFavorito(a.isFavorito()); f.setSpoiler(a.isSpoiler()); return f;
    }
    @Transactional
    public void excluir(Long id) { repository.delete(buscar(id)); }
}
