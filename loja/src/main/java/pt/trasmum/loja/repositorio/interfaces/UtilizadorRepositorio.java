package pt.trasmum.loja.repositorio.interfaces;

import pt.trasmum.loja.dominio.core.Utilizador;

import java.util.List;

public interface UtilizadorRepositorio {
    Utilizador buscarPorCredenciais(String nomeUtilizador, String palavraPasse);
    Utilizador buscarPorId(int id);
    /** Procura por nome de utilizador independentemente de estar ativo ou não. */
    Utilizador buscarPorNome(String nomeUtilizador);
    void guardar(Utilizador utilizador);
    void atualizar(Utilizador utilizador);
    void desativar(int id);
    boolean verificarSessaoAtiva(int id);
    List<Utilizador> listarAtivos();
    /** Marca todos os utilizadores como fora de sessão. Chamado no arranque para limpar sessões órfãs. */
    void limparSessoesAtivas();
}
