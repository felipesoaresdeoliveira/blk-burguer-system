package telas;

import entidades.Estoque;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import ui.Tema;

public class TelaEstoque extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(TelaEstoque.class.getName());

    public TelaEstoque() {
        initComponents();
        aplicarVisual();
        validaCampos("inicio");
        montaTabela();
        Tema.tamanhoPadrao(this);
    }

    private void aplicarVisual() {
        Tema.janela(this);
        Tema.titulo(txtTitle);
        Tema.secundario(lblSubtitulo);
        Tema.primario(btnSalvar);
        Tema.perigo(btnExcluir);
        Tema.cartao(painelFormulario);
        Tema.transparente(painelCabecalho, painelTitulo, painelAcoesTopo, painelCorpo, painelLista, painelBusca,
                painelCampos, painelBotoes, chkSomenteBaixo);
        lblFormTitulo.setForeground(Tema.TEXTO);
        txtBusca.putClientProperty("JTextField.placeholderText", "nome do item");
        txtBusca.putClientProperty("JTextField.showClearButton", true);
        // Busca e filtros aplicados enquanto o usuário digita/escolhe (#22).
        txtBusca.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { montaTabela(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { montaTabela(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { montaTabela(); }
        });
        cmbFiltroTipo.addActionListener(e -> montaTabela());
        chkSomenteBaixo.addActionListener(e -> montaTabela());
    }

    /** true se o item passa pela busca e pelos filtros da tela. */
    private boolean passaNoFiltro(Estoque e) {
        String busca = txtBusca.getText().trim().toLowerCase();
        String tipo = String.valueOf(cmbFiltroTipo.getSelectedItem());
        return (busca.isEmpty() || e.getNome().toLowerCase().contains(busca))
                && ("Todos".equals(tipo) || tipo.equals(e.getTipo()))
                && (!chkSomenteBaixo.isSelected() || e.isEstoqueBaixo());
    }
    Estoque est = new Estoque();
    List<Estoque> listaEstoque = new ArrayList<>();
    private final dao.EstoqueDAO estoqueDAO = new dao.EstoqueDAO();
    private final service.EstoqueService estoqueService = new service.EstoqueService();

    public void limparCampos() {
        CampoNome.setText("");
        CampoCusto.setText("");
        CampoQnt.setValue(0);
        CampoMinimo.setValue(10);
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
            CampoMinimo.setEnabled(false);
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
            CampoMinimo.setEnabled(true);
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
            CampoMinimo.setEnabled(false);
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
                            new String[]{"Nome", "Tipo", "Quantidade", "Mínimo", "Situação", "Custo unit."}, 0) {
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            };
            listaEstoque = new ArrayList<>();
            for (Estoque e : estoqueDAO.listar()) {
                if (passaNoFiltro(e)) {
                    listaEstoque.add(e);
                }
            }
            for (Estoque e : listaEstoque) {
                modelo.addRow(new Object[]{
                    e.getNome(), e.getTipo(), e.getQuantidade(), e.getMinimo(),
                    e.getQuantidade() <= 0 ? "⚠ Sem estoque" : e.isEstoqueBaixo() ? "⚠ Baixo" : "OK",
                    String.format("R$ %.2f", e.getPreco())
                });
            }
            tabelaItens.setModel(modelo);
            tabelaItens.getColumnModel().getColumn(4).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
                @Override
                public java.awt.Component getTableCellRendererComponent(javax.swing.JTable t, Object valor,
                        boolean selecionado, boolean foco, int linha, int coluna) {
                    super.getTableCellRendererComponent(t, valor, selecionado, foco, linha, coluna);
                    String s = String.valueOf(valor);
                    setForeground(s.contains("Sem") ? Tema.PERIGO : s.contains("Baixo") ? Tema.AVISO : Tema.TEXTO_FRACO);
                    setFont(getFont().deriveFont(s.equals("OK") ? java.awt.Font.PLAIN : java.awt.Font.BOLD));
                    return this;
                }
            });
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        painelCabecalho = new javax.swing.JPanel();
        painelTitulo = new javax.swing.JPanel();
        txtTitle = new javax.swing.JLabel();
        lblSubtitulo = new javax.swing.JLabel();
        painelAcoesTopo = new javax.swing.JPanel();
        btnSair = new javax.swing.JButton();
        painelCorpo = new javax.swing.JPanel();
        painelLista = new javax.swing.JPanel();
        painelBusca = new javax.swing.JPanel();
        lblBusca = new javax.swing.JLabel();
        txtBusca = new javax.swing.JTextField();
        lblFiltroTipo = new javax.swing.JLabel();
        cmbFiltroTipo = new javax.swing.JComboBox<>();
        chkSomenteBaixo = new javax.swing.JCheckBox();
        btnLimparFiltro = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabelaItens = new javax.swing.JTable();
        painelFormulario = new javax.swing.JPanel();
        painelCampos = new javax.swing.JPanel();
        lblFormTitulo = new javax.swing.JLabel();
        TxtNome = new javax.swing.JLabel();
        CampoNome = new javax.swing.JTextField();
        txtCusto = new javax.swing.JLabel();
        CampoCusto = new javax.swing.JTextField();
        txtQnt = new javax.swing.JLabel();
        CampoQnt = new javax.swing.JSpinner();
        txtMinimo = new javax.swing.JLabel();
        CampoMinimo = new javax.swing.JSpinner();
        txtTipo = new javax.swing.JLabel();
        ComboBoxTipo = new javax.swing.JComboBox<>();
        painelBotoes = new javax.swing.JPanel();
        btnNovo = new javax.swing.JButton();
        btnEditar = new javax.swing.JButton();
        btnSalvar = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();
        btnEntrada = new javax.swing.JButton();
        btnAjustar = new javax.swing.JButton();
        btnExcluir = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("BLK Burguer - Estoque");
        setMinimumSize(new java.awt.Dimension(1180, 720));

        painelCabecalho.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 24, 14, 24));
        painelCabecalho.setLayout(new java.awt.BorderLayout());
        painelTitulo.setLayout(new java.awt.GridLayout(2, 1, 0, 2));
        txtTitle.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        txtTitle.setText("Estoque");
        painelTitulo.add(txtTitle);

        lblSubtitulo.setText("Ingredientes, bebidas e acompanhamentos");
        painelTitulo.add(lblSubtitulo);

        painelCabecalho.add(painelTitulo, java.awt.BorderLayout.LINE_START);

        painelAcoesTopo.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 8));
        btnSair.setText("Voltar");
        btnSair.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSairActionPerformed(evt);
            }
        });
        painelAcoesTopo.add(btnSair);

        painelCabecalho.add(painelAcoesTopo, java.awt.BorderLayout.LINE_END);

        getContentPane().add(painelCabecalho, java.awt.BorderLayout.PAGE_START);

        painelCorpo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 24, 24, 24));
        painelCorpo.setLayout(new java.awt.BorderLayout(20, 16));
        painelLista.setLayout(new java.awt.BorderLayout(0, 10));
        painelBusca.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        lblBusca.setText("Buscar:");
        painelBusca.add(lblBusca);

        txtBusca.setPreferredSize(new java.awt.Dimension(260, 30));
        painelBusca.add(txtBusca);

        lblFiltroTipo.setText("Tipo:");
        painelBusca.add(lblFiltroTipo);

        cmbFiltroTipo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Todos", "Ingrediente", "Bebida", "Acompanhamento" }));
        painelBusca.add(cmbFiltroTipo);

        chkSomenteBaixo.setText("Só estoque baixo");
        painelBusca.add(chkSomenteBaixo);

        btnLimparFiltro.setText("Limpar");
        btnLimparFiltro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLimparFiltroActionPerformed(evt);
            }
        });
        painelBusca.add(btnLimparFiltro);

        painelLista.add(painelBusca, java.awt.BorderLayout.PAGE_START);

        tabelaItens.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tabelaItensMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tabelaItens);
        painelLista.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        painelCorpo.add(painelLista, java.awt.BorderLayout.CENTER);

        painelFormulario.setPreferredSize(new java.awt.Dimension(360, 0));
        painelFormulario.setLayout(new java.awt.BorderLayout(0, 14));
        painelCampos.setLayout(new java.awt.GridLayout(0, 1, 0, 4));
        lblFormTitulo.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        lblFormTitulo.setText("Cadastro do item");
        painelCampos.add(lblFormTitulo);

        TxtNome.setText("Nome");
        painelCampos.add(TxtNome);

        painelCampos.add(CampoNome);

        txtCusto.setText("Custo unitário (R$)");
        painelCampos.add(txtCusto);

        CampoCusto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CampoCustoActionPerformed(evt);
            }
        });
        painelCampos.add(CampoCusto);

        txtQnt.setText("Quantidade em estoque");
        painelCampos.add(txtQnt);

        painelCampos.add(CampoQnt);

        txtMinimo.setText("Mínimo para alerta");
        painelCampos.add(txtMinimo);

        CampoMinimo.setModel(new javax.swing.SpinnerNumberModel(10, 0, null, 1));
        CampoMinimo.setToolTipText("Abaixo desta quantidade o item aparece como estoque baixo");
        painelCampos.add(CampoMinimo);

        txtTipo.setText("Tipo");
        painelCampos.add(txtTipo);

        ComboBoxTipo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Ingrediente", "Bebida", "Acompanhamento" }));
        painelCampos.add(ComboBoxTipo);

        painelFormulario.add(painelCampos, java.awt.BorderLayout.PAGE_START);

        painelBotoes.setLayout(new java.awt.GridLayout(0, 2, 8, 8));
        btnNovo.setText("Novo");
        btnNovo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNovoActionPerformed(evt);
            }
        });
        painelBotoes.add(btnNovo);

        btnEditar.setText("Editar");
        btnEditar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditarActionPerformed(evt);
            }
        });
        painelBotoes.add(btnEditar);

        btnSalvar.setText("Salvar");
        btnSalvar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSalvarActionPerformed(evt);
            }
        });
        painelBotoes.add(btnSalvar);

        btnCancelar.setText("Cancelar");
        btnCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelarActionPerformed(evt);
            }
        });
        painelBotoes.add(btnCancelar);

        btnEntrada.setText("Entrada");
        btnEntrada.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEntradaActionPerformed(evt);
            }
        });
        painelBotoes.add(btnEntrada);

        btnAjustar.setText("Ajustar");
        btnAjustar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAjustarActionPerformed(evt);
            }
        });
        painelBotoes.add(btnAjustar);

        btnExcluir.setText("Excluir");
        btnExcluir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExcluirActionPerformed(evt);
            }
        });
        painelBotoes.add(btnExcluir);

        painelFormulario.add(painelBotoes, java.awt.BorderLayout.PAGE_END);

        painelCorpo.add(painelFormulario, java.awt.BorderLayout.LINE_END);

        getContentPane().add(painelCorpo, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents


    private void btnLimparFiltroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimparFiltroActionPerformed
        txtBusca.setText("");
        cmbFiltroTipo.setSelectedIndex(0);
        chkSomenteBaixo.setSelected(false);
        montaTabela();
    }//GEN-LAST:event_btnLimparFiltroActionPerformed

    private void btnSairActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSairActionPerformed
        Main.voltar(this);
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
            item.setMinimo((Integer) CampoMinimo.getValue());

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
        CampoMinimo.setValue(est.getMinimo());
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
    private javax.swing.JSpinner CampoMinimo;
    private javax.swing.JTextField CampoNome;
    private javax.swing.JSpinner CampoQnt;
    private javax.swing.JComboBox<String> ComboBoxTipo;
    private javax.swing.JLabel TxtNome;
    private javax.swing.JButton btnAjustar;
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnEditar;
    private javax.swing.JButton btnEntrada;
    private javax.swing.JButton btnExcluir;
    private javax.swing.JButton btnLimparFiltro;
    private javax.swing.JButton btnNovo;
    private javax.swing.JButton btnSair;
    private javax.swing.JButton btnSalvar;
    private javax.swing.JCheckBox chkSomenteBaixo;
    private javax.swing.JComboBox<String> cmbFiltroTipo;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblBusca;
    private javax.swing.JLabel lblFiltroTipo;
    private javax.swing.JLabel lblFormTitulo;
    private javax.swing.JLabel lblSubtitulo;
    private javax.swing.JPanel painelAcoesTopo;
    private javax.swing.JPanel painelBotoes;
    private javax.swing.JPanel painelBusca;
    private javax.swing.JPanel painelCabecalho;
    private javax.swing.JPanel painelCampos;
    private javax.swing.JPanel painelCorpo;
    private javax.swing.JPanel painelFormulario;
    private javax.swing.JPanel painelLista;
    private javax.swing.JPanel painelTitulo;
    private javax.swing.JTable tabelaItens;
    private javax.swing.JTextField txtBusca;
    private javax.swing.JLabel txtCusto;
    private javax.swing.JLabel txtMinimo;
    private javax.swing.JLabel txtQnt;
    private javax.swing.JLabel txtTipo;
    private javax.swing.JLabel txtTitle;
    // End of variables declaration//GEN-END:variables
}