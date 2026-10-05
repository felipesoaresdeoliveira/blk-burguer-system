package ui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;

/**
 * Identidade visual do BLK Burguer: tema escuro com destaque âmbar.
 * Todas as cores do sistema saem daqui; as telas não devem usar cores fixas.
 */
public final class Tema {

    // Superfícies
    public static final Color FUNDO = new Color(0x121212);
    public static final Color MENU = new Color(0x0B0B0B);
    public static final Color CARTAO = new Color(0x1C1C1B);
    public static final Color BORDA = new Color(0x2C2C2A);
    public static final Color EIXO = new Color(0x383835);

    // Texto
    public static final Color TEXTO = new Color(0xF2F2F0);
    public static final Color TEXTO_SECUNDARIO = new Color(0xC3C2B7);
    public static final Color TEXTO_FRACO = new Color(0x898781);

    // Marca (interface: botões, títulos, foco)
    public static final Color DESTAQUE = new Color(0xF5A524);
    public static final Color DESTAQUE_TEXTO = new Color(0x141414);

    // Status (sempre acompanhados de ícone/rótulo)
    public static final Color SUCESSO = new Color(0x0CA30C);
    public static final Color AVISO = new Color(0xFAB219);
    public static final Color PERIGO = new Color(0xFF6B6B);

    /**
     * Série única dos gráficos: âmbar validado para o fundo escuro
     * (faixa de luminosidade e contraste &gt;= 3:1 sobre CARTAO).
     */
    public static final Color SERIE_UNICA = new Color(0xC98500);

    /**
     * Paleta categórica, em ordem fixa (nunca reciclada). Validada para o
     * fundo CARTAO: separação para daltonismo e contraste &gt;= 3:1.
     */
    public static final List<Color> SERIES = Arrays.asList(
            new Color(0x3987E5), // azul
            new Color(0xD95926), // laranja
            new Color(0x199E70), // verde-água
            new Color(0xC98500)); // amarelo

    private Tema() {
    }

    /** Aplica o tema. Chamar uma vez, antes de abrir a primeira janela. */
    public static void aplicar() {
        Map<String, String> p = new HashMap<>();
        p.put("@background", "#121212");
        p.put("@foreground", "#F2F2F0");
        p.put("@accentColor", "#F5A524");
        p.put("@selectionBackground", "#4A3A12");
        p.put("@selectionForeground", "#FFFFFF");
        p.put("@selectionInactiveBackground", "#3A2F14");
        p.put("Component.arc", "10");
        p.put("Button.arc", "10");
        p.put("TextComponent.arc", "8");
        p.put("Component.focusWidth", "1");
        p.put("Component.focusColor", "#F5A524");
        p.put("Component.borderColor", "#34342F");
        p.put("TextField.background", "#1C1C1B");
        p.put("PasswordField.background", "#1C1C1B");
        p.put("ComboBox.background", "#1C1C1B");
        p.put("Spinner.background", "#1C1C1B");
        p.put("TextField.disabledBackground", "#161615");
        p.put("Button.background", "#262625");
        p.put("Button.hoverBackground", "#30302E");
        p.put("Button.default.background", "#F5A524");
        p.put("Button.default.foreground", "#141414");
        p.put("Button.default.hoverBackground", "#FFB63D");
        p.put("Button.default.boldText", "true");
        p.put("Table.background", "#1C1C1B");
        p.put("Table.alternateRowColor", "#202020");
        p.put("Table.rowHeight", "28");
        p.put("Table.showHorizontalLines", "true");
        p.put("Table.showVerticalLines", "false");
        p.put("Table.gridColor", "#2C2C2A");
        p.put("Table.intercellSpacing", "0,1");
        p.put("TableHeader.background", "#161615");
        p.put("TableHeader.foreground", "#C3C2B7");
        p.put("TableHeader.height", "30");
        p.put("TableHeader.separatorColor", "#2C2C2A");
        p.put("TableHeader.bottomSeparatorColor", "#383835");
        p.put("ScrollPane.border", "1,1,1,1,#2C2C2A");
        p.put("ScrollBar.thumbArc", "999");
        p.put("ScrollBar.thumbInsets", "2,2,2,2");
        p.put("ScrollBar.width", "10");
        p.put("TitlePane.background", "#0B0B0B");
        p.put("TitlePane.unifiedBackground", "false");
        p.put("OptionPane.background", "#1C1C1B");
        p.put("Panel.background", "#121212");
        p.put("ToolTip.background", "#262625");
        p.put("ToolTip.foreground", "#F2F2F0");
        FlatLaf.setGlobalExtraDefaults(p);
        FlatDarkLaf.setup();
    }

    /** Ícone da janela e cor de fundo padrão. Chamar no construtor de cada tela. */
    public static void janela(Window janela) {
        janela.setIconImages(Arrays.asList(icone(16), icone(32), icone(64)));
        janela.setBackground(FUNDO);
    }

    /** Tamanho padrão das telas do sistema (o mesmo do dashboard), centralizada. */
    public static void tamanhoPadrao(javax.swing.JFrame tela) {
        tela.setMinimumSize(new java.awt.Dimension(1180, 720));
        tela.setSize(1280, 800);
        tela.setLocationRelativeTo(null);
        tela.getContentPane().setBackground(FUNDO);
    }

    /** Cartão com borda (formulários e blocos laterais). */
    public static void cartao(javax.swing.JPanel painel) {
        painel.setBackground(CARTAO);
        painel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(BORDA),
                javax.swing.BorderFactory.createEmptyBorder(16, 18, 18, 18)));
    }

    /** Deixa painéis de layout sem fundo próprio (herdam o fundo da tela ou do cartão). */
    public static void transparente(javax.swing.JComponent... paineis) {
        for (javax.swing.JComponent p : paineis) {
            p.setOpaque(false);
        }
    }

    /** Título de tela em destaque. */
    public static void titulo(JLabel... titulos) {
        for (JLabel t : titulos) {
            t.setForeground(DESTAQUE);
            // Folga para o negrito grande não cortar a última letra.
            t.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 10));
        }
    }

    /** Botão da ação principal da tela (fundo âmbar). */
    public static void primario(JButton... botoes) {
        for (JButton b : botoes) {
            b.putClientProperty("FlatLaf.style",
                    "background: #F5A524; foreground: #141414; hoverBackground: #FFB63D;"
                    + " pressedBackground: #D98E14; font: bold; borderWidth: 0; focusWidth: 0;"
                    + " disabledBackground: #3A3016; disabledText: #8C7A4E");
        }
    }

    /** Botão de ação destrutiva (excluir, cancelar venda). */
    public static void perigo(JButton... botoes) {
        for (JButton b : botoes) {
            b.putClientProperty("FlatLaf.style",
                    "foreground: #FF6B6B; borderColor: #5A2A2A; hoverBorderColor: #FF6B6B");
        }
    }

    /** Texto secundário (rótulos de apoio). */
    public static void secundario(JComponent... componentes) {
        for (JComponent c : componentes) {
            c.setForeground(TEXTO_SECUNDARIO);
        }
    }

    /** Ícone "B" âmbar em quadrado arredondado, desenhado em código. */
    public static Image icone(int tamanho) {
        BufferedImage img = new BufferedImage(tamanho, tamanho, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(DESTAQUE);
        int arco = Math.max(4, tamanho / 4);
        g.fillRoundRect(0, 0, tamanho, tamanho, arco, arco);
        g.setColor(DESTAQUE_TEXTO);
        g.setFont(new Font("Segoe UI", Font.BOLD, (int) (tamanho * 0.72)));
        java.awt.FontMetrics fm = g.getFontMetrics();
        String b = "B";
        g.drawString(b, (tamanho - fm.stringWidth(b)) / 2, (tamanho - fm.getHeight()) / 2 + fm.getAscent());
        g.dispose();
        return img;
    }

    /** Formata valor em reais: R$ 1.234,56. */
    public static String reais(double valor) {
        return String.format(new java.util.Locale("pt", "BR"), "R$ %,.2f", valor);
    }

    /** Formato compacto para eixos: R$ 1,2 mil. */
    public static String reaisCompacto(double valor) {
        java.util.Locale br = new java.util.Locale("pt", "BR");
        if (Math.abs(valor) >= 1000) {
            return String.format(br, "R$ %.1f mil", valor / 1000);
        }
        return String.format(br, "R$ %.0f", valor);
    }
}
