package br.com.carrent.controller;

import br.com.carrent.domain.Usuario;
import br.com.carrent.dto.UsuarioDTO;
import br.com.carrent.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios/me")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public UsuarioDTO perfil(@AuthenticationPrincipal Usuario usuario) {
        return usuarioService.buscarPerfil(usuario);
    }

    @PutMapping("/contratante")
    public UsuarioDTO atualizarContratante(@AuthenticationPrincipal Usuario usuario,
            @Valid @RequestBody UsuarioDTO.ContratanteAtualizacao dto) {
        return usuarioService.atualizarContratante(usuario, dto);
    }
}
