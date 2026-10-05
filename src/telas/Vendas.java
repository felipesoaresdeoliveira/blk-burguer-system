package telas;

import entidades.Adicional;
import entidades.Produto;
import entidades.Venda;
import java.util.Map;

public class Vendas extends javax.swing.JFrame {

    private java.util.List<Venda> itensVenda = new java.util.ArrayList<>();
    private java.util.List<Produto> produtos = new java.util.ArrayList<>();
    private Map<String, Map<String, Integer>> fichas = new java.util.HashMap<>();
    private Map<String, Adicional> adicionais = new java.util.LinkedHashMap<>();
    private final service.PedidoService pedidoService = new service.PedidoService();

    /** Carrega o cardápio, as fichas técnicas (para "tirar ingrediente") e os adicionais. */
    private void carregarEstoque() {
        produtos.clear();
        DropItemEstoque1.removeAllItems();
        try {
            produtos = new dao.ProdutoDAO().listar();
            fichas = pedidoService.fichasTecnicas();
            adicionais = pedidoService.adicionais();
            for (Produto p : produtos) {
                DropItemEstoque1.addItem(p.getNome() + " - R$ " + String.format("%.2f", p.getPreco()));
            }
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage());
        }
    }

    /** Mesas ativas para escolher onde entregar. */
    private void carregarMesas() {
        cmbMesa.removeAllItems();
        try {
            for (entidades.Mesa m : new dao.MesaDAO().listarComStatus()) {
                cmbMesa.addItem(m);
            }
        } catch (Exception e) {
            // Sem mesas carregadas, a venda ainda pode ser no balcão ou para viagem.
        }
    }

    private boolean destinoMesa() {
        return "Mesa".equals(cmbDestino.getSelectedItem());
    }

    private void atualizarDestino() {
        cmbMesa.setVisible(destinoMesa());
        cmbDestino.putClientProperty("JComponent.outline", null);
        painelLinhaDestino.revalidate();
    }

    /** Texto do destino para o pagamento e a confirmação ("Mesa 4", "Balcão - senha", "Para viagem"). */
    private String descricaoDestino() {
        if (destinoMesa()) {
            return String.valueOf(cmbMesa.getSelectedItem());
        }
        return cmbDestino.getSelectedIndex() == 3 ? "Para viagem" : "Balcão (chamar pela senha)";
    }

    /** O caixa precisa dizer onde o pedido será entregue antes de cobrar. */
    private boolean destinoValido() {
        if (cmbDestino.getSelectedIndex() <= 0 || (destinoMesa() && cmbMesa.getSelectedItem() == null)) {
            cmbDestino.putClientProperty("JComponent.outline", "error");
            cmbDestino.requestFocusInWindow();
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Escolha onde o cliente vai receber o pedido: numa mesa, no balcão (pela senha) ou para viagem.",
                    "Onde entregar?", javax.swing.JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    /** Abre a personalização do lanche (tirar, adicionais, observação). */
    private boolean personalizar(Produto produto, Venda item) {
        return DialogoPersonalizar.mostrar(this, produto, item, fichas, adicionais);
    }

    private void atualizarTabela() {
        javax.swing.table.DefaultTableModel modelo
                = new javax.swing.table.DefaultTableModel(
                        new String[]{"Produto", "Qtd", "Valor Unit.", "Total"}, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        double totalGeral = 0;
        for (Venda v : itensVenda) {
            double sub = v.getValor() * v.getQuantidade();
            totalGeral += sub;
            modelo.addRow(new Object[]{
                v.isPersonalizado() ? v.getProduto() + "  (" + v.getResumoPersonalizacao() + ")" : v.getProduto(),
                v.getQuantidade(),
                String.format("R$ %.2f", v.getValor()),
                String.format("R$ %.2f", sub)
            });
        }
        TableItensVenda1.setModel(modelo);
        TableItensVenda1.getColumnModel().getColumn(0).setPreferredWidth(620);
        TableItensVenda1.getColumnModel().getColumn(1).setPreferredWidth(60);
        NumeroTotal1.setText(String.format("R$ %.2f", totalGeral));
    }

    /** Lê valor em reais aceitando vírgula ou ponto; null se inválido. */
    private static Double lerValor(String texto) {
        try {
            return Double.valueOf(texto.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Janela de pagamento: forma de pagamento e, em dinheiro, valor recebido
     * com o troco calculado na hora. Retorna {forma, valorRecebido} ou null se
     * o usuário cancelar.
     */
    private static int clienteSelecionado(javax.swing.JComboBox<Object> combo) {
        Object c = combo.getSelectedItem();
        return c instanceof entidades.Cliente ? ((entidades.Cliente) c).getId() : 0;
    }

    private Object[] escolherPagamento(double total) {
        javax.swing.JComboBox<entidades.FormaPagamento> campoForma
                = new javax.swing.JComboBox<>(entidades.FormaPagamento.values());
        javax.swing.JTextField campoRecebido = new javax.swing.JTextField(10);
        javax.swing.JLabel lblTroco = new javax.swing.JLabel();
        lblTroco.setFont(lblTroco.getFont().deriveFont(java.awt.Font.BOLD, 14f));
        javax.swing.JComboBox<Object> campoCliente = new javax.swing.JComboBox<>();
        campoCliente.addItem("Sem cliente");
        try {
            for (entidades.Cliente c : new dao.ClienteDAO().listar("")) {
                campoCliente.addItem(c);
            }
        } catch (Exception e) {
            // Sem clientes carregados: a venda segue sem cliente.
        }

        Runnable atualizar = () -> {
            boolean dinheiro = campoForma.getSelectedItem() == entidades.FormaPagamento.DINHEIRO;
            campoRecebido.setEnabled(dinheiro);
            if (!dinheiro) {
                lblTroco.setText("Troco: R$ 0,00");
                lblTroco.setForeground(ui.Tema.SUCESSO);
                return;
            }
            Double recebido = lerValor(campoRecebido.getText());
            double troco = recebido == null ? -total : service.VendaService.troco(total, recebido);
            if (troco < 0) {
                lblTroco.setText(String.format("Faltam: R$ %.2f", -troco));
                lblTroco.setForeground(ui.Tema.PERIGO);
            } else {
                lblTroco.setText(String.format("Troco: R$ %.2f", troco));
                lblTroco.setForeground(ui.Tema.SUCESSO);
            }
        };
        campoForma.addActionListener(e -> atualizar.run());
        campoRecebido.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                atualizar.run();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                atualizar.run();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                atualizar.run();
            }
        });
        atualizar.run();

        javax.swing.JLabel lblTotal = new javax.swing.JLabel(String.format("Total: R$ %.2f", total));
        lblTotal.setFont(lblTotal.getFont().deriveFont(java.awt.Font.BOLD, 16f));
        javax.swing.JPanel painel = new javax.swing.JPanel(new java.awt.GridLayout(0, 1, 0, 6));
        painel.add(lblTotal);
        javax.swing.JLabel lblOnde = new javax.swing.JLabel("Entregar em: " + descricaoDestino());
        lblOnde.setFont(lblOnde.getFont().deriveFont(java.awt.Font.BOLD, 14f));
        lblOnde.setForeground(ui.Tema.DESTAQUE);
        painel.add(lblOnde);
        painel.add(new javax.swing.JLabel("Forma de pagamento:"));
        painel.add(campoForma);
        painel.add(new javax.swing.JLabel("Valor recebido (dinheiro):"));
        painel.add(campoRecebido);
        painel.add(lblTroco);
        painel.add(new javax.swing.JLabel("Cliente (opcional):"));
        painel.add(campoCliente);

        while (true) {
            int opcao = javax.swing.JOptionPane.showConfirmDialog(this, painel, "Finalizar venda",
                    javax.swing.JOptionPane.OK_CANCEL_OPTION, javax.swing.JOptionPane.PLAIN_MESSAGE);
            if (opcao != javax.swing.JOptionPane.OK_OPTION) {
                return null;
            }
            entidades.FormaPagamento forma = (entidades.FormaPagamento) campoForma.getSelectedItem();
            if (forma != entidades.FormaPagamento.DINHEIRO) {
                return new Object[]{forma, total, clienteSelecionado(campoCliente)};
            }
            Double recebido = lerValor(campoRecebido.getText());
            if (recebido != null && service.VendaService.troco(total, recebido) >= 0) {
                return new Object[]{forma, recebido, clienteSelecionado(campoCliente)};
            }
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Informe um valor recebido igual ou maior que o total.",
                    "Valor recebido", javax.swing.JOptionPane.WARNING_MESSAGE);
        }
    }

    public Vendas() {
        initComponents();
        ui.Tema.janela(this);
        ui.Tema.titulo(BLKBurguer1);
        ui.Tema.secundario(lblSubtitulo);
        ui.Tema.primario(BtnFinalizarVenda);
        ui.Tema.perigo(cancelarVenda);
        ui.Tema.transparente(painelCabecalho, painelTitulo, painelAcoesTopo, painelCorpo, painelAdicionar,
                painelLinhaItem, painelRodape, painelTotal, painelBotoesVenda, painelDestino, painelLinhaDestino,
                painelItemBloco);
        TxtADDItem1.setForeground(ui.Tema.TEXTO);
        lblDestinoTitulo.setForeground(ui.Tema.DESTAQUE);
        txtNomeCliente.putClientProperty("JTextField.placeholderText", "para chamar o cliente");
        cmbDestino.addActionListener(e -> atualizarDestino());
        carregarMesas();
        atualizarDestino();
        NumeroTotal1.setForeground(ui.Tema.DESTAQUE);
        BtnFinalizarVenda.setFont(BtnFinalizarVenda.getFont().deriveFont(java.awt.Font.BOLD, 16f));
        BtnFinalizarVenda.setPreferredSize(new java.awt.Dimension(200, 46));
        SelectQnt1.setModel(new javax.swing.SpinnerNumberModel(1, 1, 999, 1));
        carregarEstoque();
        atualizarTabela();
        ui.Tema.tamanhoPadrao(this);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        painelCabecalho = new javax.swing.JPanel();
        painelTitulo = new javax.swing.JPanel();
        BLKBurguer1 = new javax.swing.JLabel();
        lblSubtitulo = new javax.swing.JLabel();
        painelAcoesTopo = new javax.swing.JPanel();
        voltar = new javax.swing.JButton();
        painelCorpo = new javax.swing.JPanel();
        painelAdicionar = new javax.swing.JPanel();
        painelDestino = new javax.swing.JPanel();
        lblDestinoTitulo = new javax.swing.JLabel();
        painelLinhaDestino = new javax.swing.JPanel();
        lblDestino = new javax.swing.JLabel();
        cmbDestino = new javax.swing.JComboBox<>();
        cmbMesa = new javax.swing.JComboBox<>();
        lblNomeCliente = new javax.swing.JLabel();
        txtNomeCliente = new javax.swing.JTextField();
        painelItemBloco = new javax.swing.JPanel();
        TxtADDItem1 = new javax.swing.JLabel();
        painelLinhaItem = new javax.swing.JPanel();
        TXTProduto1 = new javax.swing.JLabel();
        DropItemEstoque1 = new javax.swing.JComboBox<>();
        TxtQnt1 = new javax.swing.JLabel();
        SelectQnt1 = new javax.swing.JSpinner();
        BtnAdd1 = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        TableItensVenda1 = new javax.swing.JTable();
        painelRodape = new javax.swing.JPanel();
        painelTotal = new javax.swing.JPanel();
        TxtTotal1 = new javax.swing.JLabel();
        NumeroTotal1 = new javax.swing.JLabel();
        painelBotoesVenda = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        cancelarVenda = new javax.swing.JButton();
        BtnFinalizarVenda = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("BLK Burguer - Nova venda");
        setMinimumSize(new java.awt.Dimension(1180, 720));

        painelCabecalho.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 24, 14, 24));
        painelCabecalho.setLayout(new java.awt.BorderLayout());
        painelTitulo.setLayout(new java.awt.GridLayout(2, 1, 0, 2));
        BLKBurguer1.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        BLKBurguer1.setText("Nova venda");
        painelTitulo.add(BLKBurguer1);

        lblSubtitulo.setText("Balcão: defina onde entregar, monte o pedido e receba");
        painelTitulo.add(lblSubtitulo);

        painelCabecalho.add(painelTitulo, java.awt.BorderLayout.LINE_START);

        painelAcoesTopo.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 8));
        voltar.setText("Voltar ao caixa");
        voltar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                voltarActionPerformed(evt);
            }
        });
        painelAcoesTopo.add(voltar);

        painelCabecalho.add(painelAcoesTopo, java.awt.BorderLayout.LINE_END);

        getContentPane().add(painelCabecalho, java.awt.BorderLayout.PAGE_START);

        painelCorpo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 24, 24, 24));
        painelCorpo.setLayout(new java.awt.BorderLayout(20, 16));
        painelAdicionar.setLayout(new java.awt.BorderLayout(0, 14));
        painelDestino.setLayout(new java.awt.BorderLayout(0, 6));
        lblDestinoTitulo.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        lblDestinoTitulo.setText("1. Onde o cliente vai receber");
        painelDestino.add(lblDestinoTitulo, java.awt.BorderLayout.PAGE_START);

        painelLinhaDestino.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 0));
        lblDestino.setText("Entregar em:");
        painelLinhaDestino.add(lblDestino);

        cmbDestino.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Selecione...", "Mesa", "Balcão (chamar pela senha)", "Para viagem" }));
        cmbDestino.setPreferredSize(new java.awt.Dimension(230, 34));
        painelLinhaDestino.add(cmbDestino);

        cmbMesa.setPreferredSize(new java.awt.Dimension(140, 34));
        painelLinhaDestino.add(cmbMesa);

        lblNomeCliente.setText("Nome do cliente (opcional):");
        painelLinhaDestino.add(lblNomeCliente);

        txtNomeCliente.setPreferredSize(new java.awt.Dimension(220, 34));
        painelLinhaDestino.add(txtNomeCliente);

        painelDestino.add(painelLinhaDestino, java.awt.BorderLayout.CENTER);

        painelAdicionar.add(painelDestino, java.awt.BorderLayout.PAGE_START);

        painelItemBloco.setLayout(new java.awt.BorderLayout(0, 6));
        TxtADDItem1.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        TxtADDItem1.setText("2. Itens do pedido");
        painelItemBloco.add(TxtADDItem1, java.awt.BorderLayout.PAGE_START);

        painelLinhaItem.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 0));
        TXTProduto1.setText("Produto:");
        painelLinhaItem.add(TXTProduto1);

        DropItemEstoque1.setPreferredSize(new java.awt.Dimension(420, 34));
        painelLinhaItem.add(DropItemEstoque1);

        TxtQnt1.setText("Qtd:");
        painelLinhaItem.add(TxtQnt1);

        SelectQnt1.setPreferredSize(new java.awt.Dimension(80, 34));
        painelLinhaItem.add(SelectQnt1);

        BtnAdd1.setText("Adicionar");
        BtnAdd1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnAdd1ActionPerformed(evt);
            }
        });
        painelLinhaItem.add(BtnAdd1);

        painelItemBloco.add(painelLinhaItem, java.awt.BorderLayout.CENTER);

        painelAdicionar.add(painelItemBloco, java.awt.BorderLayout.CENTER);

        painelCorpo.add(painelAdicionar, java.awt.BorderLayout.PAGE_START);

        jScrollPane2.setViewportView(TableItensVenda1);
        painelCorpo.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        painelRodape.setLayout(new java.awt.BorderLayout());
        painelTotal.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 0));
        TxtTotal1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        TxtTotal1.setText("Total:");
        painelTotal.add(TxtTotal1);

        NumeroTotal1.setFont(new java.awt.Font("Segoe UI", 1, 30)); // NOI18N
        NumeroTotal1.setText("R$ 0,00");
        painelTotal.add(NumeroTotal1);

        painelRodape.add(painelTotal, java.awt.BorderLayout.LINE_START);

        painelBotoesVenda.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 0));
        jButton1.setText("Remover item");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        painelBotoesVenda.add(jButton1);

        cancelarVenda.setText("Cancelar venda");
        cancelarVenda.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelarVendaActionPerformed(evt);
            }
        });
        painelBotoesVenda.add(cancelarVenda);

        BtnFinalizarVenda.setText("Finalizar venda");
        BtnFinalizarVenda.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnFinalizarVendaActionPerformed(evt);
            }
        });
        painelBotoesVenda.add(BtnFinalizarVenda);

        painelRodape.add(painelBotoesVenda, java.awt.BorderLayout.LINE_END);

        painelCorpo.add(painelRodape, java.awt.BorderLayout.PAGE_END);

        getContentPane().add(painelCorpo, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents


    private void BtnFinalizarVendaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnFinalizarVendaActionPerformed
        if (itensVenda.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Adicione itens antes de finalizar.");
            return;
        }

        if (!destinoValido()) {
            return;
        }
        double total = service.VendaService.total(itensVenda);
        Object[] pagamento = escolherPagamento(total);
        if (pagamento == null) {
            return;
        }
        entidades.FormaPagamento forma = (entidades.FormaPagamento) pagamento[0];
        double recebido = (Double) pagamento[1];

        try {
            boolean viagem = cmbDestino.getSelectedIndex() == 3;
            int mesaId = destinoMesa() ? ((entidades.Mesa) cmbMesa.getSelectedItem()).getId() : 0;
            entidades.Pedido pedido = new service.VendaService().finalizarVenda(itensVenda, forma, recebido,
                    (Integer) pagamento[2], viagem, mesaId, txtNomeCliente.getText().trim());
            String onde = destinoMesa() ? "Entregar na " + cmbMesa.getSelectedItem()
                    : viagem ? "Para viagem: chamar pela senha" : "Consumo no local: chamar pela senha";
            String msg = String.format("Venda nº %d finalizada!%n%s%nSenha do cliente: %d%nPagamento: %s",
                    pedido.getId(), onde, pedido.getSenha(), forma);
            if (pedido.getItens().stream().anyMatch(v -> v.getStatusCozinha() != null)) {
                msg += String.format("%nOs lanches foram enviados para a cozinha.");
            }
            if (forma == entidades.FormaPagamento.DINHEIRO) {
                msg += String.format("%nTroco: R$ %.2f", service.VendaService.troco(total, recebido));
            }
            javax.swing.JOptionPane.showMessageDialog(this, msg);
        } catch (service.EstoqueInsuficienteException e) {
            javax.swing.JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Estoque insuficiente", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
            javax.swing.JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Atenção", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage());
            return;
        }

        itensVenda.clear();
        atualizarTabela();
        carregarEstoque();
        SelectQnt1.setValue(1);
        cmbDestino.setSelectedIndex(0);
        txtNomeCliente.setText("");
    }//GEN-LAST:event_BtnFinalizarVendaActionPerformed

    private void BtnAdd1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnAdd1ActionPerformed
        if (DropItemEstoque1.getSelectedIndex() < 0) {
            return;
        }
        int qtd = (int) SelectQnt1.getValue();
        if (qtd <= 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Informe uma quantidade válida.");
            return;
        }

        Produto produto = produtos.get(DropItemEstoque1.getSelectedIndex());

        // Sem verificação de estoque aqui: a baixa acontece ao finalizar.
        entidades.Venda v = new entidades.Venda();
        v.setProduto(produto.getNome());
        v.setQuantidade(qtd);
        v.setValor(produto.getPreco());
        // Lanches e acompanhamentos podem ser personalizados; bebida entra direto.
        if (!"Bebida".equalsIgnoreCase(produto.getTipo()) && !personalizar(produto, v)) {
            return;
        }
        v.setTotal(service.VendaService.arredondar(v.getValor() * qtd));
        itensVenda.add(v);
        atualizarTabela();
        SelectQnt1.setValue(1);
    }//GEN-LAST:event_BtnAdd1ActionPerformed

    private void voltarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_voltarActionPerformed
        dispose();
        new TelaCaixa().setVisible(true);

    }//GEN-LAST:event_voltarActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        int linha = TableItensVenda1.getSelectedRow();
        if (linha < 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Selecione um item para remover.");
            return;
        }
        itensVenda.remove(linha);
        atualizarTabela();

    }//GEN-LAST:event_jButton1ActionPerformed

    private void cancelarVendaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelarVendaActionPerformed
        if (itensVenda.isEmpty()) {
            return;
        }
        int confirm = javax.swing.JOptionPane.showConfirmDialog(this,
                "Cancelar a venda atual? Os itens serão perdidos.",
                "Cancelar Venda", javax.swing.JOptionPane.YES_NO_OPTION);
        if (confirm == javax.swing.JOptionPane.YES_OPTION) {
            itensVenda.clear();
            atualizarTabela();
            SelectQnt1.setValue(1);
        }
    }//GEN-LAST:event_cancelarVendaActionPerformed

    public static void main(String args[]) {
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;

                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Vendas.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Vendas.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Vendas.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Vendas.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Vendas().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel BLKBurguer1;
    private javax.swing.JButton BtnAdd1;
    private javax.swing.JButton BtnFinalizarVenda;
    private javax.swing.JComboBox<String> DropItemEstoque1;
    private javax.swing.JLabel NumeroTotal1;
    private javax.swing.JSpinner SelectQnt1;
    private javax.swing.JLabel TXTProduto1;
    private javax.swing.JTable TableItensVenda1;
    private javax.swing.JLabel TxtADDItem1;
    private javax.swing.JLabel TxtQnt1;
    private javax.swing.JLabel TxtTotal1;
    private javax.swing.JButton cancelarVenda;
    private javax.swing.JComboBox<String> cmbDestino;
    private javax.swing.JComboBox<Object> cmbMesa;
    private javax.swing.JButton jButton1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblDestino;
    private javax.swing.JLabel lblDestinoTitulo;
    private javax.swing.JLabel lblNomeCliente;
    private javax.swing.JLabel lblSubtitulo;
    private javax.swing.JPanel painelAcoesTopo;
    private javax.swing.JPanel painelAdicionar;
    private javax.swing.JPanel painelBotoesVenda;
    private javax.swing.JPanel painelCabecalho;
    private javax.swing.JPanel painelCorpo;
    private javax.swing.JPanel painelDestino;
    private javax.swing.JPanel painelItemBloco;
    private javax.swing.JPanel painelLinhaDestino;
    private javax.swing.JPanel painelLinhaItem;
    private javax.swing.JPanel painelRodape;
    private javax.swing.JPanel painelTitulo;
    private javax.swing.JPanel painelTotal;
    private javax.swing.JTextField txtNomeCliente;
    private javax.swing.JButton voltar;
    // End of variables declaration//GEN-END:variables
}