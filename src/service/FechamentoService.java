package service;

import config.ConexaoBD;
import dao.CaixaDAO;
import dao.DashboardDAO;
import dao.DespesaDAO;
import dao.EstoqueDAO;
import entidades.Caixa;
import entidades.FormaPagamento;
import entidades.Modulo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Encerramento do dia (#41): um resumo da operação para conferir e imprimir. */
public class FechamentoService {

    /** Tudo o que o fechamento mostra. */
    public static class Fechamento {
        public LocalDate dia;
        public DashboardDAO.Resumo resumo;
        public Map<FormaPagamento, DashboardDAO.Linha> formas;
        public DashboardDAO.Linha maisVendido;
        public int cancelamentos;
        public double valorCancelado;
        public double descontos;
        public int perdas;
        public double valorPerdas;
        public double despesas;
        public List<Caixa> caixas = new ArrayList<>();

        public double getTotalRecebido() {
            double t = 0;
            for (DashboardDAO.Linha l : formas.values()) {
                t += l.valor;
            }
            return Math.round(t * 100) / 100.0;
        }

        /** Faturamento menos despesas e perdas do dia (visão simples, não contábil). */
        public double getResultado() {
            return Math.round((resumo.faturamento - despesas - valorPerdas) * 100) / 100.0;
        }

        public boolean temCaixaAberto() {
            return caixas.stream().anyMatch(Caixa::isAberto);
        }

        /** Soma das diferenças (contado - esperado) dos caixas fechados. */
        public double getDivergencia() {
            double d = 0;
            for (Caixa c : caixas) {
                if (!c.isAberto()) {
                    d += c.getDiferencaTotal();
                }
            }
            return Math.round(d * 100) / 100.0;
        }
    }

    private final DashboardDAO dashboardDAO = new DashboardDAO();
    private final EstoqueDAO estoqueDAO = new EstoqueDAO();
    private final DespesaDAO despesaDAO = new DespesaDAO();
    private final CaixaDAO caixaDAO = new CaixaDAO();

    public Fechamento gerar(LocalDate dia) throws SQLException {
        Sessao.exigir(Modulo.RELATORIOS);
        Fechamento f = new Fechamento();
        f.dia = dia;
        f.resumo = dashboardDAO.resumo(dia, dia);
        f.formas = dashboardDAO.porFormaPagamento(dia, dia);
        List<DashboardDAO.Linha> top = dashboardDAO.maisVendidos(dia, dia, 1);
        f.maisVendido = top.isEmpty() ? null : top.get(0);
        f.valorPerdas = estoqueDAO.totalPerdas(dia, dia);
        f.perdas = estoqueDAO.movimentacoes(dia, dia, EstoqueService.PERDA).size();
        f.despesas = despesaDAO.total(dia, dia);
        Timestamp inicio = Timestamp.valueOf(dia.atStartOfDay());
        Timestamp fim = Timestamp.valueOf(dia.plusDays(1).atStartOfDay());
        try (Connection con = ConexaoBD.getConnection()) {
            try (PreparedStatement pst = con.prepareStatement(
                    "SELECT COUNT(*), COALESCE(SUM(total), 0) FROM \"Pedido\" "
                    + "WHERE situacao = 'CANCELADO' AND COALESCE(fechado_em, data_pedido) >= ? AND COALESCE(fechado_em, data_pedido) < ?")) {
                pst.setTimestamp(1, inicio);
                pst.setTimestamp(2, fim);
                try (ResultSet rs = pst.executeQuery()) {
                    rs.next();
                    f.cancelamentos = rs.getInt(1);
                    f.valorCancelado = rs.getDouble(2);
                }
            }
            try (PreparedStatement pst = con.prepareStatement(
                    "SELECT COALESCE(SUM(desconto), 0) FROM \"Pedido\" WHERE situacao = 'PAGO' AND data_pedido >= ? AND data_pedido < ?")) {
                pst.setTimestamp(1, inicio);
                pst.setTimestamp(2, fim);
                try (ResultSet rs = pst.executeQuery()) {
                    rs.next();
                    f.descontos = rs.getDouble(1);
                }
            }
            // Caixas que funcionaram no dia: abertos nele, fechados nele ou ainda abertos desde antes.
            List<Integer> ids = new ArrayList<>();
            try (PreparedStatement pst = con.prepareStatement(
                    "SELECT id FROM \"Caixa\" WHERE aberto_em < ? AND (fechado_em IS NULL OR fechado_em >= ?) ORDER BY aberto_em")) {
                pst.setTimestamp(1, fim);
                pst.setTimestamp(2, inicio);
                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        ids.add(rs.getInt(1));
                    }
                }
            }
            for (int id : ids) {
                f.caixas.add(caixaDAO.carregar(id));
            }
        }
        return f;
    }
}
