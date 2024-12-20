package com.example.proyectosdn.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "servicio_por_dispositivo")
@Getter
@Setter
@ToString(exclude = {"servicio", "dispositivo"})
@EqualsAndHashCode(exclude = {"servicio", "dispositivo"})
public class ServicioPorDispositivo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_servicio", nullable = false)
    private Servicio servicio;

    @ManyToOne
    @JoinColumn(name = "id_dispositivo", nullable = false)
    private Dispositivo dispositivo;

    @Column(name = "estado", nullable = false)
    private Integer estado;

    @Size(max = 200)
    @Column(name = "motivo", length = 200)
    private String motivo;
}