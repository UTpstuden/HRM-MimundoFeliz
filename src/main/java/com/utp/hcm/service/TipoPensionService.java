package com.utp.hcm.service;

import com.utp.hcm.model.TipoPension;
import com.utp.hcm.repository.TipoPensionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class TipoPensionService {

    @Autowired
    private TipoPensionRepository tipoPensionRepository;

    public List<TipoPension> findAll() {
        return tipoPensionRepository.findAll();
    }

    public Optional<TipoPension> findById(int id) {
        return tipoPensionRepository.findById(id);
    }
}