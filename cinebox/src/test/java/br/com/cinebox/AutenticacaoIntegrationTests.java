package br.com.cinebox;

import br.com.cinebox.controller.*;
import br.com.cinebox.model.entity.*;
import br.com.cinebox.model.form.*;
import br.com.cinebox.model.repository.*;
import br.com.cinebox.model.service.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Testes reais de filtros, sessão, views e H2 em memória. Nenhuma transação cobre a renderização. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AutenticacaoIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired UsuarioService usuarios;
    @Autowired UsuarioRepository usuariosRepo;
    @Autowired FilmeService filmes;
    @Autowired GeneroService generos;
    @Autowired AvaliacaoRepository avaliacoes;
    @Autowired ListaFilmesRepository listas;
    @Autowired ItemListaRepository itens;
    @Autowired PasswordEncoder encoder;
    Usuario ana, bruno;
    Genero genero;
    Filme filmeAna, filmeBruno;
    private static final String SENHA = "SenhaTeste!123";

    @BeforeEach void preparar() {
        for (String tabela : new String[]{"itens_lista","avaliacoes","listas","filme_genero","filmes","generos","usuarios"})
            jdbc.update("delete from "+tabela);
        cadastrarDireto("Ana Teste","ana_teste","ana@example.com");
        cadastrarDireto("Bruno Teste","bruno_teste","bruno@example.com");
        ana=usuariosRepo.findByEmailIgnoreCase("ana@example.com").orElseThrow();
        bruno=usuariosRepo.findByEmailIgnoreCase("bruno@example.com").orElseThrow();
        GeneroForm g=new GeneroForm(); g.setNome("Drama"); genero=generos.salvar(null,g);
        filmeAna=novoFilme("Filme exclusivo da Ana"); filmeBruno=novoFilme("Filme exclusivo do Bruno");
    }
    private void cadastrarDireto(String nome,String apelido,String email) {
        CadastroForm f=new CadastroForm(); f.setNome(nome);f.setApelido(apelido);f.setEmail(email);
        f.setSenha(SENHA);f.setConfirmarSenha(SENHA);usuarios.cadastrar(f);
    }
    private Filme novoFilme(String titulo) {
        FilmeForm f=new FilmeForm();f.setTitulo(titulo);f.setDiretor("Direção Teste");f.setAnoLancamento(2024);
        f.setDuracaoMinutos(100);f.setSinopse("Uma história de teste.");f.setGeneroIds(Set.of(genero.getId()));return filmes.salvar(null,f);
    }
    private MockHttpSession entrar(String email) throws Exception {
        var result=mvc.perform(post("/login").with(csrf()).param("email",email).param("senha",SENHA))
            .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/perfil"))
            .andExpect(authenticated()).andReturn();
        return (MockHttpSession)result.getRequest().getSession(false);
    }
    private Avaliacao avaliarDireto(Usuario u,Filme f,String nota) {
        Avaliacao a=new Avaliacao();a.setUsuario(u);a.setFilme(f);a.setNota(new BigDecimal(nota));
        a.setComentario("Comentário de teste.");a.setDataAssistido(LocalDate.now());return avaliacoes.save(a);
    }
    private ListaFilmes listaDireta(Usuario u) {
        ListaFilmes l=new ListaFilmes();l.setNome("Lista de "+u.getNome());l.setUsuario(u);return listas.save(l);
    }

    @Test void cadastraPeloFormularioEPersisteSomenteHash() throws Exception {
        mvc.perform(get("/cadastro")).andExpect(status().isOk()).andExpect(content().string(containsString("name="_csrf"")));
        mvc.perform(post("/cadastro").with(csrf()).param("nome","Carla Teste").param("apelido","carla_teste")
            .param("email","Carla@Example.com").param("senha",SENHA).param("confirmarSenha",SENHA))
            .andExpect(redirectedUrl("/login")).andExpect(unauthenticated());
        var u=usuariosRepo.findByEmailIgnoreCase("carla@example.com").orElseThrow();
        assertThat(u.getEmail()).isEqualTo("carla@example.com");assertThat(u.getSenhaHash()).isNotEqualTo(SENHA);
        assertThat(encoder.matches(SENHA,u.getSenhaHash())).isTrue();
        entrar("carla@example.com");
    }
    @Test void loginValidoIdentificaContaELoginInvalidoFalha() throws Exception {
        mvc.perform(post("/login").with(csrf()).param("email","ana@example.com").param("senha","errada"))
            .andExpect(redirectedUrl("/login?erro")).andExpect(unauthenticated());
        var sessao=entrar(" ANA@EXAMPLE.COM ");
        mvc.perform(get("/perfil").session(sessao)).andExpect(status().isOk()).andExpect(content().string(containsString("Ana Teste")));
    }
    @Test void anonimoNaoAcessaFuncionalidadesPrincipais() throws Exception {
        for(String url:new String[]{"/","/filmes","/filmes/novo","/avaliacoes","/avaliacoes/novo","/perfil","/perfil/foto","/perfil/editar","/listas","/pessoas","/generos"})
            mvc.perform(get(url)).andExpect(status().is3xxRedirection()).andExpect(redirectedUrlPattern("**/login"));
        mvc.perform(post("/avaliacoes").with(csrf()).param("filmeId",filmeAna.getId().toString()).param("nota","5")
            .param("dataAssistido",LocalDate.now().toString())).andExpect(status().is3xxRedirection());
        assertThat(avaliacoes.count()).isZero();
    }
    @Test void formularioNaoTemSeletorDeAutor() throws Exception {
        var sessao=entrar("ana@example.com");
        mvc.perform(get("/avaliacoes/novo").session(sessao)).andExpect(status().isOk())
            .andExpect(content().string(not(containsString("name="usuarioId""))))
            .andExpect(content().string(not(containsString("for="usuarioId""))));
    }
    @Test void criaAvaliacaoComAutorDaSessaoIgnorandoAutorForjado() throws Exception {
        var sessao=entrar("ana@example.com");
        mvc.perform(post("/avaliacoes").session(sessao).with(csrf()).param("usuarioId",bruno.getId().toString())
            .param("filmeId",filmeAna.getId().toString()).param("nota","4.5").param("dataAssistido",LocalDate.now().toString()))
            .andExpect(redirectedUrl("/avaliacoes"));
        var a=avaliacoes.findAll().get(0);assertThat(a.getUsuario().getId()).isEqualTo(ana.getId());assertThat(a.getFilme().getId()).isEqualTo(filmeAna.getId());
        assertThat(a.getNota()).isEqualByComparingTo("4.5");
    }
    @Test void perfilEDiarioMostramSomenteAvaliacoesDaConta() throws Exception {
        avaliarDireto(ana,filmeAna,"4.5");avaliarDireto(bruno,filmeBruno,"2.0");
        for(String url:new String[]{"/perfil","/avaliacoes"}) {
            mvc.perform(get(url).session(entrar("ana@example.com"))).andExpect(status().isOk())
                .andExpect(content().string(containsString(filmeAna.getTitulo()))).andExpect(content().string(not(containsString(filmeBruno.getTitulo()))));
            mvc.perform(get(url).session(entrar("bruno@example.com"))).andExpect(status().isOk())
                .andExpect(content().string(containsString(filmeBruno.getTitulo()))).andExpect(content().string(not(containsString(filmeAna.getTitulo()))));
        }
    }
    @Test void sessaoNaoPodeEditarOuExcluirAvaliacaoDeOutraConta() throws Exception {
        var a=avaliarDireto(ana,filmeAna,"4.5");var sessao=entrar("bruno@example.com");
        mvc.perform(get("/avaliacoes/"+a.getId()+"/editar").session(sessao)).andExpect(status().isNotFound());
        mvc.perform(get("/avaliacoes/"+a.getId()+"/excluir").session(sessao)).andExpect(status().isNotFound());
        mvc.perform(post("/avaliacoes/"+a.getId()).session(sessao).with(csrf()).param("filmeId",filmeAna.getId().toString())
            .param("nota","1").param("dataAssistido",LocalDate.now().toString())).andExpect(status().isNotFound());
        mvc.perform(post("/avaliacoes/"+a.getId()+"/excluir").session(sessao).with(csrf())).andExpect(status().isNotFound());
        assertThat(avaliacoes.findById(a.getId()).orElseThrow().getNota()).isEqualByComparingTo("4.5");
    }
    @Test void autorPodeEditarExcluirESemDuplicarFilme() throws Exception {
        var a=avaliarDireto(ana,filmeAna,"4.5");var sessao=entrar("ana@example.com");
        mvc.perform(post("/avaliacoes").session(sessao).with(csrf()).param("filmeId",filmeAna.getId().toString())
            .param("nota","5").param("dataAssistido",LocalDate.now().toString())).andExpect(status().isOk())
            .andExpect(content().string(containsString("Você já avaliou")));
        mvc.perform(post("/avaliacoes/"+a.getId()).session(sessao).with(csrf()).param("filmeId",filmeAna.getId().toString())
            .param("nota","5").param("dataAssistido",LocalDate.now().toString())).andExpect(redirectedUrl("/avaliacoes"));
        assertThat(avaliacoes.findById(a.getId()).orElseThrow().getNota()).isEqualByComparingTo("5.0");
        mvc.perform(post("/avaliacoes/"+a.getId()+"/excluir").session(sessao).with(csrf())).andExpect(redirectedUrl("/avaliacoes"));
        assertThat(avaliacoes.count()).isZero();
    }
    @Test void logoutInvalidaSessaoENovoLoginTrocaPerfil() throws Exception {
        var sessao=entrar("ana@example.com");
        mvc.perform(post("/logout").session(sessao).with(csrf())).andExpect(redirectedUrl("/login?saiu")).andExpect(unauthenticated());
        assertThat(sessao.isInvalid()).isTrue();
        mvc.perform(get("/perfil")).andExpect(redirectedUrlPattern("**/login"));
        mvc.perform(get("/perfil").session(entrar("bruno@example.com"))).andExpect(status().isOk())
            .andExpect(content().string(containsString("Bruno Teste"))).andExpect(content().string(not(containsString("ana@example.com"))));
    }
    @Test void csrfProtegeEscritasELogout() throws Exception {
        var sessao=entrar("ana@example.com");
        mvc.perform(post("/logout").session(sessao)).andExpect(status().isForbidden());
        mvc.perform(post("/avaliacoes").session(sessao).param("filmeId",filmeAna.getId().toString()).param("nota","5")
            .param("dataAssistido",LocalDate.now().toString())).andExpect(status().isForbidden());
        mvc.perform(get("/perfil").session(sessao)).andExpect(status().isOk());assertThat(avaliacoes.count()).isZero();
    }
    @Test void cadastroRecusaSenhaDiferenteCurtaOuMaiorQue72Bytes() throws Exception {
        mvc.perform(post("/cadastro").with(csrf()).param("nome","Outra").param("apelido","outra").param("email","outra@example.com")
            .param("senha","SenhaValida123").param("confirmarSenha","Diferente123")).andExpect(status().isOk())
            .andExpect(model().attributeHasErrors("form")).andExpect(content().string(not(containsString("value="SenhaValida123""))));
        mvc.perform(post("/cadastro").with(csrf()).param("nome","Outra").param("apelido","outra").param("email","outra@example.com")
            .param("senha","curta").param("confirmarSenha","curta")).andExpect(status().isOk()).andExpect(model().attributeHasErrors("form"));
        String longa="á".repeat(40);
        mvc.perform(post("/cadastro").with(csrf()).param("nome","Outra").param("apelido","outra").param("email","outra@example.com")
            .param("senha",longa).param("confirmarSenha",longa)).andExpect(status().isOk()).andExpect(content().string(containsString("72 bytes")));
        assertThat(usuariosRepo.count()).isEqualTo(2);
    }
    @Test void cadastroRecusaEmailEApelidoDuplicados() throws Exception {
        mvc.perform(post("/cadastro").with(csrf()).param("nome","Outra").param("apelido","outra").param("email","ANA@EXAMPLE.COM")
            .param("senha",SENHA).param("confirmarSenha",SENHA)).andExpect(status().isOk()).andExpect(content().string(containsString("e-mail já está cadastrado")));
        mvc.perform(post("/cadastro").with(csrf()).param("nome","Outra").param("apelido","ANA_TESTE").param("email","outra@example.com")
            .param("senha",SENHA).param("confirmarSenha",SENHA)).andExpect(status().isOk()).andExpect(content().string(containsString("apelido já está em uso")));
    }
    @Test void perfilSemFotoUsaImagemGenerica() throws Exception {
        var sessao=entrar("ana@example.com");
        mvc.perform(get("/perfil").session(sessao)).andExpect(status().isOk()).andExpect(content().string(containsString("/img/avatar.svg")));
        mvc.perform(get("/perfil/foto").session(sessao)).andExpect(status().isNotFound());
    }
    @Test void editaFotoNoBancoESomenteOProprioPerfil() throws Exception {
        var sessao=entrar("ana@example.com");
        var bytes=new ByteArrayOutputStream();ImageIO.write(new BufferedImage(16,16,BufferedImage.TYPE_INT_RGB),"png",bytes);
        var foto=new MockMultipartFile("foto","foto.png","image/png",bytes.toByteArray());
        mvc.perform(multipart("/perfil/editar").file(foto).session(sessao).with(csrf()).param("nome","Ana Atualizada")
            .param("apelido","ana_teste").param("email","ana_nova@example.com").param("bio","Bio nova")
            .param("id",bruno.getId().toString()).param("senhaHash","valor-forjado"))
            .andExpect(redirectedUrl("/perfil"));
        var atual=usuariosRepo.findById(ana.getId()).orElseThrow();assertThat(atual.getNome()).isEqualTo("Ana Atualizada");
        assertThat(atual.getFoto()).isNotEmpty();assertThat(encoder.matches(SENHA,atual.getSenhaHash())).isTrue();
        assertThat(usuariosRepo.findById(bruno.getId()).orElseThrow().getNome()).isEqualTo("Bruno Teste");
        mvc.perform(get("/perfil/foto").session(sessao)).andExpect(status().isOk()).andExpect(content().contentType("image/png"))
            .andExpect(header().string("Cache-Control",containsString("no-store")));
        mvc.perform(get("/perfil").session(sessao)).andExpect(status().isOk()).andExpect(content().string(containsString("ana_nova@example.com")));
        entrar("ana_nova@example.com");
        mvc.perform(multipart("/perfil/editar").session(sessao).with(csrf()).param("nome","Ana Atualizada")
            .param("apelido","ana_teste").param("email","ana_nova@example.com").param("removerFoto","true"))
            .andExpect(redirectedUrl("/perfil"));
        assertThat(usuariosRepo.findById(ana.getId()).orElseThrow().getFoto()).isNull();
    }
    @Test void rejeitaFotoFalsaGrandeOuComDimensoesExcessivas() throws Exception {
        var sessao=entrar("ana@example.com");
        var dimensoes=new ByteArrayOutputStream();ImageIO.write(new BufferedImage(2050,1,BufferedImage.TYPE_INT_RGB),"png",dimensoes);
        for(var foto:new MockMultipartFile[]{new MockMultipartFile("foto","foto.jpg","image/jpeg","<script>x</script>".getBytes()),
                new MockMultipartFile("foto","grande.png","image/png",new byte[2*1024*1024+1]),
                new MockMultipartFile("foto","larga.png","image/png",dimensoes.toByteArray())})
            mvc.perform(multipart("/perfil/editar").file(foto).session(sessao).with(csrf()).param("nome","Ana Teste")
                .param("apelido","ana_teste").param("email","ana@example.com")).andExpect(status().isOk())
                .andExpect(model().attributeHasErrors("form"));
        assertThat(usuariosRepo.findById(ana.getId()).orElseThrow().getFoto()).isNull();
    }
    @Test void perfisPublicosNaoExpoemEmailNemPermitemGerenciarOutros() throws Exception {
        var sessao=entrar("ana@example.com");
        mvc.perform(get("/pessoas").session(sessao)).andExpect(status().isOk()).andExpect(content().string(containsString("Bruno Teste")))
            .andExpect(content().string(not(containsString("bruno@example.com"))));
        mvc.perform(get("/pessoas/"+bruno.getId()+"/editar").session(sessao)).andExpect(status().isForbidden());
        mvc.perform(post("/pessoas/"+bruno.getId()).session(sessao).with(csrf()).param("nome","Forjado"))
            .andExpect(status().is4xxClientError());
        assertThat(usuariosRepo.findById(bruno.getId()).orElseThrow().getNome()).isEqualTo("Bruno Teste");
    }
    @Test void listasTambemUsamAutorAutenticadoEProtegemItens() throws Exception {
        var sessao=entrar("ana@example.com");
        mvc.perform(post("/listas").session(sessao).with(csrf()).param("nome","Minha coleção").param("usuarioId",bruno.getId().toString()))
            .andExpect(status().is3xxRedirection());
        var l=listas.findAll().get(0);assertThat(l.getUsuario().getId()).isEqualTo(ana.getId());
        mvc.perform(post("/listas/"+l.getId()+"/itens").session(sessao).with(csrf()).param("filmeId",filmeAna.getId().toString()).param("posicao","1"))
            .andExpect(redirectedUrl("/listas/"+l.getId()));
        var item=itens.findAll().get(0);var outro=entrar("bruno@example.com");
        mvc.perform(get("/listas/"+l.getId()).session(outro)).andExpect(status().isOk())
            .andExpect(content().string(not(containsString("Editar lista")))).andExpect(content().string(not(containsString("Adicionar à lista"))));
        mvc.perform(post("/listas/"+l.getId()).session(outro).with(csrf()).param("nome","Forjada")).andExpect(status().isNotFound());
        mvc.perform(post("/listas/"+l.getId()+"/itens/"+item.getId()+"/excluir").session(outro).with(csrf())).andExpect(status().isNotFound());
        mvc.perform(post("/listas/"+l.getId()+"/excluir").session(outro).with(csrf())).andExpect(status().isNotFound());
        assertThat(itens.count()).isEqualTo(1);
        mvc.perform(post("/listas/"+l.getId()+"/excluir").session(sessao).with(csrf())).andExpect(redirectedUrl("/listas"));
        assertThat(itens.count()).isZero();assertThat(filmes.contar()).isEqualTo(2);
    }
    @Test void contaLegadaSemSenhaNaoEntraMasAvaliacoesContinuamVisiveis() throws Exception {
        var antiga=new Usuario();antiga.setNome("Pessoa antiga");antiga.setApelido("antiga");antiga.setEmail("antiga@example.com");usuariosRepo.save(antiga);
        var a=avaliarDireto(antiga,filmeAna,"3.5");
        mvc.perform(post("/login").with(csrf()).param("email","antiga@example.com").param("senha",SENHA)).andExpect(unauthenticated()).andExpect(redirectedUrl("/login?erro"));
        usuarios.ativarContaLegada("antiga@example.com",SENHA);
        mvc.perform(get("/perfil").session(entrar("antiga@example.com"))).andExpect(status().isOk()).andExpect(content().string(containsString(filmeAna.getTitulo())));
        assertThat(avaliacoes.findById(a.getId()).orElseThrow().getUsuario().getId()).isEqualTo(antiga.getId());
        assertThatThrownBy(()->usuarios.ativarContaLegada("antiga@example.com",SENHA)).isInstanceOf(RegraNegocioException.class);
        mvc.perform(post("/ativar-conta-legada").with(csrf())).andExpect(status().is3xxRedirection()); // Não existe ativação HTTP.
    }
    @Test void excluirContaRequerSenhaSemVinculosEEncerraSessao() throws Exception {
        var sessao=entrar("ana@example.com");
        mvc.perform(post("/perfil/excluir").session(sessao).with(csrf()).param("senha","errada")).andExpect(status().isOk());
        assertThat(usuariosRepo.existsById(ana.getId())).isTrue();
        var a=avaliarDireto(ana,filmeAna,"4");
        mvc.perform(post("/perfil/excluir").session(sessao).with(csrf()).param("senha",SENHA)).andExpect(status().isOk()).andExpect(content().string(containsString("Remova suas avaliações")));
        avaliacoes.deleteById(a.getId());
        mvc.perform(post("/perfil/excluir").session(sessao).with(csrf()).param("senha",SENHA)).andExpect(redirectedUrl("/login?contaExcluida"));
        assertThat(sessao.isInvalid()).isTrue();assertThat(usuariosRepo.existsById(ana.getId())).isFalse();assertThat(usuariosRepo.existsById(bruno.getId())).isTrue();
    }
    @Test void renderizaTelasExistentesAposIntegracao() throws Exception {
        var sessao=entrar("ana@example.com");var a=avaliarDireto(ana,filmeAna,"5");var l=listaDireta(ana);
        var item=new ItemLista();item.setLista(l);item.setFilme(filmeAna);item.setPosicao(1);itens.save(item);
        for(String url:new String[]{"/filmes","/filmes/novo","/filmes/"+filmeAna.getId(),"/filmes/"+filmeAna.getId()+"/editar","/filmes/"+filmeAna.getId()+"/excluir",
                "/generos","/generos/novo","/generos/"+genero.getId()+"/editar","/generos/"+genero.getId()+"/excluir","/pessoas","/perfil","/perfil/editar","/perfil/excluir",
                "/avaliacoes","/avaliacoes/novo","/avaliacoes/"+a.getId()+"/editar","/avaliacoes/"+a.getId()+"/excluir","/listas","/listas/novo",
                "/listas/"+l.getId(),"/listas/"+l.getId()+"/editar","/listas/"+l.getId()+"/excluir",
                "/listas/"+l.getId()+"/itens/"+item.getId()+"/editar","/listas/"+l.getId()+"/itens/"+item.getId()+"/excluir"})
            mvc.perform(get(url).session(sessao)).andExpect(status().isOk());
    }
    @Test void mantemCrudFilmesGenerosEBusca() throws Exception {
        var sessao=entrar("ana@example.com");
        mvc.perform(post("/generos").session(sessao).with(csrf()).param("nome","Aventura")).andExpect(redirectedUrl("/generos"));
        var g=generos.listar().stream().filter(x->x.getNome().equals("Aventura")).findFirst().orElseThrow();
        mvc.perform(post("/generos/"+g.getId()).session(sessao).with(csrf()).param("nome","Ação")).andExpect(redirectedUrl("/generos"));
        mvc.perform(post("/filmes").session(sessao).with(csrf()).param("titulo","Novo Filme").param("diretor","Direção")
            .param("anoLancamento","2024").param("duracaoMinutos","100").param("sinopse","Sinopse.").param("capa","AREIA").param("generoIds",g.getId().toString()))
            .andExpect(redirectedUrl("/filmes"));
        var f=filmes.listar("Novo Filme",g.getId()).get(0);
        mvc.perform(post("/filmes/"+f.getId()).session(sessao).with(csrf()).param("titulo","Filme Editado").param("diretor","Direção")
            .param("anoLancamento","2023").param("duracaoMinutos","99").param("sinopse","Outra sinopse.").param("capa","ORBITA").param("generoIds",g.getId().toString()))
            .andExpect(redirectedUrl("/filmes"));
        assertThat(filmes.listar("EDITADO",null)).hasSize(1);
        mvc.perform(post("/filmes/"+f.getId()+"/excluir").session(sessao).with(csrf())).andExpect(redirectedUrl("/filmes"));
        mvc.perform(post("/generos/"+g.getId()+"/excluir").session(sessao).with(csrf())).andExpect(redirectedUrl("/generos"));
    }
    @Test void mantemValidacoesEProtecaoDeVinculos() throws Exception {
        var sessao=entrar("ana@example.com");
        mvc.perform(post("/avaliacoes").session(sessao).with(csrf()).param("filmeId",filmeAna.getId().toString()).param("nota","4.3")
            .param("dataAssistido",LocalDate.now().toString())).andExpect(status().isOk()).andExpect(model().attributeHasErrors("form"));
        mvc.perform(post("/avaliacoes").session(sessao).with(csrf()).param("filmeId",filmeAna.getId().toString()).param("nota","5")
            .param("dataAssistido",LocalDate.now().plusDays(1).toString())).andExpect(status().isOk()).andExpect(model().attributeHasErrors("form"));
        avaliarDireto(ana,filmeAna,"5");
        mvc.perform(post("/filmes/"+filmeAna.getId()+"/excluir").session(sessao).with(csrf())).andExpect(flash().attributeExists("erro"));
        mvc.perform(post("/generos/"+genero.getId()+"/excluir").session(sessao).with(csrf())).andExpect(flash().attributeExists("erro"));
    }
    @Test void comentariosContinuamEscapados() throws Exception {
        var a=avaliarDireto(ana,filmeAna,"5");a.setComentario("<script>alert('teste')</script>");avaliacoes.save(a);
        mvc.perform(get("/avaliacoes").session(entrar("ana@example.com"))).andExpect(status().isOk()).andExpect(content().string(containsString("&lt;script&gt;")));
    }
    @Test void controllersNaoDependemDeRepositories() {
        for(var controller:new Class<?>[]{AutenticacaoController.class,PerfilController.class,UsuarioController.class,FilmeController.class,GeneroController.class,ListaController.class,AvaliacaoController.class,ContaNaNavegacao.class})
            for(var field:controller.getDeclaredFields())assertThat(field.getType().getPackageName()).doesNotContain("repository");
    }
}
