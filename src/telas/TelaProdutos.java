package telas;

import entidades.Estoque;

public class TelaProdutos extends javax.swing.JFrame {

    private java.util.List<entidades.Estoque> listaIngredientes = new java.util.ArrayList<>();
    private java.util.List<entidades.Estoque> ingredientesDoProduto = new java.util.ArrayList<>();
    private java.util.List<Integer> quantidadesDoProduto = new java.util.ArrayList<>();
    private int produtoSelecionadoId = -1;

    private java.sql.Connection getConnection() throws Exception {
        Class.forName("org.postgresql.Driver");
        return java.sql.DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/BLKburguer", "postgres", "2627");
    }

    public void limparCampos() {
        campoNome.setText("");
        campoPvenda.setText("");
        ComboboxTipo.setSelectedIndex(0);
        ingredientesDoProduto.clear();
        quantidadesDoProduto.clear();
        produtoSelecionadoId = -1;
        atualizarTabelaIngredientes();
    }

    public void validaCampos(String op) {
        if (op.equals("inicio")) {
            campoNome.setEnabled(false);
            campoPvenda.setEnabled(false);
            ComboboxTipo.setEnabled(false);
            cBoxAddIngrediente.setEnabled(false);
            jTextField1.setEnabled(false);
            btnNovo.setEnabled(true);
            btnEditar.setEnabled(false);
            btnExcluir.setEnabled(false);
            btnSalvar.setEnabled(false);
            btnCancelar.setEnabled(false);
            btnSair.setEnabled(true);
        } else if (op.equals("novo")) {
            campoNome.setEnabled(true);
            campoPvenda.setEnabled(true);
            ComboboxTipo.setEnabled(true);
            cBoxAddIngrediente.setEnabled(true);
            jTextField1.setEnabled(true);
            btnNovo.setEnabled(false);
            btnEditar.setEnabled(false);
            btnExcluir.setEnabled(false);
            btnSalvar.setEnabled(true);
            btnCancelar.setEnabled(true);
            btnSair.setEnabled(false);
        } else if (op.equals("selecionado")) {
            campoNome.setEnabled(false);
            campoPvenda.setEnabled(false);
            ComboboxTipo.setEnabled(false);
            cBoxAddIngrediente.setEnabled(false);
            jTextField1.setEnabled(false);
            btnNovo.setEnabled(true);
            btnEditar.setEnabled(true);
            btnExcluir.setEnabled(true);
            btnSalvar.setEnabled(false);
            btnCancelar.setEnabled(true);
            btnSair.setEnabled(false);
        }
    }

    public void carregarIngredientes() {
        listaIngredientes.clear();
        cBoxAddIngrediente.removeAllItems();
        try (java.sql.Connection con = getConnection(); java.sql.Statement stm = con.createStatement(); java.sql.ResultSet rs = stm.executeQuery("SELECT * FROM \"Estoque\"")) {
            while (rs.next()) {
                entidades.Estoque e = new entidades.Estoque();
                e.setNome(rs.getString("nome"));
                e.setPreco(rs.getDouble("valor"));
                e.setQuantidade(rs.getInt("Quantidade"));
                listaIngredientes.add(e);
                cBoxAddIngrediente.addItem(e.getNome());
            }
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(null, "Erro: " + e.getMessage());
        }
    }

    public void atualizarTabelaIngredientes() {
        javax.swing.table.DefaultTableModel modelo
                = new javax.swing.table.DefaultTableModel(
                        new String[]{"Ingrediente", "Qnt"}, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        double custo = 0;
        for (int i = 0; i < ingredientesDoProduto.size(); i++) {
            entidades.Estoque e = ingredientesDoProduto.get(i);
            int qtd = quantidadesDoProduto.get(i);
            custo += e.getPreco() * qtd;
            modelo.addRow(new Object[]{e.getNome(), qtd});
        }
        tabelaIngredientesProd.setModel(modelo);
        valorCEstimado.setText(String.format("%.2f", custo));
        try {
            double pvenda = Double.parseDouble(campoPvenda.getText().trim());
            txtValorMargem.setText(String.format("%.2f", pvenda - custo));
        } catch (Exception ex) {
            txtValorMargem.setText("0,00");
        }
    }

    public void montaTabelaProdutos() {
        try (java.sql.Connection con = getConnection(); java.sql.Statement stm = con.createStatement(); java.sql.ResultSet rs = stm.executeQuery(
                "SELECT id, nome, tipo, preco FROM public.\"Produto\"")) {

            javax.swing.table.DefaultTableModel modelo
                    = new javax.swing.table.DefaultTableModel(
                            new String[]{"Produto", "Tipo", "Preço"}, 0) {
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            };
            while (rs.next()) {
                modelo.addRow(new Object[]{
                    rs.getString("nome"),
                    rs.getString("tipo"),
                    String.format("R$ %.2f", rs.getDouble("preco"))
                });
            }
            tabelaProdutos.setModel(modelo);
            tabelaProdutos.repaint();

        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(null, "Erro: " + e.getMessage());
        }
    }

    public void montaTabelaIngredientes() {
        try (java.sql.Connection con = getConnection(); java.sql.Statement stm = con.createStatement(); java.sql.ResultSet rs = stm.executeQuery("SELECT * FROM \"Estoque\"")) {

            javax.swing.table.DefaultTableModel modelo
                    = new javax.swing.table.DefaultTableModel(
                            new String[]{"Ingrediente", "Qnt"}, 0) {
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            };
            while (rs.next()) {
                modelo.addRow(new Object[]{
                    rs.getString("nome"),
                    rs.getInt("Quantidade")
                });
            }
            tabelaIngredientesProd.setModel(modelo);

        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(null, "Erro: " + e.getMessage());
        }
    }

    public TelaProdutos() {
        initComponents();
        setLocationRelativeTo(null);
        carregarIngredientes();
        montaTabelaProdutos();
        montaTabelaIngredientes();
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
        txtSubtitulo2.setText("ingedientes do produto");

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

        txtCustoEstimado.setText("Custo estimado: R$");

        valorCEstimado.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        valorCEstimado.setForeground(new java.awt.Color(255, 255, 51));
        valorCEstimado.setText("00,00");

        jLabel2.setText("----");

        txtMargem.setText("Margem: R$");

        txtValorMargem.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        txtValorMargem.setForeground(new java.awt.Color(0, 255, 102));
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
                                .addComponent(txtValorMargem))
                            .addComponent(btnAddIngrediente, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtCustoEstimado)
                    .addComponent(valorCEstimado)
                    .addComponent(jLabel2)
                    .addComponent(txtMargem)
                    .addComponent(txtValorMargem))
                .addContainerGap(35, Short.MAX_VALUE))
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
        int confirm = javax.swing.JOptionPane.showConfirmDialog(null,
                "Excluir este produto?", "Confirmar", javax.swing.JOptionPane.YES_NO_OPTION);
        if (confirm != javax.swing.JOptionPane.YES_OPTION) {
            return;
        }
        try (java.sql.Connection con = getConnection()) {
            java.sql.Statement stm = con.createStatement();
            stm.execute("DELETE FROM \"ProdutoIngrediente\" WHERE produto_id = " + produtoSelecionadoId);
            stm.execute("DELETE FROM \"Produto\" WHERE id = " + produtoSelecionadoId);
            javax.swing.JOptionPane.showMessageDialog(null, "Produto excluído!");
            limparCampos();
            montaTabelaProdutos();
            montaTabelaIngredientes();
            limparCampos();
            validaCampos("inicio");
            validaCampos("inicio");
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(null, "Erro: " + e.getMessage());
        }
    }//GEN-LAST:event_btnExcluirActionPerformed

    private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
        limparCampos();
        validaCampos("inicio");
    }//GEN-LAST:event_btnCancelarActionPerformed

    private void btnSalvarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSalvarActionPerformed
        if (campoNome.getText().trim().isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(null, "Informe o nome do produto.");
            return;
        }
        try (java.sql.Connection con = getConnection()) {
            con.setAutoCommit(false);
            java.sql.Statement stm = con.createStatement();
            String nome = campoNome.getText().trim();
            String tipo = ComboboxTipo.getSelectedItem().toString();
            double preco = Double.parseDouble(campoPvenda.getText().trim());

            if (produtoSelecionadoId > 0) {
                // EDITAR
                stm.execute("UPDATE \"Produto\" SET nome = '" + nome
                        + "', tipo = '" + tipo + "', preco = " + preco
                        + " WHERE id = " + produtoSelecionadoId);
                stm.execute("DELETE FROM \"ProdutoIngrediente\" WHERE produto_id = "
                        + produtoSelecionadoId);
                for (int i = 0; i < ingredientesDoProduto.size(); i++) {
                    stm.execute("INSERT INTO \"ProdutoIngrediente\" (produto_id, ingrediente_nome, quantidade) VALUES ("
                            + produtoSelecionadoId + ", '"
                            + ingredientesDoProduto.get(i).getNome() + "', "
                            + quantidadesDoProduto.get(i) + ")");
                }
            } else {
                // NOVO
                stm.execute("INSERT INTO \"Produto\" (nome, tipo, preco) VALUES ('"
                        + nome + "', '" + tipo + "', " + preco + ")");
                java.sql.ResultSet rs = stm.executeQuery(
                        "SELECT id FROM \"Produto\" WHERE nome = '" + nome + "' ORDER BY id DESC LIMIT 1");
                if (rs.next()) {
                    int prodId = rs.getInt("id");
                    for (int i = 0; i < ingredientesDoProduto.size(); i++) {
                        stm.execute("INSERT INTO \"ProdutoIngrediente\" (produto_id, ingrediente_nome, quantidade) VALUES ("
                                + prodId + ", '"
                                + ingredientesDoProduto.get(i).getNome() + "', "
                                + quantidadesDoProduto.get(i) + ")");
                    }
                }
            }
            con.commit();
            javax.swing.JOptionPane.showMessageDialog(null, "Salvo com sucesso!");
            limparCampos();
            montaTabelaProdutos();
            montaTabelaIngredientes();
            limparCampos();
            validaCampos("inicio");
            validaCampos("inicio");
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(null, "Erro: " + e.getMessage());
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
            entidades.Estoque selecionado = listaIngredientes.get(cBoxAddIngrediente.getSelectedIndex());
            ingredientesDoProduto.add(selecionado);
            quantidadesDoProduto.add(qtd);
            atualizarTabelaIngredientes();
            jTextField1.setText("");
        } catch (NumberFormatException ex) {
            javax.swing.JOptionPane.showMessageDialog(null, "Informe uma quantidade válida.");
        }
    }//GEN-LAST:event_btnAddIngredienteActionPerformed

    private void tabelaProdutosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tabelaProdutosMouseClicked
        int linha = tabelaProdutos.getSelectedRow();
        if (linha < 0) {
            return;
        }
        try {
            java.sql.Connection con = getConnection();
            java.sql.Statement stm = con.createStatement();

            String nomeProd = tabelaProdutos.getValueAt(linha, 0).toString();

            java.sql.ResultSet rs = stm.executeQuery(
                    "SELECT * FROM \"Produto\" WHERE nome = '" + nomeProd + "'");
            if (rs.next()) {
                produtoSelecionadoId = rs.getInt("id");
                campoNome.setText(rs.getString("nome"));
                campoPvenda.setText(String.valueOf(rs.getDouble("preco")));
                ComboboxTipo.setSelectedItem(rs.getString("tipo"));
            }
            stm.close();

            ingredientesDoProduto.clear();
            quantidadesDoProduto.clear();

            java.sql.Statement stm2 = con.createStatement();
            java.sql.ResultSet rs2 = stm2.executeQuery(
                    "SELECT * FROM \"ProdutoIngrediente\" WHERE produto_id = "
                    + produtoSelecionadoId);
            while (rs2.next()) {
                String nomeIng = rs2.getString("ingrediente_nome");
                int qtd = rs2.getInt("quantidade");
                entidades.Estoque e = listaIngredientes.stream()
                        .filter(x -> x.getNome().equals(nomeIng))
                        .findFirst().orElse(null);
                if (e != null) {
                    ingredientesDoProduto.add(e);
                    quantidadesDoProduto.add(qtd);
                }
            }
            stm2.close();
            con.close();

            atualizarTabelaIngredientes();
            validaCampos("selecionado");

        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(null, "Erro: " + e.getMessage());
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