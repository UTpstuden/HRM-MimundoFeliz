package com.utp.hcm.repository;

import com.utp.hcm.model.Cargo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CargoRepository extends JpaRepository<Cargo, Integer> {
    List<Cargo> findByEstado(boolean estado);
}