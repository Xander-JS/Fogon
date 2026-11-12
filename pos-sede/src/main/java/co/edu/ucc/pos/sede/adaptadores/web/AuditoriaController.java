package co.edu.ucc.pos.sede.adaptadores.web;

import co.edu.ucc.pos.sede.dominio.EntradaAuditoria;
import co.edu.ucc.pos.sede.dominio.ResultadoVerificacion;
import co.edu.ucc.pos.sede.puertos.RegistroAuditoria;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auditoria")
public class AuditoriaController {

    private final RegistroAuditoria auditoria;

    public AuditoriaController(RegistroAuditoria auditoria) {
        this.auditoria = auditoria;
    }

    @GetMapping
    public List<EntradaAuditoria> listar() {
        return auditoria.listar();
    }

    @GetMapping("/verificar")
    public ResultadoVerificacion verificar() {
        return auditoria.verificar();
    }
}
