package com.gg.turnlook.features.turno;

import com.gg.turnlook.features.usuario.Usuario;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;







public interface TurnoRepository extends JpaRepository<Turno, Integer> {

    @Modifying
    @Query("update Turno t set t.estado = :nuevoEstado where t.id = :turnoId and t.estado in :estadosActuales")
    int actualizarEstadoSiEs(@Param("turnoId") Integer turnoId,
                             @Param("estadosActuales") Collection<EstadoTurno> estadosActuales,
                             @Param("nuevoEstado") EstadoTurno nuevoEstado);


    List<Turno> findByEmpleadoAndFechaHoraBetweenAndEstadoNot(Usuario empleado,
                                                  LocalDateTime inicioDia, LocalDateTime finDia,
                                                              EstadoTurno estadoTurno);


    List<Turno> findByServicioSucursalIdAndEstado(Integer sucursalId, EstadoTurno estadoTurno);


    List<Turno> findByClienteEmail(String clienteEmail);


    List<Turno> findByEmpleadoEmailAndServicioSucursalIdAndEstado(
            String empleadoEmail, Integer sucursalId, EstadoTurno estadoTurno);


}


