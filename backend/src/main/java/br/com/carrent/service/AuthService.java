package br.com.carrent.service;

import br.com.carrent.domain.Cliente;
import br.com.carrent.domain.Contratante;
import br.com.carrent.domain.Usuario;
import br.com.carrent.dto.UsuarioDTO;
import br.com.carrent.repository.UsuarioRepository;
import br.com.carrent.security.PasswordEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService implements UserDetailsService {

    private static final String ALGORITMO = "HmacSHA256";
    private static final Base64.Encoder CODIFICADOR = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODIFICADOR = Base64.getUrlDecoder();
    private static final int TAMANHO_MINIMO_SEGREDO = 32;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final byte[] segredo;
    private final long expiracaoSegundos;

    private final String hashFalso;

    public AuthService(UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.security.token-secret}") String segredo,
            @Value("${app.security.token-expiration-minutes:480}") long expiracaoMinutos) {
        if (segredo == null || segredo.length() < TAMANHO_MINIMO_SEGREDO) {
            throw new IllegalStateException("A propriedade app.security.token-secret (variável TOKEN_SECRET) "
                    + "deve ter pelo menos " + TAMANHO_MINIMO_SEGREDO + " caracteres.");
        }
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.segredo = segredo.getBytes(StandardCharsets.UTF_8);
        this.expiracaoSegundos = expiracaoMinutos * 60;
        this.hashFalso = passwordEncoder.encode("senha-inexistente");
    }

    @Transactional
    public UsuarioDTO cadastrarCliente(UsuarioDTO.Cadastro dto) {
        String email = normalizarEmail(dto.email());
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um usuário cadastrado com este e-mail.");
        }
        if (senhaExcedeLimiteBcrypt(dto.senha())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A senha deve ter no máximo 72 bytes.");
        }
        String nome = dto.nome().trim();

        Contratante contratante = new Contratante();
        contratante.setNome(nome);

        Cliente cliente = new Cliente();
        cliente.setNome(nome);
        cliente.setEmail(email);
        cliente.setSenhaHash(passwordEncoder.encode(dto.senha()));
        cliente.setContratante(contratante);

        return UsuarioDTO.de(usuarioRepository.save(cliente));
    }

    @Transactional(readOnly = true)
    public UsuarioDTO.LoginResposta login(UsuarioDTO.Login dto) {
        if (senhaExcedeLimiteBcrypt(dto.senha())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos.");
        }
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(normalizarEmail(dto.email())).orElse(null);
        String hash = usuario != null ? usuario.getSenhaHash() : hashFalso;
        boolean senhaConfere = passwordEncoder.matches(dto.senha(), hash);
        if (usuario == null || !senhaConfere || !usuario.isAtivo()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos.");
        }
        return new UsuarioDTO.LoginResposta(gerarToken(usuario), "Bearer", expiracaoSegundos, UsuarioDTO.de(usuario));
    }

    @Transactional
    public void logout(Usuario autenticado) {
        usuarioRepository.findById(autenticado.getId()).ifPresent(Usuario::invalidarTokens);
    }

    public Optional<Usuario> autenticarPorToken(String token) {
        try {
            int ponto = token.indexOf('.');
            if (ponto <= 0 || ponto == token.length() - 1) {
                return Optional.empty();
            }
            String conteudo = token.substring(0, ponto);
            byte[] assinatura = DECODIFICADOR.decode(token.substring(ponto + 1));
            if (!MessageDigest.isEqual(assinatura, assinar(conteudo))) {
                return Optional.empty();
            }

            String[] partes = new String(DECODIFICADOR.decode(conteudo), StandardCharsets.UTF_8).split(":");
            if (partes.length != 3) {
                return Optional.empty();
            }
            long id = Long.parseLong(partes[0]);
            int versao = Integer.parseInt(partes[1]);
            long expiraEm = Long.parseLong(partes[2]);
            if (Instant.now().getEpochSecond() >= expiraEm) {
                return Optional.empty();
            }
            return usuarioRepository.findById(id)
                    .filter(Usuario::isAtivo)
                    .filter(usuario -> usuario.getTokenVersion() == versao);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public String gerarToken(Usuario usuario) {
        long expiraEm = Instant.now().plusSeconds(expiracaoSegundos).getEpochSecond();
        String payload = usuario.getId() + ":" + usuario.getTokenVersion() + ":" + expiraEm;
        String conteudo = CODIFICADOR.encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        return conteudo + "." + CODIFICADOR.encodeToString(assinar(conteudo));
    }

    private byte[] assinar(String conteudo) {
        try {
            Mac mac = Mac.getInstance(ALGORITMO);
            mac.init(new SecretKeySpec(segredo, ALGORITMO));
            return mac.doFinal(conteudo.getBytes(StandardCharsets.US_ASCII));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Não foi possível assinar o token.", e);
        }
    }

    public static List<GrantedAuthority> autoridades(Usuario usuario) {
        List<GrantedAuthority> autoridades = new ArrayList<>();
        if (usuario.isAgente()) {
            autoridades.add(new SimpleGrantedAuthority("ROLE_AGENTE"));
        }
        autoridades.add(new SimpleGrantedAuthority("ROLE_" + usuario.perfil().name()));
        return autoridades;
    }

    private static boolean senhaExcedeLimiteBcrypt(String senha) {
        return senha.getBytes(StandardCharsets.UTF_8).length > 72;
    }

    public static String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(normalizarEmail(email))
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado."));
        return User.withUsername(usuario.getEmail())
                .password(usuario.getSenhaHash())
                .authorities(autoridades(usuario))
                .disabled(!usuario.isAtivo())
                .build();
    }
}
