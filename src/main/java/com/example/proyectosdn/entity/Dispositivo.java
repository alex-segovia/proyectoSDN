package com.example.proyectosdn.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dispositivo")
@Getter
@Setter
@ToString(exclude = {"usuario", "servicioPorDispositivos"})
@EqualsAndHashCode(exclude = {"usuario", "servicioPorDispositivos"})
public class Dispositivo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @Size(min = 3, max = 100)
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Pattern(regexp = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$")
    @Column(name = "mac", nullable = false, length = 100)
    private String mac;

    @Column(name = "autenticado", nullable = false)
    private Integer autenticado;

    @Column(name = "estado", nullable = false)
    private Integer estado;

    @OneToMany(mappedBy = "dispositivo")
    private List<ServicioPorDispositivo> servicioPorDispositivos = new ArrayList<>();
}
