package impressao;

import dao.DashboardDAO;
import entidades.Caixa;
import entidades.FormaPagamento;
import entidades.Pedido;
import entidades.TipoPedido;
import entidades.Venda;
import impressao.Documento.Estilo;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import service.FechamentoService;

/**
 * Os documentos imprimíveis do sistema (#38): conta da mesa, pedido para a
 * cozinha, recibo da venda e fechamento do dia. Os valores vêm do pedido
 * salvo no banco, então batem com o que aparece nas telas.
 */
public final class Documentos {

    private static final Locale BR = new Locale("pt", "BR");
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Documentos() {
    }

    static String reais(double v) {
        return String.format(BR, "R$ %,.2f", v);
    }

    private static void cabecalho(Documento d, String tipo) {
        d.centro("BLK BURGUER", Estilo.GRANDE);
        d.centro("Hamburgueria artesanal", Estilo.FRACO);
        d.separadorDuplo();
        d.centro(tipo, Estilo.NEGRITO);
        d.separador();
    }

    private static void rodapeNaoFiscal(Documento d) {
        d.separador();
        d.centro("Documento sem valor fiscal", Estilo.FRACO);
        d.centro("Impresso em " + LocalDateTime.now().format(DATA_HORA), Estilo.FRACO);
    }

    /** Identificação do pedido: número, data e para quem/onde. */
    private static void identificacao(Documento d, Pedido p) {
        d.par("Pedido nº " + p.getId(), p.getData() == null ? "" : p.getData().format(DATA_HORA), Estilo.NEGRITO);
        d.texto(p.getDescricao());
        if (p.getTipo() == TipoPedido.DELIVERY && p.getEnderecoEntrega() != null) {
            d.texto("Entrega: " + p.getEnderecoEntrega());
        }
        d.separador();
    }

    /** Itens com quantidade, nome e total; personalizações em linhas recuadas. */
    private static void itens(Documento d, List<Venda> itens) {
        for (Venda v : itens) {
            d.item(v.getQuantidade(), v.getProduto(), reais(v.getTotal()));
            if (v.getQuantidade() > 1) {
                d.recuado(v.getQuantidade() + " x " + reais(v.getValor()), 3, Estilo.FRACO);
            }
            personalizacoes(d, v, Estilo.FRACO, 3);
        }
    }

    private static void personalizacoes(Documento d, Venda v, Estilo estilo, int recuo) {
        for (String r : v.getRemocoes()) {
            d.recuado("SEM " + r, recuo, estilo);
        }
        for (String a : v.getAdicionais()) {
            d.recuado("+ " + a, recuo, estilo);
        }
        if (v.getObservacao() != null && !v.getObservacao().trim().isEmpty()) {
            d.recuado("Obs: " + v.getObservacao().trim(), recuo, estilo);
        }
    }

    private static void totais(Documento d, Pedido p) {
        double itens = Math.round((p.getSubtotal() - p.getTaxaEntrega()) * 100) / 100.0;
        d.par("Subtotal", reais(itens));
        if (p.getTaxaEntrega() > 0) {
            d.par("Taxa de entrega", reais(p.getTaxaEntrega()));
        }
        if (p.getDesconto() > 0) {
            d.par("Desconto", "- " + reais(p.getDesconto()));
        }
        d.par("TOTAL", reais(p.getTotal()), Estilo.GRANDE);
    }

    // ------------------------------------------------------------ documentos

    /** Conta da mesa para o cliente conferir antes de pagar. */
    public static Documento comanda(Pedido p, int colunas) {
        Documento d = new Documento("Conta - pedido " + p.getId(), colunas);
        cabecalho(d, p.getTipo() == TipoPedido.MESA ? "CONFERÊNCIA DE CONTA" : "COMANDA");
        identificacao(d, p);
        if (p.getItens().isEmpty()) {
            d.centro("Nenhum item lançado", Estilo.FRACO);
        }
        itens(d, p.getItens());
        d.separador();
        totais(d, p);
        if (p.getPago() > 0) {
            d.par("Já pago", reais(p.getPago()));
            d.par("Falta pagar", reais(p.getSaldo()), Estilo.NEGRITO);
        }
        rodapeNaoFiscal(d);
        return d;
    }

    /**
     * Pedido para a cozinha: letras grandes, sem preços, só o que vai para o
     * preparo (bebidas não entram).
     */
    public static Documento cozinha(Pedido p, int colunas) {
        Documento d = new Documento("Cozinha - pedido " + p.getId(), colunas);
        d.centro("COZINHA", Estilo.NEGRITO);
        d.separadorDuplo();
        String onde = p.getTipo() == TipoPedido.MESA ? "MESA " + p.getMesaNumero()
                : p.getSenha() > 0 ? "SENHA " + p.getSenha() : "PEDIDO " + p.getId();
        d.centro(onde, Estilo.GRANDE);
        d.centro(p.getDescricao(), Estilo.NORMAL);
        d.par("Pedido nº " + p.getId(), p.getData() == null ? "" : p.getData().format(HORA));
        if (p.getTipo() == TipoPedido.RETIRADA || p.getTipo() == TipoPedido.DELIVERY) {
            d.centro(p.getTipo() == TipoPedido.DELIVERY ? "*** DELIVERY ***" : "*** PARA VIAGEM ***", Estilo.NEGRITO);
        }
        d.separador();
        // Pedidos antigos não têm andamento de cozinha: aí sai tudo.
        boolean filtrar = p.getItens().stream().anyMatch(v -> v.getStatusCozinha() != null);
        int n = 0;
        for (Venda v : p.getItens()) {
            if (filtrar && v.getStatusCozinha() == null) {
                continue;
            }
            n++;
            d.texto(v.getQuantidade() + "x " + v.getProduto().toUpperCase(BR), Estilo.GRANDE);
            personalizacoes(d, v, Estilo.NEGRITO, 3);
            d.espaco();
        }
        if (n == 0) {
            d.centro("Nenhum item para a cozinha", Estilo.FRACO);
        }
        d.separador();
        d.centro("Impresso às " + LocalDateTime.now().format(HORA), Estilo.FRACO);
        return d;
    }

    /**
     * Recibo da venda.
     *
     * @param pagamentos {forma, valor, recebido, troco} de cada pagamento
     */
    public static Documento recibo(Pedido p, List<Object[]> pagamentos, int colunas) {
        Documento d = new Documento("Recibo - pedido " + p.getId(), colunas);
        cabecalho(d, "RECIBO");
        identificacao(d, p);
        itens(d, p.getItens());
        d.separador();
        totais(d, p);
        if (!pagamentos.isEmpty()) {
            d.separador();
            d.texto("Pagamento", Estilo.NEGRITO);
            double troco = 0;
            for (Object[] pg : pagamentos) {
                FormaPagamento forma = (FormaPagamento) pg[0];
                double valor = (Double) pg[1];
                double recebido = (Double) pg[2];
                troco += (Double) pg[3];
                d.par(forma.toString(), reais(valor));
                if (forma == FormaPagamento.DINHEIRO && recebido > valor) {
                    d.recuado("Recebido " + reais(recebido), 3, Estilo.FRACO);
                }
            }
            if (troco > 0) {
                d.par("Troco", reais(troco), Estilo.NEGRITO);
            }
        }
        if (p.getSaldo() > 0) {
            d.par("Falta pagar", reais(p.getSaldo()), Estilo.NEGRITO);
        }
        d.espaco();
        d.centro("Obrigado pela preferência!", Estilo.NEGRITO);
        rodapeNaoFiscal(d);
        return d;
    }

    /** Resumo do fechamento do dia (#41). */
    public static Documento fechamento(FechamentoService.Fechamento f, int colunas) {
        Documento d = new Documento("Fechamento " + f.dia.format(DATA), colunas);
        cabecalho(d, "FECHAMENTO DO DIA");
        d.centro(f.dia.format(DATA), Estilo.GRANDE);
        d.separador();
        d.texto("Vendas", Estilo.NEGRITO);
        d.par("Pedidos pagos", String.valueOf(f.resumo.vendas));
        d.par("Faturamento", reais(f.resumo.faturamento));
        d.par("Ticket médio", reais(f.resumo.ticketMedio()));
        d.par("Descontos concedidos", reais(f.descontos));
        if (f.maisVendido != null) {
            d.par("Mais vendido: " + f.maisVendido.nome, f.maisVendido.quantidade + " un.");
        }
        d.separador();
        d.texto("Recebido por forma de pagamento", Estilo.NEGRITO);
        for (DashboardDAO.Linha l : f.formas.values()) {
            d.par(l.nome, reais(l.valor));
        }
        d.par("Total recebido", reais(f.getTotalRecebido()), Estilo.NEGRITO);
        d.separador();
        d.texto("Ocorrências", Estilo.NEGRITO);
        d.par("Cancelamentos (" + f.cancelamentos + ")", reais(f.valorCancelado));
        d.par("Perdas de estoque (" + f.perdas + ")", reais(f.valorPerdas));
        d.par("Despesas do dia", reais(f.despesas));
        d.separador();
        d.texto("Caixa", Estilo.NEGRITO);
        if (f.caixas.isEmpty()) {
            d.texto("Nenhum caixa aberto no dia.", Estilo.FRACO);
        }
        for (Caixa c : f.caixas) {
            String rotulo = "Caixa " + c.getId() + " (" + c.getAbertoEm().format(HORA)
                    + (c.isAberto() ? ", ABERTO)" : " às " + c.getFechadoEm().format(HORA) + ")");
            d.par(rotulo, c.isAberto() ? reais(c.getTotalVendido()) : diferenca(c.getDiferencaTotal()));
        }
        if (f.caixas.stream().anyMatch(c -> !c.isAberto())) {
            d.recuado("Valor ao lado: diferença contada x esperada", 0, Estilo.FRACO);
        }
        d.separadorDuplo();
        d.par("RESULTADO DO DIA", reais(f.getResultado()), Estilo.NEGRITO);
        d.recuado("Faturamento - despesas - perdas", 0, Estilo.FRACO);
        rodapeNaoFiscal(d);
        return d;
    }

    private static String diferenca(double v) {
        if (Math.abs(v) < 0.005) {
            return "sem diferença";
        }
        return (v > 0 ? "sobra " : "falta ") + reais(Math.abs(v));
    }
}
