package telas;

import entidades.Estoque;
import entidades.Venda;

public class Vendas extends javax.swing.JFrame {

    private java.util.List<Venda> itensVenda = new java.util.ArrayList<>();
    private java.util.List<Estoque> listaEstoque = new java.util.ArrayList<>();

    private java.sql.Connection getConnection() throws Exception {
        Class.forName("org.postgresql.Driver");
        return java.sql.DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/BLKburguer", "postgres", "2627");
    }

    private void carregarEstoque() {
        listaEstoque.clear();
        DropItemEstoque1.removeAllItems();
        try (java.sql.Connection con = getConnection(); java.sql.Statement stm = con.createStatement(); java.sql.ResultSet rs = stm.executeQuery("SELECT * FROM \"Produto\"")) {
            while (rs.next()) {
                entidades.Estoque e = new entidades.Estoque();
                e.setNome(rs.getString("nome"));
                e.setPreco(rs.getDouble("preco"));
                listaEstoque.add(e);
                DropItemEstoque1.addItem(e.getNome() + " - R$ "
                        + String.format("%.2f", e.getPreco()));
            }
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage());
        }
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
                v.getProduto(), v.getQuantidade(),
                String.format("R$ %.2f", v.getValor()),
                String.format("R$ %.2f", sub)
            });
        }
        TableItensVenda1.setModel(modelo);
        NumeroTotal1.setText(String.format("R$ %.2f", totalGeral));
    }

    public Vendas() {
        initComponents();
        carregarEstoque();
        atualizarTabela();
        setLocationRelativeTo(null);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        TxtADDItem1 = new javax.swing.JLabel();
        DropItemEstoque1 = new javax.swing.JComboBox<>();
        TXTProduto1 = new javax.swing.JLabel();
        TxtQnt1 = new javax.swing.JLabel();
        SelectQnt1 = new javax.swing.JSpinner();
        jScrollPane2 = new javax.swing.JScrollPane();
        TableItensVenda1 = new javax.swing.JTable();
        BtnAdd1 = new javax.swing.JButton();
        BtnFinalizarVenda = new javax.swing.JButton();
        TxtTotal1 = new javax.swing.JLabel();
        BLKBurguer1 = new javax.swing.JLabel();
        NumeroTotal1 = new javax.swing.JLabel();
        cancelarVenda = new javax.swing.JButton();
        voltar = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        TxtADDItem1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        TxtADDItem1.setText("Adicionar item do estoque");

        DropItemEstoque1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        TXTProduto1.setText("Produto: ");

        TxtQnt1.setText("Qnt: ");

        TableItensVenda1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Produto", "Qnt", "Preço unit.", "Subtotal"
            }
        ));
        jScrollPane2.setViewportView(TableItensVenda1);

        BtnAdd1.setText("Adicionar");
        BtnAdd1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnAdd1ActionPerformed(evt);
            }
        });

        BtnFinalizarVenda.setText("Finalizar Venda");
        BtnFinalizarVenda.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnFinalizarVendaActionPerformed(evt);
            }
        });

        TxtTotal1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        TxtTotal1.setText("Total:  R$");

        BLKBurguer1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        BLKBurguer1.setText("BLK BURGUER - Vendas");

        NumeroTotal1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        NumeroTotal1.setForeground(new java.awt.Color(0, 204, 0));
        NumeroTotal1.setText("0");

        cancelarVenda.setText("Cancelar Venda");
        cancelarVenda.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelarVendaActionPerformed(evt);
            }
        });

        voltar.setText("Voltar");
        voltar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                voltarActionPerformed(evt);
            }
        });

        jButton1.setText("Remover Item");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(0, 0, Short.MAX_VALUE)
                                        .addComponent(BLKBurguer1))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(TXTProduto1)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(DropItemEstoque1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(TxtQnt1)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(SelectQnt1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(BtnAdd1, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(50, 50, 50))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(TxtADDItem1)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(TxtTotal1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(NumeroTotal1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 102, Short.MAX_VALUE)
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelarVenda)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(BtnFinalizarVenda)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(voltar)
                        .addGap(56, 56, 56))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(28, Short.MAX_VALUE)
                .addComponent(BLKBurguer1, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(TxtADDItem1, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(TxtQnt1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(SelectQnt1, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(BtnAdd1, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(DropItemEstoque1, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(TXTProduto1)))
                .addGap(60, 60, 60)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 231, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(TxtTotal1)
                        .addComponent(NumeroTotal1))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(cancelarVenda, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(BtnFinalizarVenda, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(voltar, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(51, 51, 51))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void BtnFinalizarVendaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnFinalizarVendaActionPerformed
        if (itensVenda.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Adicione itens antes de finalizar.");
            return;
        }

        int confirm = javax.swing.JOptionPane.showConfirmDialog(this,
                "Finalizar venda?\nTotal: " + NumeroTotal1.getText(),
                "Confirmar", javax.swing.JOptionPane.YES_NO_OPTION);

        if (confirm != javax.swing.JOptionPane.YES_OPTION) {
            return;
        }

        try (java.sql.Connection con = getConnection()) {
            con.setAutoCommit(false);

            // verifica estoque primeiro
            for (entidades.Venda v : itensVenda) {
                java.sql.Statement stmVerifica = con.createStatement();
                java.sql.ResultSet rsIng = stmVerifica.executeQuery(
                        "SELECT pi.ingrediente_nome, pi.quantidade * " + v.getQuantidade() + " as necessario, "
                        + "e.\"Quantidade\" as disponivel "
                        + "FROM \"ProdutoIngrediente\" pi "
                        + "JOIN \"Estoque\" e ON e.nome = pi.ingrediente_nome "
                        + "JOIN \"Produto\" p ON p.id = pi.produto_id "
                        + "WHERE p.nome = '" + v.getProduto() + "'");
                while (rsIng.next()) {
                    if (rsIng.getInt("necessario") > rsIng.getInt("disponivel")) {
                        javax.swing.JOptionPane.showMessageDialog(this,
                                "Estoque insuficiente de: " + rsIng.getString("ingrediente_nome")
                                + "\nNecessário: " + rsIng.getInt("necessario")
                                + " | Disponível: " + rsIng.getInt("disponivel"));
                        con.rollback();
                        return;
                    }
                }
                stmVerifica.close();
            }

            // desconta e registra
            for (entidades.Venda v : itensVenda) {
                java.sql.Statement stmSelect = con.createStatement();
                java.sql.ResultSet rsIng = stmSelect.executeQuery(
                        "SELECT pi.ingrediente_nome, pi.quantidade "
                        + "FROM \"ProdutoIngrediente\" pi "
                        + "JOIN \"Produto\" p ON p.id = pi.produto_id "
                        + "WHERE p.nome = '" + v.getProduto() + "'");

                java.util.List<String> nomes = new java.util.ArrayList<>();
                java.util.List<Integer> qtds = new java.util.ArrayList<>();
                while (rsIng.next()) {
                    nomes.add(rsIng.getString("ingrediente_nome"));
                    qtds.add(rsIng.getInt("quantidade") * v.getQuantidade());
                }
                stmSelect.close();

                java.sql.Statement stmUpdate = con.createStatement();
                for (int i = 0; i < nomes.size(); i++) {
                    stmUpdate.execute("UPDATE \"Estoque\" SET \"Quantidade\" = \"Quantidade\" - "
                            + qtds.get(i) + " WHERE nome = '" + nomes.get(i) + "'");
                }
                stmUpdate.execute("INSERT INTO \"Venda\" (produto, quantidade, valor, total) VALUES ('"
                        + v.getProduto() + "', " + v.getQuantidade() + ", "
                        + v.getValor() + ", " + v.getTotal() + ")");
                stmUpdate.close();
            }

            con.commit();
            javax.swing.JOptionPane.showMessageDialog(this, "Venda finalizada com sucesso!");

        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage());
            return;
        }

        itensVenda.clear();
        atualizarTabela();
        carregarEstoque();
        SelectQnt1.setValue(1);
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

        entidades.Estoque est = listaEstoque.get(DropItemEstoque1.getSelectedIndex());

        // sem verificação de estoque aqui — a baixa acontece ao finalizar
        entidades.Venda v = new entidades.Venda();
        v.setProduto(est.getNome());
        v.setQuantidade(qtd);
        v.setValor(est.getPreco());
        v.setTotal(est.getPreco() * qtd);
        itensVenda.add(v);
        atualizarTabela();
        SelectQnt1.setValue(1);
    }//GEN-LAST:event_BtnAdd1ActionPerformed

    private void voltarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_voltarActionPerformed
        new Main().setVisible(true);
        dispose();

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
    private javax.swing.JButton jButton1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JButton voltar;
    // End of variables declaration//GEN-END:variables
}