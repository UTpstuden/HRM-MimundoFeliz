package com.utp.hcm.repository;

import com.utp.hcm.model.Departamento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DepartamentoRepository extends JpaRepository<Departamento, Integer> {
    List<Departamento> findByEstado(boolean estado);
}