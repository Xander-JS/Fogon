package co.edu.ucc.pos.sede.dominio;

/** Si la cadena de hashes está rota, primerRegistroAlterado dice dónde. */
public record ResultadoVerificacion(boolean valida, int registros, Long primerRegistroAlterado) { }
