package telas;

import dao.AuditoriaDAO;
import entidades.AcaoAuditoria;
import entidades.Modulo;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import javax.swing.JOptionPane;
import service.Sessao;
import ui.Acabamento;
import ui.Tema;

/** Consulta da trilha de auditoria (#37): filtros simples, sem edição. */
public class TelaAuditoria extends javax.swing.JPanel implements Navegavel {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final int LIMITE = 1000;

    private final AuditoriaDAO auditoriaDAO = new AuditoriaDAO();

    public TelaAuditoria() {
        initComponents();
        Acabamento.vazio(tabelaAuditoria, "Nenhuma ação registrada com esses filtros");
        Tema.titulo(lblTitulo);
        Tema.secundario(lblSubtitulo, lblRodape);
        Tema.primario(btnFiltrar);
        Tema.transparente(painelCabecalho, painelTitulo, painelAcoesTopo, painelCorpo, painelFiltro);
        cmbAcao.addItem("Todas");
        for (AcaoAuditoria a : AcaoAuditoria.values()) {
            cmbAcao.addItem(a.toString());
        }
        txtDe.putClientProperty("JTextField.placeholderText", "dd/mm/aaaa");
        txtAte.putClientProperty("JTextField.placeholderText", "dd/mm/aaaa");
        txtUsuario.putClientProperty("JTextField.placeholderText", "nome");
        LocalDate hoje = LocalDate.now();
        txtDe.setText(hoje.minusDays(6).format(DATA));
        txtAte.setText(hoje.format(DATA));
        cmbAcao.addActionListener(e -> carregar());
        putClientProperty("BLK.botaoPadrao", btnFiltrar);
        carregar();
    }

    private static LocalDate ler(javax.swing.JTextField campo) {
        try {
            return LocalDate.parse(campo.getText().trim(), DATA);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private void carregar() {
        if (!Sessao.pode(Modulo.AUDITORIA)) {
            lblRodape.setText("Seu perfil não tem acesso à auditoria.");
            return;
        }
        LocalDate de = ler(txtDe), ate = ler(txtAte);
        if (de == null || ate == null || de.isAfter(ate)) {
            JOptionPane.showMessageDialog(this, "Informe um período válido (dd/mm/aaaa).", "Auditoria", JOptionPane.WARNING_MESSAGE);
            return;
        }
        AcaoAuditoria acao = cmbAcao.getSelectedIndex() <= 0 ? null : AcaoAuditoria.values()[cmbAcao.getSelectedIndex() - 1];
        javax.swing.table.DefaultTableModel modelo = new javax.swing.table.DefaultTableModel(
                new String[]{"Data e hora", "Usuário", "Ação", "Referência", "Detalhe"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        try {
            List<AuditoriaDAO.Registro> registros = auditoriaDAO.listar(de, ate, acao, txtUsuario.getText(), LIMITE);
            for (AuditoriaDAO.Registro r : registros) {
                modelo.addRow(new Object[]{r.data.format(DATA_HORA), r.usuario == null ? "-" : r.usuario,
                    r.getAcaoDescricao(), r.referencia == null ? "-" : r.referencia, r.detalhe == null ? "" : r.detalhe});
            }
            lblRodape.setText(registros.size() >= LIMITE
                    ? "Mostrando as " + LIMITE + " ações mais recentes: refine o período para ver as demais."
                    : registros.size() + " ação(ões) no período.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage());
        }
        tabelaAuditoria.setModel(modelo);
        int[] larguras = {140, 130, 150, 150, 420};
        for (int i = 0; i < larguras.length; i++) {
            tabelaAuditoria.getColumnModel().getColumn(i).setPreferredWidth(larguras[i]);
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
        painelFiltro = new javax.swing.JPanel();
        lblDe = new javax.swing.JLabel();
        txtDe = new javax.swing.JTextField();
        lblAte = new javax.swing.JLabel();
        txtAte = new javax.swing.JTextField();
        lblAcao = new javax.swing.JLabel();
        cmbAcao = new javax.swing.JComboBox<>();
        lblUsuario = new javax.swing.JLabel();
        txtUsuario = new javax.swing.JTextField();
        btnFiltrar = new javax.swing.JButton();
        scrollAuditoria = new javax.swing.JScrollPane();
        tabelaAuditoria = new javax.swing.JTable();
        lblRodape = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        painelCabecalho.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 24, 14, 24));
        painelCabecalho.setLayout(new java.awt.BorderLayout());
        painelTitulo.setLayout(new java.awt.GridLayout(2, 1, 0, 2));
        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblTitulo.setText("Auditoria");
        painelTitulo.add(lblTitulo);

        lblSubtitulo.setText("Ações críticas registradas automaticamente. Somente leitura: o histórico não pode ser alterado.");
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
        painelFiltro.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        lblDe.setText("De:");
        painelFiltro.add(lblDe);

        txtDe.setPreferredSize(new java.awt.Dimension(105, 30));
        painelFiltro.add(txtDe);

        lblAte.setText("Até:");
        painelFiltro.add(lblAte);

        txtAte.setPreferredSize(new java.awt.Dimension(105, 30));
        painelFiltro.add(txtAte);

        lblAcao.setText("Ação:");
        painelFiltro.add(lblAcao);

        painelFiltro.add(cmbAcao);

        lblUsuario.setText("Usuário:");
        painelFiltro.add(lblUsuario);

        txtUsuario.setPreferredSize(new java.awt.Dimension(140, 30));
        painelFiltro.add(txtUsuario);

        btnFiltrar.setText("Filtrar");
        btnFiltrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFiltrarActionPerformed(evt);
            }
        });
        painelFiltro.add(btnFiltrar);

        painelCorpo.add(painelFiltro, java.awt.BorderLayout.PAGE_START);

        scrollAuditoria.setViewportView(tabelaAuditoria);
        painelCorpo.add(scrollAuditoria, java.awt.BorderLayout.CENTER);

        lblRodape.setText(" ");
        painelCorpo.add(lblRodape, java.awt.BorderLayout.PAGE_END);

        add(painelCorpo, java.awt.BorderLayout.CENTER);

    }// </editor-fold>//GEN-END:initComponents

    private void btnVoltarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVoltarActionPerformed
        Main.voltar(this);
    }//GEN-LAST:event_btnVoltarActionPerformed

    private void btnFiltrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFiltrarActionPerformed
        carregar();
    }//GEN-LAST:event_btnFiltrarActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnFiltrar;
    private javax.swing.JButton btnVoltar;
    private javax.swing.JComboBox<String> cmbAcao;
    private javax.swing.JLabel lblAcao;
    private javax.swing.JLabel lblAte;
    private javax.swing.JLabel lblDe;
    private javax.swing.JLabel lblRodape;
    private javax.swing.JLabel lblSubtitulo;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblUsuario;
    private javax.swing.JPanel painelAcoesTopo;
    private javax.swing.JPanel painelCabecalho;
    private javax.swing.JPanel painelCorpo;
    private javax.swing.JPanel painelFiltro;
    private javax.swing.JPanel painelTitulo;
    private javax.swing.JScrollPane scrollAuditoria;
    private javax.swing.JTable tabelaAuditoria;
    private javax.swing.JTextField txtAte;
    private javax.swing.JTextField txtDe;
    private javax.swing.JTextField txtUsuario;
    // End of variables declaration//GEN-END:variables
}
