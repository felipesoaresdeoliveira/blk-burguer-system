package entidades;

/** Ações críticas registradas na trilha de auditoria (#37). */
public enum AcaoAuditoria {
    LOGIN("Entrou no sistema"),
    PEDIDO_CANCELADO("Pedido cancelado"),
    DESCONTO("Desconto concedido"),
    PRECO_ALTERADO("Preço alterado"),
    PRODUTO_EXCLUIDO("Produto excluído"),
    ESTOQUE_AJUSTE("Ajuste de estoque"),
    ESTOQUE_PERDA("Perda de estoque"),
    ESTOQUE_EXCLUIDO("Item de estoque excluído"),
    CAIXA_ABERTO("Caixa aberto"),
    CAIXA_FECHADO("Caixa fechado"),
    SANGRIA("Sangria"),
    SUPRIMENTO("Suprimento"),
    USUARIO_CRIADO("Conta criada"),
    USUARIO_ALTERADO("Conta alterada"),
    SENHA_REDEFINIDA("Senha redefinida"),
    DESPESA_REGISTRADA("Despesa registrada"),
    DESPESA_EXCLUIDA("Despesa excluída");

    private final String descricao;

    AcaoAuditoria(String descricao) {
        this.descricao = descricao;
    }

    /** Converte o nome gravado no banco; ações desconhecidas voltam null. */
    public static AcaoAuditoria de(String nome) {
        try {
            return valueOf(nome);
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return descricao;
    }
}
