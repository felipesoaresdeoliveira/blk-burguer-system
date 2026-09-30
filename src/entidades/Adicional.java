package entidades;

/** Adicional cobrado à parte em um lanche (ex.: bacon extra). */
public class Adicional {

    private int id;
    private String nome;
    private double preco;
    /** Item de estoque consumido (pode ser null) e quantidade por adicional. */
    private String ingrediente;
    private int quantidade = 1;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public double getPreco() { return preco; }
    public void setPreco(double preco) { this.preco = preco; }

    public String getIngrediente() { return ingrediente; }
    public void setIngrediente(String ingrediente) { this.ingrediente = ingrediente; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }

    @Override
    public String toString() {
        return String.format("%s (+R$ %.2f)", nome, preco);
    }
}
