package com.gg.turnlook.features.imagen;

import com.gg.turnlook.features.sucursal.Sucursal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;







public interface ImagenRepository extends JpaRepository<Imagen, Integer> {

    List<Imagen> findBySucursal(Sucursal sucursal);

    long countBySucursal(Sucursal sucursal);

}


