package entidades;

/** Categorias de despesa operacional (#40). */
public enum CategoriaDespesa {
    ALUGUEL("Aluguel"),
    ENERGIA("Energia"),
    INTERNET("Internet"),
    FORNECEDOR("Fornecedor"),
    MANUTENCAO("Manutenção"),
    OUTROS("Outros");

    private final String descricao;

    CategoriaDespesa(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
