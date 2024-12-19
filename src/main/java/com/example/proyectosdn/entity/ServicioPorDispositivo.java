package com.example.proyectosdn.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "servicio_por_dispositivo")
public class ServicioPorDispositivo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_servicio", nullable = false)
    private Servicio servicio;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_dispositivo", nullable = false)
    private Dispositivo dispositivo;

    @NotNull
    @Column(name = "estado", nullable = false)
    private Integer estado;

    @Size(max = 200)
    @Column(name = "motivo", length = 200)
    private String motivo;

}