package entidades;

public class Estoque {

    public static final String INGREDIENTE = "Ingrediente";
    public static final String BEBIDA = "Bebida";
    public static final String ACOMPANHAMENTO = "Acompanhamento";

    private String nome;
    private double preco;
    private int quantidade;
    private String tipo = INGREDIENTE;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public double getPreco() { return preco; }
    public void setPreco(double preco) { this.preco = preco; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}
