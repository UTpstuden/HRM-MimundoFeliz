package com.utp.hcm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaController {

    @RequestMapping(value = { "/", "/app/**", "/login", "/recuperar" })
    public String index() {
        return "forward:/index.html";
    }
}
