package com.example.application.controlador;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.application.modelo.Clase;
import com.example.application.modelo.EstudiantesRepository;
import com.example.application.modelo.ParticipacionRepository;
import com.example.application.modelo.Participaciones;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ParticipacionController {

    private final ParticipacionRepository participacionRepository;
    public ParticipacionController(ParticipacionRepository participacionRepository, EstudiantesRepository estudiantesRepository) {
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

    public List<Participaciones> findByEstudianteCarnet(String carnet) {
        return participacionRepository.findByEstudianteCarnet(carnet);
        // O usar: return participacionRepository.findByEstudiantes_Carnet(carnet);
    }

    /**
     * Devuelve los datos listos para el XLSX.
     *  ‑ Si «carnet» es nulo o vacío ⇒ reporte grupal
     *  ‑ Si se especifica ⇒ reporte individual
     */
    public List<Map<String, Object>> getDatosParaReporte(String carnet) {
        List<Participaciones> participaciones =
                carnet != null && !carnet.isEmpty()
                        ? findByEstudianteCarnet(carnet)
                        : findAll();

        return participaciones.stream()
                .flatMap(p -> p.getEstudiantes().stream()
                        .filter(e -> carnet == null || carnet.isEmpty() || e.getCarnet().equalsIgnoreCase(carnet))
                        .map(e -> {
                            Map<String, Object> row = new LinkedHashMap<>();
                            row.put("Carnet", e.getCarnet());
                            row.put("Apellidos", e.getApellidosEstudiante());
                            row.put("Nombres", e.getNombresEstudiante());
                            row.put("Clase", p.getClase().getNombreClase());
                            row.put("Fecha", p.getFecha());
                            row.put("Tipo", p.getDescripcion());
                            row.put("Puntaje", p.getPuntos());
                            return row;
                        })
                )
                .toList();
    }

    public List<String> getTodosLosCarnets() {
        return participacionRepository.findAll().stream()
            .flatMap(p -> p.getEstudiantes().stream())
            .map(e -> e.getCarnet())
            .distinct()
            .toList();
    }

}
