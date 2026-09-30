package ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Path2D;

/**
 * Lista de itens de estoque abaixo do mínimo. Cada linha tem ícone + rótulo
 * de status (o status nunca depende só da cor) e um medidor do saldo em
 * relação ao mínimo.
 */
public class PainelAlertas extends PainelCartao {

    private String[] itens = {"Onion rings (porção)", "Suco natural 300ml"};
    private int[] quantidades = {6, 0};
    private int[] minimos = {10, 10};
    private Rectangle[] linhas = new Rectangle[0];

    public PainelAlertas() {
        setTitulo("Estoque baixo");
    }

    public void setDados(String[] itens, int[] quantidades, int[] minimos) {
        this.itens = itens;
        this.quantidades = quantidades;
        this.minimos = minimos;
        repaint();
    }

    @Override
    protected void desenharConteudo(Graphics2D g, Rectangle area) {
        if (itens.length == 0) {
            linhas = new Rectangle[0];
            g.setFont(fonte(Font.BOLD, 13f));
            FontMetrics fm = g.getFontMetrics();
            String msg = "Tudo certo com o estoque";
            int tw = fm.stringWidth(msg) + 22;
            int x = area.x + (area.width - tw) / 2, y = area.y + area.height / 2;
            g.setColor(Tema.SUCESSO);
            g.fillOval(x, y - 13, 16, 16);
            g.setColor(Tema.CARTAO);
            g.setStroke(new java.awt.BasicStroke(2f));
            g.drawPolyline(new int[]{x + 4, x + 7, x + 12}, new int[]{y - 5, y - 2, y - 9}, 3);
            g.setColor(Tema.TEXTO_SECUNDARIO);
            g.drawString(msg, x + 22, y);
            return;
        }
        g.setFont(fonte(Font.PLAIN, 12.5f));
        FontMetrics fm = g.getFontMetrics();
        int alturaLinha = 44;
        int visiveis = Math.max(1, Math.min(itens.length, area.height / alturaLinha));
        linhas = new Rectangle[itens.length];
        for (int i = 0; i < visiveis; i++) {
            int y = area.y + i * alturaLinha;
            linhas[i] = new Rectangle(area.x, y, area.width, alturaLinha);
            boolean zerado = quantidades[i] <= 0;
            Color cor = zerado ? Tema.PERIGO : Tema.AVISO;

            desenharTriangulo(g, area.x, y + 4, cor);
            g.setFont(fonte(Font.BOLD, 12.5f));
            g.setColor(Tema.TEXTO);
            String status = zerado ? "Sem estoque" : "Baixo";
            int larguraStatus = g.getFontMetrics().stringWidth(status);
            g.drawString(CartaoIndicador.ajustar(itens[i], g.getFontMetrics(), area.width - 40 - larguraStatus),
                    area.x + 22, y + fm.getAscent() + 2);
            g.setColor(cor);
            g.drawString(status, area.x + area.width - larguraStatus, y + fm.getAscent() + 2);

            // Medidor: trilho + preenchimento proporcional ao mínimo
            int my = y + fm.getHeight() + 10, mw = area.width - 22 - 90;
            g.setColor(Tema.BORDA);
            g.fillRoundRect(area.x + 22, my, mw, 6, 6, 6);
            double frac = minimos[i] <= 0 ? 0 : Math.min(1, quantidades[i] / (double) minimos[i]);
            g.setColor(cor);
            g.fillRoundRect(area.x + 22, my, Math.max(zerado ? 0 : 6, (int) (mw * frac)), 6, 6, 6);
            g.setFont(fonte(Font.PLAIN, 11.5f));
            g.setColor(Tema.TEXTO_FRACO);
            String q = quantidades[i] + " de mín. " + minimos[i];
            g.drawString(q, area.x + area.width - g.getFontMetrics().stringWidth(q), my + 7);
        }
        if (visiveis < itens.length) {
            g.setFont(fonte(Font.PLAIN, 11.5f));
            g.setColor(Tema.TEXTO_FRACO);
            g.drawString("+ " + (itens.length - visiveis) + " item(ns) - veja em Estoque",
                    area.x + 22, area.y + visiveis * alturaLinha + 10);
        }
    }

    private static void desenharTriangulo(Graphics2D g, int x, int y, Color cor) {
        Path2D p = new Path2D.Double();
        p.moveTo(x + 7, y);
        p.lineTo(x + 14, y + 13);
        p.lineTo(x, y + 13);
        p.closePath();
        g.setColor(cor);
        g.fill(p);
        g.setColor(Tema.CARTAO);
        g.fillRect(x + 6, y + 4, 2, 5);
        g.fillRect(x + 6, y + 10, 2, 2);
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
        return itens[i] + ": " + quantidades[i] + " em estoque (mínimo " + minimos[i] + ")";
    }
}
