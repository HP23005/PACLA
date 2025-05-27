package com.example.application.controlador;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.application.modelo.Estudiantes;
import com.example.application.modelo.EstudiantesRepository;
import com.example.application.modelo.ParticipacionRepository; // Asegúrate de tener este import

@Service
public class EstudiantesController {

    // Inyección de dependencia del repositorio de Estudiantes
    private final EstudiantesRepository estudiantesRepository;

    // Inyección de dependencia del repositorio de Participacion
    private final ParticipacionRepository participacionRepository;

    // Constructor para inicializar el controlador con los repositorios necesarios
    public EstudiantesController(EstudiantesRepository estudiantesRepository, ParticipacionRepository participacionRepository) {
        this.estudiantesRepository = estudiantesRepository;
        this.participacionRepository = participacionRepository;
    }

    // Obtiene todos los estudiantes registrados
    public List<Estudiantes> findAll() {
        return estudiantesRepository.findAll();
    }

    // Guarda o actualiza un estudiante en la base de datos
    public Estudiantes save(Estudiantes estudiante) {
        return estudiantesRepository.save(estudiante);
    }

    // Busca un estudiante por su ID y devuelve un Optional para manejar la posibilidad de que no exista
    public Optional<Estudiantes> findById(Long id) {
        return estudiantesRepository.findById(id); 
    }

    // Elimina un estudiante de la base de datos
    public void delete(Estudiantes estudiante) {
        estudiantesRepository.delete(estudiante);
    }

    // Busca estudiantes por su carnet
    public List<Estudiantes> findByCarnet(String carnet) {
        return estudiantesRepository.findByCarnet(carnet);
    }

    // Método adicional para actualizar la foto de un estudiante
    public Estudiantes updateFoto(Long idEstudiante, byte[] foto) {  
        Optional<Estudiantes> optionalEstudiante = estudiantesRepository.findById(idEstudiante);
        
        if (optionalEstudiante.isPresent()) {
            Estudiantes estudiante = optionalEstudiante.get();
            estudiante.setFoto(foto);  // Actualizamos la foto con los datos binarios
            return estudiantesRepository.save(estudiante);  // Guardamos el estudiante con la nueva foto
        }
        
        return null;
    }

    // Método para obtener todos los nombres de los estudiantes
    public List<String> findAllNombres() {
        return estudiantesRepository.findAllNombresEstudiante();
    }

    // Método para buscar un estudiante por su nombre
    public Estudiantes findByNombre(String estudianteNombre) {
        return estudiantesRepository.findByEstadoEstudiante(estudianteNombre);  // Busca estudiante por nombre
    }

    public boolean existsByCarnet(String carnet) {
        return estudiantesRepository.existsByCarnet(carnet);
    }

    // Método para verificar si un estudiante tiene relaciones con otras tablas
    public boolean tieneRelaciones(Long estudianteId) {
        // Verificamos si el estudiante tiene participaciones
        long count = participacionRepository.countByEstudiantes_Id(estudianteId);
        return count > 0; // Si hay participaciones, significa que el estudiante tiene relaciones
    }
}
