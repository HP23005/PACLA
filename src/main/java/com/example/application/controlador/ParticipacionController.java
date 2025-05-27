package com.example.application.controlador;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.application.modelo.Clase;
import com.example.application.modelo.ParticipacionRepository;
import com.example.application.modelo.Participaciones;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ParticipacionController {

    private final ParticipacionRepository participacionRepository;

    // Constructor para inicializar el controlador con el repositorio de participaciones
    public ParticipacionController(ParticipacionRepository participacionRepository) {
        this.participacionRepository = participacionRepository;
    }

    // Obtiene todas las participaciones
    public List<Participaciones> findAll() {
        return participacionRepository.findAll();
    }

    // Guarda o actualiza una participación en la base de datos
    public Participaciones save(Participaciones participacion) {
        return participacionRepository.save(participacion);
    }

    // Busca una participación por su ID
    public Optional<Participaciones> findById(Long id) {
        return participacionRepository.findById(id);
    }

    // Elimina una participación por su ID (rompiendo relación con estudiantes primero)
    public void delete(Long participacionId) {
        // Intentamos obtener la participación por su ID
        Optional<Participaciones> participacionOptional = participacionRepository.findById(participacionId);

        if (participacionOptional.isPresent()) {
            Participaciones participacion = participacionOptional.get();

            // 💡 Rompemos la relación con los estudiantes antes de eliminar
            participacion.getEstudiantes().clear();
            participacionRepository.save(participacion); // Guardamos sin relaciones

            // Ahora sí eliminamos la participación
            participacionRepository.delete(participacion);
        } else {
            throw new EntityNotFoundException("Participación con ID " + participacionId + " no encontrada.");
        }
    }

    public Participaciones findByCodigoParticipacion(String codigo) {
        return participacionRepository.findByCodigoParticipacion(codigo);
    }

    public List<Participaciones> findByCodigoParticipacionLike(String codigo) {
        return participacionRepository.findByCodigoParticipacionLike(codigo);
    }
    

    // Método para actualizar la descripción de una participación
    public Participaciones updateDescripcion(Long idParticipacion, String descripcion) {
        Optional<Participaciones> optionalParticipacion = participacionRepository.findById(idParticipacion);

        // Si la participación existe, actualizamos su descripción
        if (optionalParticipacion.isPresent()) {
            Participaciones participacion = optionalParticipacion.get();
            participacion.setDescripcion(descripcion);
            return participacionRepository.save(participacion);  // Guardamos la participación actualizada
        }

        // Si no se encuentra la participación, devolvemos null
        return null;
    }


    public List<Participaciones> findByClase(Clase clase) {
        return participacionRepository.findByClase(clase);  // Aquí debería ir la implementación que obtiene las participaciones por clase.
    }

}
