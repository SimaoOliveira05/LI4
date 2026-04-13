package pt.trasmum.loja.repositorio.impl;

import pt.trasmum.loja.dominio.fornecedores.EstadoPedido;
import pt.trasmum.loja.dominio.fornecedores.LinhaPedidoRemessa;
import pt.trasmum.loja.dominio.fornecedores.PedidoRemessa;
import pt.trasmum.loja.repositorio.interfaces.PedidoRemessaRepositorio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PedidoRemessaRepositorioImpl implements PedidoRemessaRepositorio {

    private final Connection connection;

    public PedidoRemessaRepositorioImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void guardar(PedidoRemessa pedido) {
        String sql = "INSERT INTO PedidoRemessa (idFornecedor, idUtilizador, dataCriacao, estado) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, pedido.getIdFornecedor());
            ps.setInt(2, pedido.getIdUtilizador());
            ps.setDate(3, Date.valueOf(pedido.getDataCriacao()));
            ps.setString(4, pedido.getEstado().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) pedido.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao guardar pedido de remessa", e);
        }

        for (LinhaPedidoRemessa linha : pedido.getLinhas()) {
            linha.setIdPedidoRemessa(pedido.getId());
            guardarLinha(linha);
        }
    }

    @Override
    public void atualizar(PedidoRemessa pedido) {
        String sql = "UPDATE PedidoRemessa SET estado = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, pedido.getEstado().name());
            ps.setInt(2, pedido.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar pedido de remessa", e);
        }
    }

    @Override
    public List<PedidoRemessa> buscarPorEstado(EstadoPedido estado) {
        String sql = "SELECT * FROM PedidoRemessa WHERE estado = ?";
        List<PedidoRemessa> lista = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, estado.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearComLinhas(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar pedidos por estado", e);
        }
        return lista;
    }

    @Override
    public List<PedidoRemessa> buscarPendentesPorFornecedor(int idFornecedor) {
        String sql = "SELECT * FROM PedidoRemessa WHERE estado = 'PENDENTE' AND idFornecedor = ?";
        List<PedidoRemessa> lista = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idFornecedor);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearComLinhas(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar pedidos pendentes por fornecedor", e);
        }
        return lista;
    }

    private void guardarLinha(LinhaPedidoRemessa linha) {
        String sql = "INSERT INTO LinhaPedidoRemessa (idPedidoRemessa, idProduto, quantidadePretendida) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, linha.getIdPedidoRemessa());
            ps.setInt(2, linha.getIdProduto());
            ps.setInt(3, linha.getQuantidadePretendida());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) linha.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao guardar linha do pedido de remessa", e);
        }
    }

    private PedidoRemessa mapearComLinhas(ResultSet rs) throws SQLException {
        PedidoRemessa p = new PedidoRemessa();
        p.setId(rs.getInt("id"));
        p.setIdFornecedor(rs.getInt("idFornecedor"));
        p.setIdUtilizador(rs.getInt("idUtilizador"));
        p.setDataCriacao(rs.getDate("dataCriacao").toLocalDate());
        p.setEstado(EstadoPedido.valueOf(rs.getString("estado")));
        p.setLinhas(buscarLinhas(p.getId()));
        return p;
    }

    private List<LinhaPedidoRemessa> buscarLinhas(int idPedido) {
        String sql = "SELECT * FROM LinhaPedidoRemessa WHERE idPedidoRemessa = ?";
        List<LinhaPedidoRemessa> linhas = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idPedido);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LinhaPedidoRemessa l = new LinhaPedidoRemessa();
                    l.setId(rs.getInt("id"));
                    l.setIdPedidoRemessa(rs.getInt("idPedidoRemessa"));
                    l.setIdProduto(rs.getInt("idProduto"));
                    l.setQuantidadePretendida(rs.getInt("quantidadePretendida"));
                    linhas.add(l);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar linhas do pedido", e);
        }
        return linhas;
    }
}
