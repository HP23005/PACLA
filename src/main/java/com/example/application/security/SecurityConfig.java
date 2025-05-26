package com.example.application.security;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.example.application.security.CustomAuthenticationSuccessHandler;

@Configuration
public class SecurityConfig {
    
@Value("${AUTH0_DOMAIN}")
private String auth0Domain;

@Value("${AUTH0_CLIENT_ID}")
private String auth0ClientId;

@Value("${AUTH0_LOGOUT_RETURN}")
private String auth0LogoutReturn;

@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        // Permitir acceso libre a recursos estáticos, Vaadin y consola H2
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/favicon.ico",
                "/images/**",
                "/icons/**",
                "/frontend/**",
                "/VAADIN/**",
                "/webjars/**",
                "/manifest.webmanifest",
                "/sw.js",
                "/offline.html",
                "/styles/**",
                "/h2-console/**"
            ).permitAll()

            // Rutas protegidas por roles
            .requestMatchers("/estudiantes", "/clases", "/participacion").hasRole("ADMIN")
            .requestMatchers("/estudianteprofesor", "/clasesprofesor", "/participacionprofesor").hasRole("PROFESOR")
            .requestMatchers("/consulta-clases", "/participaciones-consulta").hasRole("ESTUDIANTE")

            // Proteger cualquier otra ruta autenticada
            .anyRequest().authenticated()
        )
        // OAuth2 Login con OIDC y mapeo personalizado de roles
        .oauth2Login(oauth2 -> oauth2
            .userInfoEndpoint(userInfo -> userInfo
                .oidcUserService(oidcUserService())
            )
            .successHandler(successHandler)
        )
        // Logout personalizado
        .logout(logout -> logout
            .logoutSuccessHandler(oidcLogoutSuccessHandler())
            .invalidateHttpSession(true)
            .clearAuthentication(true)
            .deleteCookies("JSESSIONID")
            .permitAll()
        )
        // CSRF ignorar en recursos estáticos y H2 Console
        .csrf(csrf -> csrf
            .ignoringRequestMatchers(
                new AntPathRequestMatcher("/VAADIN/**"),
                new AntPathRequestMatcher("/frontend/**"),
                new AntPathRequestMatcher("/favicon.ico"),
                new AntPathRequestMatcher("/robots.txt"),
                new AntPathRequestMatcher("/manifest.webmanifest"),
                new AntPathRequestMatcher("/sw.js"),
                new AntPathRequestMatcher("/offline.html"),
                new AntPathRequestMatcher("/icons/**"),
                new AntPathRequestMatcher("/styles/**"),
                new AntPathRequestMatcher("/images/**"),
                new AntPathRequestMatcher("/h2-console/**"),
                // Añade esta para ignorar CSRF en llamadas uidl de Vaadin
                request -> request.getRequestURI().equals("/") && request.getParameter("v-r") != null
            )
        )

        // Solo si tienes API con JWT (opcional)
        //.oauth2ResourceServer(oauth2 -> oauth2
        //    .jwt(jwt -> jwt
        //        .jwtAuthenticationConverter(jwtAuthenticationConverter())
        //    )
        //)
        ;

    return http.build();
}



    /**
     * Mapea los roles personalizados del token OIDC al formato Spring Security (ROLE_nombre).
     */
    private static final String ROLE_CLAIM = "https://pacla-daw.com/roles";

    private OidcUserService oidcUserService() {
        OidcUserService delegate = new OidcUserService();

        return new OidcUserService() {
            @Override
            public OidcUser loadUser(OidcUserRequest userRequest) {
                OidcUser oidcUser = delegate.loadUser(userRequest);
                List<GrantedAuthority> authorities = new ArrayList<>(oidcUser.getAuthorities());

                List<String> roles = oidcUser.getClaimAsStringList(ROLE_CLAIM);
                if (roles != null) {
                    for (String role : roles) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
                    }
                }

                return new DefaultOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
            }
        };
    }



private LogoutSuccessHandler oidcLogoutSuccessHandler() {
    return (HttpServletRequest request, HttpServletResponse response, Authentication authentication) -> {
        String logoutUrl = auth0Domain + "/v2/logout" +
                "?client_id=" + URLEncoder.encode(auth0ClientId, StandardCharsets.UTF_8) +
                "&returnTo=" + URLEncoder.encode(auth0LogoutReturn, StandardCharsets.UTF_8);
        response.sendRedirect(logoutUrl);
    };
}

    public boolean hasRole(String role) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) return false;

        String roleToCheck = "ROLE_" + role.toUpperCase();
        return auth.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(r -> r.equalsIgnoreCase(roleToCheck));
    }


@Bean
public JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    // Indica el nombre del claim donde están tus roles personalizados
    grantedAuthoritiesConverter.setAuthoritiesClaimName("https://pacla-daw.com/roles");
    // Quitar prefijo ROLE_ si no quieres que se agregue
    grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

    JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
    jwtConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
    return jwtConverter;
}

private final CustomAuthenticationSuccessHandler successHandler;

public SecurityConfig(CustomAuthenticationSuccessHandler successHandler) {
    this.successHandler = successHandler;
}


}
