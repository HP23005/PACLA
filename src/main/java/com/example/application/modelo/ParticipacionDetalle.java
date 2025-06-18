package com.example.application.modelo;

import java.time.LocalDate;

public class ParticipacionDetalle {
    private final String carnet;
    private final String clase;
    private final String descripcion;
    private final LocalDate fecha;
    private final int puntos;

    public ParticipacionDetalle(String carnet, String clase, String descripcion, LocalDate fecha, int puntos) {
        this.carnet = carnet;
        this.clase = clase;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.puntos = puntos;
    }

    // Getters
    public String getCarnet() { return carnet; }
    public String getClase() { return clase; }
    public String getDescripcion() { return descripcion; }
    public LocalDate getFecha() { return fecha; }
    public int getPuntos() { return puntos; }


    
}