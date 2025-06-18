package com.example.application.views.Clase;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import com.example.application.controlador.ClaseController;
import com.example.application.modelo.Clase;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Consulta de Clases")
@Route(value = "consulta-clases", layout = MainLayout.class)
public class ClasesReadOnlyView extends Composite<VerticalLayout> {

    private final ClaseController claseController;
    private final Grid<Clase> grid = new Grid<>(Clase.class, false);

    // Componentes de búsqueda
    private final ComboBox<String> searchCodigoClaseComboBox = new ComboBox<>("Buscar por Código de Clase");
    private final NumberField searchMaxEstudiantesField = new NumberField("Máx. Estudiantes");
    private final DatePicker searchFechaInicioPicker = new DatePicker("Fecha de Inicio");
    private final DatePicker searchFechaFinPicker = new DatePicker("Fecha de Fin");
    private final Button searchButton = new Button("Buscar");
    private final Button clearSearchButton = new Button("Limpiar");

    public ClasesReadOnlyView(ClaseController claseController) {
        this.claseController = claseController;

        VerticalLayout layout = new VerticalLayout();
        H3 title = new H3("Consulta de Clases");

        // --- SECCIÓN BÚSQUEDA ---
        FormLayout searchFormLayout = new FormLayout();

        // Configurar ComboBox con datos iniciales
        List<Clase> clases = claseController.findAll();

        searchCodigoClaseComboBox.setItems(
            clases.stream()
                .map(Clase::getCodigoClase)
                .distinct()
                .collect(Collectors.toList())
        );
        searchCodigoClaseComboBox.setPlaceholder("Seleccione código...");
        searchCodigoClaseComboBox.setClearButtonVisible(true);

        searchMaxEstudiantesField.setPlaceholder("Ingrese máximo...");
        searchFechaInicioPicker.setClearButtonVisible(true);
        searchFechaFinPicker.setClearButtonVisible(true);

        searchFormLayout.add(
            searchCodigoClaseComboBox,
            searchMaxEstudiantesField,
            searchFechaInicioPicker,
            searchFechaFinPicker
        );

        // Botones de búsqueda y limpiar
        searchButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        clearSearchButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        HorizontalLayout searchButtonLayout = new HorizontalLayout(searchButton, clearSearchButton);

        // Configurar eventos de los botones
        searchButton.addClickListener(e -> searchClases());
        clearSearchButton.addClickListener(e -> clearSearch());

        // --- SECCIÓN GRID ---
        createReadOnlyGrid();

        // Orden de los componentes en el layout principal
        layout.add(
            title,
            searchFormLayout,
            searchButtonLayout,
            grid
        );

        getContent().add(layout);

        // Cargar datos iniciales en el grid
        refreshGrid();
    }

    private void createReadOnlyGrid() {
        grid.addColumn(Clase::getCodigoClase).setHeader("Código").setSortable(true);
        grid.addColumn(Clase::getNombreClase).setHeader("Nombre").setSortable(true);
        grid.addColumn(Clase::getDescripcion).setHeader("Descripción");
        grid.addColumn(Clase::getFechaInicio).setHeader("Fecha Inicio").setSortable(true);
        grid.addColumn(Clase::getFechaFin).setHeader("Fecha Fin").setSortable(true);
        grid.addColumn(Clase::getProfesor).setHeader("Profesor").setSortable(true);
        grid.addColumn(Clase::getMaxEstudiantes).setHeader("Máx. Estudiantes").setSortable(true);

        grid.setHeight("70vh");
        grid.setMultiSort(true);
    }

    private void searchClases() {
        String codigo = searchCodigoClaseComboBox.getValue();
        Double maxEstudiantes = searchMaxEstudiantesField.getValue();
        LocalDate fechaInicio = searchFechaInicioPicker.getValue();
        LocalDate fechaFin = searchFechaFinPicker.getValue();

        List<Clase> clases = claseController.findAll();

        if (codigo != null && !codigo.trim().isEmpty()) {
            clases = clases.stream()
                    .filter(c -> c.getCodigoClase().equalsIgnoreCase(codigo.trim()))
                    .collect(Collectors.toList());
        }

        if (maxEstudiantes != null) {
            clases = clases.stream()
                    .filter(c -> c.getMaxEstudiantes() <= maxEstudiantes)
                    .collect(Collectors.toList());
        }

        if (fechaInicio != null) {
            clases = clases.stream()
                    .filter(c -> !c.getFechaInicio().isBefore(fechaInicio))
                    .collect(Collectors.toList());
        }

        if (fechaFin != null) {
            clases = clases.stream()
                    .filter(c -> !c.getFechaFin().isAfter(fechaFin))
                    .collect(Collectors.toList());
        }

        if (!clases.isEmpty()) {
            grid.setItems(clases);
            Notification.show("Clases encontradas: " + clases.size(), 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } else {
            grid.setItems(List.of());
            Notification.show("No se encontraron clases con los criterios ingresados.", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void clearSearch() {
        searchCodigoClaseComboBox.clear();
        searchMaxEstudiantesField.clear();
        searchFechaInicioPicker.clear();
        searchFechaFinPicker.clear();
        refreshGrid();
        Notification.show("Filtros de búsqueda reiniciados.", 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    private void refreshGrid() {
        List<Clase> clases = claseController.findAll();
        grid.setItems(clases);

        // Refrescar los códigos disponibles en el ComboBox
        searchCodigoClaseComboBox.setItems(clases.stream()
                .map(Clase::getCodigoClase)
                .distinct()
                .collect(Collectors.toList()));
    }
}
