package com.example.proyectosdn.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "puerto_por_servicio")
@Getter
@Setter
@ToString(exclude = {"servicio", "puerto"})
@EqualsAndHashCode(exclude = {"servicio", "puerto"})
public class PuertoPorServicio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_servicio", nullable = false)
    private Servicio servicio;

    @ManyToOne
    @JoinColumn(name = "id_puerto", nullable = false)
    private Puerto puerto;
}
