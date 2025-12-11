package com.utp.hcm.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@Entity
@Table(name = "horario_trabajo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HorarioTrabajo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "nombre_turno", nullable = false, length = 100)
    private String nombreTurno;

    @OneToMany(mappedBy = "horarioTrabajo", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<HorarioDetalle> detalles;

    /**
     * Métodos transient para compatibilidad con templates frontend
     * Estos campos no se persisten en la BD pero se incluyen en el JSON
     */

    @Transient
    @JsonProperty("horaEntrada")
    public String getHoraEntrada() {
        if (detalles == null || detalles.isEmpty()) {
            return "No definido";
        }
        // Retorna la hora de entrada del primer detalle convertida a String
        return detalles.get(0).getHoraEntrada().toString();
    }

    @Transient
    @JsonProperty("horaSalida")
    public String getHoraSalida() {
        if (detalles == null || detalles.isEmpty()) {
            return "No definido";
        }
        // Retorna la hora de salida del primer detalle convertida a String
        return detalles.get(0).getHoraSalida().toString();
    }

    @Transient
    @JsonProperty("turno")
    public String getTurno() {
        // Alias para compatibilidad con templates que usan 'turno' en lugar de
        // 'nombreTurno'
        return this.nombreTurno;
    }
}