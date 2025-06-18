package com.example.application.views.Clase;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import com.example.application.controlador.ClaseController;
import com.example.application.controlador.ParticipacionController;
import com.example.application.modelo.Clase;
import com.example.application.modelo.ClaseRepository;
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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Clases")
@Route(value = "clasesprofesor", layout = MainLayout.class)
public class ClasesViewProfesor extends Composite<VerticalLayout> {

    private final ClaseController claseController; // Controlador para gestionar las clases
    private Clase currentClase = null; // Variable para almacenar la clase actual en edición
    
    // Campos del formulario para ingresar una nueva clase
    private final TextField codigoClaseField = new TextField("Código Clase");
    private final TextField nombreClaseField = new TextField("Nombre Clase");
    private final TextField descripcionField = new TextField("Descripción (Opcional)");
    private final DatePicker fechaInicioField = new DatePicker("Fecha Inicio");
    private final DatePicker fechaFinField = new DatePicker("Fecha Fin");
    private final TextField profesorField = new TextField("Profesor");
    private final TextField maxEstudiantesField = new TextField("Máximo Estudiantes");

    // Componentes de búsqueda
    private final ComboBox<String> searchCodigoClaseComboBox = new ComboBox<>("Buscar por Código de Clase");
    private final NumberField searchMaxEstudiantesField = new NumberField("Máx. Estudiantes");
    private final DatePicker searchFechaInicioPicker = new DatePicker("Fecha de Inicio");
    private final DatePicker searchFechaFinPicker = new DatePicker("Fecha de Fin");

    private final Button searchButton = new Button("Buscar");
    private final Button clearSearchButton = new Button("Limpiar");

    private final Grid<Clase> grid = new Grid<>(Clase.class, false); // Grid para mostrar las clases

    // Constructor de la vista mejorado
    public ClasesViewProfesor(ClaseController claseController, ClaseRepository claseRepository, ParticipacionController participacionesController) {
        this.claseController = claseController;

        VerticalLayout layout = new VerticalLayout();
        H3 title = new H3("Gestión de Clases");

        // --- SECCIÓN FORMULARIO DE CLASES ---
        FormLayout formLayout = createFormLayout();
        HorizontalLayout buttonLayout = createButtonLayout();

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

        // Configuración de eventos de los botones
        searchButton.addClickListener(e -> searchClases());
        clearSearchButton.addClickListener(e -> clearSearch());

        // --- SECCIÓN GRID ---
        createGrid();

        // Armado del layout principal
        layout.add(
            title,
            formLayout,
            buttonLayout,
            searchFormLayout,
            searchButtonLayout,
            grid
        );

        layout.setSizeFull();
        layout.setSpacing(true);

        getContent().add(layout);

        // Cargar datos iniciales en el grid
        refreshGrid();
    }


    // Método para crear el formulario de ingreso de datos de la clase
    private FormLayout createFormLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.add(codigoClaseField, nombreClaseField, descripcionField, fechaInicioField, fechaFinField, profesorField, maxEstudiantesField);
        return formLayout;
    }

    // Método para crear el layout con los botones de guardar y cancelar
    private HorizontalLayout createButtonLayout() {
        Button saveButton = new Button("Guardar", event -> saveClase());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY); // Estilo del botón de guardar

        Button cancelButton = new Button("Cancelar", event -> resetFields()); // Botón de cancelar

        return new HorizontalLayout(saveButton, cancelButton); // Los botones se muestran en horizontal
    }

    // Método para crear el Grid con las clases y sus respectivas acciones
    private void createGrid() {
        grid.addColumn(Clase::getCodigoClase).setHeader("Código Clase").setSortable(true); // Columna para el código de clase
        grid.addColumn(Clase::getNombreClase).setHeader("Nombre Clase").setSortable(true); // Columna para el nombre de la clase
        grid.addColumn(Clase::getDescripcion).setHeader("Descripción"); // Columna para la descripción
        grid.addColumn(Clase::getFechaInicio).setHeader("Fecha Inicio"); // Columna para la fecha de inicio
        grid.addColumn(Clase::getFechaFin).setHeader("Fecha Fin"); // Columna para la fecha de fin
        grid.addColumn(Clase::getProfesor).setHeader("Profesor"); // Columna para el profesor
        grid.addColumn(Clase::getMaxEstudiantes).setHeader("Máximo Estudiantes"); // Columna para el máximo de estudiantes

        // Actualizar el grid con las clases
        refreshGrid();
    }

     // Método para guardar una nueva clase
     private void saveClase() {
        if (validateInputs()) {
            try {
                if (currentClase == null) {
                    currentClase = new Clase();  // Si currentClase es null, crear una nueva instancia
                }
    
                // Comprobar si ya existe una clase con el mismo código
                if (claseController.existsByCodigoClase(currentClase.getCodigoClase())) {
                    Notification notification = new Notification("No se puede guardar la clase: Ya existe una clase con el mismo código de clase.");
                        notification.setPosition(Notification.Position.MIDDLE);  // Posiciona la notificación en el centro de la pantalla
                        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);  // Le da el estilo de error (fondo rojo)
                        notification.setDuration(1000);  // La notificación se cierra después de 3 segundos
                        notification.open();
                    return;  // Salir si ya existe una clase con el mismo código
                }
    
                setClaseData();  // Asigna los valores a la entidad Clase
                claseController.save(currentClase);  // Guardar la clase
                Notification notification = new Notification("Clase guardada correctamente.");
                notification.setPosition(Notification.Position.MIDDLE);  // Posiciona la notificación en el centro de la pantalla
                    notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);  // Le da el estilo de éxito (fondo verde)
                    notification.setDuration(1000);  // La notificación se cierra después de 3 segundos
                    notification.open();
                resetFields();  // Limpiar los campos
                refreshGrid();  // Actualizar el grid con los nuevos datos
    
            } catch (Exception e) {
                Notification.show("Error al guardar la clase: " + e.getMessage());
            }
        }
    }
    
    // Método para asignar los valores del formulario a la entidad Clase
    private void setClaseData() {
        currentClase.setCodigoClase(codigoClaseField.getValue());
        currentClase.setNombreClase(nombreClaseField.getValue());
        currentClase.setDescripcion(descripcionField.getValue());
        currentClase.setFechaInicio(fechaInicioField.getValue());
        currentClase.setFechaFin(fechaFinField.getValue());
        currentClase.setProfesor(profesorField.getValue());
        currentClase.setMaxEstudiantes(Integer.parseInt(maxEstudiantesField.getValue()));
    }

    // Método para validar que los campos obligatorios estén completos
    private boolean validateInputs() {
        // Verifica si algún campo obligatorio está vacío
        if (codigoClaseField.isEmpty() || nombreClaseField.isEmpty() || fechaInicioField.isEmpty() || fechaFinField.isEmpty() || profesorField.isEmpty() || maxEstudiantesField.isEmpty()) {
            Notification notification = new Notification("Por favor, complete todos los campos obligatorios.");
            notification.setPosition(Notification.Position.MIDDLE);  // Posiciona la notificación en el centro de la pantalla
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);  // Le da el estilo de error (fondo rojo)
            notification.setDuration(1000);  // La notificación se cierra después de 3 segundos
            notification.open();
            return false;
        }
    
        // Verifica si el campo maxEstudiantes contiene solo números
        try {
            int maxEstudiantes = Integer.parseInt(maxEstudiantesField.getValue()); // Intenta convertir el valor a un número
    
            // Verifica si el número es negativo o cero
            if (maxEstudiantes <= 0) {
                Notification notification = new Notification("El campo 'Número máximo de estudiantes' debe ser un número mayor a 0.");
                notification.setPosition(Notification.Position.MIDDLE);  // Posiciona la notificación en el centro de la pantalla
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);  // Le da el estilo de error (fondo rojo)
                notification.setDuration(2000);  // La notificación se cierra después de 3 segundos
                notification.open();
                return false;  // Si es negativo o 0, retorna false
            }
        } catch (NumberFormatException e) {
            Notification notification = new Notification("El campo 'Número máximo de estudiantes' debe ser un número.");
            notification.setPosition(Notification.Position.MIDDLE);  // Posiciona la notificación en el centro de la pantalla
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);  // Le da el estilo de error (fondo rojo)
            notification.setDuration(2000);  // La notificación se cierra después de 3 segundos
            notification.open();
            return false;  // Si no es un número, retorna false
        }
    
        return true;  // Si todas las validaciones pasan, retorna true
    }    

    // Método para resetear los campos del formulario
    private void resetFields() {
        codigoClaseField.clear();
        nombreClaseField.clear();
        descripcionField.clear();
        fechaInicioField.clear();
        fechaFinField.clear();
        profesorField.clear();
        maxEstudiantesField.clear();
        currentClase = null; // Restablecer la variable de la clase
    }

    // Método para buscar clases según los criterios ingresados

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
