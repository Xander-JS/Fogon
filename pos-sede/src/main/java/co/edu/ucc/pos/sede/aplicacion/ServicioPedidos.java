package co.edu.ucc.pos.sede.aplicacion;

import co.edu.ucc.pos.sede.dominio.EventoPedido;
import co.edu.ucc.pos.sede.dominio.Pedido;
import co.edu.ucc.pos.sede.dominio.PedidoNoEncontradoException;
import co.edu.ucc.pos.sede.puertos.PublicadorEventos;
import co.edu.ucc.pos.sede.puertos.RegistroAuditoria;
import co.edu.ucc.pos.sede.puertos.RepositorioPedidos;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** Casos de uso. Solo depende de los puertos (interfaces), nunca de los adaptadores. */
public class ServicioPedidos {

    private final String sede;
    private final RepositorioPedidos repositorio;
    private final PublicadorEventos publicador;
    private final RegistroAuditoria auditoria;

    public ServicioPedidos(String sede, RepositorioPedidos repositorio,
                           PublicadorEventos publicador, RegistroAuditoria auditoria) {
        this.sede = sede;
        this.repositorio = repositorio;
        this.publicador = publicador;
        this.auditoria = auditoria;
    }

    public Pedido crear(String mesa, String mesero, String descripcion, BigDecimal total) {
        Pedido pedido = Pedido.nuevo(sede, mesa, mesero, descripcion, total);
        repositorio.guardar(pedido);
        auditoria.registrar(mesero, "PEDIDO_CREADO",
                "pedido=" + pedido.getId() + " mesa=" + mesa + " total=" + total);
        publicador.publicar(EventoPedido.confirmado(pedido));
        return pedido;
    }

    public Pedido anular(UUID id, String supervisor, String motivo) {
        Pedido pedido = repositorio.buscar(id).orElseThrow(() -> new PedidoNoEncontradoException(id));
        pedido.anular(supervisor, motivo);
        repositorio.actualizar(pedido);
        auditoria.registrar(supervisor, "PEDIDO_ANULADO", "pedido=" + id + " motivo=" + motivo);
        publicador.publicar(EventoPedido.anulado(pedido));
        return pedido;
    }

    public List<Pedido> listar() {
        return repositorio.listar();
    }
}
