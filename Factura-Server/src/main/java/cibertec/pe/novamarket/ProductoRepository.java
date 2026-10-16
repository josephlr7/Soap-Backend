package cibertec.pe.novamarket;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    java.util.Optional<Producto> findBySku(String sku);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Producto p where p.id = :id")
    java.util.Optional<Producto> findByIdForUpdate(@Param("id") Long id);
}

