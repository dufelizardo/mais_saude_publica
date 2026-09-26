# DER (Diagrama Entidade-Relacionamento) - Mais Saúde Pública API

**Data:** 2026-09-06  
**Versão:** 1.0  
**Autor:** Equipe de Desenvolvimento  

> ⚠️ **Este é um modelo de dados FUTURO/proposto**, para uma expansão de domínio clínico (Usuário, Paciente, Profissional, Atendimento, Consulta, Procedimento, Equipe, Agendamento, Notificação, Auditoria etc.) que **ainda não existe no código**. Ele não reflete o schema atual do banco. Para o schema real e implementado hoje, veja [`DER-atual.md`](./DER-atual.md) (hierarquia `UnidadeDeSaude`) e [`MODELO-RH.md`](../rh/MODELO-RH.md) (módulo de RH). Mantido lado a lado com o modelo atual como registro histórico da proposta original.

> 🔄 **Nota de reconciliação (ver [ADR-0039](./0039-mapa-de-dominios-e-prioridades-de-arquitetura.md) e [`MAPA-DE-DOMINIOS.md`](../MAPA-DE-DOMINIOS.md))**: esta proposta é de 2026-09-06, **antes** de RH e Administrativo existirem, e ficou desatualizada em pontos que agora conflitam com o que foi realmente implementado — está marcada entidade a entidade abaixo. As seções de `ATENDIMENTO`/`CONSULTA`/`PROCEDIMENTO`/`AGENDAMENTO` continuam sendo a base útil para a próxima onda (ver seção **"Modelo revisado para a próxima onda"** ao final deste documento, que substitui o DDL original). O DDL original foi removido por estar truncado e desatualizado — o schema real sempre nasce do JPA quando cada entidade é implementada (mesmo padrão de RH/Administrativo), não de um DDL escrito à mão antes do código.

---

## 📊 **Diagrama Entidade-Relacionamento (DER)**

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                                                                                     │
│  ┌─────────────┐          ┌──────────────────┐          ┌─────────────────┐       │
│  │   USUARIO   │          │   PROFISSIONAL   │          │     PACIENTE    │       │
│  ├─────────────┤          ├──────────────────┤          ├─────────────────┤       │
│  │ id (PK)     │◄─────────│ id (PK)          │          │ id (PK)         │       │
│  │ email       │          │ usuario_id (FK)  │          │ usuario_id (FK) │       │
│  │ senha_hash  │          │ unidade_id (FK)  │──────────│ cpf             │       │
│  │ nome        │          │ nome_completo    │          │ nome_completo   │       │
│  │ roles       │          │ especialidade    │          │ data_nascimento │       │
│  │ ativo       │          │ conselho         │          │ sexo            │       │
│  │ criado_em   │          │ numero_conselho  │          │ cartao_sus      │       │
│  │ atualizado_em│         │ data_contratacao │          │ telefone        │       │
│  └─────────────┘          │ data_desligamento│          │ email           │       │
│         │                  │ ativo            │          │ endereco_id (FK)│──────┐│
│         │                  │ criado_em        │          │ criado_em       │      ││
│         │                  │ atualizado_em    │          │ atualizado_em   │      ││
│         │                  └──────────────────┘          └─────────────────┘      ││
│         │                           │                           │                  ││
│         │                           │                           │                  ││
│         │                  ┌────────┴────────┐                  │                  ││
│         │                  │                 │                  │                  ││
│         │          ┌───────▼──────┐ ┌────────▼────────┐        │                  ││
│         │          │ ATENDIMENTO  │ │    CONSULTA     │        │                  ││
│         │          ├──────────────┤ ├─────────────────┤        │                  ││
│         │          │ id (PK)      │ │ id (PK)         │        │                  ││
│         │          │ paciente_id  │ │ atendimento_id  │        │                  ││
│         │          │ profissional │ │ profissional_id │        │                  ││
│         │          │ unidade_id   │ │ data_hora       │        │                  ││
│         │          │ tipo         │ │ tipo_consulta   │        │                  ││
│         │          │ data_hora    │ │ queixa_principal│        │                  ││
│         │          │ status       │ │ diagnostico     │        │                  ││
│         │          │ criado_em    │ │ receituario     │        │                  ││
│         │          │ atualizado_em│ │ exames_solicitad│        │                  ││
│         │          └──────────────┘ │ retorno         │        │                  ││
│         │                  │        │ criado_em       │        │                  ││
│         │                  │        │ atualizado_em   │        │                  ││
│         │                  │        └─────────────────┘        │                  ││
│         │                  │                                    │                  ││
│         │          ┌───────▼────────┐                           │                  ││
│         │          │   PROCEDIMENTO │                           │                  ││
│         │          ├────────────────┤                           │                  ││
│         │          │ id (PK)        │                           │                  ││
│         │          │ consulta_id    │                           │                  ││
│         │          │ tipo           │                           │                  ││
│         │          │ descricao      │                           │                  ││
│         │          │ profissional_id│                           │                  ││
│         │          │ data_realizacao│                           │                  ││
│         │          │ status         │                           │                  ││
│         │          │ criado_em      │                           │                  ││
│         │          │ atualizado_em  │                           │                  ││
│         │          └────────────────┘                           │                  ││
│         │                                    ┌─────────────────▼────┐            ││
│         │                                    │     ENDERECO         │            ││
│         │                                    ├──────────────────────┤            ││
│         │                                    │ id (PK)             │◄───────────┘│
│         │                                    │ logradouro          │             │
│         │                                    │ numero              │             │
│         │                                    │ complemento         │             │
│         │                                    │ bairro              │             │
│         │                                    │ cidade              │             │
│         │                                    │ estado              │             │
│         │                                    │ cep                 │             │
│         │                                    │ latitude            │             │
│         │                                    │ longitude           │             │
│         │                                    └──────────────────────┘             │
│         │                                                                         │
│  ┌──────▼──────────┐          ┌─────────────────┐                               │
│  │ UNIDADE_SAUDE  │          │  ESTRUTURA_ORG  │                               │
│  ├─────────────────┤          ├─────────────────┤                               │
│  │ id (PK)         │◄─────────│ id (PK)         │                               │
│  │ endereco_id (FK)│──────────│ unidade_id (FK) │                               │
│  │ nome            │          │ nome             │                               │
│  │ cnes            │          │ nivel            │                               │
│  │ tipo            │          │ pai_id (FK)     │───┐                           │
│  │ telefone        │          │ descricao       │   │ (auto-relacionamento)     │
│  │ email           │          │ criado_em       │   │                           │
│  │ site            │          │ atualizado_em   │   │                           │
│  │ status          │          └─────────────────┘   │                           │
│  │ horario_abertura│                   │            │                           │
│  │ horario_fechamento│                 │            │                           │
│  │ capacidade      │                   │            │                           │
│  │ criado_em       │                   │            │                           │
│  │ atualizado_em   │                   │            │                           │
│  └─────────────────┘                   │            │                           │
│         │                              │            │                           │
│         │                     ┌────────▼────────┐   │                           │
│         │                     │   EQUIPE        │   │                           │
│         │                     ├─────────────────┤   │                           │
│         │                     │ id (PK)         │   │                           │
│         │                     │ estrutura_id(FK)│   │                           │
│         │                     │ nome            │   │                           │
│         │                     │ tipo            │   │                           │
│         │                     │ criado_em       │   │                           │
│         │                     │ atualizado_em   │   │                           │
│         │                     └─────────────────┘   │                           │
│         │                              │            │                           │
│         │                     ┌────────▼────────┐   │                           │
│         │                     │ EQUIPE_PROFIS  │   │                           │
│         │                     ├─────────────────┤   │                           │
│         │                     │ id (PK)         │   │                           │
│         │                     │ equipe_id (FK)  │   │                           │
│         │                     │ profissional_id │   │                           │
│         │                     │ cargo           │   │                           │
│         │                     │ data_entrada    │   │                           │
│         │                     │ data_saida      │   │                           │
│         │                     │ ativo           │   │                           │
│         │                     └─────────────────┘   │                           │
│         │                              │            │                           │
│         │                              │            │                           │
│  ┌──────▼──────────┐          ┌───────▼────────┐   │                           │
│  │   ESPECIALIDADE │          │   HORARIO      │   │                           │
│  ├─────────────────┤          ├────────────────┤   │                           │
│  │ id (PK)         │          │ id (PK)        │   │                           │
│  │ nome            │          │ profissional_id│   │                           │
│  │ descricao       │          │ dia_semana     │   │                           │
│  │ ativo           │          │ hora_inicio    │   │                           │
│  └─────────────────┘          │ hora_fim       │   │                           │
│         │                     │ intervalo      │   │                           │
│         │                     │ criado_em      │   │                           │
│         │                     │ atualizado_em  │   │                           │
│         │                     └────────────────┘   │                           │
│         │                              │            │                           │
│         │                     ┌────────▼────────┐   │                           │
│         │                     │  AGENDAMENTO   │   │                           │
│         │                     ├─────────────────┤   │                           │
│         │                     │ id (PK)         │   │                           │
│         │                     │ paciente_id     │   │                           │
│         │                     │ profissional_id │   │                           │
│         │                     │ data_hora       │   │                           │
│         │                     │ status          │   │                           │
│         │                     │ tipo            │   │                           │
│         │                     │ observacao      │   │                           │
│         │                     │ criado_em       │   │                           │
│         │                     │ atualizado_em   │   │                           │
│         │                     └─────────────────┘   │                           │
│         │                              │            │                           │
│         │                     ┌────────▼────────┐   │                           │
│         │                     │  NOTIFICACAO   │   │                           │
│         │                     ├─────────────────┤   │                           │
│         │                     │ id (PK)         │   │                           │
│         │                     │ usuario_id (FK) │   │                           │
│         │                     │ titulo          │   │                           │
│         │                     │ mensagem        │   │                           │
│         │                     │ tipo            │   │                           │
│         │                     │ lida            │   │                           │
│         │                     │ data_envio      │   │                           │
│         │                     │ criado_em       │   │                           │
│         │                     └─────────────────┘   │                           │
│         │                              │            │                           │
│         │                     ┌────────▼────────┐   │                           │
│         │                     │  AUDITORIA      │   │                           │
│         │                     ├─────────────────┤   │                           │
│         │                     │ id (PK)         │   │                           │
│         │                     │ usuario_id (FK) │   │                           │
│         │                     │ acao            │   │                           │
│         │                     │ entidade        │   │                           │
│         │                     │ entidade_id     │   │                           │
│         │                     │ dados_anteriores│   │                           │
│         │                     │ dados_novos     │   │                           │
│         │                     │ ip              │   │                           │
│         │                     │ data_hora       │   │                           │
│         │                     └─────────────────┘   │                           │
│         │                                            │                           │
│         └────────────────────────────────────────────┘                           │
│                                                                                     │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 📋 **Descrição Detalhada das Entidades**

### 1. **USUARIO** (Tabela Base de Autenticação) — ⚠️ SUPERSEDIDA

> Nenhuma entidade `Usuario` foi criada. A ADR-0006 (JWT/autenticação) segue "Proposta", adiada
> conscientemente mesmo para o domínio clínico (ver ADR-0039) — RH e Administrativo inteiros foram
> construídos sem nenhuma autenticação, e a próxima onda (Paciente/Atendimento) segue o mesmo
> padrão por decisão explícita. Esta tabela fica como registro da proposta original, não como algo
> a implementar na próxima onda.

Armazena as credenciais e perfis de acesso ao sistema.

| **Campo** | **Tipo** | **Descrição** | **Restrições** |
|-----------|----------|---------------|----------------|
| id | UUID | Identificador único | PK, NOT NULL |
| email | VARCHAR(255) | E-mail do usuário | UNIQUE, NOT NULL |
| senha_hash | VARCHAR(255) | Hash da senha (BCrypt) | NOT NULL |
| nome | VARCHAR(255) | Nome completo | NOT NULL |
| roles | VARCHAR(255) | Perfis (ADMIN, GESTOR, PROFISSIONAL, CONSULTOR) | NOT NULL |
| ativo | BOOLEAN | Indica se o usuário está ativo | DEFAULT TRUE |
| criado_em | TIMESTAMP | Data de criação | DEFAULT CURRENT_TIMESTAMP |
| atualizado_em | TIMESTAMP | Data da última atualização | DEFAULT CURRENT_TIMESTAMP |

**Relacionamentos:**
- Um para Um com `PROFISSIONAL` (opcional - nem todo usuário é profissional)
- Um para Um com `PACIENTE` (opcional - nem todo usuário é paciente)
- Um para Muitos com `NOTIFICACAO`
- Um para Muitos com `AUDITORIA`

---

### 2. **PROFISSIONAL** — ⚠️ SUPERSEDIDA

> O RH implementou um `Profissional` real e muito mais rico que esta casca (matrícula automática
> como chave única de verdade — ADR-0017 —, CPF não único, endereço embutido, 20+ entidades
> satélite: `Cargo`, `Lotacao`, `Afastamento`, etc. — ver [`MODELO-RH.md`](../rh/MODELO-RH.md)). Não
> existe (nem existirá) um `usuario_id` acoplando `Profissional` a autenticação. Qualquer domínio
> novo que precise referenciar um profissional deve usar o `Profissional` real, **por matrícula**,
> nunca recriar esta forma — mesmo princípio já usado pelo Administrativo (ADR-0034).

Profissionais de saúde que atuam no sistema.

| **Campo** | **Tipo** | **Descrição** | **Restrições** |
|-----------|----------|---------------|----------------|
| id | UUID | Identificador único | PK, NOT NULL |
| usuario_id | UUID | Referência ao usuário | FK (USUARIO) |
| unidade_id | UUID | Unidade de lotação | FK (UNIDADE_SAUDE) |
| nome_completo | VARCHAR(255) | Nome completo | NOT NULL |
| especialidade | VARCHAR(100) | Especialidade médica | |
| conselho | VARCHAR(20) | Tipo de conselho (CRM, COREN, etc.) | |
| numero_conselho | VARCHAR(50) | Número do registro no conselho | UNIQUE |
| data_contratacao | DATE | Data de contratação | |
| data_desligamento | DATE | Data de desligamento | |
| ativo | BOOLEAN | Indica se está ativo | DEFAULT TRUE |
| criado_em | TIMESTAMP | Data de criação | DEFAULT CURRENT_TIMESTAMP |
| atualizado_em | TIMESTAMP | Data da última atualização | DEFAULT CURRENT_TIMESTAMP |

**Relacionamentos:**
- Um para Um com `USUARIO`
- Muitos para Um com `UNIDADE_SAUDE`
- Muitos para Muitos com `EQUIPE` (via `EQUIPE_PROFIS`)
- Um para Muitos com `ATENDIMENTO`
- Um para Muitos com `CONSULTA` (como profissional responsável)
- Um para Muitos com `PROCEDIMENTO`
- Um para Muitos com `HORARIO`
- Um para Muitos com `AGENDAMENTO`

---

### 3. **PACIENTE**
Pacientes atendidos no sistema de saúde.

| **Campo** | **Tipo** | **Descrição** | **Restrições** |
|-----------|----------|---------------|----------------|
| id | UUID | Identificador único | PK, NOT NULL |
| usuario_id | UUID | Referência ao usuário | FK (USUARIO) |
| endereco_id | UUID | Endereço do paciente | FK (ENDERECO) |
| cpf | VARCHAR(14) | CPF do paciente | UNIQUE, NOT NULL |
| nome_completo | VARCHAR(255) | Nome completo | NOT NULL |
| data_nascimento | DATE | Data de nascimento | NOT NULL |
| sexo | VARCHAR(20) | Sexo (MASCULINO, FEMININO, OUTRO) | |
| cartao_sus | VARCHAR(20) | Número do Cartão SUS | UNIQUE |
| telefone | VARCHAR(20) | Telefone para contato | |
| email | VARCHAR(255) | E-mail do paciente | |
| criado_em | TIMESTAMP | Data de criação | DEFAULT CURRENT_TIMESTAMP |
| atualizado_em | TIMESTAMP | Data da última atualização | DEFAULT CURRENT_TIMESTAMP |

**Relacionamentos:**
- Um para Um com `USUARIO`
- Muitos para Um com `ENDERECO`
- Um para Muitos com `ATENDIMENTO`
- Um para Muitos com `CONSULTA`
- Um para Muitos com `AGENDAMENTO`

---

### 4. **UNIDADE_SAUDE** — ⚠️ SUPERSEDIDA

> A entidade real (`UnidadeDeSaude`, ver [`DER-atual.md`](./DER-atual.md)) é uma única tabela
> **autorreferenciada de 5 níveis** (Federal → Estadual → Municipal → Regional → Unidade de Saúde,
> tipo `UBS`/`HOSPITAL`/... — ADR-0002/0009/0013), não este modelo plano por `cnes`. Qualquer
> domínio novo referencia a `UnidadeDeSaude` real pelo `uuid`, nunca recria uma tabela paralela.
> Setor Administrativo (ADR-0030) já segue essa regra.

Unidades de saúde (hospitais, UBS, clínicas, etc.).

| **Campo** | **Tipo** | **Descrição** | **Restrições** |
|-----------|----------|---------------|----------------|
| id | UUID | Identificador único | PK, NOT NULL |
| endereco_id | UUID | Endereço da unidade | FK (ENDERECO) |
| nome | VARCHAR(255) | Nome da unidade | NOT NULL |
| cnes | VARCHAR(10) | Código CNES | UNIQUE, NOT NULL |
| tipo | VARCHAR(50) | Tipo (HOSPITAL, UBS, CLINICA, etc.) | NOT NULL |
| telefone | VARCHAR(20) | Telefone da unidade | |
| email | VARCHAR(255) | E-mail da unidade | |
| site | VARCHAR(255) | Website da unidade | |
| status | VARCHAR(20) | Status (ATIVO, INATIVO, EM_OBRAS) | DEFAULT 'ATIVO' |
| horario_abertura | TIME | Horário de abertura | |
| horario_fechamento | TIME | Horário de fechamento | |
| capacidade | INTEGER | Capacidade de atendimento | |
| criado_em | TIMESTAMP | Data de criação | DEFAULT CURRENT_TIMESTAMP |
| atualizado_em | TIMESTAMP | Data da última atualização | DEFAULT CURRENT_TIMESTAMP |

**Relacionamentos:**
- Muitos para Um com `ENDERECO`
- Um para Muitos com `PROFISSIONAL`
- Um para Muitos com `ATENDIMENTO`
- Um para Muitos com `ESTRUTURA_ORG`

---

### 5. **ENDERECO** — ⚠️ FORMA REVISADA

> No código real, `Endereco` é um `@Embeddable` **embutido** em cada entidade que precisa dele
> (`UnidadeDeSaude`, `Profissional`), não uma tabela própria com FK — evita join desnecessário para
> um dado que nunca é consultado sozinho. A próxima onda (`Paciente`) segue o mesmo padrão. Ver
> [`DER-atual.md`](./DER-atual.md).

Endereços de pacientes, profissionais e unidades.

| **Campo** | **Tipo** | **Descrição** | **Restrições** |
|-----------|----------|---------------|----------------|
| id | UUID | Identificador único | PK, NOT NULL |
| logradouro | VARCHAR(255) | Nome da rua/avenida | NOT NULL |
| numero | VARCHAR(20) | Número do imóvel | |
| complemento | VARCHAR(100) | Complemento (apto, sala, etc.) | |
| bairro | VARCHAR(100) | Bairro | NOT NULL |
| cidade | VARCHAR(100) | Cidade | NOT NULL |
| estado | VARCHAR(2) | Estado (UF) | NOT NULL |
| cep | VARCHAR(10) | Código postal | |
| latitude | DECIMAL(10,8) | Latitude (geolocalização) | |
| longitude | DECIMAL(11,8) | Longitude (geolocalização) | |

**Relacionamentos:**
- Um para Muitos com `PACIENTE`
- Um para Muitos com `UNIDADE_SAUDE`

---

### 6. **ATENDIMENTO**
Atendimentos realizados aos pacientes.

| **Campo** | **Tipo** | **Descrição** | **Restrições** |
|-----------|----------|---------------|----------------|
| id | UUID | Identificador único | PK, NOT NULL |
| paciente_id | UUID | Paciente atendido | FK (PACIENTE) |
| profissional_id | UUID | Profissional responsável | FK (PROFISSIONAL) |
| unidade_id | UUID | Unidade onde ocorreu | FK (UNIDADE_SAUDE) |
| tipo | VARCHAR(50) | Tipo (CONSULTA, URGENCIA, INTERNACAO) | NOT NULL |
| data_hora | TIMESTAMP | Data e hora do atendimento | NOT NULL |
| status | VARCHAR(20) | Status (AGENDADO, EM_ANDAMENTO, CONCLUIDO) | DEFAULT 'AGENDADO' |
| criado_em | TIMESTAMP | Data de criação | DEFAULT CURRENT_TIMESTAMP |
| atualizado_em | TIMESTAMP | Data da última atualização | DEFAULT CURRENT_TIMESTAMP |

**Relacionamentos:**
- Muitos para Um com `PACIENTE`
- Muitos para Um com `PROFISSIONAL`
- Muitos para Um com `UNIDADE_SAUDE`
- Um para Muitos com `CONSULTA`

---

### 7. **CONSULTA**
Consultas médicas realizadas durante atendimentos.

| **Campo** | **Tipo** | **Descrição** | **Restrições** |
|-----------|----------|---------------|----------------|
| id | UUID | Identificador único | PK, NOT NULL |
| atendimento_id | UUID | Atendimento relacionado | FK (ATENDIMENTO) |
| profissional_id | UUID | Profissional que realizou | FK (PROFISSIONAL) |
| data_hora | TIMESTAMP | Data e hora da consulta | NOT NULL |
| tipo_consulta | VARCHAR(50) | Tipo (PRIMEIRA, RETORNO, URGENCIA) | NOT NULL |
| queixa_principal | TEXT | Motivo da consulta | |
| diagnostico | TEXT | Diagnóstico médico | |
| receituario | TEXT | Prescrição médica | |
| exames_solicitados | TEXT | Exames solicitados | |
| retorno | DATE | Data do retorno | |
| criado_em | TIMESTAMP | Data de criação | DEFAULT CURRENT_TIMESTAMP |
| atualizado_em | TIMESTAMP | Data da última atualização | DEFAULT CURRENT_TIMESTAMP |

**Relacionamentos:**
- Muitos para Um com `ATENDIMENTO`
- Muitos para Um com `PROFISSIONAL`
- Um para Muitos com `PROCEDIMENTO`

---

### 8. **PROCEDIMENTO**
Procedimentos realizados durante consultas.

| **Campo** | **Tipo** | **Descrição** | **Restrições** |
|-----------|----------|---------------|----------------|
| id | UUID | Identificador único | PK, NOT NULL |
| consulta_id | UUID | Consulta relacionada | FK (CONSULTA) |
| profissional_id | UUID | Profissional que realizou | FK (PROFISSIONAL) |
| tipo | VARCHAR(100) | Tipo de procedimento (CIRURGIA, EXAME, etc.) | NOT NULL |
| descricao | TEXT | Descrição detalhada | |
| data_realizacao | TIMESTAMP | Data da realização | NOT NULL |
| status | VARCHAR(20) | Status (AGENDADO, REALIZADO, CANCELADO) | |
| criado_em | TIMESTAMP | Data de criação | DEFAULT CURRENT_TIMESTAMP |
| atualizado_em | TIMESTAMP | Data da última atualização | DEFAULT CURRENT_TIMESTAMP |

**Relacionamentos:**
- Muitos para Um com `CONSULTA`
- Muitos para Um com `PROFISSIONAL`

---

### 9. **ESTRUTURA_ORG**
Estrutura organizacional das unidades (departamentos, setores).

| **Campo** | **Tipo** | **Descrição** | **Restrições** |
|-----------|----------|---------------|----------------|
| id | UUID | Identificador único | PK, NOT NULL |
| unidade_id | UUID | Unidade relacionada | FK (UNIDADE_SAUDE) |
| nome | VARCHAR(255) | Nome da estrutura | NOT NULL |
| nivel | VARCHAR(50) | Nível (DIRETORIA, DEPARTAMENTO, SETOR) | NOT NULL |
| pai_id | UUID | Referência à estrutura pai | FK (auto) |
| descricao | TEXT | Descrição da estrutura | |
| criado_em | TIMESTAMP | Data de criação | DEFAULT CURRENT_TIMESTAMP |
| atualizado_em | TIMESTAMP | Data da última atualização | DEFAULT CURRENT_TIMESTAMP |

**Relacionamentos:**
- Muitos para Um com `UNIDADE_SAUDE`
- Auto-relacionamento (estrutura hierárquica)
- Um para Muitos com `EQUIPE`

---

### 10. **EQUIPE**
Equipes dentro da estrutura organizacional.

| **Campo** | **Tipo** | **Descrição** | **Restrições** |
|-----------|----------|---------------|----------------|
| id | UUID | Identificador único | PK, NOT NULL |
| estrutura_id | UUID | Estrutura organizacional | FK (ESTRUTURA_ORG) |
| nome | VARCHAR(255) | Nome da equipe | NOT NULL |
| tipo | VARCHAR(50) | Tipo (CLINICA, ENFERMAGEM, ADMIN) | NOT NULL |
| criado_em | TIMESTAMP | Data de criação | DEFAULT CURRENT_TIMESTAMP |
| atualizado_em | TIMESTAMP | Data da última atualização | DEFAULT CURRENT_TIMESTAMP |

**Relacionamentos:**
- Muitos para Um com `ESTRUTURA_ORG`
- Muitos para Muitos com `PROFISSIONAL` (via `EQUIPE_PROFIS`)

---

### 11. **EQUIPE_PROFIS**
Tabela de associação entre equipes e profissionais.

| **Campo** | **Tipo** | **Descrição** | **Restrições** |
|-----------|----------|---------------|----------------|
| id | UUID | Identificador único | PK, NOT NULL |
| equipe_id | UUID | Equipe relacionada | FK (EQUIPE) |
| profissional_id | UUID | Profissional relacionado | FK (PROFISSIONAL) |
| cargo | VARCHAR(100) | Cargo na equipe | |
| data_entrada | DATE | Data de entrada na equipe | |
| data_saida | DATE | Data de saída da equipe | |
| ativo | BOOLEAN | Indica se está ativo | DEFAULT TRUE |

**Relacionamentos:**
- Muitos para Um com `EQUIPE`
- Muitos para Um com `PROFISSIONAL`

---

### 12. **HORARIO**
Horários de atendimento dos profissionais.

| **Campo** | **Tipo** | **Descrição** | **Restrições** |
|-----------|----------|---------------|----------------|
| id | UUID | Identificador único | PK, NOT NULL |
| profissional_id | UUID | Profissional relacionado | FK (PROFISSIONAL) |
| dia_semana | INTEGER | Dia da semana (1=DOMINGO...7=SABADO) | NOT NULL |
| hora_inicio | TIME | Horário de início | NOT NULL |
| hora_fim | TIME | Horário de fim | NOT NULL |
| intervalo | INTEGER | Intervalo em minutos | DEFAULT 15 |
| criado_em | TIMESTAMP | Data de criação | DEFAULT CURRENT_TIMESTAMP |
| atualizado_em | TIMESTAMP | Data da última atualização | DEFAULT CURRENT_TIMESTAMP |

**Relacionamentos:**
- Muitos para Um com `PROFISSIONAL`

---

### 13. **AGENDAMENTO**
Agendamentos de consultas e procedimentos.

| **Campo** | **Tipo** | **Descrição** | **Restrições** |
|-----------|----------|---------------|----------------|
| id | UUID | Identificador único | PK, NOT NULL |
| paciente_id | UUID | Paciente agendado | FK (PACIENTE) |
| profissional_id | UUID | Profissional agendado | FK (PROFISSIONAL) |
| data_hora | TIMESTAMP | Data e hora agendada | NOT NULL |
| status | VARCHAR(20) | Status (AGENDADO, CONFIRMADO, REALIZADO, CANCELADO) | NOT NULL |
| tipo | VARCHAR(50) | Tipo (CONSULTA, PROCEDIMENTO, RETORNO) | NOT NULL |
| observacao | TEXT | Observações do agendamento | |
| criado_em | TIMESTAMP | Data de criação | DEFAULT CURRENT_TIMESTAMP |
| atualizado_em | TIMESTAMP | Data da última atualização | DEFAULT CURRENT_TIMESTAMP |

**Relacionamentos:**
- Muitos para Um com `PACIENTE`
- Muitos para Um com `PROFISSIONAL`

---

### 14. **NOTIFICACAO**
Notificações para usuários do sistema.

| **Campo** | **Tipo** | **Descrição** | **Restrições** |
|-----------|----------|---------------|----------------|
| id | UUID | Identificador único | PK, NOT NULL |
| usuario_id | UUID | Usuário destinatário | FK (USUARIO) |
| titulo | VARCHAR(255) | Título da notificação | NOT NULL |
| mensagem | TEXT | Mensagem da notificação | NOT NULL |
| tipo | VARCHAR(50) | Tipo (SISTEMA, AGENDAMENTO, ALERTA) | NOT NULL |
| lida | BOOLEAN | Indica se foi lida | DEFAULT FALSE |
| data_envio | TIMESTAMP | Data de envio | DEFAULT CURRENT_TIMESTAMP |
| criado_em | TIMESTAMP | Data de criação | DEFAULT CURRENT_TIMESTAMP |

**Relacionamentos:**
- Muitos para Um com `USUARIO`

---

### 15. **AUDITORIA**
Log de auditoria de ações no sistema.

| **Campo** | **Tipo** | **Descrição** | **Restrições** |
|-----------|----------|---------------|----------------|
| id | UUID | Identificador único | PK, NOT NULL |
| usuario_id | UUID | Usuário que executou a ação | FK (USUARIO) |
| acao | VARCHAR(100) | Ação executada (CREATE, UPDATE, DELETE) | NOT NULL |
| entidade | VARCHAR(100) | Entidade afetada | NOT NULL |
| entidade_id | UUID | ID da entidade afetada | |
| dados_anteriores | JSONB | Estado anterior (em JSON) | |
| dados_novos | JSONB | Estado novo (em JSON) | |
| ip | VARCHAR(45) | IP de origem | |
| data_hora | TIMESTAMP | Data e hora da ação | DEFAULT CURRENT_TIMESTAMP |

**Relacionamentos:**
- Muitos para Um com `USUARIO`

---

## 🔗 **Relacionamentos Principais**

### Hierarquia Estrutural
```
UNIDADE_SAUDE (1) ───────────── (N) ESTRUTURA_ORG
                                      │
                                      │ (auto-relacionamento pai/filho)
                                      │
ESTRUTURA_ORG (1) ───────────── (N) EQUIPE
                                      │
                                      │ (via EQUIPE_PROFIS)
                                      │
EQUIPE (N) ──────────────────── (N) PROFISSIONAL
```

### Fluxo de Atendimento
```
PACIENTE (1) ───────────── (N) ATENDIMENTO (1) ───────────── (N) CONSULTA (1) ───────────── (N) PROCEDIMENTO
    │                                │                               │
    │                                │                               │
    └─────────────── (N) AGENDAMENTO ──────────────── (N) ──────────┘
```

### Acessos e Autenticação
```
USUARIO (1) ───────────── (1) PROFISSIONAL
    │
    │
    └─────────────── (1) PACIENTE
    │
    │
    └─────────────── (N) NOTIFICACAO
    │
    │
    └─────────────── (N) AUDITORIA
```

---

## 🔄 Modelo revisado para a próxima onda (Paciente → Atendimento → Prontuário)

> Substitui o antigo "Script SQL (DDL - PostgreSQL)" desta seção, que estava truncado no meio de
> `CREATE TABLE consulta` e desatualizado (referenciava `usuario_id`/`unidade_saude` na forma
> supersedida). Ver decisões completas na [ADR-0039](./0039-mapa-de-dominios-e-prioridades-de-arquitetura.md)
> e no [`MAPA-DE-DOMINIOS.md`](../MAPA-DE-DOMINIOS.md). Nenhuma destas entidades está implementada
> ainda — este é o desenho de referência para quando essa onda começar, seguindo a mesma disciplina
> de "incrementos pequenos e discutidos" já usada em RH e Administrativo. O schema real (JPA/DDL)
> só nasce quando cada entidade for de fato implementada, não antes.

### Paciente — ✅ Implementado (ADR-0040)

Identidade da pessoa atendida pela rede — **sem** `usuario_id` (não há autenticação, decisão
mantida na ADR-0039) e **sem** entidade `Pessoa` compartilhada com `Profissional` (rejeitado por
YAGNI na ADR-0039; vínculo fraco por CPF resolve o caso raro de alguém ser as duas coisas, mesmo
princípio da ADR-0014).

| Campo | Tipo | Observações |
|---|---|---|
| `uuid` | UUID (PK) | |
| `nome` | String | |
| `cpf` | String | não único — mesmo motivo do `Profissional` (ADR-0017): uma pessoa pode ter mais de um registro ao longo do tempo |
| `cartaoSus` | String | identificador de referência (papel equivalente ao da matrícula do `Profissional`) |
| `dataNascimento` | LocalDate | |
| `sexo` | enum | |
| `endereco` | `@Embeddable Endereco` | reaproveita o mesmo padrão embutido de `UnidadeDeSaude`/`Profissional`, não uma tabela `ENDERECO` própria |
| `telefones` | `List<String>` | mesmo padrão de `Profissional.telefones` |
| `email` | String | |
| `ativo` | boolean | |

### Atendimento — ✅ Implementado (ADR-0041)

Registro de entrada do paciente na rede — referencia os modelos reais, não os supersedidos.

| Campo | Tipo | Observações |
|---|---|---|
| `uuid` | UUID (PK) | |
| `paciente` | FK `Paciente` | por `uuid` (Paciente não tem um equivalente à matrícula ainda) |
| `profissionalMatricula` | String | FK direta ao `Profissional` real, **por matrícula** — nunca por uuid interno (ADR-0034) |
| `unidade` | FK `UnidadeDeSaude` | a entidade real de 5 níveis, não o `UNIDADE_SAUDE` supersedido |
| `setor` | FK `Setor` (opcional) | reaproveita o Setor Administrativo (ADR-0030) quando fizer sentido (ex.: "Setor de Vacinação") |
| `agendamento` | FK `Agendamento` (opcional) | referência opcional, nunca obrigatória — mesmo padrão de `NecessidadeDePessoal.vagaAssociada` (ADR-0036): um atendimento pode nascer de um agendamento ou ser espontâneo (acolhimento) |
| `tipo` | enum | `CONSULTA`, `URGENCIA`, `INTERNACAO`, ... |
| `status` | enum | `AGENDADO`, `EM_ANDAMENTO`, `CONCLUIDO` |
| `dataHora` | LocalDateTime | |

### Consulta — ✅ Implementado (ADR-0043)

Mantida como no desenho original (campo a campo), apenas trocando as FKs supersedidas pelas reais
(`Atendimento` acima, `profissionalMatricula` em vez de `profissional_id`). `diagnostico`,
`receituario` e `exames_solicitados` continuam como campos de texto — sem entidades `Diagnostico`/
`Exame`/`Prescricao` próprias por ora (YAGNI: nenhum requisito concreto pede consulta estruturada
desses dados ainda; revisitar quando Farmácia/Laboratório entrarem no roadmap).

### Procedimento — ✅ Implementado (ADR-0044)

Mantido como no desenho original, apenas trocando `profissional_id` por `profissionalMatricula`.

### Agendamento — ✅ Implementado (ADR-0042)

Mantido como no desenho original (paciente, profissional, data/hora, status, tipo), com
`profissionalMatricula` no lugar de `profissional_id`. Existe independente de `Atendimento` — um
agendamento pode nunca virar atendimento (não comparecimento), e um atendimento pode não ter
agendamento (acolhimento espontâneo).

### Prontuário — não é uma tabela — ✅ Implementado (ADR-0045)

Decisão explícita (ADR-0039): `Prontuário` é uma **visão agregada de leitura** sobre os
`Atendimento`/`Consulta`/`Procedimento` de um `Paciente`. Implementado como
`GET /api/v1/prontuario/{pacienteId}` no backend (ADR-0045) — diferente do "Histórico funcional
consolidado" do RH (ADR-0026), que agrega inteiramente no frontend (`computed()` sobre sinais já
carregados pela tela), porque a Assistência ainda não tem nenhuma tela para reaproveitar dados já
carregados. Reavaliar apenas se um requisito concreto (ex.: documentos clínicos anexados) exigir
uma tabela própria — não antecipar agora.

Candidatos citados pelo usuário que **ainda não têm um lar claro** em `Atendimento`/`Consulta`/
`Procedimento` — registrados aqui para não perder, a posicionar quando a agregação for de fato
implementada: **Evolução** (registro de acompanhamento entre consultas, hoje só existe como
conceito para o RH — `HistoricoFuncional`, ADR-0026 — e precisaria de um equivalente clínico),
**Alergia** (lista por paciente, referenciada por `Consulta` mas não modelada), **Documento
clínico** (anexos — depende de o domínio transversal "Documentos", ver apêndice, existir primeiro).

---

## 📎 Apêndice — Esboços dos demais domínios

> **Não implementado, não decidido — apenas registro do material trazido pelo usuário**, para que
> nenhuma informação se perca (ver `MAPA-DE-DOMINIOS.md` §3 para a visão de responsabilidade em
> uma frase de cada um). Estes esboços não passaram pela mesma reconciliação contra o código real
> que a seção anterior passou — são candidatos a entidades/campos/fluxos, sujeitos a mudar por
> completo quando a onda de cada domínio de fato chegar e merecer sua própria conversa (mesma
> disciplina de "incrementos pequenos e discutidos" de RH/Administrativo). Nenhum destes vira ADR
> agora — só quando houver uma decisão real com trade-off para registrar (ver ADR-0039).

### Enfermagem (#8)

Processos de enfermagem, com peso maior em UPA/Hospital que em UBS.

> **`Triagem` já foi reconciliada e implementada** — ver
> [ADR-0047](./0047-triagem-primeira-entidade-da-enfermagem.md). O fluxo abaixo (Triagem antes do
> Atendimento) é o esboço original, **não** o que foi implementado: na versão real, `Triagem` é
> filha de `Atendimento` (mesmo papel estrutural de `Consulta`), e `classificacaoRisco` é um campo
> enum na própria `Triagem`, não uma entidade `ClassificacaoDeRisco` separada. `EvolucaoDeEnfermagem`,
> `AdministracaoDeMedicamento`, `Cuidado` e `Escala` continuam como esboço, não implementados.

- `Triagem` — pressão, temperatura, saturação, frequência cardíaca, peso.
- `ClassificacaoDeRisco` — resultado da triagem.
- `EvolucaoDeEnfermagem`, `AdministracaoDeMedicamento`, `Cuidado`, `Escala`.

```text
Paciente → Triagem (sinais vitais) → Classificação de risco → Atendimento médico/enfermagem
```

### Farmácia (#9)

Medicamentos e dispensação — regras próprias, deliberadamente **separado de Estoque** (#13):
medicamento tem lote/validade/controle de dispensação que material de almoxarifado não tem.

- `Medicamento`, `Lote` (validade, quantidade), `Dispensacao`, `MovimentacaoFarmacia`,
  `TransferenciaEntreUnidades`, `Perda`, `InventarioFarmacia`.

```text
Prescrição → Farmácia → Dispensação → Paciente
```

### Laboratório e Diagnóstico (#10)

- `SolicitacaoExame`, `Coleta`, `Amostra`, `ProcessamentoLaboratorial`, `Resultado`, `Laudo`,
  `EquipamentoLaboratorio`, `MaterialLaboratorio`, `ControleDeQualidade`.

```text
Atendimento → Solicitação de exame → Agendamento/Coleta → Amostra
  → Laboratório → Resultado → Laudo → Prontuário (agregação)
```

### Regulação (#11)

Conecta a rede inteira, não uma unidade só — coordena acesso a serviço que não está disponível na
unidade de origem.

- `SolicitacaoRegulacao`, `Fila`, `Prioridade`, `Vaga`, `Encaminhamento`, `Contrarreferencia`.

```text
UBS → Solicitação (ex.: cardiologia) → Regulação → Fila
  → Vaga disponível → Hospital/Especialista

UPA → Solicitação de internação → Regulação → Hospital
```

### Gestão de Leitos e Internação (#12)

Relevante principalmente para Hospital/UPA. Rótulo `GESTAO_DE_LEITOS` já reservado no catálogo
administrativo (ADR-0032/0037).

- `Leito`, `Quarto`/`Enfermaria`, `Internacao`, `Ocupacao`, `Bloqueio`, `Transferencia`.

```text
Paciente → Regulação → Internação → Leito → Transferência → Alta

Hospital
 ├── UTI (Leito 01, 02, 03)
 ├── Clínica Médica (Leito 10, 11)
 └── Pediatria (Leito 20, 21)
```

Integra com Administrativo: `Leito → Patrimônio → Manutenção`.

### Estoque e Almoxarifado (#13)

Materiais e insumos gerais — **diferente de Farmácia** (#9): luvas, máscaras, seringas, papel,
material de limpeza, material administrativo, EPI (não confundir com o `Epi` do RH, que é o EPI
*entregue a um profissional*, já implementado — este é o estoque de EPI antes da entrega).

- `Produto`, `Lote`, `Estoque`, `MovimentacaoEstoque`, `Inventario`, `Almoxarifado`.

```text
Entrada → Estoque → Movimentação → Consumo → Reposição

Almoxarifado Central
 ├── UBS A
 ├── UBS B
 ├── UPA A
 └── Hospital A
```

### Compras, Contratos e Fornecedores (#14)

- `Fornecedor`, `SolicitacaoCompra`, `Cotacao`, `Contrato`, `ItemContratado`, `Entrega`,
  `FiscalizacaoContratual`.

```text
Unidade → Necessidade → Solicitação → Compra → Fornecedor → Entrega → Estoque

Administrativo → Necessidade → Compras → Fornecedor → Contrato → Financeiro
```

### Patrimônio e Manutenção (#15)

Bens físicos: computadores, macas, respiradores, geladeiras, veículos, equipamentos médicos,
mobiliário.

- `Equipamento` (patrimônio, unidade, setor, fabricante, modelo, status, manutenção),
  `Manutencao` (preventiva/corretiva, histórico).

### Transporte Sanitário (#16)

- `Veiculo`, `Ambulancia`, `Motorista`, `SolicitacaoTransporte`, `Rota`, `AgendamentoTransporte`,
  `Transferencia`, `ManutencaoVeiculo`.

```text
UPA → Solicitação de transferência → Regulação → Ambulância → Hospital
```

Integrações: RH → motorista; Patrimônio → veículo; Paciente → passageiro; Atendimento → motivo;
Regulação → necessidade.

### Financeiro (#17)

- `Orcamento`, `Empenho`, `Despesa`, `Receita`, `Pagamento`, `CentroDeCusto`,
  `PrestacaoDeContas`.

```text
Unidade → Centro de custo → Despesa → Categoria → Valor
```

Decisão a tomar quando esta onda chegar (não decidida agora): sistema financeiro completo vs. só
registro/acompanhamento integrado a um ERP/sistema de governo externo — tendência inicial pela
segunda opção, dado o custo/risco de reimplementar regras financeiras públicas do zero.

### Qualidade (#18, parte)

- `Indicador` (meta, valor, período, unidade), `NaoConformidade`, `Incidente`, `PlanoDeAcao`,
  `Evidencia`, `Melhoria`.

```text
Não conformidade → Análise → Plano de ação → Execução → Verificação → Encerramento
```

### Auditoria (#18, parte)

Já esboçada como entidade `AUDITORIA` na proposta original (seção 15 acima) — mantida como
referência, com o entendimento de que roda **transversalmente** a todos os domínios, não só ao
clínico: `EventoDeAuditoria` (ação, entidade, entidade_id, dados_anteriores, dados_novos, ip,
usuário, data/hora). Perguntas que deve responder: quem alterou a lotação, quem dispensou o
medicamento, quem alterou o resultado do exame, quem aprovou a compra, quem autorizou a
transferência.

### Indicadores, BI e Gestão (#19)

**Regra explícita (mantida da ADR-0039): não é fonte de dado, só consumidor** — os indicadores
abaixo só existem quando os domínios operacionais que os alimentam já existirem.

Exemplos citados: tempo médio de espera, taxa de ocupação, número de atendimentos, absenteísmo,
produção por unidade, consumo de medicamentos, taxa de retorno, tempo de internação, produção
médica.

### Identidade e Acesso (#20, parte)

- `Usuario`, `Perfil`, `Papel`, `Permissao`, com escopo por `Unidade`/`Setor` — controle de acesso
  **por contexto organizacional**, não só por papel global:

```text
Usuário
 ├── Unidade A → Administração
 └── Unidade B → Enfermagem
```

Adiado por decisão explícita (ADR-0039/ADR-0006) — revisitar antes de produção com dado real de
paciente.

### Integrações Externas (#20, parte)

- SUS, CNES, DATASUS, e-SUS, SIGTAP, sistemas municipais, laboratórios/farmácias externos,
  sistemas financeiros, sistemas de regulação externos.

```text
Mais Saúde → Integration Layer → adapters (CNES, SUS, DATASUS, ...)
```

Não implementar nenhuma integração específica antes de ter uma arquitetura de adapters — evita
acoplar o domínio a um formato de API externo específico.

### Documentos

**Domínio transversal citado na visão original do usuário, sem número na lista final de 20 — mas
registrado aqui para não se perder** (ver `MAPA-DE-DOMINIOS.md` §3, Transversal). Qualquer domínio
poderia reaproveitá-lo em vez de ter seu próprio conceito de anexo.

- `Documento`, `Versao`, `Tipo`, `Classificacao`, `Autor`, `Aprovacao`, `Assinatura`, `Historico`.

```text
Contrato → Documento
Paciente → Documento
Processo → Documento
Auditoria → Documento
Compra → Documento
Unidade → Documento
```