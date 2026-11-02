package br.com.carrent.service;

import br.com.carrent.domain.Automovel;
import br.com.carrent.domain.Cliente;
import br.com.carrent.domain.PedidoAluguel;
import br.com.carrent.domain.PedidoAluguel.StatusPedido;
import br.com.carrent.domain.Usuario;
import br.com.carrent.domain.Usuario.Perfil;
import br.com.carrent.dto.PedidoDTO;
import br.com.carrent.repository.AutomovelRepository;
import br.com.carrent.repository.PedidoRepository;
import br.com.carrent.repository.UsuarioRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AutomovelRepository automovelRepository;
    private final AutomovelService automovelService;

    @Transactional
    public PedidoDTO criar(Usuario autenticado, PedidoDTO.Cadastro dto) {
        Cliente cliente = usuarioRepository.buscarClientePorId(autenticado.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Apenas clientes podem cadastrar pedidos."));
        if (!cliente.getContratante().possuiDadosCompletos()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Complete seus dados de contratante (nome, RG, CPF, endereço, profissão e ao menos um "
                            + "vínculo empregatício) antes de solicitar um aluguel.");
        }
        Automovel automovel = buscarAutomovelDisponivel(dto.automovelId());

        PedidoAluguel pedido = new PedidoAluguel();
        pedido.setCliente(cliente);
        pedido.setAutomovel(automovel);
        pedido.setDataSolicitacao(LocalDate.now(PedidoAluguel.FUSO_HORARIO));
        pedido.setStatus(StatusPedido.PENDENTE);
        pedido.setObservacoes(dto.observacoes());
        return PedidoDTO.de(pedidoRepository.save(pedido), false);
    }

    @Transactional(readOnly = true)
    public List<PedidoDTO> listar(Usuario autenticado, StatusPedido status) {
        List<PedidoAluguel> pedidos;
        if (autenticado.perfil() == Perfil.CLIENTE) {
            pedidos = status == null
                    ? pedidoRepository.findByClienteIdOrderByIdDesc(autenticado.getId())
                    : pedidoRepository.findByClienteIdAndStatusOrderByIdDesc(autenticado.getId(), status);
        } else {
            pedidos = status == null
                    ? pedidoRepository.findAllByOrderByIdDesc()
                    : pedidoRepository.findByStatusOrderByIdDesc(status);
        }
        return pedidos.stream().map(p -> PedidoDTO.de(p, false)).toList();
    }

    @Transactional(readOnly = true)
    public PedidoDTO buscar(Usuario autenticado, Long id) {
        return PedidoDTO.de(buscarComAcesso(autenticado, id), true);
    }

    @Transactional
    public PedidoDTO atualizar(Usuario autenticado, Long id, PedidoDTO.Atualizacao dto) {
        PedidoAluguel pedido = buscarComAcesso(autenticado, id);
        if (!pedido.podeSerModificado()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Pedidos cancelados ou com contrato executado não podem ser modificados.");
        }
        Automovel novoAutomovel = null;
        if (!pedido.getAutomovel().getId().equals(dto.automovelId())) {
            novoAutomovel = buscarAutomovelDisponivel(dto.automovelId());
        }
        pedido.modificar(novoAutomovel, dto.observacoes());
        return PedidoDTO.de(pedido, false);
    }

    @Transactional
    public PedidoDTO cancelar(Usuario autenticado, Long id) {
        if (autenticado.perfil() != Perfil.CLIENTE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas clientes podem cancelar pedidos.");
        }
        PedidoAluguel pedido = buscarComAcesso(autenticado, id);
        if (!pedido.podeSerModificado()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Pedidos cancelados ou com contrato executado não podem ser cancelados.");
        }
        pedido.cancelar();
        return PedidoDTO.de(pedido, false);
    }

    @Transactional(readOnly = true)
    public PedidoAluguel buscarComAcesso(Usuario autenticado, Long id) {
        PedidoAluguel pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado."));
        if (autenticado.perfil() == Perfil.CLIENTE && !pedido.getCliente().getId().equals(autenticado.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado.");
        }
        return pedido;
    }

    private Automovel buscarAutomovelDisponivel(Long automovelId) {
        Automovel automovel = automovelRepository.findById(automovelId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Automóvel não encontrado."));
        if (!automovelService.estaDisponivel(automovel)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O automóvel selecionado não está disponível.");
        }
        return automovel;
    }
}
