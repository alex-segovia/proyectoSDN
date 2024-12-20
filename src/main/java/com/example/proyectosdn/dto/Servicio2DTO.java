package com.example.proyectosdn.dto;

import com.example.proyectosdn.entity.Dispositivo;
import com.example.proyectosdn.entity.Puerto;
import com.example.proyectosdn.entity.ServicioPorDispositivo;
import com.example.proyectosdn.entity.Usuario;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Servicio2DTO {
    private Integer id;
    private String nombre;
    private Integer estado;
    private UsuarioDTO usuarioCreador;
    private List<Integer> puertos;
    private List<ServicioPorDispositivo> servicioPorDispositivo;
}
