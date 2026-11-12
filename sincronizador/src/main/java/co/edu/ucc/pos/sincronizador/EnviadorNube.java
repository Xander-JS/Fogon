package co.edu.ucc.pos.sincronizador;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Cada 3 segundos intenta enviar los pendientes a la nube, en orden.
 * Si la nube no responde, no pasa nada: los eventos esperan y se reintenta.
 */
@Component
public class EnviadorNube {

    private static final Logger log = LoggerFactory.getLogger(EnviadorNube.class);

    private record Pendiente(UUID eventoId, String payload) { }

    private final JdbcClient jdbc;
    private final String centralUrl;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();

    public EnviadorNube(JdbcClient jdbc, @Value("${central.url}") String centralUrl) {
        this.jdbc = jdbc;
        this.centralUrl = centralUrl;
    }

    @Scheduled(fixedDelay = 3000)
    public void enviarPendientes() {
        List<Pendiente> pendientes = jdbc.sql("""
                select evento_id, payload from outbox
                where enviado_en is null order by recibido limit 50
                """)
                .query((rs, n) -> new Pendiente(rs.getObject("evento_id", UUID.class), rs.getString("payload")))
                .list();

        for (Pendiente p : pendientes) {
            try {
                HttpRequest peticion = HttpRequest.newBuilder(URI.create(centralUrl + "/eventos"))
                        .timeout(Duration.ofSeconds(3))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(p.payload()))
                        .build();
                HttpResponse<String> respuesta = http.send(peticion, HttpResponse.BodyHandlers.ofString());
                if (respuesta.statusCode() / 100 != 2) {
                    log.warn("[SINCRONIZADOR] La nube respondió {}. Se reintenta luego.", respuesta.statusCode());
                    return;
                }
                jdbc.sql("update outbox set enviado_en = now() where evento_id = :id")
                        .param("id", p.eventoId()).update();
                log.info("[SINCRONIZADOR] Evento {} sincronizado con la nube", p.eventoId());
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return;
            } catch (IOException | RuntimeException ex) {
                log.warn("[SINCRONIZADOR] Sin conexión con la nube. {} evento(s) en espera, reintento en 3 s",
                        pendientes.size());
                return;
            }
        }
    }
}
