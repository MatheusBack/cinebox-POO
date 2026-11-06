package br.com.cinebox.model.repository;

import br.com.cinebox.model.entity.ListaFilmes;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface ListaFilmesRepository extends JpaRepository<ListaFilmes, Long> {
    @EntityGraph(attributePaths = {"usuario", "itens.filme"})
    List<ListaFilmes> findAllByOrderByIdDesc();
    @Override @EntityGraph(attributePaths = {"usuario", "itens.filme"})
    Optional<ListaFilmes> findById(Long id);
    boolean existsByUsuarioId(Long usuarioId);
}
