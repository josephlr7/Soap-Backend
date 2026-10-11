package cibertec.pe.novamarket;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "auditoria_consultas_api")
public class AuditoriaConsultaApi {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    public String proveedor;
    public String tipoConsulta;
    public String documento;
    public String resultado;
    public LocalDateTime fechaConsulta = LocalDateTime.now();
}

