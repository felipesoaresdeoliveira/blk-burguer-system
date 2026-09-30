package entidades;

public enum FormaPagamento {

    DINHEIRO("Dinheiro"),
    PIX("Pix"),
    DEBITO("Débito"),
    CREDITO("Crédito");

    private final String descricao;

    FormaPagamento(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
