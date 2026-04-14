package pt.trasmum.loja.repositorio.impl;

import pt.trasmum.loja.dominio.catalogo.Produto;
import pt.trasmum.loja.repositorio.interfaces.ProdutoRepositorio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProdutoRepositorioImpl implements ProdutoRepositorio {

    private final Connection connection;

    public ProdutoRepositorioImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void guardar(Produto produto) {
        String sql = "INSERT INTO Produto (codigoBarras, nome, categoria, precoBase, stockMinimo, ativo) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, produto.getCodigoBarras());
            ps.setString(2, produto.getNome());
            ps.setString(3, produto.getCategoria());
            ps.setDouble(4, produto.getPrecoBase());
            ps.setInt(5, produto.getStockMinimo());
            ps.setBoolean(6, produto.isAtivo());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) produto.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao guardar produto", e);
        }
    }

    @Override
    public void atualizar(Produto produto) {
        String sql = "UPDATE Produto SET codigoBarras=?, nome=?, categoria=?, precoBase=?, stockMinimo=?, ativo=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, produto.getCodigoBarras());
            ps.setString(2, produto.getNome());
            ps.setString(3, produto.getCategoria());
            ps.setDouble(4, produto.getPrecoBase());
            ps.setInt(5, produto.getStockMinimo());
            ps.setBoolean(6, produto.isAtivo());
            ps.setInt(7, produto.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar produto", e);
        }
    }

    @Override
    public Produto buscarPorCodigoBarras(String codigo) {
        String sql = "SELECT * FROM Produto WHERE codigoBarras = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar produto por código de barras", e);
        }
        return null;
    }

    @Override
    public Produto buscarPorId(int id) {
        String sql = "SELECT * FROM Produto WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar produto por id", e);
        }
        return null;
    }

    @Override
    public List<Produto> buscarAbaixoStockMinimo() {
        String sql = """
                SELECT p.* FROM Produto p
                WHERE p.ativo = 1
                  AND (SELECT COALESCE(SUM(l.quantidade), 0) FROM Lote l WHERE l.idProduto = p.id) < p.stockMinimo
                """;
        List<Produto> lista = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar produtos abaixo do stock mínimo", e);
        }
        return lista;
    }

    @Override
    public List<Produto> pesquisar(String query) {
        String sql = "SELECT * FROM Produto WHERE ativo = 1 AND (nome LIKE ? OR codigoBarras LIKE ? OR categoria LIKE ?)";
        List<Produto> lista = new ArrayList<>();
        String like = "%" + query + "%";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao pesquisar produtos", e);
        }
        return lista;
    }

    @Override
    public List<Produto> listarAtivos() {
        String sql = "SELECT * FROM Produto WHERE ativo = 1";
        List<Produto> lista = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar produtos ativos", e);
        }
        return lista;
    }

    private Produto mapear(ResultSet rs) throws SQLException {
        Produto p = new Produto();
        p.setId(rs.getInt("id"));
        p.setCodigoBarras(rs.getString("codigoBarras"));
        p.setNome(rs.getString("nome"));
        p.setCategoria(rs.getString("categoria"));
        p.setPrecoBase(rs.getDouble("precoBase"));
        p.setStockMinimo(rs.getInt("stockMinimo"));
        p.setAtivo(rs.getBoolean("ativo"));
        return p;
    }
}
