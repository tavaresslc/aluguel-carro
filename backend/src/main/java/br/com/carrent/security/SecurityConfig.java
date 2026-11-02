package br.com.carrent.security;

import br.com.carrent.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthService authService) throws Exception {
                http
                                .csrf(AbstractHttpConfigurer::disable)
                                .cors(Customizer.withDefaults())
                                .httpBasic(AbstractHttpConfigurer::disable)
                                .formLogin(AbstractHttpConfigurer::disable)
                                .logout(AbstractHttpConfigurer::disable)
                                .sessionManagement(
                                                sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .exceptionHandling(erros -> erros
                                                .authenticationEntryPoint((request, response, e) -> escreverErro(
                                                                request, response,
                                                                HttpStatus.UNAUTHORIZED,
                                                                "Autenticação necessária ou token inválido/expirado."))
                                                .accessDeniedHandler((request, response, e) -> escreverErro(request,
                                                                response,
                                                                HttpStatus.FORBIDDEN,
                                                                "Você não tem permissão para acessar este recurso.")))
                                .authorizeHttpRequests(regras -> regras
                                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                                .requestMatchers("/error").permitAll()
                                                .requestMatchers(HttpMethod.POST, "/api/auth/cadastro",
                                                                "/api/auth/login")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.PUT, "/api/usuarios/me/contratante")
                                                .hasRole("CLIENTE")
                                                .requestMatchers(HttpMethod.POST, "/api/pedidos").hasRole("CLIENTE")
                                                .requestMatchers(HttpMethod.PATCH, "/api/pedidos/*/cancelar")
                                                .hasRole("CLIENTE")
                                                .requestMatchers(HttpMethod.POST, "/api/automoveis").hasRole("AGENTE")
                                                .requestMatchers(HttpMethod.POST, "/api/pedidos/*/avaliacoes")
                                                .hasRole("AGENTE")
                                                .requestMatchers(HttpMethod.POST, "/api/pedidos/*/contrato")
                                                .hasRole("AGENTE")
                                                .requestMatchers(HttpMethod.POST, "/api/contratos/*/credito")
                                                .hasRole("BANCO")
                                                .anyRequest().authenticated())
                                .addFilterBefore(new AuthenticationFilter(authService),
                                                UsernamePasswordAuthenticationFilter.class);
                return http.build();
        }

        private static void escreverErro(HttpServletRequest request, HttpServletResponse response,
                        HttpStatus status, String mensagem) throws IOException {
                response.setStatus(status.value());
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"timestamp\":\"" + Instant.now() + "\","
                                + "\"status\":" + status.value() + ","
                                + "\"error\":\"" + escapar(status.getReasonPhrase()) + "\","
                                + "\"message\":\"" + escapar(mensagem) + "\","
                                + "\"path\":\"" + escapar(request.getRequestURI()) + "\"}");
        }

        private static String escapar(String texto) {
                return texto.replace("\\", "\\\\").replace("\"", "\\\"");
        }
}
