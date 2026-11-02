package br.com.carrent.dto;

import br.com.carrent.domain.ContratoAluguel;
import br.com.carrent.domain.ContratoAluguel.StatusContrato;
import br.com.carrent.domain.Credito;
import br.com.carrent.domain.Credito.StatusCredito;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ContratoDTO(
                Long id,
                Long pedidoId,
                PedidoDTO.ClienteResumo cliente,
                PedidoDTO.AutomovelResumo automovel,
                LocalDate dataInicio,
                LocalDate dataFim,
                BigDecimal valor,
                StatusContrato status,
                CreditoResposta credito) {

        public static ContratoDTO de(ContratoAluguel contrato, Credito credito) {
                return new ContratoDTO(contrato.getId(), contrato.getPedido().getId(),
                                PedidoDTO.ClienteResumo.de(contrato.getPedido().getCliente()),
                                PedidoDTO.AutomovelResumo.de(contrato.getAutomovel()),
                                contrato.getDataInicio(), contrato.getDataFim(), contrato.getValor(),
                                contrato.getStatus(),
                                credito == null ? null : CreditoResposta.de(credito));
        }

        public record Cadastro(
                        @NotNull(message = "A data de início é obrigatória.") LocalDate dataInicio,
                        @NotNull(message = "A data de término é obrigatória.") LocalDate dataFim,
                        @NotNull(message = "O valor é obrigatório.") @Positive(message = "O valor deve ser maior que zero.") @Digits(integer = 13, fraction = 2, message = "Valor inválido.") BigDecimal valor) {
        }

        public record CreditoCadastro(
                        @NotNull(message = "O valor do crédito é obrigatório.") @Positive(message = "O valor do crédito deve ser maior que zero.") @Digits(integer = 13, fraction = 2, message = "Valor inválido.") BigDecimal valor,

                        @NotNull(message = "A taxa de juros (% ao mês) é obrigatória.") @DecimalMin(value = "0.0", message = "A taxa de juros não pode ser negativa.") @DecimalMax(value = "100.0", message = "A taxa de juros não pode passar de 100% ao mês.") @Digits(integer = 3, fraction = 4, message = "Taxa de juros inválida.") BigDecimal taxaJuros,

                        @NotNull(message = "A quantidade de parcelas é obrigatória.") @Min(value = 1, message = "Informe ao menos 1 parcela.") @Max(value = 120, message = "No máximo 120 parcelas.") Integer quantidadeParcelas) {
        }

        public record CreditoResposta(
                        Long id,
                        Long contratoId,
                        Long bancoId,
                        String bancoNome,
                        BigDecimal valor,
                        BigDecimal taxaJuros,
                        Integer quantidadeParcelas,
                        BigDecimal valorParcela,
                        StatusCredito status) {

                public static CreditoResposta de(Credito credito) {
                        return new CreditoResposta(credito.getId(), credito.getContrato().getId(),
                                        credito.getBanco().getId(), credito.getBanco().getNome(), credito.getValor(),
                                        credito.getTaxaJuros(), credito.getQuantidadeParcelas(),
                                        credito.calcularParcelas(),
                                        credito.getStatus());
                }
        }
}
