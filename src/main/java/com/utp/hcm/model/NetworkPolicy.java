package com.utp.hcm.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "network_policy")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NetworkPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "nombre", length = 80, nullable = false)
    private String nombre;

    @Column(name = "prefijo", length = 50)
    private String prefijo;
}