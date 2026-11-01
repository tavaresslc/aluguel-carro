## Histórias de Usuário

### 1. Cadastro e autenticação

| ID   | História do usuário                                                                                    | Prioridade |
| ---- | ------------------------------------------------------------------------------------------------------ | ---------- |
| US01 | Como visitante, quero realizar meu cadastro para ter acesso ao sistema.                                | Alta       |
| US02 | Como usuário cadastrado, quero realizar login com usuário e senha para acessar minhas funcionalidades. | Alta       |
| US03 | Como usuário autenticado, quero encerrar minha sessão para garantir a segurança da minha conta.        | Média      |
| US04 | Como cliente, quero atualizar meus dados pessoais para manter minhas informações atualizadas.          | Média      |

### 2. Gerenciamento de pedidos

| ID   | História do usuário                                                                                | Prioridade |
| ---- | -------------------------------------------------------------------------------------------------- | ---------- |
| US05 | Como cliente, quero cadastrar um pedido de aluguel para solicitar um automóvel.                    | Alta       |
| US06 | Como cliente, quero consultar meus pedidos para acompanhar suas situações.                         | Alta       |
| US07 | Como cliente, quero modificar um pedido para corrigir ou atualizar suas informações.               | Alta       |
| US08 | Como cliente, quero cancelar um pedido para desistir de uma solicitação de aluguel.                | Alta       |
| US09 | Como agente, quero consultar os pedidos cadastrados para analisar as solicitações recebidas.       | Alta       |
| US10 | Como agente, quero modificar informações de um pedido para atualizar seus dados durante a análise. | Alta       |

### 3. Análise financeira

| ID   | História do usuário                                                                                                    | Prioridade |
| ---- | ---------------------------------------------------------------------------------------------------------------------- | ---------- |
| US11 | Como agente, quero visualizar os dados pessoais e financeiros do contratante para avaliar sua capacidade financeira.   | Alta       |
| US12 | Como agente, quero registrar o resultado da análise financeira para indicar se o pedido foi aprovado ou reprovado.     | Alta       |
| US13 | Como agente, quero consultar o histórico de avaliações para acompanhar as decisões realizadas sobre um pedido.         | Média      |
| US14 | Como cliente, quero acompanhar o resultado da avaliação financeira para saber se meu pedido foi aprovado ou reprovado. | Alta       |

### 4. Contratos e crédito

| ID   | História do usuário                                                                                 | Prioridade |
| ---- | --------------------------------------------------------------------------------------------------- | ---------- |
| US15 | Como agente, quero encaminhar pedidos aprovados para execução contratual para formalizar o aluguel. | Alta       |
| US16 | Como cliente, quero consultar meus contratos para acompanhar os aluguéis formalizados.              | Média      |
| US17 | Como banco, quero conceder crédito associado a um aluguel para viabilizar sua contratação.          | Alta       |
| US18 | Como agente, quero consultar os contratos para acompanhar sua execução.                             | Média      |

### 5. Automóveis e informações financeiras

| ID   | História do usuário                                                                                                                           | Prioridade |
| ---- | --------------------------------------------------------------------------------------------------------------------------------------------- | ---------- |
| US19 | Como agente, quero cadastrar automóveis para disponibilizá-los no sistema.                                                                    | Alta       |
| US20 | Como usuário, quero consultar os automóveis cadastrados para conhecer os veículos disponíveis.                                                | Alta       |
| US21 | Como contratante, quero cadastrar meus dados profissionais para comprovar minha renda.                                                        | Alta       |
| US22 | Como contratante, quero cadastrar até três vínculos empregatícios com seus respectivos rendimentos para apresentar minha situação financeira. | Alta       |
| US23 | Como agente, quero visualizar os proprietários dos automóveis para identificar a titularidade dos veículos.                                   | Média      |

### 6. Critérios de aceitação das principais histórias

US05 - Cadastrar pedido

- O cliente precisa estar autenticado.
- O pedido deve estar associado ao cliente que o cadastrou.
- Deve ser possível selecionar um automóvel.
- O pedido deve receber um identificador único.
- O pedido deve iniciar com status `PENDENTE`.

US07 - Modificar pedido

- O cliente só pode modificar seus próprios pedidos.
- Pedidos cancelados ou com contrato executado não podem ser modificados.
- As alterações devem ser persistidas no banco de dados.

US12 - Registrar avaliação financeira

- Somente agentes autenticados podem avaliar pedidos.
- A avaliação deve estar vinculada a um pedido.
- Deve ser registrado o resultado da avaliação.
- O resultado deve ser positivo ou negativo.
- Uma aprovação deve permitir o encaminhamento para contratação.

US22 - Cadastrar vínculos empregatícios

- O contratante pode possuir no máximo três vínculos.
- Cada vínculo deve possuir uma entidade empregadora e um rendimento.
- Não deve ser permitido cadastrar um quarto vínculo.
