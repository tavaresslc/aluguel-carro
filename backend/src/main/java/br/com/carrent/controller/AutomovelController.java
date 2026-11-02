package br.com.carrent.controller;

import br.com.carrent.domain.Usuario;
import br.com.carrent.dto.PedidoDTO;
import br.com.carrent.service.AutomovelService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/automoveis")
@RequiredArgsConstructor
public class AutomovelController {

    private final AutomovelService automovelService;

    @GetMapping
    public List<PedidoDTO.AutomovelResposta> listar(@AuthenticationPrincipal Usuario usuario,
            @RequestParam(required = false) Boolean disponivel) {
        return automovelService.listar(usuario, disponivel);
    }

    @GetMapping("/{id}")
    public PedidoDTO.AutomovelResposta buscar(@AuthenticationPrincipal Usuario usuario, @PathVariable Long id) {
        return automovelService.buscar(usuario, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoDTO.AutomovelResposta cadastrar(@AuthenticationPrincipal Usuario usuario,
            @Valid @RequestBody PedidoDTO.AutomovelCadastro dto) {
        return automovelService.cadastrar(usuario, dto);
    }
}
