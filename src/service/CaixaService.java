package service;

import dao.CaixaDAO;
import entidades.Caixa;
import entidades.FormaPagamento;
import entidades.Modulo;
import java.sql.SQLException;
import java.util.Map;

/** Regras do caixa diário: um aberto por vez, sangria limitada ao dinheiro em caixa. */
public class CaixaService {

    public static final String SANGRIA = "SANGRIA";
    public static final String SUPRIMENTO = "SUPRIMENTO";

    private final CaixaDAO caixaDAO = new CaixaDAO();

    /** Caixa aberto com o resumo atualizado, ou null. */
    public Caixa aberto() throws SQLException {
        int id = caixaDAO.idAberto();
        return id == 0 ? null : caixaDAO.carregar(id);
    }

    public Caixa abrir(double valorInicial) throws SQLException {
        Sessao.exigir(Modulo.CAIXA);
        if (valorInicial < 0) {
            throw new IllegalArgumentException("O valor inicial não pode ser negativo.");
        }
        return caixaDAO.carregar(caixaDAO.abrir(VendaService.arredondar(valorInicial), Sessao.usuarioId()));
    }

    public void movimentar(String tipo, double valor, String motivo) throws SQLException {
        Sessao.exigir(Modulo.CAIXA);
        Caixa caixa = aberto();
        if (caixa == null) {
            throw new IllegalStateException("Nenhum caixa aberto.");
        }
        if (valor <= 0) {
            throw new IllegalArgumentException("Informe um valor maior que zero.");
        }
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("Informe o motivo.");
        }
        if (SANGRIA.equals(tipo) && valor > caixa.getEsperado(FormaPagamento.DINHEIRO)) {
            throw new IllegalArgumentException(String.format(
                    "Sangria maior que o dinheiro em caixa (R$ %.2f).", caixa.getEsperado(FormaPagamento.DINHEIRO)));
        }
        caixaDAO.movimentar(caixa.getId(), tipo, VendaService.arredondar(valor), motivo.trim(), Sessao.usuarioId());
    }

    /** Fecha o caixa com os valores contados e retorna o caixa fechado (com diferenças). */
    public Caixa fechar(Map<FormaPagamento, Double> informado, String observacao) throws SQLException {
        Sessao.exigir(Modulo.CAIXA);
        Caixa caixa = aberto();
        if (caixa == null) {
            throw new IllegalStateException("Nenhum caixa aberto.");
        }
        for (FormaPagamento f : FormaPagamento.values()) {
            Double v = informado.get(f);
            if (v == null || v < 0) {
                throw new IllegalArgumentException("Informe o valor contado em " + f + ".");
            }
        }
        caixaDAO.fechar(caixa.getId(), informado, observacao, Sessao.usuarioId());
        return caixaDAO.carregar(caixa.getId());
    }

    public Caixa carregar(int caixaId) throws SQLException {
        return caixaDAO.carregar(caixaId);
    }

    public java.util.List<Object[]> historico() throws SQLException {
        return caixaDAO.historico(15);
    }
}
