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
        txtDataInicio.putClientProperty("JTextField.placeholderText", "dd/mm/aaaa");
        txtDataFim.putClientProperty("JTextField.placeholderText", "dd/mm/aaaa");
        tabelaVendas.getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tabelaVendas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarItens();
            }
        });
        carregarVendas();
        setLocationRelativeTo(null);
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

        lblTitulo = new javax.swing.JLabel();
        lblDe = new javax.swing.JLabel();
        txtDataInicio = new javax.swing.JTextField();
        lblAte = new javax.swing.JLabel();
        txtDataFim = new javax.swing.JTextField();
        btnFiltrar = new javax.swing.JButton();
        btnLimpar = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabelaVendas = new javax.swing.JTable();
        lblItens = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tabelaItens = new javax.swing.JTable();
        lblResumo = new javax.swing.JLabel();
        btnVoltar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("BLK Burguer - Histórico de vendas");

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblTitulo.setText("BLK BURGUER - Histórico de vendas");

        lblDe.setText("De:");

        txtDataInicio.setToolTipText("Data inicial no formato dd/mm/aaaa (vazio = sem limite)");

        lblAte.setText("Até:");

        txtDataFim.setToolTipText("Data final no formato dd/mm/aaaa (vazio = sem limite)");

        btnFiltrar.setText("Filtrar");
        btnFiltrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFiltrarActionPerformed(evt);
            }
        });

        btnLimpar.setText("Limpar");
        btnLimpar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLimparActionPerformed(evt);
            }
        });

        jScrollPane1.setViewportView(tabelaVendas);

        lblItens.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblItens.setText("Itens da venda selecionada");

        jScrollPane2.setViewportView(tabelaItens);

        lblResumo.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblResumo.setText("0 vendas");

        btnVoltar.setText("Voltar");
        btnVoltar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVoltarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTitulo)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblDe)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtDataInicio, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblAte)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtDataFim, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnFiltrar, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnLimpar, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 760, Short.MAX_VALUE)
                    .addComponent(lblItens)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 760, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblResumo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnVoltar, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTitulo)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblDe)
                    .addComponent(txtDataInicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblAte)
                    .addComponent(txtDataFim, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnFiltrar)
                    .addComponent(btnLimpar))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 230, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblItens)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblResumo)
                    .addComponent(btnVoltar, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

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
        new Main().setVisible(true);
        dispose();
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
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JTable tabelaItens;
    private javax.swing.JTable tabelaVendas;
    private javax.swing.JTextField txtDataFim;
    private javax.swing.JTextField txtDataInicio;
    // End of variables declaration//GEN-END:variables
}
