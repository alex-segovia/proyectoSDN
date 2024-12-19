package com.example.proyectosdn.controller.dto;

import lombok.Data;

import java.util.List;

@Data
public class DispositivoDTO {
    private Integer id;
    private UsuarioResumenDTO usuario;
    private String nombre;
    private String mac;
    private Integer autenticado;
    private Integer estado;
    private List<ServicioResumenDTO> servicios;

    @Data
    public static class UsuarioResumenDTO {
        private Integer id;
        private String username;
        private String nombres;
        private String apellidoPaterno;
    }

    @Data
    public static class ServicioResumenDTO {
        private Integer id;
        private String nombre;
        private Integer estado;
        private String motivo;  // del ServicioPorDispositivo
    }
}