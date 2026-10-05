package entidades;

public class Mesa {

    /** Situação exibida no mapa de mesas. */
    public enum Status {
        LIVRE("Livre"),
        OCUPADA("Ocupada"),
        AGUARDANDO_PAGAMENTO("Aguardando pagamento"),
        RESERVADA("Reservada");

        private final String descricao;

        Status(String descricao) {
            this.descricao = descricao;
        }

        @Override
        public String toString() {
            return descricao;
        }
    }

    private int id;
    private int numero;
    private int lugares = 4;
    private boolean reservada;
    private boolean ativa = true;
    /** Pedido aberto na mesa (0 = nenhum) e dados para o mapa. */
    private int pedidoId;
    private boolean contaSolicitada;
    private double totalPedido;
    private java.time.LocalDateTime abertaEm;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }

    public int getLugares() { return lugares; }
    public void setLugares(int lugares) { this.lugares = lugares; }

    public boolean isReservada() { return reservada; }
    public void setReservada(boolean reservada) { this.reservada = reservada; }

    public boolean isAtiva() { return ativa; }
    public void setAtiva(boolean ativa) { this.ativa = ativa; }

    public int getPedidoId() { return pedidoId; }
    public void setPedidoId(int pedidoId) { this.pedidoId = pedidoId; }

    public boolean isContaSolicitada() { return contaSolicitada; }
    public void setContaSolicitada(boolean contaSolicitada) { this.contaSolicitada = contaSolicitada; }

    public double getTotalPedido() { return totalPedido; }
    public void setTotalPedido(double totalPedido) { this.totalPedido = totalPedido; }

    public java.time.LocalDateTime getAbertaEm() { return abertaEm; }
    public void setAbertaEm(java.time.LocalDateTime abertaEm) { this.abertaEm = abertaEm; }

    public Status getStatus() {
        if (pedidoId > 0) {
            return contaSolicitada ? Status.AGUARDANDO_PAGAMENTO : Status.OCUPADA;
        }
        return reservada ? Status.RESERVADA : Status.LIVRE;
    }

    @Override
    public String toString() {
        return "Mesa " + numero;
    }
}
