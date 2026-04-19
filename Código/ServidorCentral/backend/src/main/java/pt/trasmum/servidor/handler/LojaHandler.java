package pt.trasmum.servidor.handler;

import io.javalin.http.Context;
import pt.trasmum.servidor.repositorio.interfaces.LojaRepositorio;

public class LojaHandler {
    private final LojaRepositorio repo;

    public LojaHandler(LojaRepositorio repo) {
        this.repo = repo;
    }

    public void listar(Context ctx) {
        ctx.json(repo.listarTodas());
    }
}
