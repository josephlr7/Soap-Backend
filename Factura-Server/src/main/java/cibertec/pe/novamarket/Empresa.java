package cibertec.pe.novamarket;

import jakarta.persistence.*;

@Entity
@Table(name = "empresas")
public class Empresa {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(unique = true, length = 11) public String ruc;
    public String razonSocial;
    public String estadoSunat;
    public String condicionSunat;
    public String direccion;
    public String nombreComercial;
    public String ubigeo;
    public String departamento;
    public String provincia;
    public String distrito;
}

