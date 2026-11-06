package br.com.cinebox;

import br.com.cinebox.model.service.UsuarioService;
import br.com.cinebox.security.AutenticacaoService;
import java.sql.DriverManager;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.assertj.core.api.Assertions.*;

/** Testa ddl-auto=update contra o esquema original sem tocar no arquivo data/cinebox. */
class CompatibilidadeLegadoTests {
    @Test void adicionaCredenciaisSemPerderIdsAvaliacoesOuListas() throws Exception {
        String url="jdbc:h2:mem:legado_"+UUID.randomUUID()+";DB_CLOSE_DELAY=-1";
        try(var con=DriverManager.getConnection(url,"sa","");var sql=con.createStatement()) {
            ScriptUtils.executeSqlScript(con,new ClassPathResource("schema-legado.sql"));
            sql.executeUpdate("insert into usuarios(id,nome,apelido,email,data_cadastro) values(101,'Legado','legado','legado@example.com',CURRENT_DATE)");
            sql.executeUpdate("insert into generos(id,nome) values(101,'Drama')");
            sql.executeUpdate("insert into filmes(id,titulo,diretor,ano_lancamento,duracao_minutos,sinopse,capa) values(101,'Filme antigo','Direção',2020,100,'Sinopse','ORBITA')");
            sql.executeUpdate("insert into filme_genero(filme_id,genero_id) values(101,101)");
            sql.executeUpdate("insert into avaliacoes(id,usuario_id,filme_id,nota,data_assistido,favorito,spoiler) values(101,101,101,4.5,CURRENT_DATE,false,false)");
            sql.executeUpdate("insert into listas(id,usuario_id,nome) values(101,101,'Lista antiga')");
            sql.executeUpdate("insert into itens_lista(id,lista_id,filme_id,posicao) values(101,101,101,1)");
        }
        try(var context=new SpringApplicationBuilder(CineboxApplication.class).web(WebApplicationType.NONE).run(
                "--spring.datasource.url="+url,"--spring.datasource.username=sa","--spring.datasource.password=",
                "--spring.jpa.hibernate.ddl-auto=update","--app.dados-exemplo=false","--spring.main.banner-mode=off")) {
            var jdbc=context.getBean(JdbcTemplate.class);
            assertThat(jdbc.queryForObject("select senha_hash from usuarios where id=101",String.class)).isNull();
            context.getBean(UsuarioService.class).ativarContaLegada("legado@example.com","NovaSenha!123");
            var usuario=context.getBean(AutenticacaoService.class).loadUserByUsername("legado@example.com");
            assertThat(usuario.isEnabled()).isTrue();
            assertThat(context.getBean(PasswordEncoder.class).matches("NovaSenha!123",usuario.getPassword())).isTrue();
            assertThat(jdbc.queryForObject("select usuario_id from avaliacoes where id=101",Long.class)).isEqualTo(101L);
            assertThat(jdbc.queryForObject("select usuario_id from listas where id=101",Long.class)).isEqualTo(101L);
            assertThat(jdbc.queryForObject("select count(*) from itens_lista",Long.class)).isEqualTo(1L);
            assertThat(jdbc.queryForObject("select data_cadastro from usuarios where id=101",java.sql.Date.class)).isNotNull();
        }
    }
}
