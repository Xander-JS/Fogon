package co.edu.ucc.pos.sincronizador;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/** Consumidor 2: guarda cada evento en su outbox local. Enviarlo a la nube es otro paso. */
@Component
public class ReceptorEventos {

    private static final Logger log = LoggerFactory.getLogger(ReceptorEventos.class);
    private final JdbcClient jdbc;

    public ReceptorEventos(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "sincronizador.pedidos", durable = "true"),
            exchange = @Exchange(value = "pedidos.eventos", type = ExchangeTypes.TOPIC),
            key = "pedido.#"))
    public void recibir(String payload,
                        @Header(AmqpHeaders.MESSAGE_ID) String eventoId,
                        @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {
        int filas = jdbc.sql("""
                insert into outbox (evento_id, routing_key, payload)
                values (:id, :rk, :payload)
                on conflict (evento_id) do nothing
                """)
                .param("id", UUID.fromString(eventoId))
                .param("rk", routingKey)
                .param("payload", payload)
                .update();
        if (filas == 1) {
            log.info("[SINCRONIZADOR] Evento {} guardado en outbox", eventoId);
        } else {
            log.info("[SINCRONIZADOR] Evento {} repetido, ignorado", eventoId);
        }
    }
}
