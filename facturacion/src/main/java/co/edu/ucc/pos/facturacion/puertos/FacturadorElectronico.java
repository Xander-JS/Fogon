package co.edu.ucc.pos.facturacion.puertos;

/**
 * Puerto hacia el proveedor tecnológico de facturación electrónica (DIAN).
 * Cambiar de proveedor = escribir otra implementación. Pedidos y cocina no se tocan.
 */
public interface FacturadorElectronico {
    /** Emite la factura y devuelve el CUFE. */
    String emitir(String eventoPedidoJson);
}
