package co.edu.ucc.pos.sede.dominio;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/** Hecho del negocio que se publica. El eventoId permite que la nube ignore duplicados. */
public record EventoPedido(UUID eventoId, String tipo, UUID pedidoId, String sede, String mesa,
                           BigDecimal total, String usuario, String motivo, Instant momento) {

    public static EventoPedido confirmado(Pedido p) {
        return new EventoPedido(UUID.randomUUID(), "CONFIRMADO", p.getId(), p.getSede(), p.getMesa(),
                p.getTotal(), p.getMesero(), null, Instant.now().truncatedTo(ChronoUnit.MILLIS));
    }

    public static EventoPedido anulado(Pedido p) {
        return new EventoPedido(UUID.randomUUID(), "ANULADO", p.getId(), p.getSede(), p.getMesa(),
                p.getTotal(), p.getAnuladoPor(), p.getMotivoAnulacion(), Instant.now().truncatedTo(ChronoUnit.MILLIS));
    }
}
