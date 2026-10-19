package dao;

import config.ConexaoBD;
import entidades.AcaoAuditoria;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Trilha de auditoria (#37). Só existe inserção e consulta: o banco recusa
 * UPDATE e DELETE na tabela (gatilho auditoria_bloqueio).
 */
public class AuditoriaDAO {

    /** Um registro da trilha. */
    public static class Registro {
        public final LocalDateTime data;
        public final String usuario;
        public final AcaoAuditoria acao;
        public final String acaoGravada;
        public final String referencia;
        public final String detalhe;

        Registro(LocalDateTime data, String usuario, String acaoGravada, String referencia, String detalhe) {
            this.data = data;
            this.usuario = usuario;
            this.acaoGravada = acaoGravada;
            this.acao = AcaoAuditoria.de(acaoGravada);
            this.referencia = referencia;
            this.detalhe = detalhe;
        }

        /** Descrição da ação; ações antigas/desconhecidas aparecem pelo nome gravado. */
        public String getAcaoDescricao() {
            return acao != null ? acao.toString() : acaoGravada;
        }
    }

    public void inserir(Connection con, Integer usuarioId, String usuarioNome, AcaoAuditoria acao,
            String referencia, String detalhe) throws SQLException {
        try (PreparedStatement pst = con.prepareStatement(
                "INSERT INTO \"Auditoria\" (usuario_id, usuario_nome, acao, referencia, detalhe) VALUES (?, ?, ?, ?, ?)")) {
            CaixaDAO.setInt(pst, 1, usuarioId);
            pst.setString(2, usuarioNome);
            pst.setString(3, acao.name());
            pst.setString(4, cortar(referencia, 100));
            pst.setString(5, cortar(detalhe, 500));
            pst.executeUpdate();
        }
    }

    private static String cortar(String texto, int max) {
        return texto == null || texto.length() <= max ? texto : texto.substring(0, max - 1) + "…";
    }

    /**
     * Consulta do período [de, ate], mais recentes primeiro.
     *
     * @param acao filtro de ação (null = todas)
     * @param usuario parte do nome do usuário (vazio = todos)
     */
    public List<Registro> listar(LocalDate de, LocalDate ate, AcaoAuditoria acao, String usuario, int limite)
            throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT data, usuario_nome, acao, referencia, detalhe FROM \"Auditoria\" WHERE data >= ? AND data < ?");
        if (acao != null) {
            sql.append(" AND acao = ?");
        }
        boolean porUsuario = usuario != null && !usuario.trim().isEmpty();
        if (porUsuario) {
            sql.append(" AND usuario_nome ILIKE ?");
        }
        sql.append(" ORDER BY data DESC, id DESC LIMIT ?");
        List<Registro> lista = new ArrayList<>();
        try (Connection con = ConexaoBD.getConnection();
                PreparedStatement pst = con.prepareStatement(sql.toString())) {
            int i = 1;
            pst.setTimestamp(i++, Timestamp.valueOf(de.atStartOfDay()));
            pst.setTimestamp(i++, Timestamp.valueOf(ate.plusDays(1).atStartOfDay()));
            if (acao != null) {
                pst.setString(i++, acao.name());
            }
            if (porUsuario) {
                pst.setString(i++, "%" + usuario.trim() + "%");
            }
            pst.setInt(i, limite);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Registro(rs.getTimestamp(1).toLocalDateTime(), rs.getString(2), rs.getString(3),
                            rs.getString(4), rs.getString(5)));
                }
            }
        }
        return lista;
    }
}
