package co.edu.ucc.pos.sede.dominio;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/** Entidad del dominio. No conoce Spring, ni la base de datos, ni RabbitMQ. */
public class Pedido {

    public enum Estado { CONFIRMADO, ANULADO }

    private final UUID id;
    private final String sede;
    private final String mesa;
    private final String mesero;
    private final String descripcion;
    private final BigDecimal total;
    private final Instant creado;
    private Estado estado;
    private String anuladoPor;
    private String motivoAnulacion;

    public Pedido(UUID id, String sede, String mesa, String mesero, String descripcion,
                  BigDecimal total, Instant creado, Estado estado,
                  String anuladoPor, String motivoAnulacion) {
        this.id = id;
        this.sede = sede;
        this.mesa = mesa;
        this.mesero = mesero;
        this.descripcion = descripcion;
        this.total = total;
        this.creado = creado;
        this.estado = estado;
        this.anuladoPor = anuladoPor;
        this.motivoAnulacion = motivoAnulacion;
    }

    public static Pedido nuevo(String sede, String mesa, String mesero, String descripcion, BigDecimal total) {
        if (total == null || total.signum() <= 0) {
            throw new ReglaNegocioException("El total del pedido debe ser mayor que cero");
        }
        return new Pedido(UUID.randomUUID(), sede, mesa, mesero, descripcion, total,
                Instant.now().truncatedTo(ChronoUnit.MILLIS), Estado.CONFIRMADO, null, null);
    }

    /** Regla: un pedido nunca se borra; se anula con supervisor y motivo, y solo una vez. */
    public void anular(String supervisor, String motivo) {
        if (estado == Estado.ANULADO) {
            throw new ReglaNegocioException("El pedido ya está anulado");
        }
        if (supervisor == null || supervisor.isBlank() || motivo == null || motivo.isBlank()) {
            throw new ReglaNegocioException("La anulación exige supervisor y motivo");
        }
        this.estado = Estado.ANULADO;
        this.anuladoPor = supervisor;
        this.motivoAnulacion = motivo;
    }

    public UUID getId() { return id; }
    public String getSede() { return sede; }
    public String getMesa() { return mesa; }
    public String getMesero() { return mesero; }
    public String getDescripcion() { return descripcion; }
    public BigDecimal getTotal() { return total; }
    public Instant getCreado() { return creado; }
    public Estado getEstado() { return estado; }
    public String getAnuladoPor() { return anuladoPor; }
    public String getMotivoAnulacion() { return motivoAnulacion; }
}
