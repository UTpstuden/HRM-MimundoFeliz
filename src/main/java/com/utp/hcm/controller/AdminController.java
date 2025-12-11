package com.utp.hcm.controller;

import com.utp.hcm.model.Asistencia; // <-- CAMBIO: Importar Asistencia
import com.utp.hcm.model.Incidencia;
import com.utp.hcm.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List; // <-- CAMBIO: Importar List

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private AsistenciaService asistenciaService; // <-- CAMBIO: Inyectado

    @Autowired
    private IncidenciaService incidenciaService; // <-- CAMBIO: Inyectado

    @Autowired
    private CargoService cargoService;

    @Autowired
    private DepartamentoService departamentoService;

    @Autowired
    private HorarioTrabajoService horarioTrabajoService;

    @Autowired
    private EmpleadoService empleadoService;

    @Autowired
    private TipoContratoService tipoContratoService;

    @Autowired
    private TipoPensionService tipoPensionService;

    @Autowired
    private NetworkPolicyService networkPolicyService;

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/dashboard")
    public String mostrarAdminDashboard(Model model) {
        model.addAttribute("dashboardMetrics", dashboardService.obtenerMetricas());
        model.addAttribute("resumenRoles", dashboardService.obtenerComparativaPorCargo());
        return "dashboard-admin";
    }
    @GetMapping("/planilla")
    public String mostrarPlanillas(Model model) {
        model.addAttribute("empleados", empleadoService.findAll());
        model.addAttribute("cargos", cargoService.findAll());
        model.addAttribute("departamentos", departamentoService.findAll());
        model.addAttribute("horarios", horarioTrabajoService.findAll());
        model.addAttribute("tiposContrato", tipoContratoService.findAll());
        model.addAttribute("tiposPension", tipoPensionService.findAll());
        return "planilla";
    }

        // NUEVO MÉTODO PARA NÓMINAS
    @GetMapping("/nomina")
    public String mostrarNominas(Model model) {
        // Pasamos la lista de empleados para el selector
        model.addAttribute("empleados", empleadoService.findAll());
        return "nomina";
    }


    @GetMapping("/configuracion")
    public String mostrarConfiguracion(Model model) {
        model.addAttribute("cargos", cargoService.findAll());
        model.addAttribute("departamentos", departamentoService.findAll());
        model.addAttribute("horarios", horarioTrabajoService.findAll());
        model.addAttribute("redes", networkPolicyService.findAll());
        return "configuracion";
    }

    @GetMapping("/asistencia")
    public String mostrarAsistencia(Model model) {

        // --- INICIO DE LÓGICA PARA WIDGETS DE ASISTENCIA (CAMBIO) ---

        // 1. Obtenemos la lista de asistencia de HOY
        List<Asistencia> asistenciasHoy = asistenciaService.getAsistenciasHoy();

        // 2. Calculamos las estadísticas
        long presentesHoy = asistenciasHoy.size();

        long tardanzasHoy = asistenciasHoy.stream()
                .filter(a -> "TARDANZA".equals(a.getEstadoEntrada()))
                .count();

        long salidasAnticipadasHoy = asistenciasHoy.stream()
                .filter(a -> "SALIDA ANTICIPADA".equals(a.getEstadoSalida()))
                .count();

        // Nota: "Ausentes" (Faltas) solo se puede calcular con la Tarea Programada de la noche.
        // Por ahora, lo dejamos en 0.
        long ausentesHoy = 0;

        // 3. Pasamos todo al modelo
        model.addAttribute("asistenciasHoy", asistenciasHoy); // La tabla
        model.addAttribute("presentesHoy", presentesHoy); // Widget
        model.addAttribute("tardanzasHoy", tardanzasHoy); // Widget
        model.addAttribute("salidasAnticipadasHoy", salidasAnticipadasHoy); // Widget
        model.addAttribute("ausentesHoy", ausentesHoy); // Widget

        // --- FIN DE LÓGICA ---

        return "asistencia";
    }

    @GetMapping("/incidencias")
    public String mostrarIncidencias(Model model) {

        // --- INICIO DE LÓGICA PARA WIDGETS DE INCIDENCIAS (CAMBIO) ---

        List<Incidencia> incidencias = incidenciaService.findAll();

        long total = incidencias.size();

        long pendientes = incidencias.stream()
                .filter(i -> "PENDIENTE".equals(i.getEstado()))
                .count();

        long justificadas = incidencias.stream()
                .filter(i -> "JUSTIFICADA".equals(i.getEstado()))
                .count();

        // CAMBIO: Contamos "NO JUSTIFICADO" en lugar de "RECHAZADA"
        long injustificadas = incidencias.stream()
                .filter(i -> "NO JUSTIFICADO".equals(i.getEstado()))
                .count();

        model.addAttribute("incidencias", incidencias);
        model.addAttribute("totalIncidencias", total);
        model.addAttribute("pendientesIncidencias", pendientes);
        model.addAttribute("justificadasIncidencias", justificadas);
        model.addAttribute("injustificadasIncidencias", injustificadas); // Arreglado

        // --- FIN DE LÓGICA ---

        return "incidencias";
    }

    @GetMapping("/gestion-usuarios")
    public String gestionUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioService.findAll());
        return "gestion-usuarios";
    }
}
