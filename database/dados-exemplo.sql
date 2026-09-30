-- BLK Burguer - dados de exemplo para desenvolvimento e apresentação
-- Execute depois de database/schema.sql em um banco VAZIO.
-- Não cria usuários: cadastre o seu login manualmente.

BEGIN;

-- Estoque (custo unitário e quantidade atual)
INSERT INTO "Estoque" (nome, valor, "Quantidade", tipo) VALUES
    ('Pão brioche',                1.20,  80, 'Ingrediente'),
    ('Blend bovino 150g',          6.50,  60, 'Ingrediente'),
    ('Queijo cheddar (fatia)',     0.90, 120, 'Ingrediente'),
    ('Bacon (fatia)',              1.10,  90, 'Ingrediente'),
    ('Alface (folha)',             0.15, 150, 'Ingrediente'),
    ('Tomate (rodela)',            0.20, 150, 'Ingrediente'),
    ('Cebola caramelizada (porção)', 0.60,  40, 'Ingrediente'),
    ('Molho da casa (porção)',     0.40, 100, 'Ingrediente'),
    ('Frango empanado',            4.20,  30, 'Ingrediente'),
    ('Batata (porção 200g)',       1.80,  50, 'Ingrediente'),
    ('Coca-Cola lata',             3.20,  48, 'Bebida'),
    ('Guaraná lata',               2.90,  36, 'Bebida'),
    ('Água mineral',               1.20,  24, 'Bebida'),
    ('Suco natural 300ml',         3.50,   8, 'Bebida'),
    ('Onion rings (porção)',       4.00,   6, 'Acompanhamento'),
    ('Nuggets (10 un.)',           5.00,  15, 'Acompanhamento');

-- Produtos do cardápio
INSERT INTO "Produto" (nome, tipo, preco) VALUES
    ('BLK Clássico',   'Lanche',         28.90),
    ('BLK Bacon',      'Lanche',         34.90),
    ('BLK Duplo',      'Lanche',         42.90),
    ('BLK Chicken',    'Lanche',         29.90),
    ('Batata frita',   'Acompanhamento', 14.90),
    ('Onion rings',    'Acompanhamento', 19.90),
    ('Nuggets',        'Acompanhamento', 18.90),
    ('Coca-Cola',      'Bebida',          7.00),
    ('Guaraná',        'Bebida',          6.50),
    ('Água',           'Bebida',          4.00),
    ('Suco natural',   'Bebida',          9.90);

-- Ficha técnica (quantidade de cada item por unidade do produto)
INSERT INTO "ProdutoIngrediente" (produto_id, ingrediente_nome, quantidade)
SELECT p.id, f.ingrediente, f.qtd
FROM (VALUES
    ('BLK Clássico', 'Pão brioche', 1), ('BLK Clássico', 'Blend bovino 150g', 1),
    ('BLK Clássico', 'Queijo cheddar (fatia)', 2), ('BLK Clássico', 'Molho da casa (porção)', 1),
    ('BLK Clássico', 'Alface (folha)', 1), ('BLK Clássico', 'Tomate (rodela)', 1),
    ('BLK Bacon', 'Pão brioche', 1), ('BLK Bacon', 'Blend bovino 150g', 1),
    ('BLK Bacon', 'Queijo cheddar (fatia)', 2), ('BLK Bacon', 'Bacon (fatia)', 3),
    ('BLK Bacon', 'Molho da casa (porção)', 1),
    ('BLK Duplo', 'Pão brioche', 1), ('BLK Duplo', 'Blend bovino 150g', 2),
    ('BLK Duplo', 'Queijo cheddar (fatia)', 4), ('BLK Duplo', 'Bacon (fatia)', 2),
    ('BLK Duplo', 'Cebola caramelizada (porção)', 1), ('BLK Duplo', 'Molho da casa (porção)', 1),
    ('BLK Chicken', 'Pão brioche', 1), ('BLK Chicken', 'Frango empanado', 1),
    ('BLK Chicken', 'Alface (folha)', 2), ('BLK Chicken', 'Tomate (rodela)', 2),
    ('BLK Chicken', 'Molho da casa (porção)', 1),
    ('Batata frita', 'Batata (porção 200g)', 1),
    ('Onion rings', 'Onion rings (porção)', 1),
    ('Nuggets', 'Nuggets (10 un.)', 1),
    ('Coca-Cola', 'Coca-Cola lata', 1),
    ('Guaraná', 'Guaraná lata', 1),
    ('Água', 'Água mineral', 1),
    ('Suco natural', 'Suco natural 300ml', 1)
) AS f(produto, ingrediente, qtd)
JOIN "Produto" p ON p.nome = f.produto;

-- Vendas dos últimos dias (o estoque acima já representa o saldo após elas)
CREATE TEMP TABLE venda_exemplo (pedido INT, dias_atras INT, hora TIME, forma TEXT, recebido NUMERIC, produto TEXT, qtd INT);
INSERT INTO venda_exemplo VALUES
    (1, 4, '12:10', 'PIX',      NULL, 'BLK Clássico', 2), (1, 4, '12:10', 'PIX',      NULL, 'Coca-Cola', 2),
    (2, 4, '19:45', 'CREDITO',  NULL, 'BLK Duplo', 1),    (2, 4, '19:45', 'CREDITO',  NULL, 'Batata frita', 1), (2, 4, '19:45', 'CREDITO', NULL, 'Guaraná', 1),
    (3, 3, '13:05', 'DINHEIRO', 50,   'BLK Bacon', 1),    (3, 3, '13:05', 'DINHEIRO', 50,   'Água', 1),
    (4, 3, '20:30', 'DEBITO',   NULL, 'BLK Chicken', 2),  (4, 3, '20:30', 'DEBITO',   NULL, 'Nuggets', 1),  (4, 3, '20:30', 'DEBITO', NULL, 'Suco natural', 2),
    (5, 2, '18:50', 'PIX',      NULL, 'BLK Clássico', 1), (5, 2, '18:50', 'PIX',      NULL, 'Onion rings', 1), (5, 2, '18:50', 'PIX', NULL, 'Coca-Cola', 1),
    (6, 1, '12:40', 'DINHEIRO', 100,  'BLK Duplo', 2),    (6, 1, '12:40', 'DINHEIRO', 100,  'Coca-Cola', 2),
    (7, 1, '21:15', 'CREDITO',  NULL, 'BLK Bacon', 2),    (7, 1, '21:15', 'CREDITO',  NULL, 'Batata frita', 2),
    (8, 0, '11:55', 'PIX',      NULL, 'BLK Clássico', 1), (8, 0, '11:55', 'PIX',      NULL, 'Guaraná', 1),
    (9, 0, '12:20', 'DEBITO',   NULL, 'BLK Chicken', 1),  (9, 0, '12:20', 'DEBITO',   NULL, 'Batata frita', 1), (9, 0, '12:20', 'DEBITO', NULL, 'Coca-Cola', 1);

CREATE TEMP TABLE pedido_exemplo AS
SELECT ve.pedido, (CURRENT_DATE - ve.dias_atras) + ve.hora AS data, ve.forma, ve.recebido,
       SUM(p.preco * ve.qtd) AS total
FROM venda_exemplo ve JOIN "Produto" p ON p.nome = ve.produto
GROUP BY ve.pedido, ve.dias_atras, ve.hora, ve.forma, ve.recebido;

INSERT INTO "Pedido" (id, data_pedido, total, forma_pagamento, valor_recebido, troco)
SELECT pedido, data, total, forma, COALESCE(recebido, total), COALESCE(recebido, total) - total
FROM pedido_exemplo ORDER BY pedido;
SELECT setval(pg_get_serial_sequence('"Pedido"', 'id'), (SELECT MAX(id) FROM "Pedido"));

INSERT INTO "Venda" (pedido_id, produto, quantidade, valor, total, data_venda)
SELECT ve.pedido, ve.produto, ve.qtd, p.preco, p.preco * ve.qtd, pe.data
FROM venda_exemplo ve
JOIN "Produto" p ON p.nome = ve.produto
JOIN pedido_exemplo pe ON pe.pedido = ve.pedido;

-- Adicionais cobrados à parte (e o que consomem do estoque)
INSERT INTO "Adicional" (nome, preco, ingrediente_nome, quantidade) VALUES
    ('Bacon extra',          4.00, 'Bacon (fatia)', 2),
    ('Cheddar extra',        3.00, 'Queijo cheddar (fatia)', 2),
    ('Carne extra',          9.00, 'Blend bovino 150g', 1),
    ('Cebola caramelizada',  3.50, 'Cebola caramelizada (porção)', 1),
    ('Molho extra',          2.00, 'Molho da casa (porção)', 1)
ON CONFLICT (nome) DO NOTHING;

-- Mesas do salão
INSERT INTO "Mesa" (numero, lugares)
SELECT n, CASE WHEN n IN (5, 10) THEN 6 WHEN n > 10 THEN 2 ELSE 4 END FROM generate_series(1, 12) n
ON CONFLICT (numero) DO NOTHING;

-- Clientes de exemplo
INSERT INTO "Cliente" (nome, telefone, endereco)
SELECT * FROM (VALUES
    ('Ana Souza', '(11) 98888-1111', 'Rua das Flores, 120 - Centro'),
    ('Bruno Lima', '(11) 97777-2222', 'Av. Brasil, 845 - apto 32'),
    ('Carla Mendes', '(11) 96666-3333', 'Rua Sete de Setembro, 77')
) AS c(nome, telefone, endereco)
WHERE NOT EXISTS (SELECT 1 FROM "Cliente");

COMMIT;
