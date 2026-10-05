package dao;

import config.ConexaoBD;
import entidades.Produto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Acesso ao banco para produtos e sua ficha técnica (ingredientes). */
public class ProdutoDAO {

    public List<Produto> listar() throws SQLException {
        List<Produto> produtos = new ArrayList<>();
        try (Connection con = ConexaoBD.getConnection();
                PreparedStatement pst = con.prepareStatement(
                        "SELECT id, nome, tipo, preco FROM \"Produto\" ORDER BY nome");
                ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                Produto p = new Produto();
                p.setId(rs.getInt("id"));
                p.setNome(rs.getString("nome"));
                p.setTipo(rs.getString("tipo"));
                p.setPreco(rs.getDouble("preco"));
                produtos.add(p);
            }
        }
        return produtos;
    }

    /** Ingredientes do produto (nome → quantidade por unidade), na ordem cadastrada. */
    public Map<String, Integer> ingredientes(int produtoId) throws SQLException {
        Map<String, Integer> ingredientes = new LinkedHashMap<>();
        try (Connection con = ConexaoBD.getConnection();
                PreparedStatement pst = con.prepareStatement(
                        "SELECT ingrediente_nome, quantidade FROM \"ProdutoIngrediente\" "
                        + "WHERE produto_id = ? ORDER BY id")) {
            pst.setInt(1, produtoId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    ingredientes.merge(rs.getString("ingrediente_nome"), rs.getInt("quantidade"), Integer::sum);
                }
            }
        }
        return ingredientes;
    }

    /**
     * Insere (id &lt;= 0) ou atualiza o produto e substitui sua composição,
     * tudo na mesma transação. Retorna o id do produto.
     */
    public int salvar(Produto produto, Map<String, Integer> ingredientes) throws SQLException {
        try (Connection con = ConexaoBD.getConnection()) {
            con.setAutoCommit(false);
            try {
                int id = produto.getId();
                if (id > 0) {
                    try (PreparedStatement pst = con.prepareStatement(
                            "UPDATE \"Produto\" SET nome = ?, tipo = ?, preco = ? WHERE id = ?")) {
                        pst.setString(1, produto.getNome());
                        pst.setString(2, produto.getTipo());
                        pst.setDouble(3, produto.getPreco());
                        pst.setInt(4, id);
                        pst.executeUpdate();
                    }
                    try (PreparedStatement pst = con.prepareStatement(
                            "DELETE FROM \"ProdutoIngrediente\" WHERE produto_id = ?")) {
                        pst.setInt(1, id);
                        pst.executeUpdate();
                    }
                } else {
                    try (PreparedStatement pst = con.prepareStatement(
                            "INSERT INTO \"Produto\" (nome, tipo, preco) VALUES (?, ?, ?) RETURNING id")) {
                        pst.setString(1, produto.getNome());
                        pst.setString(2, produto.getTipo());
                        pst.setDouble(3, produto.getPreco());
                        try (ResultSet rs = pst.executeQuery()) {
                            rs.next();
                            id = rs.getInt(1);
                        }
                    }
                }
                try (PreparedStatement pst = con.prepareStatement(
                        "INSERT INTO \"ProdutoIngrediente\" (produto_id, ingrediente_nome, quantidade) VALUES (?, ?, ?)")) {
                    for (Map.Entry<String, Integer> ing : ingredientes.entrySet()) {
                        pst.setInt(1, id);
                        pst.setString(2, ing.getKey());
                        pst.setInt(3, ing.getValue());
                        pst.addBatch();
                    }
                    pst.executeBatch();
                }
                con.commit();
                return id;
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }

    /** A composição é removida junto (ON DELETE CASCADE). */
    public void excluir(int produtoId) throws SQLException {
        try (Connection con = ConexaoBD.getConnection();
                PreparedStatement pst = con.prepareStatement("DELETE FROM \"Produto\" WHERE id = ?")) {
            pst.setInt(1, produtoId);
            pst.executeUpdate();
        }
    }
}
