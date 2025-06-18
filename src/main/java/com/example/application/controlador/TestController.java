package com.example.application.controlador;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/test")
    public String test() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return "No hay sesión iniciada";
        }

        StringBuilder roles = new StringBuilder("Roles del usuario: ");
        for (GrantedAuthority authority : auth.getAuthorities()) {
            roles.append(authority.getAuthority()).append(" ");
        }
        return roles.toString();
    }
}
