package telas;

import entidades.Estoque;
import entidades.Produto;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;

public class TelaProdutos extends javax.swing.JFrame {

    private final dao.ProdutoDAO produtoDAO = new dao.ProdutoDAO();
    private final dao.EstoqueDAO estoqueDAO = new dao.EstoqueDAO();
    private List<Estoque> listaIngredientes = new ArrayList<>();
    /** Itens exibidos no combo de ingredientes, conforme o tipo do produto. */
    private final List<Estoque> opcoesIngrediente = new ArrayList<>();
    private List<Produto> listaProdutos = new ArrayList<>();
    /** Ficha técnica em edição: ingrediente -> quantidade por unidade do produto. */
    private final Map<String, Integer> composicao = new LinkedHashMap<>();
    private int produtoSelecionadoId = -1;

    public void limparCampos() {
        campoNome.setText("");
        campoPvenda.setText("");
        ComboboxTipo.setSelectedIndex(0);
        composicao.clear();
        produtoSelecionadoId = -1;
        atualizarTabelaIngredientes();
    }

    public void validaCampos(String op) {
        boolean editando = op.equals("novo");
        campoNome.setEnabled(editando);
        campoPvenda.setEnabled(editando);
        ComboboxTipo.setEnabled(editando);
        cBoxAddIngrediente.setEnabled(editando);
        jTextField1.setEnabled(editando);
        btnAddIngrediente.setEnabled(editando);
        btnRemoverIngrediente.setEnabled(editando);
        btnSalvar.setEnabled(editando);
        btnNovo.setEnabled(!editando);
        btnEditar.setEnabled(op.equals("selecionado"));
        btnExcluir.setEnabled(op.equals("selecionado"));
        btnCancelar.setEnabled(!op.equals("inicio"));
        btnSair.setEnabled(op.equals("inicio"));
    }

    public void carregarIngredientes() {
        try {
            listaIngredientes = estoqueDAO.listar();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro: " + e.getMessage());
        }
        filtrarIngredientes();
    }

    /**
     * Itens de estoque que podem compor um produto do tipo informado: lanche usa
     * ingredientes, bebida usa bebidas (venda direta) e acompanhamento usa
     * ingredientes ou acompanhamentos.
     */
    private static boolean podeCompor(String tipoProduto, String tipoItem) {
        if ("Bebida".equals(tipoProduto)) {
            return Estoque.BEBIDA.equals(tipoItem);
        }
        if ("Acompanhamento".equals(tipoProduto)) {
            return !Estoque.BEBIDA.equals(tipoItem);
        }
        return Estoque.INGREDIENTE.equals(tipoItem);
    }

    private void filtrarIngredientes() {
        cBoxAddIngrediente.removeAllItems();
        opcoesIngrediente.clear();
        String tipoProduto = String.valueOf(ComboboxTipo.getSelectedItem());
        for (Estoque e : listaIngredientes) {
            if (podeCompor(tipoProduto, e.getTipo())) {
                opcoesIngrediente.add(e);
                cBoxAddIngrediente.addItem(e.getNome());
            }
        }
    }

    private double custoUnitario(String ingrediente) {
        for (Estoque e : listaIngredientes) {
            if (e.getNome().equals(ingrediente)) {
                return e.getPreco();
            }
        }
        return 0;
    }

    private double custoDoProduto() {
        double custo = 0;
        for (Map.Entry<String, Integer> ing : composicao.entrySet()) {
            custo += custoUnitario(ing.getKey()) * ing.getValue();
        }
        return custo;
    }

    /** Preço digitado, aceitando vírgula ou ponto; null se inválido. */
    private Double precoDigitado() {
        try {
            return Double.valueOf(campoPvenda.getText().trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public void atualizarTabelaIngredientes() {
        javax.swing.table.DefaultTableModel modelo
                = new javax.swing.table.DefaultTableModel(
                        new String[]{"Ingrediente", "Qnt", "Custo unit.", "Subtotal"}, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        for (Map.Entry<String, Integer> ing : composicao.entrySet()) {
            double unit = custoUnitario(ing.getKey());
            modelo.addRow(new Object[]{
                ing.getKey(), ing.getValue(),
                String.format("R$ %.2f", unit),
                String.format("R$ %.2f", unit * ing.getValue())
            });
        }
        tabelaIngredientesProd.setModel(modelo);
        atualizarResumo();
    }

    /** Atualiza custo, lucro por unidade, margem % e o aviso de preço abaixo do custo. */
    private void atualizarResumo() {
        double custo = custoDoProduto();
        valorCEstimado.setText(String.format("%.2f", custo));
        Double preco = precoDigitado();
        if (preco == null || preco <= 0) {
            txtValorMargem.setText("-");
            lblMargemPercentual.setText("");
            lblAvisoPreco.setText(" ");
            return;
        }
        double lucro = preco - custo;
        java.awt.Color cor = lucro < 0 ? new java.awt.Color(204, 0, 0) : new java.awt.Color(0, 153, 51);
        txtValorMargem.setForeground(cor);
        lblMargemPercentual.setForeground(cor);
        txtValorMargem.setText(String.format("%.2f", lucro));
        lblMargemPercentual.setText(String.format("(%.1f%%)", lucro / preco * 100));
        lblAvisoPreco.setText(preco < custo
                ? "Atenção: preço de venda abaixo do custo!"
                : " ");
    }

    public void montaTabelaProdutos() {
        try {
            javax.swing.table.DefaultTableModel modelo
                    = new javax.swing.table.DefaultTableModel(
                            new String[]{"Produto", "Tipo", "Preço"}, 0) {
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            };
            listaProdutos = produtoDAO.listar();
            for (Produto p : listaProdutos) {
                modelo.addRow(new Object[]{
                    p.getNome(), p.getTipo(), String.format("R$ %.2f", p.getPreco())
                });
            }
            tabelaProdutos.setModel(modelo);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro: " + e.getMessage());
        }
    }

    public TelaProdutos() {
        initComponents();
        setLocationRelativeTo(null);
        ComboboxTipo.addActionListener(e -> filtrarIngredientes());
        carregarIngredientes();
        montaTabelaProdutos();
        atualizarTabelaIngredientes();
        campoPvenda.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                atualizarResumo();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                atualizarResumo();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                atualizarResumo();
            }
        });
        validaCampos("inicio");
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        txtTitle = new javax.swing.JLabel();
        txtSubititulo = new javax.swing.JLabel();
        txtNome = new javax.swing.JLabel();
        campoNome = new javax.swing.JTextField();
        txtPvenda = new javax.swing.JLabel();
        campoPvenda = new javax.swing.JTextField();
        txtTipo = new javax.swing.JLabel();
        ComboboxTipo = new javax.swing.JComboBox<>();
        jLabel1 = new javax.swing.JLabel();
        cBoxAddIngrediente = new javax.swing.JComboBox<>();
        txtQnt = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        txtSubtitulo2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tabelaIngredientesProd = new javax.swing.JTable();
        txtCustoEstimado = new javax.swing.JLabel();
        valorCEstimado = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtMargem = new javax.swing.JLabel();
        txtValorMargem = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tabelaProdutos = new javax.swing.JTable();
        btnNovo = new javax.swing.JButton();
        btnEditar = new javax.swing.JButton();
        btnExcluir = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();
        btnSalvar = new javax.swing.JButton();
        btnSair = new javax.swing.JButton();
        btnAddIngrediente = new javax.swing.JButton();
        btnRemoverIngrediente = new javax.swing.JButton();
        lblMargemPercentual = new javax.swing.JLabel();
        lblAvisoPreco = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        txtTitle.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        txtTitle.setText("BLK BURGUER - Produto");

        txtSubititulo.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        txtSubititulo.setText("Dados do produto");

        txtNome.setText("Nome: ");

        txtPvenda.setText("Preço venda: ");

        txtTipo.setText("Tipo: ");

        ComboboxTipo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Lanche", "Bebida", "Acompanhamento" }));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel1.setText("Adicionar ingrediente");

        cBoxAddIngrediente.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        txtQnt.setText("Qnt: ");

        txtSubtitulo2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        txtSubtitulo2.setText("Ingredientes do produto");

        tabelaIngredientesProd.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Ingrediente", "Qnt"
            }
        ));
        jScrollPane2.setViewportView(tabelaIngredientesProd);

        txtCustoEstimado.setText("Custo: R$");

        valorCEstimado.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        valorCEstimado.setForeground(new java.awt.Color(51, 51, 51));
        valorCEstimado.setText("00,00");

        jLabel2.setText("|");

        txtMargem.setText("Lucro: R$");

        txtValorMargem.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        txtValorMargem.setForeground(new java.awt.Color(0, 153, 51));
        txtValorMargem.setText("00,00");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel3.setText("Produtos cadastrados ");

        tabelaProdutos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Produto", "Tipo", "Preço"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tabelaProdutos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tabelaProdutosMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(tabelaProdutos);

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

        btnAddIngrediente.setText("Adicionar ingrediente");
        btnAddIngrediente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddIngredienteActionPerformed(evt);
            }
        });

        btnRemoverIngrediente.setText("Remover ingrediente selecionado");
        btnRemoverIngrediente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoverIngredienteActionPerformed(evt);
            }
        });

        lblMargemPercentual.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblMargemPercentual.setText("(0,0%)");

        lblAvisoPreco.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblAvisoPreco.setForeground(new java.awt.Color(204, 0, 0));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtSubtitulo2)
                            .addComponent(jLabel1)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(txtTipo)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(ComboboxTipo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(txtNome)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(campoNome, javax.swing.GroupLayout.PREFERRED_SIZE, 252, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(txtSubititulo)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(txtPvenda)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(campoPvenda))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(cBoxAddIngrediente, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtQnt)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField1))
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(txtCustoEstimado)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(valorCEstimado)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtMargem)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtValorMargem)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblMargemPercentual))
                            .addComponent(btnAddIngrediente, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnRemoverIngrediente, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblAvisoPreco, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(42, 42, 42)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 292, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnNovo, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnEditar, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnExcluir, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnSalvar, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnSair, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(148, 148, 148)
                        .addComponent(txtTitle)))
                .addContainerGap(38, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addComponent(txtTitle)
                .addGap(40, 40, 40)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtSubititulo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnExcluir, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnNovo, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnEditar, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnSalvar, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnSair, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtNome)
                            .addComponent(campoNome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtPvenda)
                            .addComponent(campoPvenda, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtTipo)
                            .addComponent(ComboboxTipo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(12, 12, 12)
                        .addComponent(jLabel1)
                        .addGap(1, 1, 1)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cBoxAddIngrediente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtQnt)
                            .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnAddIngrediente)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtSubtitulo2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRemoverIngrediente)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtCustoEstimado)
                    .addComponent(valorCEstimado)
                    .addComponent(jLabel2)
                    .addComponent(txtMargem)
                    .addComponent(txtValorMargem)
                    .addComponent(lblMargemPercentual))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblAvisoPreco, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(19, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnNovoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNovoActionPerformed
        limparCampos();
        validaCampos("novo");
    }//GEN-LAST:event_btnNovoActionPerformed

    private void btnEditarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditarActionPerformed
        validaCampos("novo");
    }//GEN-LAST:event_btnEditarActionPerformed

    private void btnExcluirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExcluirActionPerformed
        if (produtoSelecionadoId < 0) {
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(null,
                "Excluir este produto?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            produtoDAO.excluir(produtoSelecionadoId);
            JOptionPane.showMessageDialog(null, "Produto excluído!");
            limparCampos();
            montaTabelaProdutos();
            validaCampos("inicio");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro: " + e.getMessage());
        }
    }//GEN-LAST:event_btnExcluirActionPerformed

    private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
        limparCampos();
        validaCampos("inicio");
    }//GEN-LAST:event_btnCancelarActionPerformed

    private void btnSalvarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSalvarActionPerformed
        if (campoNome.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Informe o nome do produto.");
            return;
        }
        Double preco = precoDigitado();
        if (preco == null || preco < 0) {
            JOptionPane.showMessageDialog(null, "Informe um preço de venda válido. Ex.: 25,90");
            return;
        }
        double custo = custoDoProduto();
        if (preco < custo) {
            int confirm = JOptionPane.showConfirmDialog(null,
                    String.format("O preço de venda (R$ %.2f) está abaixo do custo (R$ %.2f).%nSalvar mesmo assim?", preco, custo),
                    "Preço abaixo do custo", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }
        try {
            Produto produto = new Produto();
            produto.setId(produtoSelecionadoId);
            produto.setNome(campoNome.getText().trim());
            produto.setTipo(ComboboxTipo.getSelectedItem().toString());
            produto.setPreco(preco);
            produtoDAO.salvar(produto, composicao);
            JOptionPane.showMessageDialog(null, "Salvo com sucesso!");
            limparCampos();
            montaTabelaProdutos();
            validaCampos("inicio");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro: " + e.getMessage());
        }
    }//GEN-LAST:event_btnSalvarActionPerformed

    private void btnSairActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSairActionPerformed
        new telas.Main().setVisible(true);
        dispose();
    }//GEN-LAST:event_btnSairActionPerformed

    private void btnAddIngredienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddIngredienteActionPerformed
        if (cBoxAddIngrediente.getSelectedIndex() < 0) {
            return;
        }
        try {
            int qtd = Integer.parseInt(jTextField1.getText().trim());
            if (qtd <= 0) {
                throw new NumberFormatException();
            }
            String nome = opcoesIngrediente.get(cBoxAddIngrediente.getSelectedIndex()).getNome();
            // Ingrediente repetido soma na quantidade existente.
            composicao.merge(nome, qtd, Integer::sum);
            atualizarTabelaIngredientes();
            jTextField1.setText("");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Informe uma quantidade válida.");
        }
    }//GEN-LAST:event_btnAddIngredienteActionPerformed

    private void btnRemoverIngredienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoverIngredienteActionPerformed
        int linha = tabelaIngredientesProd.getSelectedRow();
        if (linha < 0) {
            JOptionPane.showMessageDialog(null, "Selecione um ingrediente na tabela para remover.");
            return;
        }
        composicao.remove(tabelaIngredientesProd.getValueAt(linha, 0).toString());
        atualizarTabelaIngredientes();
    }//GEN-LAST:event_btnRemoverIngredienteActionPerformed

    private void tabelaProdutosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tabelaProdutosMouseClicked
        int linha = tabelaProdutos.getSelectedRow();
        if (linha < 0) {
            return;
        }
        try {
            Produto p = listaProdutos.get(linha);
            produtoSelecionadoId = p.getId();
            campoNome.setText(p.getNome());
            campoPvenda.setText(String.format("%.2f", p.getPreco()));
            ComboboxTipo.setSelectedItem(p.getTipo());
            composicao.clear();
            composicao.putAll(produtoDAO.ingredientes(p.getId()));
            atualizarTabelaIngredientes();
            validaCampos("selecionado");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro: " + e.getMessage());
        }
    }//GEN-LAST:event_tabelaProdutosMouseClicked

    public static void main(String args[]) {

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TelaProdutos().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> ComboboxTipo;
    private javax.swing.JButton btnAddIngrediente;
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnEditar;
    private javax.swing.JButton btnExcluir;
    private javax.swing.JButton btnNovo;
    private javax.swing.JButton btnRemoverIngrediente;
    private javax.swing.JButton btnSair;
    private javax.swing.JButton btnSalvar;
    private javax.swing.JComboBox<String> cBoxAddIngrediente;
    private javax.swing.JTextField campoNome;
    private javax.swing.JTextField campoPvenda;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JLabel lblAvisoPreco;
    private javax.swing.JLabel lblMargemPercentual;
    private javax.swing.JTable tabelaIngredientesProd;
    private javax.swing.JTable tabelaProdutos;
    private javax.swing.JLabel txtCustoEstimado;
    private javax.swing.JLabel txtMargem;
    private javax.swing.JLabel txtNome;
    private javax.swing.JLabel txtPvenda;
    private javax.swing.JLabel txtQnt;
    private javax.swing.JLabel txtSubititulo;
    private javax.swing.JLabel txtSubtitulo2;
    private javax.swing.JLabel txtTipo;
    private javax.swing.JLabel txtTitle;
    private javax.swing.JLabel txtValorMargem;
    private javax.swing.JLabel valorCEstimado;
    // End of variables declaration//GEN-END:variables
}