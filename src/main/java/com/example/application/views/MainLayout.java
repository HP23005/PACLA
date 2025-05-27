package com.example.application.views;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.vaadin.lineawesome.LineAwesomeIcon;

import com.example.application.views.Clase.ClasesReadOnlyView;
import com.example.application.views.Clase.ClasesView;
import com.example.application.views.Clase.ClasesViewProfesor;
import com.example.application.views.Estudiante.EstudiantesView;
import com.example.application.views.Estudiante.EstudiantesViewProfesor;
import com.example.application.views.Participacion.ParticipacionesReadOnlyView;
import com.example.application.views.Participacion.ParticipacionesView;
import com.example.application.views.Participacion.ParticipacionesViewProfesor;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.theme.lumo.LumoUtility;

@PageTitle("PACLA")
public class MainLayout extends AppLayout {

    private H1 viewTitle;

    public MainLayout() {
        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        addHeaderContent();
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menú de navegación");

        viewTitle = new H1("PACLA");
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        addToNavbar(true, toggle, viewTitle);
    }

    private void addDrawerContent() {
        Span appName = new Span("PACLA");
        appName.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.FontSize.LARGE);
        Header header = new Header(appName);
        Scroller scroller = new Scroller(createNavigation());
        addToDrawer(header, scroller, createFooter());
    }

    private SideNav createNavigation() {
        SideNav nav = new SideNav();

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            if (hasAuthority("ROLE_ADMIN")) {
                nav.addItem(new SideNavItem("Estudiantes", EstudiantesView.class, LineAwesomeIcon.USER.create()));
                nav.addItem(new SideNavItem("Clases", ClasesView.class, LineAwesomeIcon.SCHOOL_SOLID.create()));
                nav.addItem(new SideNavItem("Participaciones", ParticipacionesView.class, LineAwesomeIcon.HAND_POINTER.create()));
            } else if (hasAuthority("ROLE_PROFESOR")) {
                nav.addItem(new SideNavItem("Estudiantes", EstudiantesViewProfesor.class, LineAwesomeIcon.USER.create()));
                nav.addItem(new SideNavItem("Clases", ClasesViewProfesor.class, LineAwesomeIcon.SCHOOL_SOLID.create()));
                nav.addItem(new SideNavItem("Participaciones", ParticipacionesViewProfesor.class, LineAwesomeIcon.HAND_POINTER.create()));
            } else if (hasAuthority("ROLE_ESTUDIANTE")) {
                nav.addItem(new SideNavItem("Clases", ClasesReadOnlyView.class, LineAwesomeIcon.SCHOOL_SOLID.create()));
                nav.addItem(new SideNavItem("Participaciones", ParticipacionesReadOnlyView.class, LineAwesomeIcon.HAND_POINTER.create()));
            }
        }

        return nav;
    }

    private Footer createFooter() {
        Footer footer = new Footer();
        footer.getStyle().set("display", "flex")
                .set("flexDirection", "column")
                .set("alignItems", "flex-start")
                .set("padding", "1rem");

        var auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            String email = (auth instanceof OAuth2AuthenticationToken token)
                    ? (String) token.getPrincipal().getAttributes().get("email")
                    : auth.getName();

            String rol = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(r -> r.startsWith("ROLE_"))
                    .findFirst()
                    .map(r -> r.replace("ROLE_", ""))
                    .orElse("Sin rol");

            Span rolSpan = new Span("Rol: " + rol);
            Span emailSpan = new Span("Usuario: " + email);
            rolSpan.getStyle().set("font-weight", "bold").set("font-size", "16px");
            emailSpan.getStyle().set("font-weight", "bold").set("font-size", "16px");

            Button logout = new Button("Cerrar sesión");
            logout.getStyle().set("margin-top", "0.5rem");

            Dialog confirmDialog = new Dialog();
            confirmDialog.setHeaderTitle("¿Confirmar cierre de sesión?");
            confirmDialog.add("¿Estás seguro de que deseas cerrar sesión?");

            Button confirm = new Button("Sí, cerrar sesión", e -> {
                confirmDialog.close();
                UI.getCurrent().getPage().setLocation("/logout-success");
            });
            confirm.getStyle().set("background-color", "red").set("color", "white");

            Button cancel = new Button("Cancelar", e -> confirmDialog.close());
            confirmDialog.getFooter().add(cancel, confirm);

            logout.addClickListener(e -> confirmDialog.open());

            footer.add(rolSpan, emailSpan, logout, confirmDialog);
        }

        return footer;
    }

    private boolean hasAuthority(String role) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(r -> r.equalsIgnoreCase(role));
    }
}
