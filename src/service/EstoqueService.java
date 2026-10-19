package service;

import config.ConexaoBD;
import dao.EstoqueDAO;
import entidades.AcaoAuditoria;
import entidades.MotivoPerda;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/** Regras de movimentação de estoque. Toda alteração de quantidade gera histórico. */
public class EstoqueService {

    public static final String ENTRADA = "ENTRADA";
    public static final String AJUSTE = "AJUSTE";
    public static final String PERDA = "PERDA";

    private final EstoqueDAO estoqueDAO = new EstoqueDAO();

    /** Soma a quantidade recebida ao estoque atual e retorna o novo saldo. */
    public int registrarEntrada(String item, int quantidade) throws SQLException {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade de entrada deve ser maior que zero.");
        }
        return movimentar(item, ENTRADA, quantidade, "Entrada de estoque", null);
    }

    /** Define a quantidade contada manualmente, exigindo um motivo. Retorna o novo saldo. */
    public int ajustar(String item, int novaQuantidade, String motivo) throws SQLException {
        if (novaQuantidade < 0) {
            throw new IllegalArgumentException("A quantidade não pode ser negativa.");
        }
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("Informe o motivo do ajuste.");
        }
        return movimentar(item, AJUSTE, novaQuantidade, motivo.trim(), null);
    }

    /**
     * Registra uma perda (vencimento, quebra, descarte...) (#34): baixa a
     * quantidade, guarda motivo, usuário e custo, e audita. A perda não pode
     * deixar o estoque negativo. Retorna o novo saldo.
     */
    public int registrarPerda(String item, int quantidade, MotivoPerda motivo, String observacao) throws SQLException {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade perdida deve ser maior que zero.");
        }
        if (motivo == null) {
            throw new IllegalArgumentException("Escolha o motivo da perda.");
        }
        String texto = motivo + (observacao == null || observacao.trim().isEmpty() ? "" : ": " + observacao.trim());
        return movimentar(item, PERDA, quantidade, texto, motivo.name());
    }

    private int movimentar(String item, String tipo, int valor, String motivo, String categoria) throws SQLException {
        try (Connection con = ConexaoBD.getConnection()) {
            con.setAutoCommit(false);
            try {
                int anterior = estoqueDAO.quantidadeAtual(con, item, true);
                if (anterior < 0) {
                    throw new IllegalArgumentException("Item \"" + item + "\" não encontrado no estoque.");
                }
                int nova;
                if (ENTRADA.equals(tipo)) {
                    nova = anterior + valor;
                } else if (PERDA.equals(tipo)) {
                    if (valor > anterior) {
                        throw new IllegalArgumentException("Perda maior que o estoque de " + item
                                + " (" + anterior + " disponível).");
                    }
                    nova = anterior - valor;
                } else {
                    nova = valor;
                }
                estoqueDAO.definirQuantidade(con, item, nova);
                estoqueDAO.registrarMovimentacao(con, item, tipo, anterior, nova, motivo, categoria, Sessao.usuarioId());
                if (PERDA.equals(tipo)) {
                    Auditoria.registrar(con, AcaoAuditoria.ESTOQUE_PERDA, item,
                            valor + " un. (" + anterior + " → " + nova + ") - " + motivo);
                } else if (AJUSTE.equals(tipo)) {
                    Auditoria.registrar(con, AcaoAuditoria.ESTOQUE_AJUSTE, item,
                            anterior + " → " + nova + " - " + motivo);
                }
                con.commit();
                return nova;
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }

    /** Histórico de movimentações (tipo null = todos). */
    public List<EstoqueDAO.Movimentacao> historico(LocalDate de, LocalDate ate, String tipo) throws SQLException {
        DespesaService.validarPeriodo(de, ate);
        return estoqueDAO.movimentacoes(de, ate, tipo);
    }

    /** Valor perdido no período (quantidade x custo do momento da perda). */
    public double totalPerdas(LocalDate de, LocalDate ate) throws SQLException {
        DespesaService.validarPeriodo(de, ate);
        return estoqueDAO.totalPerdas(de, ate);
    }
}
