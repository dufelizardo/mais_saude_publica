# 0006 — Estratégia de segurança e autenticação com JWT (proposta futura)

## Status

**Substituída pela [ADR-0054](./0054-modelo-de-identidade-autorizacao-e-auditoria.md)**. Conteúdo
histórico preservado abaixo, não implementado. Hoje a API não tem nenhuma camada de
autenticação/autorização (nem Spring Security, nem JWT), apesar do `README.md` do repositório
mencionar um endpoint `/auth` que não existe no código.

> Prioridade de implementação (decidida com o usuário, na época): esta proposta entra no grupo prioritário (junto com Clean Architecture e Docker), antes de novos domínios de negócio.
>
> **Atualização ([ADR-0039](./0039-mapa-de-dominios-e-prioridades-de-arquitetura.md))**: essa prioridade não foi seguida — RH e Administrativo (novos domínios de negócio) foram construídos 100% abertos, sem nenhuma autenticação. Ao entrar no domínio Assistência (Paciente/Atendimento — dado de saúde real, sensível por LGPD), o usuário foi consultado novamente e **decidiu manter o mesmo padrão de adiar**, conscientemente, com a ressalva explícita de revisitar esta ADR antes de qualquer deploy com dado real de paciente.
>
> **Substituição ([ADR-0054](./0054-modelo-de-identidade-autorizacao-e-auditoria.md), 2026-09-26)**:
> o usuário trouxe um desenho mais rico de autorização (RBAC + escopo organizacional + catálogo de
> permissões como dado, em vez das 4 roles fixas abaixo) e pediu para formalizá-lo. O **mecanismo de
> autenticação** proposto aqui (JWT + Spring Security, fluxo de login) continua válido como candidato
> técnico — o que a ADR-0054 substitui é o **modelo de autorização** (roles fixas → Papel/Permissão/
> Escopo). Implementação de ambos continua adiada pelos mesmos motivos desta ADR.

## Contexto

O projeto lidará com dados potencialmente sensíveis de saúde pública. Seria necessário controle de acesso para: proteger dados sensíveis (LGPD), diferenciar perfis de usuário, auditar operações, e permitir integração futura com outros sistemas.

Forças e restrições: exigência de API stateless e escalável; múltiplos tipos de cliente (web, mobile, integrações); autenticação não pode degradar performance; preferência por solução madura e bem documentada.

## Decisão

Adotar **JWT** (JSON Web Tokens) com **Spring Security** e **OAuth2 Resource Server**.

- Fluxo: login (`/api/auth/login`) com credenciais → servidor gera JWT com claims (`sub`, `roles`, `scope`, `iat`, `exp`, `iss`) → cliente envia `Authorization: Bearer <token>` → servidor valida assinatura/expiração/permissões a cada requisição.
- Perfis de acesso propostos: `ADMIN` (administração completa), `GESTOR` (gestão de unidades/profissionais), `PROFISSIONAL` (operação do dia a dia), `CONSULTOR` (somente leitura).
- Controle de rotas via `SecurityFilterChain` (matchers por método HTTP + role) e de método via `@PreAuthorize("hasRole('ADMIN')")` em casos de uso sensíveis (ex.: deletar unidade).

## Trade-offs considerados

| Alternativa | Vantagens | Desvantagens | Decisão |
|---|---|---|---|
| JWT (stateless) | Escalável, sem estado no servidor | Token não é revogável até expirar (precisa de blacklist para logout) | ✅ Escolhida |
| Sessão (stateful) | Invalidação fácil, simples | Escala mal, exige sticky sessions, não ideal para API REST | ❌ Rejeitada |
| OAuth2 com Authorization Code (Keycloak) | Padrão de mercado, delega autenticação | Complexo demais para o estágio atual do projeto | ⚠️ Futuro |
| API Keys | Simples | Pouco seguro, difícil gerenciar permissões finas | ❌ Rejeitada |

## Consequências

**Positivas**: qualquer instância pode validar o token sem estado compartilhado; autenticação desacoplada do domínio; controle de acesso granular via roles e `@PreAuthorize`; preparado para integrações externas.

**Negativas**: configuração inicial de Spring Security + JWT é elaborada; tokens continuam válidos até expirar — precisa de estratégia de blacklist/refresh para logout real; validação de JWT adiciona um pequeno overhead por requisição; gerenciamento de secret precisa de cuidado (variáveis de ambiente, não hardcoded).

**Neutras**: adiciona dependências (`spring-boot-starter-security`, biblioteca JWT); Swagger precisa ser configurado para permitir enviar o token nos endpoints protegidos.
