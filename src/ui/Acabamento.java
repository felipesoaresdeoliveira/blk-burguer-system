package ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Acabamento aplicado a cada tela ao ser exibida: ícones nos botões conforme
 * o texto, números alinhados à direita, status em selos coloridos (com
 * texto, nunca só cor) e mensagem nas tabelas vazias.
 */
public final class Acabamento {

    /** Palavra no início do texto do botão -> ícone. A primeira que casar vence. */
    private static final Map<String, String> ICONES = new LinkedHashMap<>();
    static {
        String[][] m = {
            {"Nova venda", "shopping-cart"}, {"Novo delivery", "truck"}, {"Nova retirada", "shopping-bag"},
            {"Nova mesa", "plus"}, {"Nova conta", "plus"}, {"Novo", "plus"}, {"Adicionar", "plus"},
            {"Salvar", "save"}, {"Editar", "pencil"}, {"Excluir", "trash-2"}, {"Remover", "trash-2"},
            {"Tirar", "x"}, {"Cancelar", "x"}, {"Limpar", "x"}, {"Atualizar", "refresh-cw"},
            {"Voltar", "arrow-left"}, {"Filtrar", "search"}, {"Aplicar", "search"}, {"Buscar", "search"},
            {"Receber", "hand-coins"}, {"Finalizar venda", "check"}, {"Enviar", "chef-hat"},
            {"Abrir caixa", "wallet"}, {"Fechar caixa", "lock"}, {"Mesas", "armchair"}, {"Pedidos", "clipboard-list"},
            {"Entrada", "package"}, {"Ajustar", "pencil"}, {"Redefinir senha", "lock"},
            {"Pedir a conta", "receipt"}, {"Cancelar pedido de conta", "receipt"}, {"Transferir", "armchair"},
            {"Saiu para entrega", "truck"}, {"Entregue", "circle-check"}, {"Taxa de entrega", "truck"},
            {"Imprimir", "printer"}, {"Abrir na TV", "maximize"}, {"Sair", "log-out"}, {"Iniciar preparo", "chef-hat"},
            {"Marcar como pronto", "circle-check"}, {"Abrir comanda", "clipboard-list"},
            {"Entrar", "log-in"},
        };
        for (String[] par : m) {
            ICONES.put(par[0], par[1]);
        }
    }

    /** Cabeçalhos de coluna que contêm status (viram selos). */
    private static final Set<String> COLUNAS_STATUS = new HashSet<>(Arrays.asList(
            "Situação", "Andamento", "Cozinha", "Status"));

    private static final Pattern NUMERO = Pattern.compile("^-?(R\\$\\s?)?-?[\\d.]+(,\\d+)?(%| min| un\\.)?$|^\\d+h\\d{2}$|^\\d+d \\d+h$");

    private Acabamento() {
    }

    /**
     * Aplica o acabamento em todos os componentes do container.
     *
     * @param janelaUnica true quando a tela está dentro do aplicativo de
     * janela única: botões "Voltar"/"Menu" ficam ocultos (o menu lateral cumpre
     * esse papel)
     */
    public static void aplicar(Container raiz, boolean janelaUnica) {
        for (Component c : raiz.getComponents()) {
            if (c instanceof JButton) {
                botao((JButton) c, janelaUnica);
            } else if (c instanceof JTable) {
                tabela((JTable) c);
            }
            if (c instanceof Container) {
                aplicar((Container) c, janelaUnica);
            }
        }
    }

    private static void botao(JButton b, boolean janelaUnica) {
        String texto = b.getText() == null ? "" : b.getText().trim();
        if (janelaUnica && (texto.equals("Voltar") || texto.equals("Menu"))) {
            b.setVisible(false);
            return;
        }
        if (b.getIcon() != null || texto.isEmpty()) {
            return;
        }
        for (Map.Entry<String, String> e : ICONES.entrySet()) {
            if (texto.startsWith(e.getKey())) {
                if (Boolean.TRUE.equals(b.getClientProperty("BLK.primario"))) {
                    b.setIcon(Icones.get(e.getValue(), 16, Tema.DESTAQUE_TEXTO));
                } else if (Boolean.TRUE.equals(b.getClientProperty("BLK.perigo"))) {
                    b.setIcon(Icones.comCor(e.getValue(), 16, () -> Tema.PERIGO));
                } else {
                    b.setIcon(Icones.get(e.getValue(), 16));
                }
                return;
            }
        }
    }

    /** Mensagem mostrada quando a tabela não tem linhas. */
    public static void vazio(JTable t, String mensagem) {
        t.putClientProperty("BLK.vazio", mensagem);
        Container pai = t.getParent();
        if (pai != null) {
            pai.repaint();
        }
    }

    private static void tabela(JTable t) {
        if (Boolean.TRUE.equals(t.getClientProperty("BLK.acabamento"))) {
            return;
        }
        t.putClientProperty("BLK.acabamento", Boolean.TRUE);
        Renderizador r = new Renderizador();
        t.setDefaultRenderer(Object.class, r);
        t.setDefaultRenderer(Number.class, r);
        t.setDefaultRenderer(Integer.class, r);
        t.setDefaultRenderer(Double.class, r);
        t.setFillsViewportHeight(true);
        Container pai = t.getParent();
        if (pai instanceof JViewport && pai.getParent() instanceof JScrollPane) {
            JScrollPane sp = (JScrollPane) pai.getParent();
            ViewportVazio vp = new ViewportVazio(t);
            sp.setViewport(vp);
            vp.setView(t);
        }
    }

    /** Cor do selo conforme o texto do status. */
    public static Color corStatus(String s) {
        String t = s.toLowerCase();
        if (t.contains("sem estoque") || t.contains("cancel") || t.contains("atras") || t.contains("desativ")) {
            return Tema.PERIGO;
        }
        if (t.contains("baixo") || t.contains("conta") || t.contains("aguard") || t.contains("preparo")) {
            return Tema.AVISO;
        }
        if (t.equals("ok") || t.contains("pago") || t.contains("pronto") || t.contains("entregue")
                || t.contains("ativa") || t.contains("livre")) {
            return Tema.SUCESSO;
        }
        return Tema.INFO;
    }

    /** Renderizador padrão: números à direita e status como selo. */
    private static class Renderizador extends DefaultTableCellRenderer {
        private boolean selo;
        private Color corSelo;

        @Override
        public Component getTableCellRendererComponent(JTable t, Object valor, boolean sel, boolean foco, int linha, int col) {
            super.getTableCellRendererComponent(t, valor, sel, foco, linha, col);
            String texto = valor == null ? "" : valor.toString();
            String cabecalho = t.getColumnName(col);
            selo = COLUNAS_STATUS.contains(cabecalho) && !texto.isEmpty() && !texto.equals("-");
            corSelo = selo ? corStatus(texto) : null;
            setHorizontalAlignment(!selo && (valor instanceof Number || NUMERO.matcher(texto).matches())
                    ? SwingConstants.RIGHT : SwingConstants.LEFT);
            setFont(t.getFont());
            return this;
        }

        @Override
        protected void paintComponent(Graphics g0) {
            if (!selo) {
                super.paintComponent(g0);
                return;
            }
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setFont(getFont().deriveFont(Font.BOLD, 12f));
            FontMetrics fm = g.getFontMetrics();
            String texto = getText();
            int w = Math.min(getWidth() - 12, fm.stringWidth(texto) + 26);
            int h = fm.getHeight() + 4;
            int x = 8, y = (getHeight() - h) / 2;
            g.setColor(new Color(corSelo.getRed(), corSelo.getGreen(), corSelo.getBlue(), 40));
            g.fillRoundRect(x, y, w, h, h, h);
            g.setColor(corSelo);
            g.fillOval(x + 9, y + h / 2 - 3, 7, 7);
            g.setColor(Tema.TEXTO);
            g.drawString(texto, x + 21, y + (h - fm.getHeight()) / 2 + fm.getAscent());
            g.dispose();
        }
    }

    /** Viewport que escreve uma mensagem quando a tabela está vazia. */
    private static class ViewportVazio extends JViewport {
        private final JTable tabela;

        ViewportVazio(JTable tabela) {
            this.tabela = tabela;
            setBackground(tabela.getBackground());
        }

        @Override
        public void paint(Graphics g0) {
            super.paint(g0);
            if (tabela.getRowCount() > 0) {
                return;
            }
            Object msg = tabela.getClientProperty("BLK.vazio");
            String texto = msg != null ? msg.toString() : "Nada por aqui ainda";
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setFont(getFont().deriveFont(Font.PLAIN, 13f));
            g.setColor(Tema.TEXTO_FRACO);
            FontMetrics fm = g.getFontMetrics();
            int alturaCab = tabela.getTableHeader() != null ? 0 : 0;
            g.drawString(texto, (getWidth() - fm.stringWidth(texto)) / 2, Math.max(48, getHeight() / 2) + alturaCab);
            g.dispose();
        }
    }

    /** Rótulo de texto auxiliar padronizado. */
    public static JLabel rotuloFraco(String texto) {
        JLabel l = new JLabel(texto);
        l.setForeground(Tema.TEXTO_FRACO);
        return l;
    }
}
