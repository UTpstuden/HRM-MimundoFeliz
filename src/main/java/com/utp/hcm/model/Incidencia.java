package com.utp.hcm.model;

import com.fasterxml.jackson.annotation.JsonIgnore; // <-- IMPORTACIÓN AÑADIDA
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "incidencia")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Incidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado empleado;

    /**
     * El registro de asistencia que originó esta incidencia (si aplica).
     * Puede ser nulo si es una "AUSENCIA" (que implementaremos a futuro).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asistencia_id")
    @JsonIgnore // <-- CAMBIO AÑADIDO
    private Asistencia asistencia;

    @Column(nullable = false)
    private LocalDate fecha;

    /**
     * Tipo de incidencia: TARDANZA, SALIDA_ANTICIPADA, FALTA
     */
    @Column(nullable = false, length = 50)
    private String tipo;

    /**
     * Estado de la incidencia: PENDIENTE, JUSTIFICADA, NO JUSTIFICADO
     */
    @Column(nullable = false, length = 50)
    private String estado;

    /**
     * Notas del admin (ej. "Justificado por cita médica").
     */
    @Column
    private String observacionAdmin;

    /**
     * Almacena los minutos asociados a la incidencia (ej. 15 min de tardanza).
     * Es nulo si no aplica (ej. "FALTA", que es el día completo).
     * ESTE CAMPO ES CLAVE PARA NÓMINA.
     */
    @Column
    private Integer minutosIncidencia;
}