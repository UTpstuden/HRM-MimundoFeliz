package com.utp.hcm.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import java.time.LocalTime;
import java.time.DayOfWeek;

@Entity
@Table(name = "horario_detalle")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HorarioDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "horario_trabajo_id")
    @JsonBackReference
    private HorarioTrabajo horarioTrabajo;

    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana", nullable = false)
    private DayOfWeek diaDeSemana;

    @Column(name = "hora_entrada", nullable = false)
    private LocalTime horaEntrada;

    @Column(name = "hora_salida", nullable = false)
    private LocalTime horaSalida;
}