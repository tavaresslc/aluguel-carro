package br.com.carrent.repository;

import br.com.carrent.domain.ContratoAluguel;
import br.com.carrent.domain.ContratoAluguel.StatusContrato;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContratoRepository extends JpaRepository<ContratoAluguel, Long> {

  @EntityGraph(attributePaths = { "pedido", "pedido.cliente", "automovel" })
  List<ContratoAluguel> findAllByOrderByIdDesc();

  @EntityGraph(attributePaths = { "pedido", "pedido.cliente", "automovel" })
  List<ContratoAluguel> findByPedidoClienteIdOrderByIdDesc(Long clienteId);

  @EntityGraph(attributePaths = { "pedido", "pedido.cliente", "automovel" })
  Optional<ContratoAluguel> findDetalheById(Long id);

  @Query("""
      select count(c) from ContratoAluguel c
      where c.automovel.id = :automovelId
        and c.status = :status
        and c.dataInicio <= :fim
        and c.dataFim >= :inicio
      """)
  long contarConflitos(@Param("automovelId") Long automovelId,
      @Param("status") StatusContrato status,
      @Param("inicio") LocalDate inicio,
      @Param("fim") LocalDate fim);

  @Query("""
      select distinct c.automovel.id from ContratoAluguel c
      where c.status = :status
        and c.dataInicio <= :hoje
        and c.dataFim >= :hoje
      """)
  List<Long> findAutomovelIdsEmUso(@Param("status") StatusContrato status, @Param("hoje") LocalDate hoje);
}
