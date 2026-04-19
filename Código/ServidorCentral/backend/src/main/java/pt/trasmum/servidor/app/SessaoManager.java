package pt.trasmum.servidor.app;

import pt.trasmum.servidor.dominio.CEO;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessaoManager {
    public static class Sessao {
        public final CEO ceo;
        public final LocalDateTime expiraEm;

        public Sessao(CEO ceo, LocalDateTime expiraEm) {
            this.ceo = ceo;
            this.expiraEm = expiraEm;
        }
    }

    private final Map<String, Sessao> sessoes = new ConcurrentHashMap<>();
    private final long duracaoHoras;

    public SessaoManager(long duracaoHoras) {
        this.duracaoHoras = duracaoHoras;
    }

    public String criar(CEO ceo) {
        String token = UUID.randomUUID().toString();
        sessoes.put(token, new Sessao(ceo, LocalDateTime.now().plusHours(duracaoHoras)));
        return token;
    }

    public CEO obter(String token) {
        if (token == null) return null;
        Sessao s = sessoes.get(token);
        if (s == null) return null;
        if (s.expiraEm.isBefore(LocalDateTime.now())) {
            sessoes.remove(token);
            return null;
        }
        return s.ceo;
    }

    public void remover(String token) {
        if (token != null) sessoes.remove(token);
    }
}
