package co.edu.ucc.pos.nube;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.bind.annotation.*;

@RestController
public class EventoController {

    private static final Logger log = LoggerFactory.getLogger(EventoController.class);

    /** Copia propia del contrato JSON. La nube no comparte clases con la sede. */
    public record EventoPedido(UUID eventoId, String tipo, UUID pedidoId, String sede, String mesa,
                               BigDecimal total, String usuario, String motivo, Instant momento) { }

    private final JdbcClient jdbc;

    public EventoController(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** Idempotente: si el mismo evento llega dos veces, la segunda se ignora. */
    @PostMapping("/eventos")
    public Map<String, String> recibir(@RequestBody EventoPedido e) {
        int filas = jdbc.sql("""
                insert into eventos_recibidos
                    (evento_id, tipo, pedido_id, sede, mesa, total, usuario, motivo, momento)
                values (:eventoId, :tipo, :pedidoId, :sede, :mesa, :total, :usuario, :motivo, :momento)
                on conflict (evento_id) do nothing
                """)
                .param("eventoId", e.eventoId())
                .param("tipo", e.tipo())
                .param("pedidoId", e.pedidoId())
                .param("sede", e.sede())
                .param("mesa", e.mesa())
                .param("total", e.total())
                .param("usuario", e.usuario())
                .param("motivo", e.motivo())
                .param("momento", e.momento().atOffset(ZoneOffset.UTC))
                .update();
        if (filas == 0) {
            log.info("[NUBE] Evento {} duplicado, ignorado", e.eventoId());
            return Map.of("estado", "duplicado");
        }
        log.info("[NUBE] Evento {} ({}) recibido de {}", e.eventoId(), e.tipo(), e.sede());
        return Map.of("estado", "registrado");
    }

    @GetMapping("/eventos")
    public List<Map<String, Object>> listar() {
        return jdbc.sql("""
                select evento_id::text as evento_id, tipo, pedido_id::text as pedido_id, sede, mesa,
                       total, usuario, motivo, momento::text as momento, recibido_en::text as recibido_en
                from eventos_recibidos order by recibido_en desc
                """).query().listOfRows();
    }

    /** Reporte central: la parte del sistema que se degrada cuando no hay conexión. */
    @GetMapping("/reportes/ventas")
    public List<Map<String, Object>> ventasPorSede() {
        return jdbc.sql("""
                select sede,
                       count(*) filter (where tipo = 'CONFIRMADO') as pedidos,
                       count(*) filter (where tipo = 'ANULADO')    as anulados,
                       coalesce(sum(total) filter (where tipo = 'CONFIRMADO'), 0)
                         - coalesce(sum(total) filter (where tipo = 'ANULADO'), 0) as venta_neta
                from eventos_recibidos group by sede order by sede
                """).query().listOfRows();
    }
}
