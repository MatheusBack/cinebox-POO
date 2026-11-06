package br.com.cinebox.config;

import br.com.cinebox.model.entity.*;
import br.com.cinebox.model.form.*;
import br.com.cinebox.model.service.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.*;

@Component
@ConditionalOnProperty(name = "app.dados-exemplo", havingValue = "true")
public class DadosExemplo implements CommandLineRunner {
    private final UsuarioService usuarios;
    private final GeneroService generos;
    private final FilmeService filmes;
    private final AvaliacaoService avaliacoes;
    private final DadosExemploService exemplos;
    public DadosExemplo(UsuarioService usuarios, GeneroService generos, FilmeService filmes, AvaliacaoService avaliacoes, DadosExemploService exemplos) {
        this.usuarios = usuarios; this.generos = generos; this.filmes = filmes; this.avaliacoes = avaliacoes; this.exemplos = exemplos;
    }
    @Override @Transactional
    public void run(String... args) {
        if (usuarios.contar() > 0 || filmes.contar() > 0 || !generos.listar().isEmpty()) { filmes.aplicarPostersPadrao(); return; }
        var ana = pessoa("Ana Martins", "ana", "Cinema, café e finais abertos.");
        var rafa = pessoa("Rafael Costa", "rafa", "Sempre cabe mais um filme na lista.");
        var lia = pessoa("Lia Santos", "lia", "Uma queda por animação e ficção científica.");
        Map<String, Long> gs = new LinkedHashMap<>();
        for (String nome : List.of("Drama", "Ficção científica", "Aventura", "Animação", "Suspense", "Crime", "Romance")) {
            GeneroForm f = new GeneroForm(); f.setNome(nome); gs.put(nome, generos.salvar(null, f).getId());
        }
        var interestelar = filme("Interestelar", "Christopher Nolan", 2014, 169, Capa.ORBITA,
            "Uma equipe atravessa o espaço em busca de um futuro para a humanidade, enquanto um pai tenta manter a conexão com sua filha.", gs, "Ficção científica", "Drama");
        var duna = filme("Duna: Parte Dois", "Denis Villeneuve", 2024, 166, Capa.AREIA,
            "Nas areias de Arrakis, Paul Atreides precisa escolher entre seus afetos e o destino que se desenha ao seu redor.", gs, "Ficção científica", "Aventura");
        var chihiro = filme("A Viagem de Chihiro", "Hayao Miyazaki", 2001, 125, Capa.VIAGEM,
            "Ao entrar em um mundo de espíritos, uma menina encontra coragem para ajudar seus pais e descobrir quem ela é.", gs, "Animação", "Aventura");
        var cidade = filme("Cidade de Deus", "Fernando Meirelles e Kátia Lund", 2002, 130, Capa.CIDADE,
            "Um jovem encontra na fotografia uma forma de observar a comunidade em que cresceu e imaginar outros caminhos.", gs, "Drama", "Crime");
        var parasita = filme("Parasita", "Bong Joon-ho", 2019, 132, Capa.CASA,
            "Duas famílias com realidades opostas passam a dividir uma casa e uma rede cada vez mais frágil de segredos.", gs, "Drama", "Suspense");
        var vidas = filme("Vidas Passadas", "Celine Song", 2023, 106, Capa.ENCONTROS,
            "Amigos de infância se reencontram depois de muitos anos e refletem sobre as vidas que escolheram e as que poderiam ter vivido.", gs, "Drama", "Romance");
        var whiplash = filme("Whiplash", "Damien Chazelle", 2014, 107, Capa.RITMO,
            "Um jovem baterista leva sua ambição ao limite sob a orientação de um professor tão exigente quanto imprevisível.", gs, "Drama");
        var chegada = filme("A Chegada", "Denis Villeneuve", 2016, 116, Capa.CONTATO,
            "Uma linguista tenta compreender visitantes misteriosos e descobre uma relação inesperada entre linguagem, tempo e memória.", gs, "Ficção científica", "Drama");
        avaliar(ana, interestelar, "5.0", "Alguns filmes parecem maiores a cada vez que a gente volta a eles.", 1, true);
        avaliar(rafa, duna, "4.5", "Som, escala e silêncio. Uma experiência que pede a maior tela possível.", 2, true);
        avaliar(lia, chihiro, "5.0", "A delicadeza de encontrar coragem nos pequenos gestos.", 3, true);
        avaliar(ana, vidas, "4.5", "Fiquei pensando em todas as conversas que continuam depois dos créditos.", 4, false);
        avaliar(rafa, parasita, "5.0", "Cada detalhe da casa conta uma parte da história.", 5, true);
        avaliar(lia, chegada, "4.5", "Uma ficção científica que encontra o seu centro nas pessoas.", 6, false);
        var l1 = lista("Para ver e sentir de novo", "Histórias que continuam com a gente depois dos créditos.", ana);
        item(l1, interestelar, 1); item(l1, vidas, 2); item(l1, chegada, 3); item(l1, chihiro, 4);
        var l2 = lista("Uma noite, muitos mundos", "Uma pequena viagem por universos que merecem uma visita.", lia);
        item(l2, duna, 1); item(l2, parasita, 2); item(l2, cidade, 3); item(l2, whiplash, 4);
        filmes.aplicarPostersPadrao();
    }
    private Usuario pessoa(String nome, String apelido, String bio) {
        return exemplos.pessoa(nome,apelido,bio);
    }
    private Filme filme(String titulo, String diretor, int ano, int minutos, Capa capa, String sinopse, Map<String, Long> gs, String... nomes) {
        FilmeForm f = new FilmeForm(); f.setTitulo(titulo); f.setDiretor(diretor); f.setAnoLancamento(ano); f.setDuracaoMinutos(minutos); f.setCapa(capa); f.setSinopse(sinopse);
        for (String nome : nomes) f.getGeneroIds().add(gs.get(nome)); return filmes.salvar(null, f);
    }
    private void avaliar(Usuario u, Filme filme, String nota, String texto, int dias, boolean favorito) {
        AvaliacaoForm f = new AvaliacaoForm(); f.setFilmeId(filme.getId()); f.setNota(new BigDecimal(nota)); f.setComentario(texto);
        f.setDataAssistido(LocalDate.now().minusDays(dias)); f.setFavorito(favorito); exemplos.avaliar(u, filme, f);
    }
    private ListaFilmes lista(String nome, String descricao, Usuario u) {
        return exemplos.lista(nome,descricao,u);
    }
    private void item(ListaFilmes lista, Filme filme, int posicao) {
        exemplos.item(lista,filme,posicao);
    }
}
