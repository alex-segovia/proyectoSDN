package com.example.proyectosdn.dto;

import lombok.Data;

import java.util.List;

@Data
public class ServicioDTO {
    private Integer id;
    private String nombre;
    private Integer estado;
    private UsuarioDTO usuarioCreador;
    private List<Integer> puertos;
    private long cantidadDispositivos;

}