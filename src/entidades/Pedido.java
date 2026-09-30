package entidades;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Pedido (cabeçalho): comanda de mesa, venda de balcão, retirada ou delivery.
 * Vendas antigas, registradas antes do agrupamento, aparecem com id negativo
 * (o id do próprio item) e sem forma de pagamento.
 */
public class Pedido {

    private int id;
    private LocalDateTime data;
    private FormaPagamento formaPagamento;
    private double total;
    private String resumoItens;

    private TipoPedido tipo = TipoPedido.BALCAO;
    private SituacaoPedido situacao = SituacaoPedido.ABERTO;
    private int mesaId;
    private int mesaNumero;
    private int clienteId;
    private String clienteNome;
    private String identificacao;
    private int senha;
    private double taxaEntrega;
    private String enderecoEntrega;
    private String statusEntrega;
    private boolean contaSolicitada;
    private double pago;
    private List<Venda> itens = new ArrayList<>();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public boolean isAvulsa() { return id < 0; }

    public LocalDateTime getData() { return data; }
    public void setData(LocalDateTime data) { this.data = data; }

    /** Forma usada na venda (vendas antigas) ou null quando houver vários pagamentos. */
    public FormaPagamento getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(FormaPagamento formaPagamento) { this.formaPagamento = formaPagamento; }

    /** Total a pagar: itens + taxa de entrega. */
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public String getResumoItens() { return resumoItens; }
    public void setResumoItens(String resumoItens) { this.resumoItens = resumoItens; }

    public TipoPedido getTipo() { return tipo; }
    public void setTipo(TipoPedido tipo) { this.tipo = tipo; }

    public SituacaoPedido getSituacao() { return situacao; }
    public void setSituacao(SituacaoPedido situacao) { this.situacao = situacao; }

    public int getMesaId() { return mesaId; }
    public void setMesaId(int mesaId) { this.mesaId = mesaId; }

    public int getMesaNumero() { return mesaNumero; }
    public void setMesaNumero(int mesaNumero) { this.mesaNumero = mesaNumero; }

    public int getClienteId() { return clienteId; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }

    public String getClienteNome() { return clienteNome; }
    public void setClienteNome(String clienteNome) { this.clienteNome = clienteNome; }

    public String getIdentificacao() { return identificacao; }
    public void setIdentificacao(String identificacao) { this.identificacao = identificacao; }

    public int getSenha() { return senha; }
    public void setSenha(int senha) { this.senha = senha; }

    public double getTaxaEntrega() { return taxaEntrega; }
    public void setTaxaEntrega(double taxaEntrega) { this.taxaEntrega = taxaEntrega; }

    public String getEnderecoEntrega() { return enderecoEntrega; }
    public void setEnderecoEntrega(String enderecoEntrega) { this.enderecoEntrega = enderecoEntrega; }

    /** null, SAIU_PARA_ENTREGA ou ENTREGUE (apenas delivery). */
    public String getStatusEntrega() { return statusEntrega; }
    public void setStatusEntrega(String statusEntrega) { this.statusEntrega = statusEntrega; }

    public boolean isContaSolicitada() { return contaSolicitada; }
    public void setContaSolicitada(boolean contaSolicitada) { this.contaSolicitada = contaSolicitada; }

    /** Soma dos pagamentos já recebidos. */
    public double getPago() { return pago; }
    public void setPago(double pago) { this.pago = pago; }

    public double getSaldo() { return Math.max(0, Math.round((total - pago) * 100) / 100.0); }

    public List<Venda> getItens() { return itens; }
    public void setItens(List<Venda> itens) { this.itens = itens; }

    /** Como o pedido é chamado nas telas: "Mesa 4", "Senha 12", "Delivery - Maria". */
    public String getDescricao() {
        switch (tipo) {
            case MESA:
                return "Mesa " + mesaNumero;
            case DELIVERY:
                return "Delivery" + (clienteNome != null ? " - " + clienteNome : "");
            default:
                String nome = identificacao != null && !identificacao.isEmpty() ? identificacao
                        : clienteNome != null ? clienteNome : null;
                return (senha > 0 ? "Senha " + senha : tipo.toString()) + (nome != null ? " - " + nome : "");
        }
    }

    /** Situação do pedido como um todo, considerando cozinha e entrega. */
    public String getAndamento() {
        if (situacao == SituacaoPedido.CANCELADO) {
            return "Cancelado";
        }
        if ("ENTREGUE".equals(statusEntrega)) {
            return "Entregue";
        }
        if ("SAIU_PARA_ENTREGA".equals(statusEntrega)) {
            return "Saiu para entrega";
        }
        StatusCozinha menor = null;
        for (Venda v : itens) {
            if (v.getStatusCozinha() != null && (menor == null || v.getStatusCozinha().ordinal() < menor.ordinal())) {
                menor = v.getStatusCozinha();
            }
        }
        if (menor == null) {
            return itens.isEmpty() ? "Sem itens" : "Pronto";
        }
        return menor.toString();
    }
}
