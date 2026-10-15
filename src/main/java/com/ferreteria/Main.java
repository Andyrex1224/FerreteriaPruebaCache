package com.ferreteria;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
@RestController
@RequestMapping("/api/productos")
// El frontend se sirve desde el mismo backend (http://localhost:8080), asi que CORS solo hace falta
// si alguien abre el index.html desde otro servidor en el puerto 5173
@CrossOrigin(origins = "http://localhost:5173", exposedHeaders = "X-Tiempo-Consulta-ms")
public class Main {

    public record Producto(long id, String nombre, BigDecimal precio) {
    }

    private static final RowMapper<Producto> MAPPER = (rs, n) ->
            new Producto(rs.getLong("id"), rs.getString("nombre"), rs.getBigDecimal("precio"));

    private final JdbcTemplate jdbc;

    // volatile: todos los hilos ven la lista en cuanto otro la guarda
    private volatile List<Producto> cacheTodos = null;
    private final Map<Long, Producto> cachePorId = new ConcurrentHashMap<>();

    public Main(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @GetMapping
    public ResponseEntity<List<Producto>> listar(@RequestParam(defaultValue = "true") boolean cache) {
        long inicio = System.nanoTime();
        if (!cache) {
            // Sin cache (/api/productos?cache=false): siempre se consulta la base de datos
            List<Producto> productos = jdbc.query("SELECT id, nombre, precio FROM productos", MAPPER);
            return ResponseEntity.ok().header("X-Tiempo-Consulta-ms", tiempoDesde(inicio)).body(productos);
        }
        List<Producto> productos = cacheTodos;
        if (productos == null) {
            synchronized (this) {
                // Primera vez: se consulta la base de datos y se guarda en la cache (una sola vez aunque lleguen varias peticiones)
                if (cacheTodos == null) {
                    cacheTodos = jdbc.query("SELECT id, nombre, precio FROM productos", MAPPER);
                }
                productos = cacheTodos;
            }
        }
        return ResponseEntity.ok().header("X-Tiempo-Consulta-ms", tiempoDesde(inicio)).body(productos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscar(@PathVariable long id) {
        long inicio = System.nanoTime();
        Producto producto = cachePorId.get(id);

        if (producto == null) {
            List<Producto> resultado = jdbc.query("SELECT id, nombre, precio FROM productos WHERE id = ?", MAPPER, id);
            if (resultado.isEmpty()) {
                return ResponseEntity.status(404).header("X-Tiempo-Consulta-ms", tiempoDesde(inicio))
                        .body(Map.of("error", "Producto no encontrado"));
            }
            producto = resultado.get(0);
            cachePorId.put(id, producto);
        }
        return ResponseEntity.ok().header("X-Tiempo-Consulta-ms", tiempoDesde(inicio)).body(producto);
    }

    private static String tiempoDesde(long inicio) {
        return String.format(Locale.US, "%.2f", (System.nanoTime() - inicio) / 1_000_000.0);
    }
}
