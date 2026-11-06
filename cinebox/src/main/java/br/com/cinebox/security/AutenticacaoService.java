package br.com.cinebox.security;

import br.com.cinebox.model.repository.UsuarioRepository;
import java.util.Locale;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AutenticacaoService implements UserDetailsService {
    private final UsuarioRepository usuarios;
    public AutenticacaoService(UsuarioRepository usuarios) { this.usuarios = usuarios; }

    @Override public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String normalizado = email == null ? "" : email.strip().toLowerCase(Locale.ROOT);
        return usuarios.findByEmailIgnoreCase(normalizado).map(UsuarioAutenticado::new)
            .orElseThrow(() -> new UsernameNotFoundException("E-mail ou senha inválidos."));
    }
}
