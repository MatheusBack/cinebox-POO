package br.com.cinebox.model.repository;

import br.com.cinebox.model.entity.Genero;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface GeneroRepository extends JpaRepository<Genero, Long> {
    List<Genero> findAllByOrderByNomeAsc();
    Optional<Genero> findByNomeIgnoreCase(String nome);
}
