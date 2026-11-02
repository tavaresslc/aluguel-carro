package br.com.carrent.controller;

import br.com.carrent.domain.PedidoAluguel.StatusPedido;
import br.com.carrent.domain.Usuario;
import br.com.carrent.dto.PedidoDTO;
import br.com.carrent.service.PedidoService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoDTO criar(@AuthenticationPrincipal Usuario usuario, @Valid @RequestBody PedidoDTO.Cadastro dto) {
        return pedidoService.criar(usuario, dto);
    }

    @GetMapping
    public List<PedidoDTO> listar(@AuthenticationPrincipal Usuario usuario,
            @RequestParam(required = false) StatusPedido status) {
        return pedidoService.listar(usuario, status);
    }

    @GetMapping("/{id}")
    public PedidoDTO buscar(@AuthenticationPrincipal Usuario usuario, @PathVariable Long id) {
        return pedidoService.buscar(usuario, id);
    }

    @PutMapping("/{id}")
    public PedidoDTO atualizar(@AuthenticationPrincipal Usuario usuario, @PathVariable Long id,
            @Valid @RequestBody PedidoDTO.Atualizacao dto) {
        return pedidoService.atualizar(usuario, id, dto);
    }

    @PatchMapping("/{id}/cancelar")
    public PedidoDTO cancelar(@AuthenticationPrincipal Usuario usuario, @PathVariable Long id) {
        return pedidoService.cancelar(usuario, id);
    }
}
