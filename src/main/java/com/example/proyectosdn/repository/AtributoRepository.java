package com.example.proyectosdn.repository;

import com.example.proyectosdn.entity.Atributo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AtributoRepository extends JpaRepository<Atributo, Integer> {
    List<Atributo> findByUsuarioCreadorId(Integer usuarioCreadorId);
    Optional<Atributo> findByNombre(String nombre);
}
