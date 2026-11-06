package br.com.cinebox.security;

import java.util.Optional;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SessaoUsuarioService {
    public Optional<Long> idAtual() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UsuarioAutenticado usuario)
            return Optional.of(usuario.getId());
        return Optional.empty();
    }
    public Long exigirId() {
        return idAtual().orElseThrow(() -> new AuthenticationCredentialsNotFoundException("Entre na sua conta para continuar."));
    }
}
