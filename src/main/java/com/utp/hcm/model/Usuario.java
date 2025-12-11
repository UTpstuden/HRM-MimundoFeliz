package com.utp.hcm.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idUsuario;

    private String correoInstitucional;
    private String password;

    // NUEVO CAMPO PARA GUARDAR EL ROL
    private String rol;

    @OneToOne
    @JoinColumn(name = "id_empleado")
    private Empleado empleado;
}