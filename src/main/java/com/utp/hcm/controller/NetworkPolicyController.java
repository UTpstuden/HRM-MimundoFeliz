package com.utp.hcm.controller;

import com.utp.hcm.service.NetworkPolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/configuracion/redes")
public class NetworkPolicyController {

    @Autowired
    private NetworkPolicyService networkPolicyService;

    @PostMapping
    public String crearRed(
            @RequestParam("nombre") String nombre,
            @RequestParam("prefijo") String prefijo,
            @RequestParam(value = "descripcion", required = false) String descripcion
    ) {
        networkPolicyService.create(nombre, prefijo, descripcion);
        return "redirect:/admin/configuracion?tab=redes";
    }

    @PostMapping("/{id}/estado")
    public String actualizarEstado(
            @PathVariable Long id,
            @RequestParam("activo") boolean activo
    ) {
        networkPolicyService.updateEstado(id, activo);
        return "redirect:/admin/configuracion?tab=redes";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminarRed(@PathVariable Long id) {
        networkPolicyService.delete(id);
        return "redirect:/admin/configuracion?tab=redes";
    }
}
