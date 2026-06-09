package pt.trasmum.loja.servico;

import pt.trasmum.loja.dominio.core.PerfilUtilizador;

public record UtilizadorDTO(String nomeUtilizador, String palavraPasse, PerfilUtilizador perfil) {}
