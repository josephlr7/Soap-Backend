package cibertec.pe.novamarket;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CajeroRepository extends JpaRepository<Cajero, Long> {
    java.util.Optional<Cajero> findByCodigoEmpleado(String codigoEmpleado);
}

