package telas;

import entidades.Adicional;
import entidades.Mesa;
import entidades.Modulo;
import entidades.Pedido;
import entidades.Produto;
import entidades.SituacaoPedido;
import entidades.TipoPedido;
import entidades.Venda;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import service.EstoqueInsuficienteException;
import service.PedidoService;
import service.Sessao;
import ui.Tema;

/**
 * Comanda de um pedido (#24): mesa, retirada ou delivery. Itens são montados
 * numa lista e enviados juntos para a cozinha; o rodapé mostra total, pago e
 * saldo, e as ações de conta, transferência, entrega e recebimento.
 */
public class TelaComanda extends javax.swing.JFrame {

    /** De onde a comanda foi aberta, para o "Voltar". */
    public enum Origem { MESAS, PEDIDOS, CAIXA }

    private final PedidoService pedidoService = new PedidoService();
    private final int pedidoId;
    private final Origem origem;
    private final List<Venda> rascunho = new ArrayList<>();
    private List<Produto> produtos = new ArrayList<>();
    private Map<String, Map<String, Integer>> fichas = new java.util.HashMap<>();
    private Map<String, Adicional> adicionais = new java.util.LinkedHashMap<>();
    private Pedido pedido;
    private final Timer timer;

    public TelaComanda(int pedidoId, Origem origem) {
        this.pedidoId = pedidoId;
        this.origem = origem;
        initComponents();
        aplicarVisual();
        carregarCardapio();
        carregar();
        timer = new Timer(5000, e -> carregar());
        timer.start();
        Tema.tamanhoPadrao(this);
    }

    private void aplicarVisual() {
        Tema.janela(this);
        Tema.titulo(lblTitulo);
        Tema.secundario(lblSubtitulo, lblRascunho);
        Tema.cartao(painelLateral);
        Tema.transparente(painelCabecalho, painelTitulo, painelAcoesTopo, painelCorpo, painelItens, painelAcoesItens,
                painelNovoItem, painelQtd, painelEnviar, painelRodape, painelBotoes);
        lblItens.setForeground(Tema.TEXTO);
        lblNovo.setForeground(Tema.TEXTO);
        lblTotais.setForeground(Tema.TEXTO);
        Tema.primario(btnEnviar, btnReceber);
        Tema.perigo(btnCancelarPedido);
        btnReceber.setVisible(Sessao.pode(Modulo.CAIXA));
        btnCancelarPedido.setVisible(Sessao.pode(Modulo.CANCELAMENTOS));
        btnVoltar.setText(origem == Origem.MESAS ? "Voltar às mesas" : origem == Origem.CAIXA ? "Voltar ao caixa" : "Voltar aos pedidos");
    }

    private void carregarCardapio() {
        try {
            produtos = new dao.ProdutoDAO().listar();
            fichas = pedidoService.fichasTecnicas();
            adicionais = pedidoService.adicionais();
            cmbProduto.removeAllItems();
            for (Produto p : produtos) {
                cmbProduto.addItem(p.getNome() + " - " + Tema.reais(p.getPreco()));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar o cardápio: " + e.getMessage());
        }
    }

    /** Recarrega o pedido (itens, status da cozinha, pagamentos). */
    private void carregar() {
        try {
            pedido = pedidoService.buscar(pedidoId);
        } catch (Exception e) {
            lblSubtitulo.setText("Sem conexão com o banco - tentando novamente...");
            lblSubtitulo.setForeground(Tema.PERIGO);
            return;
        }
        if (pedido == null) {
            JOptionPane.showMessageDialog(this, "Pedido não encontrado.");
            voltar();
            return;
        }
        boolean aberto = pedido.getSituacao() == SituacaoPedido.ABERTO;
        lblTitulo.setText(pedido.getDescricao());
        long min = Duration.between(pedido.getData(), LocalDateTime.now()).toMinutes();
        StringBuilder sub = new StringBuilder("Pedido " + pedido.getId() + " | " + pedido.getTipo() + " | aberto há " + min + " min");
        if (pedido.getTipo() == TipoPedido.DELIVERY && pedido.getEnderecoEntrega() != null) {
            sub.append(" | ").append(pedido.getEnderecoEntrega());
        }
        sub.append(" | ").append(aberto ? pedido.getAndamento() : pedido.getSituacao().toString());
        if (pedido.isContaSolicitada()) {
            sub.append(" | conta solicitada");
        }
        lblSubtitulo.setText(sub.toString());
        Tema.secundario(lblSubtitulo);

        javax.swing.table.DefaultTableModel modelo = new javax.swing.table.DefaultTableModel(
                new String[]{"Item", "Qtd", "Valor", "Total", "Cozinha"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        for (Venda v : pedido.getItens()) {
            modelo.addRow(new Object[]{
                v.isPersonalizado() ? v.getProduto() + "  (" + v.getResumoPersonalizacao() + ")" : v.getProduto(),
                v.getQuantidade(), Tema.reais(v.getValor()), Tema.reais(v.getTotal()),
                v.getStatusCozinha() == null ? "-" : v.getStatusCozinha().toString()});
        }
        int sel = tabelaItens.getSelectedRow();
        tabelaItens.setModel(modelo);
        tabelaItens.getColumnModel().getColumn(0).setPreferredWidth(380);
        if (sel >= 0 && sel < modelo.getRowCount()) {
            tabelaItens.setRowSelectionInterval(sel, sel);
        }

        String taxa = pedido.getTaxaEntrega() > 0 ? "  (inclui entrega " + Tema.reais(pedido.getTaxaEntrega()) + ")" : "";
        lblTotais.setText("<html>Total <span style='font-size:20pt'>" + Tema.reais(pedido.getTotal()) + "</span>" + taxa
                + "&nbsp;&nbsp; Pago " + Tema.reais(pedido.getPago())
                + "&nbsp;&nbsp; <span style='color:#F5A524'>Falta " + Tema.reais(pedido.getSaldo()) + "</span></html>");

        boolean mesa = pedido.getTipo() == TipoPedido.MESA;
        boolean delivery = pedido.getTipo() == TipoPedido.DELIVERY;
        for (javax.swing.JComponent c : new javax.swing.JComponent[]{cmbProduto, spnQtd, btnAdicionar, btnTirarRascunho,
            btnEnviar, btnRemoverItem}) {
            c.setEnabled(aberto);
        }
        btnTransferir.setVisible(mesa && aberto);
        btnConta.setVisible(mesa && aberto);
        btnConta.setText(pedido.isContaSolicitada() ? "Cancelar pedido de conta" : "Pedir a conta");
        btnSaiu.setVisible(delivery && pedido.getSituacao() != SituacaoPedido.CANCELADO && pedido.getStatusEntrega() == null);
        btnEntregue.setVisible(delivery && pedido.getSituacao() != SituacaoPedido.CANCELADO
                && !"ENTREGUE".equals(pedido.getStatusEntrega()));
        btnReceber.setEnabled(aberto && pedido.getSaldo() > 0 && !pedido.getItens().isEmpty());
        btnCancelarPedido.setEnabled(aberto && pedido.getPago() == 0);
        atualizarRascunho();
    }

    private void atualizarRascunho() {
        javax.swing.table.DefaultTableModel modelo = new javax.swing.table.DefaultTableModel(
                new String[]{"Item", "Qtd", "Total"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        double total = 0;
        for (Venda v : rascunho) {
            double t = v.getValor() * v.getQuantidade();
            total += t;
            modelo.addRow(new Object[]{v.isPersonalizado() ? v.getProduto() + " (" + v.getResumoPersonalizacao() + ")" : v.getProduto(),
                v.getQuantidade(), Tema.reais(t)});
        }
        tabelaRascunho.setModel(modelo);
        tabelaRascunho.getColumnModel().getColumn(0).setPreferredWidth(240);
        btnEnviar.setText(rascunho.isEmpty() ? "Enviar para a cozinha" : "Enviar " + rascunho.size() + " item(ns) | " + Tema.reais(total));
        btnEnviar.setEnabled(!rascunho.isEmpty() && pedido != null && pedido.getSituacao() == SituacaoPedido.ABERTO);
    }

    private void adicionar() {
        int i = cmbProduto.getSelectedIndex();
        if (i < 0) {
            return;
        }
        Produto p = produtos.get(i);
        Venda v = new Venda();
        v.setProduto(p.getNome());
        v.setQuantidade((Integer) spnQtd.getValue());
        v.setValor(p.getPreco());
        if (!"Bebida".equalsIgnoreCase(p.getTipo()) && !DialogoPersonalizar.mostrar(this, p, v, fichas, adicionais)) {
            return;
        }
        rascunho.add(v);
        spnQtd.setValue(1);
        atualizarRascunho();
    }

    private void enviar() {
        if (rascunho.isEmpty()) {
            return;
        }
        try {
            pedidoService.lancarItens(pedidoId, new ArrayList<>(rascunho));
            rascunho.clear();
            carregar();
        } catch (EstoqueInsuficienteException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Estoque insuficiente", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Comanda", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void removerItem() {
        int linha = tabelaItens.getSelectedRow();
        if (linha < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um item do pedido.");
            return;
        }
        Venda v = pedido.getItens().get(linha);
        if (JOptionPane.showConfirmDialog(this, "Remover " + v.getQuantidade() + "x " + v.getProduto() + " do pedido?",
                "Comanda", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            pedidoService.removerItem(pedidoId, v.getId());
            carregar();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Comanda", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void transferir() {
        try {
            List<Mesa> livres = new ArrayList<>();
            for (Mesa m : new dao.MesaDAO().listarComStatus()) {
                if (m.getPedidoId() == 0) {
                    livres.add(m);
                }
            }
            if (livres.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Não há mesa livre para transferir.");
                return;
            }
            Mesa destino = (Mesa) JOptionPane.showInputDialog(this, "Transferir a comanda para:", "Transferir mesa",
                    JOptionPane.QUESTION_MESSAGE, null, livres.toArray(), livres.get(0));
            if (destino != null) {
                pedidoService.transferirMesa(pedidoId, destino.getId());
                carregar();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Comanda", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void acao(Acao a) {
        try {
            a.executar();
            carregar();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Comanda", JOptionPane.WARNING_MESSAGE);
        }
    }

    private interface Acao {
        void executar() throws Exception;
    }

    private void voltar() {
        if (timer != null) {
            timer.stop();
        }
        dispose();
        switch (origem) {
            case MESAS:
                new TelaMesas().setVisible(true);
                break;
            case CAIXA:
                new TelaCaixa().setVisible(true);
                break;
            default:
                new TelaPedidos().setVisible(true);
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
        painelItens = new javax.swing.JPanel();
        lblItens = new javax.swing.JLabel();
        scrollItens = new javax.swing.JScrollPane();
        tabelaItens = new javax.swing.JTable();
        painelAcoesItens = new javax.swing.JPanel();
        btnRemoverItem = new javax.swing.JButton();
        painelLateral = new javax.swing.JPanel();
        painelNovoItem = new javax.swing.JPanel();
        lblNovo = new javax.swing.JLabel();
        cmbProduto = new javax.swing.JComboBox<>();
        painelQtd = new javax.swing.JPanel();
        lblQtd = new javax.swing.JLabel();
        spnQtd = new javax.swing.JSpinner();
        btnAdicionar = new javax.swing.JButton();
        lblRascunho = new javax.swing.JLabel();
        scrollRascunho = new javax.swing.JScrollPane();
        tabelaRascunho = new javax.swing.JTable();
        painelEnviar = new javax.swing.JPanel();
        btnTirarRascunho = new javax.swing.JButton();
        btnEnviar = new javax.swing.JButton();
        painelRodape = new javax.swing.JPanel();
        lblTotais = new javax.swing.JLabel();
        painelBotoes = new javax.swing.JPanel();
        btnTransferir = new javax.swing.JButton();
        btnConta = new javax.swing.JButton();
        btnSaiu = new javax.swing.JButton();
        btnEntregue = new javax.swing.JButton();
        btnCancelarPedido = new javax.swing.JButton();
        btnReceber = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("BLK Burguer - Comanda");
        setMinimumSize(new java.awt.Dimension(1180, 720));

        painelCabecalho.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 24, 14, 24));
        painelCabecalho.setLayout(new java.awt.BorderLayout());
        painelTitulo.setLayout(new java.awt.GridLayout(2, 1, 0, 2));
        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblTitulo.setText("Comanda");
        painelTitulo.add(lblTitulo);

        lblSubtitulo.setText("pedido");
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
        painelItens.setLayout(new java.awt.BorderLayout(0, 8));
        lblItens.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        lblItens.setText("Itens do pedido");
        painelItens.add(lblItens, java.awt.BorderLayout.PAGE_START);

        scrollItens.setViewportView(tabelaItens);
        painelItens.add(scrollItens, java.awt.BorderLayout.CENTER);

        painelAcoesItens.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        btnRemoverItem.setText("Remover item (ainda não iniciado)");
        btnRemoverItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoverItemActionPerformed(evt);
            }
        });
        painelAcoesItens.add(btnRemoverItem);

        painelItens.add(painelAcoesItens, java.awt.BorderLayout.PAGE_END);

        painelCorpo.add(painelItens, java.awt.BorderLayout.CENTER);

        painelLateral.setPreferredSize(new java.awt.Dimension(420, 0));
        painelLateral.setLayout(new java.awt.BorderLayout(0, 10));
        painelNovoItem.setLayout(new java.awt.GridLayout(0, 1, 0, 6));
        lblNovo.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        lblNovo.setText("Adicionar itens");
        painelNovoItem.add(lblNovo);

        painelNovoItem.add(cmbProduto);

        painelQtd.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        lblQtd.setText("Qtd:");
        painelQtd.add(lblQtd);

        spnQtd.setModel(new javax.swing.SpinnerNumberModel(1, 1, null, 1));
        spnQtd.setPreferredSize(new java.awt.Dimension(70, 32));
        painelQtd.add(spnQtd);

        btnAdicionar.setText("Adicionar");
        btnAdicionar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAdicionarActionPerformed(evt);
            }
        });
        painelQtd.add(btnAdicionar);

        painelNovoItem.add(painelQtd);

        lblRascunho.setText("Itens a enviar");
        painelNovoItem.add(lblRascunho);

        painelLateral.add(painelNovoItem, java.awt.BorderLayout.PAGE_START);

        scrollRascunho.setViewportView(tabelaRascunho);
        painelLateral.add(scrollRascunho, java.awt.BorderLayout.CENTER);

        painelEnviar.setLayout(new java.awt.GridLayout(0, 1, 0, 6));
        btnTirarRascunho.setText("Tirar item da lista");
        btnTirarRascunho.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTirarRascunhoActionPerformed(evt);
            }
        });
        painelEnviar.add(btnTirarRascunho);

        btnEnviar.setText("Enviar para a cozinha");
        btnEnviar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEnviarActionPerformed(evt);
            }
        });
        painelEnviar.add(btnEnviar);

        painelLateral.add(painelEnviar, java.awt.BorderLayout.PAGE_END);

        painelCorpo.add(painelLateral, java.awt.BorderLayout.LINE_END);

        painelRodape.setLayout(new java.awt.BorderLayout());
        lblTotais.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblTotais.setText("Total");
        painelRodape.add(lblTotais, java.awt.BorderLayout.LINE_START);

        painelBotoes.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));
        btnTransferir.setText("Transferir mesa");
        btnTransferir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTransferirActionPerformed(evt);
            }
        });
        painelBotoes.add(btnTransferir);

        btnConta.setText("Pedir a conta");
        btnConta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnContaActionPerformed(evt);
            }
        });
        painelBotoes.add(btnConta);

        btnSaiu.setText("Saiu para entrega");
        btnSaiu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaiuActionPerformed(evt);
            }
        });
        painelBotoes.add(btnSaiu);

        btnEntregue.setText("Entregue");
        btnEntregue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEntregueActionPerformed(evt);
            }
        });
        painelBotoes.add(btnEntregue);

        btnCancelarPedido.setText("Cancelar pedido");
        btnCancelarPedido.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelarPedidoActionPerformed(evt);
            }
        });
        painelBotoes.add(btnCancelarPedido);

        btnReceber.setText("Receber");
        btnReceber.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReceberActionPerformed(evt);
            }
        });
        painelBotoes.add(btnReceber);

        painelRodape.add(painelBotoes, java.awt.BorderLayout.LINE_END);

        painelCorpo.add(painelRodape, java.awt.BorderLayout.PAGE_END);

        getContentPane().add(painelCorpo, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnVoltarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVoltarActionPerformed
        if (!rascunho.isEmpty() && JOptionPane.showConfirmDialog(this,
                "Há itens que ainda não foram enviados para a cozinha. Sair mesmo assim?", "Comanda",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        voltar();
    }//GEN-LAST:event_btnVoltarActionPerformed

    private void btnRemoverItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoverItemActionPerformed
        removerItem();
    }//GEN-LAST:event_btnRemoverItemActionPerformed

    private void btnAdicionarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAdicionarActionPerformed
        adicionar();
    }//GEN-LAST:event_btnAdicionarActionPerformed

    private void btnTirarRascunhoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTirarRascunhoActionPerformed
        int linha = tabelaRascunho.getSelectedRow();
        if (linha >= 0) {
            rascunho.remove(linha);
            atualizarRascunho();
        }
    }//GEN-LAST:event_btnTirarRascunhoActionPerformed

    private void btnEnviarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEnviarActionPerformed
        enviar();
    }//GEN-LAST:event_btnEnviarActionPerformed

    private void btnTransferirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTransferirActionPerformed
        transferir();
    }//GEN-LAST:event_btnTransferirActionPerformed

    private void btnContaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnContaActionPerformed
        acao(() -> pedidoService.solicitarConta(pedidoId, !pedido.isContaSolicitada()));
    }//GEN-LAST:event_btnContaActionPerformed

    private void btnSaiuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaiuActionPerformed
        acao(() -> pedidoService.atualizarEntrega(pedidoId, "SAIU_PARA_ENTREGA"));
    }//GEN-LAST:event_btnSaiuActionPerformed

    private void btnEntregueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEntregueActionPerformed
        acao(() -> pedidoService.atualizarEntrega(pedidoId, "ENTREGUE"));
    }//GEN-LAST:event_btnEntregueActionPerformed

    private void btnCancelarPedidoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarPedidoActionPerformed
        if (JOptionPane.showConfirmDialog(this, "Cancelar o pedido " + pedidoId + "? Os itens voltam para o estoque.",
                "Cancelar pedido", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
            acao(() -> pedidoService.cancelar(pedidoId));
        }
    }//GEN-LAST:event_btnCancelarPedidoActionPerformed

    private void btnReceberActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReceberActionPerformed
        if (!rascunho.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Envie ou tire os itens da lista antes de receber.");
            return;
        }
        if (DialogoPagamento.receber(this, pedidoId)) {
            carregar();
        }
    }//GEN-LAST:event_btnReceberActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdicionar;
    private javax.swing.JButton btnCancelarPedido;
    private javax.swing.JButton btnConta;
    private javax.swing.JButton btnEntregue;
    private javax.swing.JButton btnEnviar;
    private javax.swing.JButton btnReceber;
    private javax.swing.JButton btnRemoverItem;
    private javax.swing.JButton btnSaiu;
    private javax.swing.JButton btnTirarRascunho;
    private javax.swing.JButton btnTransferir;
    private javax.swing.JButton btnVoltar;
    private javax.swing.JComboBox<String> cmbProduto;
    private javax.swing.JLabel lblItens;
    private javax.swing.JLabel lblNovo;
    private javax.swing.JLabel lblQtd;
    private javax.swing.JLabel lblRascunho;
    private javax.swing.JLabel lblSubtitulo;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblTotais;
    private javax.swing.JPanel painelAcoesItens;
    private javax.swing.JPanel painelAcoesTopo;
    private javax.swing.JPanel painelBotoes;
    private javax.swing.JPanel painelCabecalho;
    private javax.swing.JPanel painelCorpo;
    private javax.swing.JPanel painelEnviar;
    private javax.swing.JPanel painelItens;
    private javax.swing.JPanel painelLateral;
    private javax.swing.JPanel painelNovoItem;
    private javax.swing.JPanel painelQtd;
    private javax.swing.JPanel painelRodape;
    private javax.swing.JPanel painelTitulo;
    private javax.swing.JScrollPane scrollItens;
    private javax.swing.JScrollPane scrollRascunho;
    private javax.swing.JSpinner spnQtd;
    private javax.swing.JTable tabelaItens;
    private javax.swing.JTable tabelaRascunho;
    // End of variables declaration//GEN-END:variables
}
