package com.example.application.views;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.vaadin.lineawesome.LineAwesomeIcon;

import com.example.application.views.Clase.ClasesView;
import com.example.application.views.Estudiante.EstudiantesView;
import com.example.application.views.Participacion.ParticipacionesView;
import com.example.application.views.Clase.ClasesViewProfesor;
import com.example.application.views.Estudiante.EstudiantesViewProfesor;
import com.example.application.views.Participacion.ParticipacionesViewProfesor;
import com.example.application.views.Clase.ClasesReadOnlyView;
import com.example.application.views.Participacion.ParticipacionesReadOnlyView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.theme.lumo.LumoUtility;

import com.example.application.security.*;

/**
 * La clase MainLayout define la vista principal del sistema EduCantrol.
 * Contiene el menú de navegación lateral, el encabezado y la lógica 
 * para mostrar la vista de estudiantes como predeterminada.
 */
public class MainLayout extends AppLayout {

    // Título de la vista que se mostrará en el encabezado
    private H1 viewTitle;

    /**
     * Constructor que configura el layout, el menú lateral y el encabezado.
     * Además, redirige automáticamente a la vista de estudiantes si no hay vista activa.
     */
    public MainLayout() {
        setPrimarySection(Section.DRAWER); // Configura el panel lateral como sección primaria
        addDrawerContent(); // Añade el contenido del menú lateral
        addHeaderContent(); // Añade el contenido del encabezado
    
        // Si no hay vista activa, redirige a la vista de estudiantes
        if (UI.getCurrent().getInternals().getActiveViewLocation() == null) {
            UI.getCurrent().navigate("estudiantes");
        }
    }
    
private Footer createAuthFooter() {
    Footer footer = new Footer();

    var authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication != null &&
        authentication.isAuthenticated() &&
        !(authentication instanceof AnonymousAuthenticationToken)) {

        String username;
        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            OAuth2User user = oauthToken.getPrincipal();
            username = (String) user.getAttributes().get("email");
        } else {
            username = authentication.getName();
        }

        // Obtener rol principal
        String mainRole = getMainRole();

        // Mostrar correo y rol
        Span userEmail = new Span("Correo: " + username);
        Span userRole = new Span("Rol: " + mainRole);

        userEmail.getStyle().set("display", "block").set("margin-bottom", "0.2rem");
        userRole.getStyle().set("display", "block").set("margin-bottom", "0.5rem");

        // Botón de logout
        Button logout = new Button("Cerrar sesión", e -> {
            Notification.show("Cerrando sesión...", 3000, Notification.Position.MIDDLE);
            UI.getCurrent().getPage().setLocation("/logout");
        });

        footer.add(userEmail, userRole, logout);
    } else {
        Button login = new Button("Login", e -> {
            UI.getCurrent().getPage().setLocation("/oauth2/authorization/okta");
        });
        footer.add(login);
    }

    return footer;
}

private String getMainRole() {
    if (hasAuthority("ROLE_ADMIN")) {
        return "ADMIN";
    } else if (hasAuthority("ROLE_PROFESOR")) {
        return "PROFESOR";
    } else if (hasAuthority("ROLE_ESTUDIANTE")) {
        return "ESTUDIANTE";
    } else {
        return "Desconocido";
    }
}



    /**
     * Método para añadir el contenido del encabezado, que incluye un botón para
     * abrir el menú lateral y el título de la vista.
     */
    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle(); // Botón para abrir/cerrar el menú lateral
        toggle.setAriaLabel("Menú de navegación"); // Etiqueta accesible para el botón

        viewTitle = new H1(); // Título de la vista que se actualizará según la página activa
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE); // Añade estilos al título

        // Añade el botón de menú y el título al encabezado
        addToNavbar(true, toggle, viewTitle);
    }

    /**
     * Método para añadir el contenido del menú lateral, que contiene los enlaces
     * a las vistas de estudiantes, profesores, materias, periodos y horarios.
     */
    private void addDrawerContent() {
        Span appName = new Span("PACLA"); // Nombre de la aplicación
        appName.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.FontSize.LARGE); // Estilo de texto para el nombre

        Header header = new Header(appName); // Encabezado que contiene el nombre de la aplicación

        // Crear un área desplazable que contenga los elementos de navegación
        Scroller scroller = new Scroller(createNavigation());

        // Añadir el encabezado, la navegación y el pie de página al menú lateral
        addToDrawer(header, scroller, createAuthFooter());
    }

    /**
     * Método para crear el menú lateral con los enlaces a las vistas principales.
     * Cada ítem del menú está asociado a una vista y un ícono.
     */
private SideNav createNavigation() {
    SideNav nav = new SideNav();

var authentication = SecurityContextHolder.getContext().getAuthentication();
if (authentication != null && authentication.isAuthenticated()) {
    System.out.println("Roles asignados:");
    authentication.getAuthorities().forEach(a -> System.out.println(" - " + a.getAuthority()));
} else {
    System.out.println("No hay usuario autenticado.");
}

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

private boolean hasAuthority(String role) {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || auth.getAuthorities() == null) return false;

    // Aquí suponemos que role es ROLE_ADMIN, ROLE_PROFESOR, etc.
    return auth.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .anyMatch(r -> r.equalsIgnoreCase(role));
}


    /**
     * Método que se ejecuta después de cada navegación, actualizando el título de la vista
     * en el encabezado según la vista activa.
     */
    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle()); // Actualiza el título del encabezado
    }

    /**
     * Método para obtener el título de la página actual.
     * El título se extrae de la anotación @PageTitle de la vista activa.
     */
    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class); // Obtiene la anotación de título
        return title == null ? "" : title.value(); // Retorna el título o una cadena vacía si no existe
    }
}
