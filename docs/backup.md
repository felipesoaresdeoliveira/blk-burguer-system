# Backup e restauração do banco

O BLK Burguer guarda tudo no PostgreSQL (banco `blkburguer`). Os scripts da pasta `scripts/` usam as ferramentas que já vêm com o PostgreSQL (`pg_dump` e `pg_restore`).

## Gerar um backup

No Windows, dê dois cliques em `scripts\backup.bat` ou rode no terminal:

```bat
scripts\backup.bat
```

No Linux/macOS:

```sh
./scripts/backup.sh
```

O arquivo vai para a pasta `backups/` com a data no nome, por exemplo `backups/blkburguer_2026-10-07_2300.backup`. A senha do usuário `postgres` é pedida na hora. Se quiser rodar sem digitar a senha (por exemplo, num agendamento), use a variável `PGPASSWORD` ou o arquivo de senhas do PostgreSQL (`%APPDATA%\postgresql\pgpass.conf` no Windows, `~/.pgpass` no Linux).

Para outro servidor ou usuário, defina `PGHOST`, `PGPORT` e `PGUSER` antes de rodar. Para outro banco, passe o nome: `scripts\backup.bat outro_banco`.

**Rotina sugerida:** um backup por dia, no fechamento. Guarde uma cópia fora do computador (pen drive ou nuvem), porque um backup no mesmo disco não protege contra defeito no disco.

### Agendar no Windows

No Agendador de Tarefas, crie uma tarefa diária que execute `scripts\backup.bat` (com a pasta do sistema como "Iniciar em"). Configure a senha no `pgpass.conf` para a tarefa não ficar esperando digitação.

## Restaurar

Por segurança, o padrão é restaurar num banco **separado** (`blkburguer_restaurado`), para conferir antes:

```bat
scripts\restaurar.bat backups\blkburguer_2026-10-07_2300.backup
```

Para conferir, aponte uma cópia do sistema para o banco restaurado (`db.url=jdbc:postgresql://localhost:5432/blkburguer_restaurado` no `db.properties`) ou consulte pelo pgAdmin.

Para **substituir o banco em uso** (por exemplo, depois de perder o computador), feche o sistema em todas as máquinas e informe o nome do banco. O script pede para digitar `SIM`:

```bat
scripts\restaurar.bat backups\blkburguer_2026-10-07_2300.backup blkburguer
```

## O que foi testado

- Backup do banco de testes e restauração num banco separado: pedidos, itens, auditoria, despesas, totais e estoque conferem com o original.
- A trava da auditoria (não permite alterar nem apagar registros) continua valendo no banco restaurado.

## Cuidados

- A pasta `backups/` está no `.gitignore`: **backups têm dados reais e nunca devem ir para o GitHub**.
- O `db.properties` (com a senha) também não é versionado.
- Os backups não são criptografados. Guarde-os em local com acesso restrito.
