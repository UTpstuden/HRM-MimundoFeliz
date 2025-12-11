package com.utp.hcm.service;

import com.utp.hcm.model.NetworkPolicy;
import com.utp.hcm.repository.NetworkPolicyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NetworkPolicyService {

    @Autowired
    private NetworkPolicyRepository networkPolicyRepository;

    public List<NetworkPolicy> findAll() {
        return networkPolicyRepository.findAll().stream()
                .sorted(Comparator.comparing(NetworkPolicy::getNombre))
                .collect(Collectors.toList());
    }

    public List<String> getActivePrefixes() {
        return networkPolicyRepository.findByActivoTrue()
                .stream()
                .map(NetworkPolicy::getPrefijo)
                .map(this::normalizePrefix)
                .collect(Collectors.toList());
    }

    public List<NetworkPolicy> findAllActive() {
        return networkPolicyRepository.findByActivoTrue();
    }

    @Transactional
    public void create(String nombre, String prefijo, String descripcion) {
        String normalizedPrefix = normalizePrefix(prefijo);
        if (normalizedPrefix.isBlank()) {
            throw new IllegalArgumentException("El prefijo de red no puede estar vacío");
        }
        NetworkPolicy policy = NetworkPolicy.builder()
                .nombre(nombre.trim())
                .prefijo(normalizedPrefix)
                .descripcion(descripcion == null ? null : descripcion.trim())
                .activo(true)
                .build();
        networkPolicyRepository.save(policy);
    }

    @Transactional
    public void updateEstado(Long id, boolean activo) {
        networkPolicyRepository.findById(id).ifPresent(policy -> {
            policy.setActivo(activo);
            networkPolicyRepository.save(policy);
        });
    }

    @Transactional
    public void delete(Long id) {
        networkPolicyRepository.deleteById(id);
    }

    private String normalizePrefix(String raw) {
        return raw == null ? "" : raw.trim();
    }
}
