package telas;

import dao.ClienteDAO;
import entidades.Cliente;
import entidades.Modulo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import service.Sessao;
import ui.Tema;

/** Cadastro de clientes (#19): busca, edição e histórico de compras. */
public class TelaClientes extends javax.swing.JFrame {

    private final ClienteDAO clienteDAO = new ClienteDAO();
    private List<Cliente> clientes = new ArrayList<>();
    private Map<Integer, double[]> historico = new java.util.HashMap<>();
    private Cliente selecionado;

    public TelaClientes() {
        initComponents();
        Tema.janela(this);
        Tema.titulo(lblTitulo);
        Tema.secundario(lblSubtitulo, lblHistorico);
        Tema.primario(btnSalvar);
        Tema.perigo(btnExcluir);
        Tema.cartao(painelFormulario);
        Tema.transparente(painelCabecalho, painelTitulo, painelAcoesTopo, painelCorpo, painelLista, painelBusca,
                painelCampos, painelBotoes);
        lblFormTitulo.setForeground(Tema.TEXTO);
        lblHistorico.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        btnExcluir.setVisible(Sessao.pode(Modulo.CANCELAMENTOS));
        txtBusca.putClientProperty("JTextField.placeholderText", "nome ou telefone");
        txtBusca.putClientProperty("JTextField.showClearButton", true);
        txtTelefone.putClientProperty("JTextField.placeholderText", "(11) 99999-9999");
        txtBusca.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { carregar(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { carregar(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { carregar(); }
        });
        tabelaClientes.getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tabelaClientes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabelaClientes.getSelectedRow() >= 0) {
                editar(clientes.get(tabelaClientes.getSelectedRow()));
            }
        });
        carregar();
        novo();
        Tema.tamanhoPadrao(this);
    }

    private void carregar() {
        javax.swing.table.DefaultTableModel modelo = new javax.swing.table.DefaultTableModel(
                new String[]{"Nome", "Telefone", "Endereço", "Pedidos", "Total gasto"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        try {
            clientes = clienteDAO.listar(txtBusca.getText());
            historico = clienteDAO.historico();
            for (Cliente c : clientes) {
                double[] h = historico.getOrDefault(c.getId(), new double[]{0, 0});
                modelo.addRow(new Object[]{c.getNome(), nulo(c.getTelefone()), nulo(c.getEndereco()),
                    (int) h[0], Tema.reais(h[1])});
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage());
        }
        tabelaClientes.setModel(modelo);
        tabelaClientes.getColumnModel().getColumn(2).setPreferredWidth(260);
    }

    private static String nulo(String s) {
        return s == null ? "-" : s;
    }

    private void novo() {
        selecionado = null;
        tabelaClientes.clearSelection();
        lblFormTitulo.setText("Novo cliente");
        for (javax.swing.JTextField t : new javax.swing.JTextField[]{txtNome, txtTelefone, txtEndereco, txtObservacao}) {
            t.setText("");
        }
        lblHistorico.setText(" ");
        btnExcluir.setEnabled(false);
        txtNome.requestFocusInWindow();
    }

    private void editar(Cliente c) {
        selecionado = c;
        lblFormTitulo.setText("Editar cliente");
        txtNome.setText(c.getNome());
        txtTelefone.setText(c.getTelefone() == null ? "" : c.getTelefone());
        txtEndereco.setText(c.getEndereco() == null ? "" : c.getEndereco());
        txtObservacao.setText(c.getObservacao() == null ? "" : c.getObservacao());
        double[] h = historico.getOrDefault(c.getId(), new double[]{0, 0});
        lblHistorico.setText(h[0] == 0 ? "<html><br>Ainda não fez pedidos.</html>"
                : String.format("<html><br><b>%d pedido(s)</b> | total gasto %s</html>", (int) h[0], Tema.reais(h[1])));
        btnExcluir.setEnabled(true);
    }

    private void salvar() {
        if (txtNome.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe o nome do cliente.", "Clientes", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Cliente c = selecionado != null ? selecionado : new Cliente();
        c.setNome(txtNome.getText().trim());
        c.setTelefone(txtTelefone.getText());
        c.setEndereco(txtEndereco.getText());
        c.setObservacao(txtObservacao.getText());
        try {
            if (selecionado == null) {
                clienteDAO.inserir(c);
            } else {
                clienteDAO.atualizar(c);
            }
            carregar();
            novo();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage());
        }
    }

    private void excluir() {
        if (selecionado == null) {
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Excluir o cliente \"" + selecionado.getNome() + "\"?", "Clientes",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            clienteDAO.excluir(selecionado.getId());
            carregar();
            novo();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Clientes", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Cadastro rápido (usado ao abrir um delivery). Retorna o cliente criado
     * ou null se cancelado.
     */
    public static Cliente cadastroRapido(java.awt.Component pai) {
        javax.swing.JTextField nome = new javax.swing.JTextField(24);
        javax.swing.JTextField telefone = new javax.swing.JTextField(24);
        javax.swing.JTextField endereco = new javax.swing.JTextField(24);
        javax.swing.JPanel p = new javax.swing.JPanel(new java.awt.GridLayout(0, 1, 0, 4));
        p.add(new javax.swing.JLabel("Nome:"));
        p.add(nome);
        p.add(new javax.swing.JLabel("Telefone:"));
        p.add(telefone);
        p.add(new javax.swing.JLabel("Endereço:"));
        p.add(endereco);
        while (JOptionPane.showConfirmDialog(pai, p, "Novo cliente", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            if (nome.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(pai, "Informe o nome.", "Novo cliente", JOptionPane.WARNING_MESSAGE);
                continue;
            }
            Cliente c = new Cliente();
            c.setNome(nome.getText().trim());
            c.setTelefone(telefone.getText());
            c.setEndereco(endereco.getText());
            try {
                c.setId(new ClienteDAO().inserir(c));
                return c;
            } catch (Exception e) {
                JOptionPane.showMessageDialog(pai, "Erro: " + e.getMessage());
                return null;
            }
        }
        return null;
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
        painelBusca = new javax.swing.JPanel();
        lblBusca = new javax.swing.JLabel();
        txtBusca = new javax.swing.JTextField();
        btnLimparBusca = new javax.swing.JButton();
        scrollClientes = new javax.swing.JScrollPane();
        tabelaClientes = new javax.swing.JTable();
        painelFormulario = new javax.swing.JPanel();
        painelCampos = new javax.swing.JPanel();
        lblFormTitulo = new javax.swing.JLabel();
        lblNome = new javax.swing.JLabel();
        txtNome = new javax.swing.JTextField();
        lblTelefone = new javax.swing.JLabel();
        txtTelefone = new javax.swing.JTextField();
        lblEndereco = new javax.swing.JLabel();
        txtEndereco = new javax.swing.JTextField();
        lblObservacao = new javax.swing.JLabel();
        txtObservacao = new javax.swing.JTextField();
        lblHistorico = new javax.swing.JLabel();
        painelBotoes = new javax.swing.JPanel();
        btnNovo = new javax.swing.JButton();
        btnSalvar = new javax.swing.JButton();
        btnExcluir = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("BLK Burguer - Clientes");
        setMinimumSize(new java.awt.Dimension(1180, 720));

        painelCabecalho.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 24, 14, 24));
        painelCabecalho.setLayout(new java.awt.BorderLayout());
        painelTitulo.setLayout(new java.awt.GridLayout(2, 1, 0, 2));
        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblTitulo.setText("Clientes");
        painelTitulo.add(lblTitulo);

        lblSubtitulo.setText("Cadastro para delivery, retirada e histórico de compras");
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
        painelLista.setLayout(new java.awt.BorderLayout(0, 10));
        painelBusca.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        lblBusca.setText("Buscar:");
        painelBusca.add(lblBusca);

        txtBusca.setPreferredSize(new java.awt.Dimension(300, 30));
        painelBusca.add(txtBusca);

        btnLimparBusca.setText("Limpar");
        btnLimparBusca.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLimparBuscaActionPerformed(evt);
            }
        });
        painelBusca.add(btnLimparBusca);

        painelLista.add(painelBusca, java.awt.BorderLayout.PAGE_START);

        scrollClientes.setViewportView(tabelaClientes);
        painelLista.add(scrollClientes, java.awt.BorderLayout.CENTER);

        painelCorpo.add(painelLista, java.awt.BorderLayout.CENTER);

        painelFormulario.setPreferredSize(new java.awt.Dimension(380, 0));
        painelFormulario.setLayout(new java.awt.BorderLayout(0, 14));
        painelCampos.setLayout(new java.awt.GridLayout(0, 1, 0, 4));
        lblFormTitulo.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        lblFormTitulo.setText("Novo cliente");
        painelCampos.add(lblFormTitulo);

        lblNome.setText("Nome");
        painelCampos.add(lblNome);

        painelCampos.add(txtNome);

        lblTelefone.setText("Telefone");
        painelCampos.add(lblTelefone);

        painelCampos.add(txtTelefone);

        lblEndereco.setText("Endereço (para delivery)");
        painelCampos.add(lblEndereco);

        painelCampos.add(txtEndereco);

        lblObservacao.setText("Observação");
        painelCampos.add(lblObservacao);

        painelCampos.add(txtObservacao);

        painelFormulario.add(painelCampos, java.awt.BorderLayout.PAGE_START);

        lblHistorico.setText(" ");
        painelFormulario.add(lblHistorico, java.awt.BorderLayout.CENTER);

        painelBotoes.setLayout(new java.awt.GridLayout(2, 2, 8, 8));
        btnNovo.setText("Novo cliente");
        btnNovo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNovoActionPerformed(evt);
            }
        });
        painelBotoes.add(btnNovo);

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

        getContentPane().add(painelCorpo, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnVoltarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVoltarActionPerformed
        Main.voltar(this);
    }//GEN-LAST:event_btnVoltarActionPerformed

    private void btnLimparBuscaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimparBuscaActionPerformed
        txtBusca.setText("");
    }//GEN-LAST:event_btnLimparBuscaActionPerformed

    private void btnNovoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNovoActionPerformed
        novo();
    }//GEN-LAST:event_btnNovoActionPerformed

    private void btnSalvarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSalvarActionPerformed
        salvar();
    }//GEN-LAST:event_btnSalvarActionPerformed

    private void btnExcluirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExcluirActionPerformed
        excluir();
    }//GEN-LAST:event_btnExcluirActionPerformed

    private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
        novo();
    }//GEN-LAST:event_btnCancelarActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnExcluir;
    private javax.swing.JButton btnLimparBusca;
    private javax.swing.JButton btnNovo;
    private javax.swing.JButton btnSalvar;
    private javax.swing.JButton btnVoltar;
    private javax.swing.JLabel lblBusca;
    private javax.swing.JLabel lblEndereco;
    private javax.swing.JLabel lblFormTitulo;
    private javax.swing.JLabel lblHistorico;
    private javax.swing.JLabel lblNome;
    private javax.swing.JLabel lblObservacao;
    private javax.swing.JLabel lblSubtitulo;
    private javax.swing.JLabel lblTelefone;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel painelAcoesTopo;
    private javax.swing.JPanel painelBotoes;
    private javax.swing.JPanel painelBusca;
    private javax.swing.JPanel painelCabecalho;
    private javax.swing.JPanel painelCampos;
    private javax.swing.JPanel painelCorpo;
    private javax.swing.JPanel painelFormulario;
    private javax.swing.JPanel painelLista;
    private javax.swing.JPanel painelTitulo;
    private javax.swing.JScrollPane scrollClientes;
    private javax.swing.JTable tabelaClientes;
    private javax.swing.JTextField txtBusca;
    private javax.swing.JTextField txtEndereco;
    private javax.swing.JTextField txtNome;
    private javax.swing.JTextField txtObservacao;
    private javax.swing.JTextField txtTelefone;
    // End of variables declaration//GEN-END:variables
}
