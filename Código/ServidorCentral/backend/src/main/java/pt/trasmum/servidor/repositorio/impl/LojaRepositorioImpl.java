package pt.trasmum.servidor.repositorio.impl;

import pt.trasmum.servidor.dominio.Loja;
import pt.trasmum.servidor.infra.DatabaseConnection;
import pt.trasmum.servidor.repositorio.interfaces.LojaRepositorio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LojaRepositorioImpl implements LojaRepositorio {
    private final DatabaseConnection db;

    public LojaRepositorioImpl(DatabaseConnection db) {
        this.db = db;
    }

    @Override
    public void criarSeNaoExistir(Loja loja) {
        try (Connection c = db.obter()) {
            criarSeNaoExistir(c, loja);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar loja: " + e.getMessage(), e);
        }
    }

    @Override
    public void criarSeNaoExistir(Connection conn, Loja loja) {
        String sql = "INSERT INTO Loja (idLoja, nomeLoja) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE nomeLoja = VALUES(nomeLoja)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, loja.getIdLoja());
            ps.setString(2, loja.getNomeLoja());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar loja: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Loja> listarTodas() {
        List<Loja> out = new ArrayList<>();
        try (Connection c = db.obter();
             PreparedStatement ps = c.prepareStatement("SELECT idLoja, nomeLoja FROM Loja ORDER BY idLoja");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new Loja(rs.getString("idLoja"), rs.getString("nomeLoja")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar lojas: " + e.getMessage(), e);
        }
        return out;
    }

    @Override
    public Loja buscarPorId(String idLoja) {
        try (Connection c = db.obter();
             PreparedStatement ps = c.prepareStatement("SELECT idLoja, nomeLoja FROM Loja WHERE idLoja = ?")) {
            ps.setString(1, idLoja);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? new Loja(rs.getString("idLoja"), rs.getString("nomeLoja")) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar loja: " + e.getMessage(), e);
        }
    }
}
