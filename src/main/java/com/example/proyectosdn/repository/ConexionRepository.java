package com.example.proyectosdn.repository;

import com.example.proyectosdn.entity.Conexion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface ConexionRepository extends JpaRepository<Conexion, Integer> {
    @Transactional
    @Modifying
    @Query(nativeQuery = true,value = "insert into conexion(id_dispositivo_origen, id_dispositivo_destino, id_servicio, puerto, fechaHoraInicio, timeout) values ((select id from dispositivo where mac=?1 and estado=1 order by id desc limit 1),(select id from dispositivo where mac=?2 and estado=1 order by id desc limit 1),?3,?4,?5,?6)")
    void registrarConexion(String macOrigen,String macDestino,Integer idVlan,Integer puerto,String fechaHoraInicio,Integer timeout);
}