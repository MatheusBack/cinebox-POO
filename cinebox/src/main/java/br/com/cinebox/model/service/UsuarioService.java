package br.com.cinebox.model.service;

import br.com.cinebox.model.entity.Usuario;
import br.com.cinebox.model.form.*;
import br.com.cinebox.model.repository.*;
import br.com.cinebox.security.SessaoUsuarioService;
import java.time.LocalDate;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(readOnly = true)
public class UsuarioService {
    public record Perfil(Long id, String nome, String apelido, String email, String bio, LocalDate dataCadastro, boolean temFoto) {}
    public record PessoaPublica(Long id, String nome, String apelido, String bio, LocalDate dataCadastro) {}
    private final UsuarioRepository repository;
    private final AvaliacaoRepository avaliacoes;
    private final ListaFilmesRepository listas;
    private final PasswordEncoder senhas;
    private final SessaoUsuarioService sessao;
    private final FotoPerfilService fotos;

    public UsuarioService(UsuarioRepository repository, AvaliacaoRepository avaliacoes, ListaFilmesRepository listas,
                          PasswordEncoder senhas, SessaoUsuarioService sessao, FotoPerfilService fotos) {
        this.repository = repository; this.avaliacoes = avaliacoes; this.listas = listas;
        this.senhas = senhas; this.sessao = sessao; this.fotos = fotos;
    }
    public long contar() { return repository.count(); }
    public Usuario buscar(Long id) {
        return repository.findById(id).orElseThrow(() -> new NaoEncontradoException("Pessoa não encontrada."));
    }
    public List<PessoaPublica> listar() {
        return repository.findAllByOrderByNomeAsc().stream()
            .map(u -> new PessoaPublica(u.getId(), u.getNome(), u.getApelido(), u.getBio(), u.getDataCadastro())).toList();
    }
    public Perfil perfilAtual() {
        Usuario u = buscar(sessao.exigirId());
        return new Perfil(u.getId(),u.getNome(),u.getApelido(),u.getEmail(),u.getBio(),u.getDataCadastro(),u.isTemFoto());
    }
    private void verificarUnicos(Long id, UsuarioForm form) {
        repository.findByApelidoIgnoreCase(form.getApelido().strip()).filter(u -> !Objects.equals(u.getId(), id))
            .ifPresent(u -> { throw new RegraNegocioException("Esse apelido já está em uso."); });
        repository.findByEmailIgnoreCase(form.getEmail().strip()).filter(u -> !Objects.equals(u.getId(), id))
            .ifPresent(u -> { throw new RegraNegocioException("Esse e-mail já está cadastrado."); });
    }
    private void preencher(Usuario u, UsuarioForm f) {
        u.setNome(f.getNome().strip()); u.setApelido(f.getApelido().strip().toLowerCase(Locale.ROOT));
        u.setEmail(f.getEmail().strip().toLowerCase(Locale.ROOT)); u.setBio(f.getBio());
    }
    public void validarSenha(String senha) {
        if (senha == null || senha.isBlank() || senha.length() < 8 || senha.getBytes(StandardCharsets.UTF_8).length > 72)
            throw new RegraNegocioException("Use uma senha de pelo menos 8 caracteres e no máximo 72 bytes UTF-8.");
    }
    @Transactional
    public void cadastrar(CadastroForm form) {
        validarSenha(form.getSenha());
        if (!Objects.equals(form.getSenha(), form.getConfirmarSenha())) throw new RegraNegocioException("As senhas não conferem.");
        verificarUnicos(null,form);
        Usuario u = new Usuario(); preencher(u,form); u.setSenhaHash(senhas.encode(form.getSenha()));
        repository.save(u);
    }
    public UsuarioForm formularioAtual() {
        Usuario u = buscar(sessao.exigirId()); UsuarioForm f = new UsuarioForm();
        f.setNome(u.getNome()); f.setApelido(u.getApelido()); f.setEmail(u.getEmail()); f.setBio(u.getBio()); return f;
    }
    @Transactional
    public void atualizarPerfil(UsuarioForm form, MultipartFile arquivo, boolean removerFoto) {
        Long id = sessao.exigirId(); verificarUnicos(id,form);
        if (removerFoto && arquivo != null && !arquivo.isEmpty())
            throw new RegraNegocioException("Escolha remover a foto ou enviar outra, não as duas opções juntas.");
        var foto = fotos.validar(arquivo);
        Usuario u = buscar(id); preencher(u,form);
        if (removerFoto) { u.setFoto(null); u.setFotoTipo(null); }
        else if (foto != null) { u.setFoto(foto.bytes()); u.setFotoTipo(foto.tipo()); }
        repository.save(u);
    }
    public FotoPerfilService.Foto fotoAtual() {
        Usuario u = buscar(sessao.exigirId());
        return u.isTemFoto() ? new FotoPerfilService.Foto(u.getFoto(),u.getFotoTipo()) : null;
    }
    @Transactional
    public void excluirConta(String senha) {
        Long id = sessao.exigirId(); Usuario u = buscar(id);
        if (senha == null || !senhas.matches(senha,u.getSenhaHash())) throw new RegraNegocioException("A senha não confere.");
        if (avaliacoes.existsByUsuarioId(id) || listas.existsByUsuarioId(id))
            throw new RegraNegocioException("Remova suas avaliações e listas antes de excluir a conta.");
        repository.delete(u);
    }
    /** Operação administrativa LOCAL, acessível apenas pelo comando interativo, sem rota HTTP. */
    @Transactional
    public void ativarContaLegada(String email, String novaSenha) {
        validarSenha(novaSenha);
        Usuario u = repository.findByEmailIgnoreCase(email.strip())
            .orElseThrow(() -> new RegraNegocioException("Nenhuma conta encontrada para esse e-mail."));
        if (u.getSenhaHash() != null && !u.getSenhaHash().isBlank())
            throw new RegraNegocioException("A conta já tem senha. A ativação local não redefine senhas existentes.");
        u.setSenhaHash(senhas.encode(novaSenha)); repository.save(u);
    }
}
