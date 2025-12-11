package com.utp.hcm.controller;
/*
 * import org.springframework.beans.factory.annotation.Autowired;
 * import org.springframework.stereotype.Controller;
 * import org.springframework.ui.Model;
 * import org.springframework.web.bind.annotation.GetMapping;
 * import org.springframework.web.bind.annotation.PostMapping;
 * import org.springframework.web.bind.annotation.RequestParam;
 * 
 * import com.utp.hcm.service.RecuperarService;
 * 
 * @Controller
 * public class LoginController {
 * 
 * @GetMapping("/login")
 * public String mostrarLogin() {
 * return "login"; // Esto le dice a Thymeleaf que muestre la página login.html
 * }
 * 
 * @Autowired
 * private RecuperarService recuperarService;
 * 
 * @GetMapping("/recuperar")
 * public String mostrarFormulario(Model model) {
 * model.addAttribute("mensaje", null);
 * model.addAttribute("error", null);
 * return "recuperar";
 * }
 * 
 * @PostMapping("/recuperar")
 * public String procesarRecuperacion(@RequestParam("email") String email, Model
 * model) {
 * 
 * boolean exito = recuperarService.recuperarPassword(email);
 * 
 * if (exito) {
 * model.addAttribute("mensaje",
 * "Se ha enviado una nueva contraseña a tu correo institucional.");
 * model.addAttribute("error", null);
 * } else {
 * model.addAttribute("mensaje", null);
 * model.addAttribute("error",
 * "El correo ingresado no está registrado en el sistema.");
 * }
 * 
 * return "recuperar";
 * }
 * }
 */