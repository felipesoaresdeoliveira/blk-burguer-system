package ui;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

/**
 * Rosca de composição (parte do todo) com legenda ao lado. A cor de cada
 * categoria vem de um "slot" fixo da paleta, para que a mesma categoria tenha
 * sempre a mesma cor, independentemente da posição no ranking.
 */
public class GraficoRosca extends PainelCartao {

    private String[] rotulos = {"Dinheiro", "Pix", "Débito", "Crédito"};
    private double[] valores = {120, 380, 150, 260};
    private int[] slots = {0, 1, 2, 3};
    private String[] detalhes;
    private String centroValor = "";
    private String centroRotulo = "";
    private Shape[] fatias = new Shape[0];
    private Rectangle[] linhasLegenda = new Rectangle[0];

    public GraficoRosca() {
        setTitulo("Composição");
    }

    /**
     * @param slots índice da cor de cada categoria em {@link Tema#SERIES}
     * @param detalhes texto extra da legenda/tooltip (pode ser null)
     */
    public void setDados(String[] rotulos, double[] valores, int[] slots, String[] detalhes) {
        this.rotulos = rotulos;
        this.valores = valores;
        this.slots = slots;
        this.detalhes = detalhes;
        repaint();
    }

    public void setCentro(String valor, String rotulo) {
        this.centroValor = valor;
        this.centroRotulo = rotulo;
        repaint();
    }

    private double total() {
        double t = 0;
        for (double v : valores) {
            t += v;
        }
        return t;
    }

    @Override
    protected void desenharConteudo(Graphics2D g, Rectangle area) {
        double total = total();
        if (total <= 0) {
            fatias = new Shape[0];
            linhasLegenda = new Rectangle[0];
            desenharVazio(g, area, "Sem vendas no período");
            return;
        }
        int d = Math.min(area.height, (int) (area.width * 0.45));
        int cx = area.x + d / 2, cy = area.y + area.height / 2;
        int espessura = Math.max(14, d / 6);
        Ellipse2D externo = new Ellipse2D.Double(cx - d / 2.0, cy - d / 2.0, d, d);
        Ellipse2D interno = new Ellipse2D.Double(cx - d / 2.0 + espessura, cy - d / 2.0 + espessura,
                d - 2.0 * espessura, d - 2.0 * espessura);

        fatias = new Shape[valores.length];
        double inicio = 90;
        for (int i = 0; i < valores.length; i++) {
            double ext = -360 * valores[i] / total;
            if (valores[i] <= 0) {
                continue;
            }
            Area fatia = new Area(new Arc2D.Double(externo.getBounds2D(), inicio, ext, Arc2D.PIE));
            fatia.subtract(new Area(interno));
            fatias[i] = fatia;
            g.setColor(Tema.SERIES.get(slots[i]));
            g.fill(fatia);
            inicio += ext;
        }
        // Espaço de 2px na cor do fundo entre as fatias (sem contorno nas marcas)
        g.setColor(Tema.CARTAO);
        g.setStroke(new BasicStroke(2f));
        inicio = 90;
        for (int i = 0; i < valores.length; i++) {
            if (valores[i] <= 0) {
                continue;
            }
            double rad = Math.toRadians(inicio);
            g.drawLine(cx, cy, cx + (int) Math.round(Math.cos(rad) * d / 2.0), cy - (int) Math.round(Math.sin(rad) * d / 2.0));
            inicio -= 360 * valores[i] / total;
        }
        if (destacado >= 0 && fatias[destacado] != null) {
            g.setColor(new java.awt.Color(255, 255, 255, 40));
            g.fill(fatias[destacado]);
        }
        g.setColor(Tema.CARTAO);
        g.fill(interno);

        // Texto central
        g.setFont(fonte(Font.BOLD, 20f));
        FontMetrics fm = g.getFontMetrics();
        g.setColor(Tema.TEXTO);
        g.drawString(centroValor, cx - fm.stringWidth(centroValor) / 2, cy + 2);
        g.setFont(fonte(Font.PLAIN, 11f));
        FontMetrics fm2 = g.getFontMetrics();
        g.setColor(Tema.TEXTO_FRACO);
        g.drawString(centroRotulo, cx - fm2.stringWidth(centroRotulo) / 2, cy + 4 + fm2.getAscent());

        // Legenda: amostra colorida + texto em cores de texto (nunca na cor da série)
        int lx = area.x + d + 24;
        int larguraLegenda = area.x + area.width - lx;
        g.setFont(fonte(Font.PLAIN, 12.5f));
        fm = g.getFontMetrics();
        int alturaLinha = fm.getHeight() * 2 + 6;
        int ly = cy - (alturaLinha * valores.length) / 2;
        linhasLegenda = new Rectangle[valores.length];
        for (int i = 0; i < valores.length; i++) {
            int y = ly + i * alturaLinha;
            linhasLegenda[i] = new Rectangle(lx - 6, y - 2, larguraLegenda + 6, alturaLinha - 2);
            if (destacado == i) {
                g.setColor(new java.awt.Color(255, 255, 255, 12));
                g.fillRoundRect(lx - 6, y - 2, larguraLegenda + 6, alturaLinha - 4, 8, 8);
            }
            g.setColor(Tema.SERIES.get(slots[i]));
            g.fillRoundRect(lx, y + fm.getAscent() / 2 - 3, 10, 10, 3, 3);
            g.setFont(fonte(Font.PLAIN, 12.5f));
            g.setColor(Tema.TEXTO);
            g.drawString(rotulos[i], lx + 18, y + fm.getAscent());
            String pct = String.format("%.0f%%", 100 * valores[i] / total);
            g.setFont(fonte(Font.BOLD, 12.5f));
            g.drawString(pct, lx + larguraLegenda - 8 - g.getFontMetrics().stringWidth(pct), y + fm.getAscent());
            g.setFont(fonte(Font.PLAIN, 11.5f));
            g.setColor(Tema.TEXTO_FRACO);
            String det = detalhes != null ? detalhes[i] : String.valueOf(valores[i]);
            g.drawString(CartaoIndicador.ajustar(det, g.getFontMetrics(), larguraLegenda - 26), lx + 18, y + fm.getAscent() + fm.getHeight());
        }
    }

    @Override
    protected int marcaEm(int x, int y) {
        for (int i = 0; i < fatias.length; i++) {
            if (fatias[i] != null && fatias[i].contains(x, y)) {
                return i;
            }
        }
        for (int i = 0; i < linhasLegenda.length; i++) {
            if (linhasLegenda[i] != null && linhasLegenda[i].contains(x, y)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected String dicaEm(int i) {
        String pct = String.format("%.1f%%", 100 * valores[i] / total());
        return rotulos[i] + " - " + pct + (detalhes != null ? " (" + detalhes[i] + ")" : "");
    }
}
