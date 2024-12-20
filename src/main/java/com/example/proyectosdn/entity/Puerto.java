package com.example.proyectosdn.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "puerto")
@Getter
@Setter
@ToString(exclude = "puertoPorServicios")
@EqualsAndHashCode(exclude = "puertoPorServicios")
public class Puerto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "puerto", nullable = false)
    private Integer numeroPuerto;

    @OneToMany(mappedBy = "puerto")
    private List<PuertoPorServicio> puertoPorServicios = new ArrayList<>();
}