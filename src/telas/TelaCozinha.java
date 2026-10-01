package telas;

import entidades.Pedido;
import entidades.StatusCozinha;
import entidades.Venda;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import service.PedidoService;
import ui.CartaoCozinha;
import ui.Tema;

/**
 * Painel de produção da cozinha, feito para ficar numa TV: três colunas
 * (na fila, em preparo, pronto), atualização automática a cada poucos
 * segundos e alerta quando chega pedido novo.
 */
public class TelaCozinha extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(TelaCozinha.class.getName());
    /** Intervalo de atualização automática. */
    private static final int ATUALIZAR_MS = 4000;
    /** Tempo em que um pedido recém-chegado fica destacado como "NOVO". */
    private static final long NOVO_SEGUNDOS = 45;

    private final PedidoService pedidoService = new PedidoService();
    private Map<String, Map<String, Integer>> fichas = new HashMap<>();
    private long fichasCarregadasEm;
    /** Chave pedido:etapa já vista, para detectar pedido novo e tocar o alerta. */
    private final Set<Integer> pedidosVistos = new HashSet<>();
    private final Map<Integer, LocalDateTime> chegada = new HashMap<>();
    private boolean primeiraCarga = true;
    private boolean carregando;
    private List<Pedido> ultimaFila = new ArrayList<>();
    private final Timer timerAtualizar;
    private final Timer timerRelogio;

    public TelaCozinha() {
        initComponents();
        aplicarVisual();
        timerAtualizar = new Timer(ATUALIZAR_MS, e -> carregar());
        timerRelogio = new Timer(1000, e -> atualizarRelogio());
        timerAtualizar.start();
        timerRelogio.start();
        atualizarRelogio();
        carregar();
        Tema.tamanhoPadrao(this);

        // F11 alterna tela cheia; Esc sai da tela cheia.
        javax.swing.JRootPane raiz = getRootPane();
        raiz.registerKeyboardAction(e -> alternarTelaCheia(),
                javax.swing.KeyStroke.getKeyStroke("F11"), javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);
        raiz.registerKeyboardAction(e -> {
            if (isUndecorated()) {
                alternarTelaCheia();
            }
        }, javax.swing.KeyStroke.getKeyStroke("ESCAPE"), javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW);
        // Redesenha os cartões quando a largura da coluna muda.
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                montarColunas(ultimaFila);
            }
        });
    }

    /** Abre o painel em tela cheia (modo TV). */
    public static TelaCozinha abrirTelaCheia() {
        TelaCozinha t = new TelaCozinha();
        t.alternarTelaCheia();
        return t;
    }

    private void aplicarVisual() {
        Tema.janela(this);
        getContentPane().setBackground(Tema.FUNDO);
        for (JPanel p : new JPanel[]{painelTopo, painelMarca, painelContadores, painelAcoes, painelColunas,
            colFila, colPreparo, colPronto}) {
            p.setBackground(Tema.FUNDO);
        }
        painelTopo.setBackground(Tema.MENU);
        painelMarca.setOpaque(false);
        painelContadores.setOpaque(false);
        painelAcoes.setOpaque(false);
        painelTopo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Tema.BORDA), painelTopo.getBorder()));
        Tema.titulo(lblTitulo);
        Tema.secundario(lblSubtitulo);
        lblRelogio.setForeground(Tema.TEXTO);
        estilizarColuna(lblColFila, scrollFila, listaFila, new Color(0x3987E5));
        estilizarColuna(lblColPreparo, scrollPreparo, listaPreparo, Tema.DESTAQUE);
        estilizarColuna(lblColPronto, scrollPronto, listaPronto, Tema.SUCESSO);
        for (JButton b : new JButton[]{btnTelaCheia, btnSair}) {
            b.setFocusable(false);
        }
    }

    private void estilizarColuna(JLabel titulo, JScrollPane scroll, JPanel lista, Color cor) {
        titulo.setForeground(Tema.TEXTO);
        titulo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, cor), BorderFactory.createEmptyBorder(0, 2, 8, 0)));
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Tema.FUNDO);
        scroll.getVerticalScrollBar().setUnitIncrement(24);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        lista.setBackground(Tema.FUNDO);
        lista.setLayout(new BoxLayout(lista, BoxLayout.Y_AXIS));
    }

    private void atualizarRelogio() {
        lblRelogio.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
    }

    /** Busca a fila em segundo plano; a tela nunca trava esperando o banco. */
    private void carregar() {
        if (carregando) {
            return;
        }
        carregando = true;
        new SwingWorker<List<Pedido>, Void>() {
            @Override
            protected List<Pedido> doInBackground() throws Exception {
                if (System.currentTimeMillis() - fichasCarregadasEm > 60_000) {
                    fichas = pedidoService.fichasTecnicas();
                    fichasCarregadasEm = System.currentTimeMillis();
                }
                return pedidoService.filaCozinha();
            }

            @Override
            protected void done() {
                carregando = false;
                try {
                    List<Pedido> fila = get();
                    detectarNovos(fila);
                    ultimaFila = fila;
                    montarColunas(fila);
                    lblSubtitulo.setText("BLK Burguer | painel de produção | atualizado às "
                            + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                    Tema.secundario(lblSubtitulo);
                } catch (Exception e) {
                    logger.log(java.util.logging.Level.WARNING, "Falha ao atualizar a cozinha", e);
                    lblSubtitulo.setText("Sem conexão com o banco - tentando novamente...");
                    lblSubtitulo.setForeground(Tema.PERIGO);
                }
            }
        }.execute();
    }

    private void detectarNovos(List<Pedido> fila) {
        boolean chegouNovo = false;
        for (Pedido p : fila) {
            if (pedidosVistos.add(p.getId())) {
                if (!primeiraCarga) {
                    chegada.put(p.getId(), LocalDateTime.now());
                    chegouNovo = true;
                }
            }
        }
        primeiraCarga = false;
        if (chegouNovo) {
            java.awt.Toolkit.getDefaultToolkit().beep();
        }
    }

    private boolean ehNovo(int pedidoId) {
        LocalDateTime quando = chegada.get(pedidoId);
        return quando != null && java.time.Duration.between(quando, LocalDateTime.now()).getSeconds() < NOVO_SEGUNDOS;
    }

    private void montarColunas(List<Pedido> fila) {
        Map<StatusCozinha, List<Object[]>> porEtapa = new HashMap<>();
        for (StatusCozinha s : StatusCozinha.values()) {
            porEtapa.put(s, new ArrayList<>());
        }
        for (Pedido p : fila) {
            // Um pedido pode ter itens em etapas diferentes (ex.: mesa pediu mais depois).
            Map<StatusCozinha, List<Venda>> itens = new HashMap<>();
            for (Venda v : p.getItens()) {
                itens.computeIfAbsent(v.getStatusCozinha(), k -> new ArrayList<>()).add(v);
            }
            for (Map.Entry<StatusCozinha, List<Venda>> e : itens.entrySet()) {
                porEtapa.get(e.getKey()).add(new Object[]{p, e.getValue()});
            }
        }
        int nFila = preencher(listaFila, scrollFila, porEtapa.get(StatusCozinha.RECEBIDO), StatusCozinha.RECEBIDO,
                "Iniciar preparo", "Nenhum pedido na fila");
        int nPreparo = preencher(listaPreparo, scrollPreparo, porEtapa.get(StatusCozinha.EM_PREPARO), StatusCozinha.EM_PREPARO,
                "Marcar como pronto", "Nada em preparo");
        int nPronto = preencher(listaPronto, scrollPronto, porEtapa.get(StatusCozinha.PRONTO), StatusCozinha.PRONTO,
                "Entregue / retirado", "Nenhum pedido aguardando");
        contador(lblContFila, nFila, "na fila", new Color(0x3987E5));
        contador(lblContPreparo, nPreparo, "em preparo", Tema.DESTAQUE);
        contador(lblContPronto, nPronto, nPronto == 1 ? "pronto" : "prontos", Tema.SUCESSO);
    }

    private static void contador(JLabel l, int n, String texto, Color cor) {
        l.setText("<html><span style='color:" + hex(cor) + "; font-size:18pt'>" + n + "</span>&nbsp; " + texto + "</html>");
        l.setForeground(Tema.TEXTO_SECUNDARIO);
    }

    private static String hex(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }

    private int preencher(JPanel lista, JScrollPane scroll, List<Object[]> cartoes, StatusCozinha etapa,
            String textoAcao, String vazio) {
        int posicao = scroll.getVerticalScrollBar().getValue();
        lista.removeAll();
        // Largura útil da coluna (desconta a barra de rolagem, que pode aparecer).
        int largura = Math.max(260, scroll.getWidth() - scroll.getVerticalScrollBar().getPreferredSize().width - 4);
        if (cartoes.isEmpty()) {
            JLabel l = new JLabel(vazio);
            l.setFont(l.getFont().deriveFont(Font.PLAIN, 15f));
            l.setForeground(Tema.TEXTO_FRACO);
            l.setBorder(BorderFactory.createEmptyBorder(24, 4, 0, 0));
            l.setAlignmentX(Component.LEFT_ALIGNMENT);
            lista.add(l);
        }
        boolean primeiro = true;
        for (Object[] c : cartoes) {
            Pedido p = (Pedido) c[0];
            @SuppressWarnings("unchecked")
            List<Venda> itens = (List<Venda>) c[1];
            JButton acao = new JButton(textoAcao);
            acao.setFocusable(false);
            if (etapa == StatusCozinha.RECEBIDO) {
                Tema.primario(acao);
            } else if (etapa == StatusCozinha.PRONTO) {
                acao.putClientProperty("FlatLaf.style", "background: #0CA30C; foreground: #FFFFFF; font: bold; borderWidth: 0");
            }
            acao.addActionListener(e -> avancar(p.getId(), etapa, acao));
            long minutos = PedidoService.minutosDesde(p.getData());
            CartaoCozinha cartao = new CartaoCozinha(p, itens, etapa, minutos,
                    primeiro && etapa == StatusCozinha.RECEBIDO, ehNovo(p.getId()), fichas, acao, largura);
            cartao.setAlignmentX(Component.LEFT_ALIGNMENT);
            lista.add(cartao);
            lista.add(Box.createVerticalStrut(14));
            primeiro = false;
        }
        lista.revalidate();
        lista.repaint();
        javax.swing.SwingUtilities.invokeLater(() -> scroll.getVerticalScrollBar().setValue(posicao));
        return cartoes.size();
    }

    private void avancar(int pedidoId, StatusCozinha etapa, JButton botao) {
        botao.setEnabled(false);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                pedidoService.avancarCozinha(pedidoId, etapa);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (Exception e) {
                    Throwable causa = e.getCause() != null ? e.getCause() : e;
                    javax.swing.JOptionPane.showMessageDialog(TelaCozinha.this, causa.getMessage(),
                            "Cozinha", javax.swing.JOptionPane.WARNING_MESSAGE);
                }
                carregar();
            }
        }.execute();
    }

    /** Alterna entre janela e tela cheia sem bordas (modo TV). */
    private void alternarTelaCheia() {
        boolean cheia = !isUndecorated();
        dispose();
        setUndecorated(cheia);
        if (cheia) {
            setExtendedState(MAXIMIZED_BOTH);
        } else {
            setExtendedState(NORMAL);
            Tema.tamanhoPadrao(this);
        }
        btnTelaCheia.setText(cheia ? "Sair da tela cheia (Esc)" : "Tela cheia (F11)");
        setVisible(true);
    }

    private void encerrar() {
        timerAtualizar.stop();
        timerRelogio.stop();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        painelTopo = new javax.swing.JPanel();
        painelMarca = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        lblSubtitulo = new javax.swing.JLabel();
        painelContadores = new javax.swing.JPanel();
        lblContFila = new javax.swing.JLabel();
        lblContPreparo = new javax.swing.JLabel();
        lblContPronto = new javax.swing.JLabel();
        painelAcoes = new javax.swing.JPanel();
        lblRelogio = new javax.swing.JLabel();
        btnTelaCheia = new javax.swing.JButton();
        btnSair = new javax.swing.JButton();
        painelColunas = new javax.swing.JPanel();
        colFila = new javax.swing.JPanel();
        lblColFila = new javax.swing.JLabel();
        scrollFila = new javax.swing.JScrollPane();
        listaFila = new javax.swing.JPanel();
        colPreparo = new javax.swing.JPanel();
        lblColPreparo = new javax.swing.JLabel();
        scrollPreparo = new javax.swing.JScrollPane();
        listaPreparo = new javax.swing.JPanel();
        colPronto = new javax.swing.JPanel();
        lblColPronto = new javax.swing.JLabel();
        scrollPronto = new javax.swing.JScrollPane();
        listaPronto = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("BLK Burguer - Cozinha");
        setMinimumSize(new java.awt.Dimension(1100, 650));

        painelTopo.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 24, 14, 24));
        painelTopo.setLayout(new java.awt.BorderLayout(24, 0));
        painelMarca.setLayout(new java.awt.GridLayout(2, 1, 0, 0));
        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 30)); // NOI18N
        lblTitulo.setText("COZINHA");
        painelMarca.add(lblTitulo);

        lblSubtitulo.setText("BLK Burguer | painel de produção");
        painelMarca.add(lblSubtitulo);

        painelTopo.add(painelMarca, java.awt.BorderLayout.LINE_START);

        painelContadores.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 28, 6));
        lblContFila.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblContFila.setText("0 na fila");
        painelContadores.add(lblContFila);

        lblContPreparo.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblContPreparo.setText("0 em preparo");
        painelContadores.add(lblContPreparo);

        lblContPronto.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblContPronto.setText("0 prontos");
        painelContadores.add(lblContPronto);

        painelTopo.add(painelContadores, java.awt.BorderLayout.CENTER);

        painelAcoes.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 6));
        lblRelogio.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        lblRelogio.setText("00:00");
        painelAcoes.add(lblRelogio);

        btnTelaCheia.setText("Tela cheia (F11)");
        btnTelaCheia.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTelaCheiaActionPerformed(evt);
            }
        });
        painelAcoes.add(btnTelaCheia);

        btnSair.setText("Sair");
        btnSair.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSairActionPerformed(evt);
            }
        });
        painelAcoes.add(btnSair);

        painelTopo.add(painelAcoes, java.awt.BorderLayout.LINE_END);

        getContentPane().add(painelTopo, java.awt.BorderLayout.PAGE_START);

        painelColunas.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 24, 24, 24));
        painelColunas.setLayout(new java.awt.GridLayout(1, 3, 18, 0));
        colFila.setLayout(new java.awt.BorderLayout(0, 10));
        lblColFila.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblColFila.setText("NA FILA");
        colFila.add(lblColFila, java.awt.BorderLayout.PAGE_START);

        scrollFila.setViewportView(listaFila);
        colFila.add(scrollFila, java.awt.BorderLayout.CENTER);

        painelColunas.add(colFila);

        colPreparo.setLayout(new java.awt.BorderLayout(0, 10));
        lblColPreparo.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblColPreparo.setText("EM PREPARO");
        colPreparo.add(lblColPreparo, java.awt.BorderLayout.PAGE_START);

        scrollPreparo.setViewportView(listaPreparo);
        colPreparo.add(scrollPreparo, java.awt.BorderLayout.CENTER);

        painelColunas.add(colPreparo);

        colPronto.setLayout(new java.awt.BorderLayout(0, 10));
        lblColPronto.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblColPronto.setText("PRONTO PARA ENTREGAR");
        colPronto.add(lblColPronto, java.awt.BorderLayout.PAGE_START);

        scrollPronto.setViewportView(listaPronto);
        colPronto.add(scrollPronto, java.awt.BorderLayout.CENTER);

        painelColunas.add(colPronto);

        getContentPane().add(painelColunas, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnTelaCheiaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTelaCheiaActionPerformed
        alternarTelaCheia();
    }//GEN-LAST:event_btnTelaCheiaActionPerformed

    private void btnSairActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSairActionPerformed
        encerrar();
        Main.sair(this);
    }//GEN-LAST:event_btnSairActionPerformed

    public static void main(String args[]) {
        Tema.aplicar();
        java.awt.EventQueue.invokeLater(() -> new TelaCozinha().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnSair;
    private javax.swing.JButton btnTelaCheia;
    private javax.swing.JPanel colFila;
    private javax.swing.JPanel colPreparo;
    private javax.swing.JPanel colPronto;
    private javax.swing.JLabel lblColFila;
    private javax.swing.JLabel lblColPreparo;
    private javax.swing.JLabel lblColPronto;
    private javax.swing.JLabel lblContFila;
    private javax.swing.JLabel lblContPreparo;
    private javax.swing.JLabel lblContPronto;
    private javax.swing.JLabel lblRelogio;
    private javax.swing.JLabel lblSubtitulo;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel listaFila;
    private javax.swing.JPanel listaPreparo;
    private javax.swing.JPanel listaPronto;
    private javax.swing.JPanel painelAcoes;
    private javax.swing.JPanel painelColunas;
    private javax.swing.JPanel painelContadores;
    private javax.swing.JPanel painelMarca;
    private javax.swing.JPanel painelTopo;
    private javax.swing.JScrollPane scrollFila;
    private javax.swing.JScrollPane scrollPreparo;
    private javax.swing.JScrollPane scrollPronto;
    // End of variables declaration//GEN-END:variables
}
