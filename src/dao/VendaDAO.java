package dao;

import entidades.Venda;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Acesso ao banco para vendas. Todos os métodos recebem a conexão para que
 * o chamador controle a transação.
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

    public void registrarItem(Connection con, Venda item) throws SQLException {
        String sql = "INSERT INTO \"Venda\" (produto, quantidade, valor, total) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, item.getProduto());
            pst.setInt(2, item.getQuantidade());
            pst.setDouble(3, item.getValor());
            pst.setDouble(4, item.getTotal());
            pst.executeUpdate();
        }
    }
}
