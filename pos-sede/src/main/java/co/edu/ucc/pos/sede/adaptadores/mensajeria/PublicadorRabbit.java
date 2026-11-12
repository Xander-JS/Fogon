package co.edu.ucc.pos.sede.adaptadores.mensajeria;

import co.edu.ucc.pos.sede.dominio.EventoPedido;
import co.edu.ucc.pos.sede.puertos.PublicadorEventos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class PublicadorRabbit implements PublicadorEventos {

    private static final Logger log = LoggerFactory.getLogger(PublicadorRabbit.class);
    private final RabbitTemplate rabbit;

    public PublicadorRabbit(RabbitTemplate rabbit) {
        this.rabbit = rabbit;
    }

    @Override
    public void publicar(EventoPedido evento) {
        String routingKey = "pedido." + evento.tipo().toLowerCase();
        rabbit.convertAndSend(ConfiguracionRabbit.EXCHANGE, routingKey, EventoJson.aJson(evento), mensaje -> {
            // El id del evento viaja como messageId: los consumidores lo usan para no duplicar.
            mensaje.getMessageProperties().setMessageId(evento.eventoId().toString());
            return mensaje;
        });
        log.info("Evento {} publicado con routing key {}", evento.eventoId(), routingKey);
    }
}
