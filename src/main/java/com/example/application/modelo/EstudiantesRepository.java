package com.example.application.modelo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository  // La anotación @Repository marca esta interfaz como un repositorio de Spring Data JPA.
public interface EstudiantesRepository extends JpaRepository<Estudiantes, Long> { 

    // Encuentra estudiantes por su carnet, devuelve una lista
    List<Estudiantes> findByCarnet(String carnet);  // Método para encontrar estudiantes por su carnet.

    // Encuentra un estudiante por su carnet, devuelve un Optional
    Optional<Estudiantes> findOneByCarnet(String carnet);  // Cambié el nombre a 'findOneByCarnet' para reflejar mejor su propósito.

    // Encuentra estudiantes por su estado
    Estudiantes findByEstadoEstudiante(String estadoEstudiante);  // Busca un estudiante por su estado (activo, inactivo, etc.).

    // Método para obtener todos los estudiantes
    @Override  // Sobrescribe el método findAll() para devolver una lista de todos los estudiantes.
    List<Estudiantes> findAll(); 

    // Método para obtener solo los nombres de los estudiantes
    @Query("SELECT e.nombresEstudiante FROM Estudiantes e")  // Usa una consulta JPQL para devolver solo los nombres de los estudiantes.
    List<String> findAllNombresEstudiante();

    // Si el campo 'carnet' debe ser único, confirma su unicidad a nivel de base de datos.
    @Query("SELECT COUNT(e) > 0 FROM Estudiantes e WHERE e.carnet = :carnet")  // Consulta personalizada para verificar si el carnet existe.
    boolean existsByCarnet(String carnet);  // Útil para validaciones antes de guardar o actualizar.

    
}
