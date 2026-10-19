package telas;

import dao.EstoqueDAO;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import service.EstoqueService;
import ui.Acabamento;
import ui.Tema;

/**
 * Histórico de movimentações de estoque (#21, #34): entradas, ajustes e
 * perdas, com quem fez e o valor perdido no período.
 */
public final class DialogoMovimentacoes {

    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM HH:mm");
    private static final String[] PERIODOS = {"Últimos 7 dias", "Últimos 30 dias", "Este mês", "Últimos 90 dias"};
    private static final String[] TIPOS = {"Todos", "Entradas", "Ajustes", "Perdas"};

    private final EstoqueService estoqueService = new EstoqueService();
    private final JDialog dialogo;
    private final JTable tabela = new JTable();
    private final JComboBox<String> cmbPeriodo = new JComboBox<>(PERIODOS);
    private final JComboBox<String> cmbTipo = new JComboBox<>(TIPOS);
    private final JLabel lblResumo = new JLabel(" ");

    private DialogoMovimentacoes(Component pai) {
        dialogo = new JDialog(SwingUtilities.getWindowAncestor(pai), "Histórico do estoque",
                java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialogo.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        montar();
        dialogo.setSize(900, 560);
        dialogo.setLocationRelativeTo(pai);
    }

    public static void mostrar(Component pai) {
        DialogoMovimentacoes d = new DialogoMovimentacoes(pai);
        d.carregar();
        d.dialogo.setVisible(true);
    }

    private void montar() {
        JPanel raiz = new JPanel(new BorderLayout(0, 12));
        raiz.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filtros.add(new JLabel("Período:"));
        filtros.add(cmbPeriodo);
        filtros.add(new JLabel("Tipo:"));
        filtros.add(cmbTipo);
        cmbPeriodo.addActionListener(e -> carregar());
        cmbTipo.addActionListener(e -> carregar());
        raiz.add(filtros, BorderLayout.NORTH);
        Acabamento.vazio(tabela, "Nenhuma movimentação no período");
        raiz.add(new JScrollPane(tabela), BorderLayout.CENTER);

        JPanel sul = new JPanel(new BorderLayout());
        lblResumo.setFont(lblResumo.getFont().deriveFont(Font.BOLD));
        sul.add(lblResumo, BorderLayout.WEST);
        JButton btnFechar = new JButton("Fechar");
        btnFechar.addActionListener(e -> dialogo.dispose());
        sul.add(btnFechar, BorderLayout.EAST);
        raiz.add(sul, BorderLayout.SOUTH);
        dialogo.setContentPane(raiz);
        Acabamento.aplicar(raiz, false);
    }

    private LocalDate inicio() {
        LocalDate hoje = LocalDate.now();
        switch (cmbPeriodo.getSelectedIndex()) {
            case 1: return hoje.minusDays(29);
            case 2: return hoje.withDayOfMonth(1);
            case 3: return hoje.minusDays(89);
            default: return hoje.minusDays(6);
        }
    }

    private String tipo() {
        switch (cmbTipo.getSelectedIndex()) {
            case 1: return EstoqueService.ENTRADA;
            case 2: return EstoqueService.AJUSTE;
            case 3: return EstoqueService.PERDA;
            default: return null;
        }
    }

    private static String nomeTipo(String tipo) {
        switch (tipo) {
            case EstoqueService.ENTRADA: return "Entrada";
            case EstoqueService.AJUSTE: return "Ajuste";
            case EstoqueService.PERDA: return "Perda";
            default: return tipo;
        }
    }

    private void carregar() {
        DefaultTableModel modelo = new DefaultTableModel(
                new String[]{"Data", "Item", "Tipo", "Antes", "Depois", "Diferença", "Motivo", "Por", "Valor perdido"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        try {
            LocalDate de = inicio(), ate = LocalDate.now();
            List<EstoqueDAO.Movimentacao> movs = estoqueService.historico(de, ate, tipo());
            for (EstoqueDAO.Movimentacao m : movs) {
                int dif = m.getDiferenca();
                modelo.addRow(new Object[]{m.data.format(DATA_HORA), m.item, nomeTipo(m.tipo), m.anterior, m.nova,
                    (dif > 0 ? "+" : "") + dif, m.motivo == null ? "-" : m.motivo, m.usuario == null ? "-" : m.usuario,
                    EstoqueService.PERDA.equals(m.tipo) ? Tema.reais(m.getValorPerdido()) : "-"});
            }
            double perdas = estoqueService.totalPerdas(de, ate);
            lblResumo.setText(movs.size() + " movimentação(ões)  |  perdas no período: " + Tema.reais(perdas));
            lblResumo.setForeground(perdas > 0 ? Tema.PERIGO : Tema.TEXTO_SECUNDARIO);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(dialogo, "Erro: " + e.getMessage());
        }
        tabela.setModel(modelo);
        int[] larguras = {80, 170, 70, 55, 60, 70, 200, 100, 95};
        for (int i = 0; i < larguras.length; i++) {
            tabela.getColumnModel().getColumn(i).setPreferredWidth(larguras[i]);
        }
    }
}
