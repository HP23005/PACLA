package com.example.application.views.Clase;

import java.util.List;

import com.example.application.controlador.ClaseController;
import com.example.application.modelo.Clase;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Composite;
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

@PageTitle("Consulta de Clases")
@Route(value = "consulta-clases", layout = MainLayout.class)
public class ClasesReadOnlyView extends Composite<VerticalLayout> {

    private final ClaseController claseController;
    private final Grid<Clase> grid = new Grid<>(Clase.class, false);
    
    // Componentes de búsqueda
    private final TextField searchCodigoClaseField = new TextField("Buscar por Código de Clase");
    private final Button searchButton = new Button("Buscar");
    private final Button clearSearchButton = new Button("Limpiar");

    public ClasesReadOnlyView(ClaseController claseController) {
        this.claseController = claseController;

        VerticalLayout layout = new VerticalLayout();
        H3 title = new H3("Consulta de Clases");
        
        // Configurar el campo de búsqueda
        searchCodigoClaseField.setPlaceholder("Ingrese código...");
        
        // Configurar botones
        searchButton.addClickListener(e -> searchClase(searchCodigoClaseField.getValue()));
        clearSearchButton.addClickListener(e -> {
            searchCodigoClaseField.clear();
            refreshGrid();
        });
        
        // Layout para los controles de búsqueda
        HorizontalLayout searchLayout = new HorizontalLayout(
            searchCodigoClaseField,
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
        grid.addColumn(Clase::getCodigoClase).setHeader("Código").setSortable(true);
        grid.addColumn(Clase::getNombreClase).setHeader("Nombre").setSortable(true);
        grid.addColumn(Clase::getDescripcion).setHeader("Descripción");
        grid.addColumn(Clase::getFechaInicio).setHeader("Fecha Inicio").setSortable(true);
        grid.addColumn(Clase::getFechaFin).setHeader("Fecha Fin").setSortable(true);
        grid.addColumn(Clase::getProfesor).setHeader("Profesor").setSortable(true);
        grid.addColumn(Clase::getMaxEstudiantes).setHeader("Máx. Estudiantes").setSortable(true);
        
        // Configuraciones adicionales del grid
        grid.setHeight("70vh");
        grid.setMultiSort(true);
    }

    private void searchClase(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            refreshGrid();
            return;
        }
        
        // Búsqueda exacta (puedes cambiarlo a LIKE si prefieres)
        List<Clase> results = claseController.findByCodigoClase(searchTerm);
        
        if (results.isEmpty()) {
            Notification.show("No se encontraron clases con ese código");
            grid.setItems(List.of());
        } else {
            grid.setItems(results);
        }
    }

    private void refreshGrid() {
        List<Clase> clases = claseController.findAll();
        grid.setItems(clases);
    }
}