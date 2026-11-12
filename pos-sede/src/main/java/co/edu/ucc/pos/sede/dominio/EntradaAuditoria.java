package co.edu.ucc.pos.sede.dominio;

import java.time.Instant;

public record EntradaAuditoria(long id, Instant momento, String usuario, String accion,
                               String detalle, String hashAnterior, String hash) { }
