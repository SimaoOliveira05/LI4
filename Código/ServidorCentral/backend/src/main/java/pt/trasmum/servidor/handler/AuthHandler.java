package pt.trasmum.servidor.handler;

import io.javalin.http.Context;
import pt.trasmum.servidor.app.SessaoManager;
import pt.trasmum.servidor.dominio.CEO;
import pt.trasmum.servidor.dto.api.LoginRequestDTO;
import pt.trasmum.servidor.dto.api.LoginResponseDTO;
import pt.trasmum.servidor.servico.interfaces.IAutenticacaoCEOServico;

import java.util.Map;

public class AuthHandler {
    private final IAutenticacaoCEOServico autenticacao;
    private final SessaoManager sessoes;

    public AuthHandler(IAutenticacaoCEOServico autenticacao, SessaoManager sessoes) {
        this.autenticacao = autenticacao;
        this.sessoes = sessoes;
    }

    public void login(Context ctx) {
        LoginRequestDTO req = ctx.bodyAsClass(LoginRequestDTO.class);
        CEO ceo = autenticacao.autenticar(req.nomeUtilizador, req.palavraPasse);
        String token = sessoes.criar(ceo);
        ctx.status(200).json(new LoginResponseDTO(token, ceo.getNomeUtilizador()));
    }

    public void logout(Context ctx) {
        String token = extrairToken(ctx);
        sessoes.remover(token);
        ctx.status(204);
    }

    public void verificarToken(Context ctx) {
        if ("OPTIONS".equalsIgnoreCase(ctx.method().name())) return;
        String path = ctx.path();
        if (path.startsWith("/api/auth/")) return;
        String token = extrairToken(ctx);
        CEO ceo = sessoes.obter(token);
        if (ceo == null) {
            ctx.status(401).json(Map.of("erro", "Sessão inválida ou expirada"));
            ctx.skipRemainingHandlers();
        }
    }

    private String extrairToken(Context ctx) {
        String header = ctx.header("Authorization");
        if (header == null) return null;
        if (header.startsWith("Bearer ")) return header.substring(7);
        return header;
    }
}
