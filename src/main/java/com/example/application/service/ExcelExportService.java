package com.example.application.service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
public class ExcelExportService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Cabeceras fijas en orden deseado
    private static final String[] HEADERS = {
        "Carnet", "Apellidos", "Nombres", "Clase", "Fecha", "Tipo", "Puntaje"
    };

    public byte[] exportToExcel(List<Map<String, Object>> data, boolean incluirTotal) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Reporte Participaciones");

            /* ===== Estilos de celda ===== */
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            CellStyle boldStyle = workbook.createCellStyle();
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            boldStyle.setFont(boldFont);

            /* ===== Cabecera ===== */
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            /* ===== Filas de datos ===== */
            int rowNum = 1;
            double totalPuntaje = 0;

            for (Map<String, Object> rowData : data) {
                Row row = sheet.createRow(rowNum++);
                for (int i = 0; i < HEADERS.length; i++) {
                    String key = HEADERS[i];
                    Object value = rowData.get(key);
                    Cell cell = row.createCell(i);

                    if ("Puntaje".equals(key)) {
                        double puntaje = 0;
                        if (value instanceof Number number) {
                            puntaje = number.doubleValue();
                        } else if (value != null) {
                            try {
                                puntaje = Double.parseDouble(value.toString());
                            } catch (NumberFormatException e) {
                                puntaje = 0;
                            }
                        }
                        totalPuntaje += puntaje;
                        cell.setCellValue(puntaje);
                    } else if (value instanceof LocalDate ld) {
                        cell.setCellValue(FMT.format(ld));
                    } else if (value != null) {
                        cell.setCellValue(value.toString());
                    } else {
                        cell.setCellValue("");
                    }
                }
            }

            /* ===== Fila de total solo si aplica ===== */
            if (incluirTotal) {
                Row totalRow = sheet.createRow(rowNum);
                Cell labelCell = totalRow.createCell(HEADERS.length - 2); // penúltima columna
                labelCell.setCellValue("Total:");
                labelCell.setCellStyle(boldStyle);

                Cell totalCell = totalRow.createCell(HEADERS.length - 1); // última columna (Puntaje)
                totalCell.setCellValue(totalPuntaje);
                totalCell.setCellStyle(boldStyle);
            }

            /* ===== Autoajustar columnas ===== */
            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            /* ===== Salida ===== */
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }
}
