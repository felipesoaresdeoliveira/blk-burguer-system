package dao;

import config.ConexaoBD;
import entidades.Perfil;
import entidades.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Acesso ao banco para usuários do sistema. */
public class UsuarioDAO {

    private static final String CAMPOS = "\"Id\", usuario, nome, perfil, ativo";

    private static Usuario ler(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getInt("Id"));
        u.setUsuario(rs.getString("usuario"));
        u.setNome(rs.getString("nome"));
        u.setPerfil(Perfil.valueOf(rs.getString("perfil")));
        u.setAtivo(rs.getBoolean("ativo"));
        return u;
    }

    /** Usuário e o valor de senha gravado (hash ou texto antigo), ou null se não existir. */
    public Object[] buscarComSenha(String usuario) throws SQLException {
        try (Connection con = ConexaoBD.getConnection();
                PreparedStatement pst = con.prepareStatement(
                        "SELECT " + CAMPOS + ", senha FROM \"Login\" WHERE usuario = ?")) {
            pst.setString(1, usuario);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next() ? new Object[]{ler(rs), rs.getString("senha")} : null;
            }
        }
    }

    public List<Usuario> listar() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        try (Connection con = ConexaoBD.getConnection();
                PreparedStatement pst = con.prepareStatement(
                        "SELECT " + CAMPOS + " FROM \"Login\" ORDER BY ativo DESC, perfil, usuario");
                ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                usuarios.add(ler(rs));
            }
        }
        return usuarios;
    }

    public int contarAdminsAtivos() throws SQLException {
        try (Connection con = ConexaoBD.getConnection();
                PreparedStatement pst = con.prepareStatement(
                        "SELECT COUNT(*) FROM \"Login\" WHERE perfil = 'ADMIN' AND ativo");
                ResultSet rs = pst.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    public void inserir(Usuario u, String hashSenha) throws SQLException {
        try (Connection con = ConexaoBD.getConnection();
                PreparedStatement pst = con.prepareStatement(
                        "INSERT INTO \"Login\" (usuario, senha, nome, perfil, ativo) VALUES (?, ?, ?, ?, ?)")) {
            pst.setString(1, u.getUsuario());
            pst.setString(2, hashSenha);
            pst.setString(3, u.getNome());
            pst.setString(4, u.getPerfil().name());
            pst.setBoolean(5, u.isAtivo());
            pst.executeUpdate();
        }
    }

    public void atualizar(Usuario u) throws SQLException {
        try (Connection con = ConexaoBD.getConnection();
                PreparedStatement pst = con.prepareStatement(
                        "UPDATE \"Login\" SET usuario = ?, nome = ?, perfil = ?, ativo = ? WHERE \"Id\" = ?")) {
            pst.setString(1, u.getUsuario());
            pst.setString(2, u.getNome());
            pst.setString(3, u.getPerfil().name());
            pst.setBoolean(4, u.isAtivo());
            pst.setInt(5, u.getId());
            pst.executeUpdate();
        }
    }

    public void definirSenha(int id, String hashSenha) throws SQLException {
        try (Connection con = ConexaoBD.getConnection();
                PreparedStatement pst = con.prepareStatement("UPDATE \"Login\" SET senha = ? WHERE \"Id\" = ?")) {
            pst.setString(1, hashSenha);
            pst.setInt(2, id);
            pst.executeUpdate();
        }
    }
}
