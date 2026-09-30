package dao;

import config.ConexaoBD;
import entidades.Estoque;
import entidades.FormaPagamento;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Consultas agregadas do dashboard. Uma "venda" é um pedido; itens antigos
 * sem pedido contam como vendas avulsas.
 */
public class DashboardDAO {

    /** Chave que identifica uma venda na tabela Venda (pedido ou item avulso). */
    private static final String CHAVE_VENDA = "COALESCE('p' || v.pedido_id, 'v' || v.id)";

    /** Itens de vendas concluídas: pedidos pagos ou vendas antigas sem pedido. */
    private static final String VENDAS_PAGAS = "FROM \"Venda\" v LEFT JOIN \"Pedido\" pe ON pe.id = v.pedido_id "
            + "WHERE (pe.id IS NULL OR pe.situacao = 'PAGO') ";

    public static class Resumo {
        public double faturamento;
        public int vendas;

        public double ticketMedio() {
            return vendas == 0 ? 0 : faturamento / vendas;
        }
    }

    public static class Linha {
        public final String nome;
        public final double valor;
        public final int quantidade;

        Linha(String nome, double valor, int quantidade) {
            this.nome = nome;
            this.valor = valor;
            this.quantidade = quantidade;
        }
    }

    private static Timestamp inicio(LocalDate dia) {
        return Timestamp.valueOf(dia.atStartOfDay());
    }

    /** Faturamento e quantidade de vendas no período [de, ate]. */
    public Resumo resumo(LocalDate de, LocalDate ate) throws SQLException {
        String sql = "SELECT COALESCE(SUM(v.total), 0), COUNT(DISTINCT " + CHAVE_VENDA + ") "
                + VENDAS_PAGAS + "AND v.data_venda >= ? AND v.data_venda < ?";
        try (Connection con = ConexaoBD.getConnection();
                PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setTimestamp(1, inicio(de));
            pst.setTimestamp(2, inicio(ate.plusDays(1)));
            try (ResultSet rs = pst.executeQuery()) {
                rs.next();
                Resumo r = new Resumo();
                r.faturamento = rs.getDouble(1);
                r.vendas = rs.getInt(2);
                return r;
            }
        }
    }

    /** Faturamento e nº de vendas de cada dia, do mais antigo ao mais recente (dias sem venda = 0). */
    public List<Linha> faturamentoPorDia(LocalDate de, LocalDate ate) throws SQLException {
        String sql = "SELECT d::date AS dia, COALESCE(SUM(v.total), 0) AS total, "
                + "       COUNT(DISTINCT CASE WHEN v.id IS NOT NULL THEN COALESCE('p' || v.pedido_id, 'v' || v.id) END) AS vendas "
                + "FROM generate_series(?::date, ?::date, interval '1 day') d "
                + "LEFT JOIN (SELECT v.* " + VENDAS_PAGAS + ") v ON v.data_venda >= d AND v.data_venda < d + interval '1 day' "
                + "GROUP BY d ORDER BY d";
        List<Linha> dias = new ArrayList<>();
        try (Connection con = ConexaoBD.getConnection();
                PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setDate(1, Date.valueOf(de));
            pst.setDate(2, Date.valueOf(ate));
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    dias.add(new Linha(rs.getDate("dia").toLocalDate().toString(), rs.getDouble("total"), rs.getInt("vendas")));
                }
            }
        }
        return dias;
    }

    /** Vendas e valor por forma de pagamento, na ordem fixa do enum (todas as formas presentes). */
    public Map<FormaPagamento, Linha> porFormaPagamento(LocalDate de, LocalDate ate) throws SQLException {
        Map<FormaPagamento, Linha> formas = new EnumMap<>(FormaPagamento.class);
        for (FormaPagamento f : FormaPagamento.values()) {
            formas.put(f, new Linha(f.toString(), 0, 0));
        }
        String sql = "SELECT forma, SUM(valor), COUNT(DISTINCT pedido_id) FROM \"Pagamento\" "
                + "WHERE data >= ? AND data < ? GROUP BY forma";
        try (Connection con = ConexaoBD.getConnection();
                PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setTimestamp(1, inicio(de));
            pst.setTimestamp(2, inicio(ate.plusDays(1)));
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    FormaPagamento f = FormaPagamento.valueOf(rs.getString(1));
                    formas.put(f, new Linha(f.toString(), rs.getDouble(2), rs.getInt(3)));
                }
            }
        }
        return formas;
    }

    /** Produtos mais vendidos (por quantidade) no período. */
    public List<Linha> maisVendidos(LocalDate de, LocalDate ate, int limite) throws SQLException {
        String sql = "SELECT v.produto, SUM(v.total), SUM(v.quantidade) AS qtd "
                + VENDAS_PAGAS + "AND v.data_venda >= ? AND v.data_venda < ? "
                + "GROUP BY v.produto ORDER BY qtd DESC, SUM(v.total) DESC LIMIT ?";
        List<Linha> produtos = new ArrayList<>();
        try (Connection con = ConexaoBD.getConnection();
                PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setTimestamp(1, inicio(de));
            pst.setTimestamp(2, inicio(ate.plusDays(1)));
            pst.setInt(3, limite);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    produtos.add(new Linha(rs.getString(1), rs.getDouble(2), rs.getInt(3)));
                }
            }
        }
        return produtos;
    }

    /** Itens abaixo da quantidade mínima, mais críticos primeiro. */
    public List<Estoque> estoqueBaixo() throws SQLException {
        String sql = "SELECT nome, \"Quantidade\", estoque_minimo, tipo FROM \"Estoque\" "
                + "WHERE \"Quantidade\" < estoque_minimo "
                + "ORDER BY \"Quantidade\"::float / NULLIF(estoque_minimo, 0), nome";
        List<Estoque> itens = new ArrayList<>();
        try (Connection con = ConexaoBD.getConnection();
                PreparedStatement pst = con.prepareStatement(sql);
                ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                Estoque e = new Estoque();
                e.setNome(rs.getString(1));
                e.setQuantidade(rs.getInt(2));
                e.setMinimo(rs.getInt(3));
                e.setTipo(rs.getString(4));
                itens.add(e);
            }
        }
        return itens;
    }
}
