package com.example.application.views.Participacion;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import com.example.application.controlador.ParticipacionController;
import com.example.application.modelo.Estudiantes;
import com.example.application.modelo.Participaciones;
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

@PageTitle("Participaciones")
@Route(value = "participaciones-consulta", layout = MainLayout.class)
public class ParticipacionesReadOnlyView extends Composite<VerticalLayout> {

    private final ParticipacionController participacionesController;
    private final Grid<Participaciones> grid = new Grid<>(Participaciones.class, false);

    // Componentes de búsqueda
    private final ComboBox<String> searchCodigoParticipacionComboBox = new ComboBox<>("Buscar por Código de Participación");
    private final ComboBox<String> searchCarnetEstudianteComboBox = new ComboBox<>("Buscar por Carnet de Estudiante");
    private final NumberField searchMinPuntosField = new NumberField("Puntos Mínimos");
    private final NumberField searchMaxPuntosField = new NumberField("Puntos Máximos");
    private final DatePicker searchFechaDesdePicker = new DatePicker("Fecha Desde");
    private final DatePicker searchFechaHastaPicker = new DatePicker("Fecha Hasta");

    private final Button searchButton = new Button("Buscar");
    private final Button resetSearchButton = new Button("Reiniciar Búsqueda");

    public ParticipacionesReadOnlyView(ParticipacionController participacionesController) {
        this.participacionesController = participacionesController;

        VerticalLayout layout = new VerticalLayout();
        H3 title = new H3("Listado de Participaciones");

        // --- SECCIÓN BÚSQUEDA ---
        FormLayout searchFormLayout = new FormLayout();

        // Configurar ComboBoxes con datos iniciales
        List<Participaciones> todasParticipaciones = participacionesController.findAll();

        searchCodigoParticipacionComboBox.setItems(
            todasParticipaciones.stream()
                .map(Participaciones::getCodigoParticipacion)
                .distinct()
                .collect(Collectors.toList())
        );
        searchCodigoParticipacionComboBox.setPlaceholder("Seleccione código");
        searchCodigoParticipacionComboBox.setClearButtonVisible(true);

        searchCarnetEstudianteComboBox.setItems(
            todasParticipaciones.stream()
                .flatMap(p -> p.getEstudiantes().stream())
                .map(Estudiantes::getCarnet)
                .distinct()
                .collect(Collectors.toList())
        );
        searchCarnetEstudianteComboBox.setPlaceholder("Seleccione carnet");
        searchCarnetEstudianteComboBox.setClearButtonVisible(true); 

        searchFechaDesdePicker.setClearButtonVisible(true);
        searchFechaHastaPicker.setClearButtonVisible(true);


        searchFormLayout.add(
            searchCodigoParticipacionComboBox,
            searchCarnetEstudianteComboBox,
            searchMinPuntosField,
            searchMaxPuntosField,
            searchFechaDesdePicker,
            searchFechaHastaPicker
        );

        // Botones de búsqueda y reset
        searchButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        resetSearchButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        HorizontalLayout searchButtonLayout = new HorizontalLayout(searchButton, resetSearchButton);

        // Configurar eventos de los botones
        searchButton.addClickListener(e -> searchParticipaciones());
        resetSearchButton.addClickListener(e -> resetSearch());

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

        grid.setHeight("70vh");
        grid.setMultiSort(true);
    }

    private void searchParticipaciones() {
        String codigoParticipacion = searchCodigoParticipacionComboBox.getValue();
        String carnetEstudiante = searchCarnetEstudianteComboBox.getValue();
        Double minPuntos = searchMinPuntosField.getValue();
        Double maxPuntos = searchMaxPuntosField.getValue();
        LocalDate fechaDesde = searchFechaDesdePicker.getValue();
        LocalDate fechaHasta = searchFechaHastaPicker.getValue();

        List<Participaciones> participaciones = participacionesController.findAll();

        if (codigoParticipacion != null && !codigoParticipacion.isEmpty()) {
            participaciones = participaciones.stream()
                    .filter(p -> p.getCodigoParticipacion().equalsIgnoreCase(codigoParticipacion))
                    .collect(Collectors.toList());
        }
        if (carnetEstudiante != null && !carnetEstudiante.isEmpty()) {
            participaciones = participaciones.stream()
                    .filter(p -> p.getEstudiantes().stream()
                            .anyMatch(e -> e.getCarnet().equalsIgnoreCase(carnetEstudiante)))
                    .collect(Collectors.toList());
        }
        if (minPuntos != null) {
            participaciones = participaciones.stream()
                    .filter(p -> p.getPuntos() >= minPuntos)
                    .collect(Collectors.toList());
        }
        if (maxPuntos != null) {
            participaciones = participaciones.stream()
                    .filter(p -> p.getPuntos() <= maxPuntos)
                    .collect(Collectors.toList());
        }
        if (fechaDesde != null) {
            participaciones = participaciones.stream()
                    .filter(p -> !p.getFecha().isBefore(fechaDesde))
                    .collect(Collectors.toList());
        }
        if (fechaHasta != null) {
            participaciones = participaciones.stream()
                    .filter(p -> !p.getFecha().isAfter(fechaHasta))
                    .collect(Collectors.toList());
        }
        if (!participaciones.isEmpty()) {
            grid.setItems(participaciones);
            Notification.show("Participaciones encontradas: " + participaciones.size(), 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } else {
            grid.setItems(List.of());
            Notification.show("No se encontraron participaciones con los criterios ingresados.", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void resetSearch() {
        searchCodigoParticipacionComboBox.clear();
        searchCarnetEstudianteComboBox.clear();
        searchMinPuntosField.clear();
        searchMaxPuntosField.clear();
        searchFechaDesdePicker.clear();
        searchFechaHastaPicker.clear();
        refreshGrid();
            Notification.show("Filtros de búsqueda reiniciados.", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    private void refreshGrid() {
        List<Participaciones> participaciones = participacionesController.findAll();
        grid.setItems(participaciones);
    }
}
