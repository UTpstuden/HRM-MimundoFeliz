package com.utp.hcm.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "nomina")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Nomina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empleado_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private Empleado empleado;

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDate fechaGeneracion;

    // Sueldo base efectivo para el mes (proporcional si aplica)
    @Column(name = "sueldo_base", nullable = false)
    private Double sueldoBase;

    // Valores de referencia
    @Column(name = "valor_dia")
    private Double valorDia;

    @Column(name = "valor_hora")
    private Double valorHora;

    // Incidencias / detalles
    @Column(name = "dias_trabajados")
    private Integer diasTrabajados;

    @Column(name = "dias_falta")
    private Integer diasFalta;

    @Column(name = "minutos_tardanza")
    private Integer minutosTardanza;

    @Column(name = "horas_tardanza")
    private Double horasTardanza;

    @Column(name = "horas_incidencias")
    private Double horasIncidencias;

    // Descuentos detalle
    @Column(name = "descuento_tardanza")
    private Double descuentoTardanza;

    @Column(name = "descuento_faltas")
    private Double descuentoFaltas;

    @Column(name = "descuento_incidencias")
    private Double descuentoIncidencias;

    // Pensiones y asignaciones
    @Column(name = "descuento_afp")
    private Double descuentoAFP;

    @Column(name = "descuento_onp")
    private Double descuentoONP;

    @Column(name = "asignacion_familiar")
    private Double asignacionFamiliar;

    // Totales
    @Column(name = "total_neto", nullable = false)
    private Double totalNeto;

    @Column(name = "mes_periodo", nullable = false)
    private String mesPeriodo;
}
