package br.com.carrent.dto;

import br.com.carrent.domain.Agente;
import br.com.carrent.domain.Cliente;
import br.com.carrent.domain.Contratante;
import br.com.carrent.domain.Usuario;
import br.com.carrent.domain.Usuario.Perfil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

public record UsuarioDTO(
                Long id,
                String nome,
                String email,
                Perfil perfil,
                String nomeInstituicao,
                ContratanteDados contratante) {

        public static UsuarioDTO de(Usuario usuario) {
                ContratanteDados contratante = null;
                String nomeInstituicao = null;
                if (usuario instanceof Cliente cliente && cliente.getContratante() != null) {
                        contratante = ContratanteDados.de(cliente.getContratante());
                }
                if (usuario instanceof Agente agente) {
                        nomeInstituicao = agente.getNomeInstituicao();
                }
                return new UsuarioDTO(usuario.getId(), usuario.getNome(), usuario.getEmail(),
                                usuario.perfil(), nomeInstituicao, contratante);
        }

        public record Cadastro(
                        @NotBlank(message = "O nome é obrigatório.") @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres.") String nome,

                        @NotBlank(message = "O e-mail é obrigatório.") @Email(message = "E-mail inválido.") @Size(max = 160, message = "O e-mail deve ter no máximo 160 caracteres.") String email,

                        @NotBlank(message = "A senha é obrigatória.") @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres.") String senha) {
        }

        public record Login(
                        @NotBlank(message = "O e-mail é obrigatório.") String email,
                        @NotBlank(message = "A senha é obrigatória.") String senha) {
        }

        public record LoginResposta(String token, String tipoToken, long expiraEmSegundos, UsuarioDTO usuario) {
        }

        public record ContratanteAtualizacao(
                        @NotBlank(message = "O nome é obrigatório.") @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres.") String nome,

                        @NotBlank(message = "O RG é obrigatório.") @Size(max = 20, message = "O RG deve ter no máximo 20 caracteres.") String rg,

                        @NotBlank(message = "O CPF é obrigatório.") @Pattern(regexp = "^[0-9.\\-]{11,14}$", message = "CPF inválido.") String cpf,

                        @NotBlank(message = "O endereço é obrigatório.") @Size(max = 255, message = "O endereço deve ter no máximo 255 caracteres.") String endereco,

                        @NotBlank(message = "A profissão é obrigatória.") @Size(max = 100, message = "A profissão deve ter no máximo 100 caracteres.") String profissao,

                        @NotNull(message = "Informe a lista de vínculos (pode ser vazia).") @Size(max = 3, message = "Um contratante pode possuir no máximo 3 vínculos empregatícios.") @Valid List<VinculoDados> vinculos) {
        }

        public record VinculoDados(
                        @NotBlank(message = "A entidade empregadora é obrigatória.") @Size(max = 150, message = "A entidade empregadora deve ter no máximo 150 caracteres.") String entidadeEmpregadora,

                        @NotNull(message = "O rendimento é obrigatório.") @DecimalMin(value = "0.0", message = "O rendimento não pode ser negativo.") @Digits(integer = 13, fraction = 2, message = "Rendimento inválido.") BigDecimal rendimento) {
        }

        public record ContratanteDados(
                        Long id,
                        String nome,
                        String rg,
                        String cpf,
                        String endereco,
                        String profissao,
                        List<VinculoDados> vinculos,
                        BigDecimal rendaTotal,
                        boolean dadosCompletos) {

                public static ContratanteDados de(Contratante contratante) {
                        List<VinculoDados> vinculos = contratante.getVinculos().stream()
                                        .map(v -> new VinculoDados(v.getEntidadeEmpregadora(), v.getRendimento()))
                                        .toList();
                        return new ContratanteDados(contratante.getId(), contratante.getNome(), contratante.getRg(),
                                        contratante.getCpf(), contratante.getEndereco(), contratante.getProfissao(),
                                        vinculos,
                                        contratante.calcularRendaTotal(), contratante.possuiDadosCompletos());
                }
        }
}
