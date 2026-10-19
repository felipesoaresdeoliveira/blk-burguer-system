package telas;

import dao.DashboardDAO;
import entidades.Caixa;
import impressao.Documentos;
import impressao.Impressao;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import service.FechamentoService;
import ui.Acabamento;
import ui.CartaoIndicador;
import ui.Tema;

/**
 * Fechamento do dia (#41): faturamento, formas de pagamento, ticket médio,
 * mais vendido, cancelamentos, descontos, perdas, despesas e a situação dos
 * caixas, com impressão do resumo.
 */
public class TelaFechamento extends javax.swing.JPanel implements Navegavel {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DIA_EXTENSO = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", new Locale("pt", "BR"));

    private final FechamentoService fechamentoService = new FechamentoService();
    private FechamentoService.Fechamento atual;

    public TelaFechamento() {
        initComponents();
        Tema.titulo(lblTitulo);
        Tema.secundario(lblSubtitulo);
        Tema.primario(btnImprimir);
        Tema.transparente(painelCabecalho, painelTitulo, painelAcoesTopo, painelCorpo, painelFiltro, painelConteudoFech,
                painelIndicadores, painelDetalhes);
        for (javax.swing.JPanel p : new javax.swing.JPanel[]{painelFormas, painelOcorrencias, painelCaixas}) {
            Tema.cartao(p);
        }
        for (javax.swing.JLabel l : new javax.swing.JLabel[]{lblFormas, lblOcorrencias, lblCaixas}) {
            l.setForeground(Tema.TEXTO);
        }
        Acabamento.vazio(tabelaCaixas, "Nenhum caixa aberto neste dia");
        txtDia.putClientProperty("JTextField.placeholderText", "dd/mm/aaaa");
        txtDia.setText(LocalDate.now().format(DATA));
        lblSituacaoCaixa.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 12));
        putClientProperty("BLK.botaoPadrao", btnAtualizar);
        carregar();
    }

    private LocalDate dia() {
        try {
            return LocalDate.parse(txtDia.getText().trim(), DATA);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private void irPara(LocalDate dia) {
        txtDia.setText(dia.format(DATA));
        carregar();
    }

    private void carregar() {
        LocalDate dia = dia();
        if (dia == null) {
            JOptionPane.showMessageDialog(this, "Informe o dia no formato dd/mm/aaaa.", "Fechamento", JOptionPane.WARNING_MESSAGE);
            return;
        }
        btnAtualizar.setEnabled(false);
        new javax.swing.SwingWorker<FechamentoService.Fechamento, Void>() {
            @Override
            protected FechamentoService.Fechamento doInBackground() throws Exception {
                return fechamentoService.gerar(dia);
            }

            @Override
            protected void done() {
                btnAtualizar.setEnabled(true);
                try {
                    atual = get();
                    mostrar(atual);
                } catch (Exception e) {
                    Throwable causa = e.getCause() != null ? e.getCause() : e;
                    JOptionPane.showMessageDialog(TelaFechamento.this, "Erro ao gerar o fechamento: " + causa.getMessage());
                }
            }
        }.execute();
    }

    private static DefaultTableModel modelo(String... colunas) {
        return new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
    }

    private void mostrar(FechamentoService.Fechamento f) {
        String extenso = f.dia.format(DIA_EXTENSO);
        lblSubtitulo.setText(Character.toUpperCase(extenso.charAt(0)) + extenso.substring(1)
                + (f.dia.equals(LocalDate.now()) ? " (hoje, ainda em andamento)" : ""));

        cardFaturamento.setValor(Tema.reais(f.resumo.faturamento));
        cardFaturamento.setDetalhe(f.descontos > 0 ? "Descontos: " + Tema.reais(f.descontos) : "Sem descontos");
        cardPedidos.setValor(String.valueOf(f.resumo.vendas));
        cardPedidos.setDetalhe(f.resumo.itens + " itens vendidos");
        cardTicket.setValor(Tema.reais(f.resumo.ticketMedio()));
        cardTicket.setDetalhe("por pedido");
        cardMaisVendido.setValor(f.maisVendido == null ? "-" : f.maisVendido.nome);
        cardMaisVendido.setDetalhe(f.maisVendido == null ? "Sem vendas" : f.maisVendido.quantidade + " unidades");
        cardResultado.setValor(Tema.reais(f.getResultado()));
        cardResultado.setDetalhe("Despesas e perdas: " + Tema.reais(f.despesas + f.valorPerdas));
        cardResultado.setTendencia(f.getResultado() >= 0 ? CartaoIndicador.Tendencia.NEUTRA : CartaoIndicador.Tendencia.AVISO);

        DefaultTableModel formas = modelo("Forma", "Pedidos", "Valor");
        for (DashboardDAO.Linha l : f.formas.values()) {
            formas.addRow(new Object[]{l.nome, l.quantidade, Tema.reais(l.valor)});
        }
        formas.addRow(new Object[]{"Total", "", Tema.reais(f.getTotalRecebido())});
        tabelaFormas.setModel(formas);

        DefaultTableModel ocorrencias = modelo("Ocorrência", "Qtd", "Valor");
        ocorrencias.addRow(new Object[]{"Cancelamentos", f.cancelamentos, Tema.reais(f.valorCancelado)});
        ocorrencias.addRow(new Object[]{"Descontos", "", Tema.reais(f.descontos)});
        ocorrencias.addRow(new Object[]{"Perdas de estoque", f.perdas, Tema.reais(f.valorPerdas)});
        ocorrencias.addRow(new Object[]{"Despesas", "", Tema.reais(f.despesas)});
        tabelaOcorrencias.setModel(ocorrencias);
        tabelaOcorrencias.getColumnModel().getColumn(0).setPreferredWidth(150);

        DefaultTableModel caixas = modelo("Caixa", "Vendido", "Status");
        for (Caixa c : f.caixas) {
            // Aberto em outro dia: mostra a data; no próprio dia, só a hora.
            String inicio = c.getAbertoEm().toLocalDate().equals(f.dia) ? c.getAbertoEm().format(HORA)
                    : c.getAbertoEm().format(DateTimeFormatter.ofPattern("dd/MM"));
            String status;
            if (c.isAberto()) {
                status = "Aberto";
            } else if (Math.abs(c.getDiferencaTotal()) < 0.005) {
                status = "OK";
            } else {
                status = "Divergente";
            }
            caixas.addRow(new Object[]{"Nº " + c.getId() + " (" + inicio + ")", Tema.reais(c.getTotalVendido()), status});
        }
        tabelaCaixas.setModel(caixas);
        tabelaCaixas.getColumnModel().getColumn(0).setPreferredWidth(110);
        tabelaCaixas.getColumnModel().getColumn(2).setPreferredWidth(110);

        if (f.caixas.isEmpty()) {
            lblSituacaoCaixa.setText("   Nenhum caixa neste dia");
            lblSituacaoCaixa.setForeground(Tema.TEXTO_SECUNDARIO);
        } else if (f.temCaixaAberto()) {
            lblSituacaoCaixa.setText("   ● Caixa ainda aberto");
            lblSituacaoCaixa.setForeground(Tema.AVISO);
        } else if (Math.abs(f.getDivergencia()) >= 0.005) {
            lblSituacaoCaixa.setText("   ● Divergência de " + Tema.reais(f.getDivergencia()));
            lblSituacaoCaixa.setForeground(Tema.PERIGO);
        } else {
            lblSituacaoCaixa.setText("   ● Caixa fechado e conferido");
            lblSituacaoCaixa.setForeground(Tema.SUCESSO);
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        painelCabecalho = new javax.swing.JPanel();
        painelTitulo = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        lblSubtitulo = new javax.swing.JLabel();
        painelAcoesTopo = new javax.swing.JPanel();
        btnImprimir = new javax.swing.JButton();
        btnVoltar = new javax.swing.JButton();
        painelCorpo = new javax.swing.JPanel();
        painelFiltro = new javax.swing.JPanel();
        lblDia = new javax.swing.JLabel();
        txtDia = new javax.swing.JTextField();
        btnDiaAnterior = new javax.swing.JButton();
        btnHoje = new javax.swing.JButton();
        btnAtualizar = new javax.swing.JButton();
        lblSituacaoCaixa = new javax.swing.JLabel();
        painelConteudoFech = new javax.swing.JPanel();
        painelIndicadores = new javax.swing.JPanel();
        cardFaturamento = new ui.CartaoIndicador();
        cardPedidos = new ui.CartaoIndicador();
        cardTicket = new ui.CartaoIndicador();
        cardMaisVendido = new ui.CartaoIndicador();
        cardResultado = new ui.CartaoIndicador();
        painelDetalhes = new javax.swing.JPanel();
        painelFormas = new javax.swing.JPanel();
        lblFormas = new javax.swing.JLabel();
        scrollTabelaFormas = new javax.swing.JScrollPane();
        tabelaFormas = new javax.swing.JTable();
        painelOcorrencias = new javax.swing.JPanel();
        lblOcorrencias = new javax.swing.JLabel();
        scrollTabelaOcorrencias = new javax.swing.JScrollPane();
        tabelaOcorrencias = new javax.swing.JTable();
        painelCaixas = new javax.swing.JPanel();
        lblCaixas = new javax.swing.JLabel();
        scrollTabelaCaixas = new javax.swing.JScrollPane();
        tabelaCaixas = new javax.swing.JTable();

        setLayout(new java.awt.BorderLayout());

        painelCabecalho.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 24, 14, 24));
        painelCabecalho.setLayout(new java.awt.BorderLayout());
        painelTitulo.setLayout(new java.awt.GridLayout(2, 1, 0, 2));
        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblTitulo.setText("Fechamento do dia");
        painelTitulo.add(lblTitulo);

        lblSubtitulo.setText("Resumo da operação para conferir no fim do expediente");
        painelTitulo.add(lblSubtitulo);

        painelCabecalho.add(painelTitulo, java.awt.BorderLayout.LINE_START);

        painelAcoesTopo.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 8));
        btnImprimir.setText("Imprimir resumo");
        btnImprimir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImprimirActionPerformed(evt);
            }
        });
        painelAcoesTopo.add(btnImprimir);

        btnVoltar.setText("Voltar");
        btnVoltar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVoltarActionPerformed(evt);
            }
        });
        painelAcoesTopo.add(btnVoltar);

        painelCabecalho.add(painelAcoesTopo, java.awt.BorderLayout.LINE_END);

        add(painelCabecalho, java.awt.BorderLayout.PAGE_START);

        painelCorpo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 24, 24, 24));
        painelCorpo.setLayout(new java.awt.BorderLayout(20, 16));
        painelFiltro.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        lblDia.setText("Dia:");
        painelFiltro.add(lblDia);

        txtDia.setPreferredSize(new java.awt.Dimension(110, 30));
        painelFiltro.add(txtDia);

        btnDiaAnterior.setText("Dia anterior");
        btnDiaAnterior.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDiaAnteriorActionPerformed(evt);
            }
        });
        painelFiltro.add(btnDiaAnterior);

        btnHoje.setText("Hoje");
        btnHoje.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHojeActionPerformed(evt);
            }
        });
        painelFiltro.add(btnHoje);

        btnAtualizar.setText("Atualizar");
        btnAtualizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAtualizarActionPerformed(evt);
            }
        });
        painelFiltro.add(btnAtualizar);

        lblSituacaoCaixa.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblSituacaoCaixa.setText(" ");
        painelFiltro.add(lblSituacaoCaixa);

        painelCorpo.add(painelFiltro, java.awt.BorderLayout.PAGE_START);

        painelConteudoFech.setLayout(new java.awt.BorderLayout(0, 16));
        painelIndicadores.setLayout(new java.awt.GridLayout(1, 5, 14, 0));
        cardFaturamento.setTitulo("Faturamento");
        cardFaturamento.setPrincipal(true);
        painelIndicadores.add(cardFaturamento);

        cardPedidos.setTitulo("Pedidos pagos");
        painelIndicadores.add(cardPedidos);

        cardTicket.setTitulo("Ticket médio");
        painelIndicadores.add(cardTicket);

        cardMaisVendido.setTitulo("Mais vendido");
        painelIndicadores.add(cardMaisVendido);

        cardResultado.setTitulo("Resultado do dia");
        painelIndicadores.add(cardResultado);

        painelConteudoFech.add(painelIndicadores, java.awt.BorderLayout.PAGE_START);

        painelDetalhes.setLayout(new java.awt.GridLayout(1, 3, 16, 0));
        painelFormas.setLayout(new java.awt.BorderLayout(0, 8));
        lblFormas.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        lblFormas.setText("Recebido por forma");
        painelFormas.add(lblFormas, java.awt.BorderLayout.PAGE_START);

        scrollTabelaFormas.setViewportView(tabelaFormas);
        painelFormas.add(scrollTabelaFormas, java.awt.BorderLayout.CENTER);

        painelDetalhes.add(painelFormas);

        painelOcorrencias.setLayout(new java.awt.BorderLayout(0, 8));
        lblOcorrencias.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        lblOcorrencias.setText("Ocorrências");
        painelOcorrencias.add(lblOcorrencias, java.awt.BorderLayout.PAGE_START);

        scrollTabelaOcorrencias.setViewportView(tabelaOcorrencias);
        painelOcorrencias.add(scrollTabelaOcorrencias, java.awt.BorderLayout.CENTER);

        painelDetalhes.add(painelOcorrencias);

        painelCaixas.setLayout(new java.awt.BorderLayout(0, 8));
        lblCaixas.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        lblCaixas.setText("Caixas do dia");
        painelCaixas.add(lblCaixas, java.awt.BorderLayout.PAGE_START);

        scrollTabelaCaixas.setViewportView(tabelaCaixas);
        painelCaixas.add(scrollTabelaCaixas, java.awt.BorderLayout.CENTER);

        painelDetalhes.add(painelCaixas);

        painelConteudoFech.add(painelDetalhes, java.awt.BorderLayout.CENTER);

        painelCorpo.add(painelConteudoFech, java.awt.BorderLayout.CENTER);

        add(painelCorpo, java.awt.BorderLayout.CENTER);

    }// </editor-fold>//GEN-END:initComponents

    private void btnImprimirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImprimirActionPerformed
        if (atual != null) {
            FechamentoService.Fechamento f = atual;
            Impressao.visualizar(this, "Fechamento do dia", colunas -> Documentos.fechamento(f, colunas));
        }
    }//GEN-LAST:event_btnImprimirActionPerformed

    private void btnVoltarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVoltarActionPerformed
        Main.voltar(this);
    }//GEN-LAST:event_btnVoltarActionPerformed

    private void btnDiaAnteriorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDiaAnteriorActionPerformed
        LocalDate d = dia();
        irPara((d == null ? LocalDate.now() : d).minusDays(1));
    }//GEN-LAST:event_btnDiaAnteriorActionPerformed

    private void btnHojeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHojeActionPerformed
        irPara(LocalDate.now());
    }//GEN-LAST:event_btnHojeActionPerformed

    private void btnAtualizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAtualizarActionPerformed
        carregar();
    }//GEN-LAST:event_btnAtualizarActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAtualizar;
    private javax.swing.JButton btnDiaAnterior;
    private javax.swing.JButton btnHoje;
    private javax.swing.JButton btnImprimir;
    private javax.swing.JButton btnVoltar;
    private ui.CartaoIndicador cardFaturamento;
    private ui.CartaoIndicador cardMaisVendido;
    private ui.CartaoIndicador cardPedidos;
    private ui.CartaoIndicador cardResultado;
    private ui.CartaoIndicador cardTicket;
    private javax.swing.JLabel lblCaixas;
    private javax.swing.JLabel lblDia;
    private javax.swing.JLabel lblFormas;
    private javax.swing.JLabel lblOcorrencias;
    private javax.swing.JLabel lblSituacaoCaixa;
    private javax.swing.JLabel lblSubtitulo;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel painelAcoesTopo;
    private javax.swing.JPanel painelCabecalho;
    private javax.swing.JPanel painelCaixas;
    private javax.swing.JPanel painelConteudoFech;
    private javax.swing.JPanel painelCorpo;
    private javax.swing.JPanel painelDetalhes;
    private javax.swing.JPanel painelFiltro;
    private javax.swing.JPanel painelFormas;
    private javax.swing.JPanel painelIndicadores;
    private javax.swing.JPanel painelOcorrencias;
    private javax.swing.JPanel painelTitulo;
    private javax.swing.JScrollPane scrollTabelaCaixas;
    private javax.swing.JScrollPane scrollTabelaFormas;
    private javax.swing.JScrollPane scrollTabelaOcorrencias;
    private javax.swing.JTable tabelaCaixas;
    private javax.swing.JTable tabelaFormas;
    private javax.swing.JTable tabelaOcorrencias;
    private javax.swing.JTextField txtDia;
    // End of variables declaration//GEN-END:variables
}
