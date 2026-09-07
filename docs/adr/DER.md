# DER (Diagrama Entidade-Relacionamento) - Mais Saúde Pública API

**Data:** 2026-09-06  
**Versão:** 1.0  
**Autor:** Equipe de Desenvolvimento  

> ⚠️ **Este é um modelo de dados FUTURO/proposto**, para uma expansão de domínio clínico (Usuário, Paciente, Profissional, Atendimento, Consulta, Procedimento, Equipe, Agendamento, Notificação, Auditoria etc.) que **ainda não existe no código**. Ele não reflete o schema atual do banco. Para o schema real e implementado hoje (uma única entidade `UnidadeDeSaude` autorreferenciada + `Endereco` embutido), veja [`DER-atual.md`](./DER-atual.md). Mantido lado a lado com o modelo atual como visão de roadmap (ver também as propostas de arquitetura em `0003-adocao-clean-architecture.md` a `0008-frontend-angular.md`).

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

### 1. **USUARIO** (Tabela Base de Autenticação)
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

### 2. **PROFISSIONAL**
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

### 4. **UNIDADE_SAUDE**
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

### 5. **ENDERECO**
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

## 📊 **Script SQL (DDL - PostgreSQL)**

```sql
-- Criação do banco
CREATE DATABASE mais_saude_publica;
\c mais_saude_publica;

-- Extensão para UUID
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. TABELA USUARIO
CREATE TABLE usuario (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) UNIQUE NOT NULL,
    senha_hash VARCHAR(255) NOT NULL,
    nome VARCHAR(255) NOT NULL,
    roles VARCHAR(255) NOT NULL,
    ativo BOOLEAN DEFAULT TRUE,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. TABELA ENDERECO
CREATE TABLE endereco (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    logradouro VARCHAR(255) NOT NULL,
    numero VARCHAR(20),
    complemento VARCHAR(100),
    bairro VARCHAR(100) NOT NULL,
    cidade VARCHAR(100) NOT NULL,
    estado VARCHAR(2) NOT NULL,
    cep VARCHAR(10),
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8)
);

-- 3. TABELA UNIDADE_SAUDE
CREATE TABLE unidade_saude (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    endereco_id UUID REFERENCES endereco(id),
    nome VARCHAR(255) NOT NULL,
    cnes VARCHAR(10) UNIQUE NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    telefone VARCHAR(20),
    email VARCHAR(255),
    site VARCHAR(255),
    status VARCHAR(20) DEFAULT 'ATIVO',
    horario_abertura TIME,
    horario_fechamento TIME,
    capacidade INTEGER,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. TABELA PROFISSIONAL
CREATE TABLE profissional (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    usuario_id UUID UNIQUE REFERENCES usuario(id),
    unidade_id UUID REFERENCES unidade_saude(id),
    nome_completo VARCHAR(255) NOT NULL,
    especialidade VARCHAR(100),
    conselho VARCHAR(20),
    numero_conselho VARCHAR(50) UNIQUE,
    data_contratacao DATE,
    data_desligamento DATE,
    ativo BOOLEAN DEFAULT TRUE,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5. TABELA PACIENTE
CREATE TABLE paciente (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    usuario_id UUID UNIQUE REFERENCES usuario(id),
    endereco_id UUID REFERENCES endereco(id),
    cpf VARCHAR(14) UNIQUE NOT NULL,
    nome_completo VARCHAR(255) NOT NULL,
    data_nascimento DATE NOT NULL,
    sexo VARCHAR(20),
    cartao_sus VARCHAR(20) UNIQUE,
    telefone VARCHAR(20),
    email VARCHAR(255),
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 6. TABELA ATENDIMENTO
CREATE TABLE atendimento (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    paciente_id UUID NOT NULL REFERENCES paciente(id),
    profissional_id UUID NOT NULL REFERENCES profissional(id),
    unidade_id UUID NOT NULL REFERENCES unidade_saude(id),
    tipo VARCHAR(50) NOT NULL,
    data_hora TIMESTAMP NOT NULL,
    status VARCHAR(20) DEFAULT 'AGENDADO',
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 7. TABELA CONSULTA
CREATE TABLE consulta (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    atendimento_id UUID NOT NULL REFERENCES atendimento(id),
    profissional_id UUID NOT NULL REFERENCES profissional(id),
    data_hora TIMESTAMP NOT NULL,
    tipo_consulta VARCHAR(50) NOT NULL,
    queixa_principal TEXT,
    diagnost