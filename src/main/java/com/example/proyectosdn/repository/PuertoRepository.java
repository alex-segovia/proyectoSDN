package com.example.proyectosdn.repository;

import com.example.proyectosdn.entity.Puerto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PuertoRepository extends JpaRepository<Puerto, Integer> {
    Optional<Puerto> findByNumeroPuerto(Integer numeroPuerto);
}
