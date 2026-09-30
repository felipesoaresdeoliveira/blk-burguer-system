package service;

import config.ConexaoBD;
import dao.AdicionalDAO;
import dao.CaixaDAO;
import dao.PedidoDAO;
import dao.VendaDAO;
import entidades.Adicional;
import entidades.FormaPagamento;
import entidades.Modulo;
import entidades.Pedido;
import entidades.SituacaoPedido;
import entidades.StatusCozinha;
import entidades.TipoPedido;
import entidades.Venda;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Regras de pedidos: abertura (mesa, balcão, retirada, delivery), lançamento
 * de itens com baixa de estoque, pagamento em uma ou mais formas vinculado ao
 * caixa, cozinha e entrega.
 */
public class PedidoService {

    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final VendaDAO vendaDAO = new VendaDAO();
    private final CaixaDAO caixaDAO = new CaixaDAO();
    private final AdicionalDAO adicionalDAO = new AdicionalDAO();

    /** Executa um bloco em transação: commit no fim ou rollback em qualquer erro. */
    private interface Transacao<T> {
        T executar(Connection con) throws SQLException, EstoqueInsuficienteException;
    }

    private <T> T emTransacao(Transacao<T> bloco) throws SQLException, EstoqueInsuficienteException {
        try (Connection con = ConexaoBD.getConnection()) {
            con.setAutoCommit(false);
            try {
                T resultado = bloco.executar(con);
                con.commit();
                return resultado;
            } catch (SQLException | EstoqueInsuficienteException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }

    private <T> T semEstoque(Transacao<T> bloco) throws SQLException {
        try {
            return emTransacao(bloco);
        } catch (EstoqueInsuficienteException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    // ------------------------------------------------------------ abertura

    /** Abre um pedido. Mesa exige mesa livre; delivery exige cliente e endereço. */
    public Pedido abrir(Pedido novo) throws SQLException {
        Sessao.exigir(Modulo.PEDIDOS);
        validarAbertura(novo);
        int id = semEstoque(con -> abrirEm(con, novo));
        return pedidoDAO.buscar(id);
    }

    private void validarAbertura(Pedido p) {
        if (p.getTipo() == TipoPedido.MESA && p.getMesaId() <= 0) {
            throw new IllegalArgumentException("Escolha a mesa.");
        }
        if (p.getTipo() == TipoPedido.DELIVERY) {
            if (p.getClienteId() <= 0) {
                throw new IllegalArgumentException("Delivery precisa de um cliente.");
            }
            if (p.getEnderecoEntrega() == null || p.getEnderecoEntrega().trim().isEmpty()) {
                throw new IllegalArgumentException("Informe o endereço de entrega.");
            }
        } else {
            p.setTaxaEntrega(0);
        }
        if (p.getTaxaEntrega() < 0) {
            throw new IllegalArgumentException("A taxa de entrega não pode ser negativa.");
        }
    }

    private int abrirEm(Connection con, Pedido p) throws SQLException {
        if (p.getTipo().usaSenha()) {
            p.setSenha(pedidoDAO.proximaSenha(con));
        }
        return pedidoDAO.inserir(con, p, Sessao.usuarioId());
    }

    // ------------------------------------------------------------ itens

    /**
     * Lança itens no pedido aberto: confere o estoque somando o consumo de
     * todos os itens, dá baixa e envia para a cozinha o que não é bebida.
     */
    public Pedido lancarItens(int pedidoId, List<Venda> itens) throws SQLException, EstoqueInsuficienteException {
        Sessao.exigir(Modulo.PEDIDOS);
        emTransacao(con -> {
            lancarEm(con, pedidoId, itens);
            return null;
        });
        return pedidoDAO.buscar(pedidoId);
    }

    private Pedido pedidoAberto(Connection con, int pedidoId) throws SQLException {
        pedidoDAO.bloquear(con, pedidoId);
        Pedido p = pedidoDAO.buscar(con, pedidoId);
        if (p == null) {
            throw new IllegalArgumentException("Pedido não encontrado.");
        }
        if (p.getSituacao() != SituacaoPedido.ABERTO) {
            throw new IllegalStateException("O pedido " + p.getId() + " não está mais aberto.");
        }
        return p;
    }

    private void lancarEm(Connection con, int pedidoId, List<Venda> itens) throws SQLException, EstoqueInsuficienteException {
        if (itens.isEmpty()) {
            throw new IllegalArgumentException("Adicione pelo menos um item.");
        }
        pedidoAberto(con, pedidoId);
        Map<String, Adicional> adicionais = adicionalDAO.ativos(con);
        Map<String, Integer> consumo = new TreeMap<>();
        for (Venda item : itens) {
            if (item.getQuantidade() <= 0) {
                throw new IllegalArgumentException("Quantidade inválida para " + item.getProduto() + ".");
            }
            double preco = pedidoDAO.precoProduto(con, item.getProduto());
            if (preco < 0) {
                throw new IllegalArgumentException("Produto não encontrado: " + item.getProduto());
            }
            // Preço sempre recalculado aqui: produto + adicionais escolhidos.
            for (String nome : item.getAdicionais()) {
                Adicional a = adicionais.get(nome);
                if (a == null) {
                    throw new IllegalArgumentException("Adicional indisponível: " + nome);
                }
                preco += a.getPreco();
            }
            item.setValor(VendaService.arredondar(preco));
            for (Map.Entry<String, Integer> ing : consumoDoItem(con, item, adicionais).entrySet()) {
                consumo.merge(ing.getKey(), ing.getValue(), Integer::sum);
            }
        }
        Map<String, Integer> disponivel = vendaDAO.bloquearEstoque(con, consumo.keySet());
        StringBuilder faltas = new StringBuilder();
        for (Map.Entry<String, Integer> c : consumo.entrySet()) {
            int temNoEstoque = disponivel.getOrDefault(c.getKey(), 0);
            if (c.getValue() > temNoEstoque) {
                faltas.append("\n• ").append(c.getKey()).append(": necessário ").append(c.getValue())
                        .append(", disponível ").append(temNoEstoque);
            }
        }
        if (faltas.length() > 0) {
            throw new EstoqueInsuficienteException("Estoque insuficiente:" + faltas);
        }
        for (Map.Entry<String, Integer> c : consumo.entrySet()) {
            vendaDAO.baixarEstoque(con, c.getKey(), c.getValue());
        }
        for (Venda item : itens) {
            item.setTotal(VendaService.arredondar(item.getValor() * item.getQuantidade()));
            item.setStatusCozinha(pedidoDAO.vaiParaCozinha(con, item.getProduto()) ? StatusCozinha.RECEBIDO : null);
            pedidoDAO.inserirItem(con, pedidoId, item);
        }
        pedidoDAO.recalcularTotal(con, pedidoId);
        // Novo pedido na mesa cancela o "pediu a conta".
        pedidoDAO.atualizar(con, pedidoId, "conta_solicitada = FALSE");
    }

    /** Remove um item que a cozinha ainda não começou e devolve o estoque. */
    public Pedido removerItem(int pedidoId, int itemId) throws SQLException {
        Sessao.exigir(Modulo.PEDIDOS);
        semEstoque(con -> {
            Pedido p = pedidoAberto(con, pedidoId);
            Venda item = pedidoDAO.item(con, itemId);
            if (item == null || p.getItens().stream().noneMatch(v -> v.getId() == itemId)) {
                throw new IllegalArgumentException("Item não pertence a este pedido.");
            }
            if (item.getStatusCozinha() != null && item.getStatusCozinha() != StatusCozinha.RECEBIDO) {
                throw new IllegalStateException("A cozinha já começou este item (" + item.getStatusCozinha() + ").");
            }
            devolverEstoque(con, item);
            pedidoDAO.removerItem(con, itemId);
            pedidoDAO.recalcularTotal(con, pedidoId);
            return null;
        });
        return pedidoDAO.buscar(pedidoId);
    }

    private void devolverEstoque(Connection con, Venda item) throws SQLException {
        for (Map.Entry<String, Integer> ing : consumoDoItem(con, item, adicionalDAO.ativos(con)).entrySet()) {
            vendaDAO.devolverEstoque(con, ing.getKey(), ing.getValue());
        }
    }

    /**
     * Estoque consumido por um item: ficha técnica sem os ingredientes
     * retirados, mais os ingredientes dos adicionais, vezes a quantidade.
     */
    private Map<String, Integer> consumoDoItem(Connection con, Venda item, Map<String, Adicional> adicionais) throws SQLException {
        Map<String, Integer> consumo = new TreeMap<>();
        for (Map.Entry<String, Integer> ing : vendaDAO.ingredientesDoProduto(con, item.getProduto()).entrySet()) {
            if (!item.getRemocoes().contains(ing.getKey())) {
                consumo.merge(ing.getKey(), ing.getValue() * item.getQuantidade(), Integer::sum);
            }
        }
        for (String nome : item.getAdicionais()) {
            Adicional a = adicionais.get(nome);
            if (a != null && a.getIngrediente() != null) {
                consumo.merge(a.getIngrediente(), a.getQuantidade() * item.getQuantidade(), Integer::sum);
            }
        }
        return consumo;
    }

    public Map<String, Adicional> adicionais() throws SQLException {
        return adicionalDAO.ativos();
    }

    public Map<String, Map<String, Integer>> fichasTecnicas() throws SQLException {
        return pedidoDAO.fichasTecnicas();
    }

    // ------------------------------------------------------------ mesa

    public void solicitarConta(int pedidoId, boolean solicitada) throws SQLException {
        Sessao.exigir(Modulo.MESAS);
        semEstoque(con -> {
            pedidoAberto(con, pedidoId);
            pedidoDAO.atualizar(con, pedidoId, "conta_solicitada = ?", solicitada);
            return null;
        });
    }

    public void transferirMesa(int pedidoId, int novaMesaId) throws SQLException {
        Sessao.exigir(Modulo.MESAS);
        semEstoque(con -> {
            Pedido p = pedidoAberto(con, pedidoId);
            if (p.getTipo() != TipoPedido.MESA) {
                throw new IllegalArgumentException("Só comandas de mesa podem ser transferidas.");
            }
            pedidoDAO.atualizar(con, pedidoId, "mesa_id = ?", novaMesaId);
            return null;
        });
    }

    // ------------------------------------------------------------ pagamento

    /**
     * Registra um pagamento (parcial ou total) no caixa aberto. Em dinheiro, o
     * valor recebido pode ser maior e gera troco. Quando o saldo zera, o
     * pedido é fechado. Retorna o pedido atualizado.
     */
    public Pedido receberPagamento(int pedidoId, FormaPagamento forma, double valor, double recebido) throws SQLException {
        Sessao.exigir(Modulo.CAIXA);
        semEstoque(con -> {
            pagarEm(con, pedidoId, forma, valor, recebido);
            return null;
        });
        return pedidoDAO.buscar(pedidoId);
    }

    private void pagarEm(Connection con, int pedidoId, FormaPagamento forma, double valor, double recebido) throws SQLException {
        int caixaId = caixaDAO.idAberto(con);
        if (caixaId == 0) {
            throw new IllegalStateException("Abra o caixa antes de receber pagamentos.");
        }
        Pedido p = pedidoAberto(con, pedidoId);
        if (p.getItens().isEmpty()) {
            throw new IllegalArgumentException("O pedido não tem itens.");
        }
        double valorPago = VendaService.arredondar(valor);
        if (valorPago <= 0) {
            throw new IllegalArgumentException("Informe um valor maior que zero.");
        }
        if (valorPago > p.getSaldo()) {
            throw new IllegalArgumentException("O valor é maior que o saldo restante (" + String.format("R$ %.2f", p.getSaldo()) + ").");
        }
        double valorRecebido = forma == FormaPagamento.DINHEIRO ? VendaService.arredondar(recebido) : valorPago;
        double troco = VendaService.troco(valorPago, valorRecebido);
        if (troco < 0) {
            throw new IllegalArgumentException("O valor recebido é menor que o valor a pagar.");
        }
        pedidoDAO.inserirPagamento(con, pedidoId, caixaId, forma, valorPago, valorRecebido, troco, Sessao.usuarioId());
        if (VendaService.arredondar(p.getSaldo() - valorPago) <= 0) {
            List<FormaPagamento> formas = pedidoDAO.formasUsadas(con, pedidoId);
            pedidoDAO.atualizar(con, pedidoId,
                    "situacao = 'PAGO', fechado_em = NOW(), conta_solicitada = FALSE, forma_pagamento = ?",
                    formas.size() == 1 ? formas.get(0).name() : null);
        }
    }

    /**
     * Venda de balcão em uma única transação: abre o pedido com senha, lança
     * os itens (enviando à cozinha), recebe o pagamento e fecha.
     */
    public Pedido vendaBalcao(List<Venda> itens, FormaPagamento forma, double recebido, int clienteId)
            throws SQLException, EstoqueInsuficienteException {
        Sessao.exigir(Modulo.VENDA_BALCAO);
        if (forma == null) {
            throw new IllegalArgumentException("Escolha a forma de pagamento.");
        }
        int id = emTransacao(con -> {
            if (caixaDAO.idAberto(con) == 0) {
                throw new IllegalStateException("Abra o caixa antes de registrar vendas.");
            }
            Pedido novo = new Pedido();
            novo.setTipo(TipoPedido.BALCAO);
            novo.setClienteId(clienteId);
            int pedidoId = abrirEm(con, novo);
            lancarEm(con, pedidoId, itens);
            double total = pedidoDAO.buscar(con, pedidoId).getTotal();
            pagarEm(con, pedidoId, forma, total, forma == FormaPagamento.DINHEIRO ? recebido : total);
            return pedidoId;
        });
        return pedidoDAO.buscar(id);
    }

    // ------------------------------------------------------------ cancelamento

    /** Cancela um pedido sem pagamentos e devolve o estoque dos itens. */
    public void cancelar(int pedidoId) throws SQLException {
        Sessao.exigir(Modulo.CANCELAMENTOS);
        semEstoque(con -> {
            Pedido p = pedidoAberto(con, pedidoId);
            if (p.getPago() > 0) {
                throw new IllegalStateException("O pedido já tem pagamentos e não pode ser cancelado.");
            }
            for (Venda item : p.getItens()) {
                devolverEstoque(con, item);
            }
            pedidoDAO.atualizar(con, pedidoId, "situacao = 'CANCELADO', fechado_em = NOW(), conta_solicitada = FALSE");
            try (java.sql.PreparedStatement pst = con.prepareStatement(
                    "UPDATE \"Venda\" SET status_cozinha = NULL WHERE pedido_id = ?")) {
                pst.setInt(1, pedidoId);
                pst.executeUpdate();
            }
            return null;
        });
    }

    // ------------------------------------------------------------ cozinha e entrega

    public List<Pedido> emAndamento() throws SQLException {
        return pedidoDAO.emAndamento();
    }

    public Pedido buscar(int pedidoId) throws SQLException {
        return pedidoDAO.buscar(pedidoId);
    }

    public List<Object[]> pagamentos(int pedidoId) throws SQLException {
        return pedidoDAO.pagamentos(pedidoId);
    }

    public List<Pedido> filaCozinha() throws SQLException {
        Sessao.exigir(Modulo.COZINHA);
        return pedidoDAO.filaCozinha();
    }

    /** Avança os itens do pedido que estão em {@code atual} para a próxima etapa. */
    public void avancarCozinha(int pedidoId, StatusCozinha atual) throws SQLException {
        if (!Sessao.pode(Modulo.COZINHA) && !(atual == StatusCozinha.PRONTO && Sessao.pode(Modulo.PEDIDOS))) {
            throw new SecurityException("Seu perfil não pode alterar o andamento da cozinha.");
        }
        StatusCozinha proximo = atual.proximo();
        if (proximo != null) {
            pedidoDAO.avancarItens(pedidoId, atual, proximo);
        }
    }

    /** Delivery: SAIU_PARA_ENTREGA ou ENTREGUE. */
    public void atualizarEntrega(int pedidoId, String status) throws SQLException {
        Sessao.exigir(Modulo.PEDIDOS);
        semEstoque(con -> {
            Pedido p = pedidoDAO.buscar(con, pedidoId);
            if (p == null || p.getTipo() != TipoPedido.DELIVERY) {
                throw new IllegalArgumentException("Só pedidos de delivery têm entrega.");
            }
            pedidoDAO.atualizar(con, pedidoId, "status_entrega = ?", status);
            if ("SAIU_PARA_ENTREGA".equals(status) || "ENTREGUE".equals(status)) {
                // Tudo que estava pronto saiu com o entregador.
                try (java.sql.PreparedStatement pst = con.prepareStatement(
                        "UPDATE \"Venda\" SET status_cozinha = 'ENTREGUE' WHERE pedido_id = ? AND status_cozinha = 'PRONTO'")) {
                    pst.setInt(1, pedidoId);
                    pst.executeUpdate();
                }
            }
            return null;
        });
    }

    /** Minutos desde o envio à cozinha. */
    public static long minutosDesde(java.time.LocalDateTime inicio) {
        return java.time.Duration.between(inicio, java.time.LocalDateTime.now()).toMinutes();
    }
}
