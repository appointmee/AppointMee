package com.gg.turnlook.features.servicio;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;



public interface ServicioRepository extends JpaRepository<Servicio, Integer> {

    List<Servicio> findByActivoTrue();
    List<Servicio> findBySucursalId(Integer sucursalId);
}
