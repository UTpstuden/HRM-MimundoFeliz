package com.utp.hcm.service;

import com.utp.hcm.model.TipoContrato;
import com.utp.hcm.repository.TipoContratoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class TipoContratoService {

    @Autowired
    private TipoContratoRepository tipoContratoRepository;

    public List<TipoContrato> findAll() {
        return tipoContratoRepository.findAll();
    }

    public Optional<TipoContrato> findById(int id) {
        return tipoContratoRepository.findById(id);
    }
}