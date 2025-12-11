package com.utp.hcm.controller;

import com.utp.hcm.model.TipoPension;
import com.utp.hcm.service.TipoPensionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/tipos-pension")
public class TipoPensionRestController {

    @Autowired
    private TipoPensionService tipoPensionService;

    @GetMapping
    public List<TipoPension> getAllTiposPension() {
        return tipoPensionService.findAll();
    }
}
