package com.example.application.views;

import org.vaadin.lineawesome.LineAwesomeIcon;

import com.example.application.views.Clase.ClasesView;
import com.example.application.views.Estudiante.EstudiantesView;
import com.example.application.views.Participacion.ParticipacionesView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.theme.lumo.LumoUtility;

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
            UI.getCurrent().navigate("Estudiante");
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
        addToDrawer(header, scroller, createFooter());
    }

    /**
     * Método para crear el menú lateral con los enlaces a las vistas principales.
     * Cada ítem del menú está asociado a una vista y un ícono.
     */
    private SideNav createNavigation() {
        SideNav nav = new SideNav(); // Crea una barra de navegación lateral

        // Añade los elementos de navegación con los íconos correspondientes
        nav.addItem(new SideNavItem("Estudiantes", EstudiantesView.class, LineAwesomeIcon.USER.create()));
        nav.addItem(new SideNavItem("Clases", ClasesView.class, LineAwesomeIcon.SCHOOL_SOLID.create()));
        nav.addItem(new SideNavItem("Participaciones", ParticipacionesView.class, LineAwesomeIcon.HAND_POINTER.create()));

        return nav; // Retorna la barra de navegación lateral configurada
    }

    /**
     * Método para crear el pie de página del layout.
     * Actualmente no contiene contenido adicional.
     */
    private Footer createFooter() {
        Footer layout = new Footer(); // Crea un pie de página vacío
        return layout; // Retorna el pie de página
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
