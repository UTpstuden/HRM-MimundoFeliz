package com.utp.hcm.repository;

import com.utp.hcm.model.NetworkPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NetworkPolicyRepository extends JpaRepository<NetworkPolicy, Long> {
    List<NetworkPolicy> findByActivoTrue();
}
