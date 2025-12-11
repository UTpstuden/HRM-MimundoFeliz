package com.utp.hcm.repository;

import com.utp.hcm.model.Nomina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface NominaRepository extends JpaRepository<Nomina, Long> {

    @Query("SELECT DISTINCT n FROM Nomina n JOIN FETCH n.empleado WHERE n.empleado.id = :empleadoId")
    List<Nomina> findByEmpleadoId(@Param("empleadoId") Integer empleadoId);

    @Query("SELECT DISTINCT n FROM Nomina n " +
            "JOIN FETCH n.empleado e " +
            "JOIN FETCH e.cargo " +
            "JOIN FETCH e.tipoContrato " +
            "LEFT JOIN FETCH e.tipoPension")
    List<Nomina> findAllWithDetails();

    @Query("SELECT n FROM Nomina n " +
            "JOIN FETCH n.empleado e " +
            "JOIN FETCH e.cargo " +
            "JOIN FETCH e.tipoContrato " +
            "LEFT JOIN FETCH e.tipoPension " +
            "WHERE n.id = :id")
    Optional<Nomina> findByIdWithDetails(@Param("id") Long id);

    @Modifying
    @Query("DELETE FROM Nomina n WHERE n.empleado.id = :empleadoId")
    void deleteAllByEmpleadoId(@Param("empleadoId") int empleadoId);
}
