package com.example.application.views.Participacion;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

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
@Route(value = "participacion-form", layout = MainLayout.class)
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

    // Campo de búsqueda por código de participación
    private final TextField searchCodigoParticipacionField = new TextField("Buscar por Código de Participación");
    private final Button searchButtonCodigoParticipacion = new Button("Buscar");
    private final Button resetSearchButton = new Button("Reiniciar Búsqueda");

    public ParticipacionesView(@Autowired ParticipacionController participacionesController,
                                  @Autowired EstudiantesController estudiantesController,
                                  @Autowired ClaseController claseController) {
        this.participacionesController = participacionesController;
        this.estudiantesController = estudiantesController;
        this.claseController = claseController;

        VerticalLayout layout = new VerticalLayout();
        H3 title = new H3("Gestión de Participaciones");
        FormLayout formLayout = new FormLayout();

        // Configuración de los ComboBox
        claseComboBox.setItems(claseController.findAll()); // Poblar con clases disponibles
        claseComboBox.setItemLabelGenerator(Clase::getNombreClase);

        estudianteComboBox.setItems(estudiantesController.findAll()); // Poblar con estudiantes disponibles
        estudianteComboBox.setItemLabelGenerator(Estudiantes::getCarnet);

        // Agregar los campos al formulario
        formLayout.add(codigoParticipacionField, descripcionField, claseComboBox, estudianteComboBox, puntosField);

        // Botones
        Button saveButton = new Button("Guardar", event -> saveParticipacion()); // Guardar la participación
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancelar", event -> resetFields()); // Cancelar la creación/edición

        // Layout para los botones de búsqueda
        HorizontalLayout searchButtonsLayout = new HorizontalLayout(searchButtonCodigoParticipacion, resetSearchButton);

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);

        // Crear el Grid para mostrar las participaciones
        createGrid(); // Crear el Grid de participaciones

        configureSearch(); // Configurar la funcionalidad de búsqueda

        // Configurar el botón para reiniciar la búsqueda
        configureResetSearch();

        layout.add(title, formLayout, buttonLayout, searchCodigoParticipacionField, searchButtonsLayout, grid);
        getContent().add(layout);
    }

    // Configuración del botón de búsqueda
    private void configureSearch() {
        searchButtonCodigoParticipacion.addClickListener(event -> searchParticipacion()); // Accionar búsqueda al hacer click
    }

    // Función para buscar participaciones por código (con coincidencias parciales)
    private void searchParticipacion() {
        String codigoParticipacion = searchCodigoParticipacionField.getValue().trim();

        if (!codigoParticipacion.isEmpty()) {
            // Agregar los comodines '%' para buscar coincidencias parciales
            String searchTerm = "%" + codigoParticipacion + "%";  // Coincidencias parciales
            
            // Buscar participaciones que contengan parcialmente el código ingresado
            var participaciones = participacionesController.findByCodigoParticipacionLike(searchTerm);
            
            if (!participaciones.isEmpty()) {
                grid.setItems(participaciones);  // Mostrar las participaciones encontradas
                Notification.show("Participaciones encontradas");
            } else {
                Notification.show("No se encontraron participaciones con ese código");
                grid.setItems(List.of());  // Mostrar un grid vacío
            }
        } else {
            refreshGrid();  // Mostrar todas las participaciones si el campo está vacío
        }
    }

    // Configurar el botón para reiniciar la búsqueda
    private void configureResetSearch() {
        resetSearchButton.addClickListener(event -> resetSearch());
    }

    // Función de reset para la búsqueda
    private void resetSearch() {
        searchCodigoParticipacionField.clear(); // Limpiar el campo de búsqueda
        refreshGrid(); // Refrescar el grid para mostrar todas las participaciones
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
                refreshGrid();

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
                    Notification.show("Error: La participación no tiene un ID válido.");
                    return;
                }
    
                // Asegurarse de desvincular la participación de los estudiantes asociados
                // Aquí asumimos que tienes una lista de estudiantes relacionados a esta participación
                for (Estudiantes estudiante : participacion.getEstudiantes()) {
                    estudiante.getParticipacion().remove(participacion);  // Eliminamos la participación de la lista de cada estudiante
                }
    
                // Eliminar la participación
                participacionesController.delete(participacionId);
    
                Notification.show("Participación eliminada correctamente", 3000, Notification.Position.MIDDLE);
                confirmDialog.close();
                refreshGrid(); // Actualizar el grid
            } catch (EntityNotFoundException e) {
                Notification.show("La participación no fue encontrada", 3000, Notification.Position.MIDDLE);
            } catch (Exception e) {
                Notification.show("Hubo un error al eliminar la participación", 3000, Notification.Position.MIDDLE);
            }
        });
    
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR); // Botón de eliminación con estilo de error
    
        Button cancelButton = new Button("Cancelar", event -> confirmDialog.close());
    
        confirmDialog.getFooter().add(deleteButton, cancelButton);
        confirmDialog.open(); // Mostrar el diálogo de confirmación
    }
    

    // Crear el grid de participaciones
    private void createGrid() {
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
    }

    // Actualizar el grid para mostrar las participaciones
    private void refreshGrid() {
        List<Participaciones> participaciones = participacionesController.findAll();
        grid.setItems(participaciones);
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
