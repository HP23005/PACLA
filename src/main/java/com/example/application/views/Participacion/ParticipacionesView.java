package com.example.application.views.Participacion;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.application.controlador.ClaseController;
import com.example.application.controlador.EstudiantesController;
import com.example.application.controlador.ParticipacionController;
import com.example.application.modelo.Clase;
import com.example.application.modelo.Estudiantes;
import com.example.application.modelo.Participaciones;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.Text;
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

import jakarta.persistence.EntityNotFoundException;

@PageTitle("Participación Form")
@Route(value = "participacion", layout = MainLayout.class)
public class ParticipacionesView extends Composite<VerticalLayout> {

    private final ParticipacionController participacionesController;
    private final EstudiantesController estudiantesController;
    private final ClaseController claseController;

    // Componentes del formulario
    private final TextField codigoParticipacionField = new TextField("Código de Participación");
    private final TextField descripcionField = new TextField("Descripción");
    private final ComboBox<Clase> claseComboBox = new ComboBox<>("Clase");
    MultiSelectComboBox<Estudiantes> estudianteComboBox = new MultiSelectComboBox<>("Estudiantes");

    private final TextField puntosField = new TextField("Puntos");

    private final Grid<Participaciones> grid = new Grid<>(Participaciones.class, false);

    // Campos de búsqueda
    private final ComboBox<String> searchCodigoParticipacionComboBox = new ComboBox<>("Buscar por Código de Participación");
    private final ComboBox<String> searchCarnetEstudianteComboBox = new ComboBox<>("Buscar por Carnet de Estudiante");
    private final NumberField searchMinPuntosField = new NumberField("Puntos Mínimos");
    private final NumberField searchMaxPuntosField = new NumberField("Puntos Máximos");
    private final Button searchButton = new Button("Buscar");
    private final Button resetSearchButton = new Button("Reiniciar Búsqueda");
    private final Button deleteSelectedButton = new Button("Eliminar seleccionados");


    public ParticipacionesView(ParticipacionController participacionesController,
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

        deleteSelectedButton.addThemeVariants(ButtonVariant.LUMO_ERROR);    
        deleteSelectedButton.setEnabled(false);

        FormLayout searchFormLayout = new FormLayout();
        searchFormLayout.add(
                searchCodigoParticipacionComboBox,
                searchCarnetEstudianteComboBox,
                searchMinPuntosField,
                searchMaxPuntosField
        );

        HorizontalLayout searchButtonLayout = new HorizontalLayout(searchButton, resetSearchButton, deleteSelectedButton);

        configureSearch();
        configureResetSearch();
        loadSearchFilters();
        
         // Acción del botón para eliminar
        deleteSelectedButton.addClickListener(e -> deleteSelectedParticipaciones());
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

    
    private void saveParticipacion() {
        try {
            if (!validarCampos()) {
                return; // Si la validación falla, no continuar con el guardado
            }
    
            // Verificar que se haya seleccionado al menos un estudiante
            if (estudianteComboBox.getValue().isEmpty()) {
                Notification.show("Debe seleccionar al menos un estudiante");
                return;
            }
    
            // Verificar si el código de participación ya existe
            String codigoParticipacion = codigoParticipacionField.getValue();
            Participaciones existingParticipacion = participacionesController.findByCodigoParticipacion(codigoParticipacion); // Método para buscar por código
            if (existingParticipacion != null) {
                Notification notification = new Notification("El código de participación ya existe. Por favor, use uno diferente.");
                    notification.setPosition(Notification.Position.MIDDLE);  // Posiciona la notificación en el centro de la pantalla
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);  // Le da el estilo de error (fondo rojo)
                    notification.setDuration(2000);  // La notificación se cierra después de 2 segundos
                    notification.open();
                return;
            }
    
            // Crear la participación a guardar
            Participaciones participacion = new Participaciones();
            participacion.setCodigoParticipacion(codigoParticipacion);
            participacion.setDescripcion(descripcionField.getValue());
            participacion.setFecha(LocalDate.now());  // Usar la fecha actual
            participacion.setClase(claseComboBox.getValue());
    
            // Asumir que los estudiantes seleccionados son los que deben asociarse
            participacion.setEstudiantes(estudianteComboBox.getValue());
    
            // Validar y convertir el valor de puntos
            int puntos;
            try {
                puntos = Integer.parseInt(puntosField.getValue());
                if (puntos <= 0) {
                    Notification notification = new Notification("El campo 'Puntos' debe ser un número mayor que 0.");
                    notification.setPosition(Notification.Position.MIDDLE);  // Posiciona la notificación en el centro de la pantalla
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);  // Le da el estilo de error (fondo rojo)
                    notification.setDuration(1000);  // La notificación se cierra después de 3 segundos
                    notification.open();
    
                    return;
                }
            } catch (NumberFormatException e) {
                Notification.show("El campo 'Puntos' debe ser un número válido.");
                return;
            }
    
            participacion.setPuntos(puntos);
    
            // Intentar guardar la participación
            Participaciones savedParticipacion = participacionesController.save(participacion);
    
            if (savedParticipacion != null) {
                Notification notification = new Notification("Participación guardada correctamente.");
                notification.setPosition(Notification.Position.MIDDLE);  // Posiciona la notificación en el centro de la pantalla
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);  // Le da el estilo de éxito (fondo verde)
                notification.setDuration(1000);  // La notificación se cierra después de 3 segundos
                notification.open();
                refreshGrid(); // Refrescar el grid
                resetFields(); // Limpiar los campos del formulario
                loadSearchFilters(); // Actualizar filtros de búsqueda
            } else {
                Notification notification = new Notification("Hubo un problema al guardar la participación.");
                notification.setPosition(Notification.Position.MIDDLE);  // Posiciona la notificación en el centro de la pantalla
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);  // Le da el estilo de error (fondo rojo)
                notification.setDuration(1000);  // La notificación se cierra después de 3 segundos
                notification.open();
            }
    
        } catch (Exception e) {
            Notification.show("Error al guardar la participación: " + e.getMessage());
        }
    }
        

    // Validación de los campos del formulario
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


    private void openEditDialog(Participaciones participacion) {
        Dialog dialog = new Dialog();
        FormLayout formLayout = new FormLayout();

        TextField editCodigoParticipacionField = new TextField("Código de Participación");
        TextField editDescripcionField = new TextField("Descripción");
        ComboBox<Clase> editClaseComboBox = new ComboBox<>("Clase");
        MultiSelectComboBox<Estudiantes> editEstudianteComboBox = new MultiSelectComboBox<>("Estudiantes");
        NumberField editPuntosField = new NumberField("Puntos"); // <- Cambiado a NumberField

        // Configuración de campos
        editCodigoParticipacionField.setValue(participacion.getCodigoParticipacion());
        editDescripcionField.setValue(participacion.getDescripcion());
        editClaseComboBox.setItems(claseController.findAll());
        editClaseComboBox.setItemLabelGenerator(Clase::getCodigoClase);
        editClaseComboBox.setValue(participacion.getClase());

        editEstudianteComboBox.setItems(estudiantesController.findAll());
        editEstudianteComboBox.setItemLabelGenerator(Estudiantes::getCarnet);
        editEstudianteComboBox.setValue(participacion.getEstudiantes());

        editPuntosField.setValue((double) participacion.getPuntos());
        editPuntosField.setMin(1); // <- No permitir 0 o negativos
        editPuntosField.setStep(1); // Solo enteros

        formLayout.add(editCodigoParticipacionField, editDescripcionField, editClaseComboBox, editEstudianteComboBox, editPuntosField);

        Button saveButton = new Button("Guardar", event -> {
            try {
                // Validar campos
                if (editCodigoParticipacionField.isEmpty() ||
                    editDescripcionField.isEmpty() ||
                    editClaseComboBox.isEmpty() ||
                    editEstudianteComboBox.isEmpty()) {
                    Notification notification = new Notification("Todos los campos deben estar completos.");
                        notification.setPosition(Notification.Position.MIDDLE);  // Posiciona la notificación en el centro de la pantalla
                        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);  // Le da el estilo de error (fondo rojo)
                        notification.setDuration(1000);  // La notificación se cierra después de 3 segundos
                        notification.open();
                    return;
                }

                if (editPuntosField.getValue() == null || editPuntosField.getValue() <= 0) {
                    Notification.show("El campo 'Puntos' debe ser un número mayor a 0.");
                    return;
                }

                // Actualizar la participación
                participacion.setCodigoParticipacion(editCodigoParticipacionField.getValue());
                participacion.setDescripcion(editDescripcionField.getValue());
                participacion.setClase(editClaseComboBox.getValue());
                participacion.setEstudiantes(editEstudianteComboBox.getValue());
                participacion.setPuntos(editPuntosField.getValue().intValue());

                participacionesController.save(participacion);

                Notification notification = new Notification("Participación modificada correctamente.");
                notification.setPosition(Notification.Position.MIDDLE);  // Posiciona la notificación en el centro de la pantalla
                    notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);  // Le da el estilo de éxito (fondo verde)
                    notification.setDuration(1000);  // La notificación se cierra después de 3 segundos
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

    private void confirmDeleteParticipacion(Participaciones participacion) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Eliminar Participación");

        confirmDialog.add(new Text("¿Estás seguro de que deseas eliminar esta participación, aunque tenga estudiantes asociados?"));

        Button deleteButton = new Button("Eliminar", event -> {
            try {
                Long participacionId = participacion.getId();

                if (participacionId == null) {
                    Notification notification = new Notification("Error: La participación no tiene un ID válido.");
                    notification.setPosition(Notification.Position.MIDDLE);
                    notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    notification.setDuration(2000);
                    notification.open();
                    return;
                }

                // Desvincular la participación de los estudiantes asociados
                for (Estudiantes estudiante : participacion.getEstudiantes()) {
                    estudiante.getParticipacion().remove(participacion);
                }

                // Eliminar la participación
                participacionesController.delete(participacionId);

                Notification notification = new Notification("Participación eliminada correctamente.");
                notification.setPosition(Notification.Position.MIDDLE);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR); // Notificación en rojo
                notification.setDuration(2000);
                notification.open();

                confirmDialog.close();
                refreshGrid(); // Actualizar el grid
                loadSearchFilters(); // Actualizar filtros de búsqueda

            } catch (EntityNotFoundException e) {
                Notification notification = new Notification("Error: La participación no fue encontrada.");
                notification.setPosition(Notification.Position.MIDDLE);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                notification.setDuration(2000);
                notification.open();
            } catch (Exception e) {
                Notification notification = new Notification("Error al eliminar la participación.");
                notification.setPosition(Notification.Position.MIDDLE);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                notification.setDuration(2000);
                notification.open();
            }
        });

        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR); // Botón de eliminación con estilo de error

        Button cancelButton = new Button("Cancelar", event -> confirmDialog.close());

        confirmDialog.getFooter().add(deleteButton, cancelButton);
        confirmDialog.open(); // Mostrar el diálogo de confirmación
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

    // Crear el grid de participaciones
    private void createGrid() {
        grid.setSelectionMode(Grid.SelectionMode.MULTI);

        
        grid.addColumn(Participaciones::getCodigoParticipacion).setHeader("Código de Participación");
        grid.addColumn(Participaciones::getDescripcion).setHeader("Descripción");
        grid.addColumn(participacion -> participacion.getClase().getNombreClase()).setHeader("Clase");
        grid.addColumn(participacion -> participacion.getEstudiantes().stream().map(Estudiantes::getCarnet).collect(Collectors.joining(", ")))
            .setHeader("Estudiantes");  // Mostrar los estudiantes como una lista separada por comas
        grid.addColumn(Participaciones::getPuntos).setHeader("Puntos");

        grid.addComponentColumn(participacion -> {
            Button editButton = new Button("Editar");
            editButton.addClickListener(event -> openEditDialog(participacion));
            return editButton;
        }).setHeader("Editar");

        grid.addComponentColumn(participacion -> {
            Button deleteButton = new Button("Eliminar");
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR); // Estilo de error para eliminar
            deleteButton.addClickListener(event -> confirmDeleteParticipacion(participacion));
            return deleteButton;
        }).setHeader("Eliminar");

        refreshGrid(); // Cargar todas las participaciones

        grid.addSelectionListener(event -> {
            deleteSelectedButton.setEnabled(!event.getAllSelectedItems().isEmpty());
        });
    }

    private void deleteSelectedParticipaciones() {
        Set<Participaciones> seleccionadas = grid.getSelectedItems();

        if (seleccionadas == null || seleccionadas.isEmpty()) {
            Notification.show("No hay participaciones seleccionadas.", 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Eliminar Participaciones");

        confirmDialog.add(new Text("¿Estás seguro de que deseas eliminar las " + seleccionadas.size() + " participaciones seleccionadas? "
            + "Se eliminarán incluso si tienen estudiantes asociados."));

        Button eliminarBtn = new Button("Eliminar", e -> {
            try {
                for (Participaciones p : seleccionadas) {
                    // Desvincular estudiantes (si hay)
                    for (Estudiantes estudiante : p.getEstudiantes()) {
                        estudiante.getParticipacion().remove(p);
                    }

                    // Eliminar desde el controlador
                    participacionesController.delete(p.getId());
                }

                confirmDialog.close();
                refreshGrid();
                loadSearchFilters(); // (Si tienes filtros activos)
                Notification.show("Participaciones eliminadas correctamente", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            } catch (EntityNotFoundException ex) {
                Notification.show("Error: Alguna participación no fue encontrada.", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (Exception ex) {
                Notification.show("Error al eliminar las participaciones.", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        eliminarBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Button cancelarBtn = new Button("Cancelar", e -> confirmDialog.close());
        confirmDialog.getFooter().add(new HorizontalLayout(eliminarBtn, cancelarBtn));

        confirmDialog.open();
    }

    // Actualizar el grid para mostrar las participaciones
    private void refreshGrid() {
        List<Participaciones> participaciones = participacionesController.findAll();
        grid.setItems(participaciones);
        if (participaciones.isEmpty()) {
            Notification.show("No hay participaciones registradas.", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
        else {
            Notification.show("Participaciones cargadas: " + participaciones.size(), 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        }
        grid.deselectAll(); // Deselecciona todos los elementos después de actualizar
        deleteSelectedButton.setEnabled(false); // Deshabilita el botón de eliminar después de actualizar
    }

    // Resetear los campos del formulario
    private void resetFields() {
        codigoParticipacionField.clear();
        descripcionField.clear();
        puntosField.clear();
        claseComboBox.clear();
        estudianteComboBox.clear();
    }
}
