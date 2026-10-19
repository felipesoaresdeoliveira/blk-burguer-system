package service;

/** Lançada quando o estoque não cobre o consumo de uma venda. */
public class EstoqueInsuficienteException extends RuntimeException {

    public EstoqueInsuficienteException(String mensagem) {
        super(mensagem);
    }
}
