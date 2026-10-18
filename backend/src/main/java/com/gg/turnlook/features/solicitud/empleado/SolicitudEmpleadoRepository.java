package com.gg.turnlook.features.solicitud.empleado;

import com.gg.turnlook.features.solicitud.EstadoSolicitud;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;






public interface SolicitudEmpleadoRepository extends JpaRepository<SolicitudEmpleado, Integer> {


    List<SolicitudEmpleado> findBySucursalId(Integer sucursalId);

    List<SolicitudEmpleado> findByEmpleadoEmail(String empleadoEmail);

    boolean existsByEmpleadoIdAndSucursalIdAndEstado(Integer empleadoId, Integer sucursalId,
                                                     EstadoSolicitud estadoSolicitud);

}
