package com.example.application.views.Clase;

import com.example.application.controlador.ClaseController;
import com.example.application.controlador.ParticipacionController;
import com.example.application.modelo.Clase;
import com.example.application.modelo.ClaseRepository;
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
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Clases")
@Route(value = "clases", layout = MainLayout.class)
public class ClasesView extends Composite<VerticalLayout> {

    private final ClaseController claseController; // Controlador para gestionar las clases
    private final ParticipacionController participacionesController;
    private Clase currentClase = null; // Variable para almacenar la clase actual en edición
    
    // Campos del formulario para ingresar una nueva clase
    private final TextField codigoClaseField = new TextField("Código Clase");
    private final TextField nombreClaseField = new TextField("Nombre Clase");
    private final TextField descripcionField = new TextField("Descripción (Opcional)");
    private final DatePicker fechaInicioField = new DatePicker("Fecha Inicio");
    private final DatePicker fechaFinField = new DatePicker("Fecha Fin");
    private final TextField profesorField = new TextField("Profesor");
    private final TextField maxEstudiantesField = new TextField("Máximo Estudiantes");

    // Campos para búsqueda de clases
    private final TextField searchCodigoClaseField = new TextField("Buscar por Código de Clase");
    private final Button searchButton = new Button("Buscar");
    private final Button resetSearchButton = new Button("Reiniciar Búsqueda");

    private final Grid<Clase> grid = new Grid<>(Clase.class, false); // Grid para mostrar las clases

    // Constructor de la vista
    public ClasesView(ClaseController claseController, ClaseRepository claseRepository, ParticipacionController participacionesController) {
        this.claseController = claseController; // Inicialización del controlador
        this.participacionesController = participacionesController;
        // Crear el título de la vista
        H3 title = new H3("Gestión de Clases");

        // Crear el formulario y los botones para agregar o cancelar clases
        FormLayout formLayout = createFormLayout();
        HorizontalLayout buttonLayout = createButtonLayout();

        // Configuración del grid
        createGrid();

        // Configuración de la búsqueda y del botón de reiniciar búsqueda
        configureSearch();
        configureResetSearch();

        // Layout para los botones de búsqueda
        HorizontalLayout searchButtonsLayout = new HorizontalLayout(searchButton, resetSearchButton);

        // Configuración del layout principal con todos los elementos visuales
        VerticalLayout layout = new VerticalLayout(title, formLayout, buttonLayout, searchCodigoClaseField, searchButtonsLayout, grid);
        layout.setSizeFull(); // Se ajusta el tamaño del layout
        layout.setSpacing(true); // Se agrega espacio entre los elementos

        getContent().add(layout); // Agregar todo al contenido de la vista
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

        // Columna de acciones: Editar
        grid.addColumn(new ComponentRenderer<>(clase -> {
            Button editButton = new Button("Editar");
            editButton.addClickListener(e -> editClase(clase)); // Abre el formulario de edición
            return editButton;
        })).setHeader("Editar");

        // Columna de acciones: Eliminar
        grid.addColumn(new ComponentRenderer<>(clase -> {
            Button deleteButton = new Button("Eliminar");
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR); // Estilo de error para eliminar
            deleteButton.addClickListener(e -> confirmDeleteClase(clase)); // Confirmación de eliminación
            return deleteButton;
        })).setHeader("Eliminar");

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

    // Método para editar una clase existente
    private void editClase(Clase clase) {
        currentClase = clase;

        // Crear el diálogo de edición
        Dialog editDialog = new Dialog();
        editDialog.setHeaderTitle("Editar Clase");

        // Crear los campos del formulario de edición
        TextField codigoClaseDialog = new TextField("Código Clase");
        codigoClaseDialog.setValue(clase.getCodigoClase() != null ? clase.getCodigoClase() : "");

        TextField nombreClaseDialog = new TextField("Nombre Clase");
        nombreClaseDialog.setValue(clase.getNombreClase() != null ? clase.getNombreClase() : "");

        TextField descripcionDialog = new TextField("Descripción");
        descripcionDialog.setValue(clase.getDescripcion() != null ? clase.getDescripcion() : "");

        DatePicker fechaInicioDialog = new DatePicker("Fecha Inicio");
        fechaInicioDialog.setValue(clase.getFechaInicio() != null ? clase.getFechaInicio() : null); // Establecer la fecha

        DatePicker fechaFinDialog = new DatePicker("Fecha Fin");
        fechaFinDialog.setValue(clase.getFechaFin() != null ? clase.getFechaFin() : null); // Establecer la fecha

        TextField profesorDialog = new TextField("Profesor");
        profesorDialog.setValue(clase.getProfesor() != null ? clase.getProfesor() : "");

        TextField maxEstudiantesDialog = new TextField("Máximo Estudiantes");
        maxEstudiantesDialog.setValue(clase.getMaxEstudiantes() != 0 ? String.valueOf(clase.getMaxEstudiantes()) : "");

        // Crear el formulario del diálogo
        FormLayout formLayout = new FormLayout(
                codigoClaseDialog, nombreClaseDialog, descripcionDialog, fechaInicioDialog, fechaFinDialog, profesorDialog, maxEstudiantesDialog
        );
        editDialog.add(formLayout);

        // Botón de guardar cambios
        Button saveButton = new Button("Guardar", event -> {
            // Validar que el campo maxEstudiantes tenga un valor válido
            if (!validateMaxEstudiantes(maxEstudiantesDialog)) {
                return; // Si no es válido, no se guarda la clase
            }

            clase.setCodigoClase(codigoClaseDialog.getValue());
            clase.setNombreClase(nombreClaseDialog.getValue());
            clase.setDescripcion(descripcionDialog.getValue());
            clase.setFechaInicio(fechaInicioDialog.getValue());
            clase.setFechaFin(fechaFinDialog.getValue());
            clase.setProfesor(profesorDialog.getValue());
            clase.setMaxEstudiantes(Integer.parseInt(maxEstudiantesDialog.getValue()));

            claseController.save(clase); // Guardar cambios
            Notification.show("Clase actualizada correctamente");

            editDialog.close();
            refreshGrid(); // Actualizar el grid
        });

        // Botón de cancelar
        Button cancelButton = new Button("Cancelar", event -> editDialog.close());

        // Agregar los botones al pie del diálogo
        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        editDialog.getFooter().add(buttonLayout);

        editDialog.open(); // Mostrar el diálogo
    }

    // Método para validar que el número máximo de estudiantes sea válido (mayor a 0)
    private boolean validateMaxEstudiantes(TextField maxEstudiantesDialog) {
        try {
            int maxEstudiantes = Integer.parseInt(maxEstudiantesDialog.getValue());

            if (maxEstudiantes <= 0) {
                Notification notification = new Notification("El campo 'Número máximo de estudiantes' debe ser un número mayor a 0.");
                notification.setPosition(Notification.Position.MIDDLE);  // Posiciona la notificación en el centro de la pantalla
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);  // Le da el estilo de error (fondo rojo)
                notification.setDuration(2000);  // La notificación se cierra después de 2 segundos
                notification.open();
                return false;  // Si el valor es menor o igual a 0, se cancela la operación
            }
        } catch (NumberFormatException e) {
            Notification notification = new Notification("El campo 'Número máximo de estudiantes' debe ser un número válido.");
            notification.setPosition(Notification.Position.MIDDLE);  // Posiciona la notificación en el centro de la pantalla
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);  // Le da el estilo de error (fondo rojo)
            notification.setDuration(2000);  // La notificación se cierra después de 2 segundos
            notification.open();
            return false;  // Si no es un número, se cancela la operación
        }
        return true;  // Si es válido, se permite continuar
    }

    // Método para confirmar la eliminación de una clase
    private void confirmDeleteClase(Clase clase) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Eliminar Clase");

        // Verificar si la clase tiene relaciones en otras tablas
        boolean tieneRelaciones = claseTieneRelaciones(clase);

        if (tieneRelaciones) {
            // Si tiene relaciones, mostrar mensaje y no permitir eliminar
            confirmDialog.add(new Text("La clase con el codigo de clase:  " + clase.getCodigoClase() + ", tiene relaciones con otras tablas y no se puede eliminar."));
            Button cancelButton = new Button("Cerrar", event -> confirmDialog.close());
            confirmDialog.getFooter().add(cancelButton);
        } else {
            // Si no tiene relaciones, permitir la confirmación para eliminar
            confirmDialog.add(new Text("¿Seguro que deseas eliminar la clase con el codigo de clase: " 
                                        + clase.getCodigoClase() + "?"));

            Button deleteButton = new Button("Eliminar", event -> {
                try {
                    claseController.delete(clase.getId()); // Eliminar la clase por su ID
                    Notification.show("Clase eliminada correctamente");
                    confirmDialog.close();
                    refreshGrid(); // Actualizar el grid
                } catch (Exception e) {
                    Notification.show("Error al eliminar la clase: " + e.getMessage());
                }
            });
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR); // Botón de eliminación con estilo de error

            Button cancelButton = new Button("Cancelar", event -> confirmDialog.close());
            confirmDialog.getFooter().add(new HorizontalLayout(deleteButton, cancelButton));
        }

        confirmDialog.open(); // Mostrar el diálogo de confirmación
    }

    // Método para verificar si la clase tiene relaciones asociadas
    private boolean claseTieneRelaciones(Clase clase) {
        // Verificar si la clase tiene participaciones asociadas u otras relaciones
        return !participacionesController.findByClase(clase).isEmpty();  
    }

    // Método para actualizar el grid con las clases más recientes
    private void refreshGrid() {
        grid.setItems(claseController.findAll());
    }

    // Configurar la búsqueda por código de clase
    private void configureSearch() {
        searchButton.addClickListener(event -> searchClase()); // Acción al hacer clic en el botón de búsqueda
    }

    // Método para buscar clases por código
    private void searchClase() {
        String codigoClase = searchCodigoClaseField.getValue().trim();

        if (!codigoClase.isEmpty()) {
            var clases = claseController.findByCodigoClase(codigoClase); // Buscar clases por código
            if (!clases.isEmpty()) {
                grid.setItems(clases);
                Notification.show("Clases encontradas");
            } else {
                Notification.show("No se encontraron clases con ese código");
                refreshGrid(); // Si no se encuentra, mostrar todas las clases
            }
        } else {
            refreshGrid(); // Si no hay valor de búsqueda, mostrar todas las clases
        }
    }

    // Reiniciar la búsqueda
    private void configureResetSearch() {
        resetSearchButton.addClickListener(event -> {
            searchCodigoClaseField.clear();
            refreshGrid(); // Limpiar los campos de búsqueda y mostrar todas las clases
        });
    }

}
