package entidades;

import java.time.LocalDate;

/** Despesa operacional: aluguel, energia, fornecedor... (#40). */
public class Despesa {

    private int id;
    private String descricao;
    private CategoriaDespesa categoria = CategoriaDespesa.OUTROS;
    private LocalDate data = LocalDate.now();
    private double valor;
    private String usuarioNome;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public CategoriaDespesa getCategoria() { return categoria; }
    public void setCategoria(CategoriaDespesa categoria) { this.categoria = categoria; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }

    /** Quem registrou (apenas leitura). */
    public String getUsuarioNome() { return usuarioNome; }
    public void setUsuarioNome(String usuarioNome) { this.usuarioNome = usuarioNome; }
}
