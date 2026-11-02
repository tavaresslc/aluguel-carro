package br.com.carrent.service;

import br.com.carrent.domain.Agente;
import br.com.carrent.domain.AvaliacaoFinanceira;
import br.com.carrent.domain.AvaliacaoFinanceira.Parecer;
import br.com.carrent.domain.PedidoAluguel;
import br.com.carrent.domain.PedidoAluguel.StatusPedido;
import br.com.carrent.domain.Usuario;
import br.com.carrent.dto.AvaliacaoDTO;
import br.com.carrent.repository.AvaliacaoRepository;
import br.com.carrent.repository.UsuarioRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PedidoService pedidoService;

    @Transactional
    public AvaliacaoDTO registrar(Usuario autenticado, Long pedidoId, AvaliacaoDTO.Cadastro dto) {
        Agente agente = usuarioRepository.buscarAgentePorId(autenticado.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Somente agentes podem avaliar pedidos."));
        PedidoAluguel pedido = pedidoService.buscarComAcesso(autenticado, pedidoId);

        if (!pedido.podeSerModificado()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Pedidos cancelados ou com contrato executado não podem ser avaliados.");
        }
        if (dto.parecer() == Parecer.REPROVADO && (dto.justificativa() == null || dto.justificativa().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe a justificativa da reprovação.");
        }

        AvaliacaoFinanceira avaliacao = new AvaliacaoFinanceira();
        avaliacao.setPedido(pedido);
        avaliacao.setAgente(agente);
        if (dto.parecer() == Parecer.APROVADO) {
            avaliacao.aprovar(dto.justificativa());
            pedido.atualizarStatus(StatusPedido.APROVADO);
        } else {
            avaliacao.reprovar(dto.justificativa());
            pedido.atualizarStatus(StatusPedido.REPROVADO);
        }
        return AvaliacaoDTO.de(avaliacaoRepository.save(avaliacao));
    }

    @Transactional(readOnly = true)
    public List<AvaliacaoDTO> listar(Usuario autenticado, Long pedidoId) {
        pedidoService.buscarComAcesso(autenticado, pedidoId);
        return avaliacaoRepository.findByPedidoIdOrderByDataAvaliacaoDesc(pedidoId).stream()
                .map(AvaliacaoDTO::de)
                .toList();
    }
}
