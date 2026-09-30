package dao;

import config.ConexaoBD;
import entidades.Estoque;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Acesso ao banco para itens de estoque e suas movimentações. */
public class EstoqueDAO {

    public List<Estoque> listar() throws SQLException {
        List<Estoque> itens = new ArrayList<>();
        try (Connection con = ConexaoBD.getConnection();
                PreparedStatement pst = con.prepareStatement(
                        "SELECT nome, valor, \"Quantidade\", tipo FROM \"Estoque\" ORDER BY nome");
                ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                Estoque e = new Estoque();
                e.setNome(rs.getString("nome"));
                e.setPreco(rs.getDouble("valor"));
                e.setQuantidade(rs.getInt("Quantidade"));
                e.setTipo(rs.getString("tipo"));
                itens.add(e);
            }
        }
        return itens;
    }

    public void inserir(Estoque item) throws SQLException {
        try (Connection con = ConexaoBD.getConnection();
                PreparedStatement pst = con.prepareStatement(
                        "INSERT INTO \"Estoque\" (nome, valor, \"Quantidade\", tipo) VALUES (?, ?, ?, ?)")) {
            pst.setString(1, item.getNome());
            pst.setDouble(2, item.getPreco());
            pst.setInt(3, item.getQuantidade());
            pst.setString(4, item.getTipo());
            pst.executeUpdate();
        }
    }

    /** Atualiza nome, custo e tipo. A quantidade só muda por entrada ou ajuste. */
    public void atualizarCadastro(String nomeAtual, Estoque item) throws SQLException {
        try (Connection con = ConexaoBD.getConnection();
                PreparedStatement pst = con.prepareStatement(
                        "UPDATE \"Estoque\" SET nome = ?, valor = ?, tipo = ? WHERE nome = ?")) {
            pst.setString(1, item.getNome());
            pst.setDouble(2, item.getPreco());
            pst.setString(3, item.getTipo());
            pst.setString(4, nomeAtual);
            pst.executeUpdate();
        }
    }

    public void excluir(String nome) throws SQLException {
        try (Connection con = ConexaoBD.getConnection();
                PreparedStatement pst = con.prepareStatement("DELETE FROM \"Estoque\" WHERE nome = ?")) {
            pst.setString(1, nome);
            pst.executeUpdate();
        }
    }

    /** Quantidade atual do item, ou -1 se ele não existir. */
    public int quantidadeAtual(String nome) throws SQLException {
        try (Connection con = ConexaoBD.getConnection()) {
            return quantidadeAtual(con, nome, false);
        }
    }

    /** Lê a quantidade atual; com bloquear = true a linha fica travada até o fim da transação. */
    public int quantidadeAtual(Connection con, String nome, boolean bloquear) throws SQLException {
        String sql = "SELECT \"Quantidade\" FROM \"Estoque\" WHERE nome = ?" + (bloquear ? " FOR UPDATE" : "");
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, nome);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    public void definirQuantidade(Connection con, String nome, int quantidade) throws SQLException {
        try (PreparedStatement pst = con.prepareStatement(
                "UPDATE \"Estoque\" SET \"Quantidade\" = ? WHERE nome = ?")) {
            pst.setInt(1, quantidade);
            pst.setString(2, nome);
            pst.executeUpdate();
        }
    }

    public void registrarMovimentacao(Connection con, String nome, String tipo,
            int anterior, int nova, String motivo) throws SQLException {
        try (PreparedStatement pst = con.prepareStatement(
                "INSERT INTO \"MovimentacaoEstoque\" "
                + "(ingrediente_nome, tipo, quantidade_anterior, quantidade_nova, motivo) "
                + "VALUES (?, ?, ?, ?, ?)")) {
            pst.setString(1, nome);
            pst.setString(2, tipo);
            pst.setInt(3, anterior);
            pst.setInt(4, nova);
            pst.setString(5, motivo);
            pst.executeUpdate();
        }
    }
}
