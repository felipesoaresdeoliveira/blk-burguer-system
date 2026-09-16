package entidades;

public class Venda {

    private String produto;
    private int quantidade;
    private double valor;
    private double total;

    public String getProduto() { return produto; }
    public void setProduto(String produto) { this.produto = produto; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }

    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
}