package com.example.proyectosdn.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@Entity
@Table(name = "sesion_activa")
public class SesionActiva {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 100)
    @NotNull
    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @NotNull
    @Column(name = "session_start", nullable = false)
    private String sessionStart;

    @NotNull
    @Column(name = "last_activity", nullable = false)
    private String lastActivity;

    @NotNull
    @Column(name = "session_timeout", nullable = false)
    private Integer sessionTimeout;

    @ColumnDefault("1")
    @Column(name = "active")
    private Boolean active;

    @NotNull
    @Column(name = "ip")
    private String ip;

}