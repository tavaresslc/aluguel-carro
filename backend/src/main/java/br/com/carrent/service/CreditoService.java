package br.com.carrent.service;

import br.com.carrent.domain.Agente;
import br.com.carrent.domain.ContratoAluguel;
import br.com.carrent.domain.ContratoAluguel.StatusContrato;
import br.com.carrent.domain.Credito;
import br.com.carrent.domain.Usuario;
import br.com.carrent.dto.ContratoDTO;
import br.com.carrent.repository.CreditoRepository;
import br.com.carrent.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CreditoService {

    private final CreditoRepository creditoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ContratoService contratoService;

    @Transactional
    public ContratoDTO.CreditoResposta conceder(Usuario autenticado, Long contratoId,
            ContratoDTO.CreditoCadastro dto) {
        Agente banco = usuarioRepository.buscarAgentePorId(autenticado.getId())
                .filter(Agente::isBanco)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Somente bancos podem conceder crédito."));
        ContratoAluguel contrato = contratoService.buscarComAcesso(autenticado, contratoId);

        if (contrato.getStatus() != StatusContrato.EXECUTADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "O crédito só pode ser concedido para contratos executados.");
        }
        if (creditoRepository.existsByContratoId(contratoId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este contrato já possui um crédito associado.");
        }
        if (dto.valor().compareTo(contrato.getValor()) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O valor do crédito não pode exceder o valor do contrato.");
        }

        Credito credito = new Credito();
        credito.setContrato(contrato);
        credito.setBanco(banco);
        credito.setValor(dto.valor());
        credito.setTaxaJuros(dto.taxaJuros());
        credito.setQuantidadeParcelas(dto.quantidadeParcelas());
        credito.aprovar();

        return ContratoDTO.CreditoResposta.de(creditoRepository.save(credito));
    }
}
