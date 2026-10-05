package dao;

import config.ConexaoBD;
import entidades.Mesa;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Acesso ao banco para mesas; o status vem do pedido aberto em cada mesa. */
public class MesaDAO {

    public List<Mesa> listarComStatus() throws SQLException {
        String sql = "SELECT m.*, p.id AS pedido_id, p.conta_solicitada, p.total, p.data_pedido "
                + "FROM \"Mesa\" m "
                + "LEFT JOIN \"Pedido\" p ON p.mesa_id = m.id AND p.situacao = 'ABERTO' AND p.tipo = 'MESA' "
                + "WHERE m.ativa ORDER BY m.numero";
        List<Mesa> mesas = new ArrayList<>();
        try (Connection con = ConexaoBD.getConnection();
                PreparedStatement pst = con.prepareStatement(sql);
                ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                Mesa m = new Mesa();
                m.setId(rs.getInt("id"));
                m.setNumero(rs.getInt("numero"));
                m.setLugares(rs.getInt("lugares"));
                m.setReservada(rs.getBoolean("reservada"));
                m.setAtiva(rs.getBoolean("ativa"));
                m.setPedidoId(rs.getInt("pedido_id"));
                m.setContaSolicitada(rs.getBoolean("conta_solicitada"));
                m.setTotalPedido(rs.getDouble("total"));
                java.sql.Timestamp aberta = rs.getTimestamp("data_pedido");
                m.setAbertaEm(aberta == null ? null : aberta.toLocalDateTime());
                mesas.add(m);
            }
        }
        return mesas;
    }

    public void inserir(Mesa m) throws SQLException {
        try (Connection con = ConexaoBD.getConnection();
                PreparedStatement pst = con.prepareStatement(
                        "INSERT INTO \"Mesa\" (numero, lugares) VALUES (?, ?) "
                        + "ON CONFLICT (numero) DO UPDATE SET ativa = TRUE, lugares = EXCLUDED.lugares")) {
            pst.setInt(1, m.getNumero());
            pst.setInt(2, m.getLugares());
            pst.executeUpdate();
        }
    }

    public void atualizar(Mesa m) throws SQLException {
        try (Connection con = ConexaoBD.getConnection();
                PreparedStatement pst = con.prepareStatement(
                        "UPDATE \"Mesa\" SET numero = ?, lugares = ?, reservada = ? WHERE id = ?")) {
            pst.setInt(1, m.getNumero());
            pst.setInt(2, m.getLugares());
            pst.setBoolean(3, m.isReservada());
            pst.setInt(4, m.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new IllegalArgumentException("Já existe uma mesa com o número " + m.getNumero() + ".");
            }
            throw e;
        }
    }

    /** Remove a mesa do mapa (mantém o histórico de pedidos). */
    public void desativar(int id) throws SQLException {
        try (Connection con = ConexaoBD.getConnection();
                PreparedStatement pst = con.prepareStatement("UPDATE \"Mesa\" SET ativa = FALSE WHERE id = ?")) {
            pst.setInt(1, id);
            pst.executeUpdate();
        }
    }
}
