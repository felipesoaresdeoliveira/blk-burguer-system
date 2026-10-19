package ui;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.awt.Color;
import java.util.function.Supplier;
import javax.swing.Icon;
import javax.swing.UIManager;

/**
 * Ícones SVG (Lucide, licença ISC, em ui/icones). Os arquivos usam traço
 * preto; o filtro troca pela cor pedida no momento de desenhar, então o
 * ícone acompanha o tema.
 */
public final class Icones {

    private Icones() {
    }

    /** Ícone na cor do texto do tema. */
    public static Icon get(String nome, int tamanho) {
        return comCor(nome, tamanho, () -> {
            Color c = UIManager.getColor("Label.foreground");
            return c != null ? c : Tema.TEXTO;
        });
    }

    /** Ícone numa cor fixa (ex.: escuro sobre botão âmbar). */
    public static Icon get(String nome, int tamanho, Color cor) {
        return comCor(nome, tamanho, () -> cor);
    }

    /** Ícone numa cor que pode mudar (lida a cada desenho). */
    public static Icon comCor(String nome, int tamanho, Supplier<Color> cor) {
        FlatSVGIcon icone = new FlatSVGIcon("ui/icones/" + nome + ".svg", tamanho, tamanho);
        icone.setColorFilter(new FlatSVGIcon.ColorFilter(original -> {
            Color c = cor.get();
            return new Color(c.getRed(), c.getGreen(), c.getBlue(), original.getAlpha());
        }));
        return icone;
    }
}
