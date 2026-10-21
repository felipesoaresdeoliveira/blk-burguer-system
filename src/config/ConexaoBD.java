package config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Properties;

/**
 * Ponto único de conexão com o PostgreSQL.
 *
 * A configuração é lida do arquivo db.properties (na pasta onde o sistema é
 * executado). Variáveis de ambiente BLK_DB_URL, BLK_DB_USER e BLK_DB_PASSWORD,
 * quando definidas, têm prioridade sobre o arquivo.
 *
 * Abrir uma conexão com um servidor na internet demora (rede, criptografia e
 * login). Por isso as conexões não são fechadas de verdade: quando o código
 * chama close(), a conexão volta para uma lista e é reaproveitada na próxima
 * consulta. O resto do sistema continua usando getConnection() e close() como
 * sempre.
 */
public final class ConexaoBD {

    private static final String ARQUIVO = "db.properties";
    private static final String URL_PADRAO = "jdbc:postgresql://localhost:5432/BLKburguer";
    private static final String USUARIO_PADRAO = "postgres";

    /** Quantas conexões ficam guardadas para reaproveitar. */
    private static final int MAXIMO_GUARDADAS = 4;

    /** Uma conexão guardada passa a ser conferida se ficou parada mais que isto. */
    private static final long PARADA_MAXIMA_MS = 30_000;

    /** Conexão guardada e o momento em que voltou para a lista. */
    private static final class Guardada {
        final Connection conexao;
        final long desde = System.currentTimeMillis();

        Guardada(Connection conexao) {
            this.conexao = conexao;
        }
    }

    private static final LinkedList<Guardada> GUARDADAS = new LinkedList<>();
    private static Properties config;

    private ConexaoBD() {
    }

    /** Entrega uma conexão pronta (guardada ou nova). Use com try-with-resources. */
    public static Connection getConnection() throws SQLException {
        Connection real = pegarGuardada();
        if (real == null) {
            real = abrirNova();
        }
        return embrulhar(real);
    }

    private static Connection abrirNova() throws SQLException {
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

    private static Connection pegarGuardada() {
        while (true) {
            Guardada g;
            synchronized (GUARDADAS) {
                g = GUARDADAS.pollFirst();
            }
            if (g == null) {
                return null;
            }
            try {
                boolean parouMuito = System.currentTimeMillis() - g.desde > PARADA_MAXIMA_MS;
                if (!g.conexao.isClosed() && (!parouMuito || g.conexao.isValid(3))) {
                    return g.conexao;
                }
                g.conexao.close();
            } catch (SQLException e) {
                // Conexão quebrada: descarta e tenta a próxima.
            }
        }
    }

    /** Devolve a conexão para a lista (limpa, sem transação aberta) ou fecha se a lista está cheia. */
    private static void devolver(Connection real) {
        try {
            if (real.isClosed()) {
                return;
            }
            if (!real.getAutoCommit()) {
                real.rollback();
                real.setAutoCommit(true);
            }
            synchronized (GUARDADAS) {
                if (GUARDADAS.size() < MAXIMO_GUARDADAS) {
                    GUARDADAS.addFirst(new Guardada(real));
                    return;
                }
            }
            real.close();
        } catch (SQLException e) {
            try {
                real.close();
            } catch (SQLException ignorado) {
                // Já estava quebrada.
            }
        }
    }

    /**
     * Devolve um "embrulho" da conexão: tudo funciona igual, só que close()
     * manda a conexão de volta para a lista em vez de fechar.
     */
    private static Connection embrulhar(Connection real) {
        InvocationHandler tratador = (proxy, metodo, argumentos) -> {
            if (metodo.getName().equals("close")) {
                devolver(real);
                return null;
            }
            try {
                return metodo.invoke(real, argumentos);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        };
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(), new Class<?>[]{Connection.class}, tratador);
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
