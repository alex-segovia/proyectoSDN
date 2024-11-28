package com.example.proyectosdn.repository;

import com.example.proyectosdn.entity.AtributoPorDispositivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AtributoPorDispositivoRepository extends JpaRepository<AtributoPorDispositivo, Integer> {
    List<AtributoPorDispositivo> findByDispositivoId(Integer dispositivoId);
    List<AtributoPorDispositivo> findByAtributoId(Integer atributoId);
}
