package com.utp.hcm.repository;

import com.utp.hcm.model.HorarioTrabajo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HorarioTrabajoRepository extends JpaRepository<HorarioTrabajo, Integer> {
    @Modifying
    @Query("DELETE FROM HorarioDetalle hd WHERE hd.horarioTrabajo.id = :horarioId")
    void deleteByHorarioTrabajoId(@Param("horarioId") int horarioId);
}
