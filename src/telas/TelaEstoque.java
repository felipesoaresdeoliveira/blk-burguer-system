package telas;

import entidades.Estoque;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class TelaEstoque extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(TelaEstoque.class.getName());

    public TelaEstoque() {
        initComponents();
        validaCampos("inicio");
        montaTabela();
        setLocationRelativeTo(null);
    }
    Estoque est = new Estoque();
    List<Estoque> listaEstoque = new ArrayList<>();
    private final dao.EstoqueDAO estoqueDAO = new dao.EstoqueDAO();
    private final service.EstoqueService estoqueService = new service.EstoqueService();

    public void limparCampos() {
        CampoNome.setText("");
        CampoCusto.setText("");
        CampoQnt.setValue(0);
        ComboBoxTipo.setSelectedIndex(0);
        est = new Estoque();
    }

    public void validaCampos(String op) {
        boolean selecionado = op.equals("selecionado");
        btnEntrada.setEnabled(selecionado);
        btnAjustar.setEnabled(selecionado);
        if (op.equals("inicio")) {
            CampoNome.setEnabled(false);
            CampoCusto.setEnabled(false);
            CampoQnt.setEnabled(false);
            ComboBoxTipo.setEnabled(false);
            btnNovo.setEnabled(true);
            btnEditar.setEnabled(false);
            btnExcluir.setEnabled(false);
            btnSalvar.setEnabled(false);
            btnCancelar.setEnabled(false);
            btnSair.setEnabled(true);
        } else if (op.equals("novo")) {
            CampoNome.setEnabled(true);
            CampoCusto.setEnabled(true);
            CampoQnt.setEnabled(true);
            ComboBoxTipo.setEnabled(true);
            btnNovo.setEnabled(false);
            btnEditar.setEnabled(false);
            btnExcluir.setEnabled(false);
            btnSalvar.setEnabled(true);
            btnCancelar.setEnabled(true);
            btnSair.setEnabled(false);
        } else if (op.equals("editar")) {
            // Quantidade só muda por Entrada/Ajustar, para manter o histórico.
            validaCampos("novo");
            CampoQnt.setEnabled(false);
        } else if (op.equals("selecionado")) {
            CampoNome.setEnabled(false);
            CampoCusto.setEnabled(false);
            CampoQnt.setEnabled(false);
            ComboBoxTipo.setEnabled(false);
            btnNovo.setEnabled(true);
            btnEditar.setEnabled(true);
            btnExcluir.setEnabled(true);
            btnSalvar.setEnabled(false);
            btnCancelar.setEnabled(true);
            btnSair.setEnabled(false);
        }
    }

    public void montaTabela() {
        try {
            javax.swing.table.DefaultTableModel modelo
                    = new javax.swing.table.DefaultTableModel(
                            new String[]{"Nome", "Tipo", "Quantidade", "Custo unit."}, 0) {
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            };
            listaEstoque = estoqueDAO.listar();
            for (Estoque e : listaEstoque) {
                modelo.addRow(new Object[]{
                    e.getNome(), e.getTipo(), e.getQuantidade(),
                    String.format("R$ %.2f", e.getPreco())
                });
            }
            tabelaItens.setModel(modelo);
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabelaItens = new javax.swing.JTable();
        txtTitle = new javax.swing.JLabel();
        TxtNome = new javax.swing.JLabel();
        txtQnt = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        CampoNome = new javax.swing.JTextPane();
        btnNovo = new javax.swing.JButton();
        btnEditar = new javax.swing.JButton();
        btnExcluir = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();
        btnSalvar = new javax.swing.JButton();
        btnSair = new javax.swing.JButton();
        btnEntrada = new javax.swing.JButton();
        btnAjustar = new javax.swing.JButton();
        txtCusto = new javax.swing.JLabel();
        CampoCusto = new javax.swing.JTextField();
        CampoQnt = new javax.swing.JSpinner();
        txtTipo = new javax.swing.JLabel();
        ComboBoxTipo = new javax.swing.JComboBox<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        tabelaItens.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Nome", "Quantidade", "Valor"
            }
        ));
        tabelaItens.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tabelaItensMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tabelaItens);

        txtTitle.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        txtTitle.setText("BLK BURGUER - Ingredientes/itens");

        TxtNome.setText("Nome:");

        txtQnt.setText("Quantidade:");

        jScrollPane2.setViewportView(CampoNome);

        btnNovo.setText("Novo");
        btnNovo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNovoActionPerformed(evt);
            }
        });

        btnEditar.setText("Editar");
        btnEditar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditarActionPerformed(evt);
            }
        });

        btnExcluir.setText("Excluir");
        btnExcluir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExcluirActionPerformed(evt);
            }
        });

        btnCancelar.setText("Cancelar");
        btnCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelarActionPerformed(evt);
            }
        });

        btnSalvar.setText("Salvar");
        btnSalvar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSalvarActionPerformed(evt);
            }
        });

        btnSair.setText("Voltar");
        btnSair.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSairActionPerformed(evt);
            }
        });

        btnEntrada.setText("Entrada");
        btnEntrada.setToolTipText("Registrar entrada de quantidade no item selecionado");
        btnEntrada.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEntradaActionPerformed(evt);
            }
        });

        btnAjustar.setText("Ajustar");
        btnAjustar.setToolTipText("Ajustar a quantidade do item selecionado informando o motivo");
        btnAjustar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAjustarActionPerformed(evt);
            }
        });

        txtCusto.setText("Custo unit: ");

        CampoCusto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CampoCustoActionPerformed(evt);
            }
        });

        txtTipo.setText("Tipo");

        ComboBoxTipo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Ingrediente", "Bebida", "Acompanhamento" }));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(48, 48, 48)
                .addComponent(btnNovo, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnEditar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnExcluir, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnEntrada, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnAjustar, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSalvar, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSair, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(48, 48, 48))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(136, 136, 136)
                        .addComponent(txtTitle))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(txtQnt)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(CampoQnt, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtTipo)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(ComboBoxTipo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(TxtNome, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 439, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtCusto)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(CampoCusto, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane1))))
                .addContainerGap(28, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(txtTitle)
                .addGap(36, 36, 36)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtCusto)
                        .addComponent(CampoCusto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(TxtNome))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtQnt)
                    .addComponent(CampoQnt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtTipo)
                    .addComponent(ComboBoxTipo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE)
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnExcluir, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSalvar, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSair, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEntrada, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAjustar, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnNovo, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEditar, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(17, 17, 17))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnSairActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSairActionPerformed
        new Main().setVisible(true);
        dispose();
    }//GEN-LAST:event_btnSairActionPerformed

    private void btnNovoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNovoActionPerformed
        limparCampos();
        validaCampos("novo");
    }//GEN-LAST:event_btnNovoActionPerformed

    private void btnEditarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditarActionPerformed
        validaCampos("editar");
    }//GEN-LAST:event_btnEditarActionPerformed

    private void btnExcluirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExcluirActionPerformed

        int confirm = javax.swing.JOptionPane.showConfirmDialog(null,
                "Excluir \"" + est.getNome() + "\"?",
                "Confirmar", javax.swing.JOptionPane.YES_NO_OPTION);
        if (confirm != javax.swing.JOptionPane.YES_OPTION) {
            return;
        }
        try {
            estoqueDAO.excluir(est.getNome());
            limparCampos();
            montaTabela();
            validaCampos("inicio");
            javax.swing.JOptionPane.showMessageDialog(null, "Excluído com sucesso!");
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(null, "Erro: " + e.getMessage());
        }

    }//GEN-LAST:event_btnExcluirActionPerformed

    private void btnSalvarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSalvarActionPerformed
        if (CampoNome.getText().trim().isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(null, "Informe o nome.");
            return;
        }
        double custo;
        try {
            custo = Double.parseDouble(CampoCusto.getText().trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            javax.swing.JOptionPane.showMessageDialog(null, "Informe um custo válido. Ex.: 2,50");
            return;
        }
        try {
            Estoque item = new Estoque();
            item.setNome(CampoNome.getText().trim());
            item.setPreco(custo);
            item.setQuantidade((Integer) CampoQnt.getValue());
            item.setTipo(ComboBoxTipo.getSelectedItem().toString());

            if (est.getNome() != null && !est.getNome().isEmpty()) {
                estoqueDAO.atualizarCadastro(est.getNome(), item);
            } else {
                if (item.getQuantidade() < 0) {
                    javax.swing.JOptionPane.showMessageDialog(null, "A quantidade não pode ser negativa.");
                    return;
                }
                estoqueDAO.inserir(item);
            }
            limparCampos();
            montaTabela();
            validaCampos("inicio");
            javax.swing.JOptionPane.showMessageDialog(null, "Salvo com sucesso!");
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(null, "Erro: " + e.getMessage());
        }
    }//GEN-LAST:event_btnSalvarActionPerformed

    private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
        limparCampos();
        validaCampos("inicio");
    }//GEN-LAST:event_btnCancelarActionPerformed

    private void tabelaItensMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tabelaItensMouseClicked

        int linha = tabelaItens.getSelectedRow();
        if (linha < 0) {
            return;
        }
        est = listaEstoque.get(linha);
        CampoNome.setText(est.getNome());
        CampoCusto.setText(String.valueOf(est.getPreco()));
        CampoQnt.setValue(est.getQuantidade());
        ComboBoxTipo.setSelectedItem(est.getTipo());
        validaCampos("selecionado");

    }//GEN-LAST:event_tabelaItensMouseClicked

    private void CampoCustoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CampoCustoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_CampoCustoActionPerformed

    private void btnEntradaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEntradaActionPerformed
        movimentarEstoque(false);
    }//GEN-LAST:event_btnEntradaActionPerformed

    private void btnAjustarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAjustarActionPerformed
        movimentarEstoque(true);
    }//GEN-LAST:event_btnAjustarActionPerformed

    /**
     * Abre a janela de entrada (ajuste = false) ou de ajuste (ajuste = true)
     * do item selecionado, mostrando a quantidade atual e o resultado antes
     * de confirmar.
     */
    private void movimentarEstoque(boolean ajuste) {
        if (est.getNome() == null || est.getNome().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecione um item na tabela.");
            return;
        }
        String nome = est.getNome();
        int atual;
        try {
            atual = estoqueDAO.quantidadeAtual(nome);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage());
            return;
        }

        javax.swing.JSpinner campoQtd = new javax.swing.JSpinner(ajuste
                ? new javax.swing.SpinnerNumberModel(atual, 0, Integer.MAX_VALUE, 1)
                : new javax.swing.SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
        javax.swing.JTextField campoMotivo = new javax.swing.JTextField(20);
        javax.swing.JLabel resultado = new javax.swing.JLabel();
        Runnable atualizarResultado = () -> {
            int valor = (Integer) campoQtd.getValue();
            int nova = ajuste ? valor : atual + valor;
            int diferenca = nova - atual;
            resultado.setText("Ficará com: " + nova + "  (" + (diferenca >= 0 ? "+" : "") + diferenca + ")");
        };
        campoQtd.addChangeListener(e -> atualizarResultado.run());
        atualizarResultado.run();

        javax.swing.JPanel painel = new javax.swing.JPanel(new java.awt.GridLayout(0, 1, 0, 6));
        painel.add(new javax.swing.JLabel("Item: " + nome));
        painel.add(new javax.swing.JLabel("Quantidade atual: " + atual));
        painel.add(new javax.swing.JLabel(ajuste ? "Quantidade correta (contagem):" : "Quantidade recebida:"));
        painel.add(campoQtd);
        if (ajuste) {
            painel.add(new javax.swing.JLabel("Motivo do ajuste:"));
            painel.add(campoMotivo);
        }
        painel.add(resultado);

        int opcao = JOptionPane.showConfirmDialog(this, painel,
                ajuste ? "Ajustar estoque" : "Entrada de estoque",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opcao != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            int valor = (Integer) campoQtd.getValue();
            int nova = ajuste
                    ? estoqueService.ajustar(nome, valor, campoMotivo.getText())
                    : estoqueService.registrarEntrada(nome, valor);
            limparCampos();
            montaTabela();
            validaCampos("inicio");
            JOptionPane.showMessageDialog(this, "Estoque de \"" + nome + "\" atualizado para " + nova + ".");
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Atenção", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage());
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
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
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new TelaEstoque().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField CampoCusto;
    private javax.swing.JTextPane CampoNome;
    private javax.swing.JSpinner CampoQnt;
    private javax.swing.JComboBox<String> ComboBoxTipo;
    private javax.swing.JLabel TxtNome;
    private javax.swing.JButton btnAjustar;
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnEditar;
    private javax.swing.JButton btnEntrada;
    private javax.swing.JButton btnExcluir;
    private javax.swing.JButton btnNovo;
    private javax.swing.JButton btnSair;
    private javax.swing.JButton btnSalvar;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable tabelaItens;
    private javax.swing.JLabel txtCusto;
    private javax.swing.JLabel txtQnt;
    private javax.swing.JLabel txtTipo;
    private javax.swing.JLabel txtTitle;
    // End of variables declaration//GEN-END:variables
}