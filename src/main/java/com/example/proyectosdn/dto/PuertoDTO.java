package com.example.proyectosdn.dto;

import lombok.Data;

import java.util.List;

@Data
public class PuertoDTO {
    private Integer id;
    private Integer numeroPuerto;
    private int cantidadServicios;  // Número de servicios que usan este puerto
    private List<ServicioResumenDTO> servicios; // Resumen xd

    @Data
    public static class ServicioResumenDTO {
        private Integer id;
        private String nombre;
        private Integer estado;
    }
}
