package ar.com.sistemaincidentes.api.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

final class CorporateJwtAuthenticationConverter
        implements Converter<Jwt, AbstractAuthenticationToken> {

    private final String usernameClaim;
    private final String rolesClaim;
    private final String adminRole;
    private final String employeeRole;

    CorporateJwtAuthenticationConverter(
            String usernameClaim,
            String rolesClaim,
            String adminRole,
            String employeeRole
    ) {
        this.usernameClaim = texto(usernameClaim, "preferred_username");
        this.rolesClaim = texto(rolesClaim, "roles");
        this.adminRole = texto(adminRole, "INCIDENTES_ADMIN");
        this.employeeRole = texto(employeeRole, "INCIDENTES_EMPLOYEE");
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String usuario = jwt.getClaimAsString(usernameClaim);
        if (usuario == null || usuario.isBlank()) {
            usuario = jwt.getSubject();
        }

        List<GrantedAuthority> autoridades = new ArrayList<>();
        for (String rol : obtenerRoles(jwt.getClaim(rolesClaim))) {
            if (adminRole.equalsIgnoreCase(rol)) {
                autoridades.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            } else if (employeeRole.equalsIgnoreCase(rol)) {
                autoridades.add(new SimpleGrantedAuthority("ROLE_EMPLOYEE"));
            }
        }
        return new JwtAuthenticationToken(jwt, autoridades, usuario);
    }

    private Collection<String> obtenerRoles(Object valor) {
        if (valor instanceof Collection<?> coleccion) {
            return coleccion.stream().map(String::valueOf).toList();
        }
        if (valor instanceof String texto && !texto.isBlank()) {
            return List.of(texto.split("[ ,]"));
        }
        return List.of();
    }

    private static String texto(String valor, String alternativa) {
        return valor == null || valor.isBlank() ? alternativa : valor.trim();
    }
}
