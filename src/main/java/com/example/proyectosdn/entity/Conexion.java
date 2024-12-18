package com.example.proyectosdn.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "conexion")
public class Conexion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_dispositivo_origen", nullable = false)
    private Dispositivo dispositivoOrigen;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_dispositivo_destino", nullable = false)
    private Dispositivo dispositivoDestino;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_servicio", nullable = false)
    private Servicio servicio;

    @Size(max = 100)
    @NotNull
    @Column(name = "fechaHoraInicio", nullable = false, length = 100)
    private String fechaHoraInicio;

    @NotNull
    @Column(name = "timeout", nullable = false)
    private Integer timeout;

    @Column(name = "puerto")
    private Integer puerto;

}