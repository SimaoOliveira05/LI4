package pt.trasmum.servidor.servico.interfaces;

import pt.trasmum.servidor.dto.api.RemessaCentralDTO;

import java.util.List;

public interface IRemessaServico {
    List<RemessaCentralDTO> listarRemessas(String idLoja, String estadoPagamento);
}
