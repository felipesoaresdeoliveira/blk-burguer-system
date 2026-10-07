package entidades;

/** Motivos de saída de estoque que não são venda (#34). */
public enum MotivoPerda {
    VENCIMENTO("Vencimento"),
    QUEBRA("Quebra"),
    DESCARTE("Descarte"),
    PERDA("Perda");

    private final String descricao;

    MotivoPerda(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
