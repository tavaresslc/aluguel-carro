package br.com.carrent.dto;

import br.com.carrent.domain.Automovel;
import br.com.carrent.domain.Cliente;
import br.com.carrent.domain.PedidoAluguel;
import br.com.carrent.domain.PedidoAluguel.StatusPedido;
import br.com.carrent.domain.Usuario;
import br.com.carrent.domain.Usuario.Perfil;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record PedidoDTO(
        Long id,
        LocalDate dataSolicitacao,
        StatusPedido status,
        String observacoes,
        ClienteResumo cliente,
        AutomovelResumo automovel,
        UsuarioDTO.ContratanteDados contratante) {

    public static PedidoDTO de(PedidoAluguel pedido, boolean detalhado) {
        Cliente cliente = pedido.getCliente();
        UsuarioDTO.ContratanteDados contratante = detalhado
                ? UsuarioDTO.ContratanteDados.de(cliente.getContratante())
                : null;
        return new PedidoDTO(pedido.getId(), pedido.getDataSolicitacao(), pedido.getStatus(),
                pedido.getObservacoes(), ClienteResumo.de(cliente), AutomovelResumo.de(pedido.getAutomovel()),
                contratante);
    }

    public record ClienteResumo(Long id, String nome, String email) {
        public static ClienteResumo de(Cliente cliente) {
            return new ClienteResumo(cliente.getId(), cliente.getNome(), cliente.getEmail());
        }
    }

    public record AutomovelResumo(Long id, String marca, String modelo, Integer ano, String placa) {
        public static AutomovelResumo de(Automovel automovel) {
            return new AutomovelResumo(automovel.getId(), automovel.getMarca(), automovel.getModelo(),
                    automovel.getAno(), automovel.getPlaca());
        }
    }

    public record Cadastro(
            @NotNull(message = "Selecione um automóvel.") Long automovelId,
            @Size(max = 1000, message = "As observações devem ter no máximo 1000 caracteres.") String observacoes) {
    }

    public record Atualizacao(
            @NotNull(message = "Selecione um automóvel.") Long automovelId,
            @Size(max = 1000, message = "As observações devem ter no máximo 1000 caracteres.") String observacoes) {
    }

    public record AutomovelCadastro(
            @NotBlank(message = "A matrícula é obrigatória.") @Size(max = 30, message = "A matrícula deve ter no máximo 30 caracteres.") String matricula,

            @NotNull(message = "O ano é obrigatório.") @Min(value = 1900, message = "Ano inválido.") @Max(value = 2100, message = "Ano inválido.") Integer ano,

            @NotBlank(message = "A marca é obrigatória.") @Size(max = 60, message = "A marca deve ter no máximo 60 caracteres.") String marca,

            @NotBlank(message = "O modelo é obrigatório.") @Size(max = 80, message = "O modelo deve ter no máximo 80 caracteres.") String modelo,

            @NotBlank(message = "A placa é obrigatória.") @Pattern(regexp = "^[A-Za-z]{3}-?[0-9][A-Za-z0-9][0-9]{2}$", message = "Placa inválida (ex.: ABC1D23 ou ABC-1234).") String placa,

            Boolean disponivel) {
    }

    public record ProprietarioResumo(Long id, String nome, Perfil perfil) {
        public static ProprietarioResumo de(Usuario proprietario) {
            return new ProprietarioResumo(proprietario.getId(), proprietario.getNome(), proprietario.perfil());
        }
    }

    public record AutomovelResposta(
            Long id,
            String matricula,
            Integer ano,
            String marca,
            String modelo,
            String placa,
            boolean disponivel,
            ProprietarioResumo proprietario) {

        public static AutomovelResposta de(Automovel automovel, boolean disponivelAgora, boolean exibirProprietario) {
            ProprietarioResumo proprietario = exibirProprietario
                    ? ProprietarioResumo.de(automovel.getProprietario())
                    : null;
            return new AutomovelResposta(automovel.getId(), automovel.getMatricula(), automovel.getAno(),
                    automovel.getMarca(), automovel.getModelo(), automovel.getPlaca(), disponivelAgora, proprietario);
        }
    }
}
