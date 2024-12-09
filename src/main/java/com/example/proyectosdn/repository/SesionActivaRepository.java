package com.example.proyectosdn.repository;

import com.example.proyectosdn.entity.SesionActiva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SesionActivaRepository extends JpaRepository<SesionActiva, Integer> {
    @Query(nativeQuery = true,value = "select * from sesion_activa where username=?1 and active=1 order by id desc limit 1")
    SesionActiva obtenerUltimaSesionActiva(String username);

    @Query(nativeQuery = true,value = "select id from sesion_activa sa inner join usuario u on sa.username=u.username inner join dispositivo d on u.id = d.id_usuario where sa.active=1 and d.mac=?1")
    Integer usuarioDeDispositivoEstaEnSesion(String macDispositivo);
}