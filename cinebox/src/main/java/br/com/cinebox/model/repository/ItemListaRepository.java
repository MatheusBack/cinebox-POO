package br.com.cinebox.model.repository;

import br.com.cinebox.model.entity.ItemLista;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface ItemListaRepository extends JpaRepository<ItemLista, Long> {
    boolean existsByFilmeId(Long filmeId);
    Optional<ItemLista> findByListaIdAndFilmeId(Long listaId, Long filmeId);
}
