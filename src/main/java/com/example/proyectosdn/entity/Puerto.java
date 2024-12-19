package com.example.proyectosdn.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "puerto")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Puerto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "puerto", nullable = false)
    private Integer numeroPuerto;

    @OneToMany(mappedBy = "puerto")
    private List<PuertoPorServicio> puertoPorServicios = new ArrayList<>();
}