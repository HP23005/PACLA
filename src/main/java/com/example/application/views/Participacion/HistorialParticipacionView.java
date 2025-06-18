package com.example.application.views.Participacion;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.example.application.controlador.ParticipacionController;
import com.example.application.modelo.ParticipacionDetalle;
import com.example.application.modelo.Participaciones;
import com.example.application.service.ExcelExportService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.FooterRow;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrderBuilder;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;

@PageTitle("Historial por Estudiante | PACLA")
@Route(value = "historial-estudiante", layout = MainLayout.class)
public class HistorialParticipacionView extends VerticalLayout {

    private final ParticipacionController controller;
    private final ExcelExportService excelExportService;

    private final Grid<ParticipacionDetalle> grid = new Grid<>(ParticipacionDetalle.class, false);
    private final ComboBox<String> carnetField = new ComboBox<>("Carnet del Estudiante");
    private final Button exportIndividualBtn = new Button("Exportar Individual", VaadinIcon.DOWNLOAD.create());
    private final Button exportGrupalBtn = new Button("Exportar Grupal", VaadinIcon.DOWNLOAD_ALT.create());
    private final Button limpiarFiltrosBtn = new Button("Limpiar Filtros", VaadinIcon.CLOSE_CIRCLE.create());
    
    private final DatePicker fechaInicioField = new DatePicker("Desde fecha");
    private final DatePicker fechaFinField = new DatePicker("Hasta fecha");
    private final NumberField puntosMinField = new NumberField("Puntos mínimos");
    private final NumberField puntosMaxField = new NumberField("Puntos máximos");

    private final Span totalLabel = new Span("Total puntos: 0");

    private FooterRow footerRow;

    public HistorialParticipacionView(ParticipacionController controller,
                                      ExcelExportService excelExportService) {
        this.controller = controller;
        this.excelExportService = excelExportService;

        setSizeFull();
        configureGrid();
        configureCarnetField();
        configureButtons();

        add(
            new H2("Historial de Participaciones por Estudiante"),
            createSearchBar(),
            totalLabel,
            grid
        );

        limpiarFiltros();
    }

    private void configureCarnetField() {
        carnetField.setAllowCustomValue(true);
        carnetField.setPlaceholder("Seleccione o escriba un carnet");
        carnetField.setClearButtonVisible(true);

        List<String> carnets = controller.getTodosLosCarnets();
        carnetField.setItems(carnets);

        carnetField.addValueChangeListener(event -> {
            String carnetSeleccionado = event.getValue();
            boolean tieneParticipaciones = carnetSeleccionado != null && !carnetSeleccionado.isEmpty()
                    && !controller.findByEstudianteCarnet(carnetSeleccionado).isEmpty();

            exportIndividualBtn.setEnabled(tieneParticipaciones);
        });
    }

    private void configureGrid() {
        grid.setHeight("400px");
        grid.removeAllColumns();

        grid.addColumn(ParticipacionDetalle::getCarnet)
            .setHeader("Carnet").setAutoWidth(true);

        var fechaCol = grid.addColumn(ParticipacionDetalle::getFecha)
            .setHeader("Fecha")
            .setComparator(ParticipacionDetalle::getFecha)
            .setAutoWidth(true);

        grid.addColumn(ParticipacionDetalle::getClase)
            .setHeader("Clase").setAutoWidth(true);

        grid.addColumn(ParticipacionDetalle::getDescripcion)
            .setHeader("Descripción").setAutoWidth(true);

        var puntosCol = grid.addColumn(ParticipacionDetalle::getPuntos)
            .setHeader("Puntos").setAutoWidth(true);

        grid.sort(new GridSortOrderBuilder<ParticipacionDetalle>()
            .thenDesc(fechaCol).build());

        footerRow = grid.appendFooterRow();
        footerRow.getCell(puntosCol).setText("Total: 0");
    }

    private List<ParticipacionDetalle> desglosarParticipaciones(List<Participaciones> participaciones) {
        List<ParticipacionDetalle> detalles = new ArrayList<>();
        for (Participaciones p : participaciones) {
            for (var estudiante : p.getEstudiantes()) {
                detalles.add(new ParticipacionDetalle(
                    estudiante.getCarnet(),
                    p.getClase().getNombreClase(),
                    p.getDescripcion(),
                    p.getFecha(),
                    p.getPuntos()
                ));
            }
        }
        return detalles;
    }

    private void configureButtons() {
        exportIndividualBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        exportGrupalBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        limpiarFiltrosBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);

        exportIndividualBtn.getElement().setProperty("title", "Exporta solo al estudiante mostrado");
        exportGrupalBtn.getElement().setProperty("title", "Exporta todos los estudiantes");

        exportIndividualBtn.addClickListener(e -> exportToExcel(true));
        exportGrupalBtn.addClickListener(e -> exportToExcel(false));
        limpiarFiltrosBtn.addClickListener(e -> limpiarFiltros());
    }

    private VerticalLayout createSearchBar() {
        Button buscarBtn = new Button("Buscar", VaadinIcon.SEARCH.create(), e -> search());
        buscarBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // Agrupamos los filtros por fecha y puntos
        HorizontalLayout filtrosAdicionales = new HorizontalLayout(fechaInicioField, fechaFinField, puntosMinField, puntosMaxField);
        filtrosAdicionales.setAlignItems(Alignment.END);

        HorizontalLayout botones = new HorizontalLayout(buscarBtn, limpiarFiltrosBtn, exportIndividualBtn, exportGrupalBtn);
        botones.setAlignItems(Alignment.END);

        VerticalLayout searchLayout = new VerticalLayout(carnetField, filtrosAdicionales, botones);
        searchLayout.setSpacing(true);
        return searchLayout;
    }

    private void search() {
        String carnet = carnetField.getValue() != null ? carnetField.getValue().trim() : "";

        List<Participaciones> participaciones = carnet.isEmpty()
            ? controller.findAll()
            : controller.findByEstudianteCarnet(carnet);

        List<ParticipacionDetalle> detalles = desglosarParticipaciones(participaciones);

        // Aplicar filtro por fecha
        if (fechaInicioField.getValue() != null) {
            detalles = detalles.stream()
                .filter(d -> !d.getFecha().isBefore(fechaInicioField.getValue()))
                .toList();
        }
        if (fechaFinField.getValue() != null) {
            detalles = detalles.stream()
                .filter(d -> !d.getFecha().isAfter(fechaFinField.getValue()))
                .toList();
        }

        // Aplicar filtro por puntos
        if (puntosMinField.getValue() != null) {
            detalles = detalles.stream()
                .filter(d -> d.getPuntos() >= puntosMinField.getValue())
                .toList();
        }
        if (puntosMaxField.getValue() != null) {
            detalles = detalles.stream()
                .filter(d -> d.getPuntos() <= puntosMaxField.getValue())
                .toList();
        }

        grid.setItems(detalles);
        actualizarTotales(detalles);
    }

    private void limpiarFiltros() {
        carnetField.clear();
        fechaInicioField.clear();
        fechaFinField.clear();
        puntosMinField.clear();
        puntosMaxField.clear();

        List<Participaciones> todas = controller.findAll();
        List<ParticipacionDetalle> detalles = desglosarParticipaciones(todas);
        grid.setItems(detalles);
        actualizarTotales(detalles);
    }

    private void actualizarTotales(List<ParticipacionDetalle> detalles) {
        int total = detalles.stream().mapToInt(ParticipacionDetalle::getPuntos).sum();
        totalLabel.setText("Total puntos: " + total);
        footerRow.getCell(grid.getColumns().get(4)).setText("Total: " + total);
    }

    private void exportToExcel(boolean individual) {
        try {
            String carnet = individual ? carnetField.getValue() != null ? carnetField.getValue().trim() : "" : null;

            if (individual && (carnet == null || carnet.isEmpty())) {
                Notification.show("Debe seleccionar o ingresar un carnet para exportar.", 3000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
                return;
            }

            List<Map<String, Object>> data = controller.getDatosParaReporte(carnet);

            if (data == null || data.isEmpty()) {
                String mensaje = individual 
                    ? "El estudiante seleccionado no tiene participaciones para exportar." 
                    : "No hay datos para exportar.";

                Notification.show(mensaje, 3000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
                return;
            }

            byte[] excelBytes = excelExportService.exportToExcel(data, individual);
            String filename = individual ? "participacion_" + carnet + ".xlsx" : "participaciones_grupales.xlsx";

            StreamResource resource = new StreamResource(
                filename,
                () -> new ByteArrayInputStream(excelBytes)
            );
            resource.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            Anchor downloadLink = new Anchor(resource, "");
            downloadLink.getElement().setAttribute("download", true);
            downloadLink.getElement().getStyle().set("display", "none");
            add(downloadLink);
            downloadLink.getElement().executeJs("this.click()");

        } catch (Exception ex) {
            Notification.show("Error al generar Excel: " + ex.getMessage(), 5000, Notification.Position.BOTTOM_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            // Log the exception or handle it appropriately in production
        }
    }

    public Span getTotalLabel() {
        return totalLabel;
    }
}
