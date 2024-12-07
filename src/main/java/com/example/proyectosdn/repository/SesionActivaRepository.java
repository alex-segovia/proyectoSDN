package com.example.proyectosdn.repository;

import com.example.proyectosdn.entity.SesionActiva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SesionActivaRepository extends JpaRepository<SesionActiva, Integer> {
    @Query(nativeQuery = true,value = "select * from sesion_activa where username=?1 and active=1 order by id desc limit 1")
    SesionActiva obtenerUltimaSesionActiva(String username);
}