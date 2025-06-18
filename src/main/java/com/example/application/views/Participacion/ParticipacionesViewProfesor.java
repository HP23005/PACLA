package com.example.application.views.Participacion;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import com.example.application.controlador.ClaseController;
import com.example.application.controlador.EstudiantesController;
import com.example.application.controlador.ParticipacionController;
import com.example.application.modelo.Clase;
import com.example.application.modelo.Estudiantes;
import com.example.application.modelo.Participaciones;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Participación Form")
@Route(value = "participacionprofesor", layout = MainLayout.class)
public class ParticipacionesViewProfesor extends Composite<VerticalLayout> {

    private final ParticipacionController participacionesController;
    private final EstudiantesController estudiantesController;
    private final ClaseController claseController;

    // Componentes del formulario
    private final TextField codigoParticipacionField = new TextField("Código de Participación");
    private final TextField descripcionField = new TextField("Descripción");
    private final ComboBox<Clase> claseComboBox = new ComboBox<>("Clase");
    MultiSelectComboBox<Estudiantes> estudianteComboBox = new MultiSelectComboBox<>("Estudiantes");
    private final TextField puntosField = new TextField("Puntos");

    // Campos de búsqueda
    private final ComboBox<String> searchCodigoParticipacionComboBox = new ComboBox<>("Buscar por Código de Participación");
    private final ComboBox<String> searchCarnetEstudianteComboBox = new ComboBox<>("Buscar por Carnet de Estudiante");
    private final NumberField searchMinPuntosField = new NumberField("Puntos Mínimos");
    private final NumberField searchMaxPuntosField = new NumberField("Puntos Máximos");
    private final Button searchButton = new Button("Buscar");
    private final Button resetSearchButton = new Button("Reiniciar Búsqueda");

    // Grid
    private final Grid<Participaciones> grid = new Grid<>(Participaciones.class, false);

    public ParticipacionesViewProfesor(ParticipacionController participacionesController,
                                    EstudiantesController estudiantesController,
                                    ClaseController claseController) {

        this.participacionesController = participacionesController;
        this.estudiantesController = estudiantesController;
        this.claseController = claseController;

        VerticalLayout layout = new VerticalLayout();
        H3 title = new H3("Gestión de Participaciones");

        // --- SECCIÓN AGREGAR/GUARDAR ---
        FormLayout formLayout = new FormLayout();

        claseComboBox.setItems(claseController.findAll());
        claseComboBox.setItemLabelGenerator(Clase::getNombreClase);

        estudianteComboBox.setItems(estudiantesController.findAll());
        estudianteComboBox.setItemLabelGenerator(Estudiantes::getCarnet);

        formLayout.add(codigoParticipacionField, descripcionField, claseComboBox, estudianteComboBox, puntosField);

        Button saveButton = new Button("Guardar", event -> saveParticipacion());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancelar", event -> resetFields());

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);

        // --- SECCIÓN BÚSQUEDA ---
        searchButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        resetSearchButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        FormLayout searchFormLayout = new FormLayout();
        searchFormLayout.add(
                searchCodigoParticipacionComboBox,
                searchCarnetEstudianteComboBox,
                searchMinPuntosField,
                searchMaxPuntosField
        );

        HorizontalLayout searchButtonLayout = new HorizontalLayout(searchButton, resetSearchButton);

        configureSearch();
        configureResetSearch();
        loadSearchFilters();

        // --- SECCIÓN GRID ---
        createGrid();

        // Orden de visualización
        layout.add(
            title,
            formLayout, buttonLayout, // Sección AGREGAR/GUARDAR
            searchFormLayout, searchButtonLayout, // Sección BÚSQUEDA
            grid // Sección GRID
        );

        getContent().add(layout);
    }

    // --- MÉTODOS DE AGREGAR/GUARDAR ---

    private void saveParticipacion() {
        try {
            if (!validarCampos()) {
                return;
            }
            if (estudianteComboBox.getValue().isEmpty()) {
                Notification.show("Debe seleccionar al menos un estudiante");
                return;
            }
            String codigoParticipacion = codigoParticipacionField.getValue();
            Participaciones existingParticipacion = participacionesController.findByCodigoParticipacion(codigoParticipacion);
            if (existingParticipacion != null) {
                Notification notification = new Notification("El código de participación ya existe. Por favor, use uno diferente.");
                notification.setPosition(Notification.Position.MIDDLE);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                notification.setDuration(2000);
                notification.open();
                return;
            }
            Participaciones participacion = new Participaciones();
            participacion.setCodigoParticipacion(codigoParticipacion);
            participacion.setDescripcion(descripcionField.getValue());
            participacion.setFecha(LocalDate.now());
            participacion.setClase(claseComboBox.getValue());
            participacion.setEstudiantes(estudianteComboBox.getValue());
            int puntos;
            try {
                puntos = Integer.parseInt(puntosField.getValue());
                if (puntos <= 0) {
                    Notification notification = new Notification("El campo 'Puntos' debe ser un número mayor que 0.");
                    notification.setPosition(Notification.Position.MIDDLE);
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    notification.setDuration(1000);
                    notification.open();
                    return;
                }
            } catch (NumberFormatException e) {
                Notification.show("El campo 'Puntos' debe ser un número válido.");
                return;
            }
            participacion.setPuntos(puntos);
            Participaciones savedParticipacion = participacionesController.save(participacion);
            if (savedParticipacion != null) {
                Notification notification = new Notification("Participación guardada correctamente.");
                notification.setPosition(Notification.Position.MIDDLE);
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                notification.setDuration(1000);
                notification.open();
                refreshGrid(); // Refrescar el grid
                resetFields(); // Limpiar los campos del formulario
                loadSearchFilters(); // Actualizar filtros de búsqueda
            } else {
                Notification notification = new Notification("Hubo un problema al guardar la participación.");
                notification.setPosition(Notification.Position.MIDDLE);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                notification.setDuration(1000);
                notification.open();
            }
        } catch (Exception e) {
            Notification.show("Error al guardar la participación: " + e.getMessage());
        }
    }

    private boolean validarCampos() {
        if (codigoParticipacionField.isEmpty() || descripcionField.isEmpty() || puntosField.isEmpty()) {
            Notification.show("Todos los campos son obligatorios");
            return false;
        }
        if (claseComboBox.getValue() == null) {
            Notification.show("Debe seleccionar una clase");
            return false;
        }
        if (estudianteComboBox.getValue().isEmpty()) {
            Notification.show("Debe seleccionar al menos un estudiante");
            return false;
        }
        try {
            Integer.valueOf(puntosField.getValue());
        } catch (NumberFormatException e) {
            Notification.show("El campo 'Puntos' debe ser un número");
            return false;
        }
        return true;
    }

    private void resetFields() {
        codigoParticipacionField.clear();
        descripcionField.clear();
        puntosField.clear();
        claseComboBox.clear();
        estudianteComboBox.clear();
    }

    private void openEditDialog(Participaciones participacion) {
        Dialog dialog = new Dialog();
        FormLayout formLayout = new FormLayout();

        TextField editCodigoParticipacionField = new TextField("Código de Participación");
        TextField editDescripcionField = new TextField("Descripción");
        ComboBox<Clase> editClaseComboBox = new ComboBox<>("Clase");
        MultiSelectComboBox<Estudiantes> editEstudianteComboBox = new MultiSelectComboBox<>("Estudiantes");
        NumberField editPuntosField = new NumberField("Puntos");

        editCodigoParticipacionField.setValue(participacion.getCodigoParticipacion());
        editDescripcionField.setValue(participacion.getDescripcion());
        editClaseComboBox.setItems(claseController.findAll());
        editClaseComboBox.setItemLabelGenerator(Clase::getCodigoClase);
        editClaseComboBox.setValue(participacion.getClase());

        editEstudianteComboBox.setItems(estudiantesController.findAll());
        editEstudianteComboBox.setItemLabelGenerator(Estudiantes::getCarnet);
        editEstudianteComboBox.setValue(participacion.getEstudiantes());

        editPuntosField.setValue((double) participacion.getPuntos());
        editPuntosField.setMin(1);
        editPuntosField.setStep(1);

        formLayout.add(editCodigoParticipacionField, editDescripcionField, editClaseComboBox, editEstudianteComboBox, editPuntosField);

        Button saveButton = new Button("Guardar", event -> {
            try {
                if (editCodigoParticipacionField.isEmpty() ||
                    editDescripcionField.isEmpty() ||
                    editClaseComboBox.isEmpty() ||
                    editEstudianteComboBox.isEmpty()) {
                    Notification notification = new Notification("Todos los campos deben estar completos.");
                    notification.setPosition(Notification.Position.MIDDLE);
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    notification.setDuration(1000);
                    notification.open();
                    return;
                }
                if (editPuntosField.getValue() == null || editPuntosField.getValue() <= 0) {
                    Notification.show("El campo 'Puntos' debe ser un número mayor a 0.");
                    return;
                }
                participacion.setCodigoParticipacion(editCodigoParticipacionField.getValue());
                participacion.setDescripcion(editDescripcionField.getValue());
                participacion.setClase(editClaseComboBox.getValue());
                participacion.setEstudiantes(editEstudianteComboBox.getValue());
                participacion.setPuntos(editPuntosField.getValue().intValue());

                participacionesController.save(participacion);

                Notification notification = new Notification("Participación modificada correctamente.");
                notification.setPosition(Notification.Position.MIDDLE);
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                notification.setDuration(1000);
                notification.open();
                dialog.close();
                refreshGrid(); // Actualizar el grid
                loadSearchFilters(); // Actualizar filtros de búsqueda

            } catch (Exception e) {
                Notification.show("Error al guardar la participación: " + e.getMessage());
            }
        });

        Button cancelButton = new Button("Cancelar", event -> dialog.close());

        formLayout.add(new HorizontalLayout(saveButton, cancelButton));
        dialog.add(new VerticalLayout(new H3("Editar Participación"), formLayout));
        dialog.open();
    }

    // --- MÉTODOS DE BÚSQUEDA ---

    private void configureSearch() {
        searchButton.addClickListener(event -> searchParticipaciones());
    }

    private void searchParticipaciones() {
        String codigoParticipacion = searchCodigoParticipacionComboBox.getValue();
        String carnetEstudiante = searchCarnetEstudianteComboBox.getValue();
        Double minPuntos = searchMinPuntosField.getValue();
        Double maxPuntos = searchMaxPuntosField.getValue();

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

    private void configureResetSearch() {
        resetSearchButton.addClickListener(event -> {
            searchCodigoParticipacionComboBox.clear();
            searchCarnetEstudianteComboBox.clear();
            searchMinPuntosField.clear();
            searchMaxPuntosField.clear();
            refreshGrid();
            Notification.show("Filtros de búsqueda reiniciados.", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        });
    }

    private void loadSearchFilters() {
        List<String> codigosParticipacion = participacionesController.findAll().stream()
                .map(Participaciones::getCodigoParticipacion)
                .distinct()
                .collect(Collectors.toList());

        List<String> carnetsEstudiantes = participacionesController.findAll().stream()
                .flatMap(p -> p.getEstudiantes().stream())
                .map(Estudiantes::getCarnet)
                .distinct()
                .collect(Collectors.toList());

        searchCodigoParticipacionComboBox.setItems(codigosParticipacion);
        searchCarnetEstudianteComboBox.setItems(carnetsEstudiantes);

        searchCodigoParticipacionComboBox.setClearButtonVisible(true);
        searchCarnetEstudianteComboBox.setClearButtonVisible(true);
    }

    // --- SECCIÓN GRID Y UTILIDADES ---

    private void createGrid() {
        grid.addColumn(Participaciones::getCodigoParticipacion).setHeader("Código de Participación");
        grid.addColumn(Participaciones::getDescripcion).setHeader("Descripción");
        grid.addColumn(participacion -> participacion.getClase().getNombreClase()).setHeader("Clase");
        grid.addColumn(participacion -> participacion.getEstudiantes().stream().map(Estudiantes::getCarnet).collect(Collectors.joining(", ")))
            .setHeader("Estudiantes");
        grid.addColumn(Participaciones::getPuntos).setHeader("Puntos");
        grid.addComponentColumn(participacion -> {
            Button editButton = new Button("Editar");
            editButton.addClickListener(event -> openEditDialog(participacion));
            return editButton;
        }).setHeader("Editar");
        refreshGrid();
    }

    private void refreshGrid() {
        List<Participaciones> participaciones = participacionesController.findAll();
        grid.setItems(participaciones);
    }
}
