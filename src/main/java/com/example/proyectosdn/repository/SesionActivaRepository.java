package com.example.proyectosdn.repository;

import com.example.proyectosdn.entity.SesionActiva;
import com.example.proyectosdn.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface SesionActivaRepository extends JpaRepository<SesionActiva, Integer> {
    @Query(nativeQuery = true,value = "select * from sesion_activa where username=?1 and active=1 order by id desc limit 1")
    SesionActiva obtenerUltimaSesionActiva(String username);

    @Query(nativeQuery = true,value = "select sa.id from sesion_activa sa inner join usuario u on sa.username=u.username where sa.active=1 and u.username=?1 limit 1")
    Integer idSesionActivaPorUsuario(String username);

    @Query(nativeQuery = true,value = "select sa.id from sesion_activa sa where sa.active=1 and sa.ip=?1 limit 1")
    Integer idSesionActivaPorIp(String ip);

    @Transactional
    @Modifying
    @Query(nativeQuery = true,value = "delete from sesion_activa where active=1 and ip=?1")
    void eliminarSesionActivaPorIp(String ip);

    @Transactional
    @Modifying
    @Query(nativeQuery = true,value = "delete from sesion_activa where active=1 and username=?1")
    void eliminarSesionActivaPorUsername(String username);

    @Query(nativeQuery = true,value = "select u.username from sesion_activa sa inner join usuario u on sa.username=u.username where sa.active=1 and sa.ip=?1 limit 1")
    String usernameSesionActivaPorIp(String ip);

    @Query(nativeQuery = true,value = "select u.rol from sesion_activa sa inner join usuario u on sa.username=u.username where sa.active=1 and sa.ip=?1 limit 1")
    String userRolPorIp(String ip);

    @Query(nativeQuery = true,value = "select u.id from sesion_activa sa inner join usuario u on sa.username=u.username where sa.active=1 and sa.ip=?1 limit 1")
    Integer userIdPorIp(String ip);

    @Query("SELECT u FROM Usuario u " +
            "LEFT JOIN FETCH u.dispositivos " +
            "LEFT JOIN FETCH u.servicios " +
            "INNER JOIN SesionActiva sa ON sa.username = u.username " +
            "WHERE sa.active = true AND sa.ip = :ip")
    Usuario findUsuarioActivoByIp(@Param("ip") String ip);
}