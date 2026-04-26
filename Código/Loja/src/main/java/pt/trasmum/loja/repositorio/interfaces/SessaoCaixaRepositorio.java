package pt.trasmum.loja.repositorio.interfaces;

import pt.trasmum.loja.dominio.tesouraria.Sangria;
import pt.trasmum.loja.dominio.tesouraria.SessaoCaixa;

import java.util.List;

public interface SessaoCaixaRepositorio {
    void guardar(SessaoCaixa sessao);
    void atualizar(SessaoCaixa sessao);
    void guardarSangria(Sangria sangria);
    SessaoCaixa buscarSessaoAtiva(int idUtilizador);
    List<SessaoCaixa> buscarTodasAbertas();
    void reverterEmTransito();
    List<SessaoCaixa> buscarPendentes();
}
