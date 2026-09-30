package service;

import config.ConexaoBD;
import dao.VendaDAO;
import entidades.FormaPagamento;
import entidades.Venda;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Regras de finalização de venda. */
public class VendaService {

    private final VendaDAO vendaDAO = new VendaDAO();

    public static double arredondar(double valor) {
        return Math.round(valor * 100) / 100.0;
    }

    public static double total(List<Venda> itens) {
        double total = 0;
        for (Venda item : itens) {
            total += item.getTotal();
        }
        return arredondar(total);
    }

    /** Troco para pagamento em dinheiro; negativo quando o valor recebido não cobre o total. */
    public static double troco(double total, double valorRecebido) {
        return arredondar(valorRecebido - total);
    }

    /**
     * Finaliza a venda em uma única transação: soma o consumo de cada
     * ingrediente em todo o carrinho, confere o estoque e só então dá baixa,
     * cria o pedido com o pagamento e registra os itens. Se algo falhar, nada
     * é alterado. Retorna o número do pedido.
     *
     * @param valorRecebido usado apenas em dinheiro; nas demais formas o valor
     * recebido é o próprio total.
     */
    public int finalizarVenda(List<Venda> itens, FormaPagamento forma, double valorRecebido)
            throws SQLException, EstoqueInsuficienteException {
        if (itens.isEmpty()) {
            throw new IllegalArgumentException("Adicione itens antes de finalizar.");
        }
        if (forma == null) {
            throw new IllegalArgumentException("Escolha a forma de pagamento.");
        }
        double total = total(itens);
        double recebido = forma == FormaPagamento.DINHEIRO ? arredondar(valorRecebido) : total;
        double troco = troco(total, recebido);
        if (troco < 0) {
            throw new IllegalArgumentException(String.format(
                    "Valor recebido (R$ %.2f) é menor que o total (R$ %.2f).", recebido, total));
        }

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
                int pedidoId = vendaDAO.criarPedido(con, total, forma, recebido, troco);
                for (Venda item : itens) {
                    vendaDAO.registrarItem(con, pedidoId, item);
                }
                con.commit();
                return pedidoId;
            } catch (SQLException | EstoqueInsuficienteException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }
}
