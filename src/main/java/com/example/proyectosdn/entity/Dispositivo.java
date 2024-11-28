package com.example.proyectosdn.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "dispositivo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dispositivo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "mac", nullable = false, length = 100)
    private String mac;

    @Column(name = "autenticado", nullable = false)
    private Boolean autenticado;

    @Column(name = "estado", nullable = false)
    private Integer estado;

    @OneToMany(mappedBy = "dispositivo")
    private List<AtributoPorDispositivo> atributoPorDispositivos;
}
