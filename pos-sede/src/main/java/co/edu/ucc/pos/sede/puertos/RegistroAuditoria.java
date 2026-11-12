package co.edu.ucc.pos.sede.puertos;

import co.edu.ucc.pos.sede.dominio.EntradaAuditoria;
import co.edu.ucc.pos.sede.dominio.ResultadoVerificacion;
import java.util.List;

public interface RegistroAuditoria {
    void registrar(String usuario, String accion, String detalle);
    List<EntradaAuditoria> listar();
    ResultadoVerificacion verificar();
}
