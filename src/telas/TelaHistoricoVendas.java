package telas;

import entidades.Pedido;
import entidades.Venda;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class TelaHistoricoVendas extends javax.swing.JFrame {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final dao.VendaDAO vendaDAO = new dao.VendaDAO();
    private List<Pedido> pedidos = new ArrayList<>();

    public TelaHistoricoVendas() {
        initComponents();
        ui.Tema.janela(this);
        ui.Tema.titulo(lblTitulo);
        ui.Tema.primario(btnFiltrar);
        ui.Tema.secundario(lblSubtitulo);
        ui.Tema.transparente(painelCabecalho, painelTitulo, painelAcoesTopo, painelCorpo, painelFiltro, painelItens);
        lblItens.setForeground(ui.Tema.TEXTO);
        lblResumo.setForeground(ui.Tema.TEXTO);
        lblResumo.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 0, 0, 0));
        txtDataInicio.putClientProperty("JTextField.placeholderText", "dd/mm/aaaa");
        txtDataFim.putClientProperty("JTextField.placeholderText", "dd/mm/aaaa");
        tabelaVendas.getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tabelaVendas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarItens();
            }
        });
        carregarVendas();
        ui.Tema.tamanhoPadrao(this);
    }

    /** Converte o texto do filtro; vazio = sem limite. Lança exceção se inválido. */
    private LocalDate lerData(javax.swing.JTextField campo, String nomeCampo) {
        String texto = campo.getText().trim();
        if (texto.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(texto, DATA);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Data " + nomeCampo + " inválida. Use o formato dd/mm/aaaa.");
        }
    }

    private void carregarVendas() {
        LocalDate de;
        LocalDate ate;
        try {
            de = lerData(txtDataInicio, "inicial");
            ate = lerData(txtDataFim, "final");
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Filtro", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (de != null && ate != null && de.isAfter(ate)) {
            JOptionPane.showMessageDialog(this, "A data inicial é depois da data final.", "Filtro", JOptionPane.WARNING_MESSAGE);
            return;
        }

        javax.swing.table.DefaultTableModel modelo = new javax.swing.table.DefaultTableModel(
                new String[]{"Nº", "Data/hora", "Produtos", "Pagamento", "Total"}, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        double soma = 0;
        try {
            pedidos = vendaDAO.listarPedidos(de, ate);
            for (Pedido p : pedidos) {
                soma += p.getTotal();
                modelo.addRow(new Object[]{
                    p.isAvulsa() ? "-" : p.getId(),
                    p.getData().format(DATA_HORA),
                    p.getResumoItens(),
                    p.getFormaPagamento() == null ? "-" : p.getFormaPagamento().toString(),
                    String.format("R$ %.2f", p.getTotal())
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage());
        }
        tabelaVendas.setModel(modelo);
        tabelaVendas.getColumnModel().getColumn(0).setPreferredWidth(40);
        tabelaVendas.getColumnModel().getColumn(1).setPreferredWidth(120);
        tabelaVendas.getColumnModel().getColumn(2).setPreferredWidth(380);
        tabelaVendas.getColumnModel().getColumn(3).setPreferredWidth(90);
        tabelaVendas.getColumnModel().getColumn(4).setPreferredWidth(90);
        lblResumo.setText(String.format("%d venda(s)  |  Total: R$ %.2f", pedidos.size(), soma));
        mostrarItens();
    }

    private void mostrarItens() {
        javax.swing.table.DefaultTableModel modelo = new javax.swing.table.DefaultTableModel(
                new String[]{"Produto", "Qtd", "Valor unit.", "Subtotal"}, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        int linha = tabelaVendas.getSelectedRow();
        if (linha >= 0) {
            Pedido p = pedidos.get(linha);
            lblItens.setText(p.isAvulsa() ? "Itens da venda selecionada" : "Itens da venda nº " + p.getId());
            try {
                for (Venda v : vendaDAO.itensDoPedido(p.getId())) {
                    modelo.addRow(new Object[]{
                        v.getProduto(), v.getQuantidade(),
                        String.format("R$ %.2f", v.getValor()),
                        String.format("R$ %.2f", v.getTotal())
                    });
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage());
            }
        } else {
            lblItens.setText("Itens da venda selecionada");
        }
        tabelaItens.setModel(modelo);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        painelCabecalho = new javax.swing.JPanel();
        painelTitulo = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        lblSubtitulo = new javax.swing.JLabel();
        painelAcoesTopo = new javax.swing.JPanel();
        btnVoltar = new javax.swing.JButton();
        painelCorpo = new javax.swing.JPanel();
        painelFiltro = new javax.swing.JPanel();
        lblDe = new javax.swing.JLabel();
        txtDataInicio = new javax.swing.JTextField();
        lblAte = new javax.swing.JLabel();
        txtDataFim = new javax.swing.JTextField();
        btnFiltrar = new javax.swing.JButton();
        btnLimpar = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabelaVendas = new javax.swing.JTable();
        painelItens = new javax.swing.JPanel();
        lblItens = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tabelaItens = new javax.swing.JTable();
        lblResumo = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("BLK Burguer - Histórico de vendas");
        setMinimumSize(new java.awt.Dimension(1180, 720));

        painelCabecalho.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 24, 14, 24));
        painelCabecalho.setLayout(new java.awt.BorderLayout());
        painelTitulo.setLayout(new java.awt.GridLayout(2, 1, 0, 2));
        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblTitulo.setText("Histórico de vendas");
        painelTitulo.add(lblTitulo);

        lblSubtitulo.setText("Vendas finalizadas, com os itens e a forma de pagamento");
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
        lblDe.setText("De:");
        painelFiltro.add(lblDe);

        txtDataInicio.setPreferredSize(new java.awt.Dimension(120, 30));
        txtDataInicio.setToolTipText("Data inicial no formato dd/mm/aaaa (vazio = sem limite)");
        painelFiltro.add(txtDataInicio);

        lblAte.setText("Até:");
        painelFiltro.add(lblAte);

        txtDataFim.setPreferredSize(new java.awt.Dimension(120, 30));
        txtDataFim.setToolTipText("Data final no formato dd/mm/aaaa (vazio = sem limite)");
        painelFiltro.add(txtDataFim);

        btnFiltrar.setText("Filtrar");
        btnFiltrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFiltrarActionPerformed(evt);
            }
        });
        painelFiltro.add(btnFiltrar);

        btnLimpar.setText("Limpar");
        btnLimpar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLimparActionPerformed(evt);
            }
        });
        painelFiltro.add(btnLimpar);

        painelCorpo.add(painelFiltro, java.awt.BorderLayout.PAGE_START);

        jScrollPane1.setViewportView(tabelaVendas);
        painelCorpo.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        painelItens.setLayout(new java.awt.BorderLayout(0, 8));
        lblItens.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        lblItens.setText("Itens da venda selecionada");
        painelItens.add(lblItens, java.awt.BorderLayout.PAGE_START);

        jScrollPane2.setPreferredSize(new java.awt.Dimension(0, 190));
        jScrollPane2.setViewportView(tabelaItens);
        painelItens.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        lblResumo.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        lblResumo.setText("0 vendas");
        painelItens.add(lblResumo, java.awt.BorderLayout.PAGE_END);

        painelCorpo.add(painelItens, java.awt.BorderLayout.PAGE_END);

        getContentPane().add(painelCorpo, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents


    private void btnFiltrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFiltrarActionPerformed
        carregarVendas();
    }//GEN-LAST:event_btnFiltrarActionPerformed

    private void btnLimparActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimparActionPerformed
        txtDataInicio.setText("");
        txtDataFim.setText("");
        carregarVendas();
    }//GEN-LAST:event_btnLimparActionPerformed

    private void btnVoltarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVoltarActionPerformed
        Main.voltar(this);
    }//GEN-LAST:event_btnVoltarActionPerformed

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> new TelaHistoricoVendas().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnFiltrar;
    private javax.swing.JButton btnLimpar;
    private javax.swing.JButton btnVoltar;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblAte;
    private javax.swing.JLabel lblDe;
    private javax.swing.JLabel lblItens;
    private javax.swing.JLabel lblResumo;
    private javax.swing.JLabel lblSubtitulo;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel painelAcoesTopo;
    private javax.swing.JPanel painelCabecalho;
    private javax.swing.JPanel painelCorpo;
    private javax.swing.JPanel painelFiltro;
    private javax.swing.JPanel painelItens;
    private javax.swing.JPanel painelTitulo;
    private javax.swing.JTable tabelaItens;
    private javax.swing.JTable tabelaVendas;
    private javax.swing.JTextField txtDataFim;
    private javax.swing.JTextField txtDataInicio;
    // End of variables declaration//GEN-END:variables
}
