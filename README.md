# BLK Burguer

Sistema de gestão de hamburgueria feito em **Java (Swing) + PostgreSQL**, para o Projeto Integrador/Extensão 2026.

## O que o sistema faz

- **Login por perfil**: administrador, gerente, caixa, garçom e cozinha. Cada um vê só o que pode usar.
- **Vendas**: balcão, mesas (comanda), retirada e delivery, com pagamento dividido e desconto.
- **Cozinha**: painel para a TV mostrando os pedidos em tempo real.
- **Caixa**: abrir, sangria, suprimento e fechar com conferência.
- **Cadastros**: clientes, produtos (com ficha técnica) e estoque (entrada, ajuste e perda).
- **Gestão**: visão geral com gráficos, relatórios, fechamento do dia, despesas e auditoria.
- **Impressão**: recibo, conta da mesa e pedido da cozinha.

## Como rodar

Precisa de: **JDK 8+**, **Apache Ant** e **PostgreSQL**.

1. Crie o banco `blkburguer` e rode `database/schema.sql`.
   (Opcional, para ter dados de exemplo: `database/dados-exemplo.sql`.)
2. Copie `db.properties.example` para `db.properties` e coloque a senha do seu PostgreSQL.
3. Crie o primeiro usuário no banco (veja o final de `database/schema.sql`).
4. Rode:

```bash
ant run
```

O Ant baixa as bibliotecas para a pasta `lib/` na primeira vez. No NetBeans, rode `ant deps` antes de abrir o projeto.

## Como o código está organizado

O código segue um caminho simples, de cima para baixo:

```text
telas  →  service  →  dao  →  PostgreSQL
(o que o usuário vê)  (regras)  (SQL)
```

| Pasta | Para que serve |
|---|---|
| `src/telas` | Telas (Swing). Só mostram dados e chamam os serviços. |
| `src/service` | Regras do negócio: estoque, pagamento, desconto, permissões. |
| `src/dao` | Todo o SQL do sistema (um DAO para cada assunto). |
| `src/entidades` | Classes simples que representam os dados (Pedido, Produto...). |
| `src/ui` | Cores, ícones, gráficos e componentes visuais. |
| `src/impressao` | Montagem dos documentos impressos. |
| `src/config` | Conexão com o banco (`ConexaoBD`). |
| `database` | Script das tabelas e dados de exemplo. |
| `teste` | Teste simples das regras de venda. |
| `docs` | Documentos extras (rede, backup). |

Mais detalhes em [docs/estrutura.md](docs/estrutura.md).

## Perfis de acesso

| Perfil | O que acessa |
|---|---|
| Administrador | Tudo, inclusive contas de usuário e auditoria |
| Gerente | Tudo, menos contas de usuário e auditoria |
| Caixa | Vendas, caixa, pedidos, mesas, clientes e histórico |
| Garçom | Pedidos, mesas e clientes |
| Cozinha | Só o painel da cozinha |

As senhas são guardadas com hash (PBKDF2), nunca em texto.

## Para desenvolver

- Use as branches `feature/...` e abra Pull Request para `develop`.
- Não suba `build/`, `dist/`, `.jar`, `db.properties` nem backups.
- Veja também [CONTRIBUTING.md](CONTRIBUTING.md).
