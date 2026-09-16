# BLK Burguer

Sistema de gestão de hamburgueria desenvolvido em Java Swing com PostgreSQL para o Projeto Integrador/Extensão 2026.

## Funcionalidades atuais

- Login
- Controle de estoque e ingredientes
- Cadastro de produtos e composição por ingredientes
- Registro de vendas
- Baixa de estoque na finalização da venda
- Relatório de vendas e estoque crítico

## Tecnologias

- Java 8
- Java Swing
- NetBeans
- PostgreSQL
- Ant

## Estrutura de branches

O desenvolvimento do projeto segue este fluxo:

```text
main
└── develop
    ├── feature/nome-da-feature
    ├── feature/outra-feature
    └── fix/nome-da-correcao
```

- `main`: versão estável do projeto.
- `develop`: branch principal de desenvolvimento e integração.
- `feature/*`: novas funcionalidades, sempre criadas a partir de `develop`.
- `fix/*`: correções, também criadas a partir de `develop`.

As Pull Requests de desenvolvimento devem apontar para `develop`. Quando a versão estiver estável, é aberto um PR de `develop` para `main`.

## Fluxo para desenvolver

```bash
git checkout develop
git pull origin develop
git checkout -b feature/nome-da-feature
```

Depois das alterações:

```bash
git add .
git commit -m "feat: descricao da alteracao"
git push -u origin feature/nome-da-feature
```

Em seguida, abra uma Pull Request da sua branch para `develop`.

## Banco de dados

O projeto utiliza PostgreSQL e espera um banco chamado `BLKburguer`.

O backup original do banco existente no projeto anterior está em formato binário do PostgreSQL. Ele não foi incluído automaticamente nesta migração e deve ser restaurado/adicionado separadamente.

## Observações da migração

Este repositório foi criado a partir do código do projeto `Projeto-Extens-o-2026`. Arquivos gerados de compilação, como `build/`, classes compiladas e configurações privadas do NetBeans, não são versionados.

O projeto herdado ainda possui configurações de dependências e conexão com o banco que dependem do ambiente local. Essas configurações devem ser revisadas em uma próxima etapa para tornar a execução totalmente portátil entre os computadores dos desenvolvedores.
