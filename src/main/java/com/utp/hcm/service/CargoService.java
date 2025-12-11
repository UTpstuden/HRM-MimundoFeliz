package com.utp.hcm.service;

import com.utp.hcm.model.Cargo;
import com.utp.hcm.repository.CargoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CargoService {

    @Autowired
    private CargoRepository cargoRepository;

    public List<Cargo> findAll() {
        return cargoRepository.findAll();
    }

    public List<Cargo> findByEstado(boolean estado) {
        return cargoRepository.findByEstado(estado);
    }

    public List<Cargo> findActivos() {
        return cargoRepository.findByEstado(true);
    }

    public Optional<Cargo> findById(int id) {
        return cargoRepository.findById(id);
    }

    public Cargo save(Cargo cargo) {
        return cargoRepository.save(cargo);
    }

    public void deleteById(int id) {
        cargoRepository.deleteById(id);
    }

    public Cargo cambiarEstado(int id, boolean estado) {
        Optional<Cargo> cargoOptional = cargoRepository.findById(id);
        if (cargoOptional.isPresent()) {
            Cargo cargo = cargoOptional.get();
            cargo.setEstado(estado);
            return cargoRepository.save(cargo);
        }
        return null;
    }
}