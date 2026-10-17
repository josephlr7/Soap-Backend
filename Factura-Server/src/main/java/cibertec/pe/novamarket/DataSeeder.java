package cibertec.pe.novamarket;

import java.math.BigDecimal;
import java.security.SecureRandom;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {
    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final CajeroRepository cajeroRepository;
    private final EmpleadoRepository empleadoRepository;
    @Value("${novamarket.admin.password:cajero123}") private String adminPassword;

    public DataSeeder(CategoriaRepository categoriaRepository, ProductoRepository productoRepository, CajeroRepository cajeroRepository,
            EmpleadoRepository empleadoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
        this.cajeroRepository = cajeroRepository;
        this.empleadoRepository = empleadoRepository;
    }

    @Override public void run(String... args) {
        String password = (adminPassword == null || adminPassword.isBlank()) ? "cajero123" : adminPassword;
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        Empleado e = empleadoRepository.findByCodigoEmpleado("CJ-001").orElseGet(() -> {
            Empleado nuevo = new Empleado();
            nuevo.codigoEmpleado = "CJ-001";
            nuevo.usuario = "cajero";
            nuevo.nombres = "Cajero Nova";
            nuevo.rol = "CAJERO";
            nuevo.activo = true;
            return nuevo;
        });

        e.usuario = "cajero";
        e.passwordHash = encoder.encode(password);
        e.activo = true;
        empleadoRepository.save(e);
        System.out.println("=================================================================");
        System.out.println(">> Acceso NovaMarket listo | Usuario: cajero | Password: " + password);
        System.out.println("=================================================================");

        if (cajeroRepository.findByCodigoEmpleado("CJ-001").isEmpty()) {
            Cajero c = new Cajero(); c.codigoEmpleado = "CJ-001"; c.nombres = "Cajero Nova"; cajeroRepository.save(c);
        }
        if (productoRepository.count() > 0) return;
        Categoria abarrotes = categoria("Abarrotes", "Productos de despensa");
        Categoria bebidas = categoria("Bebidas", "Bebidas familiares");
        producto("NM-ARROZ-001", "Arroz extra 5kg", new BigDecimal("22.90"), 80, abarrotes);
        producto("NM-ACEITE-001", "Aceite vegetal 900ml", new BigDecimal("9.50"), 120, abarrotes);
        producto("NM-LECHE-001", "Leche evaporada pack x6", new BigDecimal("23.40"), 55, abarrotes);
        producto("NM-AGUA-001", "Agua sin gas 2.5L", new BigDecimal("3.90"), 140, bebidas);
        producto("NM-GASEOSA-001", "Gaseosa familiar 3L", new BigDecimal("10.90"), 70, bebidas);
    }
    private Categoria categoria(String nombre, String desc) { Categoria c = new Categoria(); c.nombre = nombre; c.descripcion = desc; return categoriaRepository.save(c); }
    private void producto(String sku, String nombre, BigDecimal precio, int stock, Categoria cat) { Producto p = new Producto(); p.sku = sku; p.nombre = nombre; p.precio = precio; p.stock = stock; p.unidad = "UND"; p.categoria = cat; productoRepository.save(p); }
}

