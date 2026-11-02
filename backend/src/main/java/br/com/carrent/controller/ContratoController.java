package br.com.carrent.controller;

import br.com.carrent.domain.Usuario;
import br.com.carrent.dto.ContratoDTO;
import br.com.carrent.service.ContratoService;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ContratoController {

    private final ContratoService contratoService;

    @PostMapping("/pedidos/{pedidoId}/contrato")
    @ResponseStatus(HttpStatus.CREATED)
    public ContratoDTO executar(@AuthenticationPrincipal Usuario usuario, @PathVariable Long pedidoId,
            @Valid @RequestBody ContratoDTO.Cadastro dto) {
        return contratoService.executar(usuario, pedidoId, dto);
    }

    @GetMapping("/contratos")
    public List<ContratoDTO> listar(@AuthenticationPrincipal Usuario usuario) {
        return contratoService.listar(usuario);
    }

    @GetMapping("/contratos/{id}")
    public ContratoDTO buscar(@AuthenticationPrincipal Usuario usuario, @PathVariable Long id) {
        return contratoService.buscar(usuario, id);
    }
}
