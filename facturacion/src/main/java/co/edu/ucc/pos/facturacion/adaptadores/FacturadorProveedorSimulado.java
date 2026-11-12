package co.edu.ucc.pos.facturacion.adaptadores;

import co.edu.ucc.pos.facturacion.puertos.FacturadorElectronico;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Adaptador simulado. La demora configurable permite demostrar que un proveedor lento no frena la venta. */
@Component
public class FacturadorProveedorSimulado implements FacturadorElectronico {

    private final long demoraMs;

    public FacturadorProveedorSimulado(@Value("${facturacion.demora-ms:0}") long demoraMs) {
        this.demoraMs = demoraMs;
    }

    @Override
    public String emitir(String eventoPedidoJson) {
        if (demoraMs > 0) {
            try {
                Thread.sleep(demoraMs);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Facturación interrumpida", ex);
            }
        }
        return "CUFE-SIMULADO-" + UUID.randomUUID();
    }
}
