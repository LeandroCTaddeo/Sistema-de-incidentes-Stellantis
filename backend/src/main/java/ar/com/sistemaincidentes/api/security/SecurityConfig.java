package ar.com.sistemaincidentes.api.security;

import java.io.IOException;
import java.time.Instant;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@Profile("!corporate")
public class SecurityConfig {

    @Bean
    ApiKeyAuthenticationFilter apiKeyAuthenticationFilter(
            @Value("${api.security.token:}") String token,
            @Value("${api.security.user:desktop-local}") String usuario,
            @Value("${api.security.role:ADMIN}") String rol
    ) {
        return new ApiKeyAuthenticationFilter(token, usuario, rol);
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            ApiKeyAuthenticationFilter apiKeyFilter,
            ObjectMapper objectMapper
    ) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(autorizacion -> autorizacion
                        .requestMatchers("/api/health", "/actuator/health").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/incidentes")
                        .hasAnyRole("EMPLOYEE", "ADMIN")
                        .requestMatchers("/api/incidentes/**").hasRole("ADMIN")
                        .requestMatchers("/api/reportes", "/api/reportes/**").hasRole("ADMIN")
                        .requestMatchers("/api/firmantes", "/api/firmantes/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/actual")
                        .hasAnyRole("EMPLOYEE", "ADMIN")
                        .requestMatchers("/api/usuarios/**").hasRole("ADMIN")
                        .anyRequest().denyAll())
                .exceptionHandling(excepciones -> excepciones
                        .authenticationEntryPoint((request, response, exception) ->
                                escribirError(
                                        objectMapper,
                                        request,
                                        response,
                                        HttpServletResponse.SC_UNAUTHORIZED,
                                        "Unauthorized",
                                        "Se requiere una credencial válida para acceder a este recurso."
                                ))
                        .accessDeniedHandler((request, response, exception) ->
                                escribirError(
                                        objectMapper,
                                        request,
                                        response,
                                        HttpServletResponse.SC_FORBIDDEN,
                                        "Forbidden",
                                        "La credencial no tiene permisos para acceder a este recurso."
                                )))
                .addFilterBefore(apiKeyFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    static void escribirError(
            ObjectMapper objectMapper,
            HttpServletRequest request,
            HttpServletResponse response,
            int estado,
            String error,
            String mensaje
    ) throws IOException {
        response.setStatus(estado);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(
                response.getOutputStream(),
                new SecurityErrorResponse(
                        Instant.now(),
                        estado,
                        error,
                        mensaje,
                        request.getRequestURI()
                )
        );
    }

    private record SecurityErrorResponse(
            Instant timestamp,
            int status,
            String error,
            String message,
            String path
    ) {
    }
}
