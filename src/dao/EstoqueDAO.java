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
                        "SELECT nome, valor, \"Quantidade\", tipo, estoque_minimo FROM \"Estoque\" ORDER BY nome");
                ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                Estoque e = new Estoque();
                e.setNome(rs.getString("nome"));
                e.setPreco(rs.getDouble("valor"));
                e.setQuantidade(rs.getInt("Quantidade"));
                e.setTipo(rs.getString("tipo"));
                e.setMinimo(rs.getInt("estoque_minimo"));
                itens.add(e);
            }
        }
        return itens;
    }

    public void inserir(Estoque item) throws SQLException {
        try (Connection con = ConexaoBD.getConnection();
                PreparedStatement pst = con.prepareStatement(
                        "INSERT INTO \"Estoque\" (nome, valor, \"Quantidade\", tipo, estoque_minimo) VALUES (?, ?, ?, ?, ?)")) {
            pst.setString(1, item.getNome());
            pst.setDouble(2, item.getPreco());
            pst.setInt(3, item.getQuantidade());
            pst.setString(4, item.getTipo());
            pst.setInt(5, item.getMinimo());
            pst.executeUpdate();
        }
    }

    /** Atualiza nome, custo, tipo e mínimo. A quantidade só muda por entrada ou ajuste. */
    public void atualizarCadastro(String nomeAtual, Estoque item) throws SQLException {
        try (Connection con = ConexaoBD.getConnection();
                PreparedStatement pst = con.prepareStatement(
                        "UPDATE \"Estoque\" SET nome = ?, valor = ?, tipo = ?, estoque_minimo = ? WHERE nome = ?")) {
            pst.setString(1, item.getNome());
            pst.setDouble(2, item.getPreco());
            pst.setString(3, item.getTipo());
            pst.setInt(4, item.getMinimo());
            pst.setString(5, nomeAtual);
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
        registrarMovimentacao(con, nome, tipo, anterior, nova, motivo, null, null);
    }

    /**
     * Grava a movimentação com a categoria (motivo da perda) e o usuário. O
     * custo unitário do momento fica guardado para valorizar perdas.
     */
    public void registrarMovimentacao(Connection con, String nome, String tipo, int anterior, int nova,
            String motivo, String categoria, Integer usuarioId) throws SQLException {
        try (PreparedStatement pst = con.prepareStatement(
                "INSERT INTO \"MovimentacaoEstoque\" "
                + "(ingrediente_nome, tipo, quantidade_anterior, quantidade_nova, motivo, categoria, usuario_id, custo_unitario) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, (SELECT valor FROM \"Estoque\" WHERE nome = ?))")) {
            pst.setString(1, nome);
            pst.setString(2, tipo);
            pst.setInt(3, anterior);
            pst.setInt(4, nova);
            pst.setString(5, motivo);
            pst.setString(6, categoria);
            CaixaDAO.setInt(pst, 7, usuarioId);
            pst.setString(8, nome);
            pst.executeUpdate();
        }
    }

    /** Uma linha do histórico de movimentações. */
    public static class Movimentacao {
        public final java.time.LocalDateTime data;
        public final String item;
        public final String tipo;
        public final String categoria;
        public final int anterior;
        public final int nova;
        public final String motivo;
        public final String usuario;
        public final double custoUnitario;

        Movimentacao(ResultSet rs) throws SQLException {
            data = rs.getTimestamp("data_movimentacao").toLocalDateTime();
            item = rs.getString("ingrediente_nome");
            tipo = rs.getString("tipo");
            categoria = rs.getString("categoria");
            anterior = rs.getInt("quantidade_anterior");
            nova = rs.getInt("quantidade_nova");
            motivo = rs.getString("motivo");
            usuario = rs.getString("quem");
            custoUnitario = rs.getDouble("custo_unitario");
        }

        public int getDiferenca() {
            return nova - anterior;
        }

        /** Valor perdido (perdas) ou 0. */
        public double getValorPerdido() {
            return "PERDA".equals(tipo) ? Math.round((anterior - nova) * custoUnitario * 100) / 100.0 : 0;
        }
    }

    /**
     * Histórico de movimentações do período [de, ate], mais recentes primeiro.
     *
     * @param tipo ENTRADA, AJUSTE ou PERDA (null = todos)
     */
    public List<Movimentacao> movimentacoes(java.time.LocalDate de, java.time.LocalDate ate, String tipo) throws SQLException {
        String sql = "SELECT m.*, COALESCE(u.nome, u.usuario) AS quem FROM \"MovimentacaoEstoque\" m "
                + "LEFT JOIN \"Login\" u ON u.\"Id\" = m.usuario_id "
                + "WHERE m.data_movimentacao >= ? AND m.data_movimentacao < ?"
                + (tipo != null ? " AND m.tipo = ?" : "") + " ORDER BY m.data_movimentacao DESC, m.id DESC";
        List<Movimentacao> lista = new ArrayList<>();
        try (Connection con = ConexaoBD.getConnection();
                PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setTimestamp(1, java.sql.Timestamp.valueOf(de.atStartOfDay()));
            pst.setTimestamp(2, java.sql.Timestamp.valueOf(ate.plusDays(1).atStartOfDay()));
            if (tipo != null) {
                pst.setString(3, tipo);
            }
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Movimentacao(rs));
                }
            }
        }
        return lista;
    }

    /** Valor total perdido (quantidade x custo da época) no período. */
    public double totalPerdas(java.time.LocalDate de, java.time.LocalDate ate) throws SQLException {
        try (Connection con = ConexaoBD.getConnection();
                PreparedStatement pst = con.prepareStatement(
                        "SELECT COALESCE(SUM((quantidade_anterior - quantidade_nova) * COALESCE(custo_unitario, 0)), 0) "
                        + "FROM \"MovimentacaoEstoque\" WHERE tipo = 'PERDA' "
                        + "AND data_movimentacao >= ? AND data_movimentacao < ?")) {
            pst.setTimestamp(1, java.sql.Timestamp.valueOf(de.atStartOfDay()));
            pst.setTimestamp(2, java.sql.Timestamp.valueOf(ate.plusDays(1).atStartOfDay()));
            try (ResultSet rs = pst.executeQuery()) {
                rs.next();
                return Math.round(rs.getDouble(1) * 100) / 100.0;
            }
        }
    }
}
