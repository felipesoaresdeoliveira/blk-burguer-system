package dao;

import config.ConexaoBD;
import entidades.Cliente;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Acesso ao banco para clientes. */
public class ClienteDAO {

    private static Cliente ler(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setId(rs.getInt("id"));
        c.setNome(rs.getString("nome"));
        c.setTelefone(rs.getString("telefone"));
        c.setEndereco(rs.getString("endereco"));
        c.setObservacao(rs.getString("observacao"));
        return c;
    }

    /** Clientes cujo nome ou telefone contém o filtro (vazio = todos). */
    public List<Cliente> listar(String filtro) throws SQLException {
        String f = "%" + (filtro == null ? "" : filtro.trim().toLowerCase()) + "%";
        List<Cliente> clientes = new ArrayList<>();
        try (Connection con = ConexaoBD.getConnection();
                PreparedStatement pst = con.prepareStatement(
                        "SELECT * FROM \"Cliente\" WHERE LOWER(nome) LIKE ? OR COALESCE(telefone, '') LIKE ? ORDER BY nome")) {
            pst.setString(1, f);
            pst.setString(2, f);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    clientes.add(ler(rs));
                }
            }
        }
        return clientes;
    }

    /** Quantidade de pedidos e total gasto por cliente (id -> {pedidos, total}). */
    public java.util.Map<Integer, double[]> historico() throws SQLException {
        java.util.Map<Integer, double[]> mapa = new java.util.HashMap<>();
        try (Connection con = ConexaoBD.getConnection();
                PreparedStatement pst = con.prepareStatement(
                        "SELECT cliente_id, COUNT(*), COALESCE(SUM(total), 0) FROM \"Pedido\" "
                        + "WHERE cliente_id IS NOT NULL AND situacao = 'PAGO' GROUP BY cliente_id");
                ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                mapa.put(rs.getInt(1), new double[]{rs.getInt(2), rs.getDouble(3)});
            }
        }
        return mapa;
    }

    public int inserir(Cliente c) throws SQLException {
        try (Connection con = ConexaoBD.getConnection();
                PreparedStatement pst = con.prepareStatement(
                        "INSERT INTO \"Cliente\" (nome, telefone, endereco, observacao) VALUES (?, ?, ?, ?) RETURNING id")) {
            preencher(pst, c);
            try (ResultSet rs = pst.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void atualizar(Cliente c) throws SQLException {
        try (Connection con = ConexaoBD.getConnection();
                PreparedStatement pst = con.prepareStatement(
                        "UPDATE \"Cliente\" SET nome = ?, telefone = ?, endereco = ?, observacao = ? WHERE id = ?")) {
            preencher(pst, c);
            pst.setInt(5, c.getId());
            pst.executeUpdate();
        }
    }

    /** Exclui o cliente; falha se ele já tiver pedidos (o histórico é preservado). */
    public void excluir(int id) throws SQLException {
        try (Connection con = ConexaoBD.getConnection();
                PreparedStatement pst = con.prepareStatement("DELETE FROM \"Cliente\" WHERE id = ?")) {
            pst.setInt(1, id);
            pst.executeUpdate();
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                throw new IllegalArgumentException("Este cliente já tem pedidos e não pode ser excluído.");
            }
            throw e;
        }
    }

    private static void preencher(PreparedStatement pst, Cliente c) throws SQLException {
        pst.setString(1, c.getNome().trim());
        pst.setString(2, vazioParaNulo(c.getTelefone()));
        pst.setString(3, vazioParaNulo(c.getEndereco()));
        pst.setString(4, vazioParaNulo(c.getObservacao()));
    }

    static String vazioParaNulo(String s) {
        return s == null || s.trim().isEmpty() ? null : s.trim();
    }
}
