package com.example.proyectosdn.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class DispositivoRequest {
    private Integer usuarioId;    // opcional
    private String nombre;        // opcional
    @Pattern(regexp = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$",
            message = "Formato MAC inválido")
    @NotNull(message = "MAC es obligatorio")
    private String mac;
    @NotNull(message = "autenticado es obligatorio")
    private Integer autenticado;
    @NotNull(message = "estado es obligatorio")
    private Integer estado;
}