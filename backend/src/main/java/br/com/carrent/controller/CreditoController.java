package br.com.carrent.controller;

import br.com.carrent.domain.Usuario;
import br.com.carrent.dto.ContratoDTO;
import br.com.carrent.service.CreditoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contratos/{contratoId}/credito")
@RequiredArgsConstructor
public class CreditoController {

    private final CreditoService creditoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContratoDTO.CreditoResposta conceder(@AuthenticationPrincipal Usuario usuario,
            @PathVariable Long contratoId,
            @Valid @RequestBody ContratoDTO.CreditoCadastro dto) {
        return creditoService.conceder(usuario, contratoId, dto);
    }
}
