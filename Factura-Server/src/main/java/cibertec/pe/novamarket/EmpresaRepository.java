package cibertec.pe.novamarket;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    java.util.Optional<Empresa> findByRuc(String ruc);
}

