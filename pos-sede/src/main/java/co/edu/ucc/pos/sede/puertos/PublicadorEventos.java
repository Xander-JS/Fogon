package co.edu.ucc.pos.sede.puertos;

import co.edu.ucc.pos.sede.dominio.EventoPedido;

public interface PublicadorEventos {
    void publicar(EventoPedido evento);
}
