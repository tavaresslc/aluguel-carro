package br.com.carrent.service;

import br.com.carrent.domain.Agente;
import br.com.carrent.domain.Agente.TipoAgente;
import br.com.carrent.domain.Cliente;
import br.com.carrent.domain.Contratante;
import br.com.carrent.domain.Contratante.VinculoEmpregaticio;
import br.com.carrent.domain.Usuario;
import br.com.carrent.dto.UsuarioDTO;
import br.com.carrent.repository.UsuarioRepository;
import br.com.carrent.security.PasswordEncoder;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    @Transactional(readOnly = true)
    public UsuarioDTO buscarPerfil(Usuario autenticado) {
        Usuario usuario = usuarioRepository.findById(autenticado.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
        return UsuarioDTO.de(usuario);
    }

    @Transactional
    public UsuarioDTO atualizarContratante(Usuario autenticado, UsuarioDTO.ContratanteAtualizacao dto) {
        Cliente cliente = usuarioRepository.buscarClientePorId(autenticado.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Apenas clientes possuem dados de contratante."));

        if (dto.vinculos().size() > Contratante.MAXIMO_VINCULOS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Um contratante pode possuir no máximo " + Contratante.MAXIMO_VINCULOS
                            + " vínculos empregatícios.");
        }

        String cpf = dto.cpf().replaceAll("\\D", "");
        if (!cpfValido(cpf)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CPF inválido.");
        }
        if (usuarioRepository.contarOutrosClientesComCpf(cpf, cliente.getId()) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este CPF já está cadastrado para outro cliente.");
        }

        List<VinculoEmpregaticio> vinculos = dto.vinculos().stream()
                .map(v -> new VinculoEmpregaticio(v.entidadeEmpregadora().trim(), v.rendimento()))
                .toList();

        String nome = dto.nome().trim();
        Contratante contratante = cliente.getContratante();
        contratante.atualizarDados(nome, dto.rg().trim(), cpf, dto.endereco().trim(), dto.profissao().trim());
        contratante.definirVinculos(vinculos);
        cliente.atualizarDados(nome);

        return UsuarioDTO.de(cliente);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void criarAgentesIniciais() {
        semear("banco", TipoAgente.BANCO);
        semear("empresa", TipoAgente.EMPRESA);
    }

    private void semear(String chave, TipoAgente tipo) {
        String prefixo = "app.seed." + chave + ".";
        String nome = environment.getProperty(prefixo + "nome", "");
        String email = environment.getProperty(prefixo + "email", "");
        String senha = environment.getProperty(prefixo + "senha", "");
        if (nome.isBlank() || email.isBlank() || senha.isBlank()) {
            return;
        }
        String emailNormalizado = AuthService.normalizarEmail(email);
        if (usuarioRepository.existsByEmailIgnoreCase(emailNormalizado)) {
            return;
        }
        Agente agente = new Agente();
        agente.setNome(nome.trim());
        agente.setNomeInstituicao(nome.trim());
        agente.setEmail(emailNormalizado);
        agente.setSenhaHash(passwordEncoder.encode(senha));
        agente.setTipo(tipo);
        usuarioRepository.save(agente);
        log.info("Agente inicial ({}) criado: {}", tipo, emailNormalizado);
    }

    private static boolean cpfValido(String cpf) {
        if (cpf.length() != 11 || cpf.chars().distinct().count() == 1) {
            return false;
        }
        return digitoVerificador(cpf, 9) == cpf.charAt(9) - '0'
                && digitoVerificador(cpf, 10) == cpf.charAt(10) - '0';
    }

    private static int digitoVerificador(String cpf, int quantidadeDigitos) {
        int soma = 0;
        for (int i = 0; i < quantidadeDigitos; i++) {
            soma += (cpf.charAt(i) - '0') * (quantidadeDigitos + 1 - i);
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }
}
