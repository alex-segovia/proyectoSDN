package com.example.proyectosdn.repository;

import com.example.proyectosdn.entity.ServicioPorDispositivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ServicioPorDispositivoRepository extends JpaRepository<ServicioPorDispositivo, Integer> {
    List<ServicioPorDispositivo> findByDispositivoId(Integer dispositivoId);
    List<ServicioPorDispositivo> findByAtributoId(Integer atributoId);

}