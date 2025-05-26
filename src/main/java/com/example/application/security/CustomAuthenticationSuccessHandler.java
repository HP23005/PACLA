package com.example.application.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String redirectUrl = "/";

        for (GrantedAuthority auth : authentication.getAuthorities()) {
            String role = auth.getAuthority();
            if (role.equals("ROLE_ADMIN")) {
                redirectUrl = "/estudiantes";
                break;
            } else if (role.equals("ROLE_PROFESOR")) {
                redirectUrl = "/estudianteprofesor";
                break;
            } else if (role.equals("ROLE_ESTUDIANTE")) {
                redirectUrl = "/consulta-clases";
                break;
            }
        }

        response.sendRedirect(redirectUrl);
    }
}
