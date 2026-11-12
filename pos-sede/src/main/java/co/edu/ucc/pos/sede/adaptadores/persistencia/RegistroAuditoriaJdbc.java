package co.edu.ucc.pos.sede.adaptadores.persistencia;

import co.edu.ucc.pos.sede.dominio.EntradaAuditoria;
import co.edu.ucc.pos.sede.dominio.ResultadoVerificacion;
import co.edu.ucc.pos.sede.puertos.RegistroAuditoria;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.List;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Registro solo-agregar con hash encadenado: cada fila guarda el hash de la anterior.
 * La base de datos además bloquea UPDATE y DELETE con un trigger (ver V1__esquema_inicial.sql).
 */
@Repository
public class RegistroAuditoriaJdbc implements RegistroAuditoria {

    private static final String GENESIS = "0".repeat(64);
    private final JdbcClient jdbc;

    public RegistroAuditoriaJdbc(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public synchronized void registrar(String usuario, String accion, String detalle) {
        String anterior = jdbc.sql("select hash from auditoria order by id desc limit 1")
                .query(String.class).optional().orElse(GENESIS);
        Instant momento = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        String hash = calcularHash(anterior, momento, usuario, accion, detalle);

        jdbc.sql("""
                insert into auditoria (momento, usuario, accion, detalle, hash_anterior, hash)
                values (:momento, :usuario, :accion, :detalle, :anterior, :hash)
                """)
                .param("momento", momento.atOffset(ZoneOffset.UTC))
                .param("usuario", usuario)
                .param("accion", accion)
                .param("detalle", detalle)
                .param("anterior", anterior)
                .param("hash", hash)
                .update();
    }

    @Override
    public List<EntradaAuditoria> listar() {
        return jdbc.sql("select * from auditoria order by id").query((rs, n) -> new EntradaAuditoria(
                rs.getLong("id"),
                rs.getObject("momento", OffsetDateTime.class).toInstant(),
                rs.getString("usuario"),
                rs.getString("accion"),
                rs.getString("detalle"),
                rs.getString("hash_anterior"),
                rs.getString("hash"))).list();
    }

    @Override
    public ResultadoVerificacion verificar() {
        List<EntradaAuditoria> entradas = listar();
        String anterior = GENESIS;
        for (EntradaAuditoria e : entradas) {
            String esperado = calcularHash(anterior, e.momento(), e.usuario(), e.accion(), e.detalle());
            if (!e.hashAnterior().equals(anterior) || !e.hash().equals(esperado)) {
                return new ResultadoVerificacion(false, entradas.size(), e.id());
            }
            anterior = e.hash();
        }
        return new ResultadoVerificacion(true, entradas.size(), null);
    }

    private static String calcularHash(String anterior, Instant momento, String usuario,
                                       String accion, String detalle) {
        String contenido = anterior + "|" + momento + "|" + usuario + "|" + accion + "|" + detalle;
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(sha.digest(contenido.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
