# BLK Burguer

Sistema de gestão de hamburgueria desenvolvido em Java Swing com PostgreSQL para o Projeto Integrador/Extensão 2026.

## Funcionalidades atuais

- Login de usuários
- Controle de estoque e ingredientes
- Cadastro de produtos e composição por ingredientes
- Registro e finalização de vendas
- Baixa automática de estoque
- Relatório de vendas e estoque crítico

## Tecnologias

- Java 8
- Java Swing
- PostgreSQL
- Ant
- NetBeans
- GitHub Actions

## Como preparar o projeto

### 1. Pré-requisitos

- JDK 8 ou superior
- Apache Ant
- PostgreSQL

### 2. Banco de dados

Crie um banco chamado `BLKburguer` e execute o arquivo:

```text
database/schema.sql
```

O schema versionado cria as tabelas utilizadas atualmente pelo sistema: `Login`, `Estoque`, `Produto`, `ProdutoIngrediente` e `Venda`.

Para ter um cardápio, estoque e vendas de exemplo (útil para testes e apresentação), execute em seguida `database/dados-exemplo.sql` em um banco vazio. Ele não cria usuários.

O backup binário antigo do PostgreSQL não é necessário para criar uma instalação nova do sistema.

### 3. Configurar a conexão

Copie `db.properties.example` para `db.properties` na raiz do projeto e informe URL, usuário e senha do seu PostgreSQL. O `db.properties` não é versionado.

Também é possível usar as variáveis de ambiente `BLK_DB_URL`, `BLK_DB_USER` e `BLK_DB_PASSWORD`, que têm prioridade sobre o arquivo.

Toda conexão do sistema passa pela classe `config.ConexaoBD`.

### 4. Compilar

```bash
ant clean compile
```

### 5. Executar

```bash
ant run
```

O comando `ant run` baixa automaticamente o driver JDBC PostgreSQL 42.7.13 para a pasta `lib/` quando necessário. O arquivo `.jar` não é versionado.

## Estrutura de branches

```text
main
└── develop
    ├── feature/nome-da-feature
    ├── feature/outra-feature
    └── fix/nome-da-correcao
```

- `main`: versão estável.
- `develop`: integração do desenvolvimento.
- `feature/*`: novas funcionalidades criadas a partir de `develop`.
- `fix/*`: correções criadas a partir de `develop`.

PRs de desenvolvimento devem apontar para `develop`. Uma versão pronta é promovida por PR de `develop` para `main`.

## Fluxo para desenvolver

```bash
git checkout develop
git pull origin develop
git checkout -b feature/nome-da-feature
```

Depois:

```bash
git add .
git commit -m "feat: descricao da alteracao"
git push -u origin feature/nome-da-feature
```

Abra a Pull Request para `develop` e aguarde o workflow **Build** concluir com sucesso.

## Regras do repositório

Não versionar `build/`, `dist/`, arquivos `.class`, `.jar`, `nbproject/private/`, `.idea/` ou credenciais. O template de PR contém o checklist mínimo e o `CODEOWNERS` aponta a revisão geral para `@felipesoaresdeoliveira`.

## Origem

A base inicial foi migrada de `Luiz-Hamamura/Projeto-Extens-o-2026`, preservando o código-fonte Java e os arquivos `.form` do NetBeans, mas removendo artefatos gerados e configurações locais de IDE.
