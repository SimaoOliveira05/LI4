package pt.trasmum.loja.servico.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.trasmum.loja.dominio.core.PerfilUtilizador;
import pt.trasmum.loja.dominio.core.Utilizador;
import pt.trasmum.loja.dominio.exceptions.ExcecoesSeguranca.AcessoNegadoException;

import static org.junit.jupiter.api.Assertions.*;

class AutorizacaoServicoTest {

    private AutorizacaoServico servico;

    @BeforeEach
    void setUp() {
        servico = new AutorizacaoServico();
    }

    @Test
    void exigirPerfil_perfilPermitido_naoLancaExcecao() {
        Utilizador u = new Utilizador("gestor", "hash", PerfilUtilizador.GESTOR);
        assertDoesNotThrow(() -> servico.exigirPerfil(u, PerfilUtilizador.GESTOR, PerfilUtilizador.CEO));
    }

    @Test
    void exigirPerfil_perfilNaoPermitido_lancaAcessoNegadoException() {
        Utilizador u = new Utilizador("func", "hash", PerfilUtilizador.FUNCIONARIO);
        assertThrows(AcessoNegadoException.class,
                () -> servico.exigirPerfil(u, PerfilUtilizador.GESTOR, PerfilUtilizador.CEO));
    }

    @Test
    void temPerfil_perfilCorreto_devolveTrue() {
        Utilizador u = new Utilizador("gestor", "hash", PerfilUtilizador.GESTOR);
        assertTrue(servico.temPerfil(u, PerfilUtilizador.GESTOR));
    }

    @Test
    void temPerfil_perfilErrado_devolveFalse() {
        Utilizador u = new Utilizador("func", "hash", PerfilUtilizador.FUNCIONARIO);
        assertFalse(servico.temPerfil(u, PerfilUtilizador.GESTOR));
    }
}
