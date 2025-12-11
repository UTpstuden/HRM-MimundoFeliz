package com.utp.hcm.config;

import java.text.Normalizer;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.utp.hcm.model.Asistencia;
import com.utp.hcm.model.Cargo;
import com.utp.hcm.model.Departamento;
import com.utp.hcm.model.Empleado;
import com.utp.hcm.model.HorarioDetalle;
import com.utp.hcm.model.HorarioTrabajo;
import com.utp.hcm.model.Incidencia;
import com.utp.hcm.model.NetworkPolicy;
import com.utp.hcm.model.TipoContrato;
import com.utp.hcm.model.TipoPension;
import com.utp.hcm.model.Usuario;
import com.utp.hcm.repository.AsistenciaRepository;
import com.utp.hcm.repository.CargoRepository;
import com.utp.hcm.repository.DepartamentoRepository;
import com.utp.hcm.repository.EmpleadoRepository;
import com.utp.hcm.repository.HorarioTrabajoRepository;
import com.utp.hcm.repository.IncidenciaRepository;
import com.utp.hcm.repository.NetworkPolicyRepository;
import com.utp.hcm.repository.TipoContratoRepository;
import com.utp.hcm.repository.TipoPensionRepository;
import com.utp.hcm.repository.UsuarioRepository;

@Configuration
public class DataInitializer {

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Bean
    CommandLineRunner initData(
            TipoPensionRepository tipoPensionRepository,
            TipoContratoRepository tipoContratoRepository,
            CargoRepository cargoRepository,
            DepartamentoRepository departamentoRepository,
            HorarioTrabajoRepository horarioTrabajoRepository,
            EmpleadoRepository empleadoRepository,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            NetworkPolicyRepository networkPolicyRepository,
            AsistenciaRepository asistenciaRepository,
            IncidenciaRepository incidenciaRepository) {
        return args -> {
            seedTipoPension(tipoPensionRepository);
            seedTipoContrato(tipoContratoRepository);
            seedCargos(cargoRepository);
            seedDepartamentos(departamentoRepository);
            List<HorarioTrabajo> horarios = seedHorarios(horarioTrabajoRepository);
            seedNetworkPolicies(networkPolicyRepository);

            List<Empleado> nuevos = new ArrayList<>();
            if (empleadoRepository.count() == 0) {
                nuevos = seedEmpleados(
                        empleadoRepository,
                        usuarioRepository,
                        cargoRepository.findAll(),
                        departamentoRepository.findAll(),
                        horarios,
                        tipoContratoRepository.findAll(),
                        tipoPensionRepository.findAll(),
                        passwordEncoder);
            }

            seedAsistenciasYNovedades(nuevos, asistenciaRepository, incidenciaRepository);
            seedAdminUser(usuarioRepository, passwordEncoder);
        };
    }

    private void seedTipoPension(TipoPensionRepository repository) {
        if (repository.count() == 0) {
            repository.saveAll(List.of(
                    new TipoPension(0, "AFP"),
                    new TipoPension(0, "ONP")));
        }
    }

    private void seedTipoContrato(TipoContratoRepository repository) {
        if (repository.count() == 0) {
            repository.saveAll(List.of(
                    new TipoContrato(0, "Tiempo Completo"),
                    new TipoContrato(0, "Medio Tiempo"),
                    new TipoContrato(0, "Contratista"),
                    new TipoContrato(0, "Suplente")));
        }
    }

    private void seedCargos(CargoRepository repository) {
        if (repository.count() == 0) {
            repository.saveAll(List.of(
                    Cargo.builder().nombreCargo("Docente Inicial").descripcion("Profesores del nivel inicial")
                            .estado(true).build(),
                    Cargo.builder().nombreCargo("Docente Primaria").descripcion("Especialistas de primaria")
                            .estado(true).build(),
                    Cargo.builder().nombreCargo("Docente Secundaria").descripcion("Especialistas de secundaria")
                            .estado(true).build(),
                    Cargo.builder().nombreCargo("Coordinador Académico").descripcion("Coordina el plan curricular")
                            .estado(true).build(),
                    Cargo.builder().nombreCargo("Administrativo").descripcion("Gestión administrativa").estado(true)
                            .build(),
                    Cargo.builder().nombreCargo("Soporte TI").descripcion("Mesa de ayuda tecnológica").estado(true)
                            .build()));
        }
    }

    private void seedDepartamentos(DepartamentoRepository repository) {
        if (repository.count() == 0) {
            repository.saveAll(List.of(
                    Departamento.builder().nombreDepartamento("Inicial").descripcion("Educación inicial").estado(true)
                            .build(),
                    Departamento.builder().nombreDepartamento("Primaria").descripcion("Educación primaria").estado(true)
                            .build(),
                    Departamento.builder().nombreDepartamento("Secundaria").descripcion("Educación secundaria")
                            .estado(true).build(),
                    Departamento.builder().nombreDepartamento("Administración").descripcion("Gestiones administrativas")
                            .estado(true).build(),
                    Departamento.builder().nombreDepartamento("Tecnología").descripcion("Soporte y TI").estado(true)
                            .build()));
        }
    }

    private List<HorarioTrabajo> seedHorarios(HorarioTrabajoRepository repository) {
        if (repository.count() == 0) {
            HorarioTrabajo manana = crearHorario("Turno Mañana", LocalTime.of(7, 30), LocalTime.of(15, 0));
            HorarioTrabajo tarde = crearHorario("Turno Tarde", LocalTime.of(12, 30), LocalTime.of(20, 0));
            HorarioTrabajo administrativo = crearHorario("Turno Administrativo", LocalTime.of(8, 30),
                    LocalTime.of(17, 30));
            repository.saveAll(Arrays.asList(manana, tarde, administrativo));
        }
        return repository.findAll();
    }

    private HorarioTrabajo crearHorario(String nombre, LocalTime entrada, LocalTime salida) {
        HorarioTrabajo horario = new HorarioTrabajo();
        horario.setNombreTurno(nombre);
        List<HorarioDetalle> detalles = new ArrayList<>();
        for (DayOfWeek dia : List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY)) {
            detalles.add(HorarioDetalle.builder()
                    .horarioTrabajo(horario)
                    .diaDeSemana(dia)
                    .horaEntrada(entrada)
                    .horaSalida(salida)
                    .build());
        }
        horario.setDetalles(detalles);
        return horario;
    }

    private void seedNetworkPolicies(NetworkPolicyRepository repository) {
        if (repository.count() == 0) {
            repository.saveAll(List.of(
                    NetworkPolicy.builder()
                            .nombre("Campus Principal")
                            .prefijo("192.168.10.")
                            .descripcion("Red WiFi docentes y administrativos")
                            .activo(true)
                            .build(),
                    NetworkPolicy.builder()
                            .nombre("Laboratorio de Computo")
                            .prefijo("10.10.5.")
                            .descripcion("Red cableada de laboratorios")
                            .activo(true)
                            .build()));
        }
    }

    private List<Empleado> seedEmpleados(
            EmpleadoRepository empleadoRepository,
            UsuarioRepository usuarioRepository,
            List<Cargo> cargos,
            List<Departamento> departamentos,
            List<HorarioTrabajo> horarios,
            List<TipoContrato> contratos,
            List<TipoPension> pensiones,
            PasswordEncoder passwordEncoder) {
        int objetivo = 20;
        long existentes = empleadoRepository.count();
        if (existentes >= objetivo) {
            return List.of();
        }

        int faltantes = (int) (objetivo - existentes);
        Random random = new Random(42);
        String[] nombres = { "Camila", "Valeria", "María", "Daniel", "Andrés", "Lucía", "Montserrat", "Sofía", "Mateo",
                "Sebastián", "Gabriela", "Renzo", "Rafael", "Natalia", "Ariana" };
        String[] apellidos = { "Flores", "Becerra", "Paredes", "García", "Salazar", "Ramírez", "Costa", "Mejía",
                "Quispe", "Perales", "Vera", "Campos", "Rivas", "Millan", "Cuevas" };

        List<Empleado> nuevos = new ArrayList<>();
        for (int i = 0; i < faltantes; i++) {
            String nombre = nombres[random.nextInt(nombres.length)];
            String apellido = apellidos[random.nextInt(apellidos.length)];
            Cargo cargo = cargos.get(random.nextInt(cargos.size()));
            Departamento departamento = departamentos.get(random.nextInt(departamentos.size()));
            HorarioTrabajo horario = horarios.get(random.nextInt(horarios.size()));
            TipoContrato contrato = contratos.get(random.nextInt(contratos.size()));
            TipoPension pension = pensiones.get(random.nextInt(pensiones.size()));

            String dni = String.format("%08d", 40000000 + existentes + i + 1);
            String correoPersonal = normalizar(nombre) + "." + normalizar(apellido) + (i + 1) + "@familiafeliz.com";

            Empleado empleado = Empleado.builder()
                    .nombre(nombre)
                    .apellido(apellido)
                    .dni(dni)
                    .correo(correoPersonal)
                    .sueldoBase(2400d + random.nextInt(1600))
                    .fechaContratacion(
                            LocalDate.now().minusMonths(random.nextInt(36) + 6L).minusDays(random.nextInt(20)))
                    .cargo(cargo)
                    .departamento(departamento)
                    .horarioTrabajo(horario)
                    .tipoContrato(contrato)
                    .tipoPension(pension)
                    .tieneHijosMenores(random.nextBoolean())
                    .build();

            Empleado guardado = empleadoRepository.save(empleado);
            nuevos.add(guardado);

            Usuario usuario = new Usuario();
            usuario.setEmpleado(guardado);
            usuario.setCorreoInstitucional(generarCorreoInstitucional(nombre, apellido, usuarioRepository));
            usuario.setPassword(passwordEncoder.encode("empleado123"));
            usuario.setRol("ROLE_EMPLEADO");
            usuarioRepository.save(usuario);
        }
        return nuevos;
    }

    private void seedAsistenciasYNovedades(List<Empleado> empleados, AsistenciaRepository asistenciaRepository,
            IncidenciaRepository incidenciaRepository) {
        if (empleados.isEmpty() || asistenciaRepository.count() > 0) {
            return;
        }
        Random random = new Random(24);
        LocalDate hoy = LocalDate.now();

        for (Empleado empleado : empleados) {
            boolean marcaAsistencia = random.nextDouble() < 0.8;
            if (marcaAsistencia) {
                LocalTime entradaProgramada = LocalTime.of(7, 45);
                int offsetEntrada = random.nextInt(50) - 10;
                LocalTime horaEntrada = entradaProgramada.plusMinutes(offsetEntrada);
                boolean tardanza = offsetEntrada > 5;

                LocalTime salidaProgramada = LocalTime.of(15, 0);
                int offsetSalida = random.nextInt(40) - 10;
                LocalTime horaSalida = salidaProgramada.plusMinutes(offsetSalida);
                boolean salidaAnticipada = offsetSalida < -5;

                Asistencia asistencia = Asistencia.builder()
                        .empleado(empleado)
                        .fecha(hoy)
                        .horaEntrada(horaEntrada)
                        .horaSalida(horaSalida)
                        .estadoEntrada(tardanza ? "TARDANZA" : "PUNTUAL")
                        .estadoSalida(salidaAnticipada ? "SALIDA ANTICIPADA" : "SALIDA REGISTRADA")
                        .ipEntrada("192.168.10." + (10 + random.nextInt(50)))
                        .ipSalida("192.168.10." + (60 + random.nextInt(50)))
                        .build();

                Asistencia guardada = asistenciaRepository.save(asistencia);

                if (tardanza) {
                    int minutos = (int) Math.max(Duration.between(entradaProgramada, horaEntrada).toMinutes(), 1);
                    incidenciaRepository.save(Incidencia.builder()
                            .empleado(empleado)
                            .asistencia(guardada)
                            .fecha(hoy)
                            .tipo("TARDANZA")
                            .estado(random.nextBoolean() ? "PENDIENTE" : "JUSTIFICADA")
                            .minutosIncidencia(minutos)
                            .build());
                } else if (salidaAnticipada && random.nextBoolean()) {
                    incidenciaRepository.save(Incidencia.builder()
                            .empleado(empleado)
                            .asistencia(guardada)
                            .fecha(hoy)
                            .tipo("SALIDA ANTICIPADA")
                            .estado(random.nextBoolean() ? "PENDIENTE" : "NO JUSTIFICADO")
                            .minutosIncidencia(
                                    (int) Math.abs(Duration.between(horaSalida, salidaProgramada).toMinutes()))
                            .build());
                }
            } else {
                incidenciaRepository.save(Incidencia.builder()
                        .empleado(empleado)
                        .fecha(hoy)
                        .tipo("FALTA")
                        .estado(random.nextBoolean() ? "PENDIENTE" : "NO JUSTIFICADO")
                        .build());
            }
        }
    }

    private void seedAdminUser(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        String adminEmail = "admin@mimundofeliz.edu.pe";
        if (usuarioRepository.findByCorreoInstitucional(adminEmail).isEmpty()) {
            // Crear un empleado "dummy" para el admin
            Empleado adminEmpleado = new Empleado();
            adminEmpleado.setNombre("Admin");
            adminEmpleado.setApellido("System");
            adminEmpleado.setDni("00000000");
            adminEmpleado.setCorreo("admin.system@familiafeliz.com");
            empleadoRepository.save(adminEmpleado); // <--- ¡LÍNEA AGREGADA!
            // Set other required fields with dummy data if needed, or allow nulls if entity
            // permits
            // For now assuming minimal requirements. If constraints exist, we'd need to
            // fetch a cargo/dept.

            Usuario adminUser = new Usuario();
            adminUser.setEmpleado(adminEmpleado); // Link the dummy employee (CascadeType.ALL usually handles save)
            adminUser.setCorreoInstitucional(adminEmail);
            adminUser.setPassword(passwordEncoder.encode("admin"));
            adminUser.setRol("ROLE_ADMIN");

            usuarioRepository.save(adminUser);
            System.out.println(">>> Usuario Administrador creado: " + adminEmail);
        }
    }

    private String generarCorreoInstitucional(String nombre, String apellido, UsuarioRepository usuarioRepository) {
        String base = normalizar(nombre) + "." + normalizar(apellido);
        String dominio = "@mimundofeliz.edu.pe";
        String candidato = base + dominio;
        int contador = 1;
        while (usuarioRepository.existsByCorreoInstitucional(candidato)) {
            candidato = base + contador + dominio;
            contador++;
        }
        return candidato;
    }

    private String normalizar(String valor) {
        String normalizado = Normalizer.normalize(valor.toLowerCase(Locale.ROOT), Normalizer.Form.NFD);
        return normalizado.replaceAll("[^a-z]", "");
    }
}
