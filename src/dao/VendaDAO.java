package dao;

import config.ConexaoBD;
import entidades.FormaPagamento;
import entidades.Pedido;
import entidades.Venda;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Acesso ao banco para vendas. Os métodos de gravação recebem a conexão para
 * que o chamador controle a transação.
 */
public class VendaDAO {

    /** Ingredientes e quantidade consumida por uma unidade do produto. */
    public Map<String, Integer> ingredientesDoProduto(Connection con, String produto) throws SQLException {
        String sql = "SELECT pi.ingrediente_nome, pi.quantidade "
                + "FROM \"ProdutoIngrediente\" pi "
                + "JOIN \"Produto\" p ON p.id = pi.produto_id "
                + "WHERE p.nome = ?";
        Map<String, Integer> ingredientes = new LinkedHashMap<>();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, produto);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    ingredientes.merge(rs.getString("ingrediente_nome"), rs.getInt("quantidade"), Integer::sum);
                }
            }
        }
        return ingredientes;
    }

    /**
     * Retorna a quantidade disponível dos ingredientes, bloqueando as linhas
     * até o fim da transação para que outra venda não consuma o mesmo estoque.
     */
    public Map<String, Integer> bloquearEstoque(Connection con, Collection<String> ingredientes) throws SQLException {
        Map<String, Integer> disponivel = new HashMap<>();
        if (ingredientes.isEmpty()) {
            return disponivel;
        }
        String sql = "SELECT nome, \"Quantidade\" FROM \"Estoque\" WHERE nome = ANY (?) FOR UPDATE";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            Array nomes = con.createArrayOf("varchar", ingredientes.toArray());
            pst.setArray(1, nomes);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    disponivel.put(rs.getString("nome"), rs.getInt("Quantidade"));
                }
            }
        }
        return disponivel;
    }

    public void baixarEstoque(Connection con, String ingrediente, int quantidade) throws SQLException {
        String sql = "UPDATE \"Estoque\" SET \"Quantidade\" = \"Quantidade\" - ? WHERE nome = ?";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, quantidade);
            pst.setString(2, ingrediente);
            pst.executeUpdate();
        }
    }

    /** Devolve ao estoque o que foi baixado (item cancelado). */
    public void devolverEstoque(Connection con, String ingrediente, int quantidade) throws SQLException {
        baixarEstoque(con, ingrediente, -quantidade);
    }

    /** Cria o cabeçalho da venda e retorna seu id. */
    public int criarPedido(Connection con, double total, FormaPagamento forma,
            double valorRecebido, double troco) throws SQLException {
        String sql = "INSERT INTO \"Pedido\" (total, forma_pagamento, valor_recebido, troco) "
                + "VALUES (?, ?, ?, ?) RETURNING id";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setDouble(1, total);
            pst.setString(2, forma.name());
            pst.setDouble(3, valorRecebido);
            pst.setDouble(4, troco);
            try (ResultSet rs = pst.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /**
     * Vendas do período [de, ate], mais recentes primeiro. Limites nulos não
     * filtram. Itens antigos sem pedido aparecem como vendas avulsas.
     */
    public List<Pedido> listarPedidos(LocalDate de, LocalDate ate) throws SQLException {
        Timestamp inicio = Timestamp.valueOf((de != null ? de : LocalDate.of(1900, 1, 1)).atStartOfDay());
        Timestamp fim = Timestamp.valueOf((ate != null ? ate : LocalDate.of(9999, 1, 1)).plusDays(1).atStartOfDay());
        String sql = "SELECT p.id, p.data_pedido AS data, p.forma_pagamento, p.total, "
                + "       string_agg(v.quantidade || 'x ' || v.produto, ', ' ORDER BY v.id) AS itens "
                + "FROM \"Pedido\" p JOIN \"Venda\" v ON v.pedido_id = p.id "
                + "WHERE p.situacao = 'PAGO' AND p.data_pedido >= ? AND p.data_pedido < ? "
                + "GROUP BY p.id "
                + "UNION ALL "
                + "SELECT -v.id, v.data_venda, NULL, v.total, v.quantidade || 'x ' || v.produto "
                + "FROM \"Venda\" v "
                + "WHERE v.pedido_id IS NULL AND v.data_venda >= ? AND v.data_venda < ? "
                + "ORDER BY data DESC";
        List<Pedido> pedidos = new ArrayList<>();
        try (Connection con = ConexaoBD.getConnection();
                PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setTimestamp(1, inicio);
            pst.setTimestamp(2, fim);
            pst.setTimestamp(3, inicio);
            pst.setTimestamp(4, fim);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Pedido p = new Pedido();
                    p.setId(rs.getInt("id"));
                    p.setData(rs.getTimestamp("data").toLocalDateTime());
                    String forma = rs.getString("forma_pagamento");
                    p.setFormaPagamento(forma == null ? null : FormaPagamento.valueOf(forma));
                    p.setTotal(rs.getDouble("total"));
                    p.setResumoItens(rs.getString("itens"));
                    pedidos.add(p);
                }
            }
        }
        return pedidos;
    }

    /** Itens de uma venda listada por {@link #listarPedidos}. */
    public List<Venda> itensDoPedido(int pedidoId) throws SQLException {
        String sql = pedidoId > 0
                ? "SELECT produto, quantidade, valor, total FROM \"Venda\" WHERE pedido_id = ? ORDER BY id"
                : "SELECT produto, quantidade, valor, total FROM \"Venda\" WHERE id = ?";
        List<Venda> itens = new ArrayList<>();
        try (Connection con = ConexaoBD.getConnection();
                PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, Math.abs(pedidoId));
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Venda v = new Venda();
                    v.setProduto(rs.getString("produto"));
                    v.setQuantidade(rs.getInt("quantidade"));
                    v.setValor(rs.getDouble("valor"));
                    v.setTotal(rs.getDouble("total"));
                    itens.add(v);
                }
            }
        }
        return itens;
    }

    public void registrarItem(Connection con, int pedidoId, Venda item) throws SQLException {
        String sql = "INSERT INTO \"Venda\" (pedido_id, produto, quantidade, valor, total) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, pedidoId);
            pst.setString(2, item.getProduto());
            pst.setInt(3, item.getQuantidade());
            pst.setDouble(4, item.getValor());
            pst.setDouble(5, item.getTotal());
            pst.executeUpdate();
        }
    }
}
