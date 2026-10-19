package com.gg.turnlook.features.verificacion;

import com.gg.turnlook.features.verificacion.enums.CanalVerificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificacionRepository  extends JpaRepository<Verificacion,Long> {

    Optional<Verificacion> findFirstByCanalAndDestinatarioOrderByFechaCreacionDesc(
            CanalVerificacion canal,
            String destinatario);

}
