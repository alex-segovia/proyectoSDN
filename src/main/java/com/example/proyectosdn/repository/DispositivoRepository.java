package com.example.proyectosdn.repository;

import com.example.proyectosdn.entity.Dispositivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface DispositivoRepository extends JpaRepository<Dispositivo, Integer> {
    List<Dispositivo> findByUsuarioId(Integer usuarioId);
    Optional<Dispositivo> findByMac(String mac);

    @Query(nativeQuery = true,value = "select * from dispositivo where mac=?1 and estado=1")
    Dispositivo obtenerDispositivo(String macDispositivo);

    @Transactional
    @Modifying
    @Query(nativeQuery = true,value = "update dispositivo set id_usuario=(select id from usuario where username=?1) where mac=?2")
    void actualizarIdUsuario(String username,String macDispositivo);


}