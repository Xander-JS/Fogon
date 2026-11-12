package co.edu.ucc.pos.sede.dominio;

import java.util.UUID;

public class PedidoNoEncontradoException extends RuntimeException {
    public PedidoNoEncontradoException(UUID id) { super("No existe el pedido " + id); }
}
