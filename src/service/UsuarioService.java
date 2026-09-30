package service;

import dao.UsuarioDAO;
import entidades.Modulo;
import entidades.Perfil;
import entidades.Usuario;
import java.sql.SQLException;
import java.util.List;

/** Regras de login e de gerenciamento de contas. */
public class UsuarioService {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    /**
     * Confere usuário e senha. Retorna o usuário ou null se inválido/inativo.
     * Senhas antigas em texto puro são convertidas para hash no primeiro login.
     */
    public Usuario autenticar(String login, String senha) throws SQLException {
        Object[] encontrado = usuarioDAO.buscarComSenha(login);
        if (encontrado == null) {
            return null;
        }
        Usuario u = (Usuario) encontrado[0];
        String gravado = (String) encontrado[1];
        if (!u.isAtivo() || !Senhas.confere(senha, gravado)) {
            return null;
        }
        if (!Senhas.ehHash(gravado)) {
            usuarioDAO.definirSenha(u.getId(), Senhas.gerarHash(senha));
        }
        return u;
    }

    public List<Usuario> listar() throws SQLException {
        Sessao.exigir(Modulo.USUARIOS);
        return usuarioDAO.listar();
    }

    public void criar(Usuario u, String senha) throws SQLException {
        Sessao.exigir(Modulo.USUARIOS);
        validar(u);
        Senhas.validarNova(senha);
        usuarioDAO.inserir(u, Senhas.gerarHash(senha));
    }

    public void atualizar(Usuario u) throws SQLException {
        Sessao.exigir(Modulo.USUARIOS);
        validar(u);
        boolean deixaDeSerAdminAtivo = !(u.getPerfil() == Perfil.ADMIN && u.isAtivo());
        if (deixaDeSerAdminAtivo && ehAdminAtivoHoje(u.getId()) && usuarioDAO.contarAdminsAtivos() <= 1) {
            throw new IllegalArgumentException("O sistema precisa de pelo menos um administrador ativo.");
        }
        Usuario logado = Sessao.usuario();
        if (logado != null && logado.getId() == u.getId() && !u.isAtivo()) {
            throw new IllegalArgumentException("Você não pode desativar a sua própria conta.");
        }
        usuarioDAO.atualizar(u);
    }

    public void redefinirSenha(int usuarioId, String novaSenha) throws SQLException {
        Usuario logado = Sessao.usuario();
        if (logado == null || (logado.getId() != usuarioId && !logado.pode(Modulo.USUARIOS))) {
            throw new SecurityException("Sem permissão para alterar esta senha.");
        }
        Senhas.validarNova(novaSenha);
        usuarioDAO.definirSenha(usuarioId, Senhas.gerarHash(novaSenha));
    }

    private boolean ehAdminAtivoHoje(int id) throws SQLException {
        for (Usuario atual : usuarioDAO.listar()) {
            if (atual.getId() == id) {
                return atual.getPerfil() == Perfil.ADMIN && atual.isAtivo();
            }
        }
        return false;
    }

    private static void validar(Usuario u) {
        if (u.getUsuario() == null || u.getUsuario().trim().isEmpty()) {
            throw new IllegalArgumentException("Informe o login do usuário.");
        }
        if (u.getUsuario().length() > 100) {
            throw new IllegalArgumentException("O login deve ter no máximo 100 caracteres.");
        }
        if (u.getPerfil() == null) {
            throw new IllegalArgumentException("Escolha o perfil.");
        }
    }
}
