-- BLK Burguer - schema mínimo reproduzível
-- PostgreSQL

CREATE TABLE IF NOT EXISTS "Login" (
    "Id" SERIAL PRIMARY KEY,
    usuario VARCHAR(100) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS "Estoque" (
    nome VARCHAR(100) PRIMARY KEY,
    valor DOUBLE PRECISION NOT NULL DEFAULT 0,
    "Quantidade" INTEGER NOT NULL DEFAULT 0 CHECK ("Quantidade" >= 0)
);

CREATE TABLE IF NOT EXISTS "Produto" (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL UNIQUE,
    tipo VARCHAR(50) NOT NULL,
    preco DOUBLE PRECISION NOT NULL DEFAULT 0 CHECK (preco >= 0)
);

CREATE TABLE IF NOT EXISTS "ProdutoIngrediente" (
    id SERIAL PRIMARY KEY,
    produto_id INTEGER NOT NULL REFERENCES "Produto"(id) ON DELETE CASCADE,
    ingrediente_nome VARCHAR(100) NOT NULL REFERENCES "Estoque"(nome),
    quantidade INTEGER NOT NULL CHECK (quantidade > 0)
);

CREATE TABLE IF NOT EXISTS "Venda" (
    id SERIAL PRIMARY KEY,
    produto VARCHAR(100) NOT NULL,
    quantidade INTEGER NOT NULL CHECK (quantidade > 0),
    valor DOUBLE PRECISION NOT NULL CHECK (valor >= 0),
    total DOUBLE PRECISION NOT NULL CHECK (total >= 0),
    data_venda TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

-- Histórico de entradas e ajustes de estoque (#21)
CREATE TABLE IF NOT EXISTS "MovimentacaoEstoque" (
    id SERIAL PRIMARY KEY,
    ingrediente_nome VARCHAR(100) NOT NULL REFERENCES "Estoque"(nome) ON UPDATE CASCADE ON DELETE CASCADE,
    tipo VARCHAR(20) NOT NULL,
    quantidade_anterior INTEGER NOT NULL,
    quantidade_nova INTEGER NOT NULL CHECK (quantidade_nova >= 0),
    motivo VARCHAR(255),
    data_movimentacao TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

-- Cabeçalho da venda: agrupa os itens de uma mesma compra e guarda o pagamento (#10, #17)
CREATE TABLE IF NOT EXISTS "Pedido" (
    id SERIAL PRIMARY KEY,
    data_pedido TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    total DOUBLE PRECISION NOT NULL CHECK (total >= 0),
    forma_pagamento VARCHAR(20) NOT NULL CHECK (forma_pagamento IN ('DINHEIRO', 'PIX', 'DEBITO', 'CREDITO')),
    valor_recebido DOUBLE PRECISION NOT NULL CHECK (valor_recebido >= total),
    troco DOUBLE PRECISION NOT NULL DEFAULT 0 CHECK (troco >= 0)
);

-- Itens antigos ficam com pedido_id NULL e aparecem como vendas avulsas.
ALTER TABLE "Venda" ADD COLUMN IF NOT EXISTS pedido_id INTEGER REFERENCES "Pedido"(id);

-- Categoria do item de estoque (#14)
ALTER TABLE "Estoque" ADD COLUMN IF NOT EXISTS tipo VARCHAR(20) NOT NULL DEFAULT 'Ingrediente'
    CHECK (tipo IN ('Ingrediente', 'Bebida', 'Acompanhamento'));

-- Permite usar e-mail como usuário (bancos criados antes tinham VARCHAR(25))
ALTER TABLE "Login" ALTER COLUMN usuario TYPE VARCHAR(100);

-- Quantidade mínima para alerta de estoque baixo (#26)
ALTER TABLE "Estoque" ADD COLUMN IF NOT EXISTS estoque_minimo INTEGER NOT NULL DEFAULT 10
    CHECK (estoque_minimo >= 0);

-- ===================================================================
-- Operação completa: perfis, clientes, mesas, caixa, pagamentos,
-- cozinha e delivery (#19 #24 #27 #28 #29 #35 #36)
-- ===================================================================

-- Usuários e perfis (#36). Usuários já existentes viram ADMIN.
ALTER TABLE "Login" ADD COLUMN IF NOT EXISTS nome VARCHAR(100);
ALTER TABLE "Login" ADD COLUMN IF NOT EXISTS perfil VARCHAR(20) NOT NULL DEFAULT 'ADMIN'
    CHECK (perfil IN ('ADMIN', 'GERENTE', 'CAIXA', 'GARCOM', 'COZINHA'));
ALTER TABLE "Login" ADD COLUMN IF NOT EXISTS ativo BOOLEAN NOT NULL DEFAULT TRUE;

-- Clientes (#19)
CREATE TABLE IF NOT EXISTS "Cliente" (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    telefone VARCHAR(30),
    endereco VARCHAR(255),
    observacao VARCHAR(255),
    criado_em TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

-- Mesas (#27). O status (livre/ocupada/aguardando pagamento) vem do pedido aberto.
CREATE TABLE IF NOT EXISTS "Mesa" (
    id SERIAL PRIMARY KEY,
    numero INTEGER NOT NULL UNIQUE CHECK (numero > 0),
    lugares INTEGER NOT NULL DEFAULT 4 CHECK (lugares > 0),
    reservada BOOLEAN NOT NULL DEFAULT FALSE,
    ativa BOOLEAN NOT NULL DEFAULT TRUE
);

-- Caixa (#29): apenas um aberto por vez
CREATE TABLE IF NOT EXISTS "Caixa" (
    id SERIAL PRIMARY KEY,
    aberto_em TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    aberto_por INTEGER REFERENCES "Login"("Id"),
    valor_inicial DOUBLE PRECISION NOT NULL CHECK (valor_inicial >= 0),
    fechado_em TIMESTAMP WITHOUT TIME ZONE,
    fechado_por INTEGER REFERENCES "Login"("Id"),
    informado_dinheiro DOUBLE PRECISION,
    informado_pix DOUBLE PRECISION,
    informado_debito DOUBLE PRECISION,
    informado_credito DOUBLE PRECISION,
    observacao VARCHAR(255)
);
CREATE UNIQUE INDEX IF NOT EXISTS caixa_um_aberto ON "Caixa" ((fechado_em IS NULL)) WHERE fechado_em IS NULL;

CREATE TABLE IF NOT EXISTS "MovimentacaoCaixa" (
    id SERIAL PRIMARY KEY,
    caixa_id INTEGER NOT NULL REFERENCES "Caixa"(id),
    tipo VARCHAR(12) NOT NULL CHECK (tipo IN ('SANGRIA', 'SUPRIMENTO')),
    valor DOUBLE PRECISION NOT NULL CHECK (valor > 0),
    motivo VARCHAR(255),
    usuario_id INTEGER REFERENCES "Login"("Id"),
    data TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

-- Pedido passa a representar também pedidos em aberto (comandas, delivery...)
ALTER TABLE "Pedido" DROP CONSTRAINT IF EXISTS "Pedido_check";
ALTER TABLE "Pedido" ALTER COLUMN forma_pagamento DROP NOT NULL;
ALTER TABLE "Pedido" ALTER COLUMN valor_recebido DROP NOT NULL;
ALTER TABLE "Pedido" ADD COLUMN IF NOT EXISTS tipo VARCHAR(10) NOT NULL DEFAULT 'BALCAO'
    CHECK (tipo IN ('MESA', 'BALCAO', 'RETIRADA', 'DELIVERY'));
ALTER TABLE "Pedido" ADD COLUMN IF NOT EXISTS situacao VARCHAR(10) NOT NULL DEFAULT 'PAGO'
    CHECK (situacao IN ('ABERTO', 'PAGO', 'CANCELADO'));
ALTER TABLE "Pedido" ADD COLUMN IF NOT EXISTS mesa_id INTEGER REFERENCES "Mesa"(id);
ALTER TABLE "Pedido" ADD COLUMN IF NOT EXISTS cliente_id INTEGER REFERENCES "Cliente"(id);
ALTER TABLE "Pedido" ADD COLUMN IF NOT EXISTS identificacao VARCHAR(60);
ALTER TABLE "Pedido" ADD COLUMN IF NOT EXISTS senha_retirada INTEGER;
ALTER TABLE "Pedido" ADD COLUMN IF NOT EXISTS taxa_entrega DOUBLE PRECISION NOT NULL DEFAULT 0 CHECK (taxa_entrega >= 0);
ALTER TABLE "Pedido" ADD COLUMN IF NOT EXISTS endereco_entrega VARCHAR(255);
ALTER TABLE "Pedido" ADD COLUMN IF NOT EXISTS status_entrega VARCHAR(20)
    CHECK (status_entrega IN ('SAIU_PARA_ENTREGA', 'ENTREGUE'));
ALTER TABLE "Pedido" ADD COLUMN IF NOT EXISTS conta_solicitada BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE "Pedido" ADD COLUMN IF NOT EXISTS aberto_por INTEGER REFERENCES "Login"("Id");
ALTER TABLE "Pedido" ADD COLUMN IF NOT EXISTS fechado_em TIMESTAMP WITHOUT TIME ZONE;
CREATE UNIQUE INDEX IF NOT EXISTS pedido_um_por_mesa ON "Pedido"(mesa_id) WHERE situacao = 'ABERTO';
UPDATE "Pedido" SET fechado_em = data_pedido WHERE situacao = 'PAGO' AND fechado_em IS NULL;

-- Itens do pedido: observação e acompanhamento na cozinha (#28, #32)
ALTER TABLE "Venda" ADD COLUMN IF NOT EXISTS observacao VARCHAR(255);
ALTER TABLE "Venda" ADD COLUMN IF NOT EXISTS status_cozinha VARCHAR(12)
    CHECK (status_cozinha IN ('RECEBIDO', 'EM_PREPARO', 'PRONTO', 'ENTREGUE'));
ALTER TABLE "Venda" ADD COLUMN IF NOT EXISTS enviado_em TIMESTAMP WITHOUT TIME ZONE;

-- Pagamentos: vários por pedido (pagamento dividido), vinculados ao caixa
CREATE TABLE IF NOT EXISTS "Pagamento" (
    id SERIAL PRIMARY KEY,
    pedido_id INTEGER NOT NULL REFERENCES "Pedido"(id),
    caixa_id INTEGER REFERENCES "Caixa"(id),
    forma VARCHAR(10) NOT NULL CHECK (forma IN ('DINHEIRO', 'PIX', 'DEBITO', 'CREDITO')),
    valor DOUBLE PRECISION NOT NULL CHECK (valor > 0),
    valor_recebido DOUBLE PRECISION NOT NULL,
    troco DOUBLE PRECISION NOT NULL DEFAULT 0 CHECK (troco >= 0),
    usuario_id INTEGER REFERENCES "Login"("Id"),
    data TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

-- Migra o pagamento das vendas anteriores (uma forma por pedido)
INSERT INTO "Pagamento" (pedido_id, forma, valor, valor_recebido, troco, data)
SELECT p.id, p.forma_pagamento, p.total, COALESCE(p.valor_recebido, p.total), COALESCE(p.troco, 0), p.data_pedido
FROM "Pedido" p
WHERE p.forma_pagamento IS NOT NULL AND p.total > 0
  AND NOT EXISTS (SELECT 1 FROM "Pagamento" pg WHERE pg.pedido_id = p.id);

-- Adicionais e remoções nos itens (#32)
CREATE TABLE IF NOT EXISTS "Adicional" (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(60) NOT NULL UNIQUE,
    preco DOUBLE PRECISION NOT NULL DEFAULT 0 CHECK (preco >= 0),
    ingrediente_nome VARCHAR(100) REFERENCES "Estoque"(nome) ON UPDATE CASCADE ON DELETE SET NULL,
    quantidade INTEGER NOT NULL DEFAULT 1 CHECK (quantidade > 0),
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);
-- Ingredientes retirados e adicionais escolhidos, separados por "; "
ALTER TABLE "Venda" ADD COLUMN IF NOT EXISTS remocoes VARCHAR(255);
ALTER TABLE "Venda" ADD COLUMN IF NOT EXISTS adicionais VARCHAR(255);

-- Configurações gerais (chave/valor), ex.: taxa fixa de entrega (#35)
CREATE TABLE IF NOT EXISTS "Configuracao" (
    chave VARCHAR(60) PRIMARY KEY,
    valor VARCHAR(255) NOT NULL
);
INSERT INTO "Configuracao" (chave, valor) VALUES ('taxa_entrega', '7.00') ON CONFLICT (chave) DO NOTHING;

-- Crie o primeiro usuário manualmente antes de abrir o sistema.
-- Exemplo apenas para ambiente local de desenvolvimento:
-- INSERT INTO "Login" (usuario, senha) VALUES ('admin', 'troque-esta-senha');
