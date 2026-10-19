package telas;

import java.awt.BorderLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import ui.Tema;

/**
 * Janela da TV da cozinha: o painel de produção em tela cheia, sem bordas.
 * F11 alterna tela cheia/janela; Esc sai da tela cheia. O perfil Cozinha
 * entra direto aqui; os demais podem abrir pelo módulo Cozinha.
 */
public class CozinhaTV extends JFrame {

    private final TelaCozinha painel = new TelaCozinha();
    private boolean telaCheia;

    private CozinhaTV() {
        super("BLK Burguer - Cozinha");
        Tema.janela(this);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(painel, BorderLayout.CENTER);
        painel.modoTV(true);
        ui.Acabamento.aplicar(painel, false);
        getRootPane().registerKeyboardAction(e -> alternar(), KeyStroke.getKeyStroke("F11"),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().registerKeyboardAction(e -> {
            if (telaCheia) {
                alternar();
            }
        }, KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                painel.aoSair();
            }
        });
    }

    /** Abre a TV em tela cheia. */
    public static CozinhaTV abrir() {
        CozinhaTV tv = new CozinhaTV();
        tv.alternar();
        return tv;
    }

    private void alternar() {
        telaCheia = !telaCheia;
        dispose();
        setUndecorated(telaCheia);
        if (telaCheia) {
            setExtendedState(MAXIMIZED_BOTH);
        } else {
            setExtendedState(NORMAL);
            Tema.tamanhoPadrao(this);
        }
        setVisible(true);
    }
}
