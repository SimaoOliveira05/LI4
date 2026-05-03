package pt.trasmum.loja.repositorio.impl;

import pt.trasmum.loja.dominio.core.EstadoSincronizacao;
import pt.trasmum.loja.dominio.tesouraria.DetalheNumerario;
import pt.trasmum.loja.dominio.tesouraria.DetalheNumerario.Denominacao;
import pt.trasmum.loja.dominio.tesouraria.Sangria;
import pt.trasmum.loja.dominio.tesouraria.SessaoCaixa;
import pt.trasmum.loja.repositorio.interfaces.SessaoCaixaRepositorio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SessaoCaixaRepositorioImpl implements SessaoCaixaRepositorio {

    private final Connection connection;

    public SessaoCaixaRepositorioImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void guardar(SessaoCaixa sessao) {
        String sql = "INSERT INTO SessaoCaixa (idLoja, estadoSincronizacao, idUtilizador, fundoInicial, saldoAtual, dataAbertura, dataEncerramento) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, sessao.getIdLoja());
            ps.setString(2, sessao.getEstadoSincronizacao().name());
            ps.setInt(3, sessao.getIdUtilizador());
            ps.setDouble(4, sessao.getFundoInicial());
            ps.setDouble(5, sessao.getSaldoAtual());
            ps.setTimestamp(6, Timestamp.valueOf(sessao.getDataAbertura()));
            if (sessao.getDataEncerramento() == null) ps.setNull(7, Types.TIMESTAMP);
            else ps.setTimestamp(7, Timestamp.valueOf(sessao.getDataEncerramento()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) sessao.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao guardar sessão de caixa", e);
        }

        for (DetalheNumerario d : sessao.getDetalheFundoInicial()) {
            d.setIdSessaoCaixa(sessao.getId());
            guardarDetalhe(d);
        }
    }

    @Override
    public void atualizar(SessaoCaixa sessao) {
        String sql = "UPDATE SessaoCaixa SET estadoSincronizacao=?, saldoAtual=?, saldoContado=?, diferencaFecho=?, dataEncerramento=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, sessao.getEstadoSincronizacao().name());
            ps.setDouble(2, sessao.getSaldoAtual());
            if (sessao.getSaldoContado() == null) ps.setNull(3, Types.DECIMAL);
            else ps.setDouble(3, sessao.getSaldoContado());
            if (sessao.getDiferencaFecho() == null) ps.setNull(4, Types.DECIMAL);
            else ps.setDouble(4, sessao.getDiferencaFecho());
            if (sessao.getDataEncerramento() == null) ps.setNull(5, Types.TIMESTAMP);
            else ps.setTimestamp(5, Timestamp.valueOf(sessao.getDataEncerramento()));
            ps.setInt(6, sessao.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar sessão de caixa", e);
        }
    }

    @Override
    public SessaoCaixa buscarSessaoAtiva(int idUtilizador) {
        String sql = "SELECT * FROM SessaoCaixa WHERE idUtilizador = ? AND dataEncerramento IS NULL ORDER BY dataAbertura DESC LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idUtilizador);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapearComDetalhes(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar sessão ativa", e);
        }
        return null;
    }

    @Override
    public List<SessaoCaixa> buscarTodasAbertas() {
        String sql = "SELECT * FROM SessaoCaixa WHERE dataEncerramento IS NULL";
        List<SessaoCaixa> lista = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapearComDetalhes(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar sessões abertas", e);
        }
        return lista;
    }

    @Override
    public void reverterEmTransito() {
        String sql = "UPDATE SessaoCaixa SET estadoSincronizacao = 'PENDENTE' WHERE estadoSincronizacao = 'EM_TRANSITO'";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao reverter sessões em trânsito", e);
        }
    }

    @Override
    public List<SessaoCaixa> buscarPendentes() {
        // Apenas sessões encerradas (dataEncerramento NOT NULL)
        String sql = "SELECT * FROM SessaoCaixa WHERE estadoSincronizacao = 'PENDENTE' AND dataEncerramento IS NOT NULL";
        List<SessaoCaixa> lista = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapearComDetalhes(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar sessões pendentes", e);
        }
        return lista;
    }

    @Override
    public void guardarSangria(Sangria sangria) {
        String sql = "INSERT INTO Sangria (idSessaoCaixa, dataHora, total) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, sangria.getIdSessaoCaixa());
            ps.setTimestamp(2, Timestamp.valueOf(sangria.getDataHora()));
            ps.setDouble(3, sangria.getTotal());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) sangria.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao guardar sangria", e);
        }
        for (DetalheNumerario d : sangria.getValor()) {
            d.setIdSangria(sangria.getId());
            guardarDetalhe(d);
        }
    }

    private void guardarDetalhe(DetalheNumerario detalhe) {
        String sql = "INSERT INTO DetalheNumerario (idSessaoCaixa, idSangria, denominacao, quantidade, subtotal) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (detalhe.getIdSessaoCaixa() == 0) ps.setNull(1, Types.INTEGER);
            else ps.setInt(1, detalhe.getIdSessaoCaixa());
            if (detalhe.getIdSangria() == 0) ps.setNull(2, Types.INTEGER);
            else ps.setInt(2, detalhe.getIdSangria());
            ps.setString(3, detalhe.getDenominacao().name());
            ps.setInt(4, detalhe.getQuantidade());
            ps.setDouble(5, detalhe.getSubtotal());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) detalhe.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao guardar detalhe numérico", e);
        }
    }

    private SessaoCaixa mapearComDetalhes(ResultSet rs) throws SQLException {
        SessaoCaixa s = new SessaoCaixa();
        s.setId(rs.getInt("id"));
        s.setIdLoja(rs.getString("idLoja"));
        s.setEstadoSincronizacao(EstadoSincronizacao.valueOf(rs.getString("estadoSincronizacao")));
        s.setIdUtilizador(rs.getInt("idUtilizador"));
        s.setFundoInicial(rs.getDouble("fundoInicial"));
        s.setSaldoAtual(rs.getDouble("saldoAtual"));
        double sc = rs.getDouble("saldoContado");
        if (!rs.wasNull()) s.setSaldoContado(sc);
        double df = rs.getDouble("diferencaFecho");
        if (!rs.wasNull()) s.setDiferencaFecho(df);
        s.setDataAbertura(rs.getTimestamp("dataAbertura").toLocalDateTime());
        Timestamp enc = rs.getTimestamp("dataEncerramento");
        if (enc != null) s.setDataEncerramento(enc.toLocalDateTime());
        s.setFundoInicial(buscarDetalhes(s.getId(), 0));
        s.setSangrias(buscarSangrias(s.getId()));
        return s;
    }

    private List<DetalheNumerario> buscarDetalhes(int idSessaoCaixa, int idSangria) {
        String sql;
        List<DetalheNumerario> lista = new ArrayList<>();
        try {
            if (idSangria == 0) {
                sql = "SELECT * FROM DetalheNumerario WHERE idSessaoCaixa = ? AND idSangria IS NULL";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setInt(1, idSessaoCaixa);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) lista.add(mapearDetalhe(rs));
                    }
                }
            } else {
                sql = "SELECT * FROM DetalheNumerario WHERE idSangria = ?";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setInt(1, idSangria);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) lista.add(mapearDetalhe(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar detalhes numéricos", e);
        }
        return lista;
    }

    private List<Sangria> buscarSangrias(int idSessaoCaixa) {
        String sql = "SELECT * FROM Sangria WHERE idSessaoCaixa = ?";
        List<Sangria> lista = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idSessaoCaixa);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Sangria sang = new Sangria();
                    sang.setId(rs.getInt("id"));
                    sang.setIdSessaoCaixa(rs.getInt("idSessaoCaixa"));
                    sang.setDataHora(rs.getTimestamp("dataHora").toLocalDateTime());
                    sang.setValor(buscarDetalhes(0, sang.getId()));
                    lista.add(sang);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar sangrias", e);
        }
        return lista;
    }

    private DetalheNumerario mapearDetalhe(ResultSet rs) throws SQLException {
        DetalheNumerario d = new DetalheNumerario();
        d.setId(rs.getInt("id"));
        int idSc = rs.getInt("idSessaoCaixa");
        if (!rs.wasNull()) d.setIdSessaoCaixa(idSc);
        int idSang = rs.getInt("idSangria");
        if (!rs.wasNull()) d.setIdSangria(idSang);
        d.setDenominacao(Denominacao.valueOf(rs.getString("denominacao")));
        d.setQuantidade(rs.getInt("quantidade"));
        d.setSubtotal(rs.getDouble("subtotal"));
        return d;
    }
}
