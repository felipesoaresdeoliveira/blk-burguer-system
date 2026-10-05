package entidades;

public class Usuario {

    private int id;
    private String usuario;
    private String nome;
    private Perfil perfil = Perfil.CAIXA;
    private boolean ativo = true;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    /** Nome de exibição; usa o login quando não há nome cadastrado. */
    public String getNome() { return nome == null || nome.trim().isEmpty() ? usuario : nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Perfil getPerfil() { return perfil; }
    public void setPerfil(Perfil perfil) { this.perfil = perfil; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public boolean pode(Modulo modulo) { return perfil.pode(modulo); }
}
