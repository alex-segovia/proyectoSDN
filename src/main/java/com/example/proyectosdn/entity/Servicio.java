package com.example.proyectosdn.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "servicio")
@Getter
@Setter
@ToString(exclude = {"usuarioCreador", "servicioPorDispositivos", "puertoPorServicios"})
@EqualsAndHashCode(exclude = {"usuarioCreador", "servicioPorDispositivos", "puertoPorServicios"})
public class Servicio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_usuario_creador")
    private Usuario usuarioCreador;

    @Column(name = "nombre", nullable = false, length = 45)
    private String nombre;

    @Column(name = "estado", nullable = false)
    private Integer estado;

    @OneToMany(mappedBy = "servicio")
    private List<ServicioPorDispositivo> servicioPorDispositivos = new ArrayList<>();

    @OneToMany(mappedBy = "servicio")
    private List<PuertoPorServicio> puertoPorServicios = new ArrayList<>();
}