package entidades;

/** Situação financeira do pedido. O andamento na cozinha fica em cada item. */
public enum SituacaoPedido {
    ABERTO("Aberto"),
    PAGO("Pago"),
    CANCELADO("Cancelado");

    private final String descricao;

    SituacaoPedido(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
