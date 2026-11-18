# 🔧 TURBO GARAGE

**Sistema web para oficina mecânica: clientes, veículos, agendamentos, ordens de serviço e estoque**

Projeto Integrador · Curso Técnico SENAI · Web & Backend

**Alunos:** Guilherme Felberque e Danilo Dorella

---

## Sumário
1. [Sobre o projeto](#1-sobre-o-projeto)
2. [Tecnologias](#2-tecnologias)
3. [Como rodar](#3-como-rodar)
4. [Telas do sistema](#4-telas-do-sistema)
5. [Quem pode fazer o quê](#5-quem-pode-fazer-o-quê)
6. [Fluxos principais](#6-fluxos-principais)
7. [Arquitetura em camadas](#7-arquitetura-em-camadas)
8. [Banco de dados](#8-banco-de-dados)
9. [Endpoints da API](#9-endpoints-da-api)
10. [Segurança](#10-segurança)
11. [Requisitos atendidos](#11-requisitos-atendidos)
12. [Roteiro de apresentação](#12-roteiro-de-apresentação)
13. [Observações técnicas](#13-observações-técnicas)

---

## 1. Sobre o projeto

A **Turbo Garage** é o sistema web de uma oficina mecânica.

- **Cliente:**
  - cria a conta;
  - cadastra os carros dele, dizendo qual pode ser o problema;
  - **agenda uma visita** e **pede pré-atendimento**;
  - acompanha o conserto por etapas;
  - vê o **relatório** quando o carro fica pronto.
- **Administrador (o mecânico/a oficina):**
  - consulta os clientes;
  - gerencia veículos e ordens de serviço;
  - **confirma ou recusa agendamentos**;
  - controla o **estoque de peças**;
  - emite relatórios.

**Identidade visual:** flat design de alto contraste, em preto, amarelo e branco, inspirado em faixas de segurança e no universo automotivo.

---

## 2. Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 25 |
| Framework | Spring Boot 4.1.1 (Spring Web MVC) |
| Acesso ao banco | Spring JDBC (`JdbcTemplate`) |
| Banco de dados | MySQL 8 (driver `mysql-connector-j`) |
| Criptografia | BCrypt (`jbcrypt` 0.4) |
| Validação | Jakarta Bean Validation (`spring-boot-starter-validation`) |
| Email | `spring-boot-starter-mail` (código de recuperação de senha) |
| Front-end | HTML5, CSS3 e JavaScript puro (`fetch()`) |
| API pública | [ViaCEP](https://viacep.com.br) |
| Build | Maven |

---

## 3. Como rodar

### Pré-requisitos
- Java 25 (JDK)
- MySQL 8 instalado e ligado
- IntelliJ IDEA ou VS Code (com o *Extension Pack for Java*)

### Passo a passo
1. **Abra o projeto** pela pasta `turbogarage`, a que tem o `pom.xml`, e **atualize o Maven** para ele baixar as dependências.
2. **Senha do MySQL** em `src/main/resources/application.properties`:
   ```properties
   spring.datasource.username=root
   spring.datasource.password=SUA_SENHA_DO_MYSQL
   ```
3. **Administrador** no mesmo arquivo. Login padrão:
   ```properties
   app.admin.email=admin@email.com
   app.admin.senha=minhaSenha123
   ```
4. *(Opcional)* **Email de verdade** para o código de recuperação de senha: tire os `#` das linhas `spring.mail.*` e use um Gmail com **senha de app** (https://myaccount.google.com/apppasswords). Sem isso, o código aparece no **console**.
5. **Rode** a classe `DemoApplication.java`.
6. **Acesse** http://localhost:8080

> Não precisa criar o banco nem as tabelas: quando o servidor liga, ele cria o banco `login_db`, as 7 tabelas e a conta do administrador.
>
> Se aparecer "Não é possível acessar esse site" no navegador, o servidor não ligou. Veja o erro no console: o mais comum é a senha do MySQL errada (`Access denied`).

---

## 4. Telas do sistema

| Tela | Arquivo | Quem acessa | O que faz |
|---|---|---|---|
| Login | `index.html` | Todos | Entrada com email e senha; mensagem de erro; link para recuperar acesso |
| Recuperar acesso | `recuperar.html` | Todos | **Esqueci a senha:** código de 6 números + senha nova. **Esqueci o email:** telefone + CEP |
| Cadastro | `cadastro.html` | Todos | Nome, endereço, CEP (com ViaCEP), número e telefone, mais email e senha |
| Boas-vindas | `home.html` | Logados | Imagem em tela cheia e atalhos de acordo com o perfil |
| Veículos | `veiculos.html` | Admin e cliente | **Admin:** CRUD completo. **Cliente:** cadastra o próprio carro (com observação) e vê "Meus veículos cadastrados" |
| Meus veículos | `meus-veiculos.html` | Cliente | Carros do cliente, pedido de pré-atendimento e acompanhamento por etapas |
| Agendamentos | `agendamentos.html` | Admin e cliente | **Cliente:** pede dia e horário. **Admin:** confirma ou recusa, com mensagem |
| Clientes | `cadastros.html` | Admin | Lista, busca por ID, ficha e exclusão |
| Ordens de Serviço | `ordens.html` | Admin | CRUD, filtro por status, total em R$ e botão Relatório nas concluídas |
| Relatório da OS | `relatorio.html?id=N` | Admin e cliente | Relatório de ordem **concluída**, para imprimir ou salvar em PDF |
| Estoque | `estoque.html` | Admin | Peças, entrada e saída, aviso "Repor", resumo e exportar Excel |

Em todas as telas logadas há um **timer de inatividade** (⏱) no topo.

---

## 5. Quem pode fazer o quê

| Ação | Visitante | Cliente | Admin |
|---|:---:|:---:|:---:|
| Criar conta | ✅ | | |
| Recuperar senha ou email | ✅ | ✅ | ✅ |
| Fazer login e ver boas-vindas | | ✅ | ✅ |
| Cadastrar o **próprio** veículo e ver os seus | | ✅ | |
| Pedir pré-atendimento e acompanhar as etapas | | ✅ | |
| Pedir agendamento de visita | | ✅ | |
| Ver o relatório dos **próprios** serviços concluídos | | ✅ | |
| Ver e excluir clientes | | | ✅ |
| CRUD completo de veículos e ordens de serviço | | | ✅ |
| Confirmar ou recusar agendamentos | | | ✅ |
| Controlar o estoque de peças | | | ✅ |
| Relatório de qualquer serviço concluído | | | ✅ |

**O administrador** é o email de `app.admin.email`. Só existe um: se você trocar o email no arquivo, o antigo perde o acesso. As permissões são conferidas **no Java** (`AdminInterceptor` e Services), não só escondendo botões.

---

## 6. Fluxos principais

### Do agendamento ao relatório
```
Cliente cadastra o carro (com observação do problema)
   → pede AGENDAMENTO (dia/horário)       → admin CONFIRMA ou RECUSA (com mensagem)
   → pede PRÉ-ATENDIMENTO (descreve o defeito)
        → ordem nasce como SOLICITADA ("Pré-atendimento", valor a definir)
        → admin define o valor e muda o status: ABERTA → EM ANDAMENTO → CONCLUÍDA
   → cliente acompanha a barra de etapas em "Meus veículos"
   → CONCLUÍDA: o botão "Relatório" aparece para o admin e para o cliente
```

### Status
| O quê | Status |
|---|---|
| Ordem de serviço | `SOLICITADA` (pré-atendimento) → `ABERTA` → `EM_ANDAMENTO` → `CONCLUIDA` (ou `CANCELADA`) |
| Agendamento | `PENDENTE` → `CONFIRMADO` ou `RECUSADO` |

### Onde vai a confirmação de um agendamento?
O botão **Confirmar** do admin grava no banco (`agendamentos.status = CONFIRMADO` e `agendamentos.resposta = mensagem`). O cliente vê em **Agendamentos → Meus agendamentos**, com a etiqueta verde e a mensagem do mecânico. Não é enviado email nem notificação.

---

## 7. Arquitetura em camadas

```
TurboGarage/
├── README.md
└── turbogarage/
    ├── pom.xml
    ├── banco.sql                     → script do banco + consultas para a apresentação
    └── src/main/
        ├── java/com/example/demo/
        │   ├── DemoApplication.java  → inicia o servidor
        │   ├── model/                → Usuario, Cadastro, Veiculo, OrdemServico, Agendamento, Peca
        │   ├── dto/                  → dados que chegam das telas (com validação) e respostas montadas
        │   ├── repository/           → SQL com JdbcTemplate (uma classe por tabela)
        │   ├── service/              → regras de negócio + EmailService
        │   ├── controller/           → endpoints REST + ErroHandler (erros globais)
        │   ├── config/               → AdminInterceptor (permissões), WebConfig, DadosIniciais
        │   └── util/SenhaUtil.java   → criptografia BCrypt
        └── resources/
            ├── application.properties
            ├── schema.sql            → cria as tabelas sozinho
            └── static/               → telas (HTML, css/style.css, js/sessao.js, js/comum.js, img/hero.svg)
```

**Caminho de uma requisição:**
```
Tela (fetch + JSON) → AdminInterceptor (pode?) → Controller (DTO + @Valid) → Service (regras) → Repository (SQL) → MySQL
                                     qualquer erro → ErroHandler → { "mensagem": "..." } na tela
```

**Arquivos JavaScript compartilhados:**
- `js/sessao.js`: login, permissões na tela (`data-so-admin` e `data-so-cliente`), botão Sair e **timer de inatividade**.
- `js/comum.js`: chamadas à API, avisos (toast), janela de confirmação, exportação CSV, formatação em R$ e busca sem acento.

---

## 8. Banco de dados

### Tabelas
| Tabela | O que guarda | Colunas principais |
|---|---|---|
| `usuarios` | Contas de login | email (único), senha_hash (BCrypt), perfil (`ADMIN`/`CLIENTE`) |
| `cadastros` | Dados dos clientes | nome, email, endereco, cep, numero, telefone, data_criacao, ativo (soft delete) |
| `veiculos` | Carros | placa (única), modelo, ano, **observacao**, cadastro_id (FK) |
| `ordens_servico` | Serviços | veiculo_id (FK), defeito, valor DECIMAL(10,2), status, data_abertura, **data_conclusao** |
| `agendamentos` | Visitas marcadas | veiculo_id (FK), data_hora, motivo, status, resposta (do mecânico), data_pedido |
| `pecas` | Estoque | codigo (único), nome, categoria, quantidade, estoque_minimo, preco |
| `recuperacao_senha` | Códigos de recuperação | email, codigo_hash (BCrypt), expira_em, tentativas, usado |

### Relacionamentos (chaves estrangeiras)
```
cadastros (1) ──< (N) veiculos (1) ──< (N) ordens_servico
                           │
                           └──────< (N) agendamentos
```
- Um cliente tem vários veículos; um veículo tem várias ordens e vários agendamentos.
- Um veículo com ordens **ou** agendamentos não pode ser excluído.
- `usuarios` e `cadastros` se ligam pelo **email**.

### Consultas prontas para a apresentação (no fim do `banco.sql`)
```sql
SELECT email, perfil, senha_hash FROM usuarios;                 -- senhas em hash BCrypt
SELECT * FROM veiculos;  SELECT * FROM ordens_servico;           -- persistência do CRUD
SELECT id, data_hora, status, resposta FROM agendamentos;        -- confirmações do mecânico
SELECT codigo, nome, quantidade, estoque_minimo FROM pecas
 WHERE quantidade <= estoque_minimo;                             -- peças para repor
```
O `banco.sql` também traz 5 peças de exemplo, comentadas, para preencher o estoque antes da apresentação.

---

## 9. Endpoints da API

### Autenticação e recuperação (públicas)
| Método | Rota | Resposta |
|---|---|---|
| POST | `/api/login` | 200 ou 401 "Email ou senha inválidos" |
| GET | `/api/usuario` | Quem está logado `{ email, nome, admin }` |
| POST | `/api/logout` | Encerra a sessão |
| POST | `/api/recuperar/senha/codigo` | Sempre a mesma mensagem (não revela se o email existe) |
| POST | `/api/recuperar/senha/nova` | 200 "Senha alterada com sucesso!"; 400 código errado, expirado ou bloqueado |
| POST | `/api/recuperar/email` | 200 com o email escondido (`jo***@gmail.com`); 404 se telefone + CEP não batem |

### Área do cliente (logado)
| Método | Rota | Resposta |
|---|---|---|
| GET | `/api/cliente/veiculos` | Os veículos do cliente, cada um com as suas ordens |
| POST | `/api/cliente/pre-atendimento` | 201: ordem criada como `SOLICITADA` |
| GET | `/api/cliente/agendamentos` | Os agendamentos do cliente |
| POST | `/api/cliente/agendamentos` | 201: agendamento `PENDENTE` |
| GET | `/api/cliente/ordens/{id}/relatorio` | Relatório de serviço **concluído** do próprio carro (outro carro → 403) |

### Cadastros e veículos
| Método | Rota | Acesso | Resposta |
|---|---|---|---|
| POST | `/api/cadastros` | Todos | 201 "Cadastro realizado com sucesso!" |
| GET | `/api/cadastros` · `/api/cadastros/{id}` | Admin | Lista · um (404) |
| DELETE | `/api/cadastros/{id}` | Admin | Soft delete + remove o login |
| POST | `/api/veiculos` | Admin e cliente | 201 (o cliente sempre cadastra no nome dele) |
| GET | `/api/veiculos` · `/api/veiculos/{id}` | Admin | Lista · um |
| PUT · DELETE | `/api/veiculos/{id}` | Admin | "Registro atualizado" · "Registro excluído" (409 se tem ordens ou agendamentos) |

### Ordens de serviço, agendamentos e estoque (só admin)
| Método | Rota | Resposta |
|---|---|---|
| GET · POST | `/api/ordens` | Lista · 201 |
| GET · PUT · DELETE | `/api/ordens/{id}` | Um · atualizado · excluído |
| GET | `/api/ordens/{id}/relatorio` | Relatório (409 se não está concluída) |
| GET | `/api/agendamentos` | Todos os agendamentos |
| PUT | `/api/agendamentos/{id}/validacao` | `{ status: CONFIRMADO/RECUSADO, resposta }` |
| GET · POST | `/api/pecas` | Lista · 201 |
| GET · PUT · DELETE | `/api/pecas/{id}` | Uma · atualizada · excluída |
| POST | `/api/pecas/{id}/movimento` | `{ tipo: ENTRADA/SAIDA, quantidade }` (409 se o estoque ficaria negativo) |

### Códigos de resposta (`ErroHandler`)
| Código | Quando |
|---|---|
| 200 / 201 | Deu certo / registro criado |
| 400 | Campo vazio ou inválido, JSON mal formado, código de recuperação errado |
| 401 | Não está logado |
| 403 | Logado, mas sem permissão (ou o carro não é seu) |
| 404 | Registro não encontrado |
| 409 | Duplicado (email, placa, código de peça), horário ocupado, estoque insuficiente, relatório de ordem não concluída |
| 500 | Erro inesperado (a mensagem mostra o motivo) |

---

## 10. Segurança

| Proteção | Como |
|---|---|
| Senhas | Hash **BCrypt** com salt aleatório; nunca em texto puro |
| Login | Mensagem genérica "Email ou senha inválidos" |
| Sessão | `HttpSession` no servidor (30 min) + **timer de inatividade de 10 min** na tela, que reinicia a cada movimento e faz logout ao zerar |
| Permissões | `AdminInterceptor` bloqueia as rotas do admin no Java (401/403), não só na tela |
| Dados do cliente | O Java filtra tudo pelo email da sessão: o cliente só vê e só mexe nos carros, ordens, agendamentos e relatórios **dele** |
| Recuperação de senha | Código sorteado com `SecureRandom`, guardado em hash BCrypt, vale **15 min**, **uma vez só**, bloqueia em **5 erros**, não revela se o email existe |
| SQL Injection | Todas as consultas usam parâmetros `?` (PreparedStatement) |
| XSS | Dados mostrados com `textContent`, nunca com `innerHTML` |
| Estoque | O próprio `UPDATE` impede quantidade negativa (`quantidade + ? >= 0`) |

---

## 11. Requisitos atendidos

### Parte 3: Integração end-to-end
- [x] Nome do produto e dos alunos no cabeçalho
- [x] Estilo visual de alto contraste (flat design)
- [x] Tela de login com mensagem de erro visual
- [x] Tela de boas-vindas com imagem em tela cheia
- [x] Cadastro e consulta de registros
- [x] Mensagem "Cadastro realizado com sucesso!"
- [x] Arquitetura Controller / Service / Repository / Model
- [x] Senha criptografada (BCrypt) e conexão com MySQL
- [x] ⭐ Máscaras de CEP e telefone · ⭐ ViaCEP · ⭐ `data_criacao` e soft delete

### Parte 4: APIs públicas e Multi-CRUD
- [x] API pública (ViaCEP)
- [x] CRUD completo de **Veículos** e **Ordens de Serviço**, com busca, edição e exclusão com confirmação
- [x] Avisos "Registro salvo / atualizado / excluído" e menu de navegação
- [x] ⭐ Relacionamento entre entidades · ⭐ Exportação para Excel · ⭐ Tratamento global de exceções

### Funcionalidades extras (pedidas pelo professor)
- [x] Cliente vê os veículos dele e pede pré-atendimento de ordem de serviço
- [x] Observação do possível problema no cadastro do veículo
- [x] Agendamento pelo cliente, validado pelo mecânico na conta dele
- [x] Aba de estoque de peças no admin
- [x] Relatório da ordem de serviço só do carro arrumado
- [x] Timer de inatividade que reinicia ao mexer no site
- [x] Recuperação de senha e de email

---

## 12. Roteiro de apresentação

**Antes:** MySQL ligado, servidor rodando, Workbench aberto. Para mostrar o timer expirando sem esperar 10 minutos, troque `MINUTOS_INATIVIDADE` para `1` em `js/sessao.js`.

1. **Login com erro:** senha errada → mensagem vermelha.
2. **Cadastro com ViaCEP:** "Cadastre-se" → digite um CEP (`01001000`) e mostre a rua aparecendo sozinha → "Cadastro realizado com sucesso!".
3. **Cliente cadastra o carro:** entre como cliente → **Veículos** → cadastre com a observação "barulho ao frear" → o carro aparece em "Meus veículos cadastrados".
4. **Agendamento:** **Agendamentos** → escolha carro, dia e horário → "Aguardando confirmação".
5. **Pré-atendimento:** **Meus veículos** → descreva o defeito → envie.
6. **Mecânico confirma:** saia e entre como admin → **Agendamentos** → escreva "Pode trazer às 9h" → **Confirmar**.
7. **Ordem de serviço:** **Ordens de Serviço** → filtre "Pré-atendimento" → **Editar** → coloque o valor e mude para **Em andamento** → depois para **Concluída**.
8. **Relatório:** clique em **Relatório** → **Imprimir / salvar PDF**.
9. **Cliente acompanha:** entre como cliente → **Meus veículos** (barra de etapas e "Ver relatório") e **Agendamentos** (Confirmado + mensagem).
10. **Estoque:** como admin → **Estoque** → cadastre peças, dê entrada e saída, tente tirar mais do que tem ("Estoque insuficiente") e marque "Só o que precisa repor".
11. **CRUD da Parte 4:** em **Veículos**, edite, busque, exclua com confirmação e exporte para o Excel.
12. **Recuperar senha:** no login → "Esqueci minha senha ou email" → código no console → senha nova → entre com ela. Mostre também a aba "Esqueci o email".
13. **Timer:** mostre o ⏱ no topo, mexa o mouse e veja ele reiniciar; espere zerar para mostrar o logout automático.
14. **Banco:** no Workbench, rode as consultas da seção 8 (senhas em hash, agendamento `CONFIRMADO`, peças para repor).

---

## 13. Observações técnicas

- **Por que Spring JDBC e não JPA?** Quando o projeto começou, o MySQL usado era antigo, e o JPA do Spring Boot 4 exige MySQL 8. O enunciado aceita "Spring Data JPA **ou** Spring JDBC". Mantivemos o JDBC: nele nós mesmos escrevemos o SQL. O projeto roda no MySQL 8.
- **Tabelas antigas:** se o banco veio de uma versão anterior, a classe `DadosIniciais` adiciona sozinha as colunas que faltarem (`perfil`, `email`, `observacao`, `data_conclusao`).
- **ViaCEP precisa de internet.** Sem conexão, o endereço é digitado à mão.
- **Email:** sem `spring.mail.*` configurado, o código de recuperação aparece no console. Se o email estiver mal configurado, o sistema não trava e também mostra no console.
- **Regras de negócio importantes:**
  - placa e código de peça não repetem;
  - agendamento só em data futura, de segunda a sábado, das 8h às 17h, sem dois agendamentos no mesmo horário;
  - só ordem **concluída** gera relatório;
  - a data de conclusão é gravada quando o status vira Concluída;
  - um cliente excluído perde o login.
- **Limitações conhecidas** (melhorias futuras):
  - o CEP inexistente só é avisado, não bloqueado;
  - a confirmação do agendamento não manda email ao cliente;
  - as peças do estoque ainda não são ligadas às ordens de serviço.
