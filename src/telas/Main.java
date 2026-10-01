package telas;

import dao.DashboardDAO;
import entidades.Estoque;
import entidades.FormaPagamento;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import entidades.Modulo;
import entidades.Perfil;
import entidades.Usuario;
import service.Sessao;
import ui.CartaoIndicador;
import ui.Tema;

/** Tela inicial: dashboard com indicadores do dia, gráficos e navegação. */
public class Main extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Main.class.getName());
    private static final Locale BR = new Locale("pt", "BR");

    private final DashboardDAO dashboardDAO = new DashboardDAO();

    public Main() {
        initComponents();
        aplicarVisual();
        aplicarPermissoes();
        Tema.tamanhoPadrao(this);
        if (Sessao.pode(Modulo.DASHBOARD)) {
            carregarDados();
        }
    }

    // ------------------------------------------------------------ navegação

    /** Abre a tela inicial do usuário logado: a cozinha vai direto para o painel da TV. */
    public static void abrirInicio() {
        Usuario u = Sessao.usuario();
        if (u != null && u.getPerfil() == Perfil.COZINHA) {
            TelaCozinha.abrirTelaCheia();
        } else {
            new Main().setVisible(true);
        }
    }

    /** Volta de uma tela para o início do usuário. */
    public static void voltar(javax.swing.JFrame tela) {
        tela.dispose();
        abrirInicio();
    }

    /** Encerra a sessão e volta para o login. */
    public static void sair(javax.swing.JFrame tela) {
        Sessao.encerrar();
        tela.dispose();
        new Login().setVisible(true);
    }

    /** Esconde do menu o que o perfil não acessa; sem dashboard, mostra atalhos. */
    private void aplicarPermissoes() {
        Object[][] itens = {
            {btnVisaoGeral, Modulo.DASHBOARD}, {btnVenda, Modulo.VENDA_BALCAO}, {btnCaixa, Modulo.CAIXA},
            {btnCozinha, Modulo.COZINHA}, {btnHistorico, Modulo.HISTORICO}, {btnProdutos, Modulo.PRODUTOS},
            {btnEstoque, Modulo.ESTOQUE}, {btnRelatorio, Modulo.RELATORIOS}, {btnUsuarios, Modulo.USUARIOS}};
        java.util.List<javax.swing.JButton> atalhos = new java.util.ArrayList<>();
        for (Object[] item : itens) {
            javax.swing.JButton b = (javax.swing.JButton) item[0];
            boolean pode = Sessao.pode((Modulo) item[1]);
            if (!pode) {
                // Remover (e não só esconder): a grade não deixa buraco no menu.
                painelBotoes.remove(b);
            }
            if (pode && b != btnVisaoGeral) {
                atalhos.add(b);
            }
        }
        Usuario u = Sessao.usuario();
        lblUsuarioLogado.setText(u == null ? "Não identificado"
                : "<html><b>" + u.getNome() + "</b><br>" + u.getPerfil() + "</html>");
        if (!Sessao.pode(Modulo.DASHBOARD)) {
            btnAtualizar.setVisible(false);
            lblTitulo.setText("Olá, " + (u == null ? "" : u.getNome()) + "!");
            lblData.setText("Escolha o que deseja fazer");
            painelConteudo.remove(painelCorpo);
            painelConteudo.add(montarAtalhos(atalhos), java.awt.BorderLayout.CENTER);
        }
    }

    /** Botões grandes para os módulos do perfil (usado por quem não vê o dashboard). */
    private javax.swing.JPanel montarAtalhos(java.util.List<javax.swing.JButton> modulos) {
        javax.swing.JPanel grade = new javax.swing.JPanel(new java.awt.GridLayout(0, 3, 16, 16));
        grade.setBackground(Tema.FUNDO);
        for (javax.swing.JButton origem : modulos) {
            javax.swing.JButton b = new javax.swing.JButton(origem.getText());
            b.setFont(b.getFont().deriveFont(java.awt.Font.BOLD, 20f));
            b.setPreferredSize(new java.awt.Dimension(260, 120));
            b.setFocusable(false);
            if (origem == btnVenda) {
                Tema.primario(b);
                b.putClientProperty("FlatLaf.style", "background: #F5A524; foreground: #141414; font: bold +8;"
                        + " hoverBackground: #FFB63D; borderWidth: 0; arc: 16");
            } else {
                b.putClientProperty("FlatLaf.style", "background: #1C1C1B; hoverBackground: #262625; font: bold +8; arc: 16");
            }
            b.addActionListener(e -> origem.doClick());
            grade.add(b);
        }
        javax.swing.JPanel topo = new javax.swing.JPanel(new java.awt.BorderLayout());
        topo.setBackground(Tema.FUNDO);
        topo.add(grade, java.awt.BorderLayout.NORTH);
        return topo;
    }

    private void aplicarVisual() {
        Tema.janela(this);
        getContentPane().setBackground(Tema.FUNDO);
        painelMenu.setBackground(Tema.MENU);
        painelMenu.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(0, 0, 0, 1, Tema.BORDA),
                painelMenu.getBorder()));
        for (javax.swing.JPanel p : new javax.swing.JPanel[]{painelNavegacao, painelMarca, painelBotoes, painelRodape}) {
            p.setOpaque(false);
        }
        for (javax.swing.JPanel p : new javax.swing.JPanel[]{painelConteudo, painelCabecalho, painelTitulo, painelAcoes,
            painelCorpo, painelIndicadores, painelGraficos}) {
            p.setBackground(Tema.FUNDO);
        }
        Tema.titulo(lblLogo);
        Tema.secundario(lblSlogan, lblData);
        lblTitulo.setForeground(Tema.TEXTO);

        for (javax.swing.JButton b : new javax.swing.JButton[]{btnVisaoGeral, btnVenda, btnCaixa, btnCozinha,
            btnHistorico, btnProdutos, btnEstoque, btnRelatorio, btnUsuarios, btnSair}) {
            b.putClientProperty("JButton.buttonType", "toolBarButton");
            b.putClientProperty("FlatLaf.style", "margin: 9,12,9,12; font: +1; hoverBackground: #1F1F1E");
            b.setFocusable(false);
        }
        // Item da tela atual
        btnVisaoGeral.putClientProperty("FlatLaf.style",
                "margin: 9,12,9,12; font: +1 bold; background: #2A2413; foreground: #F5A524");
        btnVisaoGeral.putClientProperty("JButton.buttonType", null);
        Tema.primario(btnVenda);
        btnVenda.putClientProperty("JButton.buttonType", null);
        btnVenda.putClientProperty("FlatLaf.style", "margin: 9,12,9,12; font: +1 bold; background: #F5A524;"
                + " foreground: #141414; hoverBackground: #FFB63D; borderWidth: 0; focusWidth: 0");
        btnSair.setForeground(Tema.TEXTO_FRACO);
        lblUsuarioLogado.setForeground(Tema.TEXTO_SECUNDARIO);
        getRootPane().setDefaultButton(null);
    }

    /** Busca os números em segundo plano para a tela não travar. */
    private void carregarDados() {
        btnAtualizar.setEnabled(false);
        LocalDate hoje = LocalDate.now();
        lblData.setText(capitalizar(hoje.format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", BR))));
        new javax.swing.SwingWorker<Object[], Void>() {
            @Override
            protected Object[] doInBackground() throws Exception {
                LocalDate seteDias = hoje.minusDays(6);
                return new Object[]{
                    dashboardDAO.resumo(hoje, hoje),
                    dashboardDAO.resumo(hoje.minusDays(1), hoje.minusDays(1)),
                    dashboardDAO.resumo(seteDias, hoje),
                    dashboardDAO.faturamentoPorDia(seteDias, hoje),
                    dashboardDAO.porFormaPagamento(seteDias, hoje),
                    dashboardDAO.maisVendidos(seteDias, hoje, 5),
                    dashboardDAO.estoqueBaixo()
                };
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void done() {
                btnAtualizar.setEnabled(true);
                try {
                    Object[] r = get();
                    mostrarIndicadores((DashboardDAO.Resumo) r[0], (DashboardDAO.Resumo) r[1], (DashboardDAO.Resumo) r[2],
                            (List<DashboardDAO.Linha>) r[5], (List<Estoque>) r[6]);
                    mostrarFaturamento((List<DashboardDAO.Linha>) r[3], (DashboardDAO.Resumo) r[2]);
                    mostrarPagamentos((Map<FormaPagamento, DashboardDAO.Linha>) r[4]);
                    mostrarMaisVendidos((List<DashboardDAO.Linha>) r[5]);
                    mostrarEstoque((List<Estoque>) r[6]);
                } catch (Exception e) {
                    logger.log(java.util.logging.Level.WARNING, "Falha ao carregar o dashboard", e);
                    javax.swing.JOptionPane.showMessageDialog(Main.this,
                            "Não foi possível carregar os dados do dashboard.\n" + e.getMessage());
                }
            }
        }.execute();
    }

    private static String capitalizar(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private void mostrarIndicadores(DashboardDAO.Resumo hoje, DashboardDAO.Resumo ontem, DashboardDAO.Resumo semana,
            List<DashboardDAO.Linha> maisVendidos, List<Estoque> estoqueBaixo) {
        cardFaturamento.setValor(Tema.reais(hoje.faturamento));
        if (ontem.faturamento > 0) {
            double variacao = (hoje.faturamento - ontem.faturamento) / ontem.faturamento * 100;
            cardFaturamento.setTendencia(variacao >= 0 ? CartaoIndicador.Tendencia.ALTA : CartaoIndicador.Tendencia.BAIXA);
            cardFaturamento.setDetalhe(String.format(BR, "%+.0f%% vs ontem (%s)", variacao, Tema.reais(ontem.faturamento)));
        } else {
            cardFaturamento.setTendencia(CartaoIndicador.Tendencia.NEUTRA);
            cardFaturamento.setDetalhe("Sem vendas ontem");
        }

        cardVendas.setValor(String.valueOf(hoje.vendas));
        cardVendas.setTendencia(CartaoIndicador.Tendencia.NEUTRA);
        cardVendas.setDetalhe("Ontem: " + ontem.vendas + " | 7 dias: " + semana.vendas);

        cardTicket.setValor(Tema.reais(hoje.ticketMedio()));
        cardTicket.setTendencia(CartaoIndicador.Tendencia.NEUTRA);
        cardTicket.setDetalhe("Média 7 dias: " + Tema.reais(semana.ticketMedio()));

        if (maisVendidos.isEmpty()) {
            cardMaisVendido.setValor("-");
            cardMaisVendido.setDetalhe("Sem vendas no período");
        } else {
            DashboardDAO.Linha top = maisVendidos.get(0);
            cardMaisVendido.setValor(top.nome);
            cardMaisVendido.setDetalhe(top.quantidade + " unidades | " + Tema.reais(top.valor));
        }

        cardEstoque.setValor(String.valueOf(estoqueBaixo.size()));
        if (estoqueBaixo.isEmpty()) {
            cardEstoque.setTendencia(CartaoIndicador.Tendencia.NEUTRA);
            cardEstoque.setDetalhe("Todos acima do mínimo");
        } else {
            cardEstoque.setTendencia(CartaoIndicador.Tendencia.AVISO);
            cardEstoque.setDetalhe(estoqueBaixo.size() == 1 ? "item para repor" : "itens para repor");
        }
    }

    private void mostrarFaturamento(List<DashboardDAO.Linha> dias, DashboardDAO.Resumo semana) {
        String[] rotulos = new String[dias.size()];
        double[] valores = new double[dias.size()];
        String[] dicas = new String[dias.size()];
        DateTimeFormatter curto = DateTimeFormatter.ofPattern("EEE dd", BR);
        DateTimeFormatter longo = DateTimeFormatter.ofPattern("EEEE, dd/MM", BR);
        for (int i = 0; i < dias.size(); i++) {
            DashboardDAO.Linha d = dias.get(i);
            LocalDate dia = LocalDate.parse(d.nome);
            rotulos[i] = dia.equals(LocalDate.now()) ? "Hoje" : dia.format(curto).replace(".", "");
            valores[i] = d.valor;
            dicas[i] = capitalizar(dia.format(longo)) + ": " + Tema.reais(d.valor)
                    + " em " + d.quantidade + (d.quantidade == 1 ? " venda" : " vendas");
        }
        graficoFaturamento.setSubtitulo("Últimos 7 dias | total " + Tema.reais(semana.faturamento)
                + " em " + semana.vendas + " vendas");
        graficoFaturamento.setDados(rotulos, valores, dicas);
    }

    private void mostrarPagamentos(Map<FormaPagamento, DashboardDAO.Linha> formas) {
        int n = formas.size();
        String[] rotulos = new String[n];
        double[] valores = new double[n];
        int[] slots = new int[n];
        String[] detalhes = new String[n];
        int i = 0, totalVendas = 0;
        for (Map.Entry<FormaPagamento, DashboardDAO.Linha> e : formas.entrySet()) {
            // A cor segue a forma de pagamento (ordem fixa do enum), não a posição no ranking.
            slots[i] = e.getKey().ordinal();
            rotulos[i] = e.getKey().toString();
            valores[i] = e.getValue().valor;
            detalhes[i] = e.getValue().quantidade + (e.getValue().quantidade == 1 ? " venda | " : " vendas | ")
                    + Tema.reais(e.getValue().valor);
            totalVendas += e.getValue().quantidade;
            i++;
        }
        graficoPagamentos.setCentro(String.valueOf(totalVendas), totalVendas == 1 ? "venda" : "vendas");
        graficoPagamentos.setDados(rotulos, valores, slots, detalhes);
    }

    private void mostrarMaisVendidos(List<DashboardDAO.Linha> produtos) {
        int n = produtos.size();
        String[] rotulos = new String[n];
        double[] valores = new double[n];
        String[] textos = new String[n];
        String[] dicas = new String[n];
        for (int i = 0; i < n; i++) {
            DashboardDAO.Linha p = produtos.get(i);
            rotulos[i] = p.nome;
            valores[i] = p.quantidade;
            textos[i] = p.quantidade + " un.";
            dicas[i] = p.nome + ": " + p.quantidade + " unidades | " + Tema.reais(p.valor);
        }
        graficoProdutos.setDados(rotulos, valores, textos, dicas);
    }

    private void mostrarEstoque(List<Estoque> itens) {
        String[] nomes = new String[itens.size()];
        int[] qtd = new int[itens.size()];
        int[] min = new int[itens.size()];
        for (int i = 0; i < itens.size(); i++) {
            nomes[i] = itens.get(i).getNome();
            qtd[i] = itens.get(i).getQuantidade();
            min[i] = itens.get(i).getMinimo();
        }
        painelAlertas.setDados(nomes, qtd, min);
    }

    private void abrir(javax.swing.JFrame tela) {
        tela.setVisible(true);
        dispose();
    }

    /** Abre a tela só se o perfil tiver acesso (proteção extra além do menu). */
    private void abrirSePuder(Modulo modulo, java.util.function.Supplier<javax.swing.JFrame> tela) {
        if (!Sessao.pode(modulo)) {
            javax.swing.JOptionPane.showMessageDialog(this, "Seu perfil não tem acesso a " + modulo + ".");
            return;
        }
        abrir(tela.get());
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        painelMenu = new javax.swing.JPanel();
        painelNavegacao = new javax.swing.JPanel();
        painelMarca = new javax.swing.JPanel();
        lblLogo = new javax.swing.JLabel();
        lblSlogan = new javax.swing.JLabel();
        painelBotoes = new javax.swing.JPanel();
        btnVisaoGeral = new javax.swing.JButton();
        btnVenda = new javax.swing.JButton();
        btnCaixa = new javax.swing.JButton();
        btnCozinha = new javax.swing.JButton();
        btnHistorico = new javax.swing.JButton();
        btnProdutos = new javax.swing.JButton();
        btnEstoque = new javax.swing.JButton();
        btnRelatorio = new javax.swing.JButton();
        btnUsuarios = new javax.swing.JButton();
        painelRodape = new javax.swing.JPanel();
        lblUsuarioLogado = new javax.swing.JLabel();
        btnSair = new javax.swing.JButton();
        painelConteudo = new javax.swing.JPanel();
        painelCabecalho = new javax.swing.JPanel();
        painelTitulo = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        lblData = new javax.swing.JLabel();
        painelAcoes = new javax.swing.JPanel();
        btnAtualizar = new javax.swing.JButton();
        painelCorpo = new javax.swing.JPanel();
        painelIndicadores = new javax.swing.JPanel();
        cardFaturamento = new ui.CartaoIndicador();
        cardVendas = new ui.CartaoIndicador();
        cardTicket = new ui.CartaoIndicador();
        cardMaisVendido = new ui.CartaoIndicador();
        cardEstoque = new ui.CartaoIndicador();
        painelGraficos = new javax.swing.JPanel();
        graficoFaturamento = new ui.GraficoColunas();
        graficoPagamentos = new ui.GraficoRosca();
        graficoProdutos = new ui.GraficoBarras();
        painelAlertas = new ui.PainelAlertas();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("BLK Burguer");
        setMinimumSize(new java.awt.Dimension(1180, 720));

        painelMenu.setBorder(javax.swing.BorderFactory.createEmptyBorder(22, 16, 20, 16));
        painelMenu.setPreferredSize(new java.awt.Dimension(232, 0));
        painelMenu.setLayout(new java.awt.BorderLayout());
        painelNavegacao.setLayout(new java.awt.BorderLayout(0, 24));
        painelMarca.setLayout(new java.awt.GridLayout(2, 1, 0, 2));
        lblLogo.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        lblLogo.setText("BLK BURGUER");
        painelMarca.add(lblLogo);

        lblSlogan.setText("Gestão da hamburgueria");
        painelMarca.add(lblSlogan);

        painelNavegacao.add(painelMarca, java.awt.BorderLayout.PAGE_START);

        painelBotoes.setLayout(new java.awt.GridLayout(0, 1, 0, 6));
        btnVisaoGeral.setText("Visão geral");
        btnVisaoGeral.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnVisaoGeral.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVisaoGeralActionPerformed(evt);
            }
        });
        painelBotoes.add(btnVisaoGeral);

        btnVenda.setText("Nova venda");
        btnVenda.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnVenda.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVendaActionPerformed(evt);
            }
        });
        painelBotoes.add(btnVenda);

        btnCaixa.setText("Caixa");
        btnCaixa.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnCaixa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCaixaActionPerformed(evt);
            }
        });
        painelBotoes.add(btnCaixa);

        btnCozinha.setText("Cozinha");
        btnCozinha.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnCozinha.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCozinhaActionPerformed(evt);
            }
        });
        painelBotoes.add(btnCozinha);

        btnHistorico.setText("Histórico de vendas");
        btnHistorico.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnHistorico.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHistoricoActionPerformed(evt);
            }
        });
        painelBotoes.add(btnHistorico);

        btnProdutos.setText("Produtos");
        btnProdutos.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnProdutos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnProdutosActionPerformed(evt);
            }
        });
        painelBotoes.add(btnProdutos);

        btnEstoque.setText("Estoque");
        btnEstoque.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnEstoque.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEstoqueActionPerformed(evt);
            }
        });
        painelBotoes.add(btnEstoque);

        btnRelatorio.setText("Relatórios");
        btnRelatorio.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnRelatorio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRelatorioActionPerformed(evt);
            }
        });
        painelBotoes.add(btnRelatorio);

        btnUsuarios.setText("Usuários");
        btnUsuarios.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnUsuarios.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUsuariosActionPerformed(evt);
            }
        });
        painelBotoes.add(btnUsuarios);

        painelNavegacao.add(painelBotoes, java.awt.BorderLayout.CENTER);

        painelMenu.add(painelNavegacao, java.awt.BorderLayout.PAGE_START);

        painelRodape.setLayout(new java.awt.GridLayout(2, 1, 0, 6));
        lblUsuarioLogado.setText("usuário");
        painelRodape.add(lblUsuarioLogado);

        btnSair.setText("Sair / trocar usuário");
        btnSair.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnSair.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSairActionPerformed(evt);
            }
        });
        painelRodape.add(btnSair);

        painelMenu.add(painelRodape, java.awt.BorderLayout.PAGE_END);

        getContentPane().add(painelMenu, java.awt.BorderLayout.LINE_START);

        painelConteudo.setBorder(javax.swing.BorderFactory.createEmptyBorder(22, 24, 24, 24));
        painelConteudo.setLayout(new java.awt.BorderLayout(0, 18));
        painelCabecalho.setLayout(new java.awt.BorderLayout());
        painelTitulo.setLayout(new java.awt.GridLayout(2, 1, 0, 2));
        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblTitulo.setText("Visão geral");
        painelTitulo.add(lblTitulo);

        lblData.setText("hoje");
        painelTitulo.add(lblData);

        painelCabecalho.add(painelTitulo, java.awt.BorderLayout.LINE_START);

        painelAcoes.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 0, 8));
        btnAtualizar.setText("Atualizar");
        btnAtualizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAtualizarActionPerformed(evt);
            }
        });
        painelAcoes.add(btnAtualizar);

        painelCabecalho.add(painelAcoes, java.awt.BorderLayout.LINE_END);

        painelConteudo.add(painelCabecalho, java.awt.BorderLayout.PAGE_START);

        painelCorpo.setLayout(new java.awt.BorderLayout(0, 16));
        painelIndicadores.setLayout(new java.awt.GridLayout(1, 5, 14, 0));
        cardFaturamento.setTitulo("Faturamento hoje");
        cardFaturamento.setPrincipal(true);
        painelIndicadores.add(cardFaturamento);

        cardVendas.setTitulo("Vendas hoje");
        painelIndicadores.add(cardVendas);

        cardTicket.setTitulo("Ticket médio hoje");
        painelIndicadores.add(cardTicket);

        cardMaisVendido.setTitulo("Mais vendido (7 dias)");
        painelIndicadores.add(cardMaisVendido);

        cardEstoque.setTitulo("Estoque baixo");
        painelIndicadores.add(cardEstoque);

        painelCorpo.add(painelIndicadores, java.awt.BorderLayout.PAGE_START);

        painelGraficos.setLayout(new java.awt.GridLayout(2, 2, 16, 16));
        graficoFaturamento.setTitulo("Faturamento por dia");
        graficoFaturamento.setSubtitulo("Últimos 7 dias");
        painelGraficos.add(graficoFaturamento);

        graficoPagamentos.setTitulo("Formas de pagamento");
        graficoPagamentos.setSubtitulo("Últimos 7 dias");
        painelGraficos.add(graficoPagamentos);

        graficoProdutos.setTitulo("Mais vendidos");
        graficoProdutos.setSubtitulo("Últimos 7 dias, por quantidade");
        painelGraficos.add(graficoProdutos);

        painelAlertas.setTitulo("Estoque baixo");
        painelAlertas.setSubtitulo("Itens abaixo da quantidade mínima");
        painelGraficos.add(painelAlertas);

        painelCorpo.add(painelGraficos, java.awt.BorderLayout.CENTER);

        painelConteudo.add(painelCorpo, java.awt.BorderLayout.CENTER);

        getContentPane().add(painelConteudo, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnVisaoGeralActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVisaoGeralActionPerformed
        carregarDados();
    }//GEN-LAST:event_btnVisaoGeralActionPerformed

    private void btnVendaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVendaActionPerformed
        abrirSePuder(Modulo.VENDA_BALCAO, Vendas::new);
    }//GEN-LAST:event_btnVendaActionPerformed

    private void btnHistoricoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHistoricoActionPerformed
        abrirSePuder(Modulo.HISTORICO, TelaHistoricoVendas::new);
    }//GEN-LAST:event_btnHistoricoActionPerformed

    private void btnProdutosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProdutosActionPerformed
        abrirSePuder(Modulo.PRODUTOS, TelaProdutos::new);
    }//GEN-LAST:event_btnProdutosActionPerformed

    private void btnEstoqueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEstoqueActionPerformed
        abrirSePuder(Modulo.ESTOQUE, TelaEstoque::new);
    }//GEN-LAST:event_btnEstoqueActionPerformed

    private void btnRelatorioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRelatorioActionPerformed
        abrirSePuder(Modulo.RELATORIOS, TelaRelatorio::new);
    }//GEN-LAST:event_btnRelatorioActionPerformed

    private void btnSairActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSairActionPerformed
        sair(this);
    }//GEN-LAST:event_btnSairActionPerformed

    private void btnCaixaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCaixaActionPerformed
        abrirSePuder(Modulo.CAIXA, TelaCaixa::new);
    }//GEN-LAST:event_btnCaixaActionPerformed

    private void btnCozinhaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCozinhaActionPerformed
        abrirSePuder(Modulo.COZINHA, TelaCozinha::new);
    }//GEN-LAST:event_btnCozinhaActionPerformed

    private void btnUsuariosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUsuariosActionPerformed
        abrirSePuder(Modulo.USUARIOS, TelaUsuarios::new);
    }//GEN-LAST:event_btnUsuariosActionPerformed

    private void btnAtualizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAtualizarActionPerformed
        carregarDados();
    }//GEN-LAST:event_btnAtualizarActionPerformed

    public static void main(String args[]) {
        Tema.aplicar();
        // Sem login a tela não tem permissões: sempre começa pelo Login.
        java.awt.EventQueue.invokeLater(() -> new Login().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAtualizar;
    private javax.swing.JButton btnCaixa;
    private javax.swing.JButton btnCozinha;
    private javax.swing.JButton btnEstoque;
    private javax.swing.JButton btnHistorico;
    private javax.swing.JButton btnProdutos;
    private javax.swing.JButton btnRelatorio;
    private javax.swing.JButton btnSair;
    private javax.swing.JButton btnUsuarios;
    private javax.swing.JButton btnVenda;
    private javax.swing.JButton btnVisaoGeral;
    private ui.CartaoIndicador cardEstoque;
    private ui.CartaoIndicador cardFaturamento;
    private ui.CartaoIndicador cardMaisVendido;
    private ui.CartaoIndicador cardTicket;
    private ui.CartaoIndicador cardVendas;
    private ui.GraficoColunas graficoFaturamento;
    private ui.GraficoRosca graficoPagamentos;
    private ui.GraficoBarras graficoProdutos;
    private javax.swing.JLabel lblData;
    private javax.swing.JLabel lblLogo;
    private javax.swing.JLabel lblSlogan;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblUsuarioLogado;
    private javax.swing.JPanel painelAcoes;
    private ui.PainelAlertas painelAlertas;
    private javax.swing.JPanel painelBotoes;
    private javax.swing.JPanel painelCabecalho;
    private javax.swing.JPanel painelConteudo;
    private javax.swing.JPanel painelCorpo;
    private javax.swing.JPanel painelGraficos;
    private javax.swing.JPanel painelIndicadores;
    private javax.swing.JPanel painelMarca;
    private javax.swing.JPanel painelMenu;
    private javax.swing.JPanel painelNavegacao;
    private javax.swing.JPanel painelRodape;
    private javax.swing.JPanel painelTitulo;
    // End of variables declaration//GEN-END:variables
}
