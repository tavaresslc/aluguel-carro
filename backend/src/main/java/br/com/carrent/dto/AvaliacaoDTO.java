package br.com.carrent.dto;

import br.com.carrent.domain.Agente;
import br.com.carrent.domain.AvaliacaoFinanceira;
import br.com.carrent.domain.AvaliacaoFinanceira.Parecer;
import br.com.carrent.domain.Usuario.Perfil;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record AvaliacaoDTO(
                Long id,
                Long pedidoId,
                Long agenteId,
                String agenteNome,
                String instituicao,
                Perfil tipoAgente,
                LocalDateTime dataAvaliacao,
                Parecer parecer,
                String justificativa) {

        public static AvaliacaoDTO de(AvaliacaoFinanceira avaliacao) {
                Agente agente = avaliacao.getAgente();
                return new AvaliacaoDTO(avaliacao.getId(), avaliacao.getPedido().getId(), agente.getId(),
                                agente.getNome(), agente.getNomeInstituicao(), agente.perfil(),
                                avaliacao.getDataAvaliacao(), avaliacao.getParecer(), avaliacao.getJustificativa());
        }

        public record Cadastro(
                        @NotNull(message = "Informe o parecer (APROVADO ou REPROVADO).") Parecer parecer,
                        @Size(max = 1000, message = "A justificativa deve ter no máximo 1000 caracteres.") String justificativa) {
        }
}
