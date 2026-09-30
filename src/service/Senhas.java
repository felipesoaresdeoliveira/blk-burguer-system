package service;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Hash de senhas com PBKDF2-HMAC-SHA256 e sal aleatório.
 * Formato gravado: pbkdf2$iteracoes$sal$hash (Base64).
 */
public final class Senhas {

    private static final String PREFIXO = "pbkdf2$";
    private static final int ITERACOES = 120_000;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private Senhas() {
    }

    public static String gerarHash(String senha) {
        byte[] sal = new byte[16];
        ALEATORIO.nextBytes(sal);
        byte[] hash = pbkdf2(senha.toCharArray(), sal, ITERACOES);
        Base64.Encoder b64 = Base64.getEncoder();
        return PREFIXO + ITERACOES + "$" + b64.encodeToString(sal) + "$" + b64.encodeToString(hash);
    }

    /** true se o valor gravado já é um hash (e não uma senha antiga em texto). */
    public static boolean ehHash(String gravado) {
        return gravado != null && gravado.startsWith(PREFIXO);
    }

    public static boolean confere(String senha, String gravado) {
        if (gravado == null) {
            return false;
        }
        if (!ehHash(gravado)) {
            // Senha antiga em texto puro: comparação em tempo constante.
            return MessageDigest.isEqual(senha.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                    gravado.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
        String[] partes = gravado.split("\\$");
        if (partes.length != 4) {
            return false;
        }
        int iteracoes = Integer.parseInt(partes[1]);
        byte[] sal = Base64.getDecoder().decode(partes[2]);
        byte[] esperado = Base64.getDecoder().decode(partes[3]);
        return MessageDigest.isEqual(esperado, pbkdf2(senha.toCharArray(), sal, iteracoes));
    }

    /** Regra mínima de senha para novas contas. */
    public static void validarNova(String senha) {
        if (senha == null || senha.length() < 6) {
            throw new IllegalArgumentException("A senha deve ter pelo menos 6 caracteres.");
        }
    }

    private static byte[] pbkdf2(char[] senha, byte[] sal, int iteracoes) {
        try {
            KeySpec spec = new PBEKeySpec(senha, sal, iteracoes, 256);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao calcular hash de senha", e);
        }
    }
}
