package com.example.application.views.Estudiante;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.application.controlador.EstudiantesController;
import com.example.application.modelo.Estudiantes;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

// Define el título de la página que se mostrará en el navegador
@PageTitle("Estudiantes")
@Route(value = "estudiantes", layout = MainLayout.class)
public class EstudiantesView extends Composite<VerticalLayout>  implements BeforeEnterObserver {

@Override
public void beforeEnter(BeforeEnterEvent event) {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.isAuthenticated()) {
        var roles = auth.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toSet());

        System.out.println("Roles actuales: " + roles);

        if (roles.contains("ROLE_ADMIN")) {
            event.forwardTo("estudiantes");
        } else if (roles.contains("ROLE_PROFESOR")) {
            event.forwardTo("estudianteprofesor");
        } else if (roles.contains("ROLE_ESTUDIANTE")) {
            event.forwardTo("consulta-clases");
        } else {
            event.rerouteTo("access-denied");
        }
    } else {
        event.rerouteTo("login");
    }
}

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
    private final Button deleteSelectedButton = new Button("Eliminar seleccionados");

    private final Grid<Estudiantes> grid = new Grid<>(Estudiantes.class, false);

    // Para cargar la foto del estudiante
    private final MemoryBuffer buffer = new MemoryBuffer();
    private final Upload fotoUpload = new Upload(buffer);

    // Constructor con inyección de dependencias
    public EstudiantesView(EstudiantesController estudiantesController) {
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
        deleteSelectedButton.addThemeVariants(ButtonVariant.LUMO_ERROR);    
        deleteSelectedButton.setEnabled(false);


        HorizontalLayout buttonLayout = new HorizontalLayout(searchButton, clearSearchButton, deleteSelectedButton);

        // Añadir todo al layout principal
        layout.add(searchFormLayout, buttonLayout);

        // Eventos
        searchButton.addClickListener(e -> searchEstudiantes());
        clearSearchButton.addClickListener(e -> clearSearchFilters());
        // Acción del botón para eliminar
        deleteSelectedButton.addClickListener(e -> deleteSelectedEstudiantes());
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

    private void deleteSelectedEstudiantes() {
        Set<Estudiantes> seleccionados = grid.getSelectedItems();

        if (seleccionados == null || seleccionados.isEmpty()) {
            Notification.show("No hay estudiantes seleccionados.", 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        List<Estudiantes> noEliminables = seleccionados.stream()
            .filter(est -> estudiantesController.tieneRelaciones(est.getIdEstudiante()))
            .toList();

        if (!noEliminables.isEmpty()) {
            Dialog aviso = new Dialog();
            aviso.setHeaderTitle("Advertencia");
            aviso.add(new Text("Algunos estudiantes tienen relaciones en otras tablas y no pueden eliminarse."));
            Button cerrar = new Button("Cerrar", evt -> aviso.close());
            aviso.getFooter().add(cerrar);
            aviso.open();
        } else {
            Dialog confirmDialog = new Dialog();
            confirmDialog.setHeaderTitle("Confirmar eliminación");
            confirmDialog.add(new Text("¿Estás seguro de eliminar " + seleccionados.size() + " estudiante(s)?"));

            Button confirmar = new Button("Eliminar", evt -> {
                seleccionados.forEach(est -> estudiantesController.delete(est));
                grid.setItems(estudiantesController.findAll());
                confirmDialog.close();
                Notification.show("Estudiantes eliminados correctamente", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            });
            confirmar.addThemeVariants(ButtonVariant.LUMO_ERROR);

            Button cancelar = new Button("Cancelar", evt -> confirmDialog.close());
            confirmDialog.getFooter().add(new HorizontalLayout(confirmar, cancelar));

            confirmDialog.open();
        }
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

        Button importButton = new Button("Importar"); // Botón para importar datos
        importButton.addClickListener(event -> openImportDialogEstudiantes()); // Abre un diálogo para la importación de estudiantes

        return new HorizontalLayout(saveButton, cancelButton, importButton);
    }

    // Abre un diálogo para importar estudiantes desde un archivo CSV o XLSX
    // Método para abrir el cuadro de diálogo de importación de estudiantes
    private void openImportDialogEstudiantes() {
        Dialog importDialog = new Dialog();
        importDialog.setHeaderTitle("Importar Estudiantes"); // Título del diálogo

        Upload upload = new Upload();
        MemoryBuffer importBuffer = new MemoryBuffer();
        upload.setReceiver(importBuffer);

        Span status = new Span("No se ha cargado ningún archivo."); // Estado de carga

        // Escucha cuando el archivo se carga exitosamente
        final String[] fileNameHolder = new String[1]; // Para almacenar el nombre del archivo cargado
        upload.addSucceededListener(event -> {
            fileNameHolder[0] = event.getFileName(); // Captura el nombre del archivo
            status.setText("Archivo cargado temporalmente: " + event.getFileName());
        });

        // Botón para aceptar la importación
        Button acceptButton = new Button("Aceptar", e -> {
            try {
                InputStream fileData = importBuffer.getInputStream();

                // Validar si se seleccionó un archivo
                if (fileNameHolder[0] == null || fileNameHolder[0].isEmpty()) {
                    Notification.show("Debe cargar un archivo antes de continuar.", 5000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }

                // Procesar el archivo en segundo plano
                ExecutorService executorService = Executors.newSingleThreadExecutor();
                executorService.submit(new FileProcessingTaskEstudiantes(fileData, fileNameHolder[0], estudiantesController, UI.getCurrent(), grid));

                Notification.show("La importación está en proceso...");
                importDialog.close();
            } catch (Exception ex) {
                Notification.show("Error al iniciar la importación: " + ex.getMessage(), 5000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        acceptButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // Botón para cancelar la importación
        Button cancelButton = new Button("Cancelar", e -> importDialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout(acceptButton, cancelButton);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        VerticalLayout dialogLayout = new VerticalLayout(upload, status, buttonLayout);
        dialogLayout.setSpacing(true);
        dialogLayout.setPadding(true);
        importDialog.add(dialogLayout);

        importDialog.open();
    }


    private List<Estudiantes> estudiantesList; // Lista de estudiantes para el data provider
    private ListDataProvider<Estudiantes> dataProvider;
    
    // Crea la grilla para visualizar a los estudiantes
    private void createGrid() {
        grid.setSelectionMode(Grid.SelectionMode.MULTI);

         // Inicializar la lista y el data provider
        estudiantesList = new ArrayList<>(estudiantesController.findAll());
        dataProvider = new ListDataProvider<>(estudiantesList);
        grid.setDataProvider(dataProvider);

        // Columnas normales
        grid.addColumn(Estudiantes::getCarnet).setHeader("Carnet").setSortable(true);
        grid.addColumn(Estudiantes::getNombresEstudiante).setHeader("Nombres");
        grid.addColumn(Estudiantes::getApellidosEstudiante).setHeader("Apellidos");
        grid.addColumn(Estudiantes::getEstadoEstudiante).setHeader("Estado");
        grid.addColumn(Estudiantes::getFechaNacimiento).setHeader("Fecha de Nacimiento");
        grid.addColumn(Estudiantes::getNivelAcademico).setHeader("Nivel Académico");
        grid.addColumn(Estudiantes::getNombrePadre).setHeader("Nombre Padre");
        grid.addColumn(Estudiantes::getNombreMadre).setHeader("Nombre Madre");

        // Columna de ver foto
        grid.addColumn(new ComponentRenderer<>(estudiante -> {
            Button verFotoBtn = new Button("Ver Foto");
            verFotoBtn.setEnabled(estudiante.getFoto() != null);

            verFotoBtn.addClickListener(e -> {
                Dialog dialog = new Dialog();
                dialog.setWidth("400px");
                dialog.setHeight("400px");

                if (estudiante.getFoto() != null) {
                    String base64Image = "data:image/jpeg;base64," +
                            java.util.Base64.getEncoder().encodeToString(estudiante.getFoto());
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

        // Columna de editar
        grid.addColumn(new ComponentRenderer<>(estudiante -> {
            Button editButton = new Button("Editar");
            editButton.addClickListener(e -> editEstudiante(estudiante));
            return editButton;
        })).setHeader("Editar");

        // Columna de eliminar individual
        grid.addColumn(new ComponentRenderer<>(estudiante -> {
            Button deleteButton = new Button("Eliminar");
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            deleteButton.addClickListener(e -> confirmDeleteEstudiante(estudiante));
            return deleteButton;
        })).setHeader("Eliminar");

        estudiantesList = new ArrayList<>(estudiantesController.findAll());
        grid.setItems(estudiantesList);

        grid.addSelectionListener(event -> {
            deleteSelectedButton.setEnabled(!event.getAllSelectedItems().isEmpty());
        });
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
            updateCarnetComboBoxItems(); // Actualizar el ComboBox de búsqueda de carnets
    
            // Mostrar notificación de éxito
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
        grid.getDataProvider().refreshAll(); // Refresca la grilla para mostrar los cambios
        grid.deselectAll(); // Deselecciona todos los elementos después de actualizar
        deleteSelectedButton.setEnabled(false); // Deshabilita el botón de eliminar después de actualizar
        Notification.show("Grilla actualizada con los últimos datos.", 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
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
            // Actualizar la grilla
            updateCarnetComboBoxItems();
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


    // Método para importar un archivo CSV o XLSX
    // Clase que procesa archivos de importación de estudiantes en segundo plano (.xlsx y .csv)
    private static class FileProcessingTaskEstudiantes implements Runnable {
        private final InputStream fileData;
        private final String fileName;
        private final EstudiantesController controller;
        private final UI ui;
        private final Grid<Estudiantes> grid;

        public FileProcessingTaskEstudiantes(InputStream fileData, String fileName, EstudiantesController controller, UI ui, Grid<Estudiantes> grid) {
            this.fileData = fileData;
            this.fileName = fileName.toLowerCase(); // Nombre del archivo para detectar extensión
            this.controller = controller;
            this.ui = ui;
            this.grid = grid;
        }

        @Override
        public void run() {
            List<Estudiantes> estudiantes = new ArrayList<>();
            Set<String> carnetsProcesados = new HashSet<>();
            List<String> errores = new ArrayList<>();
            int batchSize = 10;

            try {
                if (fileName.endsWith(".xlsx")) {
                    procesarXLSX(estudiantes, carnetsProcesados, errores, batchSize);
                } else if (fileName.endsWith(".csv")) {
                    procesarCSV(estudiantes, carnetsProcesados, errores, batchSize);
                } else {
                    throw new IllegalArgumentException("Formato de archivo no soportado.");
                }

                if (!estudiantes.isEmpty()) {
                    controller.insertarEstudiantesEnBatch(estudiantes);
                }

                if (errores.isEmpty()) {
                    showCompletionNotification();
                } else {
                    showErrorDialog(errores);
                }

                ui.access(() -> grid.setItems(controller.findAll()));

            } catch (IOException | IllegalArgumentException e) {
                showErrorDialog(Collections.singletonList("Error general en la importación: " + e.getMessage()));
            }
        }

        private void procesarXLSX(List<Estudiantes> estudiantes, Set<String> carnetsProcesados, List<String> errores, int batchSize) throws IOException {
            try (XSSFWorkbook workbook = new XSSFWorkbook(fileData)) {
                XSSFSheet sheet = workbook.getSheetAt(0);

                for (Row row : sheet) {
                    if (row.getRowNum() == 0) continue; // Ignorar cabecera

                    try {
                        String carnet = getCellValueAsString(row.getCell(0));
                        String nombres = getCellValueAsString(row.getCell(1));
                        String apellidos = getCellValueAsString(row.getCell(2));
                        LocalDate fechaNacimiento = getCellValueAsDate(row.getCell(3));
                        String nivelAcademico = getCellValueAsString(row.getCell(4));
                        String nombrePadre = getCellValueAsString(row.getCell(5));
                        String nombreMadre = getCellValueAsString(row.getCell(6));
                        String estadoEstudiante = getCellValueAsString(row.getCell(7));

                        if (carnet.isEmpty() || nombres.isEmpty() || apellidos.isEmpty()) {
                            List<String> camposVacios = new ArrayList<>();
                            if (carnet.isEmpty()) camposVacios.add("Carnet");
                            if (nombres.isEmpty()) camposVacios.add("Nombres");
                            if (apellidos.isEmpty()) camposVacios.add("Apellidos");
                            errores.add("Fila " + (row.getRowNum() + 1) + ": Los siguientes campos están vacíos: " + String.join(", ", camposVacios) + ".");
                            continue;
                        }

                        if (carnetsProcesados.contains(carnet)) {
                            errores.add("Fila " + (row.getRowNum() + 1) + ": El carnet '" + carnet + "' ya está duplicado en el archivo.");
                            continue;
                        }

                        if (controller.existsByCarnet(carnet)) {
                            errores.add("Fila " + (row.getRowNum() + 1) + ": El carnet '" + carnet + "' ya existe en la base de datos.");
                            continue;
                        }

                        if (fechaNacimiento != null && fechaNacimiento.isAfter(LocalDate.now())) {
                            errores.add("Fila " + (row.getRowNum() + 1) + ": La fecha de nacimiento '" + fechaNacimiento + "' no puede ser futura.");
                            continue;
                        }


                        carnetsProcesados.add(carnet);

                        Estudiantes estudiante = new Estudiantes();
                        estudiante.setCarnet(carnet);
                        estudiante.setNombresEstudiante(nombres);
                        estudiante.setApellidosEstudiante(apellidos);
                        estudiante.setFechaNacimiento(fechaNacimiento);
                        estudiante.setNivelAcademico(nivelAcademico);
                        estudiante.setNombrePadre(nombrePadre);
                        estudiante.setNombreMadre(nombreMadre);
                        estudiante.setEstadoEstudiante(estadoEstudiante);

                        estudiantes.add(estudiante);

                        if (estudiantes.size() % batchSize == 0) {
                            List<Estudiantes> batch = new ArrayList<>(estudiantes);
                            controller.insertarEstudiantesEnBatch(batch);
                            estudiantes.clear();

                            ui.access(() -> {
                                grid.setItems(controller.findAll());
                                Notification notification = new Notification("Se ingresaron " + batch.size() + " estudiantes correctamente.", 3000, Notification.Position.TOP_END);
                                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                                notification.open();
                            });
                        }

                    } catch (NullPointerException | IllegalStateException | IllegalArgumentException e) {
                        errores.add("Fila " + (row.getRowNum() + 1) + ": Error al procesar los datos: " + e.getMessage());
                    }
                }
            }
        }

        private void procesarCSV(List<Estudiantes> estudiantes, Set<String> carnetsProcesados, List<String> errores, int batchSize) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(fileData))) {
                String line;
                int lineNumber = 0;

                while ((line = reader.readLine()) != null) {
                    lineNumber++;

                    if (lineNumber == 1) continue; // Ignorar cabecera

                    String[] columns = line.split(",");

                    if (columns.length < 8) {
                        errores.add("Línea " + lineNumber + ": Faltan columnas (se esperaban 8 y se encontraron " + columns.length + ").");
                        continue;
                    }

                    try {
                        String carnet = columns[0].trim();
                        String nombres = columns[1].trim();
                        String apellidos = columns[2].trim();
                        LocalDate fechaNacimiento = parseDate(columns[3].trim());
                        String nivelAcademico = columns[4].trim();
                        String nombrePadre = columns[5].trim();
                        String nombreMadre = columns[6].trim();
                        String estadoEstudiante = columns[7].trim();

                        if (carnet.isEmpty() || nombres.isEmpty() || apellidos.isEmpty()) {
                            List<String> camposVacios = new ArrayList<>();
                            if (carnet.isEmpty()) camposVacios.add("Carnet");
                            if (nombres.isEmpty()) camposVacios.add("Nombres");
                            if (apellidos.isEmpty()) camposVacios.add("Apellidos");
                            errores.add("Línea " + lineNumber + ": Los siguientes campos están vacíos: " + String.join(", ", camposVacios) + ".");

                            continue;
                        }

                        if (carnetsProcesados.contains(carnet)) {
                            errores.add("Línea " + lineNumber + ": El carnet '" + carnet + "' ya está duplicado en el archivo.");
                            continue;
                        }

                        if (controller.existsByCarnet(carnet)) {
                            errores.add("Línea " + lineNumber + ": El carnet '" + carnet + "' ya existe en la base de datos.");
                            continue;
                        }

                        if (fechaNacimiento != null && fechaNacimiento.isAfter(LocalDate.now())) {
                            errores.add("Línea " + lineNumber + ": La fecha de nacimiento '" + fechaNacimiento + "' no puede ser futura.");
                            continue;
                        }

                        carnetsProcesados.add(carnet);

                        Estudiantes estudiante = new Estudiantes();
                        estudiante.setCarnet(carnet);
                        estudiante.setNombresEstudiante(nombres);
                        estudiante.setApellidosEstudiante(apellidos);
                        estudiante.setFechaNacimiento(fechaNacimiento);
                        estudiante.setNivelAcademico(nivelAcademico);
                        estudiante.setNombrePadre(nombrePadre);
                        estudiante.setNombreMadre(nombreMadre);
                        estudiante.setEstadoEstudiante(estadoEstudiante);

                        estudiantes.add(estudiante);

                        if (estudiantes.size() % batchSize == 0) {
                            List<Estudiantes> batch = new ArrayList<>(estudiantes);
                            controller.insertarEstudiantesEnBatch(batch);
                            estudiantes.clear();

                            ui.access(() -> {
                                grid.setItems(controller.findAll());
                                Notification notification = new Notification("Se ingresaron " + batch.size() + " estudiantes correctamente.", 3000, Notification.Position.TOP_END);
                                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                                notification.open();
                            });
                        }

                        if (!estudiantes.isEmpty()) {
                            controller.insertarEstudiantesEnBatch(estudiantes);
                        }


                    } catch (NumberFormatException | ArrayIndexOutOfBoundsException | NullPointerException e) {
                        errores.add("Línea " + lineNumber + ": Error al procesar los datos: " + e.getMessage());
                    }
                }
            } catch (IOException e) {
                errores.add("Error leyendo archivo CSV: " + e.getMessage());
            }
        }

        private void showErrorDialog(List<String> errores) {
            ui.access(() -> {
                Dialog errorDialog = new Dialog();
                errorDialog.setWidth("800px");
                errorDialog.setHeight("500px");

                // Impide que se cierre accidentalmente
                errorDialog.setCloseOnEsc(false);
                errorDialog.setCloseOnOutsideClick(false);

                H3 title = new H3("Errores de Importación");
                Div content = new Div();
                content.getStyle().set("overflow-y", "auto").set("max-height", "400px").set("white-space", "pre-wrap");

                StringBuilder errorList = new StringBuilder("Se encontraron errores durante la importación:\n\n");
                errores.forEach(error -> errorList.append(error).append("\n"));

                content.setText(errorList.toString());

                Button closeButton = new Button("Cerrar", event -> errorDialog.close());
                closeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

                HorizontalLayout footer = new HorizontalLayout(closeButton);
                footer.setJustifyContentMode(JustifyContentMode.END);
                footer.setWidthFull();

                errorDialog.add(title, content, footer);
                errorDialog.open();
            });
        }

        private void showCompletionNotification() {
            ui.access(() -> {
                Notification notification = new Notification();
                notification.setDuration(5000);
                notification.setPosition(Notification.Position.TOP_END);
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                Span message = new Span("Importación completada exitosamente.");
                notification.add(message);
                notification.open();
            });
        }

        private String getCellValueAsString(Cell cell) {
            if (cell == null) {
                return "";
            }
            return switch (cell.getCellType()) {
                case STRING -> cell.getStringCellValue();
                case NUMERIC -> {
                    if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                        yield new SimpleDateFormat("yyyy-MM-dd").format(cell.getDateCellValue());
                    } else {
                        yield String.format("%.0f", cell.getNumericCellValue());
                    }
                }
                default -> "";
            };
        }

        private LocalDate getCellValueAsDate(Cell cell) {
            if (cell == null) return null;

            if (cell.getCellType() == CellType.NUMERIC && org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                return cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            } else if (cell.getCellType() == CellType.STRING) {
                return parseDate(cell.getStringCellValue());
            }
            return null;
        }

        private LocalDate parseDate(String dateStr) {
            String[] dateFormats = { "dd-MM-yy", "dd/MM/yyyy", "MM/dd/yyyy", "yyyy-MM-dd" };
            for (String format : dateFormats) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat(format);
                    sdf.setLenient(false);
                    Date parsedDate = sdf.parse(dateStr);
                    return parsedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                } catch (ParseException ignored) { }
            }
            System.err.println("Formato de fecha no reconocido: " + dateStr);
            return null;
        }
    }



   
}
