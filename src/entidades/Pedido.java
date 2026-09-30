package entidades;

import java.time.LocalDateTime;

/**
 * Venda agrupada (cabeçalho). Vendas antigas, registradas antes do
 * agrupamento, aparecem com id negativo (o id do próprio item) e sem forma
 * de pagamento.
 */
public class Pedido {

    private int id;
    private LocalDateTime data;
    private FormaPagamento formaPagamento;
    private double total;
    private String resumoItens;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public boolean isAvulsa() { return id < 0; }

    public LocalDateTime getData() { return data; }
    public void setData(LocalDateTime data) { this.data = data; }

    public FormaPagamento getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(FormaPagamento formaPagamento) { this.formaPagamento = formaPagamento; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public String getResumoItens() { return resumoItens; }
    public void setResumoItens(String resumoItens) { this.resumoItens = resumoItens; }
}
