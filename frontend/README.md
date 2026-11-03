# Carrent - Frontend

Interface web do sistema de **aluguel de automóveis**. Consome a API REST do [`/backend`](../backend) e implementa as histórias de usuário US01-US23 descritas em [`/docs`](../docs).

React 19 | Vite 8 | TypeScript | Tailwind CSS 4 | React Router 7

---

## 1. Instalação e execução

### Pré-requisitos

- Node.js 20.19+ ou 22.12+ (exigência do Vite 8) e npm 10+
- Backend rodando em `http://localhost:8080` (veja [`backend/README.MD`](../backend/README.MD)). No perfil `dev` do backend já existem dois agentes de teste: `banco@carrent.dev` / `Banco@12345` e `empresa@carrent.dev` / `Empresa@12345`.

### Passo a passo (local)

```bash
cd frontend
npm install
cp .env.example .env      # Windows: copy .env.example .env
npm run dev
```

Abra `http://localhost:5173` (origem já liberada no CORS padrão do backend).

### Variável de ambiente

| Variável        | Padrão                   | Descrição                                           |
| ---------------- | ------------------------- | ----------------------------------------------------- |
| `VITE_API_URL` | `http://localhost:8080` | URL base do backend, **sem** `/api` no final. |

Só entram no bundle variáveis com prefixo `VITE_`, e elas ficam visíveis a qualquer visitante. Nunca coloque segredos nelas.

### Scripts

| Comando             | O que faz                                                         |
| ------------------- | ----------------------------------------------------------------- |
| `npm run dev`     | Servidor de desenvolvimento com recarga automática               |
| `npm run build`   | Checagem de tipos (`tsc -b`) + build de produção em `dist/` |
| `npm run preview` | Serve o build de produção localmente                            |
| `npm run lint`    | Análise estática com oxlint                                     |

### Deploy na Vercel

1. Importe o repositório na Vercel e defina **Root Directory** como `frontend`. O preset **Vite** é detectado sozinho (build `npm run build`, saída `dist`).
2. Em **Settings > Environment Variables**, crie `VITE_API_URL` com a URL pública (HTTPS) do backend.
3. No backend, inclua o domínio da Vercel em `CORS_ALLOWED_ORIGINS` (ex.: `https://carrent.vercel.app`).
4. Faça o deploy. O [`vercel.json`](vercel.json) já cuida do fallback de rotas (SPA) e dos cabeçalhos de segurança (seção 7).

Com a CLI: `npx vercel` dentro de `frontend/`.

---

## 2. Esquema de cores

Definido uma única vez em [`src/index.css`](src/index.css) (`@theme`), de onde o Tailwind gera as classes `bg-brand-600`, `text-brand-700` etc.

| Papel            | Token / classe                      | Valor                     | Uso                                                               |
| ---------------- | ----------------------------------- | ------------------------- | ----------------------------------------------------------------- |
| Primária        | `brand-600` (hover `brand-700`) | `#2557d6` / `#1d45ad` | Botões principais, links, item ativo da navegação              |
| Primária suave  | `brand-50` / `brand-100`        | `#eef4ff` / `#dbe7ff` | Fundos de ícones, destaques, alertas informativos                |
| Primária escura | `brand-900`                       | `#14285f`               | Painel lateral das telas de login/cadastro                        |
| Foco             | `brand-500`                       | `#3d6df0`               | Anel de foco de teclado                                           |
| Neutros          | `slate-50` ... `slate-900`      | escala do Tailwind        | Fundo da página (`slate-50`), cartões brancos, bordas, textos |

**Cores de status** (centralizadas em [`utils/labels.ts`](src/utils/labels.ts) e aplicadas pelo `StatusBadge`; sempre acompanhadas do texto, nunca só pela cor):

| Status                         | Cor       |
| ------------------------------ | --------- |
| `PENDENTE`                   | Âmbar    |
| `APROVADO`                   | Verde     |
| `REPROVADO`                  | Vermelho  |
| `CANCELADO`                  | Cinza     |
| `CONTRATADO` / `EXECUTADO` | Azul-céu |

Azul transmite confiança e seriedade (contexto financeiro); o verde/vermelho/âmbar seguem a convenção universal de sucesso/erro/atenção.

## 3. Fontes

| Fonte           | Pesos    | Uso                                            | Token            |
| --------------- | -------- | ---------------------------------------------- | ---------------- |
| **Sora**  | 600, 700 | Títulos (`h1`-`h3`), marca "Carrent"      | `font-display` |
| **Inter** | 400-600  | Todo o restante (texto, formulários, tabelas) | `font-sans`    |

Carregadas do Google Fonts em [`index.html`](index.html) (sem dependência npm). Se não carregarem, o navegador cai para `system-ui`.

---

## 4. Estrutura de pastas

```
frontend/
├── index.html                # página base (lang pt-BR, fontes, favicon)
├── vercel.json               # fallback SPA + cabeçalhos de segurança
├── .env.example              # variáveis de ambiente
├── vite.config.ts            # plugins: React (+ React Compiler) e Tailwind
└── src/
    ├── main.tsx              # ponto de entrada
    ├── App.tsx               # tabela de rotas
    ├── index.css             # Tailwind + tema (cores e fontes)
    ├── types/api.ts          # tipos TypeScript espelhando os DTOs do backend
    ├── services/             # comunicação HTTP com a API
    ├── context/              # estado global de autenticação
    ├── hooks/                # lógica reutilizável (auth, busca, envio de formulário)
    ├── utils/                # formatação, rótulos de status, helpers
    ├── components/
    │   ├── ui/               # blocos genéricos (botão, campos, tabela, modal...)
    │   ├── layout/           # esqueleto das telas e proteção de rotas
    │   ├── automovel/        # componentes do domínio "automóvel"
    │   ├── pedido/           # componentes do domínio "pedido/avaliação"
    │   ├── contrato/         # componentes do domínio "contrato/crédito"
    │   └── usuario/          # componentes do domínio "usuário/contratante"
    └── pages/                # uma tela por rota
```

A divisão segue o diagrama de pacotes de [`docs/MODELING.md`](../docs/MODELING.md): `pages`, `components`, `services` e `hooks`.

---

## 5. Hierarquia, telas e componentes

### 5.1 Árvore de rotas e layouts

```
BrowserRouter
└── AuthProvider                      (sessão e usuário logado)
    └── Routes
        ├── GuestOnly                 (visitante; logado é redirecionado)
        │   └── AuthLayout            (painel de marca + formulário)
        │       ├── /login            > Login
        │       └── /cadastro         > Cadastro
        └── RequireAuth               (exige login; senão vai para /login)
            ├── /                     > Home (redireciona, ver abaixo)
            └── AppLayout             (cabeçalho, navegação, <main>)
                ├── /automoveis       > Automoveis
                ├── /pedidos          > Pedidos
                ├── /pedidos/:id      > PedidoDetalhe
                ├── /contratos        > Contratos
                ├── /contratos/:id    > ContratoDetalhe
                └── /perfil           > Perfil
```

`Home` (em `App.tsx`) leva o cliente com dados de contratante incompletos para `/perfil` e todos os demais para `/pedidos`. Rotas inexistentes voltam para `/`.

### 5.2 Telas (`src/pages`)

| Tela                | Quem usa  | O que faz                                                                                                                                                                     | Histórias       |
| ------------------- | --------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------- |
| `Login`           | visitante | Autenticação por e-mail e senha.                                                                                                                                            | US02             |
| `Cadastro`        | visitante | Cria conta de cliente e já entra no sistema.                                                                                                                                 | US01             |
| `Automoveis`      | todos     | Lista com filtro (todos/disponíveis/indisponíveis). Cliente: botão "Solicitar aluguel". Agente: "Novo automóvel" e proprietário em cada cartão.                         | US19, US20, US23 |
| `Pedidos`         | todos     | Tabela com filtro por status. Cliente vê os seus e cria novos; agente vê todos.                                                                                             | US05, US06, US09 |
| `PedidoDetalhe`   | todos     | Dados do pedido e histórico de avaliações. Cliente: editar e cancelar. Agente: dados/renda do contratante, proprietário do veículo, editar, avaliar e executar contrato. | US07-US15        |
| `Contratos`       | todos     | Tabela de contratos (cliente vê os seus; agente, todos) com o crédito associado.                                                                                            | US16, US18       |
| `ContratoDetalhe` | todos     | Dados do contrato e do crédito. Banco: "Conceder crédito" (contrato executado sem crédito).                                                                                | US16-US18        |
| `Perfil`          | todos     | Dados da conta. Cliente: formulário de contratante com até 3 vínculos empregatícios e renda.                                                                              | US04, US21, US22 |

Sair da conta (US03) fica no cabeçalho do `AppLayout`.

### 5.3 Componentes genéricos (`components/ui`)

Não conhecem o domínio; são reaproveitados em todas as telas.

| Componente                                          | Papel                                                                                                                                        |
| --------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------- |
| `Button` / `LinkButton`                         | Botão (variantes `primary`, `secondary`, `danger`, `ghost`; estado `loading`) e link com a mesma aparência.                      |
| `Input`, `Select`, `Textarea` (`Field.tsx`) | Campo com rótulo, dica e erro. O erro vem sozinho do formulário, pelo `name` do campo.                                                   |
| `Form`                                            | `<form>` com envio assíncrono: bloqueia duplo clique, mostra erro da API, erros por campo, mensagem de sucesso e botões Cancelar/Enviar. |
| `Modal`                                           | Janela de diálogo baseada no `<dialog>` nativo (foco preso, Esc fecha, clique fora fecha).                                                |
| `FormModal`                                       | `Modal` + `Form`: base de todos os formulários em janela (novo automóvel, avaliação, contrato, crédito).                            |
| `ConfirmModal`                                    | Confirmação de ação destrutiva (cancelar pedido).                                                                                        |
| `DataTable`                                       | Tabela genérica orientada a colunas, com linha clicável e rolagem horizontal no celular.                                                   |
| `DataState`                                       | Resolve os estados de uma consulta: carregando, erro (com "Tentar novamente") ou o conteúdo.                                                |
| `Card`, `InfoGrid`                              | Cartão com título e grade rótulo/valor para detalhes.                                                                                     |
| `Badge`, `StatusBadge`                          | Selo colorido; `StatusBadge` traduz qualquer status da API em texto + cor.                                                                 |
| `Alert`                                           | Mensagens de erro, aviso, informação e sucesso.                                                                                            |
| `EmptyState`                                      | Tela vazia com ícone e orientação do próximo passo.                                                                                      |
| `PageHeader`                                      | Título, subtítulo e ações da página.                                                                                                    |
| `Spinner`                                         | Indicador de carregamento.                                                                                                                   |

### 5.4 Layout (`components/layout`)

| Componente      | Papel                                                                                                |
| --------------- | ---------------------------------------------------------------------------------------------------- |
| `AppLayout`   | Cabeçalho fixo (marca, nome/perfil, Sair), navegação por abas e área de conteúdo.               |
| `AuthLayout`  | Tela dividida: painel de marca (apenas desktop) e formulário.                                       |
| `RouteGuards` | `RequireAuth` e `GuestOnly`: controlam o acesso às rotas conforme haja ou não usuário logado. |
| `Brand`       | Logotipo (ícone + nome).                                                                            |

### 5.5 Componentes de domínio

| Componente             | Papel                                                                                                                                 |
| ---------------------- | ------------------------------------------------------------------------------------------------------------------------------------- |
| `AutomovelCard`      | Cartão do veículo (disponibilidade, proprietário para agentes) com espaço para uma ação.                                        |
| `AutomovelFormModal` | Cadastro de automóvel (agente).                                                                                                      |
| `PedidoFormModal`    | Cria **ou** edita um pedido. Lista apenas automóveis disponíveis; avisa o cliente se faltam dados de contratante.             |
| `AvaliacaoModal`     | Registra parecer (aprovado/reprovado); a justificativa passa a ser obrigatória ao reprovar.                                          |
| `AvaliacaoHistorico` | Linha do tempo das avaliações de um pedido (mais recente primeiro).                                                                 |
| `ContratoFormModal`  | Executa o contrato de um pedido aprovado (datas e valor; valida término ≥ início).                                                 |
| `CreditoFormModal`   | Concessão de crédito pelo banco (valor, juros % ao mês, parcelas).                                                                 |
| `CreditoResumo`      | Exibe o crédito concedido, com valor da parcela calculado pelo backend.                                                              |
| `ContratanteForm`    | Dados pessoais/profissionais e de 1 a 3 vínculos empregatícios (adicionar/remover; o botão some ao chegar em 3). CPF com máscara. |
| `ContratanteResumo`  | Visão somente leitura do contratante e da renda total, para a análise do agente.                                                    |

### 5.6 Camadas de apoio

**`services/`**: único ponto que fala com a API.

| Arquivo                                                                           | Responsabilidade                                                                                                                                                               |
| --------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `http.ts`                                                                       | Cliente `fetch` (URL base, cabeçalho `Authorization`, JSON, tratamento de erros em `ApiError` com mensagens por campo). Um `401` com sessão ativa encerra a sessão. |
| `session.ts`                                                                    | Guarda o token e sua validade no `sessionStorage`.                                                                                                                           |
| `auth.ts`, `usuarios.ts`, `automoveis.ts`, `pedidos.ts`, `contratos.ts` | Uma função por endpoint, agrupadas como no backend (avaliações e contrato do pedido ficam em `pedidos.ts`; crédito em `contratos.ts`).                                |

**`context/` e `hooks/`**

| Arquivo                      | Responsabilidade                                                                                                        |
| ---------------------------- | ----------------------------------------------------------------------------------------------------------------------- |
| `context/AuthProvider.tsx` | Mantém o usuário logado, restaura a sessão (`GET /usuarios/me`), expira o token no horário e expõe login/logout. |
| `hooks/useAuth.ts`         | Acesso ao contexto com atalhos `isCliente`, `isAgente`, `isBanco`.                                                |
| `hooks/useFetch.ts`        | Busca de dados com estados de carregamento/erro, recarga e revalidação sem "piscar" a tela.                           |
| `hooks/useSubmit.ts`       | Estado de envio de formulários (pendente, erro, erros por campo, sucesso).                                             |

**`utils/`**: `format.ts` (moeda BRL, datas, máscara de CPF), `labels.ts` (textos e cores de status/perfil), `cx.ts` (junta classes CSS).

---

## 6. Cobertura dos endpoints

| Endpoint                              | Onde é usado                                          |
| ------------------------------------- | ------------------------------------------------------ |
| `POST /api/auth/cadastro`           | `Cadastro`                                           |
| `POST /api/auth/login`              | `Login`, `Cadastro` (login automático)            |
| `POST /api/auth/logout`             | Botão Sair do `AppLayout`                           |
| `GET /api/usuarios/me`              | Restauração da sessão e `Perfil`                  |
| `PUT /api/usuarios/me/contratante`  | `ContratanteForm`                                    |
| `GET /api/automoveis`               | `Automoveis`, `PedidoFormModal` (`?disponivel=`) |
| `GET /api/automoveis/{id}`          | `PedidoDetalhe` (proprietário do veículo, agentes) |
| `POST /api/automoveis`              | `AutomovelFormModal`                                 |
| `POST /api/pedidos`                 | `PedidoFormModal`                                    |
| `GET /api/pedidos`                  | `Pedidos` (`?status=`)                             |
| `GET /api/pedidos/{id}`             | `PedidoDetalhe`                                      |
| `PUT /api/pedidos/{id}`             | `PedidoFormModal` (modo edição)                    |
| `PATCH /api/pedidos/{id}/cancelar`  | `PedidoDetalhe` (`ConfirmModal`)                   |
| `POST /api/pedidos/{id}/avaliacoes` | `AvaliacaoModal`                                     |
| `GET /api/pedidos/{id}/avaliacoes`  | `AvaliacaoHistorico` em `PedidoDetalhe`            |
| `POST /api/pedidos/{id}/contrato`   | `ContratoFormModal`                                  |
| `GET /api/contratos`                | `Contratos`                                          |
| `GET /api/contratos/{id}`           | `ContratoDetalhe`                                    |
| `POST /api/contratos/{id}/credito`  | `CreditoFormModal`                                   |

As ações só aparecem para quem tem permissão (por exemplo, "Conceder crédito" só para `BANCO`), espelhando as regras do backend. Isso é conveniência de interface: a autorização real continua sendo do backend, que responde `401/403/404/409` e cujas mensagens são exibidas ao usuário.

---

## 7. Segurança

- **Autorização no servidor**: o frontend nunca é a barreira. Esconder botões e rotas só melhora a experiência; toda regra é reaplicada pelo backend.
- **Token na sessão do navegador**: o JWT fica em `sessionStorage` (some ao fechar a aba) e **apenas ele e sua validade** são guardados. Dados pessoais (CPF, RG, renda) ficam só em memória e são recarregados via `GET /usuarios/me`. Se a API responder `401`, ou o token expirar, a sessão é limpa e o usuário volta ao login. O logout também invalida o token no servidor.
- **Contra XSS**: o React escapa todo conteúdo renderizado, e o projeto não usa `dangerouslySetInnerHTML`, `eval` nem HTML vindo da API. A CSP abaixo bloqueia scripts que não sejam do próprio domínio.
- **Cabeçalhos HTTP** (em `vercel.json`): `Content-Security-Policy` (scripts e estilos só do próprio domínio; fontes do Google; `connect-src` apenas HTTPS; sem frames/objetos), `X-Frame-Options: DENY` (anti-clickjacking), `X-Content-Type-Options`, `Referrer-Policy: no-referrer`, `Permissions-Policy` e `Strict-Transport-Security`.
- **Requisições**: `credentials: "omit"` (sem cookies, logo sem risco de CSRF), `referrerPolicy: "no-referrer"` e URL da API fixada por variável de ambiente no build.
- **Validação**: os formulários usam as mesmas restrições do backend (tamanhos máximos, e-mail, senha de 8 a 72 caracteres, padrão de placa e CPF, limites numéricos) para feedback imediato; erros de validação do servidor são mostrados junto ao campo correspondente. O servidor continua sendo a validação definitiva.
- **Parâmetros de rota**: o `:id` das URLs é validado como inteiro positivo antes de qualquer chamada.
- **Campos de senha** com `autocomplete` correto (`current-password`/`new-password`) para gerenciadores de senha.
- **Segredos**: `.env` está no `.gitignore`; somente `.env.example` é versionado. O frontend não contém chaves nem credenciais.
- **Dependências**: sem pacotes além dos necessários; `npm audit` sem vulnerabilidades conhecidas.

---

## 8. Dependências e seus papéis

Nenhuma dependência foi adicionada além das que já estavam no projeto: requisições usam `fetch` nativo, formulários usam a API nativa de formulários + validação HTML5, e o estado global usa o Context do React. Todas estão na versão estável mais recente compatível; a instalação ocorre sem avisos e `npm audit` retorna 0 vulnerabilidades.

### Produção (`dependencies`)

| Pacote                | Papel no sistema                                                                                                              |
| --------------------- | ----------------------------------------------------------------------------------------------------------------------------- |
| `react`             | Biblioteca de interface: componentes, estado, contexto e hooks.                                                               |
| `react-dom`         | Renderiza os componentes React no navegador (`createRoot`).                                                                 |
| `react-router`      | Roteamento SPA: rotas, rotas aninhadas/layouts, redirecionamentos de proteção, `NavLink`, `useParams`, `useNavigate`. |
| `tailwindcss`       | Estilização por classes utilitárias e tema (cores/fontes) em CSS; gera só o CSS usado.                                    |
| `@tailwindcss/vite` | Plugin que integra o Tailwind 4 ao Vite (sem `postcss`/`tailwind.config`).                                                |
| `lucide-react`      | Ícones SVG (carro, pedido, contrato, alerta...), importados individualmente para não pesar o bundle.                        |

### Desenvolvimento (`devDependencies`)

| Pacote                                 | Papel no sistema                                                                                                |
| -------------------------------------- | --------------------------------------------------------------------------------------------------------------- |
| `vite`                               | Servidor de desenvolvimento e empacotador do build de produção.                                               |
| `@vitejs/plugin-react`               | Suporte a JSX/TSX e *Fast Refresh* no Vite.                                                                   |
| `babel-plugin-react-compiler`        | React Compiler: memoriza componentes automaticamente, dispensando `useMemo`/`useCallback`/`memo` manuais. |
| `@rolldown/plugin-babel`             | Executa o Babel dentro do Vite 8 para aplicar o React Compiler.                                                 |
| `@babel/core`                        | Núcleo do Babel exigido pelo plugin acima.                                                                     |
| `typescript`                         | Tipagem estática; `npm run build` falha se houver erro de tipos.                                             |
| `@types/react`, `@types/react-dom` | Tipos do React para o TypeScript.                                                                               |
| `@types/babel__core`                 | Tipos do Babel usados em `vite.config.ts`.                                                                    |
| `@types/node`                        | Tipos do Node usados pela configuração do Vite.                                                               |
| `oxlint`                             | Linter rápido (regras de hooks e de componentes React) executado por `npm run lint`.                         |

---

## 9. Boas práticas de IHC aplicadas

- **Visibilidade do estado do sistema**: indicadores de carregamento, botões com *loading* que impedem envio duplicado, selos de status coloridos e mensagem de sucesso após salvar.
- **Prevenção e recuperação de erros**: confirmação antes de cancelar um pedido, campos validados antes do envio, mensagens do servidor em português, botão "Tentar novamente" em falhas de rede.
- **Reconhecimento em vez de memorização**: navegação fixa com ícones e rótulos, listas de automóveis em seletores, filtros sempre visíveis.
- **Orientação para o próximo passo**: telas vazias explicam o que fazer, aviso com atalho para completar o perfil antes de solicitar aluguel, e o cliente novo é levado direto ao formulário de contratante.
- **Consistência**: um único conjunto de componentes para formulários, janelas, tabelas e selos em todas as telas.
- **Acessibilidade**: rótulos associados aos campos, erros ligados por `aria-describedby`, foco visível, janelas nativas com foco preso, cores sempre acompanhadas de texto e campos e botões principais com 44 px de altura.
- **Responsividade**: layout fluido (celular a desktop), tabelas com rolagem horizontal e navegação rolável.
