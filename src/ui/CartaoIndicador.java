package ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import javax.swing.JComponent;

/**
 * Indicador numérico (stat tile): rótulo, valor e uma linha de detalhe
 * opcional com variação (seta + texto) ou status.
 */
public class CartaoIndicador extends JComponent {

    /** Como colorir a linha de detalhe. */
    public enum Tendencia { NEUTRA, ALTA, BAIXA, AVISO }

    private String titulo = "Indicador";
    private String valor = "-";
    private String detalhe = "";
    private Tendencia tendencia = Tendencia.NEUTRA;
    private boolean principal;

    public CartaoIndicador() {
        setOpaque(false);
        setPreferredSize(new Dimension(200, 104));
    }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; repaint(); }

    public String getValor() { return valor; }
    public void setValor(String valor) { this.valor = valor; repaint(); }

    public String getDetalhe() { return detalhe; }
    public void setDetalhe(String detalhe) { this.detalhe = detalhe; repaint(); }

    public Tendencia getTendencia() { return tendencia; }
    public void setTendencia(Tendencia tendencia) { this.tendencia = tendencia; repaint(); }

    /** O indicador principal da tela ganha a barra de destaque âmbar. */
    public boolean isPrincipal() { return principal; }
    public void setPrincipal(boolean principal) { this.principal = principal; repaint(); }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        g.setColor(Tema.CARTAO);
        g.fillRoundRect(0, 0, w - 1, h - 1, 14, 14);
        g.setColor(Tema.BORDA);
        g.drawRoundRect(0, 0, w - 1, h - 1, 14, 14);
        if (principal) {
            g.setColor(Tema.DESTAQUE);
            g.fillRoundRect(0, 12, 4, h - 24, 4, 4);
        }

        int x = 18;
        g.setFont(PainelCartao.fonte(Font.PLAIN, 12.5f));
        g.setColor(Tema.TEXTO_SECUNDARIO);
        FontMetrics fm = g.getFontMetrics();
        int y = 16 + fm.getAscent();
        g.drawString(titulo, x, y);

        g.setFont(PainelCartao.fonte(Font.BOLD, principal ? 30f : 26f));
        g.setColor(Tema.TEXTO);
        fm = g.getFontMetrics();
        y += 8 + fm.getAscent();
        g.drawString(ajustar(valor, fm, w - x - 14), x, y);

        if (detalhe != null && !detalhe.isEmpty()) {
            g.setFont(PainelCartao.fonte(Font.PLAIN, 12f));
            fm = g.getFontMetrics();
            y = h - 14;
            int tx = x;
            Color cor = corDetalhe();
            if (tendencia != Tendencia.NEUTRA) {
                desenharIcone(g, tx, y - fm.getAscent() / 2 - 1, cor);
                tx += 14;
            }
            g.setColor(tendencia == Tendencia.NEUTRA ? Tema.TEXTO_FRACO : Tema.TEXTO_SECUNDARIO);
            g.drawString(ajustar(detalhe, fm, w - tx - 12), tx, y);
        }
        g.dispose();
    }

    private Color corDetalhe() {
        switch (tendencia) {
            case ALTA: return Tema.SUCESSO;
            case BAIXA: return Tema.PERIGO;
            case AVISO: return Tema.AVISO;
            default: return Tema.TEXTO_FRACO;
        }
    }

    /** Seta para cima/baixo ou triângulo de aviso: o status nunca depende só da cor. */
    private void desenharIcone(Graphics2D g, int x, int cy, Color cor) {
        g.setColor(cor);
        Path2D p = new Path2D.Double();
        if (tendencia == Tendencia.BAIXA) {
            p.moveTo(x, cy - 4); p.lineTo(x + 10, cy - 4); p.lineTo(x + 5, cy + 4);
        } else {
            p.moveTo(x, cy + 4); p.lineTo(x + 10, cy + 4); p.lineTo(x + 5, cy - 4);
        }
        p.closePath();
        g.fill(p);
        if (tendencia == Tendencia.AVISO) {
            g.setColor(Tema.CARTAO);
            g.fillRect(x + 4, cy - 1, 2, 3);
        }
    }

    /** Corta com reticências para não vazar do cartão. */
    static String ajustar(String texto, FontMetrics fm, int largura) {
        if (fm.stringWidth(texto) <= largura) {
            return texto;
        }
        String s = texto;
        while (s.length() > 1 && fm.stringWidth(s + "…") > largura) {
            s = s.substring(0, s.length() - 1);
        }
        return s + "…";
    }
}
