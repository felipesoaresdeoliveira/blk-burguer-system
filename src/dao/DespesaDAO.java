package dao;

import config.ConexaoBD;
import entidades.CategoriaDespesa;
import entidades.Despesa;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Acesso ao banco para despesas operacionais (#40). */
public class DespesaDAO {

    public int inserir(Connection con, Despesa d, Integer usuarioId) throws SQLException {
        try (PreparedStatement pst = con.prepareStatement(
                "INSERT INTO \"Despesa\" (descricao, categoria, data, valor, usuario_id) VALUES (?, ?, ?, ?, ?) RETURNING id")) {
            pst.setString(1, d.getDescricao());
            pst.setString(2, d.getCategoria().name());
            pst.setDate(3, Date.valueOf(d.getData()));
            pst.setDouble(4, d.getValor());
            CaixaDAO.setInt(pst, 5, usuarioId);
            try (ResultSet rs = pst.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void atualizar(Connection con, Despesa d) throws SQLException {
        try (PreparedStatement pst = con.prepareStatement(
                "UPDATE \"Despesa\" SET descricao = ?, categoria = ?, data = ?, valor = ? WHERE id = ?")) {
            pst.setString(1, d.getDescricao());
            pst.setString(2, d.getCategoria().name());
            pst.setDate(3, Date.valueOf(d.getData()));
            pst.setDouble(4, d.getValor());
            pst.setInt(5, d.getId());
            pst.executeUpdate();
        }
    }

    public void excluir(Connection con, int id) throws SQLException {
        try (PreparedStatement pst = con.prepareStatement("DELETE FROM \"Despesa\" WHERE id = ?")) {
            pst.setInt(1, id);
            pst.executeUpdate();
        }
    }

    public Despesa buscar(Connection con, int id) throws SQLException {
        try (PreparedStatement pst = con.prepareStatement(
                "SELECT d.*, NULL AS quem FROM \"Despesa\" d WHERE d.id = ?")) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next() ? ler(rs) : null;
            }
        }
    }

    /** Despesas do período [de, ate], mais recentes primeiro; categoria null = todas. */
    public List<Despesa> listar(LocalDate de, LocalDate ate, CategoriaDespesa categoria) throws SQLException {
        String sql = "SELECT d.*, COALESCE(u.nome, u.usuario) AS quem FROM \"Despesa\" d "
                + "LEFT JOIN \"Login\" u ON u.\"Id\" = d.usuario_id WHERE d.data BETWEEN ? AND ?"
                + (categoria != null ? " AND d.categoria = ?" : "") + " ORDER BY d.data DESC, d.id DESC";
        List<Despesa> lista = new ArrayList<>();
        try (Connection con = ConexaoBD.getConnection();
                PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setDate(1, Date.valueOf(de));
            pst.setDate(2, Date.valueOf(ate));
            if (categoria != null) {
                pst.setString(3, categoria.name());
            }
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    lista.add(ler(rs));
                }
            }
        }
        return lista;
    }

    /** Total por categoria no período (todas as categorias presentes, na ordem do enum). */
    public Map<CategoriaDespesa, Double> totalPorCategoria(LocalDate de, LocalDate ate) throws SQLException {
        Map<CategoriaDespesa, Double> totais = new EnumMap<>(CategoriaDespesa.class);
        for (CategoriaDespesa c : CategoriaDespesa.values()) {
            totais.put(c, 0.0);
        }
        try (Connection con = ConexaoBD.getConnection();
                PreparedStatement pst = con.prepareStatement(
                        "SELECT categoria, SUM(valor) FROM \"Despesa\" WHERE data BETWEEN ? AND ? GROUP BY categoria")) {
            pst.setDate(1, Date.valueOf(de));
            pst.setDate(2, Date.valueOf(ate));
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    totais.put(CategoriaDespesa.valueOf(rs.getString(1)), rs.getDouble(2));
                }
            }
        }
        return totais;
    }

    /** Soma das despesas no período. */
    public double total(LocalDate de, LocalDate ate) throws SQLException {
        double soma = 0;
        for (double v : totalPorCategoria(de, ate).values()) {
            soma += v;
        }
        return Math.round(soma * 100) / 100.0;
    }

    private static Despesa ler(ResultSet rs) throws SQLException {
        Despesa d = new Despesa();
        d.setId(rs.getInt("id"));
        d.setDescricao(rs.getString("descricao"));
        d.setCategoria(CategoriaDespesa.valueOf(rs.getString("categoria")));
        d.setData(rs.getDate("data").toLocalDate());
        d.setValor(rs.getDouble("valor"));
        d.setUsuarioNome(rs.getString("quem"));
        return d;
    }
}
