package ui;

import entidades.Mesa;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.function.Consumer;
import javax.swing.JComponent;

/** Cartão de uma mesa no mapa: número, lugares, status (cor + texto) e, se ocupada, total e tempo. */
public class CartaoMesa extends JComponent {

    public static final Color LIVRE = new Color(0x0CA30C);
    public static final Color OCUPADA = new Color(0x3987E5);
    public static final Color AGUARDANDO = new Color(0xF5A524);
    public static final Color RESERVADA = new Color(0x9085E9);

    private final Mesa mesa;
    private boolean sobre;

    /**
     * @param aoClicar chamado no clique esquerdo
     * @param aoMenu chamado no clique direito (pode ser null)
     */
    public CartaoMesa(Mesa mesa, Consumer<Mesa> aoClicar, Consumer<MouseEvent> aoMenu) {
        this.mesa = mesa;
        setPreferredSize(new Dimension(220, 150));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setToolTipText(mesa + " - " + mesa.getStatus() + " | clique para abrir" + (aoMenu != null ? ", botão direito para opções" : ""));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                sobre = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                sobre = false;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger() || javax.swing.SwingUtilities.isRightMouseButton(e)) {
                    if (aoMenu != null) {
                        aoMenu.accept(e);
                    }
                } else if (contains(e.getPoint())) {
                    aoClicar.accept(mesa);
                }
            }
        });
    }

    public static Color cor(Mesa.Status status) {
        switch (status) {
            case OCUPADA: return OCUPADA;
            case AGUARDANDO_PAGAMENTO: return AGUARDANDO;
            case RESERVADA: return RESERVADA;
            default: return LIVRE;
        }
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        Color cor = cor(mesa.getStatus());
        g.setColor(sobre ? new Color(0x242423) : Tema.CARTAO);
        g.fillRoundRect(0, 0, w - 1, h - 1, 16, 16);
        g.setColor(cor);
        g.setStroke(new BasicStroke(mesa.getStatus() == Mesa.Status.LIVRE ? 1.5f : 2.5f));
        g.drawRoundRect(1, 1, w - 3, h - 3, 16, 16);
        g.fillRoundRect(0, 14, 5, h - 28, 4, 4);

        int x = 18;
        g.setFont(PainelCartao.fonte(Font.PLAIN, 12f));
        g.setColor(Tema.TEXTO_FRACO);
        g.drawString("MESA", x, 26);
        g.setFont(PainelCartao.fonte(Font.BOLD, 34f));
        g.setColor(Tema.TEXTO);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(String.valueOf(mesa.getNumero()), x, 26 + fm.getAscent());
        g.setFont(PainelCartao.fonte(Font.PLAIN, 12f));
        g.setColor(Tema.TEXTO_FRACO);
        String lugares = mesa.getLugares() + " lugares";
        g.drawString(lugares, w - 16 - g.getFontMetrics().stringWidth(lugares), 26);

        // Status: bolinha + texto
        int y = h - 18;
        g.setColor(cor);
        g.fillOval(x, y - 10, 10, 10);
        g.setFont(PainelCartao.fonte(Font.BOLD, 13f));
        g.drawString(mesa.getStatus().toString(), x + 16, y);

        if (mesa.getPedidoId() > 0) {
            g.setFont(PainelCartao.fonte(Font.BOLD, 15f));
            g.setColor(Tema.TEXTO);
            String total = Tema.reais(mesa.getTotalPedido());
            int tw = g.getFontMetrics().stringWidth(total);
            g.drawString(total, w - 16 - tw, 26 + fm.getAscent() - 8);
            if (mesa.getAbertaEm() != null) {
                long min = Duration.between(mesa.getAbertaEm(), LocalDateTime.now()).toMinutes();
                String tempo = min < 60 ? min + " min" : (min / 60) + "h" + String.format("%02d", min % 60);
                g.setFont(PainelCartao.fonte(Font.PLAIN, 12f));
                g.setColor(Tema.TEXTO_SECUNDARIO);
                g.drawString(tempo, w - 16 - g.getFontMetrics().stringWidth(tempo), 26 + fm.getAscent() + 10);
            }
        }
        g.dispose();
    }
}
