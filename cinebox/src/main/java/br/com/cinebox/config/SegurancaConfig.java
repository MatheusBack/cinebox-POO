package br.com.cinebox.config;

import jakarta.servlet.DispatcherType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class SegurancaConfig {
    @Bean
    public SecurityFilterChain seguranca(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                .requestMatchers("/login", "/cadastro", "/css/**", "/js/**", "/img/**", "/error").permitAll()
                .anyRequest().authenticated())
            .formLogin(login -> login.loginPage("/login").loginProcessingUrl("/login")
                .usernameParameter("email").passwordParameter("senha")
                .defaultSuccessUrl("/perfil", true).failureUrl("/login?erro").permitAll())
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST"))
                .invalidateHttpSession(true).clearAuthentication(true).deleteCookies("JSESSIONID")
                .logoutSuccessUrl("/login?saiu"))
            .sessionManagement(session -> session.sessionFixation(fixation -> fixation.migrateSession()))
            .exceptionHandling(errors -> errors.accessDeniedPage("/acesso-negado"));
        // CSRF continua habilitado; Thymeleaf adiciona o token aos formulários POST.
        return http.build();
    }
}
