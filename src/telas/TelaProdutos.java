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
        java.awt.Color cor = lucro < 0 ? ui.Tema.PERIGO : ui.Tema.SUCESSO;
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
            String busca = txtBusca.getText().trim().toLowerCase();
            String tipo = String.valueOf(cmbFiltroTipo.getSelectedItem());
            listaProdutos = new ArrayList<>();
            for (Produto p : produtoDAO.listar()) {
                if ((busca.isEmpty() || p.getNome().toLowerCase().contains(busca))
                        && ("Todos".equals(tipo) || tipo.equals(p.getTipo()))) {
                    listaProdutos.add(p);
                }
            }
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
        ui.Tema.janela(this);
        ui.Tema.titulo(txtTitle);
        ui.Tema.secundario(lblSubtitulo, txtSubtitulo2);
        ui.Tema.primario(btnSalvar);
        ui.Tema.perigo(btnExcluir);
        ui.Tema.cartao(painelFicha);
        ui.Tema.transparente(painelCabecalho, painelTitulo, painelAcoesTopo, painelCorpo, painelLista, painelTopoLista,
                painelBusca, painelDados, painelPrecoTipo, painelPrecoTipoCampos, painelAddIngrediente,
                painelQtdIngrediente, painelRodapeFicha, painelResumo, painelBotoes);
        for (javax.swing.JLabel l : new javax.swing.JLabel[]{jLabel3, txtSubititulo, jLabel1}) {
            l.setForeground(ui.Tema.TEXTO);
        }
        valorCEstimado.setForeground(ui.Tema.TEXTO);
        lblAvisoPreco.setForeground(ui.Tema.PERIGO);
        txtBusca.putClientProperty("JTextField.placeholderText", "nome do produto");
        txtBusca.putClientProperty("JTextField.showClearButton", true);
        // Busca e filtro por tipo enquanto o usuário digita/escolhe (#22).
        txtBusca.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { montaTabelaProdutos(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { montaTabelaProdutos(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { montaTabelaProdutos(); }
        });
        cmbFiltroTipo.addActionListener(e -> montaTabelaProdutos());
        ui.Tema.tamanhoPadrao(this);
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

        painelCabecalho = new javax.swing.JPanel();
        painelTitulo = new javax.swing.JPanel();
        txtTitle = new javax.swing.JLabel();
        lblSubtitulo = new javax.swing.JLabel();
        painelAcoesTopo = new javax.swing.JPanel();
        btnSair = new javax.swing.JButton();
        painelCorpo = new javax.swing.JPanel();
        painelLista = new javax.swing.JPanel();
        painelTopoLista = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        painelBusca = new javax.swing.JPanel();
        lblBusca = new javax.swing.JLabel();
        txtBusca = new javax.swing.JTextField();
        lblFiltroTipo = new javax.swing.JLabel();
        cmbFiltroTipo = new javax.swing.JComboBox<>();
        btnLimparFiltro = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        tabelaProdutos = new javax.swing.JTable();
        painelFicha = new javax.swing.JPanel();
        painelDados = new javax.swing.JPanel();
        txtSubititulo = new javax.swing.JLabel();
        txtNome = new javax.swing.JLabel();
        campoNome = new javax.swing.JTextField();
        painelPrecoTipo = new javax.swing.JPanel();
        txtPvenda = new javax.swing.JLabel();
        txtTipo = new javax.swing.JLabel();
        painelPrecoTipoCampos = new javax.swing.JPanel();
        campoPvenda = new javax.swing.JTextField();
        ComboboxTipo = new javax.swing.JComboBox<>();
        jLabel1 = new javax.swing.JLabel();
        painelAddIngrediente = new javax.swing.JPanel();
        cBoxAddIngrediente = new javax.swing.JComboBox<>();
        painelQtdIngrediente = new javax.swing.JPanel();
        txtQnt = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        btnAddIngrediente = new javax.swing.JButton();
        txtSubtitulo2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tabelaIngredientesProd = new javax.swing.JTable();
        painelRodapeFicha = new javax.swing.JPanel();
        btnRemoverIngrediente = new javax.swing.JButton();
        painelResumo = new javax.swing.JPanel();
        txtCustoEstimado = new javax.swing.JLabel();
        valorCEstimado = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtMargem = new javax.swing.JLabel();
        txtValorMargem = new javax.swing.JLabel();
        lblMargemPercentual = new javax.swing.JLabel();
        lblAvisoPreco = new javax.swing.JLabel();
        painelBotoes = new javax.swing.JPanel();
        btnNovo = new javax.swing.JButton();
        btnEditar = new javax.swing.JButton();
        btnSalvar = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();
        btnExcluir = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("BLK Burguer - Produtos");
        setMinimumSize(new java.awt.Dimension(1180, 720));

        painelCabecalho.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 24, 14, 24));
        painelCabecalho.setLayout(new java.awt.BorderLayout());
        painelTitulo.setLayout(new java.awt.GridLayout(2, 1, 0, 2));
        txtTitle.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        txtTitle.setText("Produtos");
        painelTitulo.add(txtTitle);

        lblSubtitulo.setText("Cardápio, ficha técnica, custo e margem de cada produto");
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
        painelTopoLista.setLayout(new java.awt.GridLayout(2, 1, 0, 6));
        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        jLabel3.setText("Produtos cadastrados");
        painelTopoLista.add(jLabel3);

        painelBusca.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        lblBusca.setText("Buscar:");
        painelBusca.add(lblBusca);

        txtBusca.setPreferredSize(new java.awt.Dimension(240, 30));
        painelBusca.add(txtBusca);

        lblFiltroTipo.setText("Tipo:");
        painelBusca.add(lblFiltroTipo);

        cmbFiltroTipo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Todos", "Lanche", "Bebida", "Acompanhamento" }));
        painelBusca.add(cmbFiltroTipo);

        btnLimparFiltro.setText("Limpar");
        btnLimparFiltro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLimparFiltroActionPerformed(evt);
            }
        });
        painelBusca.add(btnLimparFiltro);

        painelTopoLista.add(painelBusca);

        painelLista.add(painelTopoLista, java.awt.BorderLayout.PAGE_START);

        tabelaProdutos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tabelaProdutosMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(tabelaProdutos);
        painelLista.add(jScrollPane3, java.awt.BorderLayout.CENTER);

        painelCorpo.add(painelLista, java.awt.BorderLayout.CENTER);

        painelFicha.setPreferredSize(new java.awt.Dimension(500, 0));
        painelFicha.setLayout(new java.awt.BorderLayout(0, 10));
        painelDados.setLayout(new java.awt.GridLayout(0, 1, 0, 4));
        txtSubititulo.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        txtSubititulo.setText("Dados do produto");
        painelDados.add(txtSubititulo);

        txtNome.setText("Nome");
        painelDados.add(txtNome);

        painelDados.add(campoNome);

        painelPrecoTipo.setLayout(new java.awt.GridLayout(1, 2, 10, 0));
        txtPvenda.setText("Preço de venda (R$)");
        painelPrecoTipo.add(txtPvenda);

        txtTipo.setText("Tipo");
        painelPrecoTipo.add(txtTipo);

        painelDados.add(painelPrecoTipo);

        painelPrecoTipoCampos.setLayout(new java.awt.GridLayout(1, 2, 10, 0));
        painelPrecoTipoCampos.add(campoPvenda);

        ComboboxTipo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Lanche", "Bebida", "Acompanhamento" }));
        painelPrecoTipoCampos.add(ComboboxTipo);

        painelDados.add(painelPrecoTipoCampos);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        jLabel1.setText("Ficha técnica");
        painelDados.add(jLabel1);

        painelAddIngrediente.setLayout(new java.awt.BorderLayout(8, 0));
        painelAddIngrediente.add(cBoxAddIngrediente, java.awt.BorderLayout.CENTER);

        painelQtdIngrediente.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 6, 0));
        txtQnt.setText("Qtd:");
        painelQtdIngrediente.add(txtQnt);

        jTextField1.setPreferredSize(new java.awt.Dimension(56, 30));
        painelQtdIngrediente.add(jTextField1);

        btnAddIngrediente.setText("Adicionar");
        btnAddIngrediente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddIngredienteActionPerformed(evt);
            }
        });
        painelQtdIngrediente.add(btnAddIngrediente);

        painelAddIngrediente.add(painelQtdIngrediente, java.awt.BorderLayout.LINE_END);

        painelDados.add(painelAddIngrediente);

        txtSubtitulo2.setText("Ingredientes do produto");
        painelDados.add(txtSubtitulo2);

        painelFicha.add(painelDados, java.awt.BorderLayout.PAGE_START);

        jScrollPane2.setViewportView(tabelaIngredientesProd);
        painelFicha.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        painelRodapeFicha.setLayout(new java.awt.GridLayout(0, 1, 0, 6));
        btnRemoverIngrediente.setText("Remover ingrediente selecionado");
        btnRemoverIngrediente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoverIngredienteActionPerformed(evt);
            }
        });
        painelRodapeFicha.add(btnRemoverIngrediente);

        painelResumo.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 6, 0));
        txtCustoEstimado.setText("Custo: R$");
        painelResumo.add(txtCustoEstimado);

        valorCEstimado.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        valorCEstimado.setText("0,00");
        painelResumo.add(valorCEstimado);

        jLabel2.setText("|");
        painelResumo.add(jLabel2);

        txtMargem.setText("Lucro: R$");
        painelResumo.add(txtMargem);

        txtValorMargem.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        txtValorMargem.setText("0,00");
        painelResumo.add(txtValorMargem);

        lblMargemPercentual.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblMargemPercentual.setText("");
        painelResumo.add(lblMargemPercentual);

        painelRodapeFicha.add(painelResumo);

        lblAvisoPreco.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblAvisoPreco.setText(" ");
        painelRodapeFicha.add(lblAvisoPreco);

        painelBotoes.setLayout(new java.awt.GridLayout(1, 5, 6, 0));
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

        btnExcluir.setText("Excluir");
        btnExcluir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExcluirActionPerformed(evt);
            }
        });
        painelBotoes.add(btnExcluir);

        painelRodapeFicha.add(painelBotoes);

        painelFicha.add(painelRodapeFicha, java.awt.BorderLayout.PAGE_END);

        painelCorpo.add(painelFicha, java.awt.BorderLayout.LINE_END);

        getContentPane().add(painelCorpo, java.awt.BorderLayout.CENTER);

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

    private void btnLimparFiltroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimparFiltroActionPerformed
        txtBusca.setText("");
        cmbFiltroTipo.setSelectedIndex(0);
        montaTabelaProdutos();
    }//GEN-LAST:event_btnLimparFiltroActionPerformed

    private void btnSairActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSairActionPerformed
        Main.voltar(this);
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
    private javax.swing.JButton btnLimparFiltro;
    private javax.swing.JButton btnNovo;
    private javax.swing.JButton btnRemoverIngrediente;
    private javax.swing.JButton btnSair;
    private javax.swing.JButton btnSalvar;
    private javax.swing.JComboBox<String> cBoxAddIngrediente;
    private javax.swing.JTextField campoNome;
    private javax.swing.JTextField campoPvenda;
    private javax.swing.JComboBox<String> cmbFiltroTipo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JLabel lblAvisoPreco;
    private javax.swing.JLabel lblBusca;
    private javax.swing.JLabel lblFiltroTipo;
    private javax.swing.JLabel lblMargemPercentual;
    private javax.swing.JLabel lblSubtitulo;
    private javax.swing.JPanel painelAcoesTopo;
    private javax.swing.JPanel painelAddIngrediente;
    private javax.swing.JPanel painelBotoes;
    private javax.swing.JPanel painelBusca;
    private javax.swing.JPanel painelCabecalho;
    private javax.swing.JPanel painelCorpo;
    private javax.swing.JPanel painelDados;
    private javax.swing.JPanel painelFicha;
    private javax.swing.JPanel painelLista;
    private javax.swing.JPanel painelPrecoTipo;
    private javax.swing.JPanel painelPrecoTipoCampos;
    private javax.swing.JPanel painelQtdIngrediente;
    private javax.swing.JPanel painelResumo;
    private javax.swing.JPanel painelRodapeFicha;
    private javax.swing.JPanel painelTitulo;
    private javax.swing.JPanel painelTopoLista;
    private javax.swing.JTable tabelaIngredientesProd;
    private javax.swing.JTable tabelaProdutos;
    private javax.swing.JTextField txtBusca;
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