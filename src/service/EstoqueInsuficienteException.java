package service;

/** Lançada quando o estoque não cobre o consumo de uma venda. */
public class EstoqueInsuficienteException extends Exception {

    public EstoqueInsuficienteException(String mensagem) {
        super(mensagem);
    }
}
