package pt.trasmum.servidor.handler;

import io.javalin.http.Context;
import pt.trasmum.servidor.servico.interfaces.IMonitorServico;

import java.time.LocalDate;

public class MonitorHandler {
    private final IMonitorServico monitor;

    public MonitorHandler(IMonitorServico monitor) {
        this.monitor = monitor;
    }

    public void estado(Context ctx) {
        String d = ctx.queryParam("data");
        LocalDate data = (d == null || d.isBlank()) ? LocalDate.now() : LocalDate.parse(d);
        ctx.json(monitor.obterEstadoRede(data));
    }
}
