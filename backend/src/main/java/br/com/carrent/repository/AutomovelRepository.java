package br.com.carrent.repository;

import br.com.carrent.domain.Automovel;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AutomovelRepository extends JpaRepository<Automovel, Long> {

    @EntityGraph(attributePaths = "proprietario")
    List<Automovel> findAllByOrderByIdDesc();

    boolean existsByPlacaIgnoreCase(String placa);

    boolean existsByMatriculaIgnoreCase(String matricula);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Automovel a where a.id = :id")
    Optional<Automovel> buscarParaContratar(@Param("id") Long id);
}
