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

@Entity
public class Estudiantes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombres_estudiante")
    private String nombresEstudiante;

    private String apellidosEstudiante;
    private LocalDate fechaNacimiento;

    @Column(unique = true, nullable = false)
    private String carnet;

    private String nivelAcademico;
    private String nombrePadre;
    private String nombreMadre;

    @Column(name = "estado_estudiante")
    private String estadoEstudiante;

    @Column(columnDefinition = "BYTEA")
    private byte[] foto;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "estudiante_participacion",
        joinColumns = @JoinColumn(name = "id_estudiante"),
        inverseJoinColumns = @JoinColumn(name = "id_participacion")
    )
    private Set<Participaciones> participacion = new HashSet<>();


    public Estudiantes() {}

    public Estudiantes(String nombresEstudiante, String apellidosEstudiante, LocalDate fechaNacimiento,
                        String carnet, String nivelAcademico, String estadoEstudiante) {
        this.nombresEstudiante = nombresEstudiante;
        this.apellidosEstudiante = apellidosEstudiante;
        this.fechaNacimiento = fechaNacimiento;
        this.carnet = carnet;
        this.nivelAcademico = nivelAcademico;
        this.estadoEstudiante = estadoEstudiante;
    }

    // Getters y Setters
    public Long getIdEstudiante() {
        return id;
    }

    public void setIdEstudiantes(Long id) {
        this.id = id;
    }

    public String getNombresEstudiante() {
        return nombresEstudiante;
    }

    public void setNombresEstudiante(String nombresEstudiante) {
        this.nombresEstudiante = nombresEstudiante;
    }

    public String getApellidosEstudiante() {
        return apellidosEstudiante;
    }

    public void setApellidosEstudiante(String apellidosEstudiante) {
        this.apellidosEstudiante = apellidosEstudiante;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        if (fechaNacimiento.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser futura.");
        }
        if (fechaNacimiento.isBefore(LocalDate.now().minusYears(120))) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser mayor a 120 años atrás.");
        }
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getNivelAcademico() {
        return nivelAcademico;
    }

    public void setNivelAcademico(String nivelAcademico) {
        this.nivelAcademico = nivelAcademico;
    }

    public String getNombrePadre() {
        return nombrePadre;
    }

    public void setNombrePadre(String nombrePadre) {
        this.nombrePadre = nombrePadre;
    }

    public String getNombreMadre() {
        return nombreMadre;
    }

    public void setNombreMadre(String nombreMadre) {
        this.nombreMadre = nombreMadre;
    }

    public String getEstadoEstudiante() {
        return estadoEstudiante;
    }

    public void setEstadoEstudiante(String estadoEstudiante) {
        this.estadoEstudiante = estadoEstudiante;
    }

    public byte[] getFoto() {
        return foto;
    }

    public void setFoto(byte[] foto) {
        this.foto = foto;
    }

    @Override
    public String toString() {
        return "Estudiantes{" +
               "id=" + id +
               ", nombresEstudiante='" + nombresEstudiante + '\'' +
               ", apellidosEstudiante='" + apellidosEstudiante + '\'' +
               ", fechaNacimiento=" + fechaNacimiento +
               ", carnet='" + carnet + '\'' +
               ", nivelAcademico='" + nivelAcademico + '\'' +
               ", estadoEstudiante='" + estadoEstudiante + '\'' +
               ", foto=" + (foto != null ? "image data" : "no photo") +
               '}'; 
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Estudiantes that = (Estudiantes) o;
        return Objects.equals(carnet, that.carnet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(carnet);
    }

    public String getCarnet() {
        return carnet;
    }

    public void setCarnet(String carnet) {
        this.carnet = carnet;
    }

    public Set<Participaciones> getParticipacion() {
        return participacion;
    }

    public void setParticipacion(Set<Participaciones> participacion) {
        this.participacion = participacion;
    }

     // Métodos para agregar y quitar la participacion a través de la relación ManyToMany
     public void addParticipaciones(Participaciones participaciones) {
        this.participacion.add(participaciones);  // Añade la participacion al estudiante.
        participaciones.getEstudiantes().add(this);  // Añade el estudiante a la participacion.
    }
    
    public void removeParticipaciones(Participaciones participaciones) {
        this.participacion.remove(participaciones);  // Elimina las participaciones del estudiante.
        participaciones.getEstudiantes().remove(this);  // Elimina el estudiante de la participacion.
    }
}
