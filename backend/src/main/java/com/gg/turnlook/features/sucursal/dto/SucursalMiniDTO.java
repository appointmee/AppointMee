package com.gg.turnlook.features.sucursal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;






@Data
@AllArgsConstructor
@NoArgsConstructor
public class SucursalMiniDTO {


    private Integer id;
    private String nombre;
    private String categoria;
    private String fotoPerfil;
    private Integer puntuacion;
    private Long cantidadPuntuaciones;

}



