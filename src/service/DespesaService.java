package service;

import config.ConexaoBD;
import dao.DespesaDAO;
import entidades.AcaoAuditoria;
import entidades.CategoriaDespesa;
import entidades.Despesa;
import entidades.Modulo;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Despesas operacionais (#40): registro simples, sem virar contabilidade. */
public class DespesaService {

    private final DespesaDAO despesaDAO = new DespesaDAO();

    public List<Despesa> listar(LocalDate de, LocalDate ate, CategoriaDespesa categoria) throws SQLException {
        Sessao.exigir(Modulo.DESPESAS);
        validarPeriodo(de, ate);
        return despesaDAO.listar(de, ate, categoria);
    }

    public Map<CategoriaDespesa, Double> totalPorCategoria(LocalDate de, LocalDate ate) throws SQLException {
        validarPeriodo(de, ate);
        return despesaDAO.totalPorCategoria(de, ate);
    }

    /** Soma das despesas do período (usada também em relatórios e no fechamento). */
    public double total(LocalDate de, LocalDate ate) throws SQLException {
        validarPeriodo(de, ate);
        return despesaDAO.total(de, ate);
    }

    /** Insere (id 0) ou atualiza a despesa. Retorna o id. */
    public int salvar(Despesa d) throws SQLException {
        Sessao.exigir(Modulo.DESPESAS);
        validar(d);
        d.setDescricao(d.getDescricao().trim());
        d.setValor(VendaService.arredondar(d.getValor()));
        try (Connection con = ConexaoBD.getConnection()) {
            con.setAutoCommit(false);
            try {
                int id = d.getId();
                if (id > 0) {
                    despesaDAO.atualizar(con, d);
                } else {
                    id = despesaDAO.inserir(con, d, Sessao.usuarioId());
                    Auditoria.registrar(con, AcaoAuditoria.DESPESA_REGISTRADA, "Despesa " + id,
                            d.getCategoria() + ": " + d.getDescricao() + " - " + String.format("R$ %.2f", d.getValor()));
                }
                con.commit();
                return id;
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }

    public void excluir(int id) throws SQLException {
        Sessao.exigir(Modulo.DESPESAS);
        try (Connection con = ConexaoBD.getConnection()) {
            con.setAutoCommit(false);
            try {
                Despesa d = despesaDAO.buscar(con, id);
                if (d == null) {
                    throw new IllegalArgumentException("Despesa não encontrada.");
                }
                despesaDAO.excluir(con, id);
                Auditoria.registrar(con, AcaoAuditoria.DESPESA_EXCLUIDA, "Despesa " + id,
                        d.getCategoria() + ": " + d.getDescricao() + " - " + String.format("R$ %.2f", d.getValor())
                        + " de " + d.getData());
                con.commit();
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }

    private static void validar(Despesa d) {
        if (d.getDescricao() == null || d.getDescricao().trim().isEmpty()) {
            throw new IllegalArgumentException("Informe a descrição da despesa.");
        }
        if (d.getDescricao().trim().length() > 150) {
            throw new IllegalArgumentException("A descrição deve ter no máximo 150 caracteres.");
        }
        if (d.getCategoria() == null) {
            throw new IllegalArgumentException("Escolha a categoria.");
        }
        if (d.getData() == null) {
            throw new IllegalArgumentException("Informe a data da despesa.");
        }
        if (d.getData().isAfter(LocalDate.now().plusYears(1))) {
            throw new IllegalArgumentException("A data está muito no futuro.");
        }
        if (d.getValor() <= 0) {
            throw new IllegalArgumentException("O valor deve ser maior que zero.");
        }
    }

    static void validarPeriodo(LocalDate de, LocalDate ate) {
        if (de == null || ate == null) {
            throw new IllegalArgumentException("Informe o período.");
        }
        if (de.isAfter(ate)) {
            throw new IllegalArgumentException("A data inicial é depois da final.");
        }
    }
}
