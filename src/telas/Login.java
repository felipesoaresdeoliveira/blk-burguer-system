package telas;

import java.awt.Color;
import javax.swing.JLabel;
import ui.Icones;
import ui.Tema;

public class Login extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Login.class.getName());

    private final service.UsuarioService usuarioService = new service.UsuarioService();

    public Login() {
        config.ConexaoBD.aquecer(7);
        initComponents();
        Tema.janela(this);
        setResizable(false);
        estilizar();
        // Enter em qualquer campo aciona o botão Entrar.
        getRootPane().setDefaultButton(btnEntrar);
        setSize(900, 560);
        setLocationRelativeTo(null);
    }

    private void estilizar() {
        getContentPane().setBackground(Tema.FUNDO);
        Tema.transparente(painelMarcaTopo, painelMarcaTexto, painelDestaques, painelEntrar, painelFormulario,
                painelTitulo, painelCampos, painelAcao, painelRodape);

        // Painel da marca: sempre escuro, nos dois temas.
        painelMarca.setBackground(new Color(0x1C1A17));
        lblMarcaIcone.setIcon(new javax.swing.ImageIcon(Tema.icone(56)));
        lblMarca.setForeground(Tema.AMBAR);
        lblMarcaSlogan.setForeground(new Color(0xB8B2A7));
        lblRodapeMarca.setForeground(new Color(0x8A847A));
        destaque(lblDestaque1, "shopping-cart");
        destaque(lblDestaque2, "chef-hat");
        destaque(lblDestaque3, "chart-column");

        Tema.titulo(lblTitulo);
        Tema.secundario(lblSubtitulo);
        lblUsuario.setForeground(Tema.TEXTO_SECUNDARIO);
        lblSenha.setForeground(Tema.TEXTO_SECUNDARIO);
        lblMensagem.setForeground(Tema.PERIGO);
        Tema.primario(btnEntrar);
        btnEntrar.putClientProperty("FlatLaf.style", btnEntrar.getClientProperty("FlatLaf.style") + "; margin: 10,14,10,14");
        btnEntrar.setIcon(Icones.get("log-in", 18, Tema.DESTAQUE_TEXTO));
        campoUsuario.putClientProperty("JTextField.placeholderText", "seu usuário ou e-mail");
        campoUsuario.putClientProperty("JTextField.leadingIcon", Icones.get("user", 16, Tema.TEXTO_FRACO));
        campoSenha.putClientProperty("JTextField.placeholderText", "sua senha");
        campoSenha.putClientProperty("JTextField.leadingIcon", Icones.get("lock", 16, Tema.TEXTO_FRACO));
        campoSenha.putClientProperty("FlatLaf.style", "showRevealButton: true");

        btnTema.setText(Tema.isEscuro() ? "Tema claro" : "Tema escuro");
        btnTema.setIcon(Icones.get(Tema.isEscuro() ? "sun" : "moon", 16));
        btnTema.putClientProperty("JButton.buttonType", "borderless");
        btnTema.setForeground(Tema.TEXTO_SECUNDARIO);
    }

    private static void destaque(JLabel l, String icone) {
        l.setIcon(Icones.get(icone, 20, Tema.AMBAR));
        l.setIconTextGap(12);
        l.setForeground(new Color(0xE8E4DC));
        l.setFont(l.getFont().deriveFont(14f));
    }

    private void mostrarMensagem(String texto) {
        lblMensagem.setText(texto == null ? " " : "<html>" + texto + "</html>");
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        painelMarca = new javax.swing.JPanel();
        painelMarcaTopo = new javax.swing.JPanel();
        lblMarcaIcone = new javax.swing.JLabel();
        painelMarcaTexto = new javax.swing.JPanel();
        lblMarca = new javax.swing.JLabel();
        lblMarcaSlogan = new javax.swing.JLabel();
        painelDestaques = new javax.swing.JPanel();
        lblDestaque1 = new javax.swing.JLabel();
        lblDestaque2 = new javax.swing.JLabel();
        lblDestaque3 = new javax.swing.JLabel();
        lblRodapeMarca = new javax.swing.JLabel();
        painelEntrar = new javax.swing.JPanel();
        painelFormulario = new javax.swing.JPanel();
        painelTitulo = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        lblSubtitulo = new javax.swing.JLabel();
        painelCampos = new javax.swing.JPanel();
        lblUsuario = new javax.swing.JLabel();
        campoUsuario = new javax.swing.JTextField();
        lblSenha = new javax.swing.JLabel();
        campoSenha = new javax.swing.JPasswordField();
        painelAcao = new javax.swing.JPanel();
        lblMensagem = new javax.swing.JLabel();
        btnEntrar = new javax.swing.JButton();
        painelRodape = new javax.swing.JPanel();
        btnTema = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("BLK Burguer - Entrar");

        painelMarca.setBorder(javax.swing.BorderFactory.createEmptyBorder(48, 44, 36, 44));
        painelMarca.setPreferredSize(new java.awt.Dimension(380, 0));
        painelMarca.setLayout(new java.awt.BorderLayout(0, 24));
        painelMarcaTopo.setLayout(new java.awt.BorderLayout(0, 14));
        painelMarcaTopo.add(lblMarcaIcone, java.awt.BorderLayout.PAGE_START);

        painelMarcaTexto.setLayout(new java.awt.GridLayout(2, 1, 0, 2));
        lblMarca.setFont(new java.awt.Font("Segoe UI", 1, 28)); // NOI18N
        lblMarca.setText("BLK BURGUER");
        painelMarcaTexto.add(lblMarca);

        lblMarcaSlogan.setText("Gestão da hamburgueria");
        painelMarcaTexto.add(lblMarcaSlogan);

        painelMarcaTopo.add(painelMarcaTexto, java.awt.BorderLayout.CENTER);

        painelMarca.add(painelMarcaTopo, java.awt.BorderLayout.PAGE_START);

        painelDestaques.setLayout(new java.awt.GridLayout(0, 1, 0, 14));
        lblDestaque1.setText("Balcão, mesas, retirada e delivery");
        painelDestaques.add(lblDestaque1);

        lblDestaque2.setText("Painel da cozinha em tempo real");
        painelDestaques.add(lblDestaque2);

        lblDestaque3.setText("Caixa, estoque e relatórios");
        painelDestaques.add(lblDestaque3);

        painelMarca.add(painelDestaques, java.awt.BorderLayout.CENTER);

        lblRodapeMarca.setText("Cada pessoa entra com a sua conta");
        painelMarca.add(lblRodapeMarca, java.awt.BorderLayout.PAGE_END);

        getContentPane().add(painelMarca, java.awt.BorderLayout.LINE_START);

        painelEntrar.setBorder(javax.swing.BorderFactory.createEmptyBorder(84, 64, 24, 64));
        painelEntrar.setLayout(new java.awt.BorderLayout());
        painelFormulario.setLayout(new java.awt.BorderLayout(0, 22));
        painelTitulo.setLayout(new java.awt.GridLayout(2, 1, 0, 4));
        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 26)); // NOI18N
        lblTitulo.setText("Entrar");
        painelTitulo.add(lblTitulo);

        lblSubtitulo.setText("Use a conta criada pelo administrador");
        painelTitulo.add(lblSubtitulo);

        painelFormulario.add(painelTitulo, java.awt.BorderLayout.PAGE_START);

        painelCampos.setLayout(new java.awt.GridLayout(0, 1, 0, 6));
        lblUsuario.setText("Usuário");
        painelCampos.add(lblUsuario);

        painelCampos.add(campoUsuario);

        lblSenha.setText("Senha");
        painelCampos.add(lblSenha);

        painelCampos.add(campoSenha);

        painelFormulario.add(painelCampos, java.awt.BorderLayout.CENTER);

        painelAcao.setLayout(new java.awt.BorderLayout(0, 8));
        lblMensagem.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblMensagem.setText(" ");
        painelAcao.add(lblMensagem, java.awt.BorderLayout.PAGE_START);

        btnEntrar.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        btnEntrar.setText("Entrar");
        btnEntrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEntrarActionPerformed(evt);
            }
        });
        painelAcao.add(btnEntrar, java.awt.BorderLayout.CENTER);

        painelFormulario.add(painelAcao, java.awt.BorderLayout.PAGE_END);

        painelEntrar.add(painelFormulario, java.awt.BorderLayout.PAGE_START);

        painelRodape.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 0, 0));
        btnTema.setText("Tema claro");
        btnTema.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTemaActionPerformed(evt);
            }
        });
        painelRodape.add(btnTema);

        painelEntrar.add(painelRodape, java.awt.BorderLayout.PAGE_END);

        getContentPane().add(painelEntrar, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnEntrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEntrarActionPerformed
        String usuario = campoUsuario.getText().trim();
        String senha = new String(campoSenha.getPassword());
        if (usuario.isEmpty() || senha.isEmpty()) {
            mostrarMensagem("Informe usuário e senha.");
            return;
        }

        btnEntrar.setEnabled(false);
        btnEntrar.setText("Entrando...");
        mostrarMensagem(null);
        // Consulta fora da thread da interface para a tela não travar se o banco demorar.
        new javax.swing.SwingWorker<entidades.Usuario, Void>() {
            @Override
            protected entidades.Usuario doInBackground() throws Exception {
                return usuarioService.autenticar(usuario, senha);
            }

            @Override
            protected void done() {
                btnEntrar.setEnabled(true);
                btnEntrar.setText("Entrar");
                try {
                    entidades.Usuario logado = get();
                    if (logado != null) {
                        service.Sessao.iniciar(logado);
                        new Thread(() -> service.Auditoria.registrar(entidades.AcaoAuditoria.LOGIN, logado.getUsuario(),
                                "Perfil " + logado.getPerfil())).start();
                        Main.abrirInicio();
                        dispose();
                    } else {
                        mostrarMensagem("Usuário ou senha incorretos.");
                        campoSenha.setText("");
                        campoSenha.requestFocusInWindow();
                    }
                } catch (Exception e) {
                    logger.log(java.util.logging.Level.WARNING, "Falha ao acessar o banco", e);
                    mostrarMensagem("Banco de dados indisponível. Verifique o PostgreSQL e o db.properties.");
                }
            }
        }.execute();
    }//GEN-LAST:event_btnEntrarActionPerformed

    private void btnTemaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTemaActionPerformed
        String usuario = campoUsuario.getText();
        Tema.alternar();
        Login novo = new Login();
        novo.campoUsuario.setText(usuario);
        novo.setLocation(getLocation());
        dispose();
        novo.setVisible(true);
    }//GEN-LAST:event_btnTemaActionPerformed

    public static void main(String args[]) {
        Tema.aplicar();

        java.awt.EventQueue.invokeLater(() -> new Login().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnEntrar;
    private javax.swing.JButton btnTema;
    private javax.swing.JPasswordField campoSenha;
    private javax.swing.JTextField campoUsuario;
    private javax.swing.JLabel lblDestaque1;
    private javax.swing.JLabel lblDestaque2;
    private javax.swing.JLabel lblDestaque3;
    private javax.swing.JLabel lblMarca;
    private javax.swing.JLabel lblMarcaIcone;
    private javax.swing.JLabel lblMarcaSlogan;
    private javax.swing.JLabel lblMensagem;
    private javax.swing.JLabel lblRodapeMarca;
    private javax.swing.JLabel lblSenha;
    private javax.swing.JLabel lblSubtitulo;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblUsuario;
    private javax.swing.JPanel painelAcao;
    private javax.swing.JPanel painelCampos;
    private javax.swing.JPanel painelDestaques;
    private javax.swing.JPanel painelEntrar;
    private javax.swing.JPanel painelFormulario;
    private javax.swing.JPanel painelMarca;
    private javax.swing.JPanel painelMarcaTexto;
    private javax.swing.JPanel painelMarcaTopo;
    private javax.swing.JPanel painelRodape;
    private javax.swing.JPanel painelTitulo;
    // End of variables declaration//GEN-END:variables
}
