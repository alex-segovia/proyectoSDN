package com.example.proyectosdn.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @Column(name = "nombres", nullable = false, length = 45)
    private String nombres;

    @Column(name = "apellido_paterno", nullable = false, length = 45)
    private String apellidoPaterno;

    @Column(name = "apellido_materno", nullable = false, length = 45)
    private String apellidoMaterno;

    @Column(name = "rol", nullable = false, length = 45)
    private String rol;

    @Column(name = "dni", nullable = false, length = 8)
    private String dni;

    @Column(name = "correo", nullable = false, length = 100)
    private String correo;

    @Column(name = "fecha_ultima_conexion", length = 45)
    private String fechaUltimaConexion;

    @Column(name = "estado", nullable = false)
    private Integer estado;

    @OneToMany(mappedBy = "usuario")
    private List<Dispositivo> dispositivos;

    @OneToMany(mappedBy = "usuarioCreador")
    private List<Atributo> atributos;
}
