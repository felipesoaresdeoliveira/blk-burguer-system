package telas;

import entidades.Modulo;
import entidades.Perfil;
import entidades.Usuario;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import service.Sessao;
import ui.Acabamento;
import ui.Icones;
import ui.Tema;

/**
 * Janela única do sistema: menu lateral fixo (agrupado e com ícones, filtrado
 * pelo perfil), barra superior com o caminho e o relógio, e a área onde cada
 * módulo é exibido. As telas são painéis ({@link Navegavel}) trocados aqui.
 */
public class Main extends javax.swing.JFrame {

    /** Um item do menu lateral. */
    private static final class Item {
        final String grupo;
        final String rotulo;
        final String icone;
        final Modulo modulo;

        Item(String grupo, String rotulo, String icone, Modulo modulo) {
            this.grupo = grupo;
            this.rotulo = rotulo;
            this.icone = icone;
            this.modulo = modulo;
        }
    }

    /** Itens do menu, na ordem em que aparecem. */
    private static final List<Item> ITENS = new ArrayList<>();
    static {
        ITENS.add(new Item("", "Visão geral", "layout-dashboard", Modulo.DASHBOARD));
        ITENS.add(new Item("Operação", "Caixa", "wallet", Modulo.CAIXA));
        ITENS.add(new Item("Operação", "Mesas", "armchair", Modulo.MESAS));
        ITENS.add(new Item("Operação", "Pedidos", "clipboard-list", Modulo.PEDIDOS));
        ITENS.add(new Item("Operação", "Cozinha", "chef-hat", Modulo.COZINHA));
        ITENS.add(new Item("Cadastros", "Clientes", "users", Modulo.CLIENTES));
        ITENS.add(new Item("Cadastros", "Produtos", "sandwich", Modulo.PRODUTOS));
        ITENS.add(new Item("Cadastros", "Estoque", "package", Modulo.ESTOQUE));
        ITENS.add(new Item("Gestão", "Histórico de vendas", "history", Modulo.HISTORICO));
        ITENS.add(new Item("Gestão", "Relatórios", "chart-column", Modulo.RELATORIOS));
        ITENS.add(new Item("Gestão", "Fechamento do dia", "calendar-check", Modulo.RELATORIOS));
        ITENS.add(new Item("Gestão", "Despesas", "banknote", Modulo.DESPESAS));
        ITENS.add(new Item("Gestão", "Usuários", "shield-user", Modulo.USUARIOS));
        ITENS.add(new Item("Gestão", "Auditoria", "scroll-text", Modulo.AUDITORIA));
    }

    /** Cria a tela de um item do menu. */
    private static JPanel criarTela(String rotulo) {
        switch (rotulo) {
            case "Caixa": return new TelaCaixa();
            case "Mesas": return new TelaMesas();
            case "Pedidos": return new TelaPedidos();
            case "Cozinha": return new TelaCozinha();
            case "Clientes": return new TelaClientes();
            case "Produtos": return new TelaProdutos();
            case "Estoque": return new TelaEstoque();
            case "Histórico de vendas": return new TelaHistoricoVendas();
            case "Relatórios": return new TelaRelatorio();
            case "Fechamento do dia": return new TelaFechamento();
            case "Despesas": return new TelaDespesas();
            case "Usuários": return new TelaUsuarios();
            case "Auditoria": return new TelaAuditoria();
            default: return new TelaVisaoGeral();
        }
    }

    /** Qual item do menu fica marcado quando esta tela está aberta. */
    private static String itemDaTela(JPanel tela) {
        if (tela instanceof Vendas || tela instanceof TelaCaixa) return "Caixa";
        if (tela instanceof TelaComanda || tela instanceof TelaMesas) return "Mesas";
        if (tela instanceof TelaPedidos) return "Pedidos";
        if (tela instanceof TelaCozinha) return "Cozinha";
        if (tela instanceof TelaClientes) return "Clientes";
        if (tela instanceof TelaProdutos) return "Produtos";
        if (tela instanceof TelaEstoque) return "Estoque";
        if (tela instanceof TelaHistoricoVendas) return "Histórico de vendas";
        if (tela instanceof TelaRelatorio) return "Relatórios";
        if (tela instanceof TelaFechamento) return "Fechamento do dia";
        if (tela instanceof TelaDespesas) return "Despesas";
        if (tela instanceof TelaUsuarios) return "Usuários";
        if (tela instanceof TelaAuditoria) return "Auditoria";
        return "Visão geral";
    }

    /** Telas que ficam "dentro" de um item do menu mostram um complemento no caminho. */
    private static String complementoDaTela(JPanel tela) {
        if (tela instanceof Vendas) return "Nova venda";
        if (tela instanceof TelaComanda) return "Comanda";
        return null;
    }

    private static Main atual;

    private final Map<String, JButton> botoesMenu = new LinkedHashMap<>();
    private JPanel telaAtual;
    private final Timer relogio;

    public Main() {
        initComponents();
        aplicarVisual();
        montarMenu();
        relogio = new Timer(1000, e -> atualizarRelogio());
        relogio.start();
        atualizarRelogio();
        Tema.tamanhoPadrao(this);
    }

    // ------------------------------------------------------------ navegação (uso pelas telas)

    /** Abre a tela inicial do usuário logado: a cozinha vai direto para a TV. */
    public static void abrirInicio() {
        Usuario u = Sessao.usuario();
        if (u != null && u.getPerfil() == Perfil.COZINHA) {
            CozinhaTV.abrir();
            return;
        }
        navegar(telaInicial());
    }

    /** Primeira tela do perfil: visão geral, caixa ou o primeiro módulo permitido. */
    private static JPanel telaInicial() {
        if (Sessao.pode(Modulo.DASHBOARD)) {
            return criarTela("Visão geral");
        }
        if (Sessao.pode(Modulo.CAIXA)) {
            return criarTela("Caixa");
        }
        for (Item i : ITENS) {
            if (Sessao.pode(i.modulo)) {
                return criarTela(i.rotulo);
            }
        }
        return criarTela("Visão geral");
    }

    /** Mostra a tela na janela única (cria a janela se ainda não existe). */
    public static void navegar(JPanel tela) {
        if (atual == null) {
            atual = new Main();
            atual.setVisible(true);
        }
        atual.mostrar(tela);
    }

    /** Volta para a tela inicial do perfil. */
    public static void voltar(Component origem) {
        navegar(telaInicial());
    }

    /** Compatibilidade: o antigo "Menu" agora é a tela inicial. */
    public static void abrirMenu(Component origem) {
        voltar(origem);
    }

    /** Encerra a sessão, fecha as janelas do sistema e volta para o login. */
    public static void sair(Component origem) {
        if (atual != null) {
            atual.fechar();
        }
        java.awt.Window janela = origem == null ? null : SwingUtilities.getWindowAncestor(origem);
        if (janela != null) {
            janela.dispose();
        }
        Sessao.encerrar();
        new Login().setVisible(true);
    }

    /** Abre uma tela sozinha numa janela (útil para testar uma tela isolada). */
    public static void testar(JPanel tela) {
        Tema.aplicar();
        navegar(tela);
    }

    // ------------------------------------------------------------ janela

    private void aplicarVisual() {
        Tema.janela(this);
        getContentPane().setBackground(Tema.FUNDO);
        painelMenu.setBackground(Tema.MENU);
        painelMenu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, Tema.BORDA), painelMenu.getBorder()));
        Tema.transparente(painelMarca, painelMarcaTexto, painelRodapeMenu, painelAcoesMenu, painelNav, painelTopoDireita);
        painelRodapeMenu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Tema.BORDA), BorderFactory.createEmptyBorder(10, 0, 0, 0)));
        scrollMenu.setBorder(BorderFactory.createEmptyBorder());
        scrollMenu.setOpaque(false);
        scrollMenu.getViewport().setOpaque(false);
        scrollMenu.getVerticalScrollBar().setUnitIncrement(16);
        painelPrincipal.setBackground(Tema.FUNDO);
        painelConteudo.setBackground(Tema.FUNDO);
        painelTopo.setBackground(Tema.FUNDO);
        painelTopo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Tema.BORDA), painelTopo.getBorder()));
        lblMarcaIcone.setIcon(new javax.swing.ImageIcon(Tema.icone(36)));
        lblLogo.setForeground(Tema.DESTAQUE);
        lblSlogan.setForeground(Tema.TEXTO_FRACO);
        lblCaminho.setForeground(Tema.TEXTO_SECUNDARIO);
        lblRelogio.setForeground(Tema.TEXTO_SECUNDARIO);
        lblRelogio.setIcon(Icones.comCor("clock", 16, () -> Tema.TEXTO_SECUNDARIO));
        lblRelogio.setIconTextGap(6);

        Usuario u = Sessao.usuario();
        lblUsuarioLogado.setText(u == null ? "Não identificado"
                : "<html><b>" + u.getNome() + "</b><br><span style='font-size:9pt'>" + u.getPerfil() + "</span></html>");
        lblUsuarioLogado.setForeground(Tema.TEXTO_SECUNDARIO);
        lblUsuarioLogado.setIcon(Icones.comCor("user", 18, () -> Tema.TEXTO_SECUNDARIO));
        lblUsuarioLogado.setIconTextGap(10);
        lblUsuarioLogado.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        for (JButton b : new JButton[]{btnTema, btnSair}) {
            estiloMenu(b);
            b.putClientProperty("FlatLaf.style", "margin: 8,8,8,8; hoverBackground: " + hex(Tema.HOVER));
            b.setText(null);
        }
        btnTema.setToolTipText(Tema.isEscuro() ? "Usar tema claro" : "Usar tema escuro");
        btnTema.setIcon(Icones.comCor(Tema.isEscuro() ? "sun" : "moon", 18, () -> Tema.TEXTO_SECUNDARIO));
        btnSair.setIcon(Icones.comCor("log-out", 18, () -> Tema.TEXTO_SECUNDARIO));
    }

    private static void estiloMenu(JButton b) {
        b.putClientProperty("JButton.buttonType", "toolBarButton");
        b.setFocusable(false);
        b.setHorizontalAlignment(JButton.LEFT);
        b.setIconTextGap(12);
        b.setForeground(Tema.TEXTO_SECUNDARIO);
        b.putClientProperty("FlatLaf.style", "margin: 8,12,8,12; hoverBackground: " + hex(Tema.HOVER));
        // Sem fundo próprio: um item que deixou de ser o selecionado não fica cinza.
        b.setBackground(Tema.MENU);
    }

    private static String hex(java.awt.Color c) {
        return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    }

    /** Menu lateral agrupado, só com os módulos que o perfil acessa. */
    private void montarMenu() {
        painelNav.setLayout(new BoxLayout(painelNav, BoxLayout.Y_AXIS));
        painelNav.removeAll();
        String grupoAtual = null;
        for (Item i : ITENS) {
            if (!Sessao.pode(i.modulo)) {
                continue;
            }
            if (!i.grupo.equals(grupoAtual)) {
                grupoAtual = i.grupo;
                if (!i.grupo.isEmpty()) {
                    JLabel cab = new JLabel(i.grupo.toUpperCase());
                    cab.setFont(cab.getFont().deriveFont(Font.BOLD, 11f));
                    cab.setForeground(Tema.TEXTO_FRACO);
                    cab.setBorder(BorderFactory.createEmptyBorder(12, 12, 4, 0));
                    cab.setAlignmentX(Component.LEFT_ALIGNMENT);
                    painelNav.add(cab);
                }
            }
            JButton b = new JButton(i.rotulo);
            estiloMenu(b);
            b.setIcon(Icones.comCor(i.icone, 18, () -> Tema.TEXTO_SECUNDARIO));
            b.setAlignmentX(Component.LEFT_ALIGNMENT);
            b.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 40));
            b.addActionListener(e -> mostrar(criarTela(i.rotulo)));
            botoesMenu.put(i.rotulo, b);
            painelNav.add(b);
            painelNav.add(Box.createVerticalStrut(2));
        }
        painelNav.revalidate();
    }

    /** Destaca no menu o item da tela aberta. */
    private void marcarMenu(JPanel tela) {
        String marcado = itemDaTela(tela);
        for (Item item : ITENS) {
            JButton b = botoesMenu.get(item.rotulo);
            if (b == null) {
                continue;
            }
            if (item.rotulo.equals(marcado)) {
                b.putClientProperty("JButton.buttonType", null);
                b.putClientProperty("FlatLaf.style", "margin: 8,12,8,12; font: bold; borderWidth: 0; focusWidth: 0;"
                        + " background: " + hex(Tema.SELECIONADO) + "; foreground: " + hex(Tema.DESTAQUE));
                b.setIcon(Icones.comCor(item.icone, 18, () -> Tema.DESTAQUE));
            } else {
                estiloMenu(b);
                b.setIcon(Icones.comCor(item.icone, 18, () -> Tema.TEXTO_SECUNDARIO));
            }
        }
    }

    /** Caminho mostrado na barra superior: "Operação › Caixa › Nova venda". */
    private String caminho(JPanel tela) {
        String marcado = itemDaTela(tela);
        for (Item i : ITENS) {
            if (i.rotulo.equals(marcado)) {
                String base = (i.grupo.isEmpty() ? "" : i.grupo + "   ›   ") + i.rotulo;
                String complemento = complementoDaTela(tela);
                return complemento == null ? base : base + "   ›   " + complemento;
            }
        }
        return "BLK Burguer";
    }

    private void mostrar(JPanel tela) {
        if (telaAtual instanceof Navegavel) {
            ((Navegavel) telaAtual).aoSair();
        }
        painelConteudo.removeAll();
        painelConteudo.add(tela, BorderLayout.CENTER);
        telaAtual = tela;
        if (tela instanceof TelaCozinha) {
            ((TelaCozinha) tela).modoTV(false);
        }
        Acabamento.aplicar(tela, true);
        Object padrao = tela.getClientProperty("BLK.botaoPadrao");
        getRootPane().setDefaultButton(padrao instanceof JButton ? (JButton) padrao : null);
        lblCaminho.setText(caminho(tela));
        marcarMenu(tela);
        setTitle("BLK Burguer - " + caminho(tela).replace("   ›   ", " / "));
        painelConteudo.revalidate();
        painelConteudo.repaint();
        tela.requestFocusInWindow();
    }

    private void atualizarRelogio() {
        lblRelogio.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
    }

    private void fechar() {
        if (telaAtual instanceof Navegavel) {
            ((Navegavel) telaAtual).aoSair();
        }
        relogio.stop();
        dispose();
        if (atual == this) {
            atual = null;
        }
    }

    /** Troca o tema e recria a janela na mesma tela (as cores são lidas na criação). */
    private void trocarTema() {
        String itemAberto = telaAtual == null ? null : itemDaTela(telaAtual);
        Tema.alternar();
        java.awt.Rectangle limites = getBounds();
        int estado = getExtendedState();
        fechar();
        navegar(itemAberto == null ? telaInicial() : criarTela(itemAberto));
        atual.setBounds(limites);
        atual.setExtendedState(estado);
        ui.Aviso.info(atual.painelConteudo, Tema.isEscuro() ? "Tema escuro ativado" : "Tema claro ativado");
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        painelMenu = new javax.swing.JPanel();
        painelMarca = new javax.swing.JPanel();
        lblMarcaIcone = new javax.swing.JLabel();
        painelMarcaTexto = new javax.swing.JPanel();
        lblLogo = new javax.swing.JLabel();
        lblSlogan = new javax.swing.JLabel();
        scrollMenu = new javax.swing.JScrollPane();
        painelNav = new javax.swing.JPanel();
        painelRodapeMenu = new javax.swing.JPanel();
        lblUsuarioLogado = new javax.swing.JLabel();
        painelAcoesMenu = new javax.swing.JPanel();
        btnTema = new javax.swing.JButton();
        btnSair = new javax.swing.JButton();
        painelPrincipal = new javax.swing.JPanel();
        painelTopo = new javax.swing.JPanel();
        lblCaminho = new javax.swing.JLabel();
        painelTopoDireita = new javax.swing.JPanel();
        lblRelogio = new javax.swing.JLabel();
        painelConteudo = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("BLK Burguer");
        setMinimumSize(new java.awt.Dimension(1180, 720));

        painelMenu.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 14, 16, 14));
        painelMenu.setPreferredSize(new java.awt.Dimension(248, 0));
        painelMenu.setLayout(new java.awt.BorderLayout(0, 18));
        painelMarca.setLayout(new java.awt.BorderLayout(10, 0));
        painelMarca.add(lblMarcaIcone, java.awt.BorderLayout.LINE_START);

        painelMarcaTexto.setLayout(new java.awt.GridLayout(2, 1, 0, 0));
        lblLogo.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblLogo.setText("BLK BURGUER");
        painelMarcaTexto.add(lblLogo);

        lblSlogan.setText("Gestão da hamburgueria");
        painelMarcaTexto.add(lblSlogan);

        painelMarca.add(painelMarcaTexto, java.awt.BorderLayout.CENTER);

        painelMenu.add(painelMarca, java.awt.BorderLayout.PAGE_START);

        scrollMenu.setViewportView(painelNav);
        painelMenu.add(scrollMenu, java.awt.BorderLayout.CENTER);

        painelRodapeMenu.setLayout(new java.awt.BorderLayout(4, 0));
        lblUsuarioLogado.setText("usuário");
        painelRodapeMenu.add(lblUsuarioLogado, java.awt.BorderLayout.CENTER);

        painelAcoesMenu.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 2, 0));
        btnTema.setToolTipText("Alternar tema claro/escuro");
        btnTema.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTemaActionPerformed(evt);
            }
        });
        painelAcoesMenu.add(btnTema);

        btnSair.setToolTipText("Sair / trocar usuário");
        btnSair.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSairActionPerformed(evt);
            }
        });
        painelAcoesMenu.add(btnSair);

        painelRodapeMenu.add(painelAcoesMenu, java.awt.BorderLayout.LINE_END);

        painelMenu.add(painelRodapeMenu, java.awt.BorderLayout.PAGE_END);

        getContentPane().add(painelMenu, java.awt.BorderLayout.LINE_START);

        painelPrincipal.setLayout(new java.awt.BorderLayout());
        painelTopo.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 24, 12, 24));
        painelTopo.setLayout(new java.awt.BorderLayout());
        lblCaminho.setText("Início");
        painelTopo.add(lblCaminho, java.awt.BorderLayout.LINE_START);

        painelTopoDireita.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 14, 0));
        lblRelogio.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblRelogio.setText("00:00");
        painelTopoDireita.add(lblRelogio);

        painelTopo.add(painelTopoDireita, java.awt.BorderLayout.LINE_END);

        painelPrincipal.add(painelTopo, java.awt.BorderLayout.PAGE_START);

        painelConteudo.setLayout(new java.awt.BorderLayout());
        painelPrincipal.add(painelConteudo, java.awt.BorderLayout.CENTER);

        getContentPane().add(painelPrincipal, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnTemaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTemaActionPerformed
        trocarTema();
    }//GEN-LAST:event_btnTemaActionPerformed

    private void btnSairActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSairActionPerformed
        sair(this);
    }//GEN-LAST:event_btnSairActionPerformed

    public static void main(String args[]) {
        // Sem login a janela não tem permissões: o sistema sempre começa pelo Login.
        Tema.aplicar();
        java.awt.EventQueue.invokeLater(() -> new Login().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnSair;
    private javax.swing.JButton btnTema;
    private javax.swing.JLabel lblCaminho;
    private javax.swing.JLabel lblLogo;
    private javax.swing.JLabel lblMarcaIcone;
    private javax.swing.JLabel lblRelogio;
    private javax.swing.JLabel lblSlogan;
    private javax.swing.JLabel lblUsuarioLogado;
    private javax.swing.JPanel painelAcoesMenu;
    private javax.swing.JPanel painelConteudo;
    private javax.swing.JPanel painelMarca;
    private javax.swing.JPanel painelMarcaTexto;
    private javax.swing.JPanel painelMenu;
    private javax.swing.JPanel painelNav;
    private javax.swing.JPanel painelPrincipal;
    private javax.swing.JPanel painelRodapeMenu;
    private javax.swing.JPanel painelTopo;
    private javax.swing.JPanel painelTopoDireita;
    private javax.swing.JScrollPane scrollMenu;
    // End of variables declaration//GEN-END:variables
}
