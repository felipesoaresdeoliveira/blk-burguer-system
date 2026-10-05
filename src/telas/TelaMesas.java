package telas;

import dao.MesaDAO;
import entidades.Mesa;
import entidades.Modulo;
import entidades.Pedido;
import entidades.TipoPedido;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import service.PedidoService;
import service.Sessao;
import ui.CartaoMesa;
import ui.Tema;

/** Mapa de mesas (#27): status por cor e texto, abertura de comanda, reserva e cadastro. */
public class TelaMesas extends javax.swing.JFrame {

    private final MesaDAO mesaDAO = new MesaDAO();
    private final PedidoService pedidoService = new PedidoService();
    private final boolean podeGerenciar = Sessao.pode(Modulo.CADASTROS);
    private final Timer timer;
    private List<Mesa> mesas = new ArrayList<>();
    private boolean carregando;

    public TelaMesas() {
        initComponents();
        Tema.janela(this);
        Tema.titulo(lblTitulo);
        Tema.secundario(lblSubtitulo);
        Tema.transparente(painelCabecalho, painelTitulo, painelAcoesTopo, painelCorpo);
        btnNovaMesa.setVisible(podeGerenciar);
        scrollMapa.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        scrollMapa.getViewport().setBackground(Tema.FUNDO);
        scrollMapa.getVerticalScrollBar().setUnitIncrement(24);
        painelMapa.setBackground(Tema.FUNDO);
        painelMapa.setLayout(new java.awt.GridLayout(0, 5, 16, 16));
        timer = new Timer(5000, e -> carregar());
        timer.start();
        carregar();
        Tema.tamanhoPadrao(this);
    }

    private void carregar() {
        if (carregando) {
            return;
        }
        carregando = true;
        new SwingWorker<List<Mesa>, Void>() {
            @Override
            protected List<Mesa> doInBackground() throws Exception {
                return mesaDAO.listarComStatus();
            }

            @Override
            protected void done() {
                carregando = false;
                try {
                    mesas = get();
                    montarMapa();
                } catch (Exception e) {
                    lblLegenda.setText("Sem conexão com o banco - tentando novamente...");
                    lblLegenda.setForeground(Tema.PERIGO);
                }
            }
        }.execute();
    }

    private void montarMapa() {
        painelMapa.removeAll();
        int[] cont = new int[Mesa.Status.values().length];
        for (Mesa m : mesas) {
            cont[m.getStatus().ordinal()]++;
            painelMapa.add(new CartaoMesa(m, this::clicar, e -> menu(m, e)));
        }
        if (mesas.isEmpty()) {
            javax.swing.JLabel vazio = new javax.swing.JLabel(podeGerenciar
                    ? "Nenhuma mesa cadastrada. Use \"Nova mesa\"." : "Nenhuma mesa cadastrada.");
            vazio.setForeground(Tema.TEXTO_FRACO);
            painelMapa.add(vazio);
        }
        StringBuilder legenda = new StringBuilder("<html>");
        for (Mesa.Status s : Mesa.Status.values()) {
            java.awt.Color c = CartaoMesa.cor(s);
            legenda.append(String.format("<span style='color:#%02x%02x%02x'>●</span> %d %s &nbsp;&nbsp;&nbsp;",
                    c.getRed(), c.getGreen(), c.getBlue(), cont[s.ordinal()], s.toString().toLowerCase()));
        }
        lblLegenda.setText(legenda + "</html>");
        lblLegenda.setForeground(Tema.TEXTO_SECUNDARIO);
        painelMapa.revalidate();
        painelMapa.repaint();
    }

    private void clicar(Mesa m) {
        if (m.getPedidoId() > 0) {
            abrirComanda(m.getPedidoId());
            return;
        }
        if (m.getStatus() == Mesa.Status.RESERVADA
                && JOptionPane.showConfirmDialog(this, "A mesa " + m.getNumero() + " está reservada. Abrir a comanda mesmo assim?",
                        "Mesa reservada", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        String ident = JOptionPane.showInputDialog(this,
                "Abrir comanda da mesa " + m.getNumero() + ".\nNome do cliente ou observação (opcional):",
                "Abrir mesa", JOptionPane.QUESTION_MESSAGE);
        if (ident == null) {
            return;
        }
        try {
            Pedido novo = new Pedido();
            novo.setTipo(TipoPedido.MESA);
            novo.setMesaId(m.getId());
            novo.setIdentificacao(ident.trim());
            Pedido aberto = pedidoService.abrir(novo);
            if (m.isReservada()) {
                m.setReservada(false);
                mesaDAO.atualizar(m);
            }
            abrirComanda(aberto.getId());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Mesas", JOptionPane.WARNING_MESSAGE);
            carregar();
        }
    }

    private void abrirComanda(int pedidoId) {
        timer.stop();
        dispose();
        new TelaComanda(pedidoId, TelaComanda.Origem.MESAS).setVisible(true);
    }

    /** Botão direito: reservar/liberar e, para gerente/admin, editar e remover. */
    private void menu(Mesa m, MouseEvent e) {
        JPopupMenu menu = new JPopupMenu();
        if (m.getPedidoId() == 0) {
            JMenuItem reservar = new JMenuItem(m.isReservada() ? "Liberar reserva" : "Reservar mesa");
            reservar.addActionListener(a -> {
                m.setReservada(!m.isReservada());
                salvar(m);
            });
            menu.add(reservar);
        }
        if (podeGerenciar) {
            JMenuItem editar = new JMenuItem("Editar número/lugares");
            editar.addActionListener(a -> editar(m));
            menu.add(editar);
            JMenuItem remover = new JMenuItem("Remover mesa");
            remover.setEnabled(m.getPedidoId() == 0);
            remover.addActionListener(a -> {
                if (JOptionPane.showConfirmDialog(this, "Remover a mesa " + m.getNumero() + " do mapa?", "Mesas",
                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    try {
                        mesaDAO.desativar(m.getId());
                        carregar();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, ex.getMessage());
                    }
                }
            });
            menu.add(remover);
        }
        if (menu.getComponentCount() > 0) {
            menu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    private void salvar(Mesa m) {
        try {
            mesaDAO.atualizar(m);
            carregar();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Mesas", JOptionPane.WARNING_MESSAGE);
        }
    }

    /** Número e lugares; retorna false se cancelado ou inválido. */
    private boolean pedirDados(Mesa m, String titulo) {
        javax.swing.JSpinner numero = new javax.swing.JSpinner(new javax.swing.SpinnerNumberModel(Math.max(1, m.getNumero()), 1, 999, 1));
        javax.swing.JSpinner lugares = new javax.swing.JSpinner(new javax.swing.SpinnerNumberModel(m.getLugares(), 1, 30, 1));
        javax.swing.JPanel p = new javax.swing.JPanel(new java.awt.GridLayout(0, 2, 8, 8));
        p.add(new javax.swing.JLabel("Número da mesa:"));
        p.add(numero);
        p.add(new javax.swing.JLabel("Lugares:"));
        p.add(lugares);
        if (JOptionPane.showConfirmDialog(this, p, titulo, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
                != JOptionPane.OK_OPTION) {
            return false;
        }
        m.setNumero((Integer) numero.getValue());
        m.setLugares((Integer) lugares.getValue());
        return true;
    }

    private void editar(Mesa m) {
        if (pedirDados(m, "Editar mesa " + m.getNumero())) {
            salvar(m);
        }
    }

    private void novaMesa() {
        Mesa m = new Mesa();
        int maior = 0;
        for (Mesa x : mesas) {
            maior = Math.max(maior, x.getNumero());
        }
        m.setNumero(maior + 1);
        if (!pedirDados(m, "Nova mesa")) {
            return;
        }
        for (Mesa x : mesas) {
            if (x.getNumero() == m.getNumero()) {
                JOptionPane.showMessageDialog(this, "Já existe a mesa " + m.getNumero() + ".", "Mesas", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        try {
            mesaDAO.inserir(m);
            carregar();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Mesas", JOptionPane.WARNING_MESSAGE);
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
        btnNovaMesa = new javax.swing.JButton();
        btnAtualizar = new javax.swing.JButton();
        btnVoltar = new javax.swing.JButton();
        painelCorpo = new javax.swing.JPanel();
        lblLegenda = new javax.swing.JLabel();
        scrollMapa = new javax.swing.JScrollPane();
        painelMapa = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("BLK Burguer - Mesas");
        setMinimumSize(new java.awt.Dimension(1180, 720));

        painelCabecalho.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 24, 14, 24));
        painelCabecalho.setLayout(new java.awt.BorderLayout());
        painelTitulo.setLayout(new java.awt.GridLayout(2, 1, 0, 2));
        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblTitulo.setText("Mesas");
        painelTitulo.add(lblTitulo);

        lblSubtitulo.setText("Clique numa mesa para abrir ou ver a comanda");
        painelTitulo.add(lblSubtitulo);

        painelCabecalho.add(painelTitulo, java.awt.BorderLayout.LINE_START);

        painelAcoesTopo.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 8));
        btnNovaMesa.setText("Nova mesa");
        btnNovaMesa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNovaMesaActionPerformed(evt);
            }
        });
        painelAcoesTopo.add(btnNovaMesa);

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
        lblLegenda.setText(" ");
        painelCorpo.add(lblLegenda, java.awt.BorderLayout.PAGE_START);

        scrollMapa.setViewportView(painelMapa);
        painelCorpo.add(scrollMapa, java.awt.BorderLayout.CENTER);

        getContentPane().add(painelCorpo, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnNovaMesaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNovaMesaActionPerformed
        novaMesa();
    }//GEN-LAST:event_btnNovaMesaActionPerformed

    private void btnAtualizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAtualizarActionPerformed
        carregar();
    }//GEN-LAST:event_btnAtualizarActionPerformed

    private void btnVoltarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVoltarActionPerformed
        timer.stop();
        Main.voltar(this);
    }//GEN-LAST:event_btnVoltarActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAtualizar;
    private javax.swing.JButton btnNovaMesa;
    private javax.swing.JButton btnVoltar;
    private javax.swing.JLabel lblLegenda;
    private javax.swing.JLabel lblSubtitulo;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel painelAcoesTopo;
    private javax.swing.JPanel painelCabecalho;
    private javax.swing.JPanel painelCorpo;
    private javax.swing.JPanel painelMapa;
    private javax.swing.JPanel painelTitulo;
    private javax.swing.JScrollPane scrollMapa;
    // End of variables declaration//GEN-END:variables
}
