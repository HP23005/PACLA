package com.example.application.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                    Authentication authentication) throws IOException, ServletException {
    var authorities = authentication.getAuthorities();

    String targetUrl = "/";
    if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
        targetUrl = "/estudiantes";
    } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_PROFESOR"))) {
        targetUrl = "/participacionprofesor";
    } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ESTUDIANTE"))) {
        targetUrl = "/consulta-clases";
    }

    response.sendRedirect(targetUrl);
}

}
