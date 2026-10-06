package ui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
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
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;

/**
 * Identidade visual do BLK Burguer: tema escuro (padrão) ou claro, com
 * destaque âmbar. Todas as cores do sistema saem daqui; as telas não devem
 * usar cores fixas. Os campos são atualizados quando o tema muda.
 */
public final class Tema {

    // Superfícies
    public static Color FUNDO;
    public static Color MENU;
    public static Color CARTAO;
    public static Color BORDA;
    public static Color EIXO;
    /** Fundo de item de menu/linha sob o mouse. */
    public static Color HOVER;
    /** Fundo do item de menu selecionado. */
    public static Color SELECIONADO;

    // Texto
    public static Color TEXTO;
    public static Color TEXTO_SECUNDARIO;
    public static Color TEXTO_FRACO;

    // Marca (interface: botões, títulos, foco)
    /** Âmbar usado como fundo de botão principal (igual nos dois temas). */
    public static final Color AMBAR = new Color(0xF5A524);
    /** Âmbar para texto/ícone de destaque, com contraste no fundo do tema. */
    public static Color DESTAQUE;
    public static final Color DESTAQUE_TEXTO = new Color(0x141414);

    // Status (sempre acompanhados de ícone/rótulo)
    public static Color SUCESSO;
    public static Color AVISO;
    public static Color PERIGO;
    public static Color INFO;

    /** Série única dos gráficos (validada para o fundo dos cartões de cada tema). */
    public static Color SERIE_UNICA;

    /** Paleta categórica em ordem fixa, validada para o fundo dos cartões de cada tema. */
    public static List<Color> SERIES;

    private static final Preferences PREFS = Preferences.userNodeForPackage(Tema.class);
    private static boolean escuro = true;

    static {
        definirCores(PREFS.getBoolean("temaEscuro", true));
    }

    private Tema() {
    }

    public static boolean isEscuro() {
        return escuro;
    }

    private static void definirCores(boolean temaEscuro) {
        escuro = temaEscuro;
        if (temaEscuro) {
            FUNDO = new Color(0x121212);
            MENU = new Color(0x0B0B0B);
            CARTAO = new Color(0x1C1C1B);
            BORDA = new Color(0x2C2C2A);
            EIXO = new Color(0x383835);
            HOVER = new Color(0x1F1F1E);
            SELECIONADO = new Color(0x2A2413);
            TEXTO = new Color(0xF2F2F0);
            TEXTO_SECUNDARIO = new Color(0xC3C2B7);
            TEXTO_FRACO = new Color(0x898781);
            DESTAQUE = AMBAR;
            SUCESSO = new Color(0x0CA30C);
            AVISO = new Color(0xFAB219);
            PERIGO = new Color(0xFF6B6B);
            INFO = new Color(0x60A5FA);
            SERIE_UNICA = new Color(0xC98500);
            SERIES = Arrays.asList(new Color(0x3987E5), new Color(0xD95926), new Color(0x199E70), new Color(0xC98500));
        } else {
            FUNDO = new Color(0xF4F3EF);
            MENU = new Color(0xFFFFFF);
            CARTAO = new Color(0xFFFFFF);
            BORDA = new Color(0xE3E1DA);
            EIXO = new Color(0xC3C2B7);
            HOVER = new Color(0xF1EFE9);
            SELECIONADO = new Color(0xFCEFD6);
            TEXTO = new Color(0x1A1A19);
            TEXTO_SECUNDARIO = new Color(0x52514E);
            TEXTO_FRACO = new Color(0x7A7873);
            DESTAQUE = new Color(0xA86400);
            SUCESSO = new Color(0x0A7F0A);
            AVISO = new Color(0xA86400);
            PERIGO = new Color(0xC62828);
            INFO = new Color(0x1C5CAB);
            SERIE_UNICA = new Color(0xC27A00);
            SERIES = Arrays.asList(new Color(0x2A78D6), new Color(0xEB6834), new Color(0x1BAF7A), new Color(0xEDA100));
        }
    }

    private static String hex(Color c) {
        return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    }

    /** Cor no formato #RRGGBB para usar em textos HTML dos rótulos. */
    public static String hexHtml(Color c) {
        return hex(c);
    }

    /** Aplica o tema salvo. Chamar uma vez, antes de abrir a primeira janela. */
    public static void aplicar() {
        Map<String, String> p = new HashMap<>();
        p.put("@background", hex(FUNDO));
        p.put("@foreground", hex(TEXTO));
        p.put("@accentColor", hex(AMBAR));
        p.put("@selectionBackground", escuro ? "#4A3A12" : "#FCE3B0");
        p.put("@selectionForeground", escuro ? "#FFFFFF" : "#1A1A19");
        p.put("@selectionInactiveBackground", escuro ? "#3A2F14" : "#F6E7C8");
        p.put("@selectionInactiveForeground", escuro ? "#FFFFFF" : "#1A1A19");
        p.put("Component.arc", "10");
        p.put("Button.arc", "10");
        p.put("TextComponent.arc", "8");
        p.put("Component.focusWidth", "1");
        p.put("Component.focusColor", hex(AMBAR));
        p.put("Component.borderColor", escuro ? "#34342F" : "#D6D3CA");
        p.put("TextField.background", hex(CARTAO));
        p.put("PasswordField.background", hex(CARTAO));
        p.put("ComboBox.background", hex(CARTAO));
        p.put("Spinner.background", hex(CARTAO));
        p.put("TextField.disabledBackground", escuro ? "#161615" : "#EFEDE7");
        p.put("Button.background", escuro ? "#262625" : "#FFFFFF");
        p.put("Button.hoverBackground", escuro ? "#30302E" : "#F1EFE9");
        p.put("Button.borderColor", escuro ? "#3A3A37" : "#D6D3CA");
        p.put("Button.default.background", hex(AMBAR));
        p.put("Button.default.foreground", "#141414");
        p.put("Button.default.hoverBackground", "#FFB63D");
        p.put("Button.default.boldText", "true");
        p.put("Button.margin", "6,14,6,14");
        p.put("Button.iconTextGap", "8");
        p.put("Table.background", hex(CARTAO));
        p.put("Table.alternateRowColor", escuro ? "#202020" : "#FAF9F6");
        p.put("Table.rowHeight", "32");
        p.put("Table.showHorizontalLines", "true");
        p.put("Table.showVerticalLines", "false");
        p.put("Table.gridColor", hex(BORDA));
        p.put("Table.intercellSpacing", "0,1");
        p.put("Table.cellMargins", "2,10,2,10");
        p.put("TableHeader.background", escuro ? "#161615" : "#F4F3EF");
        p.put("TableHeader.foreground", hex(TEXTO_SECUNDARIO));
        p.put("TableHeader.height", "34");
        p.put("TableHeader.cellMargins", "2,10,2,10");
        p.put("TableHeader.separatorColor", hex(BORDA));
        p.put("TableHeader.bottomSeparatorColor", hex(EIXO));
        p.put("ScrollPane.border", "1,1,1,1," + hex(BORDA));
        p.put("ScrollBar.thumbArc", "999");
        p.put("ScrollBar.thumbInsets", "2,2,2,2");
        p.put("ScrollBar.width", "10");
        p.put("TitlePane.background", hex(MENU));
        p.put("TitlePane.unifiedBackground", "false");
        p.put("OptionPane.background", hex(CARTAO));
        p.put("Panel.background", hex(FUNDO));
        p.put("ToolTip.background", escuro ? "#262625" : "#FFFFFF");
        p.put("ToolTip.foreground", hex(TEXTO));
        p.put("PopupMenu.background", hex(CARTAO));
        p.put("MenuItem.selectionBackground", escuro ? "#2A2413" : "#FCEFD6");
        p.put("MenuItem.selectionForeground", hex(TEXTO));
        FlatLaf.setGlobalExtraDefaults(p);
        if (escuro) {
            FlatDarkLaf.setup();
        } else {
            FlatLightLaf.setup();
        }
    }

    /**
     * Troca entre escuro e claro, salva a preferência e atualiza a interface.
     * Telas já abertas devem ser recriadas para pegar as cores novas.
     */
    public static void alternar() {
        definirCores(!escuro);
        PREFS.putBoolean("temaEscuro", escuro);
        aplicar();
        FlatLaf.updateUI();
    }

    /** Ícone da janela e cor de fundo padrão. Chamar no construtor de cada janela. */
    public static void janela(Window janela) {
        janela.setIconImages(Arrays.asList(icone(16), icone(32), icone(64)));
        janela.setBackground(FUNDO);
    }

    /** Tamanho padrão das janelas do sistema, centralizada. */
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
            t.setForeground(TEXTO);
            // Folga para o negrito grande não cortar a última letra.
            t.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 10));
        }
    }

    /** Botão da ação principal da tela (fundo âmbar, texto escuro nos dois temas). */
    public static void primario(JButton... botoes) {
        for (JButton b : botoes) {
            b.putClientProperty("BLK.primario", Boolean.TRUE);
            b.putClientProperty("FlatLaf.style",
                    "background: #F5A524; foreground: #141414; hoverBackground: #FFB63D;"
                    + " pressedBackground: #D98E14; font: bold; borderWidth: 0; focusWidth: 0;"
                    + " disabledBackground: " + (escuro ? "#3A3016" : "#F3E2BD") + "; disabledText: #8C7A4E");
        }
    }

    /** Botão de ação destrutiva (excluir, cancelar venda). */
    public static void perigo(JButton... botoes) {
        String cor = hex(PERIGO);
        for (JButton b : botoes) {
            b.putClientProperty("BLK.perigo", Boolean.TRUE);
            b.putClientProperty("FlatLaf.style",
                    "foreground: " + cor + "; borderColor: " + (escuro ? "#5A2A2A" : "#E8B4B4") + "; hoverBorderColor: " + cor);
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
        g.setColor(AMBAR);
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

    /** Tempo decorrido legível: "12 min", "1h05", "2d 3h". */
    public static String duracao(long minutos) {
        if (minutos < 60) {
            return minutos + " min";
        }
        if (minutos < 24 * 60) {
            return (minutos / 60) + "h" + String.format("%02d", minutos % 60);
        }
        return (minutos / (24 * 60)) + "d " + (minutos / 60 % 24) + "h";
    }
}
