package com.utp.hcm.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cargo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cargo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "nombre_cargo", nullable = false, length = 100)
    private String nombreCargo;

    @Column(length = 255)
    private String descripcion;

    @Builder.Default
    private boolean estado = true;
}