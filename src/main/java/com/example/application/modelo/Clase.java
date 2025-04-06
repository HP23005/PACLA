package com.example.application.modelo;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class Clase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_clase", unique = true, nullable = false)
    private String codigoClase;

    private String nombreClase;
    private String descripcion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    private String profesor;
    private int maxEstudiantes;

    @OneToMany(mappedBy = "clase") // Relación con Participaciones
    private Set<Participaciones> participaciones = new HashSet<>();

    public Clase() {}

    public Clase(String codigoClase, String nombreClase, String descripcion, LocalDate fechaInicio, LocalDate fechaFin, 
                 String profesor, int maxEstudiantes) {
        this.codigoClase = codigoClase;
        this.nombreClase = nombreClase;
        this.descripcion = descripcion;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.profesor = profesor;
        this.maxEstudiantes = maxEstudiantes;
    }

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigoClase() {
        return codigoClase;
    }

    public void setCodigoClase(String codigoClase) {
        this.codigoClase = codigoClase;
    }

    public String getNombreClase() {
        return nombreClase;
    }

    public void setNombreClase(String nombreClase) {
        this.nombreClase = nombreClase;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        if (fechaInicio.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser futura.");
        }
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        if (fechaFin.isBefore(fechaInicio)) {
            throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la fecha de inicio.");
        }
        this.fechaFin = fechaFin;
    }

    public String getProfesor() {
        return profesor;
    }

    public void setProfesor(String profesor) {
        this.profesor = profesor;
    }

    public int getMaxEstudiantes() {
        return maxEstudiantes;
    }

    public void setMaxEstudiantes(int maxEstudiantes) {
        this.maxEstudiantes = maxEstudiantes;
    }

    public Set<Participaciones> getParticipaciones() {
        return participaciones;
    }

    public void setParticipaciones(Set<Participaciones> participaciones) {
        this.participaciones = participaciones;
    }

    @Override
    public String toString() {
        return "Clase{" +
               "id=" + id +
               ", codigoClase='" + codigoClase + '\'' +
               ", nombreClase='" + nombreClase + '\'' +
               ", descripcion='" + descripcion + '\'' +
               ", fechaInicio=" + fechaInicio +
               ", fechaFin=" + fechaFin +
               ", profesor='" + profesor + '\'' +
               ", maxEstudiantes=" + maxEstudiantes +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Clase clase = (Clase) o;
        return Objects.equals(codigoClase, clase.codigoClase);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigoClase);
    }
}
