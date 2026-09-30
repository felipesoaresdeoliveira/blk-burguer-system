package dao;

import config.ConexaoBD;
import entidades.Caixa;
import entidades.FormaPagamento;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Acesso ao banco para o controle de caixa. */
public class CaixaDAO {

    /** Id do caixa aberto, ou 0 se não houver. */
    public int idAberto(Connection con) throws SQLException {
        try (PreparedStatement pst = con.prepareStatement("SELECT id FROM \"Caixa\" WHERE fechado_em IS NULL");
                ResultSet rs = pst.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int idAberto() throws SQLException {
        try (Connection con = ConexaoBD.getConnection()) {
            return idAberto(con);
        }
    }

    public int abrir(double valorInicial, Integer usuarioId) throws SQLException {
        try (Connection con = ConexaoBD.getConnection();
                PreparedStatement pst = con.prepareStatement(
                        "INSERT INTO \"Caixa\" (valor_inicial, aberto_por) VALUES (?, ?) RETURNING id")) {
            pst.setDouble(1, valorInicial);
            setInt(pst, 2, usuarioId);
            try (ResultSet rs = pst.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new IllegalStateException("Já existe um caixa aberto.");
            }
            throw e;
        }
    }

    public void movimentar(int caixaId, String tipo, double valor, String motivo, Integer usuarioId) throws SQLException {
        try (Connection con = ConexaoBD.getConnection();
                PreparedStatement pst = con.prepareStatement(
                        "INSERT INTO \"MovimentacaoCaixa\" (caixa_id, tipo, valor, motivo, usuario_id) VALUES (?, ?, ?, ?, ?)")) {
            pst.setInt(1, caixaId);
            pst.setString(2, tipo);
            pst.setDouble(3, valor);
            pst.setString(4, motivo);
            setInt(pst, 5, usuarioId);
            pst.executeUpdate();
        }
    }

    public void fechar(int caixaId, Map<FormaPagamento, Double> informado, String observacao, Integer usuarioId)
            throws SQLException {
        try (Connection con = ConexaoBD.getConnection();
                PreparedStatement pst = con.prepareStatement(
                        "UPDATE \"Caixa\" SET fechado_em = NOW(), fechado_por = ?, informado_dinheiro = ?, "
                        + "informado_pix = ?, informado_debito = ?, informado_credito = ?, observacao = ? "
                        + "WHERE id = ? AND fechado_em IS NULL")) {
            setInt(pst, 1, usuarioId);
            pst.setDouble(2, informado.get(FormaPagamento.DINHEIRO));
            pst.setDouble(3, informado.get(FormaPagamento.PIX));
            pst.setDouble(4, informado.get(FormaPagamento.DEBITO));
            pst.setDouble(5, informado.get(FormaPagamento.CREDITO));
            pst.setString(6, observacao);
            pst.setInt(7, caixaId);
            if (pst.executeUpdate() == 0) {
                throw new IllegalStateException("Este caixa já foi fechado.");
            }
        }
    }

    /** Caixa com totais por forma, sangrias/suprimentos e movimentações. */
    public Caixa carregar(int caixaId) throws SQLException {
        Caixa c = new Caixa();
        try (Connection con = ConexaoBD.getConnection()) {
            try (PreparedStatement pst = con.prepareStatement(
                    "SELECT c.*, u.usuario, u.nome FROM \"Caixa\" c LEFT JOIN \"Login\" u ON u.\"Id\" = c.aberto_por WHERE c.id = ?")) {
                pst.setInt(1, caixaId);
                try (ResultSet rs = pst.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }
                    c.setId(caixaId);
                    c.setAbertoEm(rs.getTimestamp("aberto_em").toLocalDateTime());
                    Timestamp fechado = rs.getTimestamp("fechado_em");
                    c.setFechadoEm(fechado == null ? null : fechado.toLocalDateTime());
                    c.setValorInicial(rs.getDouble("valor_inicial"));
                    String nome = rs.getString("nome");
                    c.setAbertoPor(nome != null ? nome : rs.getString("usuario"));
                    if (fechado != null) {
                        c.getInformado().put(FormaPagamento.DINHEIRO, rs.getDouble("informado_dinheiro"));
                        c.getInformado().put(FormaPagamento.PIX, rs.getDouble("informado_pix"));
                        c.getInformado().put(FormaPagamento.DEBITO, rs.getDouble("informado_debito"));
                        c.getInformado().put(FormaPagamento.CREDITO, rs.getDouble("informado_credito"));
                    }
                }
            }
            try (PreparedStatement pst = con.prepareStatement(
                    "SELECT forma, SUM(valor), COUNT(*) FROM \"Pagamento\" WHERE caixa_id = ? GROUP BY forma")) {
                pst.setInt(1, caixaId);
                try (ResultSet rs = pst.executeQuery()) {
                    int n = 0;
                    while (rs.next()) {
                        c.getRecebido().put(FormaPagamento.valueOf(rs.getString(1)), rs.getDouble(2));
                        n += rs.getInt(3);
                    }
                    c.setPagamentos(n);
                }
            }
            List<Caixa.Movimentacao> movs = new ArrayList<>();
            try (PreparedStatement pst = con.prepareStatement(
                    "SELECT m.tipo, m.valor, m.motivo, m.data, COALESCE(u.nome, u.usuario) AS quem "
                    + "FROM \"MovimentacaoCaixa\" m LEFT JOIN \"Login\" u ON u.\"Id\" = m.usuario_id "
                    + "WHERE m.caixa_id = ? ORDER BY m.data")) {
                pst.setInt(1, caixaId);
                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        movs.add(new Caixa.Movimentacao(rs.getString("tipo"), rs.getDouble("valor"),
                                rs.getString("motivo"), rs.getString("quem"), rs.getTimestamp("data").toLocalDateTime()));
                        if ("SANGRIA".equals(rs.getString("tipo"))) {
                            c.setSangrias(c.getSangrias() + rs.getDouble("valor"));
                        } else {
                            c.setSuprimentos(c.getSuprimentos() + rs.getDouble("valor"));
                        }
                    }
                }
            }
            c.setMovimentacoes(movs);
        }
        return c;
    }

    /** Últimos caixas fechados: {id, aberto_em, fechado_em, total vendido, diferença total}. */
    public List<Object[]> historico(int limite) throws SQLException {
        String sql = "SELECT c.id, c.aberto_em, c.fechado_em, "
                + "  COALESCE((SELECT SUM(valor) FROM \"Pagamento\" p WHERE p.caixa_id = c.id), 0) AS vendido "
                + "FROM \"Caixa\" c WHERE c.fechado_em IS NOT NULL ORDER BY c.fechado_em DESC LIMIT ?";
        List<Object[]> lista = new ArrayList<>();
        try (Connection con = ConexaoBD.getConnection();
                PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, limite);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Object[]{rs.getInt(1), rs.getTimestamp(2).toLocalDateTime(),
                        rs.getTimestamp(3).toLocalDateTime(), rs.getDouble(4)});
                }
            }
        }
        return lista;
    }

    static void setInt(PreparedStatement pst, int i, Integer valor) throws SQLException {
        if (valor == null || valor <= 0) {
            pst.setNull(i, Types.INTEGER);
        } else {
            pst.setInt(i, valor);
        }
    }
}
