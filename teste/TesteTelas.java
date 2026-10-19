import entidades.*;
import javax.swing.*;

/** Abre todas as telas na janela unica para ver se alguma quebra (precisa do banco ligado). */
public class TesteTelas {
    public static void main(String[] a) throws Exception {
        ui.Tema.aplicar();
        Usuario u = new Usuario(); u.setId(1); u.setUsuario("admin"); u.setNome("Teste"); u.setPerfil(Perfil.ADMIN);
        service.Sessao.iniciar(u);
        String[] nomes = {"TelaVisaoGeral", "TelaCaixa", "TelaMesas", "TelaPedidos", "TelaCozinha", "TelaClientes", "TelaProdutos",
            "TelaEstoque", "TelaHistoricoVendas", "TelaRelatorio", "TelaFechamento", "TelaDespesas", "TelaUsuarios", "TelaAuditoria", "Vendas"};
        for (String t : nomes) {
            SwingUtilities.invokeAndWait(() -> {
                try { telas.Main.navegar((JPanel) Class.forName("telas." + t).getConstructor().newInstance()); }
                catch (Exception e) { throw new RuntimeException(t, e); }
            });
            Thread.sleep(500);
            System.out.println("OK " + t);
        }
        System.exit(0);
    }
}
