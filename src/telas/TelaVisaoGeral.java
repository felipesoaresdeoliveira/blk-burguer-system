package telas;

import dao.DashboardDAO;
import entidades.Estoque;
import entidades.FormaPagamento;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import ui.CartaoIndicador;
import ui.Tema;

/** Visão geral (dashboard): indicadores do dia e gráficos dos últimos 7 dias. */
public class TelaVisaoGeral extends javax.swing.JPanel implements Navegavel {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(TelaVisaoGeral.class.getName());
    private static final Locale BR = new Locale("pt", "BR");

    private final DashboardDAO dashboardDAO = new DashboardDAO();

    public TelaVisaoGeral() {
        initComponents();
        setBackground(Tema.FUNDO);
        Tema.titulo(lblTitulo);
        Tema.secundario(lblData);
        Tema.transparente(painelCabecalho, painelTitulo, painelAcoesTopo, painelCorpo, painelIndicadores, painelGraficos);
        carregarDados();
    }

    /** Busca os números em segundo plano para a tela não travar. */
    private void carregarDados() {
        btnAtualizar.setEnabled(false);
        LocalDate hoje = LocalDate.now();
        lblData.setText(capitalizar(hoje.format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", BR))));
        new javax.swing.SwingWorker<Object[], Void>() {
            @Override
            protected Object[] doInBackground() throws Exception {
                LocalDate seteDias = hoje.minusDays(6);
                return new Object[]{
                    dashboardDAO.resumo(hoje, hoje),
                    dashboardDAO.resumo(hoje.minusDays(1), hoje.minusDays(1)),
                    dashboardDAO.resumo(seteDias, hoje),
                    dashboardDAO.faturamentoPorDia(seteDias, hoje),
                    dashboardDAO.porFormaPagamento(seteDias, hoje),
                    dashboardDAO.maisVendidos(seteDias, hoje, 5),
                    dashboardDAO.estoqueBaixo()
                };
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void done() {
                btnAtualizar.setEnabled(true);
                try {
                    Object[] r = get();
                    mostrarIndicadores((DashboardDAO.Resumo) r[0], (DashboardDAO.Resumo) r[1], (DashboardDAO.Resumo) r[2],
                            (List<DashboardDAO.Linha>) r[5], (List<Estoque>) r[6]);
                    mostrarFaturamento((List<DashboardDAO.Linha>) r[3], (DashboardDAO.Resumo) r[2]);
                    mostrarPagamentos((Map<FormaPagamento, DashboardDAO.Linha>) r[4]);
                    mostrarMaisVendidos((List<DashboardDAO.Linha>) r[5]);
                    mostrarEstoque((List<Estoque>) r[6]);
                } catch (Exception e) {
                    logger.log(java.util.logging.Level.WARNING, "Falha ao carregar o dashboard", e);
                    javax.swing.JOptionPane.showMessageDialog(TelaVisaoGeral.this,
                            "Não foi possível carregar os dados do dashboard.\n" + e.getMessage());
                }
            }
        }.execute();
    }

    private static String capitalizar(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private void mostrarIndicadores(DashboardDAO.Resumo hoje, DashboardDAO.Resumo ontem, DashboardDAO.Resumo semana,
            List<DashboardDAO.Linha> maisVendidos, List<Estoque> estoqueBaixo) {
        cardFaturamento.setValor(Tema.reais(hoje.faturamento));
        if (ontem.faturamento > 0) {
            double variacao = (hoje.faturamento - ontem.faturamento) / ontem.faturamento * 100;
            cardFaturamento.setTendencia(variacao >= 0 ? CartaoIndicador.Tendencia.ALTA : CartaoIndicador.Tendencia.BAIXA);
            cardFaturamento.setDetalhe(String.format(BR, "%+.0f%% vs ontem (%s)", variacao, Tema.reais(ontem.faturamento)));
        } else {
            cardFaturamento.setTendencia(CartaoIndicador.Tendencia.NEUTRA);
            cardFaturamento.setDetalhe("Sem vendas ontem");
        }

        cardVendas.setValor(String.valueOf(hoje.vendas));
        cardVendas.setTendencia(CartaoIndicador.Tendencia.NEUTRA);
        cardVendas.setDetalhe("Ontem: " + ontem.vendas + " | 7 dias: " + semana.vendas);

        cardTicket.setValor(Tema.reais(hoje.ticketMedio()));
        cardTicket.setTendencia(CartaoIndicador.Tendencia.NEUTRA);
        cardTicket.setDetalhe("Média 7 dias: " + Tema.reais(semana.ticketMedio()));

        if (maisVendidos.isEmpty()) {
            cardMaisVendido.setValor("-");
            cardMaisVendido.setDetalhe("Sem vendas no período");
        } else {
            DashboardDAO.Linha top = maisVendidos.get(0);
            cardMaisVendido.setValor(top.nome);
            cardMaisVendido.setDetalhe(top.quantidade + " unidades | " + Tema.reais(top.valor));
        }

        cardEstoque.setValor(String.valueOf(estoqueBaixo.size()));
        if (estoqueBaixo.isEmpty()) {
            cardEstoque.setTendencia(CartaoIndicador.Tendencia.NEUTRA);
            cardEstoque.setDetalhe("Todos acima do mínimo");
        } else {
            cardEstoque.setTendencia(CartaoIndicador.Tendencia.AVISO);
            cardEstoque.setDetalhe(estoqueBaixo.size() == 1 ? "item para repor" : "itens para repor");
        }
    }

    private void mostrarFaturamento(List<DashboardDAO.Linha> dias, DashboardDAO.Resumo semana) {
        String[] rotulos = new String[dias.size()];
        double[] valores = new double[dias.size()];
        String[] dicas = new String[dias.size()];
        DateTimeFormatter curto = DateTimeFormatter.ofPattern("EEE dd", BR);
        DateTimeFormatter longo = DateTimeFormatter.ofPattern("EEEE, dd/MM", BR);
        for (int i = 0; i < dias.size(); i++) {
            DashboardDAO.Linha d = dias.get(i);
            LocalDate dia = LocalDate.parse(d.nome);
            rotulos[i] = dia.equals(LocalDate.now()) ? "Hoje" : dia.format(curto).replace(".", "");
            valores[i] = d.valor;
            dicas[i] = capitalizar(dia.format(longo)) + ": " + Tema.reais(d.valor)
                    + " em " + d.quantidade + (d.quantidade == 1 ? " venda" : " vendas");
        }
        graficoFaturamento.setSubtitulo("Últimos 7 dias | total " + Tema.reais(semana.faturamento)
                + " em " + semana.vendas + " vendas");
        graficoFaturamento.setDados(rotulos, valores, dicas);
    }

    private void mostrarPagamentos(Map<FormaPagamento, DashboardDAO.Linha> formas) {
        int n = formas.size();
        String[] rotulos = new String[n];
        double[] valores = new double[n];
        int[] slots = new int[n];
        String[] detalhes = new String[n];
        int i = 0, totalVendas = 0;
        for (Map.Entry<FormaPagamento, DashboardDAO.Linha> e : formas.entrySet()) {
            // A cor segue a forma de pagamento (ordem fixa do enum), não a posição no ranking.
            slots[i] = e.getKey().ordinal();
            rotulos[i] = e.getKey().toString();
            valores[i] = e.getValue().valor;
            detalhes[i] = e.getValue().quantidade + (e.getValue().quantidade == 1 ? " venda | " : " vendas | ")
                    + Tema.reais(e.getValue().valor);
            totalVendas += e.getValue().quantidade;
            i++;
        }
        graficoPagamentos.setCentro(String.valueOf(totalVendas), totalVendas == 1 ? "venda" : "vendas");
        graficoPagamentos.setDados(rotulos, valores, slots, detalhes);
    }

    private void mostrarMaisVendidos(List<DashboardDAO.Linha> produtos) {
        int n = produtos.size();
        String[] rotulos = new String[n];
        double[] valores = new double[n];
        String[] textos = new String[n];
        String[] dicas = new String[n];
        for (int i = 0; i < n; i++) {
            DashboardDAO.Linha p = produtos.get(i);
            rotulos[i] = p.nome;
            valores[i] = p.quantidade;
            textos[i] = p.quantidade + " un.";
            dicas[i] = p.nome + ": " + p.quantidade + " unidades | " + Tema.reais(p.valor);
        }
        graficoProdutos.setDados(rotulos, valores, textos, dicas);
    }

    private void mostrarEstoque(List<Estoque> itens) {
        String[] nomes = new String[itens.size()];
        int[] qtd = new int[itens.size()];
        int[] min = new int[itens.size()];
        for (int i = 0; i < itens.size(); i++) {
            nomes[i] = itens.get(i).getNome();
            qtd[i] = itens.get(i).getQuantidade();
            min[i] = itens.get(i).getMinimo();
        }
        painelAlertas.setDados(nomes, qtd, min);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        painelCabecalho = new javax.swing.JPanel();
        painelTitulo = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        lblData = new javax.swing.JLabel();
        painelAcoesTopo = new javax.swing.JPanel();
        btnAtualizar = new javax.swing.JButton();
        painelCorpo = new javax.swing.JPanel();
        painelIndicadores = new javax.swing.JPanel();
        cardFaturamento = new ui.CartaoIndicador();
        cardVendas = new ui.CartaoIndicador();
        cardTicket = new ui.CartaoIndicador();
        cardMaisVendido = new ui.CartaoIndicador();
        cardEstoque = new ui.CartaoIndicador();
        painelGraficos = new javax.swing.JPanel();
        graficoFaturamento = new ui.GraficoColunas();
        graficoPagamentos = new ui.GraficoRosca();
        graficoProdutos = new ui.GraficoBarras();
        painelAlertas = new ui.PainelAlertas();

        setLayout(new java.awt.BorderLayout());

        painelCabecalho.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 24, 14, 24));
        painelCabecalho.setLayout(new java.awt.BorderLayout());
        painelTitulo.setLayout(new java.awt.GridLayout(2, 1, 0, 2));
        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblTitulo.setText("Visão geral");
        painelTitulo.add(lblTitulo);

        lblData.setText("hoje");
        painelTitulo.add(lblData);

        painelCabecalho.add(painelTitulo, java.awt.BorderLayout.LINE_START);

        painelAcoesTopo.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 8));
        btnAtualizar.setText("Atualizar");
        btnAtualizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAtualizarActionPerformed(evt);
            }
        });
        painelAcoesTopo.add(btnAtualizar);

        painelCabecalho.add(painelAcoesTopo, java.awt.BorderLayout.LINE_END);

        add(painelCabecalho, java.awt.BorderLayout.PAGE_START);

        painelCorpo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 24, 24, 24));
        painelCorpo.setLayout(new java.awt.BorderLayout(20, 16));
        painelIndicadores.setLayout(new java.awt.GridLayout(1, 5, 14, 0));
        cardFaturamento.setTitulo("Faturamento hoje");
        cardFaturamento.setPrincipal(true);
        painelIndicadores.add(cardFaturamento);

        cardVendas.setTitulo("Vendas hoje");
        painelIndicadores.add(cardVendas);

        cardTicket.setTitulo("Ticket médio hoje");
        painelIndicadores.add(cardTicket);

        cardMaisVendido.setTitulo("Mais vendido (7 dias)");
        painelIndicadores.add(cardMaisVendido);

        cardEstoque.setTitulo("Estoque baixo");
        painelIndicadores.add(cardEstoque);

        painelCorpo.add(painelIndicadores, java.awt.BorderLayout.PAGE_START);

        painelGraficos.setLayout(new java.awt.GridLayout(2, 2, 16, 16));
        graficoFaturamento.setTitulo("Faturamento por dia");
        graficoFaturamento.setSubtitulo("Últimos 7 dias");
        painelGraficos.add(graficoFaturamento);

        graficoPagamentos.setTitulo("Formas de pagamento");
        graficoPagamentos.setSubtitulo("Últimos 7 dias");
        painelGraficos.add(graficoPagamentos);

        graficoProdutos.setTitulo("Mais vendidos");
        graficoProdutos.setSubtitulo("Últimos 7 dias, por quantidade");
        painelGraficos.add(graficoProdutos);

        painelAlertas.setTitulo("Estoque baixo");
        painelAlertas.setSubtitulo("Itens abaixo da quantidade mínima");
        painelGraficos.add(painelAlertas);

        painelCorpo.add(painelGraficos, java.awt.BorderLayout.CENTER);

        add(painelCorpo, java.awt.BorderLayout.CENTER);

    }// </editor-fold>//GEN-END:initComponents

    private void btnAtualizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAtualizarActionPerformed
        carregarDados();
    }//GEN-LAST:event_btnAtualizarActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAtualizar;
    private ui.CartaoIndicador cardEstoque;
    private ui.CartaoIndicador cardFaturamento;
    private ui.CartaoIndicador cardMaisVendido;
    private ui.CartaoIndicador cardTicket;
    private ui.CartaoIndicador cardVendas;
    private ui.GraficoColunas graficoFaturamento;
    private ui.GraficoRosca graficoPagamentos;
    private ui.GraficoBarras graficoProdutos;
    private javax.swing.JLabel lblData;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel painelAcoesTopo;
    private ui.PainelAlertas painelAlertas;
    private javax.swing.JPanel painelCabecalho;
    private javax.swing.JPanel painelCorpo;
    private javax.swing.JPanel painelGraficos;
    private javax.swing.JPanel painelIndicadores;
    private javax.swing.JPanel painelTitulo;
    // End of variables declaration//GEN-END:variables
}
