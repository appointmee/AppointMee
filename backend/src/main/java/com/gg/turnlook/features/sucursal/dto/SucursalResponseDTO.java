package com.gg.turnlook.features.sucursal.dto;

import com.gg.turnlook.features.imagen.dto.ImagenResponseDTO;
import com.gg.turnlook.features.usuario.dto.UsuarioMiniDTO;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@AllArgsConstructor
@NoArgsConstructor
public class SucursalResponseDTO {

     private Integer id;
     private String nombre;
     private String direccion;
     private String telefono;
     private String descripcion;
     private LocalDate fechaCreacion;
     private String categoria;
     private LocalTime horaApertura;
     private LocalTime horaCierre;
     private Integer puntuacion;
     private Long cantidadPuntuaciones;
     private UsuarioMiniDTO empleador;
     private Set<UsuarioMiniDTO> empleados;
     private String fotoPerfil;
     private List<ImagenResponseDTO> imagenes;

}
