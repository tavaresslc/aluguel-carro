package br.com.carrent.service;

import br.com.carrent.domain.Automovel;
import br.com.carrent.domain.ContratoAluguel.StatusContrato;
import br.com.carrent.domain.PedidoAluguel;
import br.com.carrent.domain.Usuario;
import br.com.carrent.dto.PedidoDTO;
import br.com.carrent.repository.AutomovelRepository;
import br.com.carrent.repository.ContratoRepository;
import br.com.carrent.repository.UsuarioRepository;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AutomovelService {

    private final AutomovelRepository automovelRepository;
    private final ContratoRepository contratoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<PedidoDTO.AutomovelResposta> listar(Usuario autenticado, Boolean disponivel) {
        Set<Long> emUso = new HashSet<>(contratoRepository.findAutomovelIdsEmUso(StatusContrato.EXECUTADO, hoje()));
        boolean exibirProprietario = autenticado.isAgente();
        return automovelRepository.findAllByOrderByIdDesc().stream()
                .map(a -> PedidoDTO.AutomovelResposta.de(a, a.isDisponivel() && !emUso.contains(a.getId()),
                        exibirProprietario))
                .filter(a -> disponivel == null || a.disponivel() == disponivel)
                .toList();
    }

    @Transactional(readOnly = true)
    public PedidoDTO.AutomovelResposta buscar(Usuario autenticado, Long id) {
        Automovel automovel = automovelRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Automóvel não encontrado."));
        return PedidoDTO.AutomovelResposta.de(automovel, estaDisponivel(automovel), autenticado.isAgente());
    }

    @Transactional
    public PedidoDTO.AutomovelResposta cadastrar(Usuario autenticado, PedidoDTO.AutomovelCadastro dto) {
        if (!autenticado.isAgente()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas agentes podem cadastrar automóveis.");
        }
        String placa = dto.placa().replace("-", "").toUpperCase(Locale.ROOT);
        String matricula = dto.matricula().trim();
        if (automovelRepository.existsByPlacaIgnoreCase(placa)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um automóvel cadastrado com esta placa.");
        }
        if (automovelRepository.existsByMatriculaIgnoreCase(matricula)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Já existe um automóvel cadastrado com esta matrícula.");
        }
        Usuario proprietario = usuarioRepository.findById(autenticado.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        Automovel automovel = new Automovel();
        automovel.setMatricula(matricula);
        automovel.setAno(dto.ano());
        automovel.setMarca(dto.marca().trim());
        automovel.setModelo(dto.modelo().trim());
        automovel.setPlaca(placa);
        automovel.setDisponivel(dto.disponivel() == null || dto.disponivel());
        automovel.setProprietario(proprietario);

        automovel = automovelRepository.save(automovel);
        return PedidoDTO.AutomovelResposta.de(automovel, estaDisponivel(automovel), true);
    }

    @Transactional(readOnly = true)
    public boolean estaDisponivel(Automovel automovel) {
        if (!automovel.isDisponivel()) {
            return false;
        }
        LocalDate hoje = hoje();
        return contratoRepository.contarConflitos(automovel.getId(), StatusContrato.EXECUTADO, hoje, hoje) == 0;
    }

    private static LocalDate hoje() {
        return LocalDate.now(PedidoAluguel.FUSO_HORARIO);
    }
}
