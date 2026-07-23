package ar.com.sistemaincidentes.api.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    public static final String HEADER_API_KEY = "X-API-Key";

    private final byte[] tokenEsperado;
    private final String usuario;
    private final String rol;

    public ApiKeyAuthenticationFilter(String tokenEsperado, String usuario, String rol) {
        this.tokenEsperado = bytes(tokenEsperado);
        this.usuario = valorOAlternativa(usuario, "desktop-local");
        this.rol = normalizarRol(rol);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String tokenRecibido = request.getHeader(HEADER_API_KEY);

        if (SecurityContextHolder.getContext().getAuthentication() == null
                && tokenEsperado.length > 0
                && tokenRecibido != null
                && MessageDigest.isEqual(tokenEsperado, bytes(tokenRecibido))) {
            var autenticacion = UsernamePasswordAuthenticationToken.authenticated(
                    usuario,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + rol))
            );
            SecurityContextHolder.getContext().setAuthentication(autenticacion);
        }

        filterChain.doFilter(request, response);
    }

    private static byte[] bytes(String valor) {
        return valor == null ? new byte[0] : valor.getBytes(StandardCharsets.UTF_8);
    }

    private static String valorOAlternativa(String valor, String alternativa) {
        return valor == null || valor.isBlank() ? alternativa : valor.trim();
    }

    static String normalizarRol(String valor) {
        String rolNormalizado = valorOAlternativa(valor, "ADMIN").toUpperCase();
        return rolNormalizado.startsWith("ROLE_")
                ? rolNormalizado.substring("ROLE_".length())
                : rolNormalizado;
    }
}
