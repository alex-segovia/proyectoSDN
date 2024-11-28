package com.example.proyectosdn.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
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

    @NotBlank(message = "El nombre es requerido")
    @Size(min = 3, max = 45, message = "El nombre debe tener entre 3 y 45 caracteres")
    @Column(name = "nombre", nullable = false, length = 45)
    private String nombre;

    @Min(value = 1, message = "El puerto debe ser mayor a 0")
    @Max(value = 65535, message = "El puerto debe ser menor a 65536")
    @Column(name = "puerto")
    private Integer puerto;

    @NotNull(message = "El estado es requerido")
    @Column(name = "estado", nullable = false)
    private Integer estado;

    @ManyToOne
    @JoinColumn(name = "id_usuario_creador")
    private Usuario usuarioCreador;

    @OneToMany(mappedBy = "atributo")
    private List<AtributoPorDispositivo> atributoPorDispositivos = new ArrayList<>();
}