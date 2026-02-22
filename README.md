# Tasklist API

API RESTful reativa para gerenciamento de tarefas e subtarefas, com autenticação JWT, controle de acesso por usuário e arquitetura não-bloqueante baseada em Spring WebFlux.

Projeto desenvolvido com foco em boas práticas de segurança, arquitetura reativa e organização de domínio.

---

## 🎯 Objetivos do Projeto

- Implementar API REST reativa utilizando WebFlux
- Aplicar autenticação segura com JWT (Access + Refresh Token)
- Garantir isolamento de dados por usuário
- Utilizar persistência reativa com R2DBC
- Implementar cache com Caffeine e Redis
- Aplicar paginação, ordenação e filtros dinâmicos
- Documentação via OpenAPI

---

## 🏗 Arquitetura e Stack

### Backend
- Spring Boot
- Spring WebFlux (programação reativa)
- Spring Security
- Spring Data R2DBC
- MySQL

### Cache
- Caffeine (in-memory)
- Redis (distribuído)

### Testes
- Spring Testcontainers

### Documentação
- OpenAPI (Swagger)

---

## 🔐 Segurança

- Autenticação baseada em JWT (Access Token + Refresh Token)
- Chaves RSA geradas por instância
- Controle de acesso por propriedade do recurso
- Retorno 404 para recursos que não pertencem ao usuário
- Apenas `/api/auth/**` é público

### Configuração via ambiente

| Variável | Padrão |
|----------|--------|
| `APP_JWT_REFRESH_DURATION` | `24h` |
| `APP_JWT_ACCESS_DURATION`  | `1h` |
| `APP_JWT_ISSUER`           | `tasklist` |

---

## 🚀 Funcionalidades

### 👤 Usuário
- Cadastro com confirmação por email
- Login com Access e Refresh Token
- Renovação de token
- Recuperação de senha
- Gerenciamento de emails vinculados
- Exclusão da própria conta

### 📌 Tarefas
- CRUD completo
- Paginação e ordenação
- Filtros por status
- Busca por conteúdo
- Seleção dinâmica de campos
- Contagem total de registros

Status possíveis:
- `PENDING`
- `IN_PROGRESS`
- `COMPLETED`

### 📎 Subtarefas
- CRUD completo
- Reordenação por posição (BEFORE / AFTER)
- Atualização em lote de status
- Paginação e filtros
- Contagem total

---

## 🐳 Execução com Docker

Produção:

```bash
docker compose -f docker-compose.yaml up
```

Desenvolvimento:

```bash
docker compose up
```

---

## 📘 Documentação da API

Ativar profile `doc`:

```bash
SPRING_PROFILES_ACTIVE=doc
```

Acessar:

```
http://host:port/swagger-ui/index
```

---

## 🧠 Conceitos Aplicados

- Programação reativa e backpressure
- Isolamento de domínio por usuário
- Segurança stateless
- Cache estratégico
- Versionamento otimista
- Arquitetura orientada a recursos
- Separação clara entre camadas (security, domain, infra)

---

## 📄 Licença

Projeto pessoal para fins de estudo e demonstração técnica.

Sem garantia de suporte ou uso comercial.
