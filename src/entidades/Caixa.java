package entidades;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Caixa diário com o resumo das entradas por forma de pagamento. */
public class Caixa {

    /** Sangria ou suprimento registrado no caixa. */
    public static class Movimentacao {
        public final String tipo;
        public final double valor;
        public final String motivo;
        public final String usuario;
        public final LocalDateTime data;

        public Movimentacao(String tipo, double valor, String motivo, String usuario, LocalDateTime data) {
            this.tipo = tipo;
            this.valor = valor;
            this.motivo = motivo;
            this.usuario = usuario;
            this.data = data;
        }
    }

    private int id;
    private LocalDateTime abertoEm;
    private String abertoPor;
    private double valorInicial;
    private LocalDateTime fechadoEm;
    private final Map<FormaPagamento, Double> recebido = new EnumMap<>(FormaPagamento.class);
    private final Map<FormaPagamento, Double> informado = new EnumMap<>(FormaPagamento.class);
    private double suprimentos;
    private double sangrias;
    private int pagamentos;
    private List<Movimentacao> movimentacoes = new ArrayList<>();

    public Caixa() {
        for (FormaPagamento f : FormaPagamento.values()) {
            recebido.put(f, 0.0);
        }
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDateTime getAbertoEm() { return abertoEm; }
    public void setAbertoEm(LocalDateTime abertoEm) { this.abertoEm = abertoEm; }

    public String getAbertoPor() { return abertoPor; }
    public void setAbertoPor(String abertoPor) { this.abertoPor = abertoPor; }

    public double getValorInicial() { return valorInicial; }
    public void setValorInicial(double valorInicial) { this.valorInicial = valorInicial; }

    public LocalDateTime getFechadoEm() { return fechadoEm; }
    public void setFechadoEm(LocalDateTime fechadoEm) { this.fechadoEm = fechadoEm; }

    public boolean isAberto() { return fechadoEm == null; }

    /** Total recebido em pagamentos por forma. */
    public Map<FormaPagamento, Double> getRecebido() { return recebido; }

    /** Valores contados no fechamento (vazio enquanto aberto). */
    public Map<FormaPagamento, Double> getInformado() { return informado; }

    public double getSuprimentos() { return suprimentos; }
    public void setSuprimentos(double suprimentos) { this.suprimentos = suprimentos; }

    public double getSangrias() { return sangrias; }
    public void setSangrias(double sangrias) { this.sangrias = sangrias; }

    public int getPagamentos() { return pagamentos; }
    public void setPagamentos(int pagamentos) { this.pagamentos = pagamentos; }

    public List<Movimentacao> getMovimentacoes() { return movimentacoes; }
    public void setMovimentacoes(List<Movimentacao> movimentacoes) { this.movimentacoes = movimentacoes; }

    /** Valor esperado na gaveta/conta para a forma: dinheiro soma o fundo e as movimentações. */
    public double getEsperado(FormaPagamento forma) {
        double v = recebido.get(forma);
        if (forma == FormaPagamento.DINHEIRO) {
            v += valorInicial + suprimentos - sangrias;
        }
        return Math.round(v * 100) / 100.0;
    }

    public double getTotalVendido() {
        double t = 0;
        for (double v : recebido.values()) {
            t += v;
        }
        return Math.round(t * 100) / 100.0;
    }
}
