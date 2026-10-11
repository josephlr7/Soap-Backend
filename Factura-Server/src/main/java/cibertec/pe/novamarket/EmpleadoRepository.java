package cibertec.pe.novamarket;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {
    Optional<Empleado> findByUsuarioAndActivoTrue(String usuario);
    Optional<Empleado> findByCodigoEmpleado(String codigoEmpleado);
}
