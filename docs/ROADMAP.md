# Roadmap — BLK Burguer

Este roadmap organiza a evolução para Felipe e Luiz trabalharem em paralelo sem bloquear um ao outro.

## Fluxo Git

`main` → versão estável.
`develop` → integração.
`feature/*`, `fix/*`, `refactor/*`, `chore/*` e `docs/*` → sempre criadas a partir de `develop`.

## Fase 0 — Base e estabilidade

- #4 Centralizar conexão PostgreSQL.
- #9 Corrigir baixa de estoque.
- #10 Histórico de vendas.
- #16 Melhorar ficha técnica.
- #17 Formas de pagamento e troco.
- #21 Entradas e ajustes de estoque.

## Fase 1 — Operação principal

- #19 Clientes.
- #24 Comandas/status.
- #27 Gestão visual de mesas.
- #28 Painel da cozinha.
- #32 Adicionais e observações.
- #35 Balcão, retirada e delivery.

Fluxo alvo: Cliente → Mesa/Retirada/Delivery → Comanda/Pedido → Cozinha → Pagamento.

## Fase 2 — Caixa e comercial

- #18 Descontos.
- #29 Controle de caixa.
- #30 Pagamento parcial/divisão.
- #31 Combos.
- #33 Fornecedores/compras.
- #34 Perdas e desperdícios.

## Fase 3 — Gestão

- #20 Dashboard.
- #25 Relatórios por período.
- #26 Estoque baixo.
- #36 Perfis e permissões.
- #37 Auditoria.
- #40 Despesas.
- #41 Fechamento diário.

## Fase 4 — Acabamento

- #22 Busca e filtros.
- #23 Padronização visual.
- #38 Impressões.
- #39 Reservas.
- #42 Backup e restauração.

## Divisão inicial

### Felipe
Banco e regras de negócio, estoque, financeiro/caixa, permissões, auditoria e consistência do schema.

### Luiz
Operação, telas e navegação, mesas, cozinha, delivery/retirada e experiência visual.

## Definição de pronto

- Implementação funcional.
- Schema atualizado quando necessário.
- Fluxo testado manualmente.
- `ant clean compile` passando.
- Sem credenciais ou artefatos locais.
- Pull Request para `develop`.
- PR explica como validar.