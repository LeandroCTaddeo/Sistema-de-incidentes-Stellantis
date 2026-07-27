package ar.com.sistemaincidentes.api.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

class CorporateJwtAuthenticationConverterTest {

    private final CorporateJwtAuthenticationConverter converter =
            new CorporateJwtAuthenticationConverter(
                    "preferred_username",
                    "roles",
                    "INCIDENTES_ADMIN",
                    "INCIDENTES_EMPLOYEE"
            );

    @Test
    void obtieneUsuarioYPermisoAdministradorDesdeClaimsFirmados() {
        Jwt jwt = jwt("leand", List.of("INCIDENTES_ADMIN"));

        var authentication = converter.convert(jwt);

        assertThat(authentication.getName()).isEqualTo("leand");
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void noOtorgaPermisosAnteUnRolDesconocido() {
        Jwt jwt = jwt("leand", List.of("OTRO_SISTEMA_ADMIN"));

        var authentication = converter.convert(jwt);

        assertThat(authentication.getAuthorities()).isEmpty();
    }

    private Jwt jwt(String usuario, List<String> roles) {
        return new Jwt(
                "token-prueba",
                Instant.now(),
                Instant.now().plusSeconds(300),
                java.util.Map.of("alg", "RS256"),
                java.util.Map.of(
                        "sub", "usuario-id",
                        "preferred_username", usuario,
                        "roles", roles
                )
        );
    }
}
