package com.example.application.modelo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParticipacionRepository extends JpaRepository<Participaciones, Long> {

    // Método para buscar participaciones por el código de participación
    Participaciones findByCodigoParticipacion(String codigo);
    List<Participaciones> findByCodigoParticipacionLike(String codigo);
    // Método para encontrar participaciones asociadas a una clase
    List<Participaciones> findByClase(Clase clase);
    // Puedes agregar más métodos personalizados si es necesario
    long countByEstudiantes_Id(Long estudianteId);
}
