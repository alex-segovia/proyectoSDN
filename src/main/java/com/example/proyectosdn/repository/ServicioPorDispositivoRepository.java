package com.example.proyectosdn.repository;

import com.example.proyectosdn.entity.ServicioPorDispositivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ServicioPorDispositivoRepository extends JpaRepository<ServicioPorDispositivo, Integer> {
    Optional<ServicioPorDispositivo> findByServicioIdAndDispositivoId(Integer servicioId, Integer dispositivoId);
    List<ServicioPorDispositivo> findByServicioId(Integer servicioId);
    List<ServicioPorDispositivo> findByDispositivoId(Integer dispositivoId);

    @Transactional
    @Modifying
    void deleteByServicioId(Integer servicioId);

    List<ServicioPorDispositivo> findByEstado(Integer estado);

    @Query(nativeQuery = true,value = "select * from servicio_por_dispositivo where estado=?1 and id_servicio=?2")
    List<ServicioPorDispositivo> listarSolcitudesDeServicios(Integer estado,Integer idServicio);

}