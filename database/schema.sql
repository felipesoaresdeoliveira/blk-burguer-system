-- BLK Burguer - schema mínimo reproduzível
-- PostgreSQL

CREATE TABLE IF NOT EXISTS "Login" (
    "Id" SERIAL PRIMARY KEY,
    usuario VARCHAR(25) NOT NULL UNIQUE,
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

-- Crie o primeiro usuário manualmente antes de abrir o sistema.
-- Exemplo apenas para ambiente local de desenvolvimento:
-- INSERT INTO "Login" (usuario, senha) VALUES ('admin', 'troque-esta-senha');
