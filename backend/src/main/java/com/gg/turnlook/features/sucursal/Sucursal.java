package com.gg.turnlook.features.sucursal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gg.turnlook.features.imagen.Imagen;
import com.gg.turnlook.features.sucursal.categoria.Categoria;
import com.gg.turnlook.features.usuario.Usuario;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;






@Entity
@Table(name = "sucursales")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Sucursal {

    /// ATRIBUTOS

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String nombre;

    @Column(unique = true, nullable = false)
    private String direccion;

    @Column(nullable = true)
    private String telefono;

    @Column(nullable = false)
    private String descripcion;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDate fechaCreacion;

    @Column(name = "hora_apertura", nullable = false)
    private LocalTime horaApertura;

    @Column(name = "hora_cierre", nullable = false)
    private LocalTime horaCierre;

    @Column(nullable = false)
    private boolean activo = true;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "imagen_id")
    private Imagen fotoPerfil;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empleador_id")
    private Usuario empleador;


    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "sucursal_empleados",
            joinColumns = @JoinColumn(name = "sucursal_id"),
            inverseJoinColumns = @JoinColumn(name = "usuario_id")
    )
    @JsonIgnoreProperties("sucursalesEmpleado")
    private Set<Usuario> empleados = new HashSet<>();




    @PrePersist
    public void prePersist() {
        this.fechaCreacion = LocalDate.now();
    }



    /// CONSTRUCTORES


    public Sucursal(String nombre, String direccion, String telefono, String descripcion, LocalTime horaApertura, LocalTime horaCierre, Categoria categoria, Usuario empleador) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.telefono = telefono;
        this.descripcion = descripcion;
        this.horaApertura = horaApertura;
        this.horaCierre = horaCierre;
        this.categoria = categoria;
        this.empleador = empleador;
    }


}



