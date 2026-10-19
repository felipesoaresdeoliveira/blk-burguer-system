import entidades.*;
import java.util.*;
import service.*;

public class TesteRapido {
    static int ok, falhas;
    static void ver(boolean c, String n) { if (c) ok++; else falhas++; System.out.println((c ? "  OK    " : "  FALHA ") + n); }
    static Venda item(String p, int q) { Venda v = new Venda(); v.setProduto(p); v.setQuantidade(q); return v; }

    public static void main(String[] a) throws Exception {
        Usuario u = new Usuario(); u.setId(1); u.setUsuario("admin"); u.setNome("Teste"); u.setPerfil(Perfil.ADMIN);
        Sessao.iniciar(u);
        PedidoService ps = new PedidoService();

        Pedido p = ps.vendaBalcao(new ArrayList<>(Arrays.asList(item("BLK Clássico", 2))), FormaPagamento.PIX, 0, 0, false, 0, "Ana", 5.0);
        ver(p.getSituacao() == SituacaoPedido.PAGO && p.getTotal() == 52.8, "venda com desconto paga (total " + p.getTotal() + ")");

        Pedido r = ps.abrir(retirada());
        ps.lancarItens(r.getId(), new ArrayList<>(Arrays.asList(item("BLK Bacon", 1))));
        ps.receberPagamento(r.getId(), FormaPagamento.PIX, 10, 10);
        ver(ps.buscar(r.getId()).getSituacao() == SituacaoPedido.ABERTO, "pagamento parcial mantém aberto");
        ps.receberPagamento(r.getId(), FormaPagamento.PIX, 24.9, 24.9);
        ver(ps.buscar(r.getId()).getSituacao() == SituacaoPedido.PAGO, "pagamento total fecha o pedido");

        Pedido c = ps.abrir(retirada());
        ps.lancarItens(c.getId(), new ArrayList<>(Arrays.asList(item("Nuggets", 1))));
        ps.cancelar(c.getId());
        ver(ps.buscar(c.getId()).getSituacao() == SituacaoPedido.CANCELADO, "cancelamento funciona");

        Pedido e = ps.abrir(retirada());
        try {
            ps.lancarItens(e.getId(), new ArrayList<>(Arrays.asList(item("BLK Duplo", 5000))));
            ver(false, "estoque insuficiente deveria falhar");
        } catch (EstoqueInsuficienteException ex) {
            ver(ps.buscar(e.getId()).getItens().isEmpty(), "estoque insuficiente recusado e nada gravado");
        }
        try { ps.cancelar(c.getId()); ver(false, "cancelar de novo deveria falhar"); }
        catch (IllegalStateException ex) { ver(true, "pedido já cancelado recusado"); }
        System.out.println(ok + " ok, " + falhas + " falha(s)");
        System.exit(falhas);
    }
    static Pedido retirada() { Pedido p = new Pedido(); p.setTipo(TipoPedido.RETIRADA); return p; }
}
