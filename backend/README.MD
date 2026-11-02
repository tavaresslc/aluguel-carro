# Aluguel de Carro - Backend

API REST do sistema de **aluguel de carros** (Spring Boot 4 | Java 25 | PostgreSQL/Supabase).
Implementa as histórias de usuário US01-US23 descritas em [`/docs`](../docs).

---

## 1. Visão geral

| Ator                       | O que faz                                                                                                                                            |
| -------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Cliente**          | Cadastra-se, mantém seus dados de contratante (até 3 vínculos empregatícios), cria/modifica/cancela pedidos, acompanha avaliações e contratos. |
| **Agente - Empresa** | Cadastra automóveis, consulta/modifica pedidos, avalia financeiramente, executa contratos.                                                          |
| **Agente - Banco**   | Tudo que a empresa faz **e** concede crédito para um contrato.                                                                                |

Fluxo principal:

```
Cliente se cadastra > completa dados do contratante > cria PEDIDO (PENDENTE)
   > Agente registra AVALIAÇÃO FINANCEIRA (APROVADO | REPROVADO)
   > pedido APROVADO > Agente executa CONTRATO (pedido vira CONTRATADO)
   > Banco pode conceder CRÉDITO ao contrato
```

Status do pedido: `PENDENTE`, `APROVADO`, `REPROVADO`, `CANCELADO`, `CONTRATADO`.

---

## 2. Arquitetura e organização

Arquitetura **MVC em camadas** (conforme o diagrama de pacotes em `docs/diagrams/package.png`):

```
backend/src/main/java/br/com/carrent
├── CarrentApplication.java      # ponto de entrada
├── controller/                  # endpoints REST (recebem HTTP, validam o corpo, delegam ao service)
├── service/                     # regras de negócio e transações
├── repository/                  # acesso ao banco (Spring Data JPA)
├── domain/                      # entidades JPA e regras próprias de cada entidade
├── dto/                         # objetos de entrada/saída da API (records)
├── security/                    # autenticação por token, autorização por perfil, senha
└── config/                      # CORS e conexão com o Supabase
```

Dependências entre camadas: `controller > service > repository > domain`; os controllers só trafegam DTOs (as entidades nunca vão para o JSON).

### Pacote por pacote

| Classe                            | Responsabilidade                                                                                                                                                               |
| --------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `controller/*Controller`        | Um controller por área: `Auth`, `Usuario`, `Automovel`, `Pedido`, `Avaliacao`, `Contrato`, `Credito`.                                                           |
| `service/AuthService`           | Cadastro de cliente, login, logout, emissão/validação do token. Também é o `UserDetailsService` do Spring Security.                                                     |
| `service/UsuarioService`        | Perfil do usuário, dados do contratante/vínculos (CPF válido e único) e criação dos agentes iniciais.                                                                    |
| `service/AutomovelService`      | Cadastro/consulta de automóveis e cálculo de disponibilidade.                                                                                                                |
| `service/PedidoService`         | Criar, listar, consultar, modificar e cancelar pedidos, com regras de acesso por dono.                                                                                         |
| `service/AvaliacaoService`      | Registrar parecer financeiro e consultar o histórico.                                                                                                                         |
| `service/ContratoService`       | Executar contrato (com bloqueio de conflito de datas) e consultar contratos.                                                                                                   |
| `service/CreditoService`        | Conceder crédito (apenas banco).                                                                                                                                              |
| `domain/*`                      | `Usuario` (abstrata) > `Cliente` e `Agente`; `Contratante` (+ vínculos), `Automovel`, `PedidoAluguel`, `AvaliacaoFinanceira`, `ContratoAluguel`, `Credito`. |
| `dto/UsuarioDTO`                | Usuário + payloads de cadastro, login, atualização do contratante.                                                                                                          |
| `dto/PedidoDTO`                 | Pedido + payloads de pedido **e de automóvel** (o automóvel é sempre usado no contexto de um pedido).                                                                 |
| `dto/AvaliacaoDTO`              | Avaliação financeira + payload de registro.                                                                                                                                  |
| `dto/ContratoDTO`               | Contrato + payloads de execução **e de crédito** (crédito só existe ligado a um contrato).                                                                          |
| `security/SecurityConfig`       | Cadeia de filtros: API stateless, sem CSRF, regras de autorização por perfil, erros 401/403 em JSON.                                                                         |
| `security/AuthenticationFilter` | Lê `Authorization: Bearer <token>` e autentica a requisição.                                                                                                              |
| `security/PasswordEncoder`      | BCrypt, registrado como o `PasswordEncoder` do Spring Security.                                                                                                              |
| `config/CorsConfig`             | CORS para o frontend (origens por variável de ambiente).                                                                                                                      |
| `config/SupabaseConfig`         | Pool HikariCP ajustado para o Supabase (SSL, `prepareThreshold=0`, pool pequeno).                                                                                            |

### Decisões de modelagem

| Diagrama                                                  | Implementação                                                                                    |
| --------------------------------------------------------- | -------------------------------------------------------------------------------------------------- |
| `Empresa` e `Banco` (especializações de `Agente`) | `Agente` com o campo `tipo` (`EMPRESA` \| `BANCO`). Só `BANCO` concede crédito.        |
| `Proprietario` (cliente, empresa ou banco)              | `Automovel.proprietario` aponta para `Usuario`, ancestral comum dos três.                     |
| `VinculoEmpregaticio`                                   | Classe embutida em `Contratante` (tabela `vinculo_empregaticio`), limite de 3 por contratante. |
| `Cliente 1-1 Contratante`                               | Criado vazio no cadastro e completado em `PUT /api/usuarios/me/contratante`.                     |

### Regras de negócio implementadas

- Cadastro e autenticação são obrigatórios para todo o sistema (exceto cadastro e login).
- Cliente só vê/modifica/cancela **seus** pedidos (para pedidos de outros, responde `404`).
- Pedido nasce `PENDENTE`. Para criá-lo o cliente precisa ter dados de contratante completos (nome, RG, CPF, endereço, profissão e ao menos 1 vínculo) e o automóvel precisa estar disponível.
- Pedidos `CANCELADO` ou `CONTRATADO` não podem ser modificados, cancelados ou reavaliados.
- Trocar o automóvel de um pedido já avaliado o devolve a `PENDENTE` (o parecer anterior não vale para outro carro).
- Avaliação: só agentes; pode haver várias por pedido (histórico); reprovação exige justificativa; o status do pedido acompanha o último parecer.
- Contrato: só de pedido `APROVADO`; datas válidas (início não passado, fim ≥ início); sem sobreposição de período para o mesmo automóvel; o pedido passa a `CONTRATADO`.
- Crédito: só banco; só em contrato `EXECUTADO`; no máximo um por contrato; valor ≤ valor do contrato. A parcela é calculada pela Tabela Price (taxa em % ao mês).
- Contratante: no máximo **3 vínculos**; CPF validado (dígitos verificadores) e único.
- Disponibilidade do automóvel = habilitado no cadastro **e** sem contrato executado vigente hoje.
- O proprietário do automóvel só aparece para agentes (US23).

---

## 3. Dependências (pom.xml) e para que cada uma é usada

| Dependência                       | Uso no projeto                                                                                                                                                                                                                  |
| ---------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `spring-boot-starter-webmvc`     | API REST: controllers, serialização JSON (records/DTOs), tratamento de erros (`ResponseStatusException`).                                                                                                                   |
| `spring-boot-starter-data-jpa`   | Entidades JPA/Hibernate, herança `JOINED`, `@ElementCollection`, repositórios Spring Data (consultas derivadas, JPQL, `@EntityGraph` contra N+1, `@Lock` pessimista na contratação), transações. Traz o HikariCP. |
| `spring-boot-starter-security`   | Cadeia de filtros stateless, autorização por perfil (`hasRole`), BCrypt, `@AuthenticationPrincipal`.                                                                                                                      |
| `spring-boot-starter-validation` | Bean Validation nos DTOs (`@NotBlank`, `@Email`, `@Size`, `@Digits`, `@Valid`...). Erros viram `400` com os campos inválidos.                                                                                      |
| `postgresql`                     | Driver JDBC do PostgreSQL/Supabase.                                                                                                                                                                                             |
| `lombok`                         | Reduz boilerplate: `@Getter/@Setter`, `@NoArgsConstructor`, `@RequiredArgsConstructor` (injeção por construtor), `@Slf4j`.                                                                                            |
| `spring-boot-devtools`           | Reinício automático em desenvolvimento (não vai no pacote final de produção).                                                                                                                                              |
| `spring-boot-starter-*-test`     | JUnit 5, AssertJ, Mockito e suporte a testes.                                                                                                                                                                                   |

Nenhuma dependência foi adicionada. O token de acesso é assinado com HMAC-SHA256 usando apenas o JDK (`javax.crypto`).

---

## 4. Autenticação

1. `POST /api/auth/login` devolve um `token`.
2. Envie em toda requisição protegida: `Authorization: Bearer <token>`.
3. O token expira em `TOKEN_EXPIRATION_MINUTES` (padrão 480 = 8 h) e deixa de valer no **logout** (`POST /api/auth/logout`) ou se o usuário for desativado.
4. Sem token/token inválido > `401`. Perfil sem permissão > `403`.

No Postman: defina uma variável `token` e use `Authorization > Bearer Token > {{token}}`. No frontend, guarde o token (ex.: `localStorage`) e envie no header.

---

## 5. Rodando localmente

### 5.1 Pré-requisitos

- **Java 25** (JDK) - o `pom.xml` exige `java.version=25`.
- Não precisa instalar Maven: use o wrapper (`./mvnw`, ou `mvnw.cmd` no Windows).
- Um banco PostgreSQL - o do Supabase (seção 6) ou um Postgres local.

### 5.2 Configure o banco

Siga a [seção 6](#6-configurando-o-banco-de-dados-supabase) e obtenha `DB_URL`, `DB_USERNAME` e `DB_PASSWORD`.

### 5.3 Crie o arquivo `backend/.env`

(já está no `.gitignore`; formato `CHAVE=valor`, sem aspas e sem `\`)

```properties
DB_URL=jdbc:postgresql://aws-0-sa-east-1.pooler.supabase.com:5432/postgres
DB_USERNAME=postgres.SEU_PROJECT_REF
DB_PASSWORD=sua-senha-do-banco
SPRING_PROFILES_ACTIVE=dev
```

> Alternativa: exporte as variáveis no terminal (`export DB_URL=...` no Linux/macOS, `$env:DB_URL="..."` no PowerShell) ou configure-as no run configuration da IDE.

### 5.4 Execute

```bash
cd backend
./mvnw spring-boot:run          # Windows: mvnw.cmd spring-boot:run
```

A API sobe em `http://localhost:8080`. Na primeira execução o Hibernate cria as tabelas sozinho.

No perfil `dev` são criados automaticamente dois agentes para teste:

| Agente  | E-mail                  | Senha             |
| ------- | ----------------------- | ----------------- |
| Banco   | `banco@carrent.dev`   | `Banco@12345`   |
| Empresa | `empresa@carrent.dev` | `Empresa@12345` |

(Clientes se cadastram por `POST /api/auth/cadastro`.)

### 5.5 Testes

```bash
./mvnw test
```

Os testes unitários (regras de negócio e token) rodam sem banco. O teste de contexto (`contextLoads`) só roda se `DB_URL` estiver definida.

### 5.6 Roteiro rápido no Postman ou Insomnia

1. `POST /api/auth/cadastro` (cliente) > `POST /api/auth/login` > salve o token do cliente.
2. `PUT /api/usuarios/me/contratante` com um CPF válido e 1-3 vínculos.
3. Login do **empresa** > `POST /api/automoveis`.
4. Cliente: `GET /api/automoveis` > `POST /api/pedidos`.
5. Empresa/Banco: `POST /api/pedidos/{id}/avaliacoes` com `APROVADO`.
6. Empresa/Banco: `POST /api/pedidos/{id}/contrato`.
7. Banco: `POST /api/contratos/{id}/credito`.
8. Cliente: `GET /api/contratos` (já mostra o crédito).

---

## 6. Configurando o banco de dados (Supabase)

1. Acesse [https://supabase.com](https://supabase.com) > **New project**. Defina nome, **senha do banco** (guarde-a) e a região (ex.: Americas).
2. Aguarde o projeto ficar pronto.
3. Clique em **Connect** (topo do painel) > aba **Connection string** > método **Session pooler** (funciona em redes só IPv4 e suporta o DDL do Hibernate) e copie os dados:

   - Host: `aws-0-<regiao>.pooler.supabase.com`
   - Porta: `5432`
   - Banco: `postgres`
   - Usuário: `postgres.<project-ref>` (no pooler o usuário leva o ref do projeto)
4. Monte a URL **JDBC** (diferente da URI `postgresql://` exibida):

   ```
   DB_URL=jdbc:postgresql://aws-0-sa-east-1.pooler.supabase.com:5432/postgres
   DB_USERNAME=postgres.abcdefghijklmnop
   DB_PASSWORD=<senha definida no passo 1>
   ```

   O `sslmode=require` é acrescentado automaticamente pela aplicação para hosts `supabase`.
5. **Tabelas**: não é preciso criar nada. Com `JPA_DDL_AUTO=update` (padrão) o Hibernate cria `usuario`, `cliente`, `agente`, `contratante`, `vinculo_empregaticio`, `automovel`, `pedido_aluguel`, `avaliacao_financeira`, `contrato_aluguel` e `credito` na primeira inicialização. Confira em **Table Editor**.
6. Outras opções de conexão:

   - _Direct connection_ (`db.<ref>.supabase.co:5432`): só funciona com IPv6 (ou add-on IPv4).
   - _Transaction pooler_ (porta `6543`): recomendado para muitas instâncias/serverless; a aplicação já desliga prepared statements (`prepareThreshold=0`). Rode a primeira inicialização (criação das tabelas) pelo Session pooler.
7. **Segurança**: o acesso é feito só pelo backend com o usuário do banco; não exponha a senha nem a `anon key` no frontend. Se quiser, ative **RLS** nas tabelas - o usuário `postgres` do backend não é afetado e a API REST pública do Supabase fica sem acesso aos dados.

---

## 7. Variáveis de ambiente

| Variável                                                          | Obrigatória                | Padrão                        | Descrição                                                                         |
| ------------------------------------------------------------------ | --------------------------- | ------------------------------ | ----------------------------------------------------------------------------------- |
| `DB_URL`                                                         | sim                         | -                              | URL JDBC do Supabase.                                                               |
| `DB_USERNAME`                                                    | sim                         | -                              | Usuário do banco.                                                                  |
| `DB_PASSWORD`                                                    | sim                         | -                              | Senha do banco.                                                                     |
| `TOKEN_SECRET`                                                   | sim (exceto perfil `dev`) | -                              | Segredo ≥ 32 caracteres para assinar tokens. Gere com `openssl rand -base64 48`. |
| `SPRING_PROFILES_ACTIVE`                                         | não                        | -                              | `dev` (local) ou `prod`.                                                        |
| `CORS_ALLOWED_ORIGINS`                                           | não                        | `http://localhost:5173`      | Origens do frontend, separadas por vírgula.                                        |
| `TOKEN_EXPIRATION_MINUTES`                                       | não                        | `480`                        | Validade do token.                                                                  |
| `PORT`                                                           | não                        | `8080`                       | Porta HTTP (plataformas de deploy a definem).                                       |
| `JPA_DDL_AUTO`                                                   | não                        | `update`                     | `update`, `validate`, `none`...                                               |
| `JPA_SHOW_SQL`                                                   | não                        | `false` (`true` no dev)    | Loga o SQL.                                                                         |
| `DB_POOL_MAX` / `DB_POOL_MIN_IDLE`                             | não                        | `5` / `1`                  | Tamanho do pool.                                                                    |
| `SEED_BANCO_NOME/EMAIL/SENHA`, `SEED_EMPRESA_NOME/EMAIL/SENHA` | não                        | vazio (preenchidos no `dev`) | Criam o banco/empresa iniciais na subida, se ainda não existirem.                  |

---

## 8. Produção

1. **Gere o pacote** (os testes de contexto são ignorados sem banco):

   ```bash
   cd backend
   ./mvnw clean package
   ```

   Resultado: `target/carrent-0.0.1-SNAPSHOT.jar`.
2. **Defina as variáveis** na plataforma/servidor: `SPRING_PROFILES_ACTIVE=prod`, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `TOKEN_SECRET`, `CORS_ALLOWED_ORIGINS=https://seu-frontend.com`, e os `SEED_BANCO_*` / `SEED_EMPRESA_*` para criar os agentes reais (senhas fortes).
3. **Execute**:

   ```bash
   java -jar target/carrent-0.0.1-SNAPSHOT.jar
   ```

   - Em plataformas (Render, Railway, Fly.io etc.): _build command_ `./mvnw clean package`; _start command_ `java -jar target/carrent-0.0.1-SNAPSHOT.jar`; use um ambiente com **Java 25**. A porta vem de `PORT`; HTTPS é terminado pelo proxy da plataforma.
4. **Depois do primeiro deploy** (tabelas criadas), defina `JPA_DDL_AUTO=validate` para que o Hibernate pare de alterar o esquema automaticamente. Mudanças futuras de esquema devem ser aplicadas com cuidado/SQL manual (o projeto não usa ferramenta de migração).
5. **Remova** os `SEED_*` com senhas de teste depois que os agentes reais existirem (alterar a variável não altera um agente já criado).
6. O frontend deve usar a URL pública do backend e estar em `CORS_ALLOWED_ORIGINS`.

---

## 9. Endpoints

Base: `http://localhost:8080`. Todos trocam JSON (`Content-Type: application/json`). Acesso vazio = público | Acesso preenchido = exige `Authorization: Bearer <token>`.

| Método | Rota                             | Acesso         | História        | O que faz                                                         |
| ------- | -------------------------------- | -------------- | ---------------- | ----------------------------------------------------------------- |
| POST    | `/api/auth/cadastro`           |                | US01             | Cadastra um cliente.                                              |
| POST    | `/api/auth/login`              |                | US02             | Autentica e devolve o token.                                      |
| POST    | `/api/auth/logout`             | qualquer       | US03             | Encerra a sessão (invalida os tokens).                           |
| GET     | `/api/usuarios/me`             | qualquer       | US04/US11        | Dados do usuário logado (cliente: inclui contratante).           |
| PUT     | `/api/usuarios/me/contratante` | cliente        | US04, US21, US22 | Atualiza dados pessoais/profissionais e vínculos (máx. 3).      |
| GET     | `/api/automoveis`              | qualquer       | US20, US23       | Lista automóveis (`?disponivel=true\|false`).                   |
| GET     | `/api/automoveis/{id}`         | qualquer       | US20, US23       | Detalha um automóvel.                                            |
| POST    | `/api/automoveis`              | agente         | US19             | Cadastra automóvel (o agente vira o proprietário).              |
| POST    | `/api/pedidos`                 | cliente        | US05             | Cria pedido `PENDENTE`.                                         |
| GET     | `/api/pedidos`                 | cliente/agente | US06, US09, US14 | Cliente: seus pedidos. Agente: todos. Filtro `?status=`.        |
| GET     | `/api/pedidos/{id}`            | cliente/agente | US06, US11, US14 | Detalhe do pedido + dados do contratante.                         |
| PUT     | `/api/pedidos/{id}`            | cliente/agente | US07, US10       | Modifica automóvel/observações.                                |
| PATCH   | `/api/pedidos/{id}/cancelar`   | cliente        | US08             | Cancela o próprio pedido.                                        |
| POST    | `/api/pedidos/{id}/avaliacoes` | agente         | US12             | Registra aprovação/reprovação.                                |
| GET     | `/api/pedidos/{id}/avaliacoes` | cliente/agente | US13, US14       | Histórico de avaliações do pedido.                             |
| POST    | `/api/pedidos/{id}/contrato`   | agente         | US15             | Executa o contrato de um pedido aprovado.                         |
| GET     | `/api/contratos`               | cliente/agente | US16, US18       | Cliente: seus contratos. Agente: todos (com crédito, se houver). |
| GET     | `/api/contratos/{id}`          | cliente/agente | US16, US18       | Detalha um contrato.                                              |
| POST    | `/api/contratos/{id}/credito`  | banco          | US17             | Concede crédito ao contrato.                                     |

### 9.1 Autenticação

**POST `/api/auth/cadastro`** - `201`

```json
{
  "nome": "Maria Silva",
  "email": "maria@email.com",
  "senha": "senha-forte-123"
}
```

Senha: 8 a 72 caracteres. E-mail duplicado > `409`.

**POST `/api/auth/login`** - `200`

```json
{ "email": "maria@email.com", "senha": "senha-forte-123" }
```

Resposta:

```json
{
  "token": "MToxOjE3...assinatura",
  "tipoToken": "Bearer",
  "expiraEmSegundos": 28800,
  "usuario": {
    "id": 1,
    "nome": "Maria Silva",
    "email": "maria@email.com",
    "perfil": "CLIENTE",
    "nomeInstituicao": null,
    "contratante": { "...": "..." }
  }
}
```

`perfil` é `CLIENTE`, `EMPRESA` ou `BANCO` - o frontend usa para decidir quais telas mostrar. Credenciais erradas > `401`.

**POST `/api/auth/logout`** - `204`, sem corpo.

### 9.2 Usuário

**GET `/api/usuarios/me`** - devolve o mesmo objeto `usuario` do login.

**PUT `/api/usuarios/me/contratante`** - cliente - `200`

```json
{
  "nome": "Maria Silva",
  "rg": "12.345.678-9",
  "cpf": "529.982.247-25",
  "endereco": "Rua das Flores, 100 - São Paulo/SP",
  "profissao": "Engenheira",
  "vinculos": [
    { "entidadeEmpregadora": "Empresa X", "rendimento": 6500.0 },
    { "entidadeEmpregadora": "Consultoria Y", "rendimento": 2000.0 }
  ]
}
```

A lista **substitui** os vínculos atuais (use `[]` para remover todos). Mais de 3 vínculos > `400`. CPF inválido > `400`; CPF de outro cliente > `409`. A resposta traz `rendaTotal` e `dadosCompletos`.

### 9.3 Automóveis

**POST `/api/automoveis`** - agente - `201`

```json
{
  "matricula": "RENAVAM-123456",
  "ano": 2023,
  "marca": "Toyota",
  "modelo": "Corolla",
  "placa": "ABC1D23",
  "disponivel": true
}
```

`disponivel` é opcional (padrão `true`). Placa: `ABC1D23` ou `ABC-1234`. Placa/matrícula duplicada > `409`.

**GET `/api/automoveis?disponivel=true`** - exemplo de item:

```json
{
  "id": 1,
  "matricula": "RENAVAM-123456",
  "ano": 2023,
  "marca": "Toyota",
  "modelo": "Corolla",
  "placa": "ABC1D23",
  "disponivel": true,
  "proprietario": { "id": 2, "nome": "Empresa Carrent", "perfil": "EMPRESA" }
}
```

`proprietario` só vem preenchido para agentes (`null` para clientes).

### 9.4 Pedidos

**POST `/api/pedidos`** - cliente - `201`

```json
{ "automovelId": 1, "observacoes": "Preciso do carro para uma viagem." }
```

Dados de contratante incompletos > `409`; automóvel indisponível > `409`; inexistente > `404`.

**GET `/api/pedidos?status=PENDENTE`** - lista; `status` opcional.

```json
[
  {
    "id": 1,
    "dataSolicitacao": "2026-11-01",
    "status": "PENDENTE",
    "observacoes": "...",
    "cliente": { "id": 1, "nome": "Maria Silva", "email": "maria@email.com" },
    "automovel": {
      "id": 1,
      "marca": "Toyota",
      "modelo": "Corolla",
      "ano": 2023,
      "placa": "ABC1D23"
    },
    "contratante": null
  }
]
```

**GET `/api/pedidos/{id}`** - igual ao item acima, porém com `contratante` preenchido (dados pessoais, vínculos, `rendaTotal`) para a análise financeira.

**PUT `/api/pedidos/{id}`**

```json
{ "automovelId": 1, "observacoes": "Nova observação" }
```

Cliente só no próprio pedido. Pedido cancelado/contratado > `409`.

**PATCH `/api/pedidos/{id}/cancelar`** - sem corpo. Cancelado/contratado > `409`.

### 9.5 Avaliações financeiras

**POST `/api/pedidos/{id}/avaliacoes`** - agente - `201`

```json
{
  "parecer": "APROVADO",
  "justificativa": "Renda compatível com o valor do aluguel."
}
```

`parecer`: `APROVADO` ou `REPROVADO` (reprovação exige `justificativa`). O status do pedido é atualizado. Resposta:

```json
{
  "id": 1,
  "pedidoId": 1,
  "agenteId": 2,
  "agenteNome": "Banco Carrent",
  "instituicao": "Banco Carrent",
  "tipoAgente": "BANCO",
  "dataAvaliacao": "2026-11-01T14:40:00",
  "parecer": "APROVADO",
  "justificativa": "Renda compatível com o valor do aluguel."
}
```

**GET `/api/pedidos/{id}/avaliacoes`** - lista do mais recente para o mais antigo. O cliente consulta o resultado do próprio pedido.

### 9.6 Contratos

**POST `/api/pedidos/{id}/contrato`** - agente - `201`

```json
{ "dataInicio": "2026-11-10", "dataFim": "2026-11-20", "valor": 1500.0 }
```

Só para pedido `APROVADO` (senão `409`); período que cruza outro contrato do mesmo carro > `409`; datas inválidas > `400`.

**GET `/api/contratos`** / **GET `/api/contratos/{id}`**

```json
{
  "id": 1,
  "pedidoId": 1,
  "cliente": { "id": 1, "nome": "Maria Silva", "email": "maria@email.com" },
  "automovel": {
    "id": 1,
    "marca": "Toyota",
    "modelo": "Corolla",
    "ano": 2023,
    "placa": "ABC1D23"
  },
  "dataInicio": "2026-11-10",
  "dataFim": "2026-11-20",
  "valor": 1500.0,
  "status": "EXECUTADO",
  "credito": null
}
```

### 9.7 Crédito

**POST `/api/contratos/{id}/credito`** - banco - `201`

```json
{ "valor": 1000.0, "taxaJuros": 1.5, "quantidadeParcelas": 12 }
```

`taxaJuros` é % ao mês. Resposta:

```json
{
  "id": 1,
  "contratoId": 1,
  "bancoId": 2,
  "bancoNome": "Banco Carrent",
  "valor": 1000.0,
  "taxaJuros": 1.5,
  "quantidadeParcelas": 12,
  "valorParcela": 91.68,
  "status": "APROVADO"
}
```

Empresa tentando conceder > `403`; contrato já com crédito > `409`; valor acima do contrato > `400`.

### 9.8 Formato de erro

```json
{
  "timestamp": "2026-11-01T17:40:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "O automóvel selecionado não está disponível.",
  "path": "/api/pedidos"
}
```

Erros de validação (`400`) incluem também `errors`, com o campo e a mensagem de cada problema.

| Status | Quando                                                                     |
| ------ | -------------------------------------------------------------------------- |
| 400    | Corpo inválido / regra de dados (datas, CPF, valores).                    |
| 401    | Sem token, token inválido/expirado, login incorreto.                      |
| 403    | Perfil sem permissão para a operação.                                   |
| 404    | Recurso inexistente (ou de outro cliente).                                 |
| 409    | Conflito de estado (duplicidade, pedido já contratado, indisponível...). |

---

## 10. Limitações conhecidas

- Sem ferramenta de migração de banco (Flyway/Liquibase não estão no projeto): o esquema é gerido pelo Hibernate.
- Listagens não têm paginação.
- Sem limitação de tentativas de login (rate limiting).
- Sem endpoint de _health check_ público (nenhum endpoint fora do necessário foi criado); use verificação por porta TCP na plataforma de deploy.
