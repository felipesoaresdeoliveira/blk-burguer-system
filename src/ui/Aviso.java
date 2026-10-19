package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Aviso discreto no canto inferior direito da janela, que some sozinho.
 * Substitui as caixas de "Salvo com sucesso" que travavam a tela.
 */
public final class Aviso {

    private enum Tipo { SUCESSO, INFO, ERRO }

    private Aviso() {
    }

    public static void sucesso(Component origem, String mensagem) {
        mostrar(origem, mensagem, Tipo.SUCESSO);
    }

    public static void info(Component origem, String mensagem) {
        mostrar(origem, mensagem, Tipo.INFO);
    }

    public static void erro(Component origem, String mensagem) {
        mostrar(origem, mensagem, Tipo.ERRO);
    }

    private static void mostrar(Component origem, String mensagem, Tipo tipo) {
        JRootPane raiz = origem == null ? null : SwingUtilities.getRootPane(origem);
        if (raiz == null) {
            javax.swing.JOptionPane.showMessageDialog(origem, mensagem);
            return;
        }
        Color cor = tipo == Tipo.SUCESSO ? Tema.SUCESSO : tipo == Tipo.ERRO ? Tema.PERIGO : Tema.INFO;
        String icone = tipo == Tipo.SUCESSO ? "circle-check" : tipo == Tipo.ERRO ? "circle-x" : "info";

        JPanel cartao = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(new Color(0, 0, 0, 60));
                g.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 4, 14, 14);
                g.setColor(Tema.CARTAO);
                g.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 6, 14, 14);
                g.setColor(cor);
                g.fillRoundRect(0, 0, 5, getHeight() - 6, 4, 4);
                g.setColor(Tema.BORDA);
                g.drawRoundRect(0, 0, getWidth() - 5, getHeight() - 7, 14, 14);
                g.dispose();
            }
        };
        cartao.setOpaque(false);
        cartao.setBorder(BorderFactory.createEmptyBorder(12, 18, 16, 20));
        JLabel ic = new JLabel(Icones.get(icone, 20, cor));
        JLabel texto = new JLabel("<html>" + mensagem.replace("\n", "<br>") + "</html>");
        texto.setFont(texto.getFont().deriveFont(Font.BOLD, 13f));
        texto.setForeground(Tema.TEXTO);
        cartao.add(ic, BorderLayout.WEST);
        cartao.add(texto, BorderLayout.CENTER);
        Dimension d = cartao.getPreferredSize();
        int largura = Math.min(Math.max(d.width, 260), 460);
        int altura = cartao.getPreferredSize().height;

        JLayeredPane camadas = raiz.getLayeredPane();
        // Empilha acima de avisos que ainda estão na tela.
        int deslocamento = 0;
        for (Component c : camadas.getComponentsInLayer(JLayeredPane.POPUP_LAYER)) {
            if ("BLK.aviso".equals(c.getName())) {
                deslocamento += c.getHeight() + 8;
            }
        }
        cartao.setName("BLK.aviso");
        cartao.setBounds(camadas.getWidth() - largura - 24, camadas.getHeight() - altura - 24 - deslocamento, largura, altura);
        camadas.add(cartao, JLayeredPane.POPUP_LAYER);
        camadas.repaint();

        Timer fechar = new Timer(tipo == Tipo.ERRO ? 5000 : 3000, e -> {
            camadas.remove(cartao);
            camadas.repaint();
        });
        fechar.setRepeats(false);
        fechar.start();
    }
}
