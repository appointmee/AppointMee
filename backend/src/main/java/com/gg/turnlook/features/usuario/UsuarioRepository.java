package com.gg.turnlook.features.usuario;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;







public interface UsuarioRepository extends JpaRepository<Usuario,Integer> {


    @EntityGraph(attributePaths = "roles")
    Optional<Usuario> findByEmailAndActivoTrue(String email);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select u from Usuario u where u.id = :id")
    Optional<Usuario> bloquearPorId(Integer id);

    boolean existsByEmail(String email);

    Optional<Usuario> findByEmailAndActivoFalse(String email);

    boolean existsByEmailAndActivoFalse(String email);

}


