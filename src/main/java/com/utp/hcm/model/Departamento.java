package com.utp.hcm.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "departamento")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Departamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "nombre_departamento", nullable = false, length = 100)
    private String nombreDepartamento;

    @Column(length = 255)
    private String descripcion;

    @Builder.Default
    private boolean estado = true;
}