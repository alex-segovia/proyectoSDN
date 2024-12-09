package com.example.proyectosdn.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

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

    @NotBlank(message = "El attribute es requerido")
    @Column(name = "attribute", nullable = false, length = 64)
    private String attribute;

    @Column(name = "op", nullable = false, length = 2)
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

    @NotNull(message = "El estado es requerido")
    @Column(name = "estado", nullable = false)
    private Integer estado;

    @OneToMany(mappedBy = "usuario")
    private List<Dispositivo> dispositivos = new ArrayList<>();

    @OneToMany(mappedBy = "usuarioCreador")
    private List<Servicio> servicios = new ArrayList<>();
}
