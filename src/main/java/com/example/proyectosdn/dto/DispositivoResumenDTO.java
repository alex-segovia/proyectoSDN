package com.example.proyectosdn.dto;

import lombok.Data;

@Data
public class DispositivoResumenDTO {
    private Integer id;
    private String nombre;
    private String mac;
    private Integer estado;
}