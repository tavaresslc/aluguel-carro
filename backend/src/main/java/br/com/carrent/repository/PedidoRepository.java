package br.com.carrent.repository;

import br.com.carrent.domain.PedidoAluguel;
import br.com.carrent.domain.PedidoAluguel.StatusPedido;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<PedidoAluguel, Long> {

    @EntityGraph(attributePaths = { "cliente", "automovel" })
    List<PedidoAluguel> findAllByOrderByIdDesc();

    @EntityGraph(attributePaths = { "cliente", "automovel" })
    List<PedidoAluguel> findByStatusOrderByIdDesc(StatusPedido status);

    @EntityGraph(attributePaths = { "cliente", "automovel" })
    List<PedidoAluguel> findByClienteIdOrderByIdDesc(Long clienteId);

    @EntityGraph(attributePaths = { "cliente", "automovel" })
    List<PedidoAluguel> findByClienteIdAndStatusOrderByIdDesc(Long clienteId, StatusPedido status);
}
