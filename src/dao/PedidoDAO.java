package dao;

import config.ConexaoBD;
import entidades.FormaPagamento;
import entidades.Pedido;
import entidades.SituacaoPedido;
import entidades.StatusCozinha;
import entidades.TipoPedido;
import entidades.Venda;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Acesso ao banco para pedidos, itens, pagamentos e fila da cozinha.
 * Métodos de gravação recebem a conexão para o serviço controlar a transação.
 */
public class PedidoDAO {

    private static final String SELECT_PEDIDO = "SELECT p.*, m.numero AS mesa_numero, c.nome AS cliente_nome, "
            + "  COALESCE((SELECT SUM(valor) FROM \"Pagamento\" pg WHERE pg.pedido_id = p.id), 0) AS pago "
            + "FROM \"Pedido\" p "
            + "LEFT JOIN \"Mesa\" m ON m.id = p.mesa_id "
            + "LEFT JOIN \"Cliente\" c ON c.id = p.cliente_id ";

    private static Pedido ler(ResultSet rs) throws SQLException {
        Pedido p = new Pedido();
        p.setId(rs.getInt("id"));
        p.setData(rs.getTimestamp("data_pedido").toLocalDateTime());
        String forma = rs.getString("forma_pagamento");
        p.setFormaPagamento(forma == null ? null : FormaPagamento.valueOf(forma));
        p.setTotal(rs.getDouble("total"));
        p.setTipo(TipoPedido.valueOf(rs.getString("tipo")));
        p.setSituacao(SituacaoPedido.valueOf(rs.getString("situacao")));
        p.setMesaId(rs.getInt("mesa_id"));
        p.setMesaNumero(rs.getInt("mesa_numero"));
        p.setClienteId(rs.getInt("cliente_id"));
        p.setClienteNome(rs.getString("cliente_nome"));
        p.setIdentificacao(rs.getString("identificacao"));
        p.setSenha(rs.getInt("senha_retirada"));
        p.setTaxaEntrega(rs.getDouble("taxa_entrega"));
        p.setEnderecoEntrega(rs.getString("endereco_entrega"));
        p.setStatusEntrega(rs.getString("status_entrega"));
        p.setContaSolicitada(rs.getBoolean("conta_solicitada"));
        p.setPago(rs.getDouble("pago"));
        return p;
    }

    private static Venda lerItem(ResultSet rs) throws SQLException {
        Venda v = new Venda();
        v.setId(rs.getInt("id"));
        v.setProduto(rs.getString("produto"));
        v.setQuantidade(rs.getInt("quantidade"));
        v.setValor(rs.getDouble("valor"));
        v.setTotal(rs.getDouble("total"));
        v.setObservacao(rs.getString("observacao"));
        String st = rs.getString("status_cozinha");
        v.setStatusCozinha(st == null ? null : StatusCozinha.valueOf(st));
        return v;
    }

    // ------------------------------------------------------------ pedidos

    public int inserir(Connection con, Pedido p, Integer usuarioId) throws SQLException {
        String sql = "INSERT INTO \"Pedido\" (total, tipo, situacao, mesa_id, cliente_id, identificacao, "
                + "senha_retirada, taxa_entrega, endereco_entrega, aberto_por) "
                + "VALUES (?, ?, 'ABERTO', ?, ?, ?, ?, ?, ?, ?) RETURNING id";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setDouble(1, p.getTaxaEntrega());
            pst.setString(2, p.getTipo().name());
            CaixaDAO.setInt(pst, 3, p.getMesaId());
            CaixaDAO.setInt(pst, 4, p.getClienteId());
            pst.setString(5, ClienteDAO.vazioParaNulo(p.getIdentificacao()));
            CaixaDAO.setInt(pst, 6, p.getSenha());
            pst.setDouble(7, p.getTaxaEntrega());
            pst.setString(8, ClienteDAO.vazioParaNulo(p.getEnderecoEntrega()));
            CaixaDAO.setInt(pst, 9, usuarioId);
            try (ResultSet rs = pst.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new IllegalStateException("Esta mesa já tem uma comanda aberta.");
            }
            throw e;
        }
    }

    /** Próxima senha de balcão/retirada do dia (reinicia a cada dia). */
    public int proximaSenha(Connection con) throws SQLException {
        try (PreparedStatement pst = con.prepareStatement(
                "SELECT COALESCE(MAX(senha_retirada), 0) + 1 FROM \"Pedido\" WHERE data_pedido >= CURRENT_DATE");
                ResultSet rs = pst.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    public Pedido buscar(Connection con, int id) throws SQLException {
        try (PreparedStatement pst = con.prepareStatement(SELECT_PEDIDO + "WHERE p.id = ?")) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Pedido p = ler(rs);
                p.setItens(itens(con, id));
                return p;
            }
        }
    }

    public Pedido buscar(int id) throws SQLException {
        try (Connection con = ConexaoBD.getConnection()) {
            return buscar(con, id);
        }
    }

    /** Trava o pedido até o fim da transação (evita dois pagamentos simultâneos). */
    public void bloquear(Connection con, int id) throws SQLException {
        try (PreparedStatement pst = con.prepareStatement("SELECT id FROM \"Pedido\" WHERE id = ? FOR UPDATE")) {
            pst.setInt(1, id);
            pst.executeQuery().close();
        }
    }

    /**
     * Pedidos em andamento: abertos, ou pagos que ainda estão na cozinha ou
     * saíram para entrega. Mais antigos primeiro.
     */
    public List<Pedido> emAndamento() throws SQLException {
        String sql = SELECT_PEDIDO
                + "WHERE p.situacao = 'ABERTO' "
                + "   OR (p.situacao = 'PAGO' AND p.data_pedido >= CURRENT_DATE - 1 AND ("
                + "        EXISTS (SELECT 1 FROM \"Venda\" v WHERE v.pedido_id = p.id "
                + "                AND v.status_cozinha IN ('RECEBIDO', 'EM_PREPARO', 'PRONTO')) "
                + "     OR (p.tipo = 'DELIVERY' AND COALESCE(p.status_entrega, '') <> 'ENTREGUE'))) "
                + "ORDER BY p.data_pedido";
        List<Pedido> pedidos = new ArrayList<>();
        try (Connection con = ConexaoBD.getConnection();
                PreparedStatement pst = con.prepareStatement(sql);
                ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                pedidos.add(ler(rs));
            }
            for (Pedido p : pedidos) {
                p.setItens(itens(con, p.getId()));
            }
        }
        return pedidos;
    }

    public void recalcularTotal(Connection con, int pedidoId) throws SQLException {
        try (PreparedStatement pst = con.prepareStatement(
                "UPDATE \"Pedido\" SET total = taxa_entrega + "
                + "COALESCE((SELECT SUM(total) FROM \"Venda\" WHERE pedido_id = ?), 0) WHERE id = ?")) {
            pst.setInt(1, pedidoId);
            pst.setInt(2, pedidoId);
            pst.executeUpdate();
        }
    }

    /** Executa um UPDATE simples em "Pedido" com o id como último parâmetro. */
    public void atualizar(Connection con, int pedidoId, String set, Object... valores) throws SQLException {
        try (PreparedStatement pst = con.prepareStatement("UPDATE \"Pedido\" SET " + set + " WHERE id = ?")) {
            int i = 1;
            for (Object v : valores) {
                pst.setObject(i++, v);
            }
            pst.setInt(i, pedidoId);
            pst.executeUpdate();
        }
    }

    // ------------------------------------------------------------ itens

    public List<Venda> itens(Connection con, int pedidoId) throws SQLException {
        List<Venda> itens = new ArrayList<>();
        try (PreparedStatement pst = con.prepareStatement(
                "SELECT * FROM \"Venda\" WHERE pedido_id = ? ORDER BY id")) {
            pst.setInt(1, pedidoId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    itens.add(lerItem(rs));
                }
            }
        }
        return itens;
    }

    public Venda item(Connection con, int itemId) throws SQLException {
        try (PreparedStatement pst = con.prepareStatement("SELECT * FROM \"Venda\" WHERE id = ? FOR UPDATE")) {
            pst.setInt(1, itemId);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next() ? lerItem(rs) : null;
            }
        }
    }

    public void inserirItem(Connection con, int pedidoId, Venda item) throws SQLException {
        String sql = "INSERT INTO \"Venda\" (pedido_id, produto, quantidade, valor, total, observacao, status_cozinha, enviado_em) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, pedidoId);
            pst.setString(2, item.getProduto());
            pst.setInt(3, item.getQuantidade());
            pst.setDouble(4, item.getValor());
            pst.setDouble(5, item.getTotal());
            pst.setString(6, ClienteDAO.vazioParaNulo(item.getObservacao()));
            pst.setString(7, item.getStatusCozinha() == null ? null : item.getStatusCozinha().name());
            pst.setTimestamp(8, item.getStatusCozinha() == null ? null : new Timestamp(System.currentTimeMillis()));
            pst.executeUpdate();
        }
    }

    public void removerItem(Connection con, int itemId) throws SQLException {
        try (PreparedStatement pst = con.prepareStatement("DELETE FROM \"Venda\" WHERE id = ?")) {
            pst.setInt(1, itemId);
            pst.executeUpdate();
        }
    }

    /** true se o produto passa pela cozinha (bebidas vão direto para o cliente). */
    public boolean vaiParaCozinha(Connection con, String produto) throws SQLException {
        try (PreparedStatement pst = con.prepareStatement("SELECT tipo FROM \"Produto\" WHERE nome = ?")) {
            pst.setString(1, produto);
            try (ResultSet rs = pst.executeQuery()) {
                return !rs.next() || !"Bebida".equalsIgnoreCase(rs.getString(1));
            }
        }
    }

    // ------------------------------------------------------------ pagamentos

    public void inserirPagamento(Connection con, int pedidoId, int caixaId, FormaPagamento forma,
            double valor, double recebido, double troco, Integer usuarioId) throws SQLException {
        String sql = "INSERT INTO \"Pagamento\" (pedido_id, caixa_id, forma, valor, valor_recebido, troco, usuario_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, pedidoId);
            pst.setInt(2, caixaId);
            pst.setString(3, forma.name());
            pst.setDouble(4, valor);
            pst.setDouble(5, recebido);
            pst.setDouble(6, troco);
            CaixaDAO.setInt(pst, 7, usuarioId);
            pst.executeUpdate();
        }
    }

    /** Pagamentos do pedido: {forma, valor, recebido, troco}. */
    public List<Object[]> pagamentos(int pedidoId) throws SQLException {
        List<Object[]> lista = new ArrayList<>();
        try (Connection con = ConexaoBD.getConnection();
                PreparedStatement pst = con.prepareStatement(
                        "SELECT forma, valor, valor_recebido, troco FROM \"Pagamento\" WHERE pedido_id = ? ORDER BY id")) {
            pst.setInt(1, pedidoId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Object[]{FormaPagamento.valueOf(rs.getString(1)), rs.getDouble(2), rs.getDouble(3), rs.getDouble(4)});
                }
            }
        }
        return lista;
    }

    /** Formas distintas usadas nos pagamentos do pedido. */
    public List<FormaPagamento> formasUsadas(Connection con, int pedidoId) throws SQLException {
        List<FormaPagamento> formas = new ArrayList<>();
        try (PreparedStatement pst = con.prepareStatement(
                "SELECT DISTINCT forma FROM \"Pagamento\" WHERE pedido_id = ?")) {
            pst.setInt(1, pedidoId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    formas.add(FormaPagamento.valueOf(rs.getString(1)));
                }
            }
        }
        return formas;
    }

    // ------------------------------------------------------------ cozinha

    /**
     * Pedidos com itens na cozinha (recebido, em preparo ou pronto). Cada
     * pedido traz apenas esses itens, e a data é o envio mais antigo.
     */
    public List<Pedido> filaCozinha() throws SQLException {
        String sql = SELECT_PEDIDO.replace("SELECT p.*,", "SELECT p.*, v.id AS item_id, v.produto, v.quantidade, v.valor AS item_valor, "
                + "v.total AS item_total, v.observacao AS item_obs, v.status_cozinha, v.enviado_em,")
                + "JOIN \"Venda\" v ON v.pedido_id = p.id "
                + "WHERE v.status_cozinha IN ('RECEBIDO', 'EM_PREPARO', 'PRONTO') AND p.situacao <> 'CANCELADO' "
                + "ORDER BY v.enviado_em, v.id";
        Map<Integer, Pedido> pedidos = new LinkedHashMap<>();
        try (Connection con = ConexaoBD.getConnection();
                PreparedStatement pst = con.prepareStatement(sql);
                ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                Pedido p = pedidos.get(id);
                if (p == null) {
                    p = ler(rs);
                    p.setData(rs.getTimestamp("enviado_em").toLocalDateTime());
                    pedidos.put(id, p);
                }
                Venda v = new Venda();
                v.setId(rs.getInt("item_id"));
                v.setProduto(rs.getString("produto"));
                v.setQuantidade(rs.getInt("quantidade"));
                v.setObservacao(rs.getString("item_obs"));
                v.setStatusCozinha(StatusCozinha.valueOf(rs.getString("status_cozinha")));
                p.getItens().add(v);
            }
        }
        return new ArrayList<>(pedidos.values());
    }

    /** Move os itens do pedido que estão em {@code de} para {@code para}. Retorna quantos mudaram. */
    public int avancarItens(int pedidoId, StatusCozinha de, StatusCozinha para) throws SQLException {
        try (Connection con = ConexaoBD.getConnection();
                PreparedStatement pst = con.prepareStatement(
                        "UPDATE \"Venda\" SET status_cozinha = ? WHERE pedido_id = ? AND status_cozinha = ?")) {
            pst.setString(1, para.name());
            pst.setInt(2, pedidoId);
            pst.setString(3, de.name());
            return pst.executeUpdate();
        }
    }
}
