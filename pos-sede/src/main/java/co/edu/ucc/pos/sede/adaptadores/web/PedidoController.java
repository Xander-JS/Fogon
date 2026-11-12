package co.edu.ucc.pos.sede.adaptadores.web;

import co.edu.ucc.pos.sede.aplicacion.ServicioPedidos;
import co.edu.ucc.pos.sede.dominio.Pedido;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    public record CrearPedido(@NotBlank String mesa, @NotBlank String mesero,
                              @NotBlank String descripcion, @NotNull @Positive BigDecimal total) { }

    public record AnularPedido(@NotBlank String supervisor, @NotBlank String motivo) { }

    public record PedidoRespuesta(UUID id, String sede, String mesa, String mesero, String descripcion,
                                  BigDecimal total, String estado, Instant creado,
                                  String anuladoPor, String motivoAnulacion) {
        static PedidoRespuesta de(Pedido p) {
            return new PedidoRespuesta(p.getId(), p.getSede(), p.getMesa(), p.getMesero(), p.getDescripcion(),
                    p.getTotal(), p.getEstado().name(), p.getCreado(), p.getAnuladoPor(), p.getMotivoAnulacion());
        }
    }

    private final ServicioPedidos servicio;

    public PedidoController(ServicioPedidos servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoRespuesta crear(@Valid @RequestBody CrearPedido r) {
        return PedidoRespuesta.de(servicio.crear(r.mesa(), r.mesero(), r.descripcion(), r.total()));
    }

    @PostMapping("/{id}/anular")
    public PedidoRespuesta anular(@PathVariable UUID id, @Valid @RequestBody AnularPedido r) {
        return PedidoRespuesta.de(servicio.anular(id, r.supervisor(), r.motivo()));
    }

    @GetMapping
    public List<PedidoRespuesta> listar() {
        return servicio.listar().stream().map(PedidoRespuesta::de).toList();
    }
}
