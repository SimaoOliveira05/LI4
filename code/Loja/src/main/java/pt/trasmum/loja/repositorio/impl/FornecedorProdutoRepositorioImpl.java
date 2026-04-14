package pt.trasmum.loja.repositorio.impl;

import pt.trasmum.loja.dominio.catalogo.Produto;
import pt.trasmum.loja.dominio.fornecedores.FornecedorProduto;
import pt.trasmum.loja.repositorio.interfaces.FornecedorProdutoRepositorio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FornecedorProdutoRepositorioImpl implements FornecedorProdutoRepositorio {

    private final Connection connection;

    public FornecedorProdutoRepositorioImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void associar(int idFornecedor, int idProduto, double precoFornecedor) {
        String sql = "INSERT INTO FornecedorProduto (idFornecedor, idProduto, precoFornecedor) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idFornecedor);
            ps.setInt(2, idProduto);
            ps.setDouble(3, precoFornecedor);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao associar produto a fornecedor", e);
        }
    }

    @Override
    public void desassociar(int idFornecedor, int idProduto) {
        String sql = "DELETE FROM FornecedorProduto WHERE idFornecedor = ? AND idProduto = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idFornecedor);
            ps.setInt(2, idProduto);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao desassociar produto do fornecedor", e);
        }
    }

    @Override
    public void atualizarPreco(int idFornecedor, int idProduto, double novoPreco) {
        String sql = "UPDATE FornecedorProduto SET precoFornecedor = ? WHERE idFornecedor = ? AND idProduto = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDouble(1, novoPreco);
            ps.setInt(2, idFornecedor);
            ps.setInt(3, idProduto);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar preço do fornecedor", e);
        }
    }

    @Override
    public List<Produto> listarProdutosDoFornecedor(int idFornecedor) {
        String sql = "SELECT p.* FROM Produto p " +
                     "INNER JOIN FornecedorProduto fp ON fp.idProduto = p.id " +
                     "WHERE fp.idFornecedor = ? AND p.ativo = 1 " +
                     "ORDER BY p.nome";
        List<Produto> lista = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idFornecedor);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Produto p = new Produto();
                    p.setId(rs.getInt("id"));
                    p.setCodigoBarras(rs.getString("codigoBarras"));
                    p.setNome(rs.getString("nome"));
                    p.setCategoria(rs.getString("categoria"));
                    p.setPrecoBase(rs.getDouble("precoBase"));
                    p.setStockMinimo(rs.getInt("stockMinimo"));
                    p.setAtivo(rs.getBoolean("ativo"));
                    lista.add(p);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar produtos do fornecedor", e);
        }
        return lista;
    }

    @Override
    public List<FornecedorProduto> listarDetalhado(int idFornecedor) {
        String sql = "SELECT * FROM FornecedorProduto WHERE idFornecedor = ?";
        List<FornecedorProduto> lista = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idFornecedor);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(new FornecedorProduto(
                        rs.getInt("idFornecedor"),
                        rs.getInt("idProduto"),
                        rs.getDouble("precoFornecedor")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar catálogo do fornecedor", e);
        }
        return lista;
    }

    @Override
    public boolean fornece(int idFornecedor, int idProduto) {
        String sql = "SELECT 1 FROM FornecedorProduto WHERE idFornecedor = ? AND idProduto = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idFornecedor);
            ps.setInt(2, idProduto);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao verificar associação fornecedor-produto", e);
        }
    }

    @Override
    public double precoDoFornecedor(int idFornecedor, int idProduto) {
        String sql = "SELECT precoFornecedor FROM FornecedorProduto WHERE idFornecedor = ? AND idProduto = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idFornecedor);
            ps.setInt(2, idProduto);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("precoFornecedor");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao obter preço do fornecedor", e);
        }
        return -1;
    }
}
