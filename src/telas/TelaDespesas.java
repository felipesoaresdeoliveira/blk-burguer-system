package telas;

import entidades.CategoriaDespesa;
import entidades.Despesa;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import service.DespesaService;
import ui.Acabamento;
import ui.Aviso;
import ui.Tema;

/** Despesas operacionais (#40): registro por categoria e totais do período. */
public class TelaDespesas extends javax.swing.JPanel implements Navegavel {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private final DespesaService despesaService = new DespesaService();
    private List<Despesa> despesas = new ArrayList<>();
    private Despesa selecionada;

    public TelaDespesas() {
        initComponents();
        Acabamento.vazio(tabelaDespesas, "Nenhuma despesa no período");
        Tema.titulo(lblTitulo);
        Tema.secundario(lblSubtitulo, lblCategorias);
        Tema.primario(btnSalvar);
        Tema.perigo(btnExcluir);
        Tema.cartao(painelFormulario);
        Tema.transparente(painelCabecalho, painelTitulo, painelAcoesTopo, painelCorpo, painelLista, painelFiltro,
                painelCampos, painelBotoes);
        lblFormTitulo.setForeground(Tema.TEXTO);
        lblResumo.setForeground(Tema.TEXTO);
        lblCategorias.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        txtDe.putClientProperty("JTextField.placeholderText", "dd/mm/aaaa");
        txtAte.putClientProperty("JTextField.placeholderText", "dd/mm/aaaa");
        txtData.putClientProperty("JTextField.placeholderText", "dd/mm/aaaa");
        txtValor.putClientProperty("JTextField.placeholderText", "0,00");
        txtDescricao.putClientProperty("JTextField.placeholderText", "ex.: conta de luz de setembro");
        LocalDate hoje = LocalDate.now();
        txtDe.setText(hoje.withDayOfMonth(1).format(DATA));
        txtAte.setText(hoje.format(DATA));
        cmbCategoriaFiltro.addActionListener(e -> carregar());
        tabelaDespesas.getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tabelaDespesas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabelaDespesas.getSelectedRow() >= 0) {
                editar(despesas.get(tabelaDespesas.getSelectedRow()));
            }
        });
        putClientProperty("BLK.botaoPadrao", btnSalvar);
        carregar();
        nova();
    }

    private static LocalDate ler(javax.swing.JTextField campo) {
        try {
            return LocalDate.parse(campo.getText().trim(), DATA);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static Double lerValor(String texto) {
        try {
            String t = texto.trim().replace("R$", "").trim();
            if (t.contains(",")) {
                t = t.replace(".", "").replace(',', '.');
            }
            return t.isEmpty() ? null : Double.valueOf(t);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void carregar() {
        LocalDate de = ler(txtDe), ate = ler(txtAte);
        if (de == null || ate == null) {
            JOptionPane.showMessageDialog(this, "Informe as datas no formato dd/mm/aaaa.", "Despesas", JOptionPane.WARNING_MESSAGE);
            return;
        }
        CategoriaDespesa filtro = cmbCategoriaFiltro.getSelectedIndex() <= 0 ? null
                : CategoriaDespesa.values()[cmbCategoriaFiltro.getSelectedIndex() - 1];
        javax.swing.table.DefaultTableModel modelo = new javax.swing.table.DefaultTableModel(
                new String[]{"Data", "Descrição", "Categoria", "Valor", "Por"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        try {
            despesas = despesaService.listar(de, ate, filtro);
            double soma = 0;
            for (Despesa d : despesas) {
                soma += d.getValor();
                modelo.addRow(new Object[]{d.getData().format(DATA), d.getDescricao(), d.getCategoria(),
                    Tema.reais(d.getValor()), d.getUsuarioNome() == null ? "-" : d.getUsuarioNome()});
            }
            lblResumo.setText(despesas.size() + " despesa(s)  |  Total: " + Tema.reais(soma));
            mostrarCategorias(despesaService.totalPorCategoria(de, ate));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Despesas", JOptionPane.WARNING_MESSAGE);
        }
        tabelaDespesas.setModel(modelo);
        tabelaDespesas.getColumnModel().getColumn(0).setPreferredWidth(80);
        tabelaDespesas.getColumnModel().getColumn(1).setPreferredWidth(230);
    }

    /** Total de cada categoria no período, no rodapé do formulário. */
    private void mostrarCategorias(Map<CategoriaDespesa, Double> totais) {
        StringBuilder sb = new StringBuilder("<html><br><b>No período, por categoria</b><br>");
        boolean alguma = false;
        for (Map.Entry<CategoriaDespesa, Double> e : totais.entrySet()) {
            if (e.getValue() > 0) {
                alguma = true;
                sb.append(e.getKey()).append(": ").append(Tema.reais(e.getValue())).append("<br>");
            }
        }
        lblCategorias.setText(alguma ? sb + "</html>" : "<html><br>Nenhuma despesa no período.</html>");
    }

    private void nova() {
        selecionada = null;
        tabelaDespesas.clearSelection();
        lblFormTitulo.setText("Nova despesa");
        txtDescricao.setText("");
        txtValor.setText("");
        txtData.setText(LocalDate.now().format(DATA));
        cmbCategoria.setSelectedIndex(0);
        btnExcluir.setEnabled(false);
        txtDescricao.requestFocusInWindow();
    }

    private void editar(Despesa d) {
        selecionada = d;
        lblFormTitulo.setText("Editar despesa");
        txtDescricao.setText(d.getDescricao());
        cmbCategoria.setSelectedIndex(d.getCategoria().ordinal());
        txtData.setText(d.getData().format(DATA));
        txtValor.setText(String.format("%.2f", d.getValor()).replace('.', ','));
        btnExcluir.setEnabled(true);
    }

    private void salvar() {
        LocalDate data = ler(txtData);
        Double valor = lerValor(txtValor.getText());
        if (data == null) {
            JOptionPane.showMessageDialog(this, "Informe a data no formato dd/mm/aaaa.", "Despesas", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (valor == null) {
            JOptionPane.showMessageDialog(this, "Informe o valor, ex.: 150,00.", "Despesas", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Despesa d = selecionada != null ? selecionada : new Despesa();
        d.setDescricao(txtDescricao.getText());
        d.setCategoria(CategoriaDespesa.values()[cmbCategoria.getSelectedIndex()]);
        d.setData(data);
        d.setValor(valor);
        try {
            boolean nova = d.getId() == 0;
            despesaService.salvar(d);
            carregar();
            nova();
            Aviso.sucesso(this, nova ? "Despesa registrada" : "Despesa atualizada");
        } catch (IllegalArgumentException | SecurityException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Despesas", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage());
        }
    }

    private void excluir() {
        if (selecionada == null) {
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Excluir a despesa \"" + selecionada.getDescricao() + "\" ("
                + Tema.reais(selecionada.getValor()) + ")?", "Despesas", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            despesaService.excluir(selecionada.getId());
            carregar();
            nova();
            Aviso.sucesso(this, "Despesa excluída");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Despesas", JOptionPane.WARNING_MESSAGE);
        }
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
        painelLista = new javax.swing.JPanel();
        painelFiltro = new javax.swing.JPanel();
        lblDe = new javax.swing.JLabel();
        txtDe = new javax.swing.JTextField();
        lblAte = new javax.swing.JLabel();
        txtAte = new javax.swing.JTextField();
        cmbCategoriaFiltro = new javax.swing.JComboBox<>();
        btnFiltrar = new javax.swing.JButton();
        scrollDespesas = new javax.swing.JScrollPane();
        tabelaDespesas = new javax.swing.JTable();
        lblResumo = new javax.swing.JLabel();
        painelFormulario = new javax.swing.JPanel();
        painelCampos = new javax.swing.JPanel();
        lblFormTitulo = new javax.swing.JLabel();
        lblDescricao = new javax.swing.JLabel();
        txtDescricao = new javax.swing.JTextField();
        lblCategoria = new javax.swing.JLabel();
        cmbCategoria = new javax.swing.JComboBox<>();
        lblData = new javax.swing.JLabel();
        txtData = new javax.swing.JTextField();
        lblValor = new javax.swing.JLabel();
        txtValor = new javax.swing.JTextField();
        lblCategorias = new javax.swing.JLabel();
        painelBotoes = new javax.swing.JPanel();
        btnNova = new javax.swing.JButton();
        btnSalvar = new javax.swing.JButton();
        btnExcluir = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        painelCabecalho.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 24, 14, 24));
        painelCabecalho.setLayout(new java.awt.BorderLayout());
        painelTitulo.setLayout(new java.awt.GridLayout(2, 1, 0, 2));
        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblTitulo.setText("Despesas");
        painelTitulo.add(lblTitulo);

        lblSubtitulo.setText("Gastos da operação por período e categoria");
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

        add(painelCabecalho, java.awt.BorderLayout.PAGE_START);

        painelCorpo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 24, 24, 24));
        painelCorpo.setLayout(new java.awt.BorderLayout(20, 16));
        painelLista.setLayout(new java.awt.BorderLayout(0, 10));
        painelFiltro.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        lblDe.setText("De:");
        painelFiltro.add(lblDe);

        txtDe.setPreferredSize(new java.awt.Dimension(105, 30));
        painelFiltro.add(txtDe);

        lblAte.setText("Até:");
        painelFiltro.add(lblAte);

        txtAte.setPreferredSize(new java.awt.Dimension(105, 30));
        painelFiltro.add(txtAte);

        cmbCategoriaFiltro.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Todas", "Aluguel", "Energia", "Internet", "Fornecedor", "Manutenção", "Outros" }));
        painelFiltro.add(cmbCategoriaFiltro);

        btnFiltrar.setText("Filtrar");
        btnFiltrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFiltrarActionPerformed(evt);
            }
        });
        painelFiltro.add(btnFiltrar);

        painelLista.add(painelFiltro, java.awt.BorderLayout.PAGE_START);

        scrollDespesas.setViewportView(tabelaDespesas);
        painelLista.add(scrollDespesas, java.awt.BorderLayout.CENTER);

        lblResumo.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblResumo.setText(" ");
        painelLista.add(lblResumo, java.awt.BorderLayout.PAGE_END);

        painelCorpo.add(painelLista, java.awt.BorderLayout.CENTER);

        painelFormulario.setPreferredSize(new java.awt.Dimension(340, 0));
        painelFormulario.setLayout(new java.awt.BorderLayout(0, 14));
        painelCampos.setLayout(new java.awt.GridLayout(0, 1, 0, 4));
        lblFormTitulo.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        lblFormTitulo.setText("Nova despesa");
        painelCampos.add(lblFormTitulo);

        lblDescricao.setText("Descrição");
        painelCampos.add(lblDescricao);

        painelCampos.add(txtDescricao);

        lblCategoria.setText("Categoria");
        painelCampos.add(lblCategoria);

        cmbCategoria.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Aluguel", "Energia", "Internet", "Fornecedor", "Manutenção", "Outros" }));
        painelCampos.add(cmbCategoria);

        lblData.setText("Data");
        painelCampos.add(lblData);

        painelCampos.add(txtData);

        lblValor.setText("Valor (R$)");
        painelCampos.add(lblValor);

        painelCampos.add(txtValor);

        painelFormulario.add(painelCampos, java.awt.BorderLayout.PAGE_START);

        lblCategorias.setText(" ");
        painelFormulario.add(lblCategorias, java.awt.BorderLayout.CENTER);

        painelBotoes.setLayout(new java.awt.GridLayout(2, 2, 8, 8));
        btnNova.setText("Nova despesa");
        btnNova.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNovaActionPerformed(evt);
            }
        });
        painelBotoes.add(btnNova);

        btnSalvar.setText("Salvar");
        btnSalvar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSalvarActionPerformed(evt);
            }
        });
        painelBotoes.add(btnSalvar);

        btnExcluir.setText("Excluir");
        btnExcluir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExcluirActionPerformed(evt);
            }
        });
        painelBotoes.add(btnExcluir);

        btnCancelar.setText("Cancelar");
        btnCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelarActionPerformed(evt);
            }
        });
        painelBotoes.add(btnCancelar);

        painelFormulario.add(painelBotoes, java.awt.BorderLayout.PAGE_END);

        painelCorpo.add(painelFormulario, java.awt.BorderLayout.LINE_END);

        add(painelCorpo, java.awt.BorderLayout.CENTER);

    }// </editor-fold>//GEN-END:initComponents

    private void btnVoltarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVoltarActionPerformed
        Main.voltar(this);
    }//GEN-LAST:event_btnVoltarActionPerformed

    private void btnFiltrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFiltrarActionPerformed
        carregar();
    }//GEN-LAST:event_btnFiltrarActionPerformed

    private void btnNovaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNovaActionPerformed
        nova();
    }//GEN-LAST:event_btnNovaActionPerformed

    private void btnSalvarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSalvarActionPerformed
        salvar();
    }//GEN-LAST:event_btnSalvarActionPerformed

    private void btnExcluirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExcluirActionPerformed
        excluir();
    }//GEN-LAST:event_btnExcluirActionPerformed

    private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
        nova();
    }//GEN-LAST:event_btnCancelarActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnExcluir;
    private javax.swing.JButton btnFiltrar;
    private javax.swing.JButton btnNova;
    private javax.swing.JButton btnSalvar;
    private javax.swing.JButton btnVoltar;
    private javax.swing.JComboBox<String> cmbCategoria;
    private javax.swing.JComboBox<String> cmbCategoriaFiltro;
    private javax.swing.JLabel lblAte;
    private javax.swing.JLabel lblCategoria;
    private javax.swing.JLabel lblCategorias;
    private javax.swing.JLabel lblData;
    private javax.swing.JLabel lblDe;
    private javax.swing.JLabel lblDescricao;
    private javax.swing.JLabel lblFormTitulo;
    private javax.swing.JLabel lblResumo;
    private javax.swing.JLabel lblSubtitulo;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblValor;
    private javax.swing.JPanel painelAcoesTopo;
    private javax.swing.JPanel painelBotoes;
    private javax.swing.JPanel painelCabecalho;
    private javax.swing.JPanel painelCampos;
    private javax.swing.JPanel painelCorpo;
    private javax.swing.JPanel painelFiltro;
    private javax.swing.JPanel painelFormulario;
    private javax.swing.JPanel painelLista;
    private javax.swing.JPanel painelTitulo;
    private javax.swing.JScrollPane scrollDespesas;
    private javax.swing.JTable tabelaDespesas;
    private javax.swing.JTextField txtAte;
    private javax.swing.JTextField txtData;
    private javax.swing.JTextField txtDe;
    private javax.swing.JTextField txtDescricao;
    private javax.swing.JTextField txtValor;
    // End of variables declaration//GEN-END:variables
}
