package pt.trasmum.loja.repositorio.interfaces;

import pt.trasmum.loja.dominio.catalogo.Desconto;
import pt.trasmum.loja.dominio.catalogo.Lote;

import java.util.List;

public interface LoteRepositorio {
    void guardar(Lote lote);
    void atualizar(Lote lote);
    List<Lote> buscarLotesFEFO(int idProduto);
    List<Lote> buscarLotesComDesconto();
    List<Lote> buscarAbaixoDaValidade(int diasLimite);
    void guardarDesconto(Desconto desconto, int idLote);
    /** Devolve um mapa idProduto → stock total (soma de quantidades em todos os lotes). */
    java.util.Map<Integer, Integer> stockPorProduto();
}
