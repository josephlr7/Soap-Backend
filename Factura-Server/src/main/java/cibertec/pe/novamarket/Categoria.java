package cibertec.pe.novamarket;

import jakarta.persistence.*;

@Entity
@Table(name = "categorias")
public class Categoria {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(nullable = false, unique = true) public String nombre;
    public String descripcion;
}

