package br.com.cinebox.security;

import br.com.cinebox.model.entity.Usuario;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/** Guarda um ID estável na sessão; nunca depende de um autor enviado pelo navegador. */
public class UsuarioAutenticado implements UserDetails, CredentialsContainer {
    private static final long serialVersionUID = 1L;
    private final Long id;
    private final String email;
    private final boolean habilitado;
    private String senhaHash;

    public UsuarioAutenticado(Usuario usuario) {
        id = usuario.getId(); email = usuario.getEmail(); senhaHash = usuario.getSenhaHash();
        habilitado = senhaHash != null && !senhaHash.isBlank();
    }
    public Long getId() { return id; }
    @Override public String getUsername() { return email; }
    @Override public String getPassword() { return senhaHash == null ? "" : senhaHash; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USUARIO"));
    }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return habilitado; }
    @Override public void eraseCredentials() { senhaHash = null; }
}
