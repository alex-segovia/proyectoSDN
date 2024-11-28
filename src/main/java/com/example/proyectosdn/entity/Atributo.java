package com.example.proyectosdn.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "atributo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Atributo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_usuario_creador")
    private Usuario usuarioCreador;

    @Column(name = "nombre", nullable = false, length = 45)
    private String nombre;

    @Column(name = "puerto")
    private Integer puerto;

    @Column(name = "estado", nullable = false)
    private Integer estado;

    @OneToMany(mappedBy = "atributo")
    private List<AtributoPorDispositivo> atributoPorDispositivos;
}