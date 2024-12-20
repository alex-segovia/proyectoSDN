package com.example.proyectosdn.dto.vistas;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DispositivoSelectDTO {
    private Integer id;
    private String nombre;
    private String mac;
}
