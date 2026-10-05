package ui;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Path2D;

/**
 * Barras horizontais de uma única série, ordenadas (ex.: produtos mais
 * vendidos). Nome à esquerda, barra fina e valor na ponta da barra.
 */
public class GraficoBarras extends PainelCartao {

    private String[] rotulos = {"BLK Clássico", "BLK Bacon", "Coca-Cola", "Batata frita", "BLK Duplo"};
    private double[] valores = {18, 14, 12, 9, 6};
    private String[] textosValor;
    private String[] dicas;
    private Rectangle[] linhas = new Rectangle[0];

    public GraficoBarras() {
        setTitulo("Ranking");
    }

    /**
     * @param textosValor texto na ponta de cada barra (pode ser null)
     * @param dicas tooltip de cada barra (pode ser null)
     */
    public void setDados(String[] rotulos, double[] valores, String[] textosValor, String[] dicas) {
        this.rotulos = rotulos;
        this.valores = valores;
        this.textosValor = textosValor;
        this.dicas = dicas;
        repaint();
    }

    @Override
    protected void desenharConteudo(Graphics2D g, Rectangle area) {
        double max = 0;
        for (double v : valores) {
            max = Math.max(max, v);
        }
        if (valores.length == 0 || max <= 0) {
            linhas = new Rectangle[0];
            desenharVazio(g, area, "Sem vendas no período");
            return;
        }
        g.setFont(fonte(Font.PLAIN, 12.5f));
        FontMetrics fm = g.getFontMetrics();
        int larguraRotulo = 0;
        for (String r : rotulos) {
            larguraRotulo = Math.max(larguraRotulo, fm.stringWidth(r));
        }
        larguraRotulo = Math.min(larguraRotulo, area.width / 3);
        int larguraValor = 0;
        for (int i = 0; i < valores.length; i++) {
            larguraValor = Math.max(larguraValor, fm.stringWidth(textoValor(i)));
        }
        int x0 = area.x + larguraRotulo + 12;
        int larguraUtil = area.x + area.width - x0 - larguraValor - 10;
        int n = valores.length;
        double banda = Math.min(40, area.height / (double) n);
        int espessura = (int) Math.min(18, banda * 0.55);

        g.setColor(Tema.EIXO);
        g.drawLine(x0, area.y, x0, area.y + (int) (banda * n));

        linhas = new Rectangle[n];
        for (int i = 0; i < n; i++) {
            int cy = area.y + (int) Math.round(banda * i + banda / 2);
            linhas[i] = new Rectangle(area.x, (int) (area.y + banda * i), area.width, (int) Math.ceil(banda));
            if (destacado == i) {
                g.setColor(new java.awt.Color(255, 255, 255, 12));
                g.fillRoundRect(area.x - 6, linhas[i].y, area.width + 12, linhas[i].height, 8, 8);
            }
            g.setColor(destacado == i ? Tema.TEXTO : Tema.TEXTO_SECUNDARIO);
            String r = CartaoIndicador.ajustar(rotulos[i], fm, larguraRotulo);
            g.drawString(r, x0 - 12 - fm.stringWidth(r), cy + fm.getAscent() / 2 - 1);

            int comp = (int) Math.round(larguraUtil * valores[i] / max);
            if (comp > 0) {
                g.setColor(destacado == i ? Tema.SERIE_UNICA.brighter() : Tema.SERIE_UNICA);
                g.fill(barraArredondada(x0 + 1, cy - espessura / 2.0, comp, espessura, Math.min(4, comp)));
            }
            g.setColor(Tema.TEXTO);
            g.drawString(textoValor(i), x0 + comp + 8, cy + fm.getAscent() / 2 - 1);
        }
    }

    private String textoValor(int i) {
        return textosValor != null ? textosValor[i] : String.valueOf((long) valores[i]);
    }

    /** Barra com a ponta direita arredondada e a base (esquerda) reta. */
    private static Path2D barraArredondada(double x, double y, double w, double h, double r) {
        Path2D p = new Path2D.Double();
        p.moveTo(x, y);
        p.lineTo(x + w - r, y);
        p.quadTo(x + w, y, x + w, y + r);
        p.lineTo(x + w, y + h - r);
        p.quadTo(x + w, y + h, x + w - r, y + h);
        p.lineTo(x, y + h);
        p.closePath();
        return p;
    }

    @Override
    protected int marcaEm(int x, int y) {
        for (int i = 0; i < linhas.length; i++) {
            if (linhas[i] != null && linhas[i].contains(x, y)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected String dicaEm(int i) {
        return dicas != null ? dicas[i] : rotulos[i] + ": " + textoValor(i);
    }
}
