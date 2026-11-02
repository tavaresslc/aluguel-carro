package br.com.carrent.repository;

import br.com.carrent.domain.AvaliacaoFinanceira;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvaliacaoRepository extends JpaRepository<AvaliacaoFinanceira, Long> {

    @EntityGraph(attributePaths = "agente")
    List<AvaliacaoFinanceira> findByPedidoIdOrderByDataAvaliacaoDesc(Long pedidoId);
}
