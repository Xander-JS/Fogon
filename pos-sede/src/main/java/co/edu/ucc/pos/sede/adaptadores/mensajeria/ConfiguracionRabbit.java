package co.edu.ucc.pos.sede.adaptadores.mensajeria;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfiguracionRabbit {

    public static final String EXCHANGE = "pedidos.eventos";

    /** Exchange durable de tipo topic: cada consumidor declara su propia cola y se suscribe. */
    @Bean
    TopicExchange exchangePedidos() {
        return new TopicExchange(EXCHANGE, true, false);
    }
}
