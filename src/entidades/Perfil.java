package entidades;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/** Perfis de usuário e os módulos que cada um acessa. */
public enum Perfil {

    ADMIN("Administrador", EnumSet.allOf(Modulo.class)),
    GERENTE("Gerente", EnumSet.complementOf(EnumSet.of(Modulo.USUARIOS))),
    CAIXA("Caixa", EnumSet.of(Modulo.VENDA_BALCAO, Modulo.CAIXA, Modulo.CLIENTES, Modulo.HISTORICO)),
    GARCOM("Garçom", EnumSet.of(Modulo.PEDIDOS, Modulo.MESAS, Modulo.CLIENTES)),
    COZINHA("Cozinha", EnumSet.of(Modulo.COZINHA));

    private final String descricao;
    private final Set<Modulo> modulos;

    Perfil(String descricao, Set<Modulo> modulos) {
        this.descricao = descricao;
        this.modulos = Collections.unmodifiableSet(modulos);
    }

    public boolean pode(Modulo modulo) {
        return modulos.contains(modulo);
    }

    public Set<Modulo> getModulos() {
        return modulos;
    }

    /** Texto curto do que o perfil faz, para a tela de usuários. */
    public String resumo() {
        switch (this) {
            case ADMIN: return "O chefe: acesso total, cria e gerencia as contas";
            case GERENTE: return "Gerencia a operação: tudo, exceto criar contas";
            case CAIXA: return "Balcão: vender, receber, abrir e fechar o caixa";
            case GARCOM: return "Mesas, comandas, pedidos e clientes";
            default: return "Painel da cozinha (ideal para a TV)";
        }
    }

    @Override
    public String toString() {
        return descricao;
    }
}
