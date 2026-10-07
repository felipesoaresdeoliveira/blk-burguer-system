# BLK Burguer

Sistema de gestão de hamburgueria desenvolvido em Java Swing com PostgreSQL para o Projeto Integrador/Extensão 2026.

## Funcionalidades atuais

- **Login e perfis**: administrador, gerente, caixa, garçom e cozinha, cada um vendo só o que pode usar
- **Operação**: venda de balcão (com desconto), mesas e comandas, retirada e delivery, pagamento dividido em várias formas
- **Cozinha**: painel em tempo real para a TV, com remoções, adicionais e fila por ordem de chegada
- **Caixa**: abertura, sangria, suprimento e fechamento com conferência por forma de pagamento
- **Cadastros**: clientes, produtos com ficha técnica e estoque com baixa automática
- **Estoque**: entradas, ajustes e perdas (vencimento, quebra, descarte), com histórico de quem fez
- **Gestão**: visão geral com gráficos, relatórios por período, fechamento do dia, despesas e resultado
- **Impressão**: conta da mesa, pedido para a cozinha, recibo e fechamento do dia, com visualização antes de imprimir (bobina 80/58 mm ou folha comum)
- **Auditoria**: cancelamentos, descontos, preços, perdas, caixa e contas ficam registrados; o banco não permite alterar esse histórico
- **Backup**: scripts para gerar e restaurar backups do PostgreSQL ([docs/backup.md](docs/backup.md))

## Tecnologias

- Java 8
- Java Swing + [FlatLaf](https://www.formdev.com/flatlaf/) (temas escuro e claro) e FlatLaf Extras + JSVG para os ícones SVG
- Ícones [Lucide](https://lucide.dev) (licença ISC, em `src/ui/icones/`)
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

O Ant baixa automaticamente as dependências para a pasta `lib/` quando necessário (driver JDBC PostgreSQL 42.7.13, FlatLaf 3.7.2, FlatLaf Extras 3.7.2 e JSVG 2.1.0). Os arquivos `.jar` não são versionados.

**NetBeans:** antes de abrir o projeto pela primeira vez, rode `ant deps` para baixar os `.jar` de `lib/`, que já estão referenciados no classpath do projeto.

## Perfis de acesso

O primeiro usuário cadastrado direto no banco vira **Administrador**. Pelo sistema, o administrador cria as demais contas em **Usuários**:

- **Administrador** (o chefe): acesso total, cria e gerencia contas e consulta a auditoria.
- **Gerente**: gerencia a operação (inclusive despesas e fechamento do dia), exceto contas e auditoria.
- **Caixa**: entra direto na tela do Caixa; vende no balcão, anota mesas/retirada/delivery, recebe (inclusive dividido), abre e fecha o caixa.
- **Garçom**: mesas, comandas e pedidos.
- **Cozinha**: abre direto o painel de produção (feito para a TV da cozinha).

As senhas são guardadas com hash PBKDF2; senhas antigas em texto são convertidas no primeiro login.

## Vários computadores

Caixa, TV da cozinha e gerência podem rodar em computadores diferentes usando o mesmo banco. O passo a passo (liberar o PostgreSQL na rede, gerar o pacote com `ant jar` e configurar cada máquina) está em [docs/rede.md](docs/rede.md).

O app do garçom pelo celular (rede local, sem depender de internet) está planejado em [docs/app-garcom.md](docs/app-garcom.md).

## Backup

Para gerar e restaurar backups do banco veja [docs/backup.md](docs/backup.md). Resumo: `scriptsackup.bat` gera um arquivo em `backups/` (fora do Git) e `scriptsestaurar.bat <arquivo>` restaura num banco separado para conferência.

## Visual e navegação

- **Janela única:** depois do login, tudo acontece em `telas.Main`, com menu lateral fixo (só aparecem os módulos do perfil logado), caminho da tela no topo e relógio. Cada tela é um `JPanel` que implementa `telas.Navegavel`; para trocar de tela use `Main.navegar(new TelaX())`. Timers e atualizações automáticas param em `aoSair()`.
- **Temas escuro e claro:** o botão no rodapé do menu (ou na tela de login) alterna o tema, e a escolha fica salva no computador. Todas as cores ficam em `ui.Tema`; as telas não usam cores fixas (`Tema.titulo`, `Tema.primario`, `Tema.perigo`, `Tema.reais`...).
- **Ícones:** `ui.Icones.get("nome", tamanho)` carrega um SVG de `src/ui/icones/` na cor do tema. Os botões ganham ícone automaticamente pelo texto (`ui.Acabamento`), que também alinha números à direita, mostra status como selos coloridos e escreve uma mensagem nas tabelas vazias.
- **Avisos:** confirmações de sucesso aparecem como aviso discreto no canto da tela (`ui.Aviso.sucesso`), sem caixa de diálogo para fechar.
- **Visão geral:** dashboard com indicadores do dia e gráficos dos últimos 7 dias (`telas.TelaVisaoGeral`). Os gráficos são componentes próprios em `ui/` (`GraficoColunas`, `GraficoRosca`, `GraficoBarras`, `PainelAlertas`, `CartaoIndicador`) declarados no `.form`, então o designer do NetBeans os exibe normalmente (depois de compilar).
- **Cozinha na TV:** a conta da cozinha abre direto o painel em tela cheia (`telas.CozinhaTV`; F11 alterna, Esc sai da tela cheia). Pelo menu, "Abrir na TV" faz o mesmo em qualquer perfil com acesso à cozinha.

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
