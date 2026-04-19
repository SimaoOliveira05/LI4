package pt.trasmum.servidor.repositorio.interfaces;

import pt.trasmum.servidor.dominio.VendaCentral;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface VendaCentralRepositorio {
    void guardar(VendaCentral venda);
    void guardar(Connection conn, VendaCentral venda);
    List<VendaCentral> buscarPorLoja(String idLoja);
    List<VendaCentral> buscarPorPeriodo(LocalDate inicio, LocalDate fim);
    List<VendaCentral> buscarPorLojaEPeriodo(String idLoja, LocalDate inicio, LocalDate fim);
    double totalVendasPorPeriodo(LocalDate inicio, LocalDate fim);
    double totalVendasPorLojaEPeriodo(String idLoja, LocalDate inicio, LocalDate fim);
    int totalTransacoesPorPeriodo(LocalDate inicio, LocalDate fim);
    int totalTransacoesPorLojaEPeriodo(String idLoja, LocalDate inicio, LocalDate fim);
    Map<String, Double> vendasPorCategoria(LocalDate inicio, LocalDate fim);
    Map<String, Double> vendasPorCategoriaPorLoja(String idLoja, LocalDate inicio, LocalDate fim);
    Map<String, Double> vendasMensaisPorLoja(String idLoja, int ano);
    Map<String, Double> totaisPorMetodoPagamento(LocalDate inicio, LocalDate fim);
    List<Object[]> relatorioAgregado(String idLoja, LocalDate inicio, LocalDate fim, String categoria);
}
