# Turbo Garage: o que mudou na versão 2.0

Alunos: Guilherme Felberque e Danilo Dorella

Comparado com a versão apresentada (Partes 3 e 4):

### Funcionalidades novas
| # | O que | Para quem | Tela |
|---|---|---|---|
| 1 | **Meus veículos + pré-atendimento:** o cliente vê os carros dele, descreve o problema e pede um pré-atendimento, que vira uma ordem de serviço com status "Pré-atendimento". Depois acompanha cada serviço por etapas | Cliente | `meus-veiculos.html` |
| 2 | **Observação no cadastro do veículo** ("qual pode ser o problema?") e lista **"Meus veículos cadastrados"** ao lado do formulário | Cliente (o admin também vê a observação) | `veiculos.html` |
| 3 | **Agendamento de visita:** o cliente escolhe carro, dia e horário; o mecânico (admin) **confirma ou recusa**, com mensagem | Cliente e admin | `agendamentos.html` |
| 4 | **Estoque de peças:** cadastro, entrada e saída, aviso de **"Repor"**, resumo com valor total e exportação para o Excel | Admin | `estoque.html` |
| 5 | **Relatório da ordem de serviço**, só de carro **já arrumado** (status Concluída), pronto para imprimir ou salvar em PDF | Admin e cliente (só os dele) | `relatorio.html` |
| 6 | **Timer de inatividade** em todas as telas logadas: qualquer movimento reinicia; ao zerar, faz logout sozinho | Todos os logados | todas |
| 7 | **Recuperar senha** (código de 6 números) e **recuperar email** (telefone + CEP) | Todos | `recuperar.html` |

### Mudanças no que já existia
- **MySQL 8:** troca do driver antigo `mysql-connector-java 5.1.49` pelo `mysql-connector-j`, e `allowPublicKeyRetrieval=true` na URL de conexão.
- **Cadastro do cliente:** o campo **Observação** saiu do formulário. A coluna continua no banco, mas vazia.
- **Ordens de serviço:**
  - novo status **SOLICITADA** ("Pré-atendimento"), com o valor "A definir";
  - filtro próprio para os pedidos de pré-atendimento;
  - botão **Relatório** nas ordens concluídas;
  - a **data de conclusão** é gravada sozinha.
- **Veículos:** um veículo com **agendamento** também não pode mais ser excluído.
- **Menu:** novos itens **Meus veículos**, **Agendamentos** e **Estoque**, cada um aparecendo só para quem pode usar.
- **Login:** link "Esqueci minha senha ou email" e aviso de "sessão expirada por inatividade".
- **Janela de confirmação:** o texto do botão agora muda conforme a ação ("Sim, excluir", "Sim, recusar").
- **Tabelas largas:** rolam dentro do cartão, sem passar da borda.

### Banco de dados
- **Tabelas novas:** `agendamentos`, `pecas` e `recuperacao_senha`.
- **Colunas novas:** `veiculos.observacao` e `ordens_servico.data_conclusao`.
- Tudo é criado **sozinho** quando o servidor liga: o `schema.sql` cria as tabelas, e a classe `DadosIniciais` adiciona as colunas que faltarem em tabelas antigas.

### Dependência nova
- `spring-boot-starter-mail`, para enviar o código de recuperação de senha por email.

### Arquivos novos
- **Telas:** `meus-veiculos.html`, `agendamentos.html`, `estoque.html`, `relatorio.html` e `recuperar.html`.
- **Java:**
  - Model: `Agendamento` e `Peca`;
  - DTO: `MeuVeiculo`, `PreAtendimentoRequest`, `AgendamentoRequest`, `ValidacaoRequest`, `PecaRequest`, `MovimentoRequest`, `RelatorioOS`, `PedidoCodigoRequest`, `NovaSenhaRequest` e `RecuperarEmailRequest`;
  - Repository: `AgendamentoRepository`, `PecaRepository` e `RecuperacaoRepository`;
  - Service: `AreaClienteService`, `AgendamentoService`, `PecaService`, `RelatorioService`, `RecuperacaoService` e `EmailService`;
  - Controller: `AreaClienteController`, `AgendamentoController`, `PecaController` e `RecuperacaoController`.
