package com.gg.turnlook.features.turno;

import com.gg.turnlook.features.usuario.Usuario;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;







public interface TurnoRepository extends JpaRepository<Turno, Integer> {


    List<Turno> findByEmpleadoAndFechaHoraBetweenAndEstadoNot(Usuario empleado,
                                                  LocalDateTime inicioDia, LocalDateTime finDia,
                                                              EstadoTurno estadoTurno);


    List<Turno> findByServicioSucursalIdAndEstado(Integer sucursalId, EstadoTurno estadoTurno);


    List<Turno> findByClienteEmail(String clienteEmail);


    List<Turno> findByEmpleadoEmailAndServicioSucursalIdAndEstado(
            String empleadoEmail, Integer sucursalId, EstadoTurno estadoTurno);


}


