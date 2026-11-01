package com.gg.turnlook.features.servicio;

import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;



public interface ServicioRepository extends JpaRepository<Servicio, Integer> {

    List<Servicio> findByActivoTrue();

    // fotoPerfil viene con JOIN para no consultarla una vez por servicio.
    @EntityGraph(attributePaths = {"fotoPerfil"})
    List<Servicio> findBySucursalId(Integer sucursalId);
}
