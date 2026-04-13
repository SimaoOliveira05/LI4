package pt.trasmum.loja.servico.interfaces;

import pt.trasmum.loja.dominio.core.Utilizador;
import pt.trasmum.loja.dominio.vendas.Devolucao;

import java.time.LocalDate;
import java.util.List;

public interface IDevolucaoServico {
    Devolucao processar(Utilizador utilizador, String numeroFatura, String codigoBarras,
                        LocalDate dataValidadeEmbalagem, int quantidade);
    List<Devolucao> obterDevolucoesPendentes();
}
