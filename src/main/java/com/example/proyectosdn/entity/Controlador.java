package com.example.proyectosdn.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "controlador")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Controlador {
    @Id
    private Integer id;

    @Column(name = "ip", nullable = false, length = 45)
    private String ip;

    @Column(name = "nombre", nullable = false, length = 45)
    private String nombre;
}