package cibertec.pe.novamarket;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PagoRepository extends JpaRepository<Pago, Long> {
    java.util.Optional<Pago> findByVentaId(Long ventaId);
}

