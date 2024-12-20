package com.example.proyectosdn.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "El username es requerido")
    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @Column(name = "attribute", length = 64)
    private String attribute;

    @Column(name = "op", length = 2)
    private String op = "=";  // Por defectoo

    @NotBlank(message = "El value es requerido")
    @Column(name = "value", nullable = false, length = 253)
    private String value;

    @NotBlank(message = "Los nombres son requeridos")
    @Column(name = "nombres", nullable = false, length = 45)
    private String nombres;

    @NotBlank(message = "El apellido paterno es requerido")
    @Column(name = "apellido_paterno", nullable = false, length = 45)
    private String apellidoPaterno;

    @NotBlank(message = "El apellido materno es requerido")
    @Column(name = "apellido_materno", nullable = false, length = 45)
    private String apellidoMaterno;

    @NotBlank(message = "El rol es requerido")
    @Column(name = "rol", nullable = false, length = 45)
    private String rol;

    @NotBlank(message = "El DNI es requerido")
    @Column(name = "dni", nullable = false, length = 8)
    private String dni;

    @Column(name = "estado")
    private Integer estado;

    @OneToMany(mappedBy = "usuario")
    private Set<Dispositivo> dispositivos = new HashSet<>();

    @OneToMany(mappedBy = "usuarioCreador")
    private Set<Servicio> servicios = new HashSet<>();
}
