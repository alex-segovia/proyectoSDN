package com.example.proyectosdn.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "puerto_por_servicio")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PuertoPorServicio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "servicio_id", nullable = false)
    private Servicio servicio;

    @ManyToOne
    @JoinColumn(name = "puerto_id", nullable = false)
    private Puerto puerto;
}
