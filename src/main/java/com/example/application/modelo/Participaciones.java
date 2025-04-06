package com.example.application.modelo;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;

@Entity
public class Participaciones {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_participacion", unique = true, nullable = false)
    private String codigoParticipacion;

    private String descripcion;
    private LocalDate fecha;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "estudiante_participacion",
        joinColumns = @JoinColumn(name = "id_participacion"),
        inverseJoinColumns = @JoinColumn(name = "id_estudiante")
    )
    private Set<Estudiantes> estudiantes = new HashSet<>();


    @ManyToOne
    @JoinColumn(name = "id_clase", nullable = false)
    private Clase clase;

    @Column(name = "puntos_asignados", nullable = false)
    private int puntos;

    public Participaciones() {}

    public Participaciones(String codigoParticipacion, String descripcion, LocalDate fecha, Clase clase, Set<Estudiantes> estudiantes, int puntos) {
        this.codigoParticipacion = codigoParticipacion;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.clase = clase;
        this.estudiantes = estudiantes;
        this.puntos = puntos;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigoParticipacion() {
        return codigoParticipacion;
    }

    public void setCodigoParticipacion(String codigoParticipacion) {
        this.codigoParticipacion = codigoParticipacion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        if (fecha.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha no puede ser futura.");
        }
        this.fecha = fecha;
    }

    public Set<Estudiantes> getEstudiantes() {
        return estudiantes;
    }

    public void setEstudiantes(Set<Estudiantes> estudiantes) {
        this.estudiantes = estudiantes;
    }

    public Clase getClase() {
        return clase;
    }

    public void setClase(Clase clase) {
        this.clase = clase;
    }

    public int getPuntos() {
        return puntos;
    }

    public void setPuntos(int puntos) {
        this.puntos = puntos;
    }

    @Override
    public String toString() {
        return "Participaciones{" +
               "id=" + id +
               ", codigoParticipacion='" + codigoParticipacion + '\'' +
               ", descripcion='" + descripcion + '\'' +
               ", fecha=" + fecha +
               ", clase=" + clase.getCodigoClase() +
               ", puntos=" + puntos +
               ", estudiantes=" + estudiantes.size() + " estudiantes" +
               '}'; // Muestra cuántos estudiantes participaron
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Participaciones that = (Participaciones) o;
        return Objects.equals(codigoParticipacion, that.codigoParticipacion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigoParticipacion);
    }
}
