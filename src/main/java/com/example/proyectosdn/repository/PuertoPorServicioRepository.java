package com.example.proyectosdn.repository;

import com.example.proyectosdn.entity.PuertoPorServicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PuertoPorServicioRepository extends JpaRepository<PuertoPorServicio, Integer> {
    List<PuertoPorServicio> findByServicioId(Integer servicioId);
    List<PuertoPorServicio> findByPuertoId(Integer puertoId);
}