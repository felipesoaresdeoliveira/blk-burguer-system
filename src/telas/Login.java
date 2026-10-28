package telas;

import ui.Icones;
import ui.Tema;

/** Tela de login: um cartão no centro com usuário, senha e o botão Entrar. */
public class Login extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Login.class.getName());

    private final service.UsuarioService usuarioService = new service.UsuarioService();

    public Login() {
        // Abre as conexões com o banco enquanto a pessoa digita (deixa o login mais rápido).
        config.ConexaoBD.aquecer(7);
        initComponents();
        Tema.janela(this);
        estilizar();
        getRootPane().setDefaultButton(btnEntrar); // Enter aciona o botão Entrar
        setResizable(false);
        setLocationRelativeTo(null);
    }

    /** Cores e ícones do tema (os textos e o layout estão no Login.form). */
    private void estilizar() {
        getContentPane().setBackground(Tema.FUNDO);
        Tema.transparente(painelFundo, painelMarca, painelTitulo, painelCampos, painelAcao);
        Tema.cartao(painelCartao);

        lblLogo.setIcon(new javax.swing.ImageIcon(Tema.icone(56)));
        lblTitulo.setForeground(Tema.DESTAQUE);
        Tema.secundario(lblSubtitulo);
        lblUsuario.setForeground(Tema.TEXTO_SECUNDARIO);
        lblSenha.setForeground(Tema.TEXTO_SECUNDARIO);
        lblMensagem.setForeground(Tema.PERIGO);

        campoUsuario.putClientProperty("JTextField.placeholderText", "seu usuário");
        campoUsuario.putClientProperty("JTextField.leadingIcon", Icones.get("user", 16, Tema.TEXTO_FRACO));
        campoSenha.putClientProperty("JTextField.placeholderText", "sua senha");
        campoSenha.putClientProperty("JTextField.leadingIcon", Icones.get("lock", 16, Tema.TEXTO_FRACO));
        campoSenha.putClientProperty("FlatLaf.style", "showRevealButton: true");

        Tema.primario(btnEntrar);
        btnEntrar.setIcon(Icones.get("log-in", 18, Tema.DESTAQUE_TEXTO));

        btnTema.setText(Tema.isEscuro() ? "Tema claro" : "Tema escuro");
        btnTema.setIcon(Icones.get(Tema.isEscuro() ? "sun" : "moon", 16));
        btnTema.putClientProperty("JButton.buttonType", "borderless");
        btnTema.setForeground(Tema.TEXTO_SECUNDARIO);
    }

    private void mostrarMensagem(String texto) {
        lblMensagem.setText(texto == null ? " " : "<html><div style='text-align:center'>" + texto + "</div></html>");
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        painelFundo = new javax.swing.JPanel();
        painelCartao = new javax.swing.JPanel();
        painelMarca = new javax.swing.JPanel();
        lblLogo = new javax.swing.JLabel();
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
        btnTema = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("BLK Burguer - Entrar");

        painelFundo.setBorder(javax.swing.BorderFactory.createEmptyBorder(28, 28, 28, 28));
        painelFundo.setLayout(new java.awt.BorderLayout(0, 0));
        painelCartao.setBorder(javax.swing.BorderFactory.createEmptyBorder(36, 40, 24, 40));
        painelCartao.setPreferredSize(new java.awt.Dimension(400, 470));
        painelCartao.setLayout(new java.awt.BorderLayout(0, 20));
        painelMarca.setLayout(new java.awt.BorderLayout(0, 10));
        lblLogo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        painelMarca.add(lblLogo, java.awt.BorderLayout.PAGE_START);

        painelTitulo.setLayout(new java.awt.GridLayout(2, 1, 0, 2));
        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 26)); // NOI18N
        lblTitulo.setText("BLK BURGUER");
        lblTitulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        painelTitulo.add(lblTitulo);

        lblSubtitulo.setText("Gestão da hamburgueria");
        lblSubtitulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        painelTitulo.add(lblSubtitulo);

        painelMarca.add(painelTitulo, java.awt.BorderLayout.CENTER);

        painelCartao.add(painelMarca, java.awt.BorderLayout.PAGE_START);

        painelCampos.setLayout(new java.awt.GridLayout(0, 1, 0, 6));
        lblUsuario.setText("Usuário");
        painelCampos.add(lblUsuario);

        painelCampos.add(campoUsuario);

        lblSenha.setText("Senha");
        painelCampos.add(lblSenha);

        painelCampos.add(campoSenha);

        painelCartao.add(painelCampos, java.awt.BorderLayout.CENTER);

        painelAcao.setLayout(new java.awt.BorderLayout(0, 8));
        lblMensagem.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblMensagem.setText(" ");
        lblMensagem.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        painelAcao.add(lblMensagem, java.awt.BorderLayout.PAGE_START);

        btnEntrar.setFont(new java.awt.Font("Segoe UI", 1, 15)); // NOI18N
        btnEntrar.setText("Entrar");
        btnEntrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEntrarActionPerformed(evt);
            }
        });
        painelAcao.add(btnEntrar, java.awt.BorderLayout.CENTER);

        btnTema.setText("Tema claro");
        btnTema.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTemaActionPerformed(evt);
            }
        });
        painelAcao.add(btnTema, java.awt.BorderLayout.PAGE_END);

        painelCartao.add(painelAcao, java.awt.BorderLayout.PAGE_END);

        painelFundo.add(painelCartao, java.awt.BorderLayout.CENTER);

        getContentPane().add(painelFundo, java.awt.BorderLayout.CENTER);

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
    private javax.swing.JLabel lblLogo;
    private javax.swing.JLabel lblMensagem;
    private javax.swing.JLabel lblSenha;
    private javax.swing.JLabel lblSubtitulo;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblUsuario;
    private javax.swing.JPanel painelAcao;
    private javax.swing.JPanel painelCampos;
    private javax.swing.JPanel painelCartao;
    private javax.swing.JPanel painelFundo;
    private javax.swing.JPanel painelMarca;
    private javax.swing.JPanel painelTitulo;
    // End of variables declaration//GEN-END:variables
}
