package co.edu.ucc.pos.sede.adaptadores.mensajeria;

import co.edu.ucc.pos.sede.dominio.EventoPedido;

/** Serializa el evento a JSON. Es el contrato con los demás servicios: no comparten clases. */
final class EventoJson {

    private EventoJson() { }

    static String aJson(EventoPedido e) {
        return "{"
                + campo("eventoId", e.eventoId()) + ","
                + campo("tipo", e.tipo()) + ","
                + campo("pedidoId", e.pedidoId()) + ","
                + campo("sede", e.sede()) + ","
                + campo("mesa", e.mesa()) + ","
                + "\"total\":" + e.total().toPlainString() + ","
                + campo("usuario", e.usuario()) + ","
                + campo("motivo", e.motivo()) + ","
                + campo("momento", e.momento())
                + "}";
    }

    private static String campo(String nombre, Object valor) {
        if (valor == null) {
            return "\"" + nombre + "\":null";
        }
        return "\"" + nombre + "\":\"" + escapar(valor.toString()) + "\"";
    }

    private static String escapar(String texto) {
        return texto.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}
