package service;

import config.ConexaoBD;
import dao.VendaDAO;
import entidades.Venda;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Regras de finalização de venda. */
public class VendaService {

    private final VendaDAO vendaDAO = new VendaDAO();

    /**
     * Finaliza a venda em uma única transação: soma o consumo de cada
     * ingrediente em todo o carrinho, confere o estoque e só então dá baixa
     * e registra os itens. Se faltar qualquer ingrediente, nada é alterado.
     */
    public void finalizarVenda(List<Venda> itens) throws SQLException, EstoqueInsuficienteException {
        try (Connection con = ConexaoBD.getConnection()) {
            con.setAutoCommit(false);
            try {
                Map<String, Integer> consumo = new TreeMap<>();
                for (Venda item : itens) {
                    for (Map.Entry<String, Integer> ing : vendaDAO.ingredientesDoProduto(con, item.getProduto()).entrySet()) {
                        consumo.merge(ing.getKey(), ing.getValue() * item.getQuantidade(), Integer::sum);
                    }
                }

                Map<String, Integer> disponivel = vendaDAO.bloquearEstoque(con, consumo.keySet());
                StringBuilder faltas = new StringBuilder();
                for (Map.Entry<String, Integer> c : consumo.entrySet()) {
                    int temNoEstoque = disponivel.getOrDefault(c.getKey(), 0);
                    if (c.getValue() > temNoEstoque) {
                        faltas.append("\n• ").append(c.getKey())
                                .append(": necessário ").append(c.getValue())
                                .append(", disponível ").append(temNoEstoque);
                    }
                }
                if (faltas.length() > 0) {
                    throw new EstoqueInsuficienteException("Estoque insuficiente para finalizar a venda:" + faltas);
                }

                for (Map.Entry<String, Integer> c : consumo.entrySet()) {
                    vendaDAO.baixarEstoque(con, c.getKey(), c.getValue());
                }
                for (Venda item : itens) {
                    vendaDAO.registrarItem(con, item);
                }
                con.commit();
            } catch (SQLException | EstoqueInsuficienteException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }
}
