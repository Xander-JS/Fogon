package co.edu.ucc.pos.sede.config;

import co.edu.ucc.pos.sede.aplicacion.ServicioPedidos;
import co.edu.ucc.pos.sede.puertos.PublicadorEventos;
import co.edu.ucc.pos.sede.puertos.RegistroAuditoria;
import co.edu.ucc.pos.sede.puertos.RepositorioPedidos;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Aquí se "enchufan" los adaptadores a los puertos. Dominio y aplicación quedan libres de Spring. */
@Configuration
public class ConfiguracionAplicacion {

    @Bean
    ServicioPedidos servicioPedidos(@Value("${pos.sede}") String sede, RepositorioPedidos repositorio,
                                    PublicadorEventos publicador, RegistroAuditoria auditoria) {
        return new ServicioPedidos(sede, repositorio, publicador, auditoria);
    }
}
