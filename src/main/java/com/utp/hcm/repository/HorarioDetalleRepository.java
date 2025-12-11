package com.utp.hcm.repository;

import com.utp.hcm.model.HorarioDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface HorarioDetalleRepository extends JpaRepository<HorarioDetalle, Integer> {
    
    @Modifying
    @Transactional
    @Query("DELETE FROM HorarioDetalle hd WHERE hd.horarioTrabajo.id = :horarioId")
    void deleteByHorarioTrabajoId(@Param("horarioId") int horarioId);
}