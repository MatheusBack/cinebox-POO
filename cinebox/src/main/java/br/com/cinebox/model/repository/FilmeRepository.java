package br.com.cinebox.model.repository;

import br.com.cinebox.model.entity.Filme;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface FilmeRepository extends JpaRepository<Filme, Long> {
    @EntityGraph(attributePaths = "generos")
    @Query("select distinct f from Filme f left join f.generos g where " +
           "(:termo = '' or lower(f.titulo) like lower(concat('%', :termo, '%')) or lower(f.diretor) like lower(concat('%', :termo, '%'))) " +
           "and (:generoId is null or g.id = :generoId) order by f.titulo")
    List<Filme> pesquisar(@Param("termo") String termo, @Param("generoId") Long generoId);
    @Override @EntityGraph(attributePaths = "generos")
    Optional<Filme> findById(Long id);
    boolean existsByGenerosId(Long generoId);
}
