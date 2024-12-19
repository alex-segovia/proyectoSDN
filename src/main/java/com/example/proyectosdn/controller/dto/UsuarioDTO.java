package com.example.proyectosdn.controller.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UsuarioDTO {
    private Integer id;
    private String username;
    private String nombres;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String rol;
    private String dni;
    private Integer estado;
    private List<DispositivoResumenDTO> dispositivos = new ArrayList<>();
    private List<ServicioResumenDTO> serviciosCreados = new ArrayList<>();
}
