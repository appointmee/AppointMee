package com.gg.turnlook.features.verificacion;

import com.gg.turnlook.features.verificacion.enums.CanalVerificacion;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name="verificacion")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Setter

public class Verificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private CanalVerificacion canal;

    @Column(nullable=false)
    private String destinatario;

    @Column(nullable=false)
    private String codigoHash;

    @Column(nullable=false)
    private Instant fechaCreacion;

    @Column(nullable=false)
    private Instant fechaVencimiento;

    private Instant fechaUso;

    @Column(nullable = false)
    private Integer intentosFallidos;

}
