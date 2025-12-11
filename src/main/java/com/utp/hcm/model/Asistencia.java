package com.utp.hcm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Esta entidad representa un registro de asistencia de un empleado para un día
 * específico.
 */
@Entity
@Table(name = "asistencia")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asistencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * El empleado al que pertenece este registro de asistencia.
     * La carga es EAGER para que siempre tengamos el empleado al consultar la
     * asistencia.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado empleado;

    @Column(nullable = false)
    private LocalDate fecha;

    private LocalTime horaEntrada;

    private LocalTime horaSalida;

    /**
     * Almacena el estado de la marcación de entrada (ej. "PUNTUAL", "TARDANZA").
     */
    @Column(length = 20)
    private String estadoEntrada;

    /**
     * Almacena el estado de la marcación de salida (ej. "A TIEMPO", "SALIDA
     * TEMPRANA").
     * (Lo dejamos para lógica futura, por ahora puede ser null).
     */
    @Column(length = 20)
    private String estadoSalida;

    /**
     * Campo para notas o justificaciones futuras.
     */
    @Column
    private String observacion;

    /**
     * IP desde donde se registró la entrada del colaborador.
     */
    @Column(name = "ip_entrada", length = 45)
    private String ipEntrada;

    /**
     * IP desde donde se registró la salida del colaborador.
     */
    @Column(name = "ip_salida", length = 45)
    private String ipSalida;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "network_policy_id", nullable = true) // nullable=true si el registro de asistencia puede no
                                                             // tener una política
    private NetworkPolicy networkPolicy;

    /**
     * Define la relación inversa: una Asistencia puede tener múltiples Incidencias.
     * Si se borra una Asistencia, se borran sus incidencias (CascadeType.REMOVE).
     */
    @OneToMany(mappedBy = "asistencia", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonIgnore // Ignorar al serializar para evitar bucles
    private List<Incidencia> incidencias;
}
