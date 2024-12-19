package com.example.proyectosdn.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UsuarioRequest {
    @NotBlank(message = "El username es requerido")
    private String username;

    private String password; // Solo para creación

    @NotBlank(message = "Los nombres son requeridos")
    private String nombres;

    @NotBlank(message = "El apellido paterno es requerido")
    private String apellidoPaterno;

    @NotBlank(message = "El apellido materno es requerido")
    private String apellidoMaterno;

    @NotBlank(message = "El rol es requerido")
    private String rol;

    @NotBlank(message = "El DNI es requerido")
    @Size(min = 8, max = 8, message = "El DNI debe tener 8 caracteres")
    private String dni;

    private Integer estado;
}
