package pt.trasmum.servidor.repositorio.interfaces;

import pt.trasmum.servidor.dominio.EstadoPagamentoRemessa;
import pt.trasmum.servidor.dominio.RemessaCentral;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;

public interface RemessaCentralRepositorio {
    void guardar(RemessaCentral remessa);
    void guardar(Connection conn, RemessaCentral remessa);
    void atualizarEstadoPagamento(Connection conn, String idLoja, int idOriginalRemessa, EstadoPagamentoRemessa estado);
    List<RemessaCentral> listarTodas();
    List<RemessaCentral> buscarPorLoja(String idLoja);
    List<RemessaCentral> buscarPorEstado(EstadoPagamentoRemessa estado);
    List<RemessaCentral> buscarPorLojaEEstado(String idLoja, EstadoPagamentoRemessa estado);
    double totalDespesasPorPeriodo(LocalDate inicio, LocalDate fim);
    double totalDespesasPorLojaEPeriodo(String idLoja, LocalDate inicio, LocalDate fim);
}
