package com.example.application.controlador;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.application.modelo.Clase;
import com.example.application.modelo.ClaseRepository;

@Service
public class ClaseController {

    // Inyección de dependencia del repositorio de Clase
    private final ClaseRepository claseRepository;

    // Constructor para inicializar el controlador con el repositorio de Clase
    public ClaseController(ClaseRepository claseRepository) {
        this.claseRepository = claseRepository;
    }

    // Obtiene todas las clases registradas
    public List<Clase> findAll() {
        return claseRepository.findAll();
    }

    // Guarda o actualiza una clase en la base de datos
    public Clase save(Clase clase) {
        return claseRepository.save(clase);
    }

    // Busca una clase por su ID y devuelve un Optional para manejar la posibilidad de que no exista
    public Optional<Clase> findById(Long id) {
        return claseRepository.findById(id);
    }

    // Elimina una clase de la base de datos por su ID
    public void delete(Long idClase) {
        Optional<Clase> claseOptional = claseRepository.findById(idClase);
        
        // Si la clase existe, la elimina
        if (claseOptional.isPresent()) {
            claseRepository.delete(claseOptional.get());
        } else {
            // Si la clase no existe, puedes lanzar una excepción o hacer alguna otra acción
            throw new IllegalArgumentException("Clase con ID " + idClase + " no encontrada.");
        }
    }

    // Busca clases por su código de clase
    public List<Clase> findByCodigoClase(String codigoClase) {
        return claseRepository.findByCodigoClase(codigoClase);
    }

    // Método adicional para actualizar la descripción de una clase
    public Clase updateDescripcion(Long idClase, String descripcion) {
        Optional<Clase> optionalClase = claseRepository.findById(idClase);

        // Si la clase existe, actualizamos su descripción
        if (optionalClase.isPresent()) {
            Clase clase = optionalClase.get();
            clase.setDescripcion(descripcion);  // Actualizamos la descripción de la clase
            return claseRepository.save(clase);  // Guardamos la clase con la nueva descripción
        }

        // Si no se encuentra la clase, devolvemos null
        return null;
    }

    // Método para verificar si existe una clase por su código
    public boolean existsByCodigoClase(String codigoClase) {
        return !claseRepository.findByCodigoClase(codigoClase).isEmpty();
    }

    
}
