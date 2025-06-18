package com.example.application.views.Estudiante;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.example.application.controlador.EstudiantesController;
import com.example.application.modelo.Estudiantes;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
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
@Route(value = "estudianteprofesor", layout = MainLayout.class)
public class EstudiantesViewProfesor extends Composite<VerticalLayout> {

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
    private final ComboBox<String> searchCarnetComboBox = new ComboBox<>("Buscar por Carnet");
    private final DatePicker searchFechaNacimientoPicker = new DatePicker("Fecha de Nacimiento");
    private final TextField searchNombresField = new TextField("Nombres");
    private final TextField searchApellidosField = new TextField("Apellidos");
    private final Button searchButton = new Button("Buscar");
    private final Button clearSearchButton = new Button("Limpiar");


    private final Grid<Estudiantes> grid = new Grid<>(Estudiantes.class, false);

    // Para cargar la foto del estudiante
    private final MemoryBuffer buffer = new MemoryBuffer();
    private final Upload fotoUpload = new Upload(buffer);

    // Constructor con inyección de dependencias
    public EstudiantesViewProfesor(EstudiantesController estudiantesController) {
        this.estudiantesController = estudiantesController;

        // Título de la vista
        H3 title = new H3("Gestión de Estudiantes");

        // Crear el formulario de entrada y botones
        FormLayout formLayout = createFormLayout();
        HorizontalLayout buttonLayout = createButtonLayout();

        // Crear la grilla
        createGrid();

        // Layout principal sin los campos de búsqueda viejos
        VerticalLayout layout = new VerticalLayout(title, formLayout, buttonLayout);
        layout.setSizeFull();
        layout.setSpacing(true);

        // Añadir los filtros y botones de búsqueda al layout principal
        setupSearchLayout(layout);

        // Finalmente, añadir la grilla
        layout.add(grid);

        getContent().add(layout);
    }

    // Configura el layout de búsqueda

    private void setupSearchLayout(VerticalLayout layout) {
        // Cargar carnets para ComboBox
        List<String> carnets = estudiantesController.findAll().stream()
            .map(Estudiantes::getCarnet)
            .distinct()
            .sorted()
            .toList();
        searchCarnetComboBox.setItems(carnets);
        searchCarnetComboBox.setClearButtonVisible(true);
        searchCarnetComboBox.setPlaceholder("Seleccione carnet...");

        searchFechaNacimientoPicker.setClearButtonVisible(true);

        // Layout para los filtros
        FormLayout searchFormLayout = new FormLayout(
            searchCarnetComboBox,
            searchFechaNacimientoPicker,
            searchNombresField,
            searchApellidosField
        );

        // Botones buscar y limpiar
        searchButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        clearSearchButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        HorizontalLayout buttonLayout = new HorizontalLayout(searchButton, clearSearchButton);

        // Añadir todo al layout principal
        layout.add(searchFormLayout, buttonLayout);

        // Eventos
        searchButton.addClickListener(e -> searchEstudiantes());
        clearSearchButton.addClickListener(e -> clearSearchFilters());
    }

    // Método para actualizar el ComboBox con carnets
    private void updateCarnetComboBoxItems() {
        List<String> carnets = estudiantesController.findAll().stream()
            .map(Estudiantes::getCarnet)
            .distinct()
            .sorted()
            .toList();
        searchCarnetComboBox.setItems(carnets);
    }

    // Busca estudiantes según los filtros aplicados

    private void searchEstudiantes() {
        List<Estudiantes> estudiantes = estudiantesController.findAll();

        String carnet = searchCarnetComboBox.getValue();
        java.time.LocalDate fechaNacimiento = searchFechaNacimientoPicker.getValue();
        String nombres = searchNombresField.getValue() != null ? searchNombresField.getValue().trim().toLowerCase() : "";
        String apellidos = searchApellidosField.getValue() != null ? searchApellidosField.getValue().trim().toLowerCase() : "";

        if (carnet != null && !carnet.isEmpty()) {
            estudiantes = estudiantes.stream()
                .filter(e -> e.getCarnet() != null && e.getCarnet().equalsIgnoreCase(carnet))
                .toList();
        }

        if (fechaNacimiento != null) {
            estudiantes = estudiantes.stream()
                .filter(e -> fechaNacimiento.equals(e.getFechaNacimiento()))
                .toList();
        }

        if (!nombres.isEmpty()) {
            estudiantes = estudiantes.stream()
                .filter(e -> e.getNombresEstudiante() != null &&
                            e.getNombresEstudiante().toLowerCase().contains(nombres))
                .toList();
        }

        if (!apellidos.isEmpty()) {
            estudiantes = estudiantes.stream()
                .filter(e -> e.getApellidosEstudiante() != null &&
                            e.getApellidosEstudiante().toLowerCase().contains(apellidos))
                .toList();
        }

        if (estudiantes.isEmpty()) {
            Notification.show("No se encontraron estudiantes con los criterios ingresados.", 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } else {
            Notification.show("Estudiantes encontrados: " + estudiantes.size(), 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        }

        grid.setItems(estudiantes);
    }

    private void clearSearchFilters() {
        searchCarnetComboBox.clear();
        searchFechaNacimientoPicker.clear();
        searchNombresField.clear();
        searchApellidosField.clear();
        refreshGrid();
        Notification.show("Filtros de búsqueda reiniciados.", 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
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
            Button verFotoBtn = new Button("Ver Foto");

            // Deshabilitar el botón si no hay foto
            verFotoBtn.setEnabled(estudiante.getFoto() != null);

            verFotoBtn.addClickListener(e -> {
                Dialog dialog = new Dialog();
                dialog.setWidth("400px");
                dialog.setHeight("400px");

                if (estudiante.getFoto() != null) {
                    String base64Image = "data:image/jpeg;base64," + java.util.Base64.getEncoder().encodeToString(estudiante.getFoto());
                    Image image = new Image(base64Image, "Foto del estudiante");
                    image.setWidth("100%");
                    image.setHeight("100%");
                    dialog.add(image);
                } else {
                    dialog.add(new Text("No hay foto disponible."));
                }

                dialog.open();
            });

            return verFotoBtn;
        })).setHeader("Foto");

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
            updateCarnetComboBoxItems(); // Actualiza el ComboBox de búsqueda con el nuevo carnet
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
    
}
