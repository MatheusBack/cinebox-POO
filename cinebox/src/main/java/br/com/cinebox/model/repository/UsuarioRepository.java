package br.com.cinebox.model.repository;

import br.com.cinebox.model.entity.Usuario;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    List<Usuario> findAllByOrderByNomeAsc();
    Optional<Usuario> findByApelidoIgnoreCase(String apelido);
    Optional<Usuario> findByEmailIgnoreCase(String email);
}
