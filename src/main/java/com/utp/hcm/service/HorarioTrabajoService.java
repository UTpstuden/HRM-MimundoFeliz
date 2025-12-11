package com.utp.hcm.service;

import com.utp.hcm.model.HorarioTrabajo;
import com.utp.hcm.model.HorarioDetalle;
import com.utp.hcm.repository.HorarioTrabajoRepository;
import com.utp.hcm.repository.HorarioDetalleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class HorarioTrabajoService {

    @Autowired
    private HorarioTrabajoRepository horarioTrabajoRepository;

    @Autowired
    private HorarioDetalleRepository horarioDetalleRepository;

    public List<HorarioTrabajo> findAll() {
        return horarioTrabajoRepository.findAll();
    }

    public Optional<HorarioTrabajo> findById(int id) {
        return horarioTrabajoRepository.findById(id);
    }

    @Transactional
    public HorarioTrabajo save(HorarioTrabajo horarioTrabajo) {
        // Si es una actualización, eliminar los detalles existentes primero
        if (horarioTrabajo.getId() != 0) {
            horarioDetalleRepository.deleteByHorarioTrabajoId(horarioTrabajo.getId());
        }
        
        // Asegurar que cada detalle tenga referencia al horario padre
        if (horarioTrabajo.getDetalles() != null) {
            for (HorarioDetalle detalle : horarioTrabajo.getDetalles()) {
                detalle.setHorarioTrabajo(horarioTrabajo);
            }
        }
        
        // SOLO guardar el horario padre - los detalles se guardarán por CASCADE
        return horarioTrabajoRepository.save(horarioTrabajo);
    }

    @Transactional
    public void deleteById(int id) {
        // Eliminar detalles primero
        horarioDetalleRepository.deleteByHorarioTrabajoId(id);
        // Luego eliminar el horario
        horarioTrabajoRepository.deleteById(id);
    }
}