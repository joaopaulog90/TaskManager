# TaskManager — Desafio Técnico

Sistema simplificado de gerenciamento de tarefas para equipes de desenvolvimento.

## Stack

- **Backend:** Spring Boot 3.4, Java 21, Maven Wrapper
- **Banco:** PostgreSQL 16
- **Segurança:** Spring Security + JWT (JJWT)
- **Cache:** Caffeine (resumo de tarefas por projeto)
- **Documentação:** SpringDoc OpenAPI (Swagger UI)
- **Frontend:** Angular 17+, Node 22, TypeScript (standalone components)
- **Ambiente:** Docker Compose (3 containers: postgres, backend-dev, frontend-dev)

## Como rodar

### Pré-requisitos
- Docker e Docker Compose instalados

### Subir o ambiente completo
```bash
cp .env.example .env
docker compose up -d
```

no Windows usar o comando 

```bash
copy .env.example .env
docker compose up -d
```

Os três serviços sobem automaticamente:
- **PostgreSQL** na porta 5432
- **Backend** na porta 8080 
- **Frontend** na porta 4200

### Usuário admin inicial
Na primeira inicialização, um usuário ADMIN é criado automaticamente:
- **Email:** `admin@taskmanager.com`
- **Senha:** `admin123`

Configurável via variáveis de ambiente: `ADMIN_SEED_EMAIL`, `ADMIN_SEED_SENHA`, `ADMIN_SEED_NOME`.

### Verificar status
```bash
docker compose ps
docker compose logs backend-dev
```

### Rodar testes no container
```bash
# Backend
docker compose exec -w /workspace backend-dev bash -lc "./mvnw test"

# Frontend
docker compose exec -w /workspace frontend-dev bash -c 'CHROME_BIN=/usr/bin/chromium npx ng test --watch=false --browsers=ChromeHeadlessNoSandbox'
```

### Acessos
| Serviço | URL |
|---|---|
| Frontend | http://localhost:4200 |
| Backend API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |

## Estrutura do projeto

```
task-manager/
├── backend/                # API Spring Boot
│   └── src/
│       ├── main/java/com/taskmanager/api/
│       │   ├── config/         # Segurança, OpenAPI, cache, admin seed
│       │   ├── controller/     # Endpoints REST (finos)
│       │   ├── dto/            # Request e Response DTOs
│       │   │   ├── request/
│       │   │   └── response/
│       │   ├── entity/         # Entidades JPA e enums
│       │   ├── exception/      # Handler global (RFC 7807)
│       │   ├── repository/     # Spring Data JPA + Specifications
│       │   ├── security/       # Filtro JWT, UserDetailsService
│       │   └── service/        # Regras de negócio
│       └── resources/
│           ├── application.yml
│           └── application-test.yml
├── frontend/               # Angular 17+ (diferencial)
│   └── src/app/
│       ├── core/
│       │   ├── interceptors/   # Interceptor JWT (adiciona Authorization header)
│       │   ├── models/         # Interfaces TypeScript
│       │   └── services/       # Serviços HTTP (auth, projeto, tarefa, usuario, toast)
│       └── features/
│           ├── login/          # Tela de login e cadastro
│           ├── projects/       # Lista de projetos + gerenciamento de usuários (ADMIN)
│           └── project-detail/ # Tarefas, filtros, membros, histórico (componentizado)
│               ├── project-detail.component.ts  # Componente principal (orquestrador)
│               ├── formulario-tarefa.component.ts  # Formulário de criação de tarefa
│               ├── painel-membros.component.ts     # Painel de membros do projeto
│               ├── resumo-cards.component.ts       # Cards de resumo por status
│               └── historico-tarefa-modal.component.ts  # Modal de histórico de alterações
├── compose.yaml
├── .env.example
└── README.md
```

## Decisões técnicas e tradeoffs

### Autenticação JWT stateless
JWT stateless sem refresh token para simplicidade

### Admin seed automático
Na primeira execução, se não existir nenhum usuário ADMIN, o sistema cria um automaticamente com credenciais configuráveis via `application.yml` ou variáveis de ambiente. Isso garante que o sistema sempre tenha um ponto de entrada administrativo sem intervenção manual no banco.

### PostgreSQL
Ambiente Docker usa PostgreSQL 16. Testes usam H2 no modo PostgreSQL para isolar o estado e rodar rápido sem container externo.

### Getters e Setters
Optei por não usar Lombok para manter o código totalmente legível.

### Cache com Caffeine
O endpoint de resumo por projeto (`/tasks/summary`) é cacheado com Caffeine (TTL de 10 minutos, máximo 200 entradas). Qualquer mutação de tarefa (criação, atualização, exclusão) invalida o cache do respectivo projeto via `@CacheEvict`. Cache é desabilitado automaticamente no profile de teste (`spring.cache.type: none`).

### Audit log de tarefas
Cada alteração em uma tarefa (status, prioridade, responsável, título, descrição, prazo) gera um registro na tabela `task_history` com: campo alterado, valor anterior, valor novo, autor e timestamp.

### Paginação com Spring Data Pageable
Implementado via `Pageable` do Spring Data. Retorna metadados (`totalElementos`, `totalPaginas`, `pagina`, `tamanho`) no response. Configuração padrão: 20 itens/página.

### Frontend componentizado com standalone components
Angular 17+ com standalone components.

### Notificações por polling
O frontend detecta tarefas atribuídas ao usuário logado via polling a cada 6 segundos.

### Responsividade
Media queries nos componentes principais.

## Regras de negócio implementadas

| Regra | Comportamento |
|---|---|
| Tarefa DONE → TODO | Proibido (backend 422 + validação frontend em dropdown e drag-and-drop). Pode voltar a IN_PROGRESS. |
| Tarefa CRITICAL → DONE | Apenas ADMIN. MEMBER recebe 403. |
| WIP limit (5 tarefas) | Responsável não pode ter mais de 5 tarefas IN_PROGRESS. Retorna 422 com mensagem clara. |
| Responsável deve ser membro | Atribuição de tarefa valida se o usuário é membro do projeto. |
| Visibilidade por projeto | Usuário só vê e acessa projetos dos quais é membro. |
| Criar/editar/excluir projeto | Apenas ADMIN. MEMBER não vê o formulário no frontend. |
| Gerenciar membros do projeto | Apenas ADMIN. Botão "Membros" aparece apenas para ADMIN no detalhe do projeto. |
| Excluir tarefa | Apenas ADMIN. MEMBER pode criar e editar, mas não excluir. |
| Cadastro de usuário | Qualquer pessoa pode se cadastrar. Novos usuários entram como MEMBER. |

## O que faria diferente com mais tempo

1. **Notificações via WebSocket** — substituir polling por push de eventos, eliminando requests desnecessários, hoje o front fica consultando o backend para ver se teve alteracao na tarefa.
2. **Cobertura de testes mais ampla** — testes de contrato para a API, testes de componente Angular para todos os sub-componentes.
