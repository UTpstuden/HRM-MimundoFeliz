package com.utp.hcm.service;

import com.utp.hcm.model.Departamento;
import com.utp.hcm.repository.DepartamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class DepartamentoService {

    @Autowired
    private DepartamentoRepository departamentoRepository;

    public List<Departamento> findAll() {
        return departamentoRepository.findAll();
    }

    public List<Departamento> findByEstado(boolean estado) {
        return departamentoRepository.findByEstado(estado);
    }

    public List<Departamento> findActivos() {
        return departamentoRepository.findByEstado(true);
    }

    public Optional<Departamento> findById(int id) {
        return departamentoRepository.findById(id);
    }

    public Departamento save(Departamento departamento) {
        return departamentoRepository.save(departamento);
    }

    public void deleteById(int id) {
        departamentoRepository.deleteById(id);
    }

    public Departamento cambiarEstado(int id, boolean estado) {
        Optional<Departamento> departamentoOptional = departamentoRepository.findById(id);
        if (departamentoOptional.isPresent()) {
            Departamento departamento = departamentoOptional.get();
            departamento.setEstado(estado);
            return departamentoRepository.save(departamento);
        }
        return null;
    }
}