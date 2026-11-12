package co.edu.ucc.pos.facturacion.adaptadores;

import co.edu.ucc.pos.facturacion.puertos.FacturadorElectronico;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/** Consumidor 3: solo le interesan los pedidos confirmados. */
@Component
public class EventoPedidoListener {

    private static final Logger log = LoggerFactory.getLogger(EventoPedidoListener.class);
    private final FacturadorElectronico facturador;

    public EventoPedidoListener(FacturadorElectronico facturador) {
        this.facturador = facturador;
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "facturacion.pedidos", durable = "true"),
            exchange = @Exchange(value = "pedidos.eventos", type = ExchangeTypes.TOPIC),
            key = "pedido.confirmado"))
    public void recibir(String evento, @Header(AmqpHeaders.MESSAGE_ID) String eventoId) {
        log.info("[FACTURACION] Facturando evento {}...", eventoId);
        try {
            String cufe = facturador.emitir(evento);
            log.info("[FACTURACION] Evento {} facturado. CUFE={}", eventoId, cufe);
        } catch (RuntimeException ex) {
            log.error("[FACTURACION] Falló la facturación del evento {}: {}", eventoId, ex.getMessage());
        }
    }
}
