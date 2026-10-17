package cibertec.pe.novamarket;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/auth", "/auth"})
@CrossOrigin(origins = "*")
public class AuthController {
    private final EmpleadoRepository empleadoRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthController(EmpleadoRepository empleadoRepository) {
        this.empleadoRepository = empleadoRepository;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        Empleado empleado = empleadoRepository.findByUsuarioAndActivoTrue(request.usuario)
                .filter(e -> passwordEncoder.matches(request.password, e.passwordHash))
                .orElseThrow(() -> new IllegalArgumentException("Usuario o password incorrectos"));

        LoginResponse response = new LoginResponse();
        response.empleadoId = empleado.id;
        response.codigoEmpleado = empleado.codigoEmpleado;
        response.usuario = empleado.usuario;
        response.nombres = empleado.nombres;
        response.rol = empleado.rol;
        return response;
    }
}
