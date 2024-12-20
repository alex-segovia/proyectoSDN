package com.example.proyectosdn.repository;

import com.example.proyectosdn.entity.ServicioPorDispositivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ServicioPorDispositivoRepository extends JpaRepository<ServicioPorDispositivo, Integer> {
    Optional<ServicioPorDispositivo> findByServicioIdAndDispositivoId(Integer servicioId, Integer dispositivoId);
    List<ServicioPorDispositivo> findByServicioId(Integer servicioId);
    List<ServicioPorDispositivo> findByDispositivoId(Integer dispositivoId);
    void deleteByServicioId(Integer servicioId);

}