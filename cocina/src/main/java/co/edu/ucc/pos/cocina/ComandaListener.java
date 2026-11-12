package co.edu.ucc.pos.cocina;

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

/** Consumidor 1: cocina tiene su propia cola durable. Funciona aunque no haya internet. */
@Component
public class ComandaListener {

    private static final Logger log = LoggerFactory.getLogger(ComandaListener.class);

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "cocina.pedidos", durable = "true"),
            exchange = @Exchange(value = "pedidos.eventos", type = ExchangeTypes.TOPIC),
            key = "pedido.*"))
    public void recibir(String evento, @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {
        if ("pedido.confirmado".equals(routingKey)) {
            log.info("[COCINA] NUEVA COMANDA -> {}", evento);
        } else {
            log.info("[COCINA] COMANDA CANCELADA -> {}", evento);
        }
    }
}
