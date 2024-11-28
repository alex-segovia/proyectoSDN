package com.example.proyectosdn.repository;

import com.example.proyectosdn.entity.Controlador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ControladorRepository extends JpaRepository<Controlador, Integer> {
    Optional<Controlador> findByIp(String ip);
}
