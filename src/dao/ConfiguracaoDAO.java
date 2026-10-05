package dao;

import config.ConexaoBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Configurações gerais do sistema guardadas no banco (chave/valor). */
public class ConfiguracaoDAO {

    public static final String TAXA_ENTREGA = "taxa_entrega";

    public String ler(String chave, String padrao) throws SQLException {
        try (Connection con = ConexaoBD.getConnection();
                PreparedStatement pst = con.prepareStatement("SELECT valor FROM \"Configuracao\" WHERE chave = ?")) {
            pst.setString(1, chave);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next() ? rs.getString(1) : padrao;
            }
        }
    }

    public void gravar(String chave, String valor) throws SQLException {
        try (Connection con = ConexaoBD.getConnection();
                PreparedStatement pst = con.prepareStatement(
                        "INSERT INTO \"Configuracao\" (chave, valor) VALUES (?, ?) "
                        + "ON CONFLICT (chave) DO UPDATE SET valor = EXCLUDED.valor")) {
            pst.setString(1, chave);
            pst.setString(2, valor);
            pst.executeUpdate();
        }
    }

    /** Taxa fixa de entrega do delivery. */
    public double taxaEntrega() throws SQLException {
        try {
            return Double.parseDouble(ler(TAXA_ENTREGA, "0"));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
