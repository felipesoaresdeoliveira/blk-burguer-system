package ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Path2D;

/**
 * Colunas verticais de uma única série (ex.: faturamento por dia).
 * Colunas finas com topo arredondado, grade discreta, rótulo de valor apenas
 * no maior valor e no último, e tooltip em cada coluna.
 */
public class GraficoColunas extends PainelCartao {

    private String[] rotulos = {"Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom"};
    private double[] valores = {120, 340, 280, 510, 460, 720, 390};
    private String[] dicas;
    private Rectangle[] colunas = new Rectangle[0];

    public GraficoColunas() {
        setTitulo("Faturamento");
    }

    /**
     * @param dicas texto do tooltip de cada coluna (pode ser null)
     */
    public void setDados(String[] rotulos, double[] valores, String[] dicas) {
        this.rotulos = rotulos;
        this.valores = valores;
        this.dicas = dicas;
        repaint();
    }

    /** Arredonda o topo do eixo para um valor "limpo" (1, 2, 2,5 ou 5 × 10^n). */
    static double topoLimpo(double max) {
        if (max <= 0) {
            return 1;
        }
        double exp = Math.pow(10, Math.floor(Math.log10(max)));
        for (double m : new double[]{1, 2, 2.5, 5, 10}) {
            if (m * exp >= max) {
                return m * exp;
            }
        }
        return 10 * exp;
    }

    @Override
    protected void desenharConteudo(Graphics2D g, Rectangle area) {
        double max = 0;
        for (double v : valores) {
            max = Math.max(max, v);
        }
        if (valores.length == 0 || max <= 0) {
            colunas = new Rectangle[0];
            desenharVazio(g, area, "Sem vendas no período");
            return;
        }
        double topo = topoLimpo(max);
        int linhas = 4;

        g.setFont(fonte(Font.PLAIN, 11f));
        FontMetrics fm = g.getFontMetrics();
        int larguraEixo = 0;
        for (int i = 0; i <= linhas; i++) {
            larguraEixo = Math.max(larguraEixo, fm.stringWidth(Tema.reaisCompacto(topo * i / linhas)));
        }
        int x0 = area.x + larguraEixo + 8;
        int y0 = area.y + fm.getHeight() + 4;               // espaço para rótulo de valor
        int yBase = area.y + area.height - fm.getHeight() - 6; // espaço para rótulos do eixo x
        int alturaUtil = yBase - y0;
        int largura = area.x + area.width - x0;

        // Grade horizontal (linha fina, sólida) e valores do eixo
        for (int i = 0; i <= linhas; i++) {
            int y = yBase - (int) Math.round(alturaUtil * i / (double) linhas);
            g.setColor(i == 0 ? Tema.EIXO : Tema.BORDA);
            g.drawLine(x0, y, x0 + largura, y);
            g.setColor(Tema.TEXTO_FRACO);
            String t = Tema.reaisCompacto(topo * i / linhas);
            g.drawString(t, x0 - 8 - fm.stringWidth(t), y + fm.getAscent() / 2 - 1);
        }

        int n = valores.length;
        double banda = largura / (double) n;
        int espessura = (int) Math.min(24, Math.max(6, banda * 0.55));
        int iMax = 0;
        for (int i = 1; i < n; i++) {
            if (valores[i] > valores[iMax]) {
                iMax = i;
            }
        }
        colunas = new Rectangle[n];
        for (int i = 0; i < n; i++) {
            int cx = x0 + (int) Math.round(banda * i + banda / 2);
            int alt = (int) Math.round(alturaUtil * valores[i] / topo);
            int x = cx - espessura / 2;
            colunas[i] = new Rectangle((int) Math.round(x0 + banda * i), y0, (int) Math.ceil(banda), yBase - y0);

            if (alt > 0) {
                Color cor = Tema.SERIE_UNICA;
                if (destacado == i) {
                    cor = cor.brighter();
                }
                g.setColor(cor);
                g.fill(colunaArredondada(x, yBase - alt, espessura, alt, Math.min(4, alt)));
            }

            // Rótulos do eixo x
            g.setColor(destacado == i ? Tema.TEXTO : Tema.TEXTO_FRACO);
            String r = rotulos[i];
            g.drawString(r, cx - fm.stringWidth(r) / 2, yBase + fm.getAscent() + 5);

            // Rótulo de valor só no maior e no último (seletivo)
            if (i == iMax || i == n - 1) {
                g.setColor(Tema.TEXTO_SECUNDARIO);
                String v = Tema.reaisCompacto(valores[i]);
                g.drawString(v, cx - fm.stringWidth(v) / 2, yBase - alt - 5);
            }
        }

        // Linha-guia na coluna sob o mouse
        if (destacado >= 0 && destacado < n) {
            int cx = x0 + (int) Math.round(banda * destacado + banda / 2);
            g.setColor(new Color(255, 255, 255, 30));
            g.setStroke(new BasicStroke(1f));
            g.drawLine(cx, y0, cx, yBase);
        }
    }

    /** Retângulo com os dois cantos de cima arredondados e base reta. */
    static Path2D colunaArredondada(double x, double y, double w, double h, double r) {
        Path2D p = new Path2D.Double();
        p.moveTo(x, y + h);
        p.lineTo(x, y + r);
        p.quadTo(x, y, x + r, y);
        p.lineTo(x + w - r, y);
        p.quadTo(x + w, y, x + w, y + r);
        p.lineTo(x + w, y + h);
        p.closePath();
        return p;
    }

    @Override
    protected int marcaEm(int x, int y) {
        for (int i = 0; i < colunas.length; i++) {
            if (colunas[i] != null && colunas[i].contains(x, y)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected String dicaEm(int i) {
        if (dicas != null && i < dicas.length) {
            return dicas[i];
        }
        return rotulos[i] + ": " + Tema.reais(valores[i]);
    }
}
