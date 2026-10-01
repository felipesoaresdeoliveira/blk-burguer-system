package entidades;

/** Áreas do sistema controladas por perfil. */
public enum Modulo {
    DASHBOARD("Visão geral"),
    VENDA_BALCAO("Nova venda"),
    PEDIDOS("Pedidos"),
    MESAS("Mesas"),
    COZINHA("Cozinha"),
    CAIXA("Caixa"),
    CLIENTES("Clientes"),
    HISTORICO("Histórico de vendas"),
    PRODUTOS("Produtos"),
    ESTOQUE("Estoque"),
    RELATORIOS("Relatórios"),
    USUARIOS("Usuários"),
    /** Cadastros gerais da operação: mesas, taxa de entrega. */
    CADASTROS("Cadastros gerais"),
    /** Ações sensíveis: cancelar pedido, excluir cadastros. */
    CANCELAMENTOS("Cancelamentos");

    private final String descricao;

    Modulo(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
