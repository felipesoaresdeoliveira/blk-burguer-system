# BLK Burguer

Sistema de gestão de hamburgueria feito em Java (Swing) com banco PostgreSQL.
Projeto Integrador/Extensão 2026.

## O que o sistema faz

- Login com perfis: administrador, gerente, caixa, garçom e cozinha
- Venda no balcão, mesas (comanda), retirada e delivery
- Pagamento em dinheiro, pix, débito e crédito (pode dividir)
- Painel da cozinha com os pedidos
- Controle de caixa (abrir, sangria, suprimento e fechar)
- Cadastro de clientes, produtos e estoque
- Visão geral com gráficos, relatórios, fechamento do dia e despesas
- Impressão de recibo, conta e pedido da cozinha

## Como rodar

Precisa de JDK 8 ou superior, Apache Ant e PostgreSQL.

1. Crie o banco `blkburguer` e execute `database/schema.sql`
   (para ter dados de exemplo, execute também `database/dados-exemplo.sql`).
2. Copie `db.properties.example` para `db.properties` e coloque a senha do seu PostgreSQL.
3. Crie o primeiro usuário no banco (exemplo no final do `schema.sql`).
4. Rode `ant run`.

No NetBeans, rode `ant deps` uma vez para baixar as bibliotecas da pasta `lib/`.

## Como o código está organizado

```text
telas  ->  service  ->  dao  ->  PostgreSQL
```

| Pasta | O que tem |
|---|---|
| `src/telas` | As telas (Swing) |
| `src/service` | As regras do sistema (estoque, pagamento, permissões) |
| `src/dao` | Os comandos SQL |
| `src/entidades` | As classes que representam os dados (Pedido, Produto...) |
| `src/ui` | Cores, ícones e gráficos |
| `src/impressao` | Os documentos para imprimir |
| `src/config` | Conexão com o banco |
| `database` | Tabelas e dados de exemplo |

Veja mais em [docs/estrutura.md](docs/estrutura.md).
