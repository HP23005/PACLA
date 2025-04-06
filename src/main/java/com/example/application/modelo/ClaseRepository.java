package com.example.application.modelo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaseRepository extends JpaRepository<Clase, Long> {
    
    // Método para encontrar clases por código de clase
    List<Clase> findByCodigoClase(String codigoClase);

    // Este método es suficiente para verificar si existe una clase con el código proporcionado
    boolean existsByCodigoClase(String codigoClase);
    

}
