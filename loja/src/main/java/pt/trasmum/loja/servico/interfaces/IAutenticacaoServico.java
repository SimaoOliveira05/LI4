package pt.trasmum.loja.servico.interfaces;

import pt.trasmum.loja.dominio.core.Utilizador;

public interface IAutenticacaoServico {
    Utilizador autenticar(String nomeUtilizador, String palavraPasse);
    void encerrarSessao(Utilizador utilizador);
    boolean verificarSessaoDuplicada(Utilizador utilizador);
}
