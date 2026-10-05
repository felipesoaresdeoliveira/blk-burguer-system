package telas;

import entidades.FormaPagamento;
import entidades.Pedido;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import service.PedidoService;
import ui.Tema;

/**
 * Recebimento de um pedido (#30): um ou mais pagamentos, em formas
 * diferentes, até o saldo zerar. Permite dividir o saldo por pessoas.
 */
public final class DialogoPagamento {

    private final PedidoService pedidoService = new PedidoService();
    private final JDialog dialogo;
    private final int pedidoId;
    private Pedido pedido;
    private boolean houvePagamento;

    private final JLabel lblTotal = valor();
    private final JLabel lblPago = valor();
    private final JLabel lblSaldo = valor();
    private final JTable tabela = new JTable();
    private final JComboBox<FormaPagamento> cmbForma = new JComboBox<>(FormaPagamento.values());
    private final JTextField txtValor = new JTextField(9);
    private final JTextField txtRecebido = new JTextField(9);
    private final JLabel lblTroco = new JLabel(" ");
    private final JSpinner spnPessoas = new JSpinner(new SpinnerNumberModel(2, 2, 30, 1));
    private final JButton btnRegistrar = new JButton("Registrar pagamento");

    private DialogoPagamento(Component pai, int pedidoId) {
        this.pedidoId = pedidoId;
        dialogo = new JDialog(SwingUtilities.getWindowAncestor(pai), "Receber pagamento",
                java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialogo.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        montar();
        dialogo.setSize(720, 560);
        dialogo.setLocationRelativeTo(pai);
    }

    /**
     * Abre o recebimento do pedido. Retorna true se ao menos um pagamento
     * foi registrado (a tela que chamou deve se atualizar).
     */
    public static boolean receber(Component pai, int pedidoId) {
        DialogoPagamento d = new DialogoPagamento(pai, pedidoId);
        if (!d.carregar()) {
            return false;
        }
        d.dialogo.setVisible(true);
        return d.houvePagamento;
    }

    private static JLabel valor() {
        JLabel l = new JLabel("-");
        l.setFont(l.getFont().deriveFont(Font.BOLD, 22f));
        return l;
    }

    private static JPanel bloco(String titulo, JLabel valor) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        Tema.cartao(p);
        JLabel t = new JLabel(titulo);
        t.setForeground(Tema.TEXTO_SECUNDARIO);
        p.add(t, BorderLayout.NORTH);
        p.add(valor, BorderLayout.CENTER);
        return p;
    }

    private void montar() {
        JPanel raiz = new JPanel(new BorderLayout(0, 14));
        raiz.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JPanel resumo = new JPanel(new GridLayout(1, 3, 12, 0));
        resumo.add(bloco("Total", lblTotal));
        resumo.add(bloco("Já pago", lblPago));
        resumo.add(bloco("Falta pagar", lblSaldo));
        lblSaldo.setForeground(Tema.DESTAQUE);
        raiz.add(resumo, BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(0, 6));
        JLabel tit = new JLabel("Pagamentos registrados");
        tit.setFont(tit.getFont().deriveFont(Font.BOLD));
        centro.add(tit, BorderLayout.NORTH);
        centro.add(new JScrollPane(tabela), BorderLayout.CENTER);
        raiz.add(centro, BorderLayout.CENTER);

        // Formulário do próximo pagamento
        JPanel form = new JPanel(new GridLayout(0, 4, 10, 8));
        form.add(new JLabel("Forma:"));
        form.add(cmbForma);
        form.add(new JLabel("Valor deste pagamento:"));
        form.add(txtValor);
        form.add(new JLabel("Valor recebido (dinheiro):"));
        form.add(txtRecebido);
        form.add(new JLabel("Troco:"));
        form.add(lblTroco);
        JPanel dividir = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 6, 0));
        dividir.add(new JLabel("Dividir o que falta por"));
        dividir.add(spnPessoas);
        dividir.add(new JLabel("pessoas"));
        JButton btnDividir = new JButton("Dividir");
        btnDividir.addActionListener(e -> dividir());
        dividir.add(btnDividir);
        JButton btnTudo = new JButton("Valor total restante");
        btnTudo.addActionListener(e -> txtValor.setText(formatar(pedido.getSaldo())));
        dividir.add(btnTudo);

        JPanel sul = new JPanel(new BorderLayout(0, 10));
        sul.add(dividir, BorderLayout.NORTH);
        sul.add(form, BorderLayout.CENTER);
        JPanel botoes = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));
        JButton btnFechar = new JButton("Fechar");
        btnFechar.addActionListener(e -> dialogo.dispose());
        Tema.primario(btnRegistrar);
        btnRegistrar.addActionListener(e -> registrar());
        botoes.add(btnFechar);
        botoes.add(btnRegistrar);
        sul.add(botoes, BorderLayout.SOUTH);
        raiz.add(sul, BorderLayout.SOUTH);

        cmbForma.addActionListener(e -> atualizarTroco());
        javax.swing.event.DocumentListener troco = new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { atualizarTroco(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { atualizarTroco(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { atualizarTroco(); }
        };
        txtValor.getDocument().addDocumentListener(troco);
        txtRecebido.getDocument().addDocumentListener(troco);
        dialogo.getRootPane().setDefaultButton(btnRegistrar);
        dialogo.setContentPane(raiz);
    }

    private static String formatar(double v) {
        return String.format("%.2f", v);
    }

    private static Double ler(String texto) {
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

    private boolean carregar() {
        try {
            pedido = pedidoService.buscar(pedidoId);
            if (pedido == null) {
                JOptionPane.showMessageDialog(dialogo, "Pedido não encontrado.");
                return false;
            }
            dialogo.setTitle("Receber pagamento - pedido " + pedido.getId() + " | " + pedido.getDescricao());
            lblTotal.setText(Tema.reais(pedido.getTotal()));
            lblPago.setText(Tema.reais(pedido.getPago()));
            lblSaldo.setText(Tema.reais(pedido.getSaldo()));
            javax.swing.table.DefaultTableModel modelo = new javax.swing.table.DefaultTableModel(
                    new String[]{"Forma", "Valor", "Recebido", "Troco"}, 0) {
                @Override
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            };
            List<Object[]> pagamentos = pedidoService.pagamentos(pedidoId);
            for (Object[] p : pagamentos) {
                modelo.addRow(new Object[]{p[0], Tema.reais((Double) p[1]), Tema.reais((Double) p[2]), Tema.reais((Double) p[3])});
            }
            tabela.setModel(modelo);
            txtValor.setText(formatar(pedido.getSaldo()));
            txtRecebido.setText("");
            btnRegistrar.setEnabled(pedido.getSaldo() > 0);
            atualizarTroco();
            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(dialogo, "Erro: " + e.getMessage());
            return false;
        }
    }

    /** Preenche o valor com a parte de uma pessoa; a última parte acerta os centavos. */
    private void dividir() {
        int pessoas = (Integer) spnPessoas.getValue();
        double parte = Math.floor(pedido.getSaldo() / pessoas * 100) / 100.0;
        txtValor.setText(formatar(parte));
    }

    private void atualizarTroco() {
        boolean dinheiro = cmbForma.getSelectedItem() == FormaPagamento.DINHEIRO;
        txtRecebido.setEnabled(dinheiro);
        Double valor = ler(txtValor.getText());
        if (!dinheiro) {
            lblTroco.setText("-");
            lblTroco.setForeground(Tema.TEXTO_FRACO);
            return;
        }
        Double recebido = ler(txtRecebido.getText());
        if (valor == null || recebido == null) {
            lblTroco.setText("-");
            lblTroco.setForeground(Tema.TEXTO_FRACO);
            return;
        }
        double troco = Math.round((recebido - valor) * 100) / 100.0;
        lblTroco.setText(troco < 0 ? "Faltam " + Tema.reais(-troco) : Tema.reais(troco));
        lblTroco.setForeground(troco < 0 ? Tema.PERIGO : Tema.SUCESSO);
    }

    private void registrar() {
        FormaPagamento forma = (FormaPagamento) cmbForma.getSelectedItem();
        Double valor = ler(txtValor.getText());
        if (valor == null || valor <= 0) {
            JOptionPane.showMessageDialog(dialogo, "Informe o valor deste pagamento.", "Pagamento", JOptionPane.WARNING_MESSAGE);
            return;
        }
        double recebido = valor;
        if (forma == FormaPagamento.DINHEIRO) {
            Double r = ler(txtRecebido.getText());
            recebido = r == null ? valor : r;
        }
        try {
            Pedido atualizado = pedidoService.receberPagamento(pedidoId, forma, valor, recebido);
            houvePagamento = true;
            double troco = Math.round((recebido - valor) * 100) / 100.0;
            if (forma == FormaPagamento.DINHEIRO && troco > 0) {
                JOptionPane.showMessageDialog(dialogo, "Troco: " + Tema.reais(troco), "Troco", JOptionPane.INFORMATION_MESSAGE);
            }
            if (atualizado.getSaldo() <= 0) {
                JOptionPane.showMessageDialog(dialogo, "Pedido " + pedidoId + " pago por completo!", "Pagamento",
                        JOptionPane.INFORMATION_MESSAGE);
                dialogo.dispose();
                return;
            }
            carregar();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(dialogo, e.getMessage(), "Pagamento", JOptionPane.WARNING_MESSAGE);
        }
    }
}
