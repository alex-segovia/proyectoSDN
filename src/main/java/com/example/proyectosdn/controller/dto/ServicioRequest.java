package com.example.proyectosdn.controller.dto;

import lombok.Data;

import java.util.List;

@Data
public class ServicioRequest {
    private String nombre;
    private Integer estado;
    private Integer usuarioCreadorId;
    private List<Integer> puertos;
}
