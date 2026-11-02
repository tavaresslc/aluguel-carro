package br.com.carrent.repository;

import br.com.carrent.domain.Agente;
import br.com.carrent.domain.Cliente;
import br.com.carrent.domain.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    @Query("select c from Cliente c where c.id = :id")
    Optional<Cliente> buscarClientePorId(@Param("id") Long id);

    @Query("select a from Agente a where a.id = :id")
    Optional<Agente> buscarAgentePorId(@Param("id") Long id);

    @Query("select count(c) from Cliente c where c.contratante.cpf = :cpf and c.id <> :clienteId")
    long contarOutrosClientesComCpf(@Param("cpf") String cpf, @Param("clienteId") Long clienteId);
}
