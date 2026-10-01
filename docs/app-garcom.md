# App do garçom (Fase B) — arquitetura decidida

## Objetivo

O garçom anota o pedido no celular, na mesa. O pedido entra na comanda da mesa, vai para o painel da cozinha e aparece no Caixa para receber, exatamente como os pedidos lançados pelo desktop.

## Funciona sem internet

Tudo roda na **rede local (Wi-Fi do restaurante)**. O roteador mantém a rede local mesmo sem internet; só o roteador e o PC servidor precisam estar ligados (recomenda-se nobreak nos dois).

O 4G **não** resolve: um celular no 4G está fora da rede local e não alcança o servidor.

```text
             Wi-Fi do restaurante (não precisa de internet)
 ┌─────────────┐   ┌──────────────────────────────┐   ┌────────────┐
 │ Celular do  │──►│ PC servidor                  │◄──│ PC do Caixa│
 │ garçom      │   │ • PostgreSQL                 │   │ (desktop)  │
 │ (navegador) │   │ • Servidor web embutido (API)│   └────────────┘
 └─────────────┘   │   usa as mesmas regras Java  │   ┌────────────┐
                   └──────────────────────────────┘◄──│ TV cozinha │
                                                      └────────────┘
```

## Decisões

| Tema | Decisão |
|---|---|
| Tecnologia | Servidor HTTP **embutido** no próprio sistema (`com.sun.net.httpserver`, Java 8), sem Tomcat. |
| Regras | A API chama os mesmos serviços do desktop (`PedidoService`, `CaixaService`...). Nenhuma regra duplicada. |
| Página | HTML/CSS/JS leve no tema BLK, feita para celular. Atualiza por consulta periódica (como a TV da cozinha). |
| Login | Mesmas contas do sistema, perfil **Garçom**. Sessão por token; só na rede local. |
| Pagamento | **Só o Caixa recebe.** O garçom marca "pediu a conta". |
| Wi-Fi instável | O celular guarda o pedido e reenvia quando a conexão volta. Cada envio tem um código único para não lançar em dobro. |
| Acesso | A tela do servidor mostra o endereço (ex.: `http://192.168.0.10:8080`) e um QR code. |

## Etapas

1. Servidor embutido + API JSON: login, cardápio (com fichas e adicionais), mapa de mesas, comanda, lançar itens, pedir a conta.
2. Página do garçom: mapa de mesas, cardápio com personalização (tirar, adicionais, observação), envio para a cozinha.
3. Fila offline no celular, idempotência no servidor, QR code e instruções em `docs/rede.md`.

## O que já existe e será reaproveitado

- Pedido de mesa com itens enviados à cozinha por item (`Venda.status_cozinha`).
- Personalização de lanche (`remocoes`, `adicionais`, `observacao`) com preço e baixa de estoque calculados no servidor.
- Uma comanda aberta por mesa (índice único no banco) — dois garçons não abrem a mesma mesa.
- Recebimento dividido no Caixa (`Pagamento`).
