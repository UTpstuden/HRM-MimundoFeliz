package com.utp.hcm.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tipo_pension")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoPension {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "nombre", nullable = false, unique = true, length = 50)
    private String nombre;
}
