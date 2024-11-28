package com.example.proyectosdn.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "atributo_por_dispositivo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AtributoPorDispositivo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_atributo", nullable = false)
    private Atributo atributo;

    @ManyToOne
    @JoinColumn(name = "id_dispositivo", nullable = false)
    private Dispositivo dispositivo;

    @Column(name = "estado", nullable = false)
    private Integer estado;

    @Column(name = "motivo", length = 200)
    private String motivo;
}
