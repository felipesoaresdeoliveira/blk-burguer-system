package telas;

import entidades.Adicional;
import entidades.Produto;
import entidades.Venda;
import java.util.Map;

/** Janela de personalização de lanche, usada no balcão e na comanda. */
public final class DialogoPersonalizar {

    private DialogoPersonalizar() {
    }

    /**
     * Personalização do lanche: marcar ingredientes para tirar, escolher
     * adicionais pagos e escrever uma observação. Retorna false se cancelado.
     */
    public static boolean mostrar(java.awt.Component pai, Produto produto, Venda item,
            Map<String, Map<String, Integer>> fichas, Map<String, Adicional> adicionais) {
        javax.swing.JPanel painel = new javax.swing.JPanel(new java.awt.BorderLayout(0, 12));
        javax.swing.JLabel titulo = new javax.swing.JLabel(item.getQuantidade() + "x " + produto.getNome());
        titulo.setFont(titulo.getFont().deriveFont(java.awt.Font.BOLD, 18f));
        titulo.setForeground(ui.Tema.DESTAQUE);
        painel.add(titulo, java.awt.BorderLayout.NORTH);

        javax.swing.JPanel colunas = new javax.swing.JPanel(new java.awt.GridLayout(1, 2, 24, 0));
        java.util.List<javax.swing.JCheckBox> tirar = new java.util.ArrayList<>();
        javax.swing.JPanel pTirar = new javax.swing.JPanel(new java.awt.GridLayout(0, 1, 0, 2));
        pTirar.add(cabecalho("Tirar do lanche"));
        Map<String, Integer> ficha = fichas.getOrDefault(produto.getNome(), java.util.Collections.emptyMap());
        for (String ing : ficha.keySet()) {
            javax.swing.JCheckBox c = new javax.swing.JCheckBox("Sem " + ing);
            c.setName(ing);
            tirar.add(c);
            pTirar.add(c);
        }
        if (ficha.isEmpty()) {
            pTirar.add(new javax.swing.JLabel("Sem ficha técnica"));
        }
        java.util.List<javax.swing.JCheckBox> extras = new java.util.ArrayList<>();
        javax.swing.JPanel pExtras = new javax.swing.JPanel(new java.awt.GridLayout(0, 1, 0, 2));
        pExtras.add(cabecalho("Adicionais"));
        for (Adicional a : adicionais.values()) {
            javax.swing.JCheckBox c = new javax.swing.JCheckBox(a.toString());
            c.setName(a.getNome());
            extras.add(c);
            pExtras.add(c);
        }
        colunas.add(pTirar);
        colunas.add(pExtras);
        painel.add(colunas, java.awt.BorderLayout.CENTER);

        javax.swing.JPanel sul = new javax.swing.JPanel(new java.awt.BorderLayout(0, 6));
        javax.swing.JTextField obs = new javax.swing.JTextField(30);
        obs.putClientProperty("JTextField.placeholderText", "ex.: ponto da carne, cortar ao meio, para viagem");
        sul.add(cabecalho("Observação para a cozinha"), java.awt.BorderLayout.NORTH);
        sul.add(obs, java.awt.BorderLayout.CENTER);
        javax.swing.JLabel preco = new javax.swing.JLabel();
        preco.setFont(preco.getFont().deriveFont(java.awt.Font.BOLD, 15f));
        sul.add(preco, java.awt.BorderLayout.SOUTH);
        painel.add(sul, java.awt.BorderLayout.SOUTH);
        Runnable atualizarPreco = () -> {
            double unit = produto.getPreco();
            for (javax.swing.JCheckBox c : extras) {
                if (c.isSelected()) {
                    unit += adicionais.get(c.getName()).getPreco();
                }
            }
            preco.setText(String.format("Valor: %d x R$ %.2f = R$ %.2f", item.getQuantidade(), unit, unit * item.getQuantidade()));
        };
        for (javax.swing.JCheckBox c : extras) {
            c.addActionListener(e -> atualizarPreco.run());
        }
        atualizarPreco.run();

        Object[] opcoes = {"Adicionar", "Cancelar"};
        int r = javax.swing.JOptionPane.showOptionDialog(pai, painel, "Personalizar lanche",
                javax.swing.JOptionPane.DEFAULT_OPTION, javax.swing.JOptionPane.PLAIN_MESSAGE, null, opcoes, opcoes[0]);
        if (r != 0) {
            return false;
        }
        for (javax.swing.JCheckBox c : tirar) {
            if (c.isSelected()) {
                item.getRemocoes().add(c.getName());
            }
        }
        double unit = produto.getPreco();
        for (javax.swing.JCheckBox c : extras) {
            if (c.isSelected()) {
                item.getAdicionais().add(c.getName());
                unit += adicionais.get(c.getName()).getPreco();
            }
        }
        item.setObservacao(obs.getText().trim());
        item.setValor(service.VendaService.arredondar(unit));
        return true;
    }

    private static javax.swing.JLabel cabecalho(String texto) {
        javax.swing.JLabel l = new javax.swing.JLabel(texto);
        l.setFont(l.getFont().deriveFont(java.awt.Font.BOLD));
        l.setForeground(ui.Tema.TEXTO_SECUNDARIO);
        return l;
    }
}
