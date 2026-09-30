package entidades;

/** Item de um pedido (tabela Venda). */
public class Venda {

    private int id;
    private String produto;
    private int quantidade;
    private double valor;
    private double total;
    private String observacao;
    /** null = não passa pela cozinha (ex.: bebida). */
    private StatusCozinha statusCozinha;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getProduto() { return produto; }
    public void setProduto(String produto) { this.produto = produto; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }

    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }

    public StatusCozinha getStatusCozinha() { return statusCozinha; }
    public void setStatusCozinha(StatusCozinha statusCozinha) { this.statusCozinha = statusCozinha; }
}
