package br.com.carrent.repository;

import br.com.carrent.domain.Credito;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditoRepository extends JpaRepository<Credito, Long> {

    boolean existsByContratoId(Long contratoId);

    @EntityGraph(attributePaths = { "banco", "contrato" })
    Optional<Credito> findByContratoId(Long contratoId);

    @EntityGraph(attributePaths = { "banco", "contrato" })
    List<Credito> findByContratoIdIn(Collection<Long> contratoIds);
}
