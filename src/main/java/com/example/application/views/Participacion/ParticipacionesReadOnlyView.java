package com.example.application.views.Participacion;

import java.util.List;
import java.util.stream.Collectors;

import com.example.application.controlador.ParticipacionController;
import com.example.application.modelo.Estudiantes;
import com.example.application.modelo.Participaciones;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Participaciones")
@Route(value = "participaciones-consulta", layout = MainLayout.class)
public class ParticipacionesReadOnlyView extends Composite<VerticalLayout> {

    private final ParticipacionController participacionesController;
    private final Grid<Participaciones> grid = new Grid<>(Participaciones.class, false);
    
    // Componentes de búsqueda
    private final TextField searchCodigoParticipacionField = new TextField("Buscar por Código de Participación");
    private final Button searchButton = new Button("Buscar");
    private final Button clearSearchButton = new Button("Limpiar");

    public ParticipacionesReadOnlyView(ParticipacionController participacionesController) {
        this.participacionesController = participacionesController;

        VerticalLayout layout = new VerticalLayout();
        H3 title = new H3("Listado de Participaciones");
        
        // Configurar el campo de búsqueda
        searchCodigoParticipacionField.setPlaceholder("Ingrese código...");
        
        // Configurar botones
        searchButton.addClickListener(e -> searchParticipacion(searchCodigoParticipacionField.getValue()));
        clearSearchButton.addClickListener(e -> {
            searchCodigoParticipacionField.clear();
            refreshGrid();
        });
        
        // Layout para los controles de búsqueda
        HorizontalLayout searchLayout = new HorizontalLayout(
            searchCodigoParticipacionField,
            searchButton,
            clearSearchButton
        );
        searchLayout.setDefaultVerticalComponentAlignment(Alignment.BASELINE);
        
        // Crear el Grid de solo lectura
        createReadOnlyGrid();
        
        layout.add(title, searchLayout, grid);
        getContent().add(layout);
        
        // Cargar datos iniciales
        refreshGrid();
    }

    private void createReadOnlyGrid() {
        // Columnas del grid (solo lectura)
        grid.addColumn(Participaciones::getCodigoParticipacion).setHeader("Código").setSortable(true);
        grid.addColumn(Participaciones::getDescripcion).setHeader("Descripción").setSortable(true);
        grid.addColumn(participacion -> participacion.getClase().getNombreClase())
            .setHeader("Clase").setSortable(true);
        grid.addColumn(participacion -> 
            participacion.getEstudiantes().stream()
                .map(Estudiantes::getCarnet)
                .collect(Collectors.joining(", ")))
            .setHeader("Estudiantes");
        grid.addColumn(Participaciones::getPuntos).setHeader("Puntos").setSortable(true);
        grid.addColumn(Participaciones::getFecha).setHeader("Fecha").setSortable(true);
        
        // Configuraciones adicionales del grid
        grid.setHeight("70vh");
        grid.setMultiSort(true);
    }

    private void searchParticipacion(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            refreshGrid();
            return;
        }
        
        // Búsqueda con coincidencias parciales
        String likeTerm = "%" + searchTerm + "%";
        List<Participaciones> results = participacionesController.findByCodigoParticipacionLike(likeTerm);
        
        if (results.isEmpty()) {
            Notification.show("No se encontraron participaciones con ese código");
            grid.setItems(List.of());
        } else {
            grid.setItems(results);
        }
    }

    private void refreshGrid() {
        List<Participaciones> participaciones = participacionesController.findAll();
        grid.setItems(participaciones);
    }
}