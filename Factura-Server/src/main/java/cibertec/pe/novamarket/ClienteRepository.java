package cibertec.pe.novamarket;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    java.util.Optional<Cliente> findByDni(String dni);
}

