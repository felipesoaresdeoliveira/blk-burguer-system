package telas;

/**
 * Tela exibida dentro da janela única ({@link Main}). A janela chama
 * {@link #aoSair()} antes de trocar de tela, para parar atualizações
 * automáticas (timers) e liberar recursos.
 *
 * <p>Para definir o botão acionado pelo Enter, a tela usa
 * {@code putClientProperty("BLK.botaoPadrao", botao)}.</p>
 */
public interface Navegavel {

    /** Chamado quando a tela deixa de ser exibida. */
    default void aoSair() {
    }
}
