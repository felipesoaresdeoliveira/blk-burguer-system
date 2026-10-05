# Usar o BLK Burguer em vários computadores

Todos os computadores (caixa, TV da cozinha, gerência) usam **o mesmo banco PostgreSQL**. Um computador fica como **servidor** (onde o PostgreSQL está instalado) e os outros se conectam a ele pela rede local.

Um pedido lançado no caixa aparece no painel da cozinha em até 4 segundos, sem precisar atualizar nada.

## 1. No computador servidor (onde está o PostgreSQL)

### 1.1 Descobrir o IP na rede

```bash
ipconfig
```

Anote o **Endereço IPv4** da placa de rede em uso (ex.: `192.168.0.10`).

### 1.2 Aceitar conexões da rede

Na pasta de dados do PostgreSQL (ex.: `C:\Program Files\PostgreSQL\18\data`):

- Em `postgresql.conf`, deixe:

  ```text
  listen_addresses = '*'
  ```

- Em `pg_hba.conf`, adicione no final (ajuste a faixa para a da sua rede):

  ```text
  host    blkburguer    blk_app    192.168.0.0/24    scram-sha-256
  ```

Reinicie o serviço **postgresql-x64-18** (Serviços do Windows).

### 1.3 Liberar a porta 5432 no firewall

Em um PowerShell **como administrador**:

```powershell
New-NetFirewallRule -DisplayName "PostgreSQL BLK Burguer" -Direction Inbound -Protocol TCP -LocalPort 5432 -Action Allow -Profile Private
```

Use apenas em rede privada (a do estabelecimento), nunca em Wi-Fi público.

### 1.4 Criar um usuário de banco só para o sistema

Evite usar o usuário `postgres` (administrador do banco) nos outros computadores:

```sql
CREATE ROLE blk_app LOGIN PASSWORD 'troque-esta-senha';
GRANT CONNECT ON DATABASE blkburguer TO blk_app;
\c blkburguer
GRANT USAGE ON SCHEMA public TO blk_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO blk_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO blk_app;
```

## 2. Gerar o pacote do sistema

No computador de desenvolvimento:

```bash
ant jar
```

A pasta `dist/` fica assim:

```text
dist/
├── BLK_BURGUER.jar
├── BLK-Burguer.bat        (atalho para abrir)
├── db.properties.example
└── lib/                   (driver PostgreSQL e tema FlatLaf)
```

## 3. Em cada computador (caixa, cozinha...)

1. Instale o Java 8 ou superior (ex.: Eclipse Temurin).
2. Copie a pasta `dist/` inteira.
3. Copie `db.properties.example` para `db.properties` na mesma pasta e aponte para o servidor:

   ```properties
   db.url=jdbc:postgresql://192.168.0.10:5432/blkburguer
   db.user=blk_app
   db.password=troque-esta-senha
   ```

4. Abra com `BLK-Burguer.bat`.

## 4. Contas de cada computador

O **administrador** cria as contas em **Usuários**:

| Computador | Perfil | O que abre |
|---|---|---|
| TV da cozinha | Cozinha | Painel de produção em tela cheia (F11/Esc alternam) |
| Balcão | Caixa | Nova venda, caixa (abrir/fechar), histórico |
| Gerência | Gerente | Tudo, exceto criar contas |
| Dono | Administrador | Tudo, inclusive usuários |

## Problemas comuns

- **"Banco de dados indisponível" no login:** confira o IP em `db.properties`, se o serviço do PostgreSQL está rodando no servidor e a regra do firewall.
- **Painel da cozinha mostra "Sem conexão com o banco":** a rede caiu; o painel tenta de novo sozinho a cada 4 segundos.
- **"Abra o caixa antes de registrar vendas":** alguém com perfil Caixa, Gerente ou Administrador precisa abrir o caixa do dia na tela **Caixa**.
