package com.example.application.views.Estudiante;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.example.application.controlador.EstudiantesController;
import com.example.application.modelo.Estudiantes;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

// Define el título de la página que se mostrará en el navegador
@PageTitle("Estudiantes")
@Route(value = "", layout = MainLayout.class)
public class EstudiantesView extends Composite<VerticalLayout> {

    // Dependencias inyectadas a través de Spring
    private final EstudiantesController estudiantesController;
    private Estudiantes currentEstudiante = null;

    // Campos del formulario
    private final TextField nombresEstudianteField = new TextField("Nombres Estudiante");
    private final TextField apellidosEstudianteField = new TextField("Apellidos Estudiante");
    private final TextField carnetField = new TextField("Carnet");
    private final TextField estadoEstudianteField = new TextField("Estado Estudiante");
    private final DatePicker fechaNacimientoField = new DatePicker("Fecha de Nacimiento");
    private final TextField nivelAcademicoField = new TextField("Nivel Académico");
    private final TextField nombrePadreField = new TextField("Nombre del Padre (Opcional)");
    private final TextField nombreMadreField = new TextField("Nombre de la Madre (Opcional)");

    // Campos de búsqueda
    private final TextField searchField = new TextField("Buscar por Carnet");
    private final Button buttonPrimary = new Button("Buscar por Carnet");
    private final Button buttonSecondary = new Button("Reiniciar Búsqueda");
    private final Grid<Estudiantes> grid = new Grid<>(Estudiantes.class, false);

    // Para cargar la foto del estudiante
    private final MemoryBuffer buffer = new MemoryBuffer();
    private final Upload fotoUpload = new Upload(buffer);

    // Constructor con inyección de dependencias
    public EstudiantesView(@Autowired EstudiantesController estudiantesController) {
        this.estudiantesController = estudiantesController;

        // Título de la vista
        H3 title = new H3("Gestión de Estudiantes");

        // Crear el formulario de entrada, botones y la grilla
        FormLayout formLayout = createFormLayout();
        HorizontalLayout buttonLayout = createButtonLayout();
        createGrid();

        // Configurar los botones de búsqueda
        buttonPrimary.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonPrimary.addClickListener(event -> filterGridByCarnet());

        buttonSecondary.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        buttonSecondary.addClickListener(event -> resetSearch());

        // Layout de búsqueda
        HorizontalLayout searchButtonsLayout = new HorizontalLayout(buttonPrimary, buttonSecondary);

        // Layout principal
        VerticalLayout layout = new VerticalLayout(title, formLayout, buttonLayout, searchField, searchButtonsLayout, grid);
        layout.setSizeFull();
        layout.setSpacing(true);

        getContent().add(layout);
    }

    // Filtra la grilla de estudiantes por carnet
    private void filterGridByCarnet() {
        String carnet = searchField.getValue().trim();

        if (!carnet.isEmpty()) {
            List<Estudiantes> estudiantesList = estudiantesController.findByCarnet(carnet);

            if (!estudiantesList.isEmpty()) {
                grid.setItems(estudiantesList);  
                Notification.show("Estudiantes encontrados");
            } else {
                Notification.show("No se encontró un estudiante con el carnet ingresado");
                grid.setItems(estudiantesController.findAll());
            }
        } else {
            grid.setItems(estudiantesController.findAll());
        }
    }

    // Reinicia la búsqueda
    private void resetSearch() {
        searchField.clear();
        grid.setItems(estudiantesController.findAll());
    }

    // Crea el formulario de entrada con los campos necesarios
    private FormLayout createFormLayout() {
        FormLayout formLayout = new FormLayout();

        H4 photoHeading = new H4("Ingrese la foto del estudiante:");

        formLayout.add(nombresEstudianteField, apellidosEstudianteField, carnetField,
                estadoEstudianteField, fechaNacimientoField, nivelAcademicoField,
                nombrePadreField, nombreMadreField, photoHeading, fotoUpload);

        // Configuración del tipo de archivo y tamaño permitido para la foto
        fotoUpload.setAcceptedFileTypes("image/jpeg", "image/png");
        fotoUpload.setMaxFiles(1);

        // Listener para manejar la carga de la imagen
        fotoUpload.addSucceededListener(event -> {
            try (InputStream inputStream = buffer.getInputStream()) {
                byte[] imageBytes = inputStream.readAllBytes();
                if (currentEstudiante == null) {
                    currentEstudiante = new Estudiantes();
                }
                currentEstudiante.setFoto(imageBytes);
                Notification.show("Imagen cargada en memoria.");
            } catch (IOException e) {
                Notification.show("Error al cargar la imagen: " + e.getMessage());
            }
        });

        return formLayout;
    }

    // Crea el layout con los botones Guardar y Cancelar
    private HorizontalLayout createButtonLayout() {
        Button saveButton = new Button("Guardar", event -> saveEstudiante());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancelar", event -> resetFields());

        return new HorizontalLayout(saveButton, cancelButton);
    }

    // Crea la grilla para visualizar a los estudiantes
    private void createGrid() {
        grid.addColumn(Estudiantes::getCarnet).setHeader("Carnet").setSortable(true);
        grid.addColumn(Estudiantes::getNombresEstudiante).setHeader("Nombres");
        grid.addColumn(Estudiantes::getApellidosEstudiante).setHeader("Apellidos");
        grid.addColumn(Estudiantes::getEstadoEstudiante).setHeader("Estado");
        grid.addColumn(Estudiantes::getFechaNacimiento).setHeader("Fecha de Nacimiento");
        grid.addColumn(Estudiantes::getNivelAcademico).setHeader("Nivel Académico");
        grid.addColumn(Estudiantes::getNombrePadre).setHeader("Nombre Padre");
        grid.addColumn(Estudiantes::getNombreMadre).setHeader("Nombre Madre");

        // Columna para mostrar la foto
        grid.addColumn(new ComponentRenderer<>(estudiante -> {
            if (estudiante.getFoto() != null) {
                String base64Image = "data:image/jpeg;base64," + java.util.Base64.getEncoder().encodeToString(estudiante.getFoto());
                Image image = new Image(base64Image, "Foto");
                image.setWidth("50px");
                image.setHeight("50px");
                return image;
            }
            return new Text("Sin Foto");
        })).setHeader("Foto");

        // Columna de botón para editar
        grid.addColumn(new ComponentRenderer<>(estudiante -> {
            Button editButton = new Button("Editar");
            editButton.addClickListener(e -> editEstudiante(estudiante));
            return editButton;
        })).setHeader("Editar");

        // Columna de botón para eliminar
        grid.addColumn(new ComponentRenderer<>(estudiante -> {
            Button deleteButton = new Button("Eliminar");
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            deleteButton.addClickListener(e -> confirmDeleteEstudiante(estudiante));
            return deleteButton;
        })).setHeader("Eliminar");

        // Cargar todos los estudiantes en la grilla
        grid.setItems(estudiantesController.findAll());
    }

    // Guarda el estudiante (nuevo o editado)
    private void saveEstudiante() {
        if (validateInputs()) {
            // Validar si la fecha de nacimiento es del futuro
            if (fechaNacimientoField.getValue() != null && fechaNacimientoField.getValue().isAfter(java.time.LocalDate.now())) {
                Notification notification = new Notification("La fecha de nacimiento no puede ser del futuro.");
                notification.setPosition(Notification.Position.MIDDLE);  // Posiciona la notificación en el centro de la pantalla
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);  // Estilo de error (fondo rojo)
                notification.setDuration(2000);  // La notificación se cierra después de 2 segundo
                notification.open();
                return; // No continuar con la operación
            }
    
            // Si la fecha de nacimiento es válida, continuar con el proceso de guardado
            if (currentEstudiante == null) {
                currentEstudiante = new Estudiantes();
            }
            setEstudianteData();
    
            if (estudiantesController.existsByCarnet(currentEstudiante.getCarnet())) {
                Notification notification = new Notification("El carnet ingresado ya está registrado. Por favor, ingrese un carnet único.");
                notification.setPosition(Notification.Position.MIDDLE);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                notification.setDuration(1000);
                notification.open();
                return;
            }
    
            estudiantesController.save(currentEstudiante);
            Notification notification = new Notification("Estudiante guardado correctamente.");
            notification.setPosition(Notification.Position.MIDDLE);
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            notification.setDuration(1000);
            notification.open();
            resetFields();
            refreshGrid();
        }
    }    
    

    // Valida los campos del formulario
    private boolean validateInputs() {
        if (nombresEstudianteField.isEmpty() || apellidosEstudianteField.isEmpty() ||
                estadoEstudianteField.isEmpty() || nivelAcademicoField.isEmpty() || fechaNacimientoField.isEmpty()) {
            Notification notification = new Notification("Por favor, complete todos los campos obligatorios.");
            notification.setPosition(Notification.Position.MIDDLE);  // Posiciona la notificación en el centro de la pantalla
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);  // Le da el estilo de error (fondo rojo)
            notification.setDuration(1000);  // La notificación se cierra después de 3 segundos
            notification.open();

            return false;
        }
        return true;
    }

    // Establece los valores del estudiante desde los campos del formulario
    private void setEstudianteData() {
        currentEstudiante.setNombresEstudiante(nombresEstudianteField.getValue());
        currentEstudiante.setApellidosEstudiante(apellidosEstudianteField.getValue());
        currentEstudiante.setCarnet(carnetField.getValue());
        currentEstudiante.setEstadoEstudiante(estadoEstudianteField.getValue());
        currentEstudiante.setFechaNacimiento(fechaNacimientoField.getValue());
        currentEstudiante.setNivelAcademico(nivelAcademicoField.getValue());
        currentEstudiante.setNombrePadre(nombrePadreField.getValue());
        currentEstudiante.setNombreMadre(nombreMadreField.getValue());
    }

    // Resetea los campos del formulario
    private void resetFields() {
        nombresEstudianteField.clear();
        apellidosEstudianteField.clear();
        carnetField.clear();
        estadoEstudianteField.clear();
        fechaNacimientoField.clear();
        nivelAcademicoField.clear();
        nombrePadreField.clear();
        nombreMadreField.clear();
        currentEstudiante = null;
    }

    // Actualiza la grilla con los datos más recientes
    private void refreshGrid() {
        grid.setItems(estudiantesController.findAll());
    }

    // Método para editar un estudiante (abre un cuadro de diálogo)
    private void editEstudiante(Estudiantes estudiante) {
        currentEstudiante = estudiante;
    
        Dialog editDialog = new Dialog();
        editDialog.setHeaderTitle("Editar Estudiante");
    
        // Campos del formulario
        TextField nombresEstudianteDialog = new TextField("Nombres Estudiante");
        nombresEstudianteDialog.setValue(estudiante.getNombresEstudiante() != null ? estudiante.getNombresEstudiante() : "");
    
        TextField apellidosEstudianteDialog = new TextField("Apellidos Estudiante");
        apellidosEstudianteDialog.setValue(estudiante.getApellidosEstudiante() != null ? estudiante.getApellidosEstudiante() : "");
    
        TextField estadoEstudianteDialog = new TextField("Estado Estudiante");
        estadoEstudianteDialog.setValue(estudiante.getEstadoEstudiante() != null ? estudiante.getEstadoEstudiante() : "");
    
        DatePicker fechaNacimientoDialog = new DatePicker("Fecha de Nacimiento");
        fechaNacimientoDialog.setValue(estudiante.getFechaNacimiento() != null ? estudiante.getFechaNacimiento() : null);
    
        TextField nivelAcademicoDialog = new TextField("Nivel Académico");
        nivelAcademicoDialog.setValue(estudiante.getNivelAcademico() != null ? estudiante.getNivelAcademico() : "");
    
        TextField nombrePadreDialog = new TextField("Nombre del Padre");
        nombrePadreDialog.setValue(estudiante.getNombrePadre() != null ? estudiante.getNombrePadre() : "");
    
        TextField nombreMadreDialog = new TextField("Nombre de la Madre");
        nombreMadreDialog.setValue(estudiante.getNombreMadre() != null ? estudiante.getNombreMadre() : "");
    
        TextField carnetEstudianteDialog = new TextField("Carnet");
        carnetEstudianteDialog.setValue(estudiante.getCarnet());
        carnetEstudianteDialog.setEnabled(false);
    
        // Previsualización de la imagen
        Image imagePreview = new Image();
        if (estudiante.getFoto() != null) {
            String base64Image = "data:image/jpeg;base64," + java.util.Base64.getEncoder().encodeToString(estudiante.getFoto());
            imagePreview.setSrc(base64Image);
        } else {
            imagePreview.setSrc("default-photo.png");
        }
        imagePreview.setWidth("150px");
        imagePreview.setHeight("150px");
    
        // Subir foto
        MemoryBuffer editBuffer = new MemoryBuffer();
        Upload upload = new Upload(editBuffer);
        upload.setAcceptedFileTypes("image/jpeg", "image/png");
        upload.setMaxFiles(1);
    
        // Listener para cargar la imagen
        upload.addSucceededListener(event -> {
            InputStream inputStream = editBuffer.getInputStream();
            if (inputStream != null) {
                try {
                    byte[] imageBytes = inputStream.readAllBytes();
                    String base64Image = "data:image/jpeg;base64," + java.util.Base64.getEncoder().encodeToString(imageBytes);
                    imagePreview.setSrc(base64Image);
                    estudiante.setFoto(imageBytes); // Guardar la imagen en el estudiante
                    Notification.show("Imagen cargada en memoria");
                } catch (IOException e) {
                    Notification.show("Error al cargar la imagen: " + e.getMessage());
                }
            }
        });
    
        FormLayout formLayout = new FormLayout(
                nombresEstudianteDialog, apellidosEstudianteDialog, estadoEstudianteDialog,
                fechaNacimientoDialog, nivelAcademicoDialog, nombrePadreDialog, nombreMadreDialog,
                carnetEstudianteDialog, imagePreview, upload
        );
        editDialog.add(formLayout);
    
        // Botón de guardar
        Button saveButton = new Button("Guardar", event -> {
            // Validar si la fecha de nacimiento es del futuro
            if (fechaNacimientoDialog.getValue() != null && fechaNacimientoDialog.getValue().isAfter(java.time.LocalDate.now())) {
                Notification.show("La fecha de nacimiento no puede ser del futuro.");
                return;
            }
    
            // Actualizar los valores del estudiante
            estudiante.setNombresEstudiante(nombresEstudianteDialog.getValue());
            estudiante.setApellidosEstudiante(apellidosEstudianteDialog.getValue());
            estudiante.setEstadoEstudiante(estadoEstudianteDialog.getValue());
            estudiante.setFechaNacimiento(fechaNacimientoDialog.getValue());
            estudiante.setNivelAcademico(nivelAcademicoDialog.getValue());
            estudiante.setNombrePadre(nombrePadreDialog.getValue());
            estudiante.setNombreMadre(nombreMadreDialog.getValue());
    
            estudiantesController.save(estudiante); // Guardar en la base de datos
            Notification.show("Estudiante actualizado correctamente");
            editDialog.close();
            refreshGrid();
        });
    
        Button cancelButton = new Button("Cancelar", event -> editDialog.close());
    
        editDialog.getFooter().add(saveButton, cancelButton);
        editDialog.open();
    }
    

    // Método para confirmar y eliminar un estudiante
    private void confirmDeleteEstudiante(Estudiantes estudiante) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Eliminar Estudiante");

        // Verificar si el estudiante tiene relaciones en otras tablas
        boolean tieneRelaciones = estudiantesController.tieneRelaciones(estudiante.getIdEstudiante());

        if (tieneRelaciones) {
            // Si tiene relaciones, mostrar mensaje y no permitir eliminar
            dialog.add(new Text("El estudiante con carnet " + estudiante.getCarnet() + " tiene relaciones con otras tablas y no se puede eliminar."));
            Button cancelButton = new Button("Cerrar", event -> dialog.close());
            dialog.getFooter().add(cancelButton);
        } else {
            // Si no tiene relaciones, permitir la confirmación para eliminar
            dialog.add(new Text("¿Seguro que deseas eliminar al estudiante con carnet " 
                                + estudiante.getCarnet() + "?"));

            Button confirmButton = new Button("Eliminar", event -> {
                try {
                    estudiantesController.delete(estudiante); // Eliminar de la base de datos
                    grid.setItems(estudiantesController.findAll()); // Actualizar la vista
                    dialog.close();
                    Notification.show("Estudiante eliminado correctamente");
                } catch (Exception e) {
                    Notification.show("Error al eliminar el estudiante: " + e.getMessage());
                }
            });
            confirmButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

            Button cancelButton = new Button("Cancelar", event -> dialog.close());
            dialog.getFooter().add(new HorizontalLayout(confirmButton, cancelButton));
        }

        dialog.open();
    }
   
    
}
