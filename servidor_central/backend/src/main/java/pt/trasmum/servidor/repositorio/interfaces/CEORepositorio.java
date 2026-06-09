package pt.trasmum.servidor.repositorio.interfaces;

import pt.trasmum.servidor.dominio.CEO;

public interface CEORepositorio {
    CEO buscarPorNomeUtilizador(String nomeUtilizador);
    void atualizar(CEO ceo);
    void criarSeNaoExistir(CEO ceo);
    void criar(CEO ceo);
    int contarTodos();
    boolean existeContaDefinitiva();
    void apagarPorNomeUtilizador(String nomeUtilizador);
}
