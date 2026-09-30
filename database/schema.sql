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

-- Crie o primeiro usuário manualmente antes de abrir o sistema.
-- Exemplo apenas para ambiente local de desenvolvimento:
-- INSERT INTO "Login" (usuario, senha) VALUES ('admin', 'troque-esta-senha');
