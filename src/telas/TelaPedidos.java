package telas;

import dao.ClienteDAO;
import dao.ConfiguracaoDAO;
import entidades.Cliente;
import entidades.Modulo;
import entidades.Pedido;
import entidades.SituacaoPedido;
import entidades.TipoPedido;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import service.PedidoService;
import service.Sessao;
import ui.Tema;

/** Central de pedidos (#35): tudo que está em andamento, retirada e delivery. */
public class TelaPedidos extends javax.swing.JFrame {

    private final PedidoService pedidoService = new PedidoService();
    private final ConfiguracaoDAO configuracaoDAO = new ConfiguracaoDAO();
    private final Timer timer;
    private List<Pedido> todos = new ArrayList<>();
    private List<Pedido> visiveis = new ArrayList<>();
    private boolean carregando;

    public TelaPedidos() {
        initComponents();
        Tema.janela(this);
        Tema.titulo(lblTitulo);
        Tema.secundario(lblSubtitulo);
        Tema.primario(btnNovaRetirada, btnNovoDelivery, btnReceber);
        Tema.transparente(painelCabecalho, painelTitulo, painelAcoesTopo, painelCorpo, painelFiltro, painelAcoes, chkSoAReceber);
        btnTaxa.setVisible(Sessao.pode(Modulo.CADASTROS));
        btnReceber.setVisible(Sessao.pode(Modulo.CAIXA));
        tabelaPedidos.getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tabelaPedidos.getSelectionModel().addListSelectionListener(e -> atualizarBotoes());
        cmbFiltro.addActionListener(e -> mostrar());
        chkSoAReceber.addActionListener(e -> mostrar());
        atualizarTaxa();
        timer = new Timer(5000, e -> carregar());
        timer.start();
        carregar();
        Tema.tamanhoPadrao(this);
    }

    private void atualizarTaxa() {
        try {
            btnTaxa.setText("Taxa de entrega: " + Tema.reais(configuracaoDAO.taxaEntrega()));
        } catch (Exception e) {
            btnTaxa.setText("Taxa de entrega");
        }
    }

    private void carregar() {
        if (carregando) {
            return;
        }
        carregando = true;
        new SwingWorker<List<Pedido>, Void>() {
            @Override
            protected List<Pedido> doInBackground() throws Exception {
                return pedidoService.emAndamento();
            }

            @Override
            protected void done() {
                carregando = false;
                try {
                    todos = get();
                    mostrar();
                } catch (Exception e) {
                    lblSubtitulo.setText("Sem conexão com o banco - tentando novamente...");
                    lblSubtitulo.setForeground(Tema.PERIGO);
                }
            }
        }.execute();
    }

    private Pedido selecionado() {
        int linha = tabelaPedidos.getSelectedRow();
        return linha < 0 || linha >= visiveis.size() ? null : visiveis.get(linha);
    }

    private void mostrar() {
        Pedido antes = selecionado();
        String filtro = String.valueOf(cmbFiltro.getSelectedItem());
        visiveis = new ArrayList<>();
        for (Pedido p : todos) {
            if (("Todos".equals(filtro) || filtro.equals(p.getTipo().toString()))
                    && (!chkSoAReceber.isSelected() || (p.getSituacao() == SituacaoPedido.ABERTO && p.getSaldo() > 0))) {
                visiveis.add(p);
            }
        }
        javax.swing.table.DefaultTableModel modelo = new javax.swing.table.DefaultTableModel(
                new String[]{"Nº", "Tipo", "Pedido", "Andamento", "Total", "Falta pagar", "Há"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        int novaSelecao = -1;
        for (int i = 0; i < visiveis.size(); i++) {
            Pedido p = visiveis.get(i);
            long min = Duration.between(p.getData(), LocalDateTime.now()).toMinutes();
            String andamento = p.getAndamento() + (p.isContaSolicitada() ? " | pediu a conta" : "");
            modelo.addRow(new Object[]{p.getId(), p.getTipo(), p.getDescricao(), andamento, Tema.reais(p.getTotal()),
                p.getSituacao() == SituacaoPedido.PAGO ? "Pago" : Tema.reais(p.getSaldo()), min + " min"});
            if (antes != null && antes.getId() == p.getId()) {
                novaSelecao = i;
            }
        }
        tabelaPedidos.setModel(modelo);
        int[] larguras = {50, 90, 260, 220, 100, 100, 70};
        for (int i = 0; i < larguras.length; i++) {
            tabelaPedidos.getColumnModel().getColumn(i).setPreferredWidth(larguras[i]);
        }
        if (novaSelecao >= 0) {
            tabelaPedidos.setRowSelectionInterval(novaSelecao, novaSelecao);
        }
        long aReceber = todos.stream().filter(p -> p.getSituacao() == SituacaoPedido.ABERTO && p.getSaldo() > 0).count();
        lblSubtitulo.setText(todos.size() + " pedido(s) em andamento | " + aReceber + " com pagamento pendente");
        Tema.secundario(lblSubtitulo);
        atualizarBotoes();
    }

    private void atualizarBotoes() {
        Pedido p = selecionado();
        boolean delivery = p != null && p.getTipo() == TipoPedido.DELIVERY && p.getSituacao() != SituacaoPedido.CANCELADO;
        btnAbrir.setEnabled(p != null);
        btnReceber.setEnabled(p != null && p.getSituacao() == SituacaoPedido.ABERTO && p.getSaldo() > 0);
        btnSaiu.setEnabled(delivery && p.getStatusEntrega() == null);
        btnEntregue.setEnabled(delivery && !"ENTREGUE".equals(p.getStatusEntrega()));
    }

    private void abrirComanda(int pedidoId) {
        timer.stop();
        dispose();
        new TelaComanda(pedidoId, TelaComanda.Origem.PEDIDOS).setVisible(true);
    }

    private void novaRetirada() {
        String nome = JOptionPane.showInputDialog(this, "Nome de quem vai retirar (opcional):", "Nova retirada",
                JOptionPane.QUESTION_MESSAGE);
        if (nome == null) {
            return;
        }
        try {
            Pedido novo = new Pedido();
            novo.setTipo(TipoPedido.RETIRADA);
            novo.setIdentificacao(nome.trim());
            Pedido aberto = pedidoService.abrir(novo);
            JOptionPane.showMessageDialog(this, "Retirada aberta. Senha do cliente: " + aberto.getSenha(), "Nova retirada",
                    JOptionPane.INFORMATION_MESSAGE);
            abrirComanda(aberto.getId());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Nova retirada", JOptionPane.WARNING_MESSAGE);
        }
    }

    /** Delivery: escolher (ou cadastrar) o cliente; endereço vem do cadastro e a taxa é a fixa configurada. */
    private void novoDelivery() {
        try {
            double taxa = configuracaoDAO.taxaEntrega();
            List<Cliente> clientes = new ClienteDAO().listar("");
            javax.swing.JComboBox<Object> cmbCliente = new javax.swing.JComboBox<>();
            for (Cliente c : clientes) {
                cmbCliente.addItem(c);
            }
            javax.swing.JTextField txtEndereco = new javax.swing.JTextField(32);
            Runnable preencher = () -> {
                Object c = cmbCliente.getSelectedItem();
                txtEndereco.setText(c instanceof Cliente && ((Cliente) c).getEndereco() != null ? ((Cliente) c).getEndereco() : "");
            };
            cmbCliente.addActionListener(e -> preencher.run());
            preencher.run();
            javax.swing.JButton btnNovoCliente = new javax.swing.JButton("+ Novo cliente");
            btnNovoCliente.addActionListener(e -> {
                Cliente c = TelaClientes.cadastroRapido(this);
                if (c != null) {
                    cmbCliente.addItem(c);
                    cmbCliente.setSelectedItem(c);
                }
            });
            javax.swing.JPanel linhaCliente = new javax.swing.JPanel(new java.awt.BorderLayout(8, 0));
            linhaCliente.add(cmbCliente, java.awt.BorderLayout.CENTER);
            linhaCliente.add(btnNovoCliente, java.awt.BorderLayout.EAST);
            javax.swing.JPanel p = new javax.swing.JPanel(new java.awt.GridLayout(0, 1, 0, 6));
            p.add(new javax.swing.JLabel("Cliente:"));
            p.add(linhaCliente);
            p.add(new javax.swing.JLabel("Endereço de entrega:"));
            p.add(txtEndereco);
            javax.swing.JLabel lblTaxa = new javax.swing.JLabel("Taxa de entrega: " + Tema.reais(taxa));
            lblTaxa.setFont(lblTaxa.getFont().deriveFont(java.awt.Font.BOLD));
            p.add(lblTaxa);
            while (JOptionPane.showConfirmDialog(this, p, "Novo delivery", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
                Object c = cmbCliente.getSelectedItem();
                if (!(c instanceof Cliente)) {
                    JOptionPane.showMessageDialog(this, "Escolha ou cadastre o cliente.", "Novo delivery", JOptionPane.WARNING_MESSAGE);
                    continue;
                }
                Pedido novo = new Pedido();
                novo.setTipo(TipoPedido.DELIVERY);
                novo.setClienteId(((Cliente) c).getId());
                novo.setEnderecoEntrega(txtEndereco.getText().trim());
                novo.setTaxaEntrega(taxa);
                try {
                    Pedido aberto = pedidoService.abrir(novo);
                    abrirComanda(aberto.getId());
                    return;
                } catch (IllegalArgumentException | IllegalStateException e) {
                    JOptionPane.showMessageDialog(this, e.getMessage(), "Novo delivery", JOptionPane.WARNING_MESSAGE);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage());
        }
    }

    private void configurarTaxa() {
        try {
            String atual = String.format("%.2f", configuracaoDAO.taxaEntrega());
            String v = JOptionPane.showInputDialog(this, "Taxa fixa de entrega (R$):", atual);
            if (v == null) {
                return;
            }
            double taxa = Double.parseDouble(v.trim().replace(',', '.'));
            if (taxa < 0) {
                throw new NumberFormatException();
            }
            configuracaoDAO.gravar(ConfiguracaoDAO.TAXA_ENTREGA, String.valueOf(taxa));
            atualizarTaxa();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Valor inválido.", "Taxa de entrega", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage());
        }
    }

    private void entrega(String status) {
        Pedido p = selecionado();
        if (p == null) {
            return;
        }
        try {
            pedidoService.atualizarEntrega(p.getId(), status);
            carregar();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Entrega", JOptionPane.WARNING_MESSAGE);
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
        btnNovaRetirada = new javax.swing.JButton();
        btnNovoDelivery = new javax.swing.JButton();
        btnTaxa = new javax.swing.JButton();
        btnAtualizar = new javax.swing.JButton();
        btnVoltar = new javax.swing.JButton();
        painelCorpo = new javax.swing.JPanel();
        painelFiltro = new javax.swing.JPanel();
        lblFiltro = new javax.swing.JLabel();
        cmbFiltro = new javax.swing.JComboBox<>();
        chkSoAReceber = new javax.swing.JCheckBox();
        scrollPedidos = new javax.swing.JScrollPane();
        tabelaPedidos = new javax.swing.JTable();
        painelAcoes = new javax.swing.JPanel();
        btnSaiu = new javax.swing.JButton();
        btnEntregue = new javax.swing.JButton();
        btnReceber = new javax.swing.JButton();
        btnAbrir = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("BLK Burguer - Pedidos");
        setMinimumSize(new java.awt.Dimension(1180, 720));

        painelCabecalho.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 24, 14, 24));
        painelCabecalho.setLayout(new java.awt.BorderLayout());
        painelTitulo.setLayout(new java.awt.GridLayout(2, 1, 0, 2));
        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblTitulo.setText("Pedidos");
        painelTitulo.add(lblTitulo);

        lblSubtitulo.setText("Mesas, balcão, retirada e delivery em andamento");
        painelTitulo.add(lblSubtitulo);

        painelCabecalho.add(painelTitulo, java.awt.BorderLayout.LINE_START);

        painelAcoesTopo.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 8));
        btnNovaRetirada.setText("Nova retirada");
        btnNovaRetirada.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNovaRetiradaActionPerformed(evt);
            }
        });
        painelAcoesTopo.add(btnNovaRetirada);

        btnNovoDelivery.setText("Novo delivery");
        btnNovoDelivery.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNovoDeliveryActionPerformed(evt);
            }
        });
        painelAcoesTopo.add(btnNovoDelivery);

        btnTaxa.setText("Taxa de entrega");
        btnTaxa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTaxaActionPerformed(evt);
            }
        });
        painelAcoesTopo.add(btnTaxa);

        btnAtualizar.setText("Atualizar");
        btnAtualizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAtualizarActionPerformed(evt);
            }
        });
        painelAcoesTopo.add(btnAtualizar);

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
        painelFiltro.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        lblFiltro.setText("Mostrar:");
        painelFiltro.add(lblFiltro);

        cmbFiltro.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Todos", "Mesa", "Balcão", "Retirada", "Delivery" }));
        painelFiltro.add(cmbFiltro);

        chkSoAReceber.setText("Só com pagamento pendente");
        painelFiltro.add(chkSoAReceber);

        painelCorpo.add(painelFiltro, java.awt.BorderLayout.PAGE_START);

        tabelaPedidos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tabelaPedidosMouseClicked(evt);
            }
        });
        scrollPedidos.setViewportView(tabelaPedidos);
        painelCorpo.add(scrollPedidos, java.awt.BorderLayout.CENTER);

        painelAcoes.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));
        btnSaiu.setText("Saiu para entrega");
        btnSaiu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaiuActionPerformed(evt);
            }
        });
        painelAcoes.add(btnSaiu);

        btnEntregue.setText("Entregue");
        btnEntregue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEntregueActionPerformed(evt);
            }
        });
        painelAcoes.add(btnEntregue);

        btnReceber.setText("Receber");
        btnReceber.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReceberActionPerformed(evt);
            }
        });
        painelAcoes.add(btnReceber);

        btnAbrir.setText("Abrir comanda");
        btnAbrir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAbrirActionPerformed(evt);
            }
        });
        painelAcoes.add(btnAbrir);

        painelCorpo.add(painelAcoes, java.awt.BorderLayout.PAGE_END);

        getContentPane().add(painelCorpo, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnNovaRetiradaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNovaRetiradaActionPerformed
        novaRetirada();
    }//GEN-LAST:event_btnNovaRetiradaActionPerformed

    private void btnNovoDeliveryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNovoDeliveryActionPerformed
        novoDelivery();
    }//GEN-LAST:event_btnNovoDeliveryActionPerformed

    private void btnTaxaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTaxaActionPerformed
        configurarTaxa();
    }//GEN-LAST:event_btnTaxaActionPerformed

    private void btnAtualizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAtualizarActionPerformed
        carregar();
    }//GEN-LAST:event_btnAtualizarActionPerformed

    private void btnVoltarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVoltarActionPerformed
        timer.stop();
        Main.voltar(this);
    }//GEN-LAST:event_btnVoltarActionPerformed

    private void tabelaPedidosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tabelaPedidosMouseClicked
        if (evt.getClickCount() == 2 && selecionado() != null) {
            abrirComanda(selecionado().getId());
        }
    }//GEN-LAST:event_tabelaPedidosMouseClicked

    private void btnSaiuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaiuActionPerformed
        entrega("SAIU_PARA_ENTREGA");
    }//GEN-LAST:event_btnSaiuActionPerformed

    private void btnEntregueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEntregueActionPerformed
        entrega("ENTREGUE");
    }//GEN-LAST:event_btnEntregueActionPerformed

    private void btnReceberActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReceberActionPerformed
        Pedido p = selecionado();
        if (p != null && DialogoPagamento.receber(this, p.getId())) {
            carregar();
        }
    }//GEN-LAST:event_btnReceberActionPerformed

    private void btnAbrirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAbrirActionPerformed
        Pedido p = selecionado();
        if (p != null) {
            abrirComanda(p.getId());
        }
    }//GEN-LAST:event_btnAbrirActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAbrir;
    private javax.swing.JButton btnAtualizar;
    private javax.swing.JButton btnEntregue;
    private javax.swing.JButton btnNovaRetirada;
    private javax.swing.JButton btnNovoDelivery;
    private javax.swing.JButton btnReceber;
    private javax.swing.JButton btnSaiu;
    private javax.swing.JButton btnTaxa;
    private javax.swing.JButton btnVoltar;
    private javax.swing.JCheckBox chkSoAReceber;
    private javax.swing.JComboBox<String> cmbFiltro;
    private javax.swing.JLabel lblFiltro;
    private javax.swing.JLabel lblSubtitulo;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel painelAcoes;
    private javax.swing.JPanel painelAcoesTopo;
    private javax.swing.JPanel painelCabecalho;
    private javax.swing.JPanel painelCorpo;
    private javax.swing.JPanel painelFiltro;
    private javax.swing.JPanel painelTitulo;
    private javax.swing.JScrollPane scrollPedidos;
    private javax.swing.JTable tabelaPedidos;
    // End of variables declaration//GEN-END:variables
}
