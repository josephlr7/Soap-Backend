package cibertec.pe.novamarket;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ComprobanteRepository extends JpaRepository<Comprobante, Long> {
    java.util.Optional<Comprobante> findByNumero(String numero);
}

