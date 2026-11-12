package co.edu.ucc.pos.sede.adaptadores.persistencia;

import co.edu.ucc.pos.sede.dominio.Pedido;
import co.edu.ucc.pos.sede.puertos.RepositorioPedidos;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class RepositorioPedidosJdbc implements RepositorioPedidos {

    private final JdbcClient jdbc;

    public RepositorioPedidosJdbc(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void guardar(Pedido p) {
        jdbc.sql("""
                insert into pedidos (id, sede, mesa, mesero, descripcion, total, estado, creado)
                values (:id, :sede, :mesa, :mesero, :descripcion, :total, :estado, :creado)
                """)
                .param("id", p.getId())
                .param("sede", p.getSede())
                .param("mesa", p.getMesa())
                .param("mesero", p.getMesero())
                .param("descripcion", p.getDescripcion())
                .param("total", p.getTotal())
                .param("estado", p.getEstado().name())
                .param("creado", p.getCreado().atOffset(ZoneOffset.UTC))
                .update();
    }

    @Override
    public void actualizar(Pedido p) {
        jdbc.sql("""
                update pedidos set estado = :estado, anulado_por = :anuladoPor, motivo_anulacion = :motivo
                where id = :id
                """)
                .param("estado", p.getEstado().name())
                .param("anuladoPor", p.getAnuladoPor())
                .param("motivo", p.getMotivoAnulacion())
                .param("id", p.getId())
                .update();
    }

    @Override
    public Optional<Pedido> buscar(UUID id) {
        return jdbc.sql("select * from pedidos where id = :id").param("id", id)
                .query(this::mapear).optional();
    }

    @Override
    public List<Pedido> listar() {
        return jdbc.sql("select * from pedidos order by creado desc").query(this::mapear).list();
    }

    private Pedido mapear(ResultSet rs, int fila) throws SQLException {
        return new Pedido(
                rs.getObject("id", UUID.class),
                rs.getString("sede"),
                rs.getString("mesa"),
                rs.getString("mesero"),
                rs.getString("descripcion"),
                rs.getBigDecimal("total"),
                rs.getObject("creado", OffsetDateTime.class).toInstant(),
                Pedido.Estado.valueOf(rs.getString("estado")),
                rs.getString("anulado_por"),
                rs.getString("motivo_anulacion"));
    }
}
