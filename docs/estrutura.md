# Estrutura do projeto

O sistema é um programa de desktop em **Java 8 + Swing + PostgreSQL**, compilado com **Ant**.

## Caminho de uma venda

1. O caixa clica em **Finalizar venda** na tela (`telas/Vendas.java`).
2. A tela chama `VendaService` (`service`), que confere permissão, estoque e valores.
3. O serviço usa os DAOs (`dao`) para gravar no PostgreSQL dentro de **uma transação**:
   se qualquer passo falhar, nada é salvo.
4. A cozinha enxerga o pedido sozinha, porque a tela dela consulta o banco a cada poucos segundos.

## Pastas

- `telas/`: uma classe por tela. `Main` é a janela única com o menu lateral.
- `service/`: regras do negócio. `Sessao` guarda o usuário logado e valida permissões.
- `dao/`: SQL com `PreparedStatement`. As gravações recebem a `Connection` do serviço para fazer parte da mesma transação.
- `entidades/`: dados do sistema. `Perfil` e `Modulo` definem o que cada usuário acessa.
- `ui/`: tema (cores), ícones e gráficos.
- `impressao/`: documentos imprimíveis.

## Transações (como funciona)

Os serviços que gravam várias coisas juntas seguem sempre o mesmo padrão:

```java
Connection con = iniciar();      // abre a conexão sem gravar ainda
try {
    // ... grava pedido, itens, baixa de estoque ...
    con.commit();                // tudo certo: grava de verdade
} catch (SQLException | RuntimeException e) {
    desfazer(con);               // deu erro: volta tudo
    throw e;
} finally {
    con.close();
}
```

## Banco de dados

O script `database/schema.sql` cria todas as tabelas e pode ser executado mais de uma vez sem problemas.
