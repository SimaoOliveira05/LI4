package pt.trasmum.servidor.servico.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.trasmum.servidor.dominio.CEO;
import pt.trasmum.servidor.dominio.ContaBloqueadaException;
import pt.trasmum.servidor.dominio.ContaCEOJaExisteException;
import pt.trasmum.servidor.dominio.CredenciaisInvalidasException;
import pt.trasmum.servidor.repositorio.interfaces.CEORepositorio;

import java.time.LocalDateTime;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutenticacaoCEOServicoTest {

    @Mock
    CEORepositorio repo;

    AutenticacaoCEOServico servico;

    @BeforeEach
    void setUp() {
        Properties config = new Properties();
        config.setProperty("auth.tentativasMaximas", "3");
        config.setProperty("auth.duracaoBloqueioMinutos", "5");
        servico = new AutenticacaoCEOServico(repo, config);
    }

    // ── autenticar ──────────────────────────────────────────────────

    @Test
    void autenticar_utilizadorInexistente_lancaCredenciaisInvalidasException() {
        when(repo.buscarPorNomeUtilizador("desconhecido")).thenReturn(null);
        assertThrows(CredenciaisInvalidasException.class,
                () -> servico.autenticar("desconhecido", "qualquerCoisa"));
    }

    @Test
    void autenticar_contaBloqueada_lancaContaBloqueadaException() {
        CEO ceo = ceoBloqueado();
        when(repo.buscarPorNomeUtilizador("bloqueado")).thenReturn(ceo);
        assertThrows(ContaBloqueadaException.class,
                () -> servico.autenticar("bloqueado", "qualquer"));
    }

    @Test
    void autenticar_palavraPasseIncorreta_lancaCredenciaisInvalidasERegistaFalhado() {
        String hashCorreto = BCrypt.hashpw("correta", BCrypt.gensalt());
        CEO ceo = new CEO(1, "ceoTest", hashCorreto, 0, null);
        when(repo.buscarPorNomeUtilizador("ceoTest")).thenReturn(ceo);

        assertThrows(CredenciaisInvalidasException.class,
                () -> servico.autenticar("ceoTest", "errada"));

        verify(repo).atualizar(ceo);
        assertEquals(1, ceo.getTentativasLogin());
    }

    @Test
    void autenticar_caminhoFeliz_retornaCeoELimpaBloqueio() {
        String hashCorreto = BCrypt.hashpw("correta", BCrypt.gensalt());
        // Usar "admin" para evitar chamada extra ao repo (apagarBootstrapSeNecessario)
        CEO ceo = new CEO(1, "admin", hashCorreto, 0, null);
        when(repo.buscarPorNomeUtilizador("admin")).thenReturn(ceo);

        CEO resultado = servico.autenticar("admin", "correta");

        assertSame(ceo, resultado);
        assertEquals(0, ceo.getTentativasLogin());
        assertNull(ceo.getBloqueadoAte());
        verify(repo).atualizar(ceo);
    }

    // ── registarFalhado ─────────────────────────────────────────────

    @Test
    void registarFalhado_abaixoDoLimiar_incrementaContadorSemBloquear() {
        CEO ceo = new CEO(1, "ceo1", "hash", 1, null);

        servico.registarFalhado(ceo);

        assertEquals(2, ceo.getTentativasLogin());
        assertNull(ceo.getBloqueadoAte());
        verify(repo).atualizar(ceo);
    }

    @Test
    void registarFalhado_aoAtingirLimiar_defineBloqueioEAtualiza() {
        // limiar = 3; CEO já tem 2 tentativas; esta é a 3ª (atinge)
        CEO ceo = new CEO(1, "ceo2", "hash", 2, null);

        servico.registarFalhado(ceo);

        assertEquals(3, ceo.getTentativasLogin());
        assertNotNull(ceo.getBloqueadoAte());
        assertTrue(ceo.getBloqueadoAte().isAfter(LocalDateTime.now()));
        verify(repo).atualizar(ceo);
    }

    // ── apagarBootstrapSeNecessario (via autenticar) ────────────────

    @Test
    void autenticar_naoAdminComBootstrapExistente_apagaBootstrap() {
        String hashCorreto = BCrypt.hashpw("pass", BCrypt.gensalt());
        CEO ceo = new CEO(1, "ceoReal", hashCorreto, 0, null);
        CEO bootstrap = new CEO(2, "admin", "hash", 0, null);
        when(repo.buscarPorNomeUtilizador("ceoReal")).thenReturn(ceo);
        when(repo.buscarPorNomeUtilizador("admin")).thenReturn(bootstrap);

        servico.autenticar("ceoReal", "pass");

        verify(repo).apagarPorNomeUtilizador("admin");
    }

    @Test
    void autenticar_naoAdminSemBootstrap_naoApagaNada() {
        String hashCorreto = BCrypt.hashpw("pass", BCrypt.gensalt());
        CEO ceo = new CEO(1, "ceoReal", hashCorreto, 0, null);
        when(repo.buscarPorNomeUtilizador("ceoReal")).thenReturn(ceo);
        when(repo.buscarPorNomeUtilizador("admin")).thenReturn(null);

        servico.autenticar("ceoReal", "pass");

        verify(repo, never()).apagarPorNomeUtilizador(any());
    }

    // ── criar ───────────────────────────────────────────────────────

    @Test
    void criar_nomeUtilizadorEmBranco_lancaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> servico.criar("", "senha123"));
        verify(repo, never()).criar(any());
    }

    @Test
    void criar_contaDefinitivaJaExiste_lancaContaCEOJaExisteException() {
        when(repo.existeContaDefinitiva()).thenReturn(true);
        assertThrows(ContaCEOJaExisteException.class,
                () -> servico.criar("catia", "senha123"));
        verify(repo, never()).criar(any());
    }

    @Test
    void criar_caminhoFeliz_persisteCeoComHashBcrypt() {
        when(repo.existeContaDefinitiva()).thenReturn(false);

        servico.criar("catia", "senha123");

        ArgumentCaptor<CEO> captor = ArgumentCaptor.forClass(CEO.class);
        verify(repo).criar(captor.capture());
        CEO persistido = captor.getValue();
        assertEquals("catia", persistido.getNomeUtilizador());
        assertTrue(BCrypt.checkpw("senha123", persistido.getHashPalavraPasse()));
        assertEquals(0, persistido.getTentativasLogin());
        assertNull(persistido.getBloqueadoAte());
    }

    // ── estaBloqueado ───────────────────────────────────────────────

    @Test
    void estaBloqueado_bloqueadoAteNulo_devolveFalse() {
        CEO ceo = new CEO(1, "c", "h", 0, null);
        assertFalse(servico.estaBloqueado(ceo));
    }

    @Test
    void estaBloqueado_bloqueadoAteNoPassado_devolveFalse() {
        CEO ceo = new CEO(1, "c", "h", 3, LocalDateTime.now().minusMinutes(1));
        assertFalse(servico.estaBloqueado(ceo));
    }

    @Test
    void estaBloqueado_bloqueadoAteNoFuturo_devolveTrue() {
        CEO ceo = new CEO(1, "c", "h", 3, LocalDateTime.now().plusMinutes(5));
        assertTrue(servico.estaBloqueado(ceo));
    }

    // ── helpers ─────────────────────────────────────────────────────

    private CEO ceoBloqueado() {
        return new CEO(1, "bloqueado", BCrypt.hashpw("pass", BCrypt.gensalt()), 3,
                LocalDateTime.now().plusMinutes(5));
    }
}
