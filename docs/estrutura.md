# Estrutura do Projeto — BLK Burguer

## Visão geral

O BLK Burguer é um sistema desktop de gestão de hamburgueria em **Java 8 + Swing + PostgreSQL**, compilado com **Ant**.

A evolução deve reduzir o acoplamento atual entre telas, regras de negócio e SQL sem transformar o projeto acadêmico em uma arquitetura desnecessariamente complexa.

## Arquitetura alvo

Swing (View) → Controller → Service → Repository/DAO → PostgreSQL

### view/
Telas Java Swing e arquivos `.form` existentes. Exibem dados, coletam entradas e disparam ações. Evitar SQL e regras de negócio diretamente nas telas.

### controller/
Coordena eventos da interface e chama serviços.

### service/
Concentra regras como validar estoque, calcular comanda, dividir pagamento, controlar status da mesa e fechar caixa.

### repository/ ou dao/
Acesso ao PostgreSQL via JDBC. Preferir `PreparedStatement`.

### model/
Entidades do domínio: Usuario, Cliente, Produto, Ingrediente, Mesa, Comanda, Pedido, ItemPedido, Caixa, Pagamento, Fornecedor e MovimentacaoEstoque.

### config/ e util/
Configurações de infraestrutura, conexão com banco, validações, formatação e helpers reutilizáveis.

## Módulos

- Operação: Pedidos, Mesas, Comandas, Cozinha, Delivery/Retirada.
- Cadastros: Clientes, Produtos, Ingredientes, Combos, Fornecedores.
- Estoque: Movimentações, Compras, Perdas, Alertas.
- Financeiro: Caixa, Pagamentos, Despesas, Fechamento diário.
- Gestão: Dashboard, Relatórios, Usuários/Permissões, Auditoria.

## Banco de dados

O banco é PostgreSQL e o schema reproduzível deve permanecer em `database/schema.sql`.

## IDE

O projeto pode ser desenvolvido no IntelliJ IDEA ou NetBeans. Os arquivos `.form` existentes foram criados no NetBeans; para edição visual dessas telas, o NetBeans é mais simples. Código Java, JDBC, regras e Git podem ser trabalhados normalmente no IntelliJ.

## Regras

- Não adicionar SQL novo diretamente nas telas.
- Preferir `PreparedStatement`.
- Não versionar credenciais.
- Toda feature nasce de `develop`.
- Toda feature entra por Pull Request para `develop`.
- Não desenvolver diretamente em `main`.
- Manter `database/schema.sql` sincronizado.
- O projeto deve continuar compilando com `ant clean compile`.