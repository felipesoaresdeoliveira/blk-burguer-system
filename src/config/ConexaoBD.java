package config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Ponto único de conexão com o PostgreSQL.
 *
 * A configuração é lida do arquivo db.properties (na pasta onde o sistema é
 * executado). Variáveis de ambiente BLK_DB_URL, BLK_DB_USER e BLK_DB_PASSWORD,
 * quando definidas, têm prioridade sobre o arquivo.
 */
public final class ConexaoBD {

    private static final String ARQUIVO = "db.properties";
    private static final String URL_PADRAO = "jdbc:postgresql://localhost:5432/BLKburguer";
    private static final String USUARIO_PADRAO = "postgres";

    private static Properties config;

    private ConexaoBD() {
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver JDBC do PostgreSQL não encontrado no classpath.", e);
        }
        return DriverManager.getConnection(
                valor("BLK_DB_URL", "db.url", URL_PADRAO),
                valor("BLK_DB_USER", "db.user", USUARIO_PADRAO),
                valor("BLK_DB_PASSWORD", "db.password", ""));
    }

    private static String valor(String variavelAmbiente, String chave, String padrao) {
        String ambiente = System.getenv(variavelAmbiente);
        if (ambiente != null && !ambiente.isEmpty()) {
            return ambiente;
        }
        return carregarConfig().getProperty(chave, padrao);
    }

    private static synchronized Properties carregarConfig() {
        if (config == null) {
            config = new Properties();
            try (InputStream in = new FileInputStream(ARQUIVO)) {
                config.load(in);
            } catch (IOException e) {
                // Sem arquivo: usa variáveis de ambiente ou valores padrão.
            }
        }
        return config;
    }
}
