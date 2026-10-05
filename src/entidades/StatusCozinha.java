package entidades;

/** Andamento de um item na cozinha. */
public enum StatusCozinha {
    RECEBIDO("Recebido"),
    EM_PREPARO("Em preparo"),
    PRONTO("Pronto"),
    ENTREGUE("Entregue");

    private final String descricao;

    StatusCozinha(String descricao) {
        this.descricao = descricao;
    }

    /** Próxima etapa, ou null se já foi entregue. */
    public StatusCozinha proximo() {
        return this == ENTREGUE ? null : values()[ordinal() + 1];
    }

    @Override
    public String toString() {
        return descricao;
    }
}
