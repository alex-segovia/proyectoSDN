package com.example.proyectosdn.repository;

import com.example.proyectosdn.entity.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ServicioRepository extends JpaRepository<Servicio, Integer> {
    List<Servicio> findByUsuarioCreadorId(Integer usuarioCreadorId);
    Optional<Servicio> findByNombre(String nombre);

    @Query(nativeQuery = true, value = "select s.id from servicio s inner join servicio_por_dispositivo spd1 on s.id = spd1.id_servicio inner join dispositivo d1 on spd1.id_dispositivo = d1.id inner join servicio_por_dispositivo spd2 on s.id=spd2.id_servicio inner join dispositivo d2 on spd2.id_dispositivo=d2.id where d1.mac=?1 and d2.mac=?2 and s.estado=1 and spd1.estado=1 and spd2.estado=1 order by s.id desc limit 1")
    Integer obtenerServicioEnComun(String macOrigen,String macDestino);
}