package ui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;

/**
 * Base dos cartões do dashboard: fundo arredondado, título, subtítulo e área
 * de conteúdo. Subclasses desenham em {@link #desenharConteudo} e respondem ao
 * mouse com {@link #dicaEm} (tooltip) e {@link #marcaEm} (destaque ao passar).
 */
public abstract class PainelCartao extends JComponent {

    protected static final int MARGEM = 16;

    private String titulo = "Título";
    private String subtitulo = "";
    /** Índice da marca sob o mouse, ou -1. */
    protected int destacado = -1;

    protected PainelCartao() {
        setOpaque(false);
        setPreferredSize(new Dimension(360, 240));
        ToolTipManager.sharedInstance().registerComponent(this);
        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int i = marcaEm(e.getX(), e.getY());
                if (i != destacado) {
                    destacado = i;
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                destacado = -1;
                repaint();
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
        repaint();
    }

    public String getSubtitulo() {
        return subtitulo;
    }

    public void setSubtitulo(String subtitulo) {
        this.subtitulo = subtitulo;
        repaint();
    }

    protected static Font fonte(int estilo, float tamanho) {
        Font base = UIManager.getFont("Label.font");
        if (base == null) {
            base = new Font("Segoe UI", Font.PLAIN, 12);
        }
        return base.deriveFont(estilo, tamanho);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        int w = getWidth(), h = getHeight();
        g.setColor(Tema.CARTAO);
        g.fillRoundRect(0, 0, w - 1, h - 1, 14, 14);
        g.setColor(Tema.BORDA);
        g.drawRoundRect(0, 0, w - 1, h - 1, 14, 14);

        g.setFont(fonte(Font.BOLD, 14f));
        g.setColor(Tema.TEXTO);
        int y = MARGEM + g.getFontMetrics().getAscent();
        g.drawString(titulo, MARGEM, y);
        int topo = y + 6;
        if (subtitulo != null && !subtitulo.isEmpty()) {
            g.setFont(fonte(Font.PLAIN, 12f));
            g.setColor(Tema.TEXTO_FRACO);
            y += g.getFontMetrics().getHeight() + 2;
            g.drawString(subtitulo, MARGEM, y);
            topo = y + 6;
        }
        Rectangle area = new Rectangle(MARGEM, topo + 8, w - 2 * MARGEM, h - topo - 8 - MARGEM);
        desenharConteudo(g, area);
        g.dispose();
    }

    /** Mensagem centralizada para quando não há dados. */
    protected void desenharVazio(Graphics2D g, Rectangle area, String mensagem) {
        g.setFont(fonte(Font.PLAIN, 13f));
        g.setColor(Tema.TEXTO_FRACO);
        int tw = g.getFontMetrics().stringWidth(mensagem);
        g.drawString(mensagem, area.x + (area.width - tw) / 2, area.y + area.height / 2);
    }

    protected abstract void desenharConteudo(Graphics2D g, Rectangle area);

    /** Índice da marca na posição (x, y), ou -1. */
    protected int marcaEm(int x, int y) {
        return -1;
    }

    /** Texto do tooltip para a marca i. */
    protected String dicaEm(int i) {
        return null;
    }

    @Override
    public String getToolTipText(MouseEvent e) {
        int i = marcaEm(e.getX(), e.getY());
        return i < 0 ? null : dicaEm(i);
    }
}
