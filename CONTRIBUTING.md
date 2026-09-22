# Contribuindo com o BLK Burguer

## Fluxo

1. Atualize `develop`.
2. Crie uma branch a partir de `develop`.
3. Implemente uma issue por vez.
4. Execute `ant clean compile`.
5. Abra Pull Request para `develop`.
6. Faça revisão cruzada.

## Branches

- `feature/nome-da-feature`
- `fix/nome-da-correcao`
- `refactor/nome`
- `chore/nome`
- `docs/nome`

## Commits

Prefira mensagens como:
- `feat: adiciona abertura de mesa`
- `fix: impede estoque negativo`
- `refactor: extrai acesso ao banco da tela`
- `docs: atualiza fluxo de desenvolvimento`
- `chore: ajusta configuração do projeto`

## Pull Requests

Trabalho normal: `feature/*` ou `fix/*` → `develop`.
Versão estável: `develop` → `main`.

Inclua issue relacionada, resumo, como testar, alterações de banco e prints quando houver mudança visual.

## Trabalho em dupla

Felipe e Luiz devem evitar trabalhar na mesma tela/arquivo ao mesmo tempo quando possível. Antes de iniciar, assuma uma issue e crie sua branch. Ao terminar, abra PR e peça revisão cruzada.

## Banco

Qualquer tabela ou coluna necessária deve ser refletida em `database/schema.sql`.

## Segurança

- Não versionar usuário/senha do PostgreSQL.
- Não concatenar entrada do usuário em SQL novo.
- Usar `PreparedStatement`.
- Validar dados antes de persistir.
- Não versionar backups reais, `.jar`, `.class`, `build/`, `dist/`, `.idea/` ou configurações privadas da IDE.