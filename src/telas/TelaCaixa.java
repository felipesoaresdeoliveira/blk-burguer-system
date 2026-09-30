package telas;

import entidades.Caixa;
import entidades.FormaPagamento;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.Map;
import javax.swing.JOptionPane;
import service.CaixaService;
import ui.CartaoIndicador;
import ui.Tema;

/** Controle de caixa: abertura, sangria/suprimento, conferência e fechamento. */
public class TelaCaixa extends javax.swing.JFrame {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    private final CaixaService caixaService = new CaixaService();
    private Caixa caixa;

    public TelaCaixa() {
        initComponents();
        Tema.janela(this);
        Tema.titulo(lblTitulo);
        Tema.primario(btnAbrir, btnFechar);
        Tema.perigo(btnSangria);
        getContentPane().setBackground(Tema.FUNDO);
        for (javax.swing.JPanel p : new javax.swing.JPanel[]{painelCabecalho, painelTitulo, painelAcoes, painelCorpo,
            painelIndicadores, painelTabelas, painelMov, painelHist}) {
            p.setBackground(Tema.FUNDO);
        }
        lblMov.setForeground(Tema.TEXTO);
        lblHist.setForeground(Tema.TEXTO);
        carregar();
        setSize(1240, 720);
        setLocationRelativeTo(null);
    }

    private static Double lerValor(String texto) {
        try {
            String t = texto.trim().replace("R$", "").trim();
            // "1.234,56" ou "10,50" (padrão brasileiro) e também "10.50".
            if (t.contains(",")) {
                t = t.replace(".", "").replace(',', '.');
            }
            return t.isEmpty() ? null : Double.valueOf(t);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void carregar() {
        try {
            caixa = caixaService.aberto();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage());
            return;
        }
        boolean aberto = caixa != null;
        btnAbrir.setVisible(!aberto);
        btnSangria.setVisible(aberto);
        btnSuprimento.setVisible(aberto);
        btnFechar.setVisible(aberto);
        if (aberto) {
            lblStatus.setText("● Aberto desde " + caixa.getAbertoEm().format(DATA_HORA)
                    + (caixa.getAbertoPor() != null ? " por " + caixa.getAbertoPor() : "")
                    + "  |  fundo de troco " + Tema.reais(caixa.getValorInicial()));
            lblStatus.setForeground(Tema.SUCESSO);
            mostrarIndicadores(caixa);
        } else {
            lblStatus.setText("○ Caixa fechado. Abra o caixa para registrar vendas.");
            lblStatus.setForeground(Tema.AVISO);
            for (CartaoIndicador c : new CartaoIndicador[]{cardDinheiro, cardPix, cardDebito, cardCredito, cardTotal}) {
                c.setValor("-");
                c.setDetalhe("");
            }
        }
        mostrarMovimentacoes();
        mostrarHistorico();
    }

    private void mostrarIndicadores(Caixa c) {
        cardDinheiro.setValor(Tema.reais(c.getEsperado(FormaPagamento.DINHEIRO)));
        cardDinheiro.setDetalhe("Vendas " + Tema.reais(c.getRecebido().get(FormaPagamento.DINHEIRO))
                + " + fundo " + Tema.reais(c.getValorInicial()));
        cardPix.setValor(Tema.reais(c.getRecebido().get(FormaPagamento.PIX)));
        cardDebito.setValor(Tema.reais(c.getRecebido().get(FormaPagamento.DEBITO)));
        cardCredito.setValor(Tema.reais(c.getRecebido().get(FormaPagamento.CREDITO)));
        cardTotal.setValor(Tema.reais(c.getTotalVendido()));
        cardTotal.setDetalhe(c.getPagamentos() + (c.getPagamentos() == 1 ? " pagamento" : " pagamentos"));
        String mov = "";
        if (c.getSuprimentos() > 0 || c.getSangrias() > 0) {
            mov = "Suprimentos " + Tema.reais(c.getSuprimentos()) + " | sangrias " + Tema.reais(c.getSangrias());
        }
        cardPix.setDetalhe("");
        cardDebito.setDetalhe("");
        cardCredito.setDetalhe(mov);
    }

    private void mostrarMovimentacoes() {
        javax.swing.table.DefaultTableModel modelo = new javax.swing.table.DefaultTableModel(
                new String[]{"Hora", "Tipo", "Valor", "Motivo", "Por"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        if (caixa != null) {
            for (Caixa.Movimentacao m : caixa.getMovimentacoes()) {
                modelo.addRow(new Object[]{m.data.format(HORA), "SANGRIA".equals(m.tipo) ? "Sangria (-)" : "Suprimento (+)",
                    Tema.reais(m.valor), m.motivo, m.usuario == null ? "-" : m.usuario});
            }
        }
        tabelaMov.setModel(modelo);
    }

    private void mostrarHistorico() {
        javax.swing.table.DefaultTableModel modelo = new javax.swing.table.DefaultTableModel(
                new String[]{"Nº", "Abertura", "Fechamento", "Vendido"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        try {
            for (Object[] h : caixaService.historico()) {
                modelo.addRow(new Object[]{h[0], ((java.time.LocalDateTime) h[1]).format(DATA_HORA),
                    ((java.time.LocalDateTime) h[2]).format(DATA_HORA), Tema.reais((Double) h[3])});
            }
        } catch (Exception e) {
            // Histórico é informativo; a tela continua funcionando sem ele.
        }
        tabelaHist.setModel(modelo);
        tabelaHist.getColumnModel().getColumn(0).setPreferredWidth(40);
    }

    private void abrir() {
        String valor = JOptionPane.showInputDialog(this, "Fundo de troco (dinheiro na gaveta ao abrir):", "0,00");
        if (valor == null) {
            return;
        }
        Double v = lerValor(valor);
        if (v == null) {
            JOptionPane.showMessageDialog(this, "Valor inválido.", "Caixa", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            caixaService.abrir(v);
            carregar();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Caixa", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void movimentar(String tipo) {
        javax.swing.JTextField campoValor = new javax.swing.JTextField(10);
        javax.swing.JTextField campoMotivo = new javax.swing.JTextField(22);
        campoMotivo.putClientProperty("JTextField.placeholderText",
                CaixaService.SANGRIA.equals(tipo) ? "ex.: depósito no banco, pagamento de fornecedor" : "ex.: reforço de troco");
        javax.swing.JPanel p = new javax.swing.JPanel(new java.awt.GridLayout(0, 1, 0, 6));
        p.add(new javax.swing.JLabel("Valor:"));
        p.add(campoValor);
        p.add(new javax.swing.JLabel("Motivo:"));
        p.add(campoMotivo);
        String titulo = CaixaService.SANGRIA.equals(tipo) ? "Sangria (retirada de dinheiro)" : "Suprimento (entrada de dinheiro)";
        if (JOptionPane.showConfirmDialog(this, p, titulo, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
                != JOptionPane.OK_OPTION) {
            return;
        }
        Double v = lerValor(campoValor.getText());
        if (v == null) {
            JOptionPane.showMessageDialog(this, "Valor inválido.", "Caixa", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            caixaService.movimentar(tipo, v, campoMotivo.getText());
            carregar();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Caixa", JOptionPane.WARNING_MESSAGE);
        }
    }

    /** Conferência: esperado x contado por forma de pagamento, com a diferença em tempo real. */
    private void fechar() {
        carregar();
        if (caixa == null) {
            return;
        }
        Map<FormaPagamento, javax.swing.JTextField> campos = new EnumMap<>(FormaPagamento.class);
        Map<FormaPagamento, javax.swing.JLabel> diferencas = new EnumMap<>(FormaPagamento.class);
        javax.swing.JPanel grade = new javax.swing.JPanel(new java.awt.GridLayout(0, 4, 12, 8));
        for (String h : new String[]{"Forma", "Esperado", "Contado", "Diferença"}) {
            javax.swing.JLabel l = new javax.swing.JLabel(h);
            l.setFont(l.getFont().deriveFont(java.awt.Font.BOLD));
            l.setForeground(Tema.TEXTO_SECUNDARIO);
            grade.add(l);
        }
        for (FormaPagamento f : FormaPagamento.values()) {
            double esperado = caixa.getEsperado(f);
            javax.swing.JTextField campo = new javax.swing.JTextField(8);
            javax.swing.JLabel dif = new javax.swing.JLabel("-");
            campos.put(f, campo);
            diferencas.put(f, dif);
            Runnable atualizar = () -> {
                Double contado = lerValor(campo.getText());
                if (contado == null) {
                    dif.setText("-");
                    dif.setForeground(Tema.TEXTO_FRACO);
                    return;
                }
                double d = Math.round((contado - esperado) * 100) / 100.0;
                dif.setText(d == 0 ? "✓ confere" : (d > 0 ? "▲ sobra " : "▼ falta ") + Tema.reais(Math.abs(d)));
                dif.setForeground(d == 0 ? Tema.SUCESSO : d > 0 ? Tema.AVISO : Tema.PERIGO);
            };
            campo.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                public void insertUpdate(javax.swing.event.DocumentEvent e) { atualizar.run(); }
                public void removeUpdate(javax.swing.event.DocumentEvent e) { atualizar.run(); }
                public void changedUpdate(javax.swing.event.DocumentEvent e) { atualizar.run(); }
            });
            grade.add(new javax.swing.JLabel(f.toString()));
            grade.add(new javax.swing.JLabel(Tema.reais(esperado)));
            grade.add(campo);
            grade.add(dif);
        }
        javax.swing.JTextField obs = new javax.swing.JTextField(30);
        javax.swing.JPanel p = new javax.swing.JPanel(new java.awt.BorderLayout(0, 12));
        javax.swing.JLabel ajuda = new javax.swing.JLabel("<html>Conte o dinheiro da gaveta e confira os extratos de Pix e das maquininhas."
                + "<br>O dinheiro esperado já inclui o fundo de troco, os suprimentos e as sangrias.</html>");
        ajuda.setForeground(Tema.TEXTO_SECUNDARIO);
        p.add(ajuda, java.awt.BorderLayout.NORTH);
        p.add(grade, java.awt.BorderLayout.CENTER);
        javax.swing.JPanel sul = new javax.swing.JPanel(new java.awt.BorderLayout(0, 4));
        sul.add(new javax.swing.JLabel("Observação (opcional):"), java.awt.BorderLayout.NORTH);
        sul.add(obs, java.awt.BorderLayout.CENTER);
        p.add(sul, java.awt.BorderLayout.SOUTH);

        while (true) {
            if (JOptionPane.showConfirmDialog(this, p, "Fechar caixa", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
                return;
            }
            Map<FormaPagamento, Double> informado = new EnumMap<>(FormaPagamento.class);
            boolean faltando = false;
            for (FormaPagamento f : FormaPagamento.values()) {
                Double v = lerValor(campos.get(f).getText());
                if (v == null || v < 0) {
                    faltando = true;
                }
                informado.put(f, v);
            }
            if (faltando) {
                JOptionPane.showMessageDialog(this, "Informe o valor contado de todas as formas (use 0 se não houver).",
                        "Fechar caixa", JOptionPane.WARNING_MESSAGE);
                continue;
            }
            try {
                Caixa fechado = caixaService.fechar(informado, obs.getText().trim());
                mostrarResumoFechamento(fechado);
                carregar();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Fechar caixa", JOptionPane.WARNING_MESSAGE);
            }
            return;
        }
    }

    private void mostrarResumoFechamento(Caixa c) {
        StringBuilder sb = new StringBuilder("<html><h3>Caixa nº " + c.getId() + " fechado</h3><table cellpadding=3>"
                + "<tr><th align=left>Forma</th><th align=right>Esperado</th><th align=right>Contado</th><th align=right>Diferença</th></tr>");
        double totalDif = 0;
        for (FormaPagamento f : FormaPagamento.values()) {
            double esperado = c.getEsperado(f);
            double contado = c.getInformado().get(f);
            double d = Math.round((contado - esperado) * 100) / 100.0;
            totalDif += d;
            sb.append("<tr><td>").append(f).append("</td><td align=right>").append(Tema.reais(esperado))
                    .append("</td><td align=right>").append(Tema.reais(contado)).append("</td><td align=right>")
                    .append(d == 0 ? "confere" : (d > 0 ? "sobra " : "falta ") + Tema.reais(Math.abs(d))).append("</td></tr>");
        }
        sb.append("</table><br>Total vendido: <b>").append(Tema.reais(c.getTotalVendido())).append("</b>");
        totalDif = Math.round(totalDif * 100) / 100.0;
        sb.append("<br>Diferença geral: <b>").append(totalDif == 0 ? "nenhuma" : Tema.reais(totalDif)).append("</b></html>");
        JOptionPane.showMessageDialog(this, sb.toString(), "Fechamento do caixa", JOptionPane.INFORMATION_MESSAGE);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        painelCabecalho = new javax.swing.JPanel();
        painelTitulo = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        lblStatus = new javax.swing.JLabel();
        painelAcoes = new javax.swing.JPanel();
        btnAbrir = new javax.swing.JButton();
        btnSuprimento = new javax.swing.JButton();
        btnSangria = new javax.swing.JButton();
        btnFechar = new javax.swing.JButton();
        btnAtualizar = new javax.swing.JButton();
        btnVoltar = new javax.swing.JButton();
        painelCorpo = new javax.swing.JPanel();
        painelIndicadores = new javax.swing.JPanel();
        cardDinheiro = new ui.CartaoIndicador();
        cardPix = new ui.CartaoIndicador();
        cardDebito = new ui.CartaoIndicador();
        cardCredito = new ui.CartaoIndicador();
        cardTotal = new ui.CartaoIndicador();
        painelTabelas = new javax.swing.JPanel();
        painelMov = new javax.swing.JPanel();
        lblMov = new javax.swing.JLabel();
        scrollMov = new javax.swing.JScrollPane();
        tabelaMov = new javax.swing.JTable();
        painelHist = new javax.swing.JPanel();
        lblHist = new javax.swing.JLabel();
        scrollHist = new javax.swing.JScrollPane();
        tabelaHist = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("BLK Burguer - Caixa");
        setMinimumSize(new java.awt.Dimension(1100, 640));

        painelCabecalho.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 24, 14, 24));
        painelCabecalho.setLayout(new java.awt.BorderLayout());
        painelTitulo.setLayout(new java.awt.GridLayout(2, 1, 0, 2));
        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblTitulo.setText("Caixa");
        painelTitulo.add(lblTitulo);

        lblStatus.setText("Caixa fechado");
        painelTitulo.add(lblStatus);

        painelCabecalho.add(painelTitulo, java.awt.BorderLayout.LINE_START);

        painelAcoes.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 8));
        btnAbrir.setText("Abrir caixa");
        btnAbrir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAbrirActionPerformed(evt);
            }
        });
        painelAcoes.add(btnAbrir);

        btnSuprimento.setText("Suprimento");
        btnSuprimento.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSuprimentoActionPerformed(evt);
            }
        });
        painelAcoes.add(btnSuprimento);

        btnSangria.setText("Sangria");
        btnSangria.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSangriaActionPerformed(evt);
            }
        });
        painelAcoes.add(btnSangria);

        btnFechar.setText("Fechar caixa");
        btnFechar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFecharActionPerformed(evt);
            }
        });
        painelAcoes.add(btnFechar);

        btnAtualizar.setText("Atualizar");
        btnAtualizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAtualizarActionPerformed(evt);
            }
        });
        painelAcoes.add(btnAtualizar);

        btnVoltar.setText("Voltar");
        btnVoltar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVoltarActionPerformed(evt);
            }
        });
        painelAcoes.add(btnVoltar);

        painelCabecalho.add(painelAcoes, java.awt.BorderLayout.LINE_END);

        getContentPane().add(painelCabecalho, java.awt.BorderLayout.PAGE_START);

        painelCorpo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 24, 24, 24));
        painelCorpo.setLayout(new java.awt.BorderLayout(0, 18));
        painelIndicadores.setLayout(new java.awt.GridLayout(1, 5, 14, 0));
        cardDinheiro.setTitulo("Dinheiro na gaveta");
        cardDinheiro.setPrincipal(true);
        painelIndicadores.add(cardDinheiro);

        cardPix.setTitulo("Pix");
        painelIndicadores.add(cardPix);

        cardDebito.setTitulo("Débito");
        painelIndicadores.add(cardDebito);

        cardCredito.setTitulo("Crédito");
        painelIndicadores.add(cardCredito);

        cardTotal.setTitulo("Total vendido");
        painelIndicadores.add(cardTotal);

        painelCorpo.add(painelIndicadores, java.awt.BorderLayout.PAGE_START);

        painelTabelas.setLayout(new java.awt.GridLayout(1, 2, 18, 0));
        painelMov.setLayout(new java.awt.BorderLayout(0, 8));
        lblMov.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        lblMov.setText("Movimentações do caixa atual");
        painelMov.add(lblMov, java.awt.BorderLayout.PAGE_START);

        scrollMov.setViewportView(tabelaMov);
        painelMov.add(scrollMov, java.awt.BorderLayout.CENTER);

        painelTabelas.add(painelMov);

        painelHist.setLayout(new java.awt.BorderLayout(0, 8));
        lblHist.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        lblHist.setText("Caixas anteriores");
        painelHist.add(lblHist, java.awt.BorderLayout.PAGE_START);

        scrollHist.setViewportView(tabelaHist);
        painelHist.add(scrollHist, java.awt.BorderLayout.CENTER);

        painelTabelas.add(painelHist);

        painelCorpo.add(painelTabelas, java.awt.BorderLayout.CENTER);

        getContentPane().add(painelCorpo, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnAbrirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAbrirActionPerformed
        abrir();
    }//GEN-LAST:event_btnAbrirActionPerformed

    private void btnSuprimentoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSuprimentoActionPerformed
        movimentar(CaixaService.SUPRIMENTO);
    }//GEN-LAST:event_btnSuprimentoActionPerformed

    private void btnSangriaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSangriaActionPerformed
        movimentar(CaixaService.SANGRIA);
    }//GEN-LAST:event_btnSangriaActionPerformed

    private void btnFecharActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFecharActionPerformed
        fechar();
    }//GEN-LAST:event_btnFecharActionPerformed

    private void btnAtualizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAtualizarActionPerformed
        carregar();
    }//GEN-LAST:event_btnAtualizarActionPerformed

    private void btnVoltarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVoltarActionPerformed
        Main.voltar(this);
    }//GEN-LAST:event_btnVoltarActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAbrir;
    private javax.swing.JButton btnAtualizar;
    private javax.swing.JButton btnFechar;
    private javax.swing.JButton btnSangria;
    private javax.swing.JButton btnSuprimento;
    private javax.swing.JButton btnVoltar;
    private ui.CartaoIndicador cardCredito;
    private ui.CartaoIndicador cardDebito;
    private ui.CartaoIndicador cardDinheiro;
    private ui.CartaoIndicador cardPix;
    private ui.CartaoIndicador cardTotal;
    private javax.swing.JLabel lblHist;
    private javax.swing.JLabel lblMov;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel painelAcoes;
    private javax.swing.JPanel painelCabecalho;
    private javax.swing.JPanel painelCorpo;
    private javax.swing.JPanel painelHist;
    private javax.swing.JPanel painelIndicadores;
    private javax.swing.JPanel painelMov;
    private javax.swing.JPanel painelTabelas;
    private javax.swing.JPanel painelTitulo;
    private javax.swing.JScrollPane scrollHist;
    private javax.swing.JScrollPane scrollMov;
    private javax.swing.JTable tabelaHist;
    private javax.swing.JTable tabelaMov;
    // End of variables declaration//GEN-END:variables
}
