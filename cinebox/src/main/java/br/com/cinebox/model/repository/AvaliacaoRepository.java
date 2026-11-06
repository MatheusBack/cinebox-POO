package br.com.cinebox.model.repository;

import br.com.cinebox.model.entity.Avaliacao;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {
    @EntityGraph(attributePaths = {"usuario", "filme"})
    List<Avaliacao> findAllByOrderByDataAssistidoDescIdDesc();
    @Override @EntityGraph(attributePaths = {"usuario", "filme"})
    Optional<Avaliacao> findById(Long id);
    @EntityGraph(attributePaths = {"usuario", "filme"})
    List<Avaliacao> findByFilmeIdOrderByDataAssistidoDescIdDesc(Long filmeId);
    Optional<Avaliacao> findByUsuarioIdAndFilmeId(Long usuarioId, Long filmeId);
    @EntityGraph(attributePaths = {"usuario", "filme"})
    List<Avaliacao> findByUsuarioIdOrderByDataAssistidoDescIdDesc(Long usuarioId);
    @EntityGraph(attributePaths = {"usuario", "filme"})
    Optional<Avaliacao> findByIdAndUsuarioId(Long id, Long usuarioId);
    boolean existsByFilmeId(Long filmeId);
    boolean existsByUsuarioId(Long usuarioId);
    @Query("select a.filme.id as filmeId, avg(a.nota) as media from Avaliacao a group by a.filme.id")
    List<MediaFilme> medias();
    interface MediaFilme {
        Long getFilmeId();
        Double getMedia();
    }
}
