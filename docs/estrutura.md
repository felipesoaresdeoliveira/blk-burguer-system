# Estrutura do projeto

O sistema é um programa de computador (desktop) em Java com banco PostgreSQL.
O código é dividido em camadas, e cada uma tem uma função:

- **Telas**: mostram os dados e recebem os cliques do usuário.
- **Service**: tem as regras (por exemplo, não vender sem estoque).
- **DAO**: só faz o acesso ao banco, com comandos SQL.
- **Entidades**: guardam os dados (Pedido, Produto, Cliente...).

## Exemplo: uma venda no balcão

1. O caixa clica em **Finalizar venda** na tela `Vendas`.
2. A tela chama o `VendaService`, que confere permissão, estoque e valores.
3. O service usa os DAOs para gravar o pedido, os itens, a baixa do estoque e o pagamento.
4. Se algo der errado no meio, nada é salvo (`rollback`). Se der tudo certo, salva (`commit`).
5. A tela da cozinha consulta o banco de tempos em tempos e mostra o pedido novo.

## Perfis

Cada perfil (`Perfil`) tem uma lista de módulos (`Modulo`) que pode usar.
O `Sessao` guarda quem está logado e o service confere a permissão antes de agir.
