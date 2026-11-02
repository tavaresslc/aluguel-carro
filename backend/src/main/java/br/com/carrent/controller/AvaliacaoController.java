package br.com.carrent.controller;

import br.com.carrent.domain.Usuario;
import br.com.carrent.dto.AvaliacaoDTO;
import br.com.carrent.service.AvaliacaoService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pedidos/{pedidoId}/avaliacoes")
@RequiredArgsConstructor
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AvaliacaoDTO registrar(@AuthenticationPrincipal Usuario usuario, @PathVariable Long pedidoId,
            @Valid @RequestBody AvaliacaoDTO.Cadastro dto) {
        return avaliacaoService.registrar(usuario, pedidoId, dto);
    }

    @GetMapping
    public List<AvaliacaoDTO> listar(@AuthenticationPrincipal Usuario usuario, @PathVariable Long pedidoId) {
        return avaliacaoService.listar(usuario, pedidoId);
    }
}
