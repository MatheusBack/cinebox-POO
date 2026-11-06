package br.com.cinebox;

import br.com.cinebox.model.service.UsuarioService;
import java.io.Console;
import java.util.Arrays;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CineboxApplication {
    public static void main(String[] args) {
        if (Arrays.asList(args).contains("--ativar-conta-legada")) { ativarContaLegada(); return; }
        SpringApplication.run(CineboxApplication.class, args);
    }
    private static void ativarContaLegada() {
        Console console = System.console();
        if (console == null) {
            System.err.println("Execute em um terminal interativo: java -jar target/cinebox-1.0.0.jar --ativar-conta-legada");
            return;
        }
        String email = console.readLine("E-mail da conta antiga: ");
        char[] senha = console.readPassword("Nova senha (mínimo 8 caracteres): ");
        char[] confirmacao = console.readPassword("Repita a senha: ");
        try {
            if (email == null || senha == null || confirmacao == null || !Arrays.equals(senha,confirmacao)) {
                console.printf("Operação cancelada: e-mail ausente ou senhas diferentes.%n"); return;
            }
            SpringApplication app = new SpringApplication(CineboxApplication.class);
            app.setWebApplicationType(WebApplicationType.NONE);
            // Não inicia servidor HTTP e não reaplica exemplos.
            try (var context = app.run("--app.dados-exemplo=false", "--spring.main.banner-mode=off")) {
                context.getBean(UsuarioService.class).ativarContaLegada(email,new String(senha));
                console.printf("Senha definida. O ID, as avaliações e as listas da conta foram preservados.%n");
            }
        } catch (br.com.cinebox.model.service.RegraNegocioException e) {
            console.printf("Não foi possível ativar: %s%n",e.getMessage());
        } finally {
            if (senha != null) Arrays.fill(senha,'\0');
            if (confirmacao != null) Arrays.fill(confirmacao,'\0');
        }
    }
}
