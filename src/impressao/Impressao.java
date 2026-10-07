package impressao;

import impressao.Documento.Estilo;
import impressao.Documento.Linha;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import ui.Icones;
import ui.Tema;

/**
 * Visualização antes de imprimir e impressão (#38). O mesmo desenho serve
 * para a tela (papel de bobina) e para a impressora: em bobina térmica a
 * fonte é ajustada para caber nas colunas; em folha A4 o cupom sai no topo
 * da página, com quebra de página quando necessário.
 */
public final class Impressao {

    private Impressao() {
    }

    /** Fonte base (monoespaçada) do documento. */
    static Font fonte(Estilo estilo, float tamanho) {
        Font base = new Font(Font.MONOSPACED, Font.PLAIN, 1).deriveFont(tamanho);
        switch (estilo) {
            case NEGRITO:
                return base.deriveFont(Font.BOLD);
            case GRANDE:
                return base.deriveFont(Font.BOLD, tamanho * Documento.FATOR_GRANDE);
            default:
                return base;
        }
    }

    /** Altura de cada linha no tamanho de fonte dado. */
    static float altura(Linha l, float tamanho) {
        return (l.estilo == Estilo.GRANDE ? Documento.FATOR_GRANDE : 1f) * tamanho * 1.32f;
    }

    /** Tamanho de fonte para que as colunas do documento caibam na largura (em pontos/pixels). */
    static float tamanhoPara(Graphics2D g, Documento d, double largura) {
        FontMetrics fm = g.getFontMetrics(fonte(Estilo.NORMAL, 100f));
        double larguraCaractere100 = fm.charWidth('M');
        return (float) (largura / (d.getColunas() * larguraCaractere100 / 100.0));
    }

    /** Desenha as linhas de [de, ate) a partir de y; retorna o y final. */
    static float desenhar(Graphics2D g, Documento d, int de, int ate, float x, float y, float tamanho, boolean tela) {
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        List<Linha> linhas = d.getLinhas();
        for (int i = de; i < ate; i++) {
            Linha l = linhas.get(i);
            float h = altura(l, tamanho);
            Font f = fonte(l.estilo, tamanho);
            g.setFont(f);
            g.setColor(l.estilo == Estilo.FRACO && tela ? new Color(0x6B6B6B) : Color.BLACK);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(l.texto, x, y + (h + fm.getAscent() - fm.getDescent()) / 2f);
            y += h;
        }
        return y;
    }

    // ------------------------------------------------------------ impressora

    private static final class Paginas implements Printable {
        private final Documento doc;
        private List<int[]> paginas;
        private float tamanho;

        Paginas(Documento doc) {
            this.doc = doc;
        }

        @Override
        public int print(Graphics g0, PageFormat pf, int indice) throws PrinterException {
            Graphics2D g = (Graphics2D) g0;
            if (paginas == null) {
                // Bobina: fonte ajustada à largura. Folha: no máximo 10 pt.
                tamanho = Math.min(10f, tamanhoPara(g, doc, pf.getImageableWidth()));
                paginas = new ArrayList<>();
                int inicio = 0;
                float y = 0;
                List<Linha> linhas = doc.getLinhas();
                for (int i = 0; i < linhas.size(); i++) {
                    float h = altura(linhas.get(i), tamanho);
                    if (y + h > pf.getImageableHeight() && i > inicio) {
                        paginas.add(new int[]{inicio, i});
                        inicio = i;
                        y = 0;
                    }
                    y += h;
                }
                paginas.add(new int[]{inicio, linhas.size()});
            }
            if (indice >= paginas.size()) {
                return NO_SUCH_PAGE;
            }
            int[] p = paginas.get(indice);
            desenhar(g, doc, p[0], p[1], (float) pf.getImageableX(), (float) pf.getImageableY(), tamanho, false);
            return PAGE_EXISTS;
        }
    }

    /** Abre o diálogo da impressora do sistema e imprime. Retorna true se imprimiu. */
    public static boolean imprimir(Component pai, Documento doc) {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("BLK Burguer - " + doc.getTitulo());
        job.setPrintable(new Paginas(doc));
        if (!job.printDialog()) {
            return false;
        }
        try {
            job.print();
            return true;
        } catch (PrinterException e) {
            JOptionPane.showMessageDialog(pai, "Não foi possível imprimir: " + e.getMessage(),
                    "Impressão", JOptionPane.WARNING_MESSAGE);
            return false;
        }
    }

    // ------------------------------------------------------------ atalhos das telas

    /** Conta/comanda do pedido salvo. */
    public static void comanda(Component pai, int pedidoId) {
        pedido(pai, pedidoId, "Conta", (p, pg, colunas) -> Documentos.comanda(p, colunas));
    }

    /** Pedido para a cozinha (só itens de preparo, sem preços). */
    public static void cozinha(Component pai, int pedidoId) {
        pedido(pai, pedidoId, "Pedido para a cozinha", (p, pg, colunas) -> Documentos.cozinha(p, colunas));
    }

    /** Recibo com itens, totais e pagamentos. */
    public static void recibo(Component pai, int pedidoId) {
        pedido(pai, pedidoId, "Recibo", Documentos::recibo);
    }

    /** Menu "Imprimir" abaixo do botão: conta, pedido da cozinha ou recibo. */
    public static void menu(JComponent botao, int pedidoId) {
        javax.swing.JPopupMenu menu = new javax.swing.JPopupMenu();
        javax.swing.JMenuItem conta = new javax.swing.JMenuItem("Conta / comanda", Icones.get("receipt", 16));
        conta.addActionListener(e -> comanda(botao, pedidoId));
        javax.swing.JMenuItem coz = new javax.swing.JMenuItem("Pedido para a cozinha", Icones.get("chef-hat", 16));
        coz.addActionListener(e -> cozinha(botao, pedidoId));
        javax.swing.JMenuItem rec = new javax.swing.JMenuItem("Recibo", Icones.get("file-text", 16));
        rec.addActionListener(e -> recibo(botao, pedidoId));
        menu.add(conta);
        menu.add(coz);
        menu.add(rec);
        menu.show(botao, 0, botao.getHeight());
    }

    private interface MontaPedido {
        Documento montar(entidades.Pedido p, List<Object[]> pagamentos, int colunas);
    }

    private static void pedido(Component pai, int pedidoId, String nome, MontaPedido montar) {
        try {
            service.PedidoService ps = new service.PedidoService();
            entidades.Pedido p = ps.buscar(pedidoId);
            if (p == null) {
                JOptionPane.showMessageDialog(pai, "Pedido não encontrado.");
                return;
            }
            List<Object[]> pagamentos = ps.pagamentos(pedidoId);
            visualizar(pai, nome + " - pedido " + pedidoId, colunas -> montar.montar(p, pagamentos, colunas));
        } catch (java.sql.SQLException e) {
            JOptionPane.showMessageDialog(pai, "Erro ao carregar o pedido: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------ visualização

    /** O papel da bobina desenhado na tela, com a borda serrilhada embaixo. */
    static final class Papel extends JComponent {
        private static final float TAMANHO = 13f;
        private static final int MARGEM = 22;
        private Documento doc;

        Papel(Documento doc) {
            this.doc = doc;
        }

        void setDocumento(Documento doc) {
            this.doc = doc;
            revalidate();
            repaint();
        }

        private int larguraTexto() {
            FontMetrics fm = getFontMetrics(fonte(Estilo.NORMAL, TAMANHO));
            return fm.charWidth('M') * doc.getColunas();
        }

        @Override
        public Dimension getPreferredSize() {
            float h = 0;
            for (Linha l : doc.getLinhas()) {
                h += altura(l, TAMANHO);
            }
            return new Dimension(larguraTexto() + MARGEM * 2 + 24, (int) h + MARGEM * 2 + 40);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int larguraPapel = larguraTexto() + MARGEM * 2;
            int x0 = (getWidth() - larguraPapel) / 2;
            int alturaPapel = getPreferredSize().height - 30;
            // Sombra e papel com serrilhado no fim (corte da bobina).
            Polygon papel = new Polygon();
            papel.addPoint(x0, 10);
            papel.addPoint(x0 + larguraPapel, 10);
            int dente = 8;
            int yFim = 10 + alturaPapel;
            papel.addPoint(x0 + larguraPapel, yFim);
            for (int x = x0 + larguraPapel; x > x0; x -= dente) {
                papel.addPoint(x - dente / 2, yFim + 5);
                papel.addPoint(Math.max(x0, x - dente), yFim);
            }
            g.translate(3, 4);
            g.setColor(new Color(0, 0, 0, 60));
            g.fill(papel);
            g.translate(-3, -4);
            g.setColor(new Color(0xFFFEF9));
            g.fill(papel);
            g.setColor(new Color(0xD9D4C7));
            g.setStroke(new BasicStroke(1f));
            g.draw(papel);
            desenhar(g, doc, 0, doc.getLinhas().size(), x0 + MARGEM, 10 + MARGEM, TAMANHO, true);
            g.dispose();
        }
    }

    /**
     * Mostra a visualização com escolha da largura da bobina e o botão
     * Imprimir. O documento é refeito ao trocar a largura.
     *
     * @param montar cria o documento para um número de colunas
     */
    public static void visualizar(Component pai, String titulo, IntFunction<Documento> montar) {
        JDialog dialogo = new JDialog(SwingUtilities.getWindowAncestor(pai), titulo,
                java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialogo.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        Documento[] atual = {montar.apply(Documento.COLUNAS_80MM)};
        Papel papel = new Papel(atual[0]);

        JPanel fundo = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 16));
        fundo.setBackground(Tema.isEscuro() ? new Color(0x2A2825) : new Color(0xE4E0D8));
        fundo.add(papel);
        JScrollPane scroll = new JScrollPane(fundo);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(24);

        JComboBox<String> cmbLargura = new JComboBox<>(new String[]{"Bobina 80 mm", "Bobina 58 mm"});
        cmbLargura.addActionListener(e -> {
            atual[0] = montar.apply(cmbLargura.getSelectedIndex() == 0 ? Documento.COLUNAS_80MM : Documento.COLUNAS_58MM);
            papel.setDocumento(atual[0]);
            fundo.revalidate();
        });
        JButton btnImprimir = new JButton("Imprimir");
        btnImprimir.setIcon(Icones.get("printer", 16, Tema.DESTAQUE_TEXTO));
        Tema.primario(btnImprimir);
        btnImprimir.addActionListener(e -> {
            if (imprimir(dialogo, atual[0])) {
                dialogo.dispose();
            }
        });
        JButton btnFechar = new JButton("Fechar");
        btnFechar.setIcon(Icones.get("x", 16));
        btnFechar.addActionListener(e -> dialogo.dispose());

        JPanel barra = new JPanel(new BorderLayout());
        barra.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        JPanel esquerda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JLabel lbl = new JLabel("Papel:");
        esquerda.add(lbl);
        esquerda.add(cmbLargura);
        JPanel direita = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        direita.add(btnFechar);
        direita.add(btnImprimir);
        barra.add(esquerda, BorderLayout.WEST);
        barra.add(direita, BorderLayout.EAST);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.add(scroll, BorderLayout.CENTER);
        raiz.add(barra, BorderLayout.SOUTH);
        dialogo.setContentPane(raiz);
        dialogo.getRootPane().setDefaultButton(btnImprimir);
        dialogo.setSize(560, 720);
        dialogo.setLocationRelativeTo(pai);
        dialogo.setVisible(true);
    }
}
