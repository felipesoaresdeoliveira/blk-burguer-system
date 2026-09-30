package dao;

import config.ConexaoBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Acesso ao banco para usuários do sistema. */
public class UsuarioDAO {

    /** true se existir usuário com essa senha. */
    public boolean autenticar(String usuario, String senha) throws SQLException {
        try (Connection con = ConexaoBD.getConnection();
                PreparedStatement pst = con.prepareStatement(
                        "SELECT 1 FROM \"Login\" WHERE usuario = ? AND senha = ?")) {
            pst.setString(1, usuario);
            pst.setString(2, senha);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next();
            }
        }
    }
}
