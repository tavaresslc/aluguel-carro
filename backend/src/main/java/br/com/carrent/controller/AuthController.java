package br.com.carrent.controller;

import br.com.carrent.domain.Usuario;
import br.com.carrent.dto.UsuarioDTO;
import br.com.carrent.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioDTO cadastrar(@Valid @RequestBody UsuarioDTO.Cadastro dto) {
        return authService.cadastrarCliente(dto);
    }

    @PostMapping("/login")
    public UsuarioDTO.LoginResposta login(@Valid @RequestBody UsuarioDTO.Login dto) {
        return authService.login(dto);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@AuthenticationPrincipal Usuario usuario) {
        authService.logout(usuario);
    }
}
