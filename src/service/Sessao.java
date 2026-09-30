package service;

import entidades.Modulo;
import entidades.Usuario;

/** Usuário logado no sistema. */
public final class Sessao {

    private static Usuario atual;

    private Sessao() {
    }

    public static void iniciar(Usuario usuario) {
        atual = usuario;
    }

    public static void encerrar() {
        atual = null;
    }

    public static Usuario usuario() {
        return atual;
    }

    /** Id do usuário logado ou null (telas abertas sem login, em testes). */
    public static Integer usuarioId() {
        return atual == null ? null : atual.getId();
    }

    public static boolean pode(Modulo modulo) {
        return atual != null && atual.pode(modulo);
    }

    /** Lança exceção se o usuário logado não tiver acesso ao módulo. */
    public static void exigir(Modulo modulo) {
        if (!pode(modulo)) {
            throw new SecurityException("Seu perfil não tem acesso a: " + modulo);
        }
    }
}
