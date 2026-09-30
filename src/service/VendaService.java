package service;

import entidades.FormaPagamento;
import entidades.Venda;
import java.sql.SQLException;
import java.util.List;

/** Venda direta de balcão e utilitários de valores. */
public class VendaService {

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
     * Finaliza uma venda de balcão: confere e baixa o estoque, envia os
     * lanches para a cozinha, registra o pagamento no caixa aberto e fecha o
     * pedido. Retorna o pedido criado (com a senha para chamar o cliente).
     */
    public entidades.Pedido finalizarVenda(List<Venda> itens, FormaPagamento forma, double valorRecebido, int clienteId)
            throws SQLException, EstoqueInsuficienteException {
        return new PedidoService().vendaBalcao(itens, forma, valorRecebido, clienteId);
    }
}
