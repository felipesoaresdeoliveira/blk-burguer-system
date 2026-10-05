package service;

import config.ConexaoBD;
import dao.EstoqueDAO;
import java.sql.Connection;
import java.sql.SQLException;

/** Regras de movimentação de estoque. Toda alteração de quantidade gera histórico. */
public class EstoqueService {

    public static final String ENTRADA = "ENTRADA";
    public static final String AJUSTE = "AJUSTE";

    private final EstoqueDAO estoqueDAO = new EstoqueDAO();

    /** Soma a quantidade recebida ao estoque atual e retorna o novo saldo. */
    public int registrarEntrada(String item, int quantidade) throws SQLException {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade de entrada deve ser maior que zero.");
        }
        return movimentar(item, ENTRADA, quantidade, true, "Entrada de estoque");
    }

    /** Define a quantidade contada manualmente, exigindo um motivo. Retorna o novo saldo. */
    public int ajustar(String item, int novaQuantidade, String motivo) throws SQLException {
        if (novaQuantidade < 0) {
            throw new IllegalArgumentException("A quantidade não pode ser negativa.");
        }
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("Informe o motivo do ajuste.");
        }
        return movimentar(item, AJUSTE, novaQuantidade, false, motivo.trim());
    }

    private int movimentar(String item, String tipo, int valor, boolean somar, String motivo) throws SQLException {
        try (Connection con = ConexaoBD.getConnection()) {
            con.setAutoCommit(false);
            try {
                int anterior = estoqueDAO.quantidadeAtual(con, item, true);
                if (anterior < 0) {
                    throw new IllegalArgumentException("Item \"" + item + "\" não encontrado no estoque.");
                }
                int nova = somar ? anterior + valor : valor;
                estoqueDAO.definirQuantidade(con, item, nova);
                estoqueDAO.registrarMovimentacao(con, item, tipo, anterior, nova, motivo);
                con.commit();
                return nova;
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }
}
