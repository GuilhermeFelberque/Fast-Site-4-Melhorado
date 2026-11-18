-- ============================================================
-- TURBO GARAGE - banco de dados (MySQL)
--
-- NÃO é obrigatório rodar este script: quando o servidor liga, o Spring
-- cria o banco login_db e executa o schema.sql (CREATE TABLE IF NOT EXISTS),
-- e o usuário admin é criado automaticamente com a senha em BCrypt.
-- Se preferir criar tudo à mão no Workbench, rode este arquivo.
-- ============================================================
CREATE DATABASE IF NOT EXISTS login_db;
USE login_db;

-- Entidade 1: usuários do login (senha SEMPRE em hash BCrypt)
CREATE TABLE IF NOT EXISTS usuarios (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    email      VARCHAR(150) NOT NULL UNIQUE,
    senha_hash VARCHAR(255) NOT NULL,
    perfil     VARCHAR(20)  NOT NULL DEFAULT 'CLIENTE'   -- ADMIN ou CLIENTE
) ENGINE=InnoDB;

-- Entidade 2: cadastros de clientes (6 campos obrigatórios + extras do desafio 3)
CREATE TABLE IF NOT EXISTS cadastros (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    nome         VARCHAR(150) NOT NULL,
    email        VARCHAR(150) NULL,                      -- liga o cadastro ao login
    endereco     VARCHAR(200) NOT NULL,
    cep          VARCHAR(9)   NOT NULL,
    numero       VARCHAR(10)  NOT NULL,
    telefone     VARCHAR(20)  NOT NULL,
    observacao   TEXT,
    data_criacao DATETIME     NOT NULL,
    ativo        TINYINT(1)   NOT NULL DEFAULT 1
) ENGINE=InnoDB;

-- ===== PARTE 4 =====
-- Veículos: cada veículo pertence a um cliente (chave estrangeira)
CREATE TABLE IF NOT EXISTS veiculos (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    placa       VARCHAR(8)   NOT NULL UNIQUE,
    modelo      VARCHAR(100) NOT NULL,
    ano         INT          NOT NULL,
    observacao  VARCHAR(500) NULL,       -- o que o cliente acha que pode ser o problema
    cadastro_id INT          NOT NULL,
    CONSTRAINT fk_veiculo_cliente FOREIGN KEY (cadastro_id) REFERENCES cadastros (id)
) ENGINE=InnoDB;

-- Ordens de serviço: cada ordem pertence a um veículo (chave estrangeira)
CREATE TABLE IF NOT EXISTS ordens_servico (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    veiculo_id    INT           NOT NULL,
    defeito       VARCHAR(500)  NOT NULL,
    valor         DECIMAL(10,2) NOT NULL,
    status        VARCHAR(20)   NOT NULL,   -- ABERTA, EM_ANDAMENTO, CONCLUIDA, CANCELADA
    data_abertura DATETIME      NOT NULL,
    data_conclusao DATETIME     NULL,       -- quando o carro ficou pronto (relatório)
    CONSTRAINT fk_ordem_veiculo FOREIGN KEY (veiculo_id) REFERENCES veiculos (id)
) ENGINE=InnoDB;

-- ===== AGENDAMENTOS =====
-- O cliente pede um dia/horário para levar o carro; o mecânico (admin) confirma ou recusa
CREATE TABLE IF NOT EXISTS agendamentos (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    veiculo_id  INT          NOT NULL,
    data_hora   DATETIME     NOT NULL,
    motivo      VARCHAR(500) NULL,
    status      VARCHAR(20)  NOT NULL,   -- PENDENTE, CONFIRMADO, RECUSADO
    resposta    VARCHAR(300) NULL,       -- mensagem do mecânico para o cliente
    data_pedido DATETIME     NOT NULL,
    CONSTRAINT fk_agendamento_veiculo FOREIGN KEY (veiculo_id) REFERENCES veiculos (id)
) ENGINE=InnoDB;

-- ===== ESTOQUE DE PEÇAS (só o admin vê) =====
CREATE TABLE IF NOT EXISTS pecas (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    codigo         VARCHAR(30)   NOT NULL UNIQUE,   -- código interno da peça
    nome           VARCHAR(120)  NOT NULL,
    categoria      VARCHAR(60)   NULL,              -- ex.: Freios, Motor, Suspensão
    quantidade     INT           NOT NULL,
    estoque_minimo INT           NOT NULL DEFAULT 0, -- abaixo disso, avisa para repor
    preco          DECIMAL(10,2) NOT NULL           -- preço unitário
) ENGINE=InnoDB;

-- Peças de exemplo para a apresentação (opcional: tire os "--" e rode no Workbench)
-- INSERT INTO pecas (codigo, nome, categoria, quantidade, estoque_minimo, preco) VALUES
-- ('FRE-001', 'Pastilha de freio dianteira', 'Freios', 12, 4, 89.90),
-- ('MOT-010', 'Filtro de óleo', 'Motor', 3, 5, 32.50),
-- ('MOT-011', 'Óleo 5W30 sintético 1L', 'Motor', 40, 10, 54.00),
-- ('SUS-020', 'Amortecedor dianteiro', 'Suspensão', 2, 2, 320.00),
-- ('ELE-030', 'Bateria 60Ah', 'Elétrica', 5, 2, 489.00);

-- ===== RECUPERAÇÃO DE SENHA =====
-- Código de 6 dígitos (guardado em hash BCrypt), vale 15 minutos e só uma vez
CREATE TABLE IF NOT EXISTS recuperacao_senha (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    email       VARCHAR(150) NOT NULL,
    codigo_hash VARCHAR(255) NOT NULL,
    expira_em   DATETIME     NOT NULL,
    tentativas  INT          NOT NULL DEFAULT 0,   -- erros de código (bloqueia em 5)
    usado       TINYINT(1)   NOT NULL DEFAULT 0,
    criado_em   DATETIME     NOT NULL
) ENGINE=InnoDB;

-- ============================================================
-- AUDITORIA (passo 6 do roteiro): mostre ao professor que a senha está criptografada
-- ============================================================
-- SELECT id, email, perfil, senha_hash FROM usuarios;
--   -> senha_hash aparece como $2a$10$.... (BCrypt) para TODOS (admin e clientes)
--
-- SELECT * FROM cadastros;
--
-- Ver cada cliente junto com o login dele:
-- SELECT c.id, c.nome, c.email, u.perfil, u.senha_hash
--   FROM cadastros c JOIN usuarios u ON u.email = c.email;

--
-- ===== PARTE 4: validação de persistência (passo 5 do roteiro) =====
-- Rode depois de cadastrar, editar e excluir nas telas:
-- SELECT * FROM veiculos;
-- SELECT * FROM ordens_servico;
--
-- Ordens com o veículo e o cliente (mostra as chaves estrangeiras funcionando):
-- SELECT o.id AS os, v.placa, v.modelo, c.nome AS cliente, o.defeito, o.valor, o.status
--   FROM ordens_servico o
--   JOIN veiculos v ON v.id = o.veiculo_id
--   JOIN cadastros c ON c.id = v.cadastro_id;

--
-- Agendamentos com veículo e cliente:
-- SELECT a.id, a.data_hora, a.status, v.placa, c.nome, c.telefone, a.motivo, a.resposta
--   FROM agendamentos a
--   JOIN veiculos v ON v.id = a.veiculo_id
--   JOIN cadastros c ON c.id = v.cadastro_id
--  ORDER BY a.data_hora;

--
-- Estoque: peças abaixo do mínimo e valor total parado em estoque
-- SELECT codigo, nome, quantidade, estoque_minimo FROM pecas WHERE quantidade <= estoque_minimo;
-- SELECT SUM(quantidade * preco) AS valor_em_estoque FROM pecas;
