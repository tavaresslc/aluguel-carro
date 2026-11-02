package br.com.carrent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import br.com.carrent.domain.Agente;
import br.com.carrent.domain.Agente.TipoAgente;
import br.com.carrent.domain.Cliente;
import br.com.carrent.domain.Contratante;
import br.com.carrent.domain.Contratante.VinculoEmpregaticio;
import br.com.carrent.domain.Credito;
import br.com.carrent.domain.Usuario;
import br.com.carrent.repository.UsuarioRepository;
import br.com.carrent.security.PasswordEncoder;
import br.com.carrent.service.AuthService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "app.security.token-secret=segredo-de-teste-com-mais-de-32-caracteres")
@EnabledIfEnvironmentVariable(named = "DB_URL", matches = ".+")
class CarrentApplicationTests {

    @Test
    void contextLoads() {
    }
}

class RegrasDeNegocioTests {

    @Test
    void calculaParcelaPelaTabelaPrice() {
        Credito credito = new Credito();
        credito.setValor(new BigDecimal("1000.00"));
        credito.setTaxaJuros(new BigDecimal("1.0"));
        credito.setQuantidadeParcelas(12);

        assertThat(credito.calcularParcelas()).isEqualByComparingTo("88.85");
    }

    @Test
    void calculaParcelaSemJuros() {
        Credito credito = new Credito();
        credito.setValor(new BigDecimal("1200.00"));
        credito.setTaxaJuros(BigDecimal.ZERO);
        credito.setQuantidadeParcelas(12);

        assertThat(credito.calcularParcelas()).isEqualByComparingTo("100.00");
    }

    @Test
    void somaRendaTotalEExigeDadosCompletos() {
        Contratante contratante = new Contratante();
        assertThat(contratante.possuiDadosCompletos()).isFalse();

        contratante.atualizarDados("Maria", "123456", "52998224725", "Rua A, 1", "Engenheira");
        contratante.definirVinculos(List.of(
                new VinculoEmpregaticio("Empresa X", new BigDecimal("3000.00")),
                new VinculoEmpregaticio("Empresa Y", new BigDecimal("1500.50"))));

        assertThat(contratante.calcularRendaTotal()).isEqualByComparingTo("4500.50");
        assertThat(contratante.possuiDadosCompletos()).isTrue();
    }

    @Test
    void naoAceitaMaisDeTresVinculos() {
        Contratante contratante = new Contratante();
        List<VinculoEmpregaticio> quatro = List.of(
                new VinculoEmpregaticio("A", BigDecimal.ONE),
                new VinculoEmpregaticio("B", BigDecimal.ONE),
                new VinculoEmpregaticio("C", BigDecimal.ONE),
                new VinculoEmpregaticio("D", BigDecimal.ONE));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> contratante.definirVinculos(quatro))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void perfisDosUsuarios() {
        Agente banco = new Agente();
        banco.setTipo(TipoAgente.BANCO);
        Agente empresa = new Agente();
        empresa.setTipo(TipoAgente.EMPRESA);

        assertThat(new Cliente().perfil()).isEqualTo(Usuario.Perfil.CLIENTE);
        assertThat(banco.perfil()).isEqualTo(Usuario.Perfil.BANCO);
        assertThat(empresa.perfil()).isEqualTo(Usuario.Perfil.EMPRESA);
        assertThat(new Cliente().isAgente()).isFalse();
        assertThat(banco.isAgente()).isTrue();
    }
}

class TokenTests {

    private static final String SEGREDO = "segredo-de-teste-com-mais-de-32-caracteres";

    private final UsuarioRepository repositorio = mock(UsuarioRepository.class);
    private final AuthService authService = new AuthService(repositorio, new PasswordEncoder(), SEGREDO, 60);

    private Cliente cliente() {
        Cliente cliente = new Cliente();
        cliente.setId(7L);
        cliente.setEmail("cliente@teste.com");
        return cliente;
    }

    @Test
    void tokenValidoAutenticaOUsuario() {
        Cliente cliente = cliente();
        when(repositorio.findById(7L)).thenReturn(Optional.of(cliente));

        String token = authService.gerarToken(cliente);

        assertThat(authService.autenticarPorToken(token)).containsSame(cliente);
    }

    @Test
    void tokenAdulteradoEhRejeitado() {
        Cliente cliente = cliente();
        when(repositorio.findById(7L)).thenReturn(Optional.of(cliente));

        String token = authService.gerarToken(cliente);
        char primeiro = token.charAt(0) == 'A' ? 'B' : 'A';
        String adulterado = primeiro + token.substring(1);

        assertThat(authService.autenticarPorToken(adulterado)).isEmpty();
        assertThat(authService.autenticarPorToken("lixo")).isEmpty();
        assertThat(authService.autenticarPorToken("a.b")).isEmpty();
    }

    @Test
    void logoutInvalidaTokensAnteriores() {
        Cliente cliente = cliente();
        when(repositorio.findById(7L)).thenReturn(Optional.of(cliente));
        String token = authService.gerarToken(cliente);

        authService.logout(cliente);

        assertThat(cliente.getTokenVersion()).isEqualTo(1);
        assertThat(authService.autenticarPorToken(token)).isEmpty();
    }

    @Test
    void usuarioDesativadoNaoAutentica() {
        Cliente cliente = cliente();
        when(repositorio.findById(7L)).thenReturn(Optional.of(cliente));
        String token = authService.gerarToken(cliente);

        cliente.setAtivo(false);

        assertThat(authService.autenticarPorToken(token)).isEmpty();
    }

    @Test
    void segredoCurtoImpedeAInicializacao() {
        org.assertj.core.api.Assertions
                .assertThatThrownBy(() -> new AuthService(repositorio, new PasswordEncoder(), "curto", 60))
                .isInstanceOf(IllegalStateException.class);
    }
}
