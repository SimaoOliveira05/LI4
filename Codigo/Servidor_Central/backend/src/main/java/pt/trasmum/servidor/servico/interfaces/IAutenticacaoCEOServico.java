package pt.trasmum.servidor.servico.interfaces;

import pt.trasmum.servidor.dominio.CEO;

public interface IAutenticacaoCEOServico {
    CEO autenticar(String nomeUtilizador, String palavraPasse);
    void criar(String nomeUtilizador, String palavraPasse);
    void registarFalhado(CEO ceo);
    void limparBloqueio(CEO ceo);
    boolean estaBloqueado(CEO ceo);
}
