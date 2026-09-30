package dao;

import config.ConexaoBD;
import entidades.Adicional;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/** Acesso ao banco para adicionais de lanche. */
public class AdicionalDAO {

    /** Adicionais ativos por nome, em ordem alfabética. */
    public Map<String, Adicional> ativos(Connection con) throws SQLException {
        Map<String, Adicional> mapa = new LinkedHashMap<>();
        try (PreparedStatement pst = con.prepareStatement(
                "SELECT * FROM \"Adicional\" WHERE ativo ORDER BY nome");
                ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                Adicional a = new Adicional();
                a.setId(rs.getInt("id"));
                a.setNome(rs.getString("nome"));
                a.setPreco(rs.getDouble("preco"));
                a.setIngrediente(rs.getString("ingrediente_nome"));
                a.setQuantidade(rs.getInt("quantidade"));
                mapa.put(a.getNome(), a);
            }
        }
        return mapa;
    }

    public Map<String, Adicional> ativos() throws SQLException {
        try (Connection con = ConexaoBD.getConnection()) {
            return ativos(con);
        }
    }
}
