package pt.trasmum.loja.repositorio.interfaces;

import pt.trasmum.loja.dominio.fornecedores.Fornecedor;

import java.util.List;

public interface FornecedorRepositorio {
    void guardar(Fornecedor fornecedor);
    void atualizar(Fornecedor fornecedor);
    Fornecedor buscarPorId(int id);
    List<Fornecedor> listarTodos();
}
