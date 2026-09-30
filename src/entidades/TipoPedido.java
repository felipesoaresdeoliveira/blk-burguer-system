package entidades;

public enum TipoPedido {
    MESA("Mesa"),
    BALCAO("Balcão"),
    RETIRADA("Retirada"),
    DELIVERY("Delivery");

    private final String descricao;

    TipoPedido(String descricao) {
        this.descricao = descricao;
    }

    /** Balcão e retirada recebem senha para chamar o cliente. */
    public boolean usaSenha() {
        return this == BALCAO || this == RETIRADA;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
