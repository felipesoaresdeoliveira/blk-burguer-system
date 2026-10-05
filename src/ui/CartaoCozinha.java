package ui;

import entidades.Pedido;
import entidades.StatusCozinha;
import entidades.Venda;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Cartão de um pedido no painel da cozinha: identificação, cronômetro com
 * status, cada lanche com a montagem, o que tirar (SEM), o que acrescentar
 * (EXTRA) e observações, e o botão para avançar a etapa.
 */
public class CartaoCozinha extends JPanel {

    /** Minutos a partir dos quais o pedido pede atenção e está atrasado. */
    public static final int MIN_ATENCAO = 12;
    public static final int MIN_ATRASADO = 20;

    private static final Color FUNDO_NOVO = new Color(0x2A2413);

    private final Color corBorda;
    private final int espessuraBorda;
    private final boolean novo;
    private final int larguraTexto;
    private final int larguraCartao;

    /**
     * @param itens itens deste pedido na etapa exibida
     * @param minutos minutos desde o envio à cozinha
     * @param proximo marca o primeiro da fila
     * @param novo chegou há poucos segundos (destaque)
     * @param fichas ficha técnica por produto, para a montagem
     * @param acao botão de avanço (pode ser null)
     * @param largura largura disponível para o cartão, usada para quebrar o texto
     */
    public CartaoCozinha(Pedido pedido, List<Venda> itens, StatusCozinha etapa, long minutos,
            boolean proximo, boolean novo, Map<String, Map<String, Integer>> fichas, JButton acao, int largura) {
        this.novo = novo;
        this.larguraCartao = Math.max(220, largura);
        this.larguraTexto = Math.max(160, larguraCartao - 80);
        setOpaque(false);
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        Color corTempo = Tema.TEXTO_SECUNDARIO;
        String rotuloTempo = null;
        if (etapa != StatusCozinha.PRONTO) {
            if (minutos >= MIN_ATRASADO) {
                corTempo = Tema.PERIGO;
                rotuloTempo = "ATRASADO";
            } else if (minutos >= MIN_ATENCAO) {
                corTempo = Tema.AVISO;
                rotuloTempo = "ATENÇÃO";
            }
        }
        if (rotuloTempo != null) {
            corBorda = corTempo;
            espessuraBorda = 3;
        } else if (proximo) {
            corBorda = Tema.DESTAQUE;
            espessuraBorda = 3;
        } else {
            corBorda = Tema.BORDA;
            espessuraBorda = 1;
        }

        // Cabeçalho: número + origem | cronômetro
        JPanel topo = transparente(new BorderLayout(8, 0));
        JPanel ident = transparente(null);
        ident.setLayout(new BoxLayout(ident, BoxLayout.Y_AXIS));
        JPanel linhaNumero = transparente(new FlowLayout(FlowLayout.LEFT, 0, 0));
        linhaNumero.add(rotulo("#" + pedido.getId(), Font.BOLD, 26f, Tema.TEXTO));
        if (proximo) {
            linhaNumero.add(Box.createHorizontalStrut(10));
            linhaNumero.add(selo("PRÓXIMO", Tema.DESTAQUE, Tema.DESTAQUE_TEXTO));
        }
        if (novo) {
            linhaNumero.add(Box.createHorizontalStrut(8));
            linhaNumero.add(selo("NOVO", new Color(0x3987E5), Color.WHITE));
        }
        ident.add(linhaNumero);
        ident.add(rotulo(pedido.getDescricao(), Font.BOLD, 17f, Tema.DESTAQUE));
        topo.add(ident, BorderLayout.CENTER);

        JPanel tempo = transparente(null);
        tempo.setLayout(new BoxLayout(tempo, BoxLayout.Y_AXIS));
        JLabel lblMin = rotulo(minutos + " min", Font.BOLD, 24f, corTempo);
        lblMin.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
        lblMin.setAlignmentX(Component.RIGHT_ALIGNMENT);
        tempo.add(lblMin);
        if (rotuloTempo != null) {
            JLabel lblStatus = rotulo("⚠ " + rotuloTempo, Font.BOLD, 13f, corTempo);
            lblStatus.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
            lblStatus.setAlignmentX(Component.RIGHT_ALIGNMENT);
            tempo.add(lblStatus);
        }
        topo.add(tempo, BorderLayout.EAST);
        add(topo, BorderLayout.NORTH);

        // Itens com montagem e personalização
        JPanel corpo = transparente(null);
        corpo.setLayout(new BoxLayout(corpo, BoxLayout.Y_AXIS));
        for (int i = 0; i < itens.size(); i++) {
            Venda v = itens.get(i);
            if (i > 0) {
                corpo.add(Box.createVerticalStrut(10));
                corpo.add(separador());
                corpo.add(Box.createVerticalStrut(8));
            }
            JLabel nome = rotulo(v.getQuantidade() + "x  " + v.getProduto(), Font.BOLD, 21f, Tema.TEXTO);
            nome.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 12));
            corpo.add(nome);
            Map<String, Integer> ficha = fichas.get(v.getProduto());
            if (ficha != null && !ficha.isEmpty()) {
                // Montagem como checklist em duas colunas (sem o que foi retirado).
                JPanel montagem = transparente(new java.awt.GridLayout(0, 2, 12, 2));
                for (Map.Entry<String, Integer> ing : ficha.entrySet()) {
                    if (v.getRemocoes().contains(ing.getKey())) {
                        continue;
                    }
                    JLabel l = rotulo("▸ " + ing.getKey() + (ing.getValue() > 1 ? "  ×" + ing.getValue() : ""),
                            Font.PLAIN, 14f, Tema.TEXTO_SECUNDARIO);
                    l.setToolTipText(l.getText());
                    montagem.add(l);
                }
                montagem.setMaximumSize(new Dimension(larguraTexto + 48, Integer.MAX_VALUE));
                corpo.add(Box.createVerticalStrut(6));
                corpo.add(montagem);
            }
            if (!v.getRemocoes().isEmpty()) {
                corpo.add(Box.createVerticalStrut(6));
                corpo.add(destaque("SEM", String.join(", ", v.getRemocoes()), Tema.PERIGO));
            }
            if (!v.getAdicionais().isEmpty()) {
                corpo.add(Box.createVerticalStrut(6));
                corpo.add(destaque("EXTRA", String.join(", ", v.getAdicionais()), Tema.DESTAQUE));
            }
            if (v.getObservacao() != null && !v.getObservacao().trim().isEmpty()) {
                corpo.add(Box.createVerticalStrut(6));
                corpo.add(destaque("OBS", v.getObservacao().trim(), new Color(0x60A5FA)));
            }
        }
        add(corpo, BorderLayout.CENTER);

        if (acao != null) {
            acao.setFont(acao.getFont().deriveFont(Font.BOLD, 16f));
            acao.setPreferredSize(new Dimension(10, 44));
            add(acao, BorderLayout.SOUTH);
        }
    }

    private static JPanel transparente(java.awt.LayoutManager layout) {
        JPanel p = layout == null ? new JPanel() : new JPanel(layout);
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    private static JLabel rotulo(String texto, int estilo, float tamanho, Color cor) {
        JLabel l = new JLabel(texto);
        l.setFont(PainelCartao.fonte(estilo, tamanho));
        l.setForeground(cor);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    /** Texto que quebra linha dentro da largura do cartão. */
    /** Quebra o texto em linhas que cabem na largura, medindo com a fonte real. */
    private static String quebrar(String texto, Font fonte, int largura) {
        java.awt.FontMetrics fm = new JLabel().getFontMetrics(fonte);
        StringBuilder html = new StringBuilder("<html>");
        StringBuilder linha = new StringBuilder();
        for (String palavra : texto.split(" ")) {
            String tentativa = linha.length() == 0 ? palavra : linha + " " + palavra;
            if (fm.stringWidth(tentativa) > largura && linha.length() > 0) {
                html.append(escapar(linha.toString())).append("<br>");
                linha = new StringBuilder(palavra);
            } else {
                linha = new StringBuilder(tentativa);
            }
        }
        return html.append(escapar(linha.toString())).append("</html>").toString();
    }

    /** Faixa colorida com rótulo (SEM / EXTRA / OBS) e o texto ao lado. */
    private JPanel destaque(String rotulo, String conteudo, Color cor) {
        JPanel linha = transparente(new BorderLayout(10, 0));
        JLabel etiqueta = selo(rotulo, cor, Tema.DESTAQUE_TEXTO);
        JPanel caixaEtiqueta = transparente(new FlowLayout(FlowLayout.LEFT, 0, 2));
        caixaEtiqueta.add(etiqueta);
        linha.add(caixaEtiqueta, BorderLayout.WEST);
        Font fonte = PainelCartao.fonte(Font.BOLD, 16f);
        int largura = larguraTexto + 48 - etiqueta.getPreferredSize().width - 14;
        linha.add(rotulo(quebrar(conteudo, fonte, largura), Font.BOLD, 16f, Tema.TEXTO), BorderLayout.CENTER);
        return linha;
    }

    private static JLabel selo(String texto, Color fundo, Color frente) {
        JLabel l = new JLabel(texto) {
            @Override
            protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(fundo);
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g.dispose();
                super.paintComponent(g0);
            }
        };
        l.setFont(PainelCartao.fonte(Font.BOLD, 12f));
        l.setForeground(frente);
        l.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        return l;
    }

    private static Component separador() {
        JPanel s = new JPanel();
        s.setBackground(Tema.BORDA);
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        s.setPreferredSize(new Dimension(10, 1));
        s.setAlignmentX(Component.LEFT_ALIGNMENT);
        return s;
    }

    private static String escapar(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(novo ? FUNDO_NOVO : Tema.CARTAO);
        g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
        g.setColor(corBorda);
        g.setStroke(new BasicStroke(espessuraBorda));
        int m = espessuraBorda / 2;
        g.drawRoundRect(m, m, getWidth() - 1 - espessuraBorda + 1, getHeight() - 1 - espessuraBorda + 1, 16, 16);
        g.dispose();
    }

    /** O cartão ocupa exatamente a largura da coluna; a altura acompanha o conteúdo. */
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(larguraCartao, super.getPreferredSize().height);
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }
}
