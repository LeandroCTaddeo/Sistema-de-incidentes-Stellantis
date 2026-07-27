package ar.com.sistemaincidentes.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.core.convert.converter.Converter;

@Configuration
@Profile("corporate")
public class CorporateSecurityConfig {

    @Bean
    Converter<Jwt, AbstractAuthenticationToken> corporateJwtAuthenticationConverter(
            @Value("${api.security.oidc.username-claim:preferred_username}") String usernameClaim,
            @Value("${api.security.oidc.roles-claim:roles}") String rolesClaim,
            @Value("${api.security.oidc.admin-role:INCIDENTES_ADMIN}") String adminRole,
            @Value("${api.security.oidc.employee-role:INCIDENTES_EMPLOYEE}") String employeeRole
    ) {
        return new CorporateJwtAuthenticationConverter(
                usernameClaim, rolesClaim, adminRole, employeeRole
        );
    }

    @Bean
    SecurityFilterChain corporateSecurityFilterChain(
            HttpSecurity http,
            ObjectMapper objectMapper,
            Converter<Jwt, AbstractAuthenticationToken> jwtConverter
    ) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorization -> authorization
                        .requestMatchers("/api/health", "/actuator/health").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/incidentes")
                        .hasAnyRole("EMPLOYEE", "ADMIN")
                        .requestMatchers("/api/incidentes/**").hasRole("ADMIN")
                        .requestMatchers("/api/reportes", "/api/reportes/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/actual")
                        .hasAnyRole("EMPLOYEE", "ADMIN")
                        .requestMatchers("/api/usuarios/**").hasRole("ADMIN")
                        .anyRequest().denyAll())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt ->
                        jwt.jwtAuthenticationConverter(jwtConverter)))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) ->
                                SecurityConfig.escribirError(
                                        objectMapper, request, response, 401,
                                        "Unauthorized",
                                        "Se requiere una credencial corporativa válida."
                                ))
                        .accessDeniedHandler((request, response, exception) ->
                                SecurityConfig.escribirError(
                                        objectMapper, request, response, 403,
                                        "Forbidden",
                                        "La identidad corporativa no tiene permisos para este recurso."
                                )))
                .build();
    }
}
