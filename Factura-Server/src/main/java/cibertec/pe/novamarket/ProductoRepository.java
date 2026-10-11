package cibertec.pe.novamarket;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    java.util.Optional<Producto> findBySku(String sku);
}

