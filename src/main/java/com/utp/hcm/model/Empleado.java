package com.utp.hcm.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;

@Entity
@Table(name = "empleado")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Empleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(nullable = false, unique = true, length = 8)
    private String dni;

    @Column(nullable = false, unique = true, length = 150)
    private String correo;

    @Column(name = "sueldo_base", nullable = false)
    private Double sueldoBase;

    @Column(name = "fecha_contratacion", nullable = false)
    private LocalDate fechaContratacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cargo_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Cargo cargo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departamento_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Departamento departamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "horario_trabajo_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "detalles"})
    private HorarioTrabajo horarioTrabajo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_contrato_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private TipoContrato tipoContrato;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_pension_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private TipoPension tipoPension;

    @Column(name = "tiene_hijos_menores")
    private Boolean tieneHijosMenores;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] foto;
}