package impressao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Documento para impressora comum ou térmica (#38), montado em colunas de
 * largura fixa (fonte monoespaçada). Cada linha já sai do tamanho certo:
 * textos longos quebram por palavra e valores ficam sempre alinhados à
 * direita, nunca partidos no meio.
 */
public class Documento {

    /** Colunas da bobina de 80 mm e de 58 mm em fonte normal. */
    public static final int COLUNAS_80MM = 42;
    public static final int COLUNAS_58MM = 32;

    /** Fator de tamanho das linhas grandes (títulos, mesa/senha na cozinha). */
    public static final float FATOR_GRANDE = 1.6f;

    public enum Estilo {
        NORMAL, NEGRITO, GRANDE, FRACO
    }

    /** Uma linha pronta para desenhar. */
    public static final class Linha {
        public final String texto;
        public final Estilo estilo;

        Linha(String texto, Estilo estilo) {
            this.texto = texto;
            this.estilo = estilo;
        }
    }

    private final String titulo;
    private final int colunas;
    private final List<Linha> linhas = new ArrayList<>();

    public Documento(String titulo, int colunas) {
        this.titulo = titulo;
        this.colunas = colunas;
    }

    public String getTitulo() {
        return titulo;
    }

    public int getColunas() {
        return colunas;
    }

    public List<Linha> getLinhas() {
        return Collections.unmodifiableList(linhas);
    }

    /** Colunas disponíveis para o estilo (linhas grandes cabem menos letras). */
    public int colunas(Estilo estilo) {
        return estilo == Estilo.GRANDE ? (int) Math.floor(colunas / FATOR_GRANDE) : colunas;
    }

    // ------------------------------------------------------------ montagem

    /** Texto alinhado à esquerda, quebrando por palavra. */
    public Documento texto(String texto) {
        return texto(texto, Estilo.NORMAL);
    }

    public Documento texto(String texto, Estilo estilo) {
        for (String parte : quebrar(texto, colunas(estilo))) {
            linhas.add(new Linha(parte, estilo));
        }
        return this;
    }

    /** Texto recuado (continuações, observações de item). */
    public Documento recuado(String texto, int recuo, Estilo estilo) {
        String margem = repetir(' ', recuo);
        for (String parte : quebrar(texto, colunas(estilo) - recuo)) {
            linhas.add(new Linha(margem + parte, estilo));
        }
        return this;
    }

    /** Texto centralizado, quebrando por palavra. */
    public Documento centro(String texto, Estilo estilo) {
        int largura = colunas(estilo);
        for (String parte : quebrar(texto, largura)) {
            int sobra = largura - parte.length();
            linhas.add(new Linha(repetir(' ', sobra / 2) + parte, estilo));
        }
        return this;
    }

    /**
     * Rótulo à esquerda e valor à direita na mesma linha. Se o rótulo não
     * couber ao lado do valor, ele quebra e o valor fica na última linha.
     */
    public Documento par(String rotulo, String valor, Estilo estilo) {
        int largura = colunas(estilo);
        int espacoRotulo = Math.max(4, largura - valor.length() - 1);
        List<String> partes = quebrar(rotulo, espacoRotulo);
        for (int i = 0; i < partes.size() - 1; i++) {
            linhas.add(new Linha(partes.get(i), estilo));
        }
        String ultima = partes.get(partes.size() - 1);
        linhas.add(new Linha(ultima + repetir(' ', Math.max(1, largura - ultima.length() - valor.length())) + valor, estilo));
        return this;
    }

    public Documento par(String rotulo, String valor) {
        return par(rotulo, valor, Estilo.NORMAL);
    }

    /**
     * Item de venda em duas colunas fixas: "2x Nome do produto ... R$ 69,80".
     * O nome quebra com recuo, alinhado depois da quantidade.
     */
    public Documento item(int quantidade, String nome, String valor) {
        String qtd = quantidade + "x ";
        int espacoNome = Math.max(6, colunas - qtd.length() - valor.length() - 1);
        List<String> partes = quebrar(nome, espacoNome);
        for (int i = 0; i < partes.size(); i++) {
            String inicio = (i == 0 ? qtd : repetir(' ', qtd.length())) + partes.get(i);
            if (i == partes.size() - 1) {
                linhas.add(new Linha(inicio + repetir(' ', Math.max(1, colunas - inicio.length() - valor.length())) + valor,
                        Estilo.NORMAL));
            } else {
                linhas.add(new Linha(inicio, Estilo.NORMAL));
            }
        }
        return this;
    }

    public Documento separador() {
        linhas.add(new Linha(repetir('-', colunas), Estilo.FRACO));
        return this;
    }

    public Documento separadorDuplo() {
        linhas.add(new Linha(repetir('=', colunas), Estilo.NORMAL));
        return this;
    }

    public Documento espaco() {
        linhas.add(new Linha("", Estilo.NORMAL));
        return this;
    }

    // ------------------------------------------------------------ utilitários

    /** Quebra por palavra; palavras maiores que a largura são partidas. */
    static List<String> quebrar(String texto, int largura) {
        List<String> partes = new ArrayList<>();
        if (texto == null || texto.isEmpty()) {
            partes.add("");
            return partes;
        }
        largura = Math.max(1, largura);
        StringBuilder atual = new StringBuilder();
        for (String palavra : texto.trim().split("\\s+")) {
            while (palavra.length() > largura) {
                if (atual.length() > 0) {
                    partes.add(atual.toString());
                    atual.setLength(0);
                }
                partes.add(palavra.substring(0, largura));
                palavra = palavra.substring(largura);
            }
            if (atual.length() == 0) {
                atual.append(palavra);
            } else if (atual.length() + 1 + palavra.length() <= largura) {
                atual.append(' ').append(palavra);
            } else {
                partes.add(atual.toString());
                atual.setLength(0);
                atual.append(palavra);
            }
        }
        if (atual.length() > 0 || partes.isEmpty()) {
            partes.add(atual.toString());
        }
        return partes;
    }

    static String repetir(char c, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(c);
        }
        return sb.toString();
    }

    /** Texto puro (para conferência e testes). */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Linha l : linhas) {
            sb.append(l.texto).append('\n');
        }
        return sb.toString();
    }
}
