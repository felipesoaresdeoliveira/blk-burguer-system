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
    /** Ingredientes retirados ("sem cebola") e adicionais escolhidos, por nome. */
    private java.util.List<String> remocoes = new java.util.ArrayList<>();
    private java.util.List<String> adicionais = new java.util.ArrayList<>();

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

    public java.util.List<String> getRemocoes() { return remocoes; }
    public void setRemocoes(java.util.List<String> remocoes) { this.remocoes = remocoes; }

    public java.util.List<String> getAdicionais() { return adicionais; }
    public void setAdicionais(java.util.List<String> adicionais) { this.adicionais = adicionais; }

    public boolean isPersonalizado() {
        return !remocoes.isEmpty() || !adicionais.isEmpty() || (observacao != null && !observacao.trim().isEmpty());
    }

    /** Texto curto da personalização: "sem Alface | + Bacon extra | obs: bem passado". */
    public String getResumoPersonalizacao() {
        java.util.List<String> partes = new java.util.ArrayList<>();
        if (!remocoes.isEmpty()) {
            partes.add("sem " + String.join(", ", remocoes));
        }
        if (!adicionais.isEmpty()) {
            partes.add("+ " + String.join(", ", adicionais));
        }
        if (observacao != null && !observacao.trim().isEmpty()) {
            partes.add("obs: " + observacao.trim());
        }
        return String.join(" | ", partes);
    }

    /** Converte a lista gravada no banco ("a; b") em lista. */
    public static java.util.List<String> lista(String texto) {
        java.util.List<String> l = new java.util.ArrayList<>();
        if (texto != null) {
            for (String p : texto.split(";")) {
                if (!p.trim().isEmpty()) {
                    l.add(p.trim());
                }
            }
        }
        return l;
    }

    public static String texto(java.util.List<String> lista) {
        return lista == null || lista.isEmpty() ? null : String.join("; ", lista);
    }
}
