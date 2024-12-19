package com.example.proyectosdn.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PuertoRequest {
    @NotNull(message = "El número de puerto es requerido")
    @Min(value = 1, message = "El puerto debe ser mayor a 0")
    @Max(value = 65535, message = "El puerto debe ser menor a 65536")
    private Integer numeroPuerto;
}
