package telas;

import entidades.Modulo;
import entidades.Perfil;
import entidades.Usuario;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import service.Sessao;
import service.UsuarioService;
import ui.Tema;

/** Contas da equipe (somente administrador): criar, editar perfil, ativar e redefinir senha. */
public class TelaUsuarios extends javax.swing.JFrame {

    private final UsuarioService usuarioService = new UsuarioService();
    private List<Usuario> usuarios = new ArrayList<>();
    /** Conta em edição, ou null para uma nova. */
    private Usuario selecionado;

    public TelaUsuarios() {
        initComponents();
        Tema.janela(this);
        Tema.titulo(lblTitulo);
        Tema.secundario(lblSubtitulo, lblPerfilInfo);
        Tema.primario(btnSalvar);
        lblFormTitulo.setForeground(Tema.TEXTO);
        getContentPane().setBackground(Tema.FUNDO);
        painelFormulario.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(Tema.BORDA),
                javax.swing.BorderFactory.createEmptyBorder(16, 18, 18, 18)));
        painelFormulario.setBackground(Tema.CARTAO);
        for (javax.swing.JPanel p : new javax.swing.JPanel[]{painelCampos, painelBotoes}) {
            p.setOpaque(false);
        }
        chkAtivo.setOpaque(false);
        lblPerfilInfo.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lblPerfilInfo.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 0, 0, 0));
        for (Perfil p : Perfil.values()) {
            cmbPerfil.addItem(p);
        }
        cmbPerfil.addActionListener(e -> mostrarPerfil());
        txtLogin.putClientProperty("JTextField.placeholderText", "ex.: cozinha, caixa1, maria");
        txtNome.putClientProperty("JTextField.placeholderText", "ex.: TV da cozinha, Maria (caixa)");
        tabelaUsuarios.getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tabelaUsuarios.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabelaUsuarios.getSelectedRow() >= 0) {
                editar(usuarios.get(tabelaUsuarios.getSelectedRow()));
            }
        });
        carregar();
        novo();
        setSize(1180, 700);
        setLocationRelativeTo(null);
    }

    private void carregar() {
        javax.swing.table.DefaultTableModel modelo = new javax.swing.table.DefaultTableModel(
                new String[]{"Nome", "Login", "Perfil", "Situação"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        try {
            usuarios = usuarioService.listar();
            for (Usuario u : usuarios) {
                boolean eu = Sessao.usuario() != null && Sessao.usuario().getId() == u.getId();
                modelo.addRow(new Object[]{u.getNome() + (eu ? "  (você)" : ""), u.getUsuario(), u.getPerfil(),
                    u.isAtivo() ? "Ativa" : "Desativada"});
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage());
        }
        tabelaUsuarios.setModel(modelo);
    }

    private void mostrarPerfil() {
        Perfil p = (Perfil) cmbPerfil.getSelectedItem();
        if (p == null) {
            return;
        }
        StringBuilder modulos = new StringBuilder();
        for (Modulo m : p.getModulos()) {
            if (m == Modulo.CANCELAMENTOS) {
                continue;
            }
            if (modulos.length() > 0) {
                modulos.append(", ");
            }
            modulos.append(m);
        }
        lblPerfilInfo.setText("<html><div style='width:320px'><b>" + p.resumo() + "</b><br>Acessa: " + modulos + "</div></html>");
    }

    private void novo() {
        selecionado = null;
        tabelaUsuarios.clearSelection();
        lblFormTitulo.setText("Nova conta");
        txtNome.setText("");
        txtLogin.setText("");
        cmbPerfil.setSelectedItem(Perfil.CAIXA);
        txtSenha.setText("");
        txtConfirmar.setText("");
        chkAtivo.setSelected(true);
        txtSenha.setEnabled(true);
        txtConfirmar.setEnabled(true);
        btnSenha.setEnabled(false);
        mostrarPerfil();
        txtNome.requestFocusInWindow();
    }

    private void editar(Usuario u) {
        selecionado = u;
        lblFormTitulo.setText("Editar: " + u.getUsuario());
        txtNome.setText(u.getNome().equals(u.getUsuario()) ? "" : u.getNome());
        txtLogin.setText(u.getUsuario());
        cmbPerfil.setSelectedItem(u.getPerfil());
        chkAtivo.setSelected(u.isAtivo());
        txtSenha.setText("");
        txtConfirmar.setText("");
        txtSenha.setEnabled(false);
        txtConfirmar.setEnabled(false);
        btnSenha.setEnabled(true);
        mostrarPerfil();
    }

    private void salvar() {
        Usuario u = selecionado != null ? selecionado : new Usuario();
        u.setNome(txtNome.getText().trim());
        u.setUsuario(txtLogin.getText().trim());
        u.setPerfil((Perfil) cmbPerfil.getSelectedItem());
        u.setAtivo(chkAtivo.isSelected());
        try {
            if (selecionado == null) {
                String senha = new String(txtSenha.getPassword());
                if (!senha.equals(new String(txtConfirmar.getPassword()))) {
                    JOptionPane.showMessageDialog(this, "As senhas não conferem.", "Usuários", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                usuarioService.criar(u, senha);
                JOptionPane.showMessageDialog(this, "Conta \"" + u.getUsuario() + "\" criada como " + u.getPerfil() + ".");
            } else {
                usuarioService.atualizar(u);
                JOptionPane.showMessageDialog(this, "Conta atualizada.");
            }
            carregar();
            novo();
        } catch (IllegalArgumentException | SecurityException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Usuários", JOptionPane.WARNING_MESSAGE);
        } catch (java.sql.SQLException e) {
            String msg = "23505".equals(e.getSQLState()) ? "Já existe uma conta com o login \"" + u.getUsuario() + "\"." : "Erro: " + e.getMessage();
            JOptionPane.showMessageDialog(this, msg, "Usuários", JOptionPane.WARNING_MESSAGE);
            if (selecionado != null) {
                carregar();
            }
        }
    }

    private void redefinirSenha() {
        if (selecionado == null) {
            return;
        }
        javax.swing.JPasswordField nova = new javax.swing.JPasswordField(18);
        javax.swing.JPasswordField confirma = new javax.swing.JPasswordField(18);
        javax.swing.JPanel p = new javax.swing.JPanel(new java.awt.GridLayout(0, 1, 0, 6));
        p.add(new javax.swing.JLabel("Nova senha para " + selecionado.getUsuario() + ":"));
        p.add(nova);
        p.add(new javax.swing.JLabel("Confirmar nova senha:"));
        p.add(confirma);
        if (JOptionPane.showConfirmDialog(this, p, "Redefinir senha", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        String senha = new String(nova.getPassword());
        if (!senha.equals(new String(confirma.getPassword()))) {
            JOptionPane.showMessageDialog(this, "As senhas não conferem.", "Usuários", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            usuarioService.redefinirSenha(selecionado.getId(), senha);
            JOptionPane.showMessageDialog(this, "Senha redefinida.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Usuários", JOptionPane.WARNING_MESSAGE);
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        painelCabecalho = new javax.swing.JPanel();
        painelTitulo = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        lblSubtitulo = new javax.swing.JLabel();
        painelTopoAcoes = new javax.swing.JPanel();
        btnVoltar = new javax.swing.JButton();
        painelCorpo = new javax.swing.JPanel();
        scrollUsuarios = new javax.swing.JScrollPane();
        tabelaUsuarios = new javax.swing.JTable();
        painelFormulario = new javax.swing.JPanel();
        painelCampos = new javax.swing.JPanel();
        lblFormTitulo = new javax.swing.JLabel();
        lblNome = new javax.swing.JLabel();
        txtNome = new javax.swing.JTextField();
        lblLogin = new javax.swing.JLabel();
        txtLogin = new javax.swing.JTextField();
        lblPerfil = new javax.swing.JLabel();
        cmbPerfil = new javax.swing.JComboBox<>();
        lblSenha = new javax.swing.JLabel();
        txtSenha = new javax.swing.JPasswordField();
        lblConfirmar = new javax.swing.JLabel();
        txtConfirmar = new javax.swing.JPasswordField();
        chkAtivo = new javax.swing.JCheckBox();
        lblPerfilInfo = new javax.swing.JLabel();
        painelBotoes = new javax.swing.JPanel();
        btnNovo = new javax.swing.JButton();
        btnSalvar = new javax.swing.JButton();
        btnSenha = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("BLK Burguer - Usuários");
        setMinimumSize(new java.awt.Dimension(1000, 640));

        painelCabecalho.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 24, 14, 24));
        painelCabecalho.setLayout(new java.awt.BorderLayout());
        painelTitulo.setLayout(new java.awt.GridLayout(2, 1, 0, 2));
        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblTitulo.setText("Usuários e acessos");
        painelTitulo.add(lblTitulo);

        lblSubtitulo.setText("Crie as contas da equipe e defina o que cada perfil pode acessar");
        painelTitulo.add(lblSubtitulo);

        painelCabecalho.add(painelTitulo, java.awt.BorderLayout.LINE_START);

        painelTopoAcoes.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 0, 8));
        btnVoltar.setText("Voltar");
        btnVoltar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVoltarActionPerformed(evt);
            }
        });
        painelTopoAcoes.add(btnVoltar);

        painelCabecalho.add(painelTopoAcoes, java.awt.BorderLayout.LINE_END);

        getContentPane().add(painelCabecalho, java.awt.BorderLayout.PAGE_START);

        painelCorpo.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 24, 24, 24));
        painelCorpo.setLayout(new java.awt.BorderLayout(20, 0));
        scrollUsuarios.setViewportView(tabelaUsuarios);
        painelCorpo.add(scrollUsuarios, java.awt.BorderLayout.CENTER);

        painelFormulario.setPreferredSize(new java.awt.Dimension(380, 0));
        painelFormulario.setLayout(new java.awt.BorderLayout(0, 14));
        painelCampos.setLayout(new java.awt.GridLayout(0, 1, 0, 4));
        lblFormTitulo.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblFormTitulo.setText("Nova conta");
        painelCampos.add(lblFormTitulo);

        lblNome.setText("Nome");
        painelCampos.add(lblNome);

        painelCampos.add(txtNome);

        lblLogin.setText("Login (usuário para entrar)");
        painelCampos.add(lblLogin);

        painelCampos.add(txtLogin);

        lblPerfil.setText("Perfil");
        painelCampos.add(lblPerfil);

        painelCampos.add(cmbPerfil);

        lblSenha.setText("Senha (mínimo 6 caracteres)");
        painelCampos.add(lblSenha);

        painelCampos.add(txtSenha);

        lblConfirmar.setText("Confirmar senha");
        painelCampos.add(lblConfirmar);

        painelCampos.add(txtConfirmar);

        chkAtivo.setSelected(true);
        chkAtivo.setText("Conta ativa");
        painelCampos.add(chkAtivo);

        painelFormulario.add(painelCampos, java.awt.BorderLayout.PAGE_START);

        lblPerfilInfo.setText(" ");
        painelFormulario.add(lblPerfilInfo, java.awt.BorderLayout.CENTER);

        painelBotoes.setLayout(new java.awt.GridLayout(2, 2, 8, 8));
        btnNovo.setText("Nova conta");
        btnNovo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNovoActionPerformed(evt);
            }
        });
        painelBotoes.add(btnNovo);

        btnSalvar.setText("Salvar");
        btnSalvar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSalvarActionPerformed(evt);
            }
        });
        painelBotoes.add(btnSalvar);

        btnSenha.setText("Redefinir senha");
        btnSenha.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSenhaActionPerformed(evt);
            }
        });
        painelBotoes.add(btnSenha);

        btnCancelar.setText("Cancelar");
        btnCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelarActionPerformed(evt);
            }
        });
        painelBotoes.add(btnCancelar);

        painelFormulario.add(painelBotoes, java.awt.BorderLayout.PAGE_END);

        painelCorpo.add(painelFormulario, java.awt.BorderLayout.LINE_END);

        getContentPane().add(painelCorpo, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnVoltarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVoltarActionPerformed
        Main.voltar(this);
    }//GEN-LAST:event_btnVoltarActionPerformed

    private void btnNovoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNovoActionPerformed
        novo();
    }//GEN-LAST:event_btnNovoActionPerformed

    private void btnSalvarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSalvarActionPerformed
        salvar();
    }//GEN-LAST:event_btnSalvarActionPerformed

    private void btnSenhaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSenhaActionPerformed
        redefinirSenha();
    }//GEN-LAST:event_btnSenhaActionPerformed

    private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
        novo();
    }//GEN-LAST:event_btnCancelarActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnNovo;
    private javax.swing.JButton btnSalvar;
    private javax.swing.JButton btnSenha;
    private javax.swing.JButton btnVoltar;
    private javax.swing.JCheckBox chkAtivo;
    private javax.swing.JComboBox<Object> cmbPerfil;
    private javax.swing.JLabel lblConfirmar;
    private javax.swing.JLabel lblFormTitulo;
    private javax.swing.JLabel lblLogin;
    private javax.swing.JLabel lblNome;
    private javax.swing.JLabel lblPerfil;
    private javax.swing.JLabel lblPerfilInfo;
    private javax.swing.JLabel lblSenha;
    private javax.swing.JLabel lblSubtitulo;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel painelBotoes;
    private javax.swing.JPanel painelCabecalho;
    private javax.swing.JPanel painelCampos;
    private javax.swing.JPanel painelCorpo;
    private javax.swing.JPanel painelFormulario;
    private javax.swing.JPanel painelTitulo;
    private javax.swing.JPanel painelTopoAcoes;
    private javax.swing.JScrollPane scrollUsuarios;
    private javax.swing.JTable tabelaUsuarios;
    private javax.swing.JPasswordField txtConfirmar;
    private javax.swing.JTextField txtLogin;
    private javax.swing.JTextField txtNome;
    private javax.swing.JPasswordField txtSenha;
    // End of variables declaration//GEN-END:variables
}
