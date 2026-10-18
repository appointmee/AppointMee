package com.gg.turnlook.features.solicitud.empleador;

import com.gg.turnlook.features.solicitud.EstadoSolicitud;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;





public interface SolicitudEmpleadorRepository extends JpaRepository<SolicitudEmpleador, Integer> {


    boolean existsByUsuarioIdAndEstado(Integer userId, EstadoSolicitud estado);


    List<SolicitudEmpleador> findByEstado(EstadoSolicitud estado);


    List<SolicitudEmpleador> findByUsuarioEmail(String userEmail);


}



