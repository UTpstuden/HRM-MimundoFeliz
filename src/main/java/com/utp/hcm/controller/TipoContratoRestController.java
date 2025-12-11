package com.utp.hcm.controller;

import com.utp.hcm.model.TipoContrato;
import com.utp.hcm.service.TipoContratoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/tipos-contrato")
public class TipoContratoRestController {

    @Autowired
    private TipoContratoService tipoContratoService;

    @GetMapping
    public List<TipoContrato> getAllTiposContrato() {
        return tipoContratoService.findAll();
    }
}