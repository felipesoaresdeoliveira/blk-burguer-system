package telas;

import dao.DashboardDAO;
import entidades.Estoque;
import entidades.FormaPagamento;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.JOptionPane;
import ui.CartaoIndicador;
import ui.Tema;

/** Relatórios por período (#25): indicadores, comparação com o período anterior e gráficos. */
public class TelaRelatorio extends javax.swing.JFrame {

    private static final Locale BR = new Locale("pt", "BR");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private final DashboardDAO dashboardDAO = new DashboardDAO();
    private boolean ajustandoDatas;

    public TelaRelatorio() {
        initComponents();
        aplicarVisual();
        cmbPeriodo.setSelectedItem("Últimos 7 dias");
        preencherDatas();
        cmbPeriodo.addActionListener(e -> {
            if (!"Personalizado".equals(cmbPeriodo.getSelectedItem())) {
                preencherDatas();
                carregar();
            }
        });
        javax.swing.event.DocumentListener paraPersonalizado = new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { marcarPersonalizado(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { marcarPersonalizado(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { marcarPersonalizado(); }
        };
        txtDe.getDocument().addDocumentListener(paraPersonalizado);
        txtAte.getDocument().addDocumentListener(paraPersonalizado);
        getRootPane().setDefaultButton(btnAplicar);
        carregar();
        Tema.tamanhoPadrao(this);
    }

    private void aplicarVisual() {
        Tema.janela(this);
        Tema.titulo(txtTitle);
        Tema.secundario(lblSubtitulo);
        Tema.primario(btnAplicar);
        Tema.transparente(painelCabecalho, painelTitulo, painelAcoesTopo, painelCorpo, painelFiltro, painelConteudoRel,
                painelIndicadores, painelGraficos, painelTabela);
        lblTabela.setForeground(Tema.TEXTO);
        txtDe.putClientProperty("JTextField.placeholderText", "dd/mm/aaaa");
        txtAte.putClientProperty("JTextField.placeholderText", "dd/mm/aaaa");
    }

    /** Digitar uma data troca o período para "Personalizado". */
    private void marcarPersonalizado() {
        if (!ajustandoDatas && !"Personalizado".equals(cmbPeriodo.getSelectedItem())) {
            javax.swing.SwingUtilities.invokeLater(() -> {
                ajustandoDatas = true;
                cmbPeriodo.setSelectedItem("Personalizado");
                ajustandoDatas = false;
            });
        }
    }

    /** Preenche "de" e "até" conforme o período escolhido. */
    private void preencherDatas() {
        LocalDate hoje = LocalDate.now();
        LocalDate de = hoje, ate = hoje;
        switch (String.valueOf(cmbPeriodo.getSelectedItem())) {
            case "Ontem": de = ate = hoje.minusDays(1); break;
            case "Últimos 7 dias": de = hoje.minusDays(6); break;
            case "Últimos 30 dias": de = hoje.minusDays(29); break;
            case "Este mês": de = hoje.withDayOfMonth(1); break;
            case "Mês passado":
                de = hoje.minusMonths(1).withDayOfMonth(1);
                ate = de.withDayOfMonth(de.lengthOfMonth());
                break;
            default: break;
        }
        ajustandoDatas = true;
        txtDe.setText(de.format(DATA));
        txtAte.setText(ate.format(DATA));
        ajustandoDatas = false;
    }

    private LocalDate ler(javax.swing.JTextField campo) {
        try {
            return LocalDate.parse(campo.getText().trim(), DATA);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private void carregar() {
        LocalDate de = ler(txtDe), ate = ler(txtAte);
        if (de == null || ate == null) {
            JOptionPane.showMessageDialog(this, "Informe as datas no formato dd/mm/aaaa.", "Relatórios", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (de.isAfter(ate)) {
            JOptionPane.showMessageDialog(this, "A data inicial é depois da data final.", "Relatórios", JOptionPane.WARNING_MESSAGE);
            return;
        }
        long dias = ChronoUnit.DAYS.between(de, ate) + 1;
        if (dias > 366) {
            JOptionPane.showMessageDialog(this, "Escolha um período de até 1 ano.", "Relatórios", JOptionPane.WARNING_MESSAGE);
            return;
        }
        LocalDate antDe = de.minusDays(dias), antAte = de.minusDays(1);
        btnAplicar.setEnabled(false);
        new javax.swing.SwingWorker<Object[], Void>() {
            @Override
            protected Object[] doInBackground() throws Exception {
                return new Object[]{
                    dashboardDAO.resumo(de, ate),
                    dashboardDAO.resumo(antDe, antAte),
                    dashboardDAO.faturamentoPorDia(de, ate),
                    dashboardDAO.porFormaPagamento(de, ate),
                    dashboardDAO.maisVendidos(de, ate, 500),
                    dashboardDAO.estoqueBaixo()
                };
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void done() {
                btnAplicar.setEnabled(true);
                try {
                    Object[] r = get();
                    String rotuloPeriodo = de.equals(ate) ? de.format(DATA) : de.format(DATA) + " a " + ate.format(DATA);
                    mostrarIndicadores((DashboardDAO.Resumo) r[0], (DashboardDAO.Resumo) r[1], (List<Estoque>) r[5]);
                    mostrarFaturamento((List<DashboardDAO.Linha>) r[2], rotuloPeriodo, dias);
                    mostrarPagamentos((Map<FormaPagamento, DashboardDAO.Linha>) r[3], rotuloPeriodo);
                    mostrarProdutos((List<DashboardDAO.Linha>) r[4], ((DashboardDAO.Resumo) r[0]).faturamento);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(TelaRelatorio.this, "Erro ao carregar o relatório: " + e.getMessage());
                }
            }
        }.execute();
    }

    /** Variação em relação ao período anterior, com seta e cor (não só cor). */
    private static void comparar(CartaoIndicador card, double atual, double anterior, String formato) {
        if (anterior <= 0) {
            card.setTendencia(CartaoIndicador.Tendencia.NEUTRA);
            card.setDetalhe(atual > 0 ? "Sem dados no período anterior" : "");
            return;
        }
        double variacao = (atual - anterior) / anterior * 100;
        card.setTendencia(variacao >= 0 ? CartaoIndicador.Tendencia.ALTA : CartaoIndicador.Tendencia.BAIXA);
        card.setDetalhe(String.format(BR, "%+.0f%% vs período anterior (" + formato + ")", variacao, anterior));
    }

    private void mostrarIndicadores(DashboardDAO.Resumo atual, DashboardDAO.Resumo anterior, List<Estoque> baixo) {
        cardFaturamento.setValor(Tema.reais(atual.faturamento));
        comparar(cardFaturamento, atual.faturamento, anterior.faturamento, "R$ %,.2f");
        cardVendas.setValor(String.valueOf(atual.vendas));
        comparar(cardVendas, atual.vendas, anterior.vendas, "%.0f");
        cardTicket.setValor(Tema.reais(atual.ticketMedio()));
        comparar(cardTicket, atual.ticketMedio(), anterior.ticketMedio(), "R$ %,.2f");
        cardItens.setValor(String.valueOf(atual.itens));
        comparar(cardItens, atual.itens, anterior.itens, "%.0f");
        cardEstoque.setValor(String.valueOf(baixo.size()));
        cardEstoque.setTendencia(baixo.isEmpty() ? CartaoIndicador.Tendencia.NEUTRA : CartaoIndicador.Tendencia.AVISO);
        cardEstoque.setDetalhe(baixo.isEmpty() ? "Todos acima do mínimo" : (baixo.size() == 1 ? "item para repor" : "itens para repor"));
    }

    private void mostrarFaturamento(List<DashboardDAO.Linha> dias, String periodo, long qtdDias) {
        String[] rotulos = new String[dias.size()];
        double[] valores = new double[dias.size()];
        String[] dicas = new String[dias.size()];
        DateTimeFormatter curto = DateTimeFormatter.ofPattern(qtdDias <= 7 ? "EEE dd" : "dd/MM", BR);
        DateTimeFormatter longo = DateTimeFormatter.ofPattern("EEEE, dd/MM", BR);
        double total = 0;
        for (int i = 0; i < dias.size(); i++) {
            DashboardDAO.Linha d = dias.get(i);
            LocalDate dia = LocalDate.parse(d.nome);
            rotulos[i] = dia.format(curto).replace(".", "");
            valores[i] = d.valor;
            total += d.valor;
            String nomeDia = dia.format(longo);
            dicas[i] = Character.toUpperCase(nomeDia.charAt(0)) + nomeDia.substring(1) + ": " + Tema.reais(d.valor)
                    + " em " + d.quantidade + (d.quantidade == 1 ? " venda" : " vendas");
        }
        graficoFaturamento.setSubtitulo(periodo + " | total " + Tema.reais(total));
        graficoFaturamento.setDados(rotulos, valores, dicas);
    }

    private void mostrarPagamentos(Map<FormaPagamento, DashboardDAO.Linha> formas, String periodo) {
        int n = formas.size(), i = 0, vendas = 0;
        String[] rotulos = new String[n];
        double[] valores = new double[n];
        int[] slots = new int[n];
        String[] detalhes = new String[n];
        for (Map.Entry<FormaPagamento, DashboardDAO.Linha> e : formas.entrySet()) {
            slots[i] = e.getKey().ordinal();
            rotulos[i] = e.getKey().toString();
            valores[i] = e.getValue().valor;
            detalhes[i] = e.getValue().quantidade + (e.getValue().quantidade == 1 ? " venda | " : " vendas | ") + Tema.reais(e.getValue().valor);
            vendas += e.getValue().quantidade;
            i++;
        }
        graficoPagamentos.setSubtitulo(periodo);
        graficoPagamentos.setCentro(String.valueOf(vendas), vendas == 1 ? "venda" : "vendas");
        graficoPagamentos.setDados(rotulos, valores, slots, detalhes);
    }

    private void mostrarProdutos(List<DashboardDAO.Linha> produtos, double faturamento) {
        int top = Math.min(8, produtos.size());
        String[] rotulos = new String[top];
        double[] valores = new double[top];
        String[] textos = new String[top];
        String[] dicas = new String[top];
        for (int i = 0; i < top; i++) {
            DashboardDAO.Linha p = produtos.get(i);
            rotulos[i] = p.nome;
            valores[i] = p.quantidade;
            textos[i] = p.quantidade + " un.";
            dicas[i] = p.nome + ": " + p.quantidade + " unidades | " + Tema.reais(p.valor);
        }
        graficoProdutos.setDados(rotulos, valores, textos, dicas);

        javax.swing.table.DefaultTableModel modelo = new javax.swing.table.DefaultTableModel(
                new String[]{"Produto", "Qtd", "Faturamento", "% do total"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        for (DashboardDAO.Linha p : produtos) {
            modelo.addRow(new Object[]{p.nome, p.quantidade, Tema.reais(p.valor),
                faturamento > 0 ? String.format(BR, "%.1f%%", p.valor / faturamento * 100) : "-"});
        }
        tabelaProdutos.setModel(modelo);
        tabelaProdutos.getColumnModel().getColumn(0).setPreferredWidth(200);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        painelCabecalho = new javax.swing.JPanel();
        painelTitulo = new javax.swing.JPanel();
        txtTitle = new javax.swing.JLabel();
        lblSubtitulo = new javax.swing.JLabel();
        painelAcoesTopo = new javax.swing.JPanel();
        btnVoltar = new javax.swing.JButton();
        painelCorpo = new javax.swing.JPanel();
        painelFiltro = new javax.swing.JPanel();
        lblPeriodo = new javax.swing.JLabel();
        cmbPeriodo = new javax.swing.JComboBox<>();
        lblDe = new javax.swing.JLabel();
        txtDe = new javax.swing.JTextField();
        lblAte = new javax.swing.JLabel();
        txtAte = new javax.swing.JTextField();
        btnAplicar = new javax.swing.JButton();
        painelConteudoRel = new javax.swing.JPanel();
        painelIndicadores = new javax.swing.JPanel();
        cardFaturamento = new ui.CartaoIndicador();
        cardVendas = new ui.CartaoIndicador();
        cardTicket = new ui.CartaoIndicador();
        cardItens = new ui.CartaoIndicador();
        cardEstoque = new ui.CartaoIndicador();
        painelGraficos = new javax.swing.JPanel();
        graficoFaturamento = new ui.GraficoColunas();
        graficoPagamentos = new ui.GraficoRosca();
        graficoProdutos = new ui.GraficoBarras();
        painelTabela = new javax.swing.JPanel();
        lblTabela = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabelaProdutos = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("BLK Burguer - Relatórios");
        setMinimumSize(new java.awt.Dimension(1180, 720));

        painelCabecalho.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 24, 14, 24));
        painelCabecalho.setLayout(new java.awt.BorderLayout());
        painelTitulo.setLayout(new java.awt.GridLayout(2, 1, 0, 2));
        txtTitle.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        txtTitle.setText("Relatórios");
        painelTitulo.add(txtTitle);

        lblSubtitulo.setText("Indicadores do período escolhido, comparados ao período anterior");
        painelTitulo.add(lblSubtitulo);

        painelCabecalho.add(painelTitulo, java.awt.BorderLayout.LINE_START);

        painelAcoesTopo.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 8));
        btnVoltar.setText("Voltar");
        btnVoltar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVoltarActionPerformed(evt);
            }
        });
        painelAcoesTopo.add(btnVoltar);

        painelCabecalho.add(painelAcoesTopo, java.awt.BorderLayout.LINE_END);

        getContentPane().add(painelCabecalho, java.awt.BorderLayout.PAGE_START);

        painelCorpo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 24, 24, 24));
        painelCorpo.setLayout(new java.awt.BorderLayout(20, 16));
        painelFiltro.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        lblPeriodo.setText("Período:");
        painelFiltro.add(lblPeriodo);

        cmbPeriodo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Hoje", "Ontem", "Últimos 7 dias", "Últimos 30 dias", "Este mês", "Mês passado", "Personalizado" }));
        painelFiltro.add(cmbPeriodo);

        lblDe.setText("De:");
        painelFiltro.add(lblDe);

        txtDe.setPreferredSize(new java.awt.Dimension(110, 30));
        painelFiltro.add(txtDe);

        lblAte.setText("Até:");
        painelFiltro.add(lblAte);

        txtAte.setPreferredSize(new java.awt.Dimension(110, 30));
        painelFiltro.add(txtAte);

        btnAplicar.setText("Aplicar");
        btnAplicar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAplicarActionPerformed(evt);
            }
        });
        painelFiltro.add(btnAplicar);

        painelCorpo.add(painelFiltro, java.awt.BorderLayout.PAGE_START);

        painelConteudoRel.setLayout(new java.awt.BorderLayout(0, 16));
        painelIndicadores.setLayout(new java.awt.GridLayout(1, 5, 14, 0));
        cardFaturamento.setTitulo("Faturamento");
        cardFaturamento.setPrincipal(true);
        painelIndicadores.add(cardFaturamento);

        cardVendas.setTitulo("Vendas");
        painelIndicadores.add(cardVendas);

        cardTicket.setTitulo("Ticket médio");
        painelIndicadores.add(cardTicket);

        cardItens.setTitulo("Itens vendidos");
        painelIndicadores.add(cardItens);

        cardEstoque.setTitulo("Estoque baixo agora");
        painelIndicadores.add(cardEstoque);

        painelConteudoRel.add(painelIndicadores, java.awt.BorderLayout.PAGE_START);

        painelGraficos.setLayout(new java.awt.GridLayout(2, 2, 16, 16));
        graficoFaturamento.setTitulo("Faturamento por dia");
        graficoFaturamento.setSubtitulo("Período");
        painelGraficos.add(graficoFaturamento);

        graficoPagamentos.setTitulo("Formas de pagamento");
        graficoPagamentos.setSubtitulo("Período");
        painelGraficos.add(graficoPagamentos);

        graficoProdutos.setTitulo("Mais vendidos");
        graficoProdutos.setSubtitulo("Top 8 por quantidade");
        painelGraficos.add(graficoProdutos);

        painelTabela.setLayout(new java.awt.BorderLayout(0, 8));
        lblTabela.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        lblTabela.setText("Vendas por produto");
        painelTabela.add(lblTabela, java.awt.BorderLayout.PAGE_START);

        jScrollPane1.setViewportView(tabelaProdutos);
        painelTabela.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        painelGraficos.add(painelTabela);

        painelConteudoRel.add(painelGraficos, java.awt.BorderLayout.CENTER);

        painelCorpo.add(painelConteudoRel, java.awt.BorderLayout.CENTER);

        getContentPane().add(painelCorpo, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnAplicarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAplicarActionPerformed
        carregar();
    }//GEN-LAST:event_btnAplicarActionPerformed

    private void btnVoltarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVoltarActionPerformed
        Main.voltar(this);
    }//GEN-LAST:event_btnVoltarActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAplicar;
    private javax.swing.JButton btnVoltar;
    private ui.CartaoIndicador cardEstoque;
    private ui.CartaoIndicador cardFaturamento;
    private ui.CartaoIndicador cardItens;
    private ui.CartaoIndicador cardTicket;
    private ui.CartaoIndicador cardVendas;
    private javax.swing.JComboBox<String> cmbPeriodo;
    private ui.GraficoColunas graficoFaturamento;
    private ui.GraficoRosca graficoPagamentos;
    private ui.GraficoBarras graficoProdutos;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblAte;
    private javax.swing.JLabel lblDe;
    private javax.swing.JLabel lblPeriodo;
    private javax.swing.JLabel lblSubtitulo;
    private javax.swing.JLabel lblTabela;
    private javax.swing.JPanel painelAcoesTopo;
    private javax.swing.JPanel painelCabecalho;
    private javax.swing.JPanel painelConteudoRel;
    private javax.swing.JPanel painelCorpo;
    private javax.swing.JPanel painelFiltro;
    private javax.swing.JPanel painelGraficos;
    private javax.swing.JPanel painelIndicadores;
    private javax.swing.JPanel painelTabela;
    private javax.swing.JPanel painelTitulo;
    private javax.swing.JTable tabelaProdutos;
    private javax.swing.JTextField txtAte;
    private javax.swing.JTextField txtDe;
    private javax.swing.JLabel txtTitle;
    // End of variables declaration//GEN-END:variables
}
