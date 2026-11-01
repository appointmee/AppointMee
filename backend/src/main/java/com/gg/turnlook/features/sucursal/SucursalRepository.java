package com.gg.turnlook.features.sucursal;

import com.gg.turnlook.features.sucursal.categoria.ECategoria;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;





public interface SucursalRepository extends JpaRepository<Sucursal, Integer> {


    // categoria y fotoPerfil se traen con JOIN en la misma query. Si no, cada
    // sucursal de la lista dispara dos queries extra al acceder a la categoria
    // o a la foto (que son lazy).


    @EntityGraph(attributePaths = {"categoria", "fotoPerfil"})
    List<Sucursal> findByActivoTrue();


    boolean existsByDireccionIgnoreCase(String direccion);


    @EntityGraph(attributePaths = {"categoria", "fotoPerfil"})
    List<Sucursal> findByNombreContainingIgnoreCaseAndCategoriaCategoriaAndActivoTrue(
            String nombre, ECategoria categoria);


    @EntityGraph(attributePaths = {"categoria", "fotoPerfil"})
    List<Sucursal> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre);


    @EntityGraph(attributePaths = {"categoria", "fotoPerfil"})
    List<Sucursal> findByCategoriaCategoriaAndActivoTrue(ECategoria categoria);


    @EntityGraph(attributePaths = {"categoria", "fotoPerfil"})
    @Query("""
                SELECT s FROM Sucursal s
                WHERE s.activo = true
                AND (
                    s.empleador.email = :email
                    OR :email IN (
                        SELECT e.email FROM s.empleados e
                    )
                )
            """)
    List<Sucursal> findSucursalesDondeEsta(String email);


}


