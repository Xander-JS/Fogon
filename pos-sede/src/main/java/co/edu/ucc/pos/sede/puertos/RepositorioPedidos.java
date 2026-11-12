package co.edu.ucc.pos.sede.puertos;

import co.edu.ucc.pos.sede.dominio.Pedido;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RepositorioPedidos {
    void guardar(Pedido pedido);
    void actualizar(Pedido pedido);
    Optional<Pedido> buscar(UUID id);
    List<Pedido> listar();
}
