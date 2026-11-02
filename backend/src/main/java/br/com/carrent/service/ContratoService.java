package br.com.carrent.service;

import br.com.carrent.domain.Automovel;
import br.com.carrent.domain.ContratoAluguel;
import br.com.carrent.domain.ContratoAluguel.StatusContrato;
import br.com.carrent.domain.Credito;
import br.com.carrent.domain.PedidoAluguel;
import br.com.carrent.domain.PedidoAluguel.StatusPedido;
import br.com.carrent.domain.Usuario;
import br.com.carrent.domain.Usuario.Perfil;
import br.com.carrent.dto.ContratoDTO;
import br.com.carrent.repository.AutomovelRepository;
import br.com.carrent.repository.ContratoRepository;
import br.com.carrent.repository.CreditoRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ContratoService {

    private final ContratoRepository contratoRepository;
    private final CreditoRepository creditoRepository;
    private final AutomovelRepository automovelRepository;
    private final PedidoService pedidoService;

    @Transactional
    public ContratoDTO executar(Usuario autenticado, Long pedidoId, ContratoDTO.Cadastro dto) {
        if (!autenticado.isAgente()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Somente agentes podem executar contratos.");
        }
        PedidoAluguel pedido = pedidoService.buscarComAcesso(autenticado, pedidoId);
        if (pedido.getStatus() != StatusPedido.APROVADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Somente pedidos com parecer financeiro positivo (APROVADO) podem ser contratados.");
        }
        if (dto.dataFim().isBefore(dto.dataInicio())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A data de término não pode ser anterior à data de início.");
        }
        if (dto.dataInicio().isBefore(LocalDate.now(PedidoAluguel.FUSO_HORARIO))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A data de início não pode estar no passado.");
        }

        Automovel automovel = automovelRepository.buscarParaContratar(pedido.getAutomovel().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Automóvel não encontrado."));
        if (!automovel.isDisponivel()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O automóvel não está habilitado para locação.");
        }
        if (contratoRepository.contarConflitos(automovel.getId(), StatusContrato.EXECUTADO,
                dto.dataInicio(), dto.dataFim()) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "O automóvel já possui contrato executado que cruza o período informado.");
        }

        ContratoAluguel contrato = new ContratoAluguel();
        contrato.setPedido(pedido);
        contrato.setAutomovel(automovel);
        contrato.setDataInicio(dto.dataInicio());
        contrato.setDataFim(dto.dataFim());
        contrato.setValor(dto.valor());
        contrato.executar();
        pedido.atualizarStatus(StatusPedido.CONTRATADO);

        return ContratoDTO.de(contratoRepository.save(contrato), null);
    }

    @Transactional(readOnly = true)
    public List<ContratoDTO> listar(Usuario autenticado) {
        List<ContratoAluguel> contratos = autenticado.perfil() == Perfil.CLIENTE
                ? contratoRepository.findByPedidoClienteIdOrderByIdDesc(autenticado.getId())
                : contratoRepository.findAllByOrderByIdDesc();
        if (contratos.isEmpty()) {
            return List.of();
        }
        List<Long> ids = contratos.stream().map(ContratoAluguel::getId).toList();
        Map<Long, Credito> creditos = creditoRepository.findByContratoIdIn(ids).stream()
                .collect(Collectors.toMap(c -> c.getContrato().getId(), Function.identity()));
        return contratos.stream().map(c -> ContratoDTO.de(c, creditos.get(c.getId()))).toList();
    }

    @Transactional(readOnly = true)
    public ContratoDTO buscar(Usuario autenticado, Long id) {
        ContratoAluguel contrato = buscarComAcesso(autenticado, id);
        Credito credito = creditoRepository.findByContratoId(id).orElse(null);
        return ContratoDTO.de(contrato, credito);
    }

    @Transactional(readOnly = true)
    public ContratoAluguel buscarComAcesso(Usuario autenticado, Long id) {
        ContratoAluguel contrato = contratoRepository.findDetalheById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contrato não encontrado."));
        boolean contratoDeOutroCliente = autenticado.perfil() == Perfil.CLIENTE
                && !contrato.getPedido().getCliente().getId().equals(autenticado.getId());
        if (contratoDeOutroCliente) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Contrato não encontrado.");
        }
        return contrato;
    }
}
