package com.example.application.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class LogoutController {

    @Value("${AUTH0_DOMAIN}")
    private String auth0Domain;

    @Value("${AUTH0_CLIENT_ID}")
    private String clientId;

    @Value("${AUTH0_LOGOUT_RETURN}")
    private String logoutReturn;

    @GetMapping("/logout-success")
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication auth) throws Exception {
        request.logout(); // Cierra sesión local
        String redirectUrl = String.format("%s/v2/logout?client_id=%s&returnTo=%s",
                auth0Domain, clientId, logoutReturn);
        response.sendRedirect(redirectUrl); // Redirige al logout de Auth0
    }
}
