package service;

import config.ConexaoBD;
import dao.AuditoriaDAO;
import entidades.AcaoAuditoria;
import entidades.Usuario;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Registro de ações críticas (#37) em nome do usuário logado. Use a versão
 * com Connection dentro da transação da própria ação: se a ação desfizer,
 * o registro desfaz junto.
 */
public final class Auditoria {

    private static final Logger LOG = Logger.getLogger(Auditoria.class.getName());
    private static final AuditoriaDAO DAO = new AuditoriaDAO();

    private Auditoria() {
    }

    /** Registra dentro da transação informada. */
    public static void registrar(Connection con, AcaoAuditoria acao, String referencia, String detalhe) throws SQLException {
        Usuario u = Sessao.usuario();
        DAO.inserir(con, Sessao.usuarioId(), u == null ? null : u.getNome(), acao, referencia, detalhe);
    }

    /**
     * Registra numa conexão própria, depois que a ação já foi gravada. Uma
     * falha aqui não desfaz a ação: fica no log da aplicação.
     */
    public static void registrar(AcaoAuditoria acao, String referencia, String detalhe) {
        try (Connection con = ConexaoBD.getConnection()) {
            registrar(con, acao, referencia, detalhe);
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Falha ao registrar auditoria: " + acao, e);
        }
    }
}
