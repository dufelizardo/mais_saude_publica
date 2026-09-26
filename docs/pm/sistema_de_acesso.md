
Eu pensaria no acesso do **Mais Saúde Pública** como um domínio próprio de **Identidade e Autorização**, transversal a todos os outros domínios.

E tem uma distinção fundamental:

> **Login identifica a pessoa. Permissão determina o que ela pode fazer. Escopo determina onde e sobre quais dados ela pode fazer isso.**

Então não basta termos:

```text
Eduardo → ADMINISTRADOR
```

Precisamos conseguir representar:

```text
Eduardo
 ├── identidade
 ├── papéis
 ├── permissões
 ├── unidades
 ├── setores
 └── escopos de acesso
```

---

# 1. A arquitetura que eu proponho

Eu dividiria em 6 conceitos:

```text
USUÁRIO
   ↓
PAPEL
   ↓
PERMISSÕES
   ↓
ESCOPO
   ↓
RECURSOS
   ↓
AÇÕES
```

Exemplo:

```text
Maria
 ↓
Enfermeira
 ↓
consultar_prontuario
registrar_evolucao
registrar_procedimento
 ↓
UBS Central
 ↓
Pacientes daquela unidade
```

Isso é muito diferente de simplesmente:

```text
Maria = enfermeira = acesso total
```

---

# 2. Primeiro: o login

O login deve representar uma **identidade digital**, não necessariamente o profissional em si.

Eu criaria algo como:

```text
Usuario
```

com:

```text
Usuario
 ├── id
 ├── login
 ├── credencial
 ├── status
 ├── pessoa/profissional
 ├── ultimoAcesso
 └── autenticação
```

Por exemplo:

```text
usuario: maria.silva
profissional: Maria Silva
```

O `Profissional` continua pertencendo ao **RH**.

O `Usuario` pertence ao domínio de **Identidade e Segurança**.

Isso é importante.

Uma pessoa pode existir no RH e **não possuir acesso ao sistema**.

E uma conta técnica pode existir sem representar um profissional.

---

# 3. O Master inicial

Aqui eu concordo com você.

O sistema precisa nascer com uma identidade capaz de configurar o ambiente.

Mas eu chamaria isso de:

```text
Administrador da Plataforma
```

e não simplesmente:

```text
MASTER
```

Porque "Master" tende a virar uma conta que faz absolutamente tudo para sempre.

### Inicialização

```text
Sistema instalado
       ↓
Bootstrap
       ↓
Administrador da Plataforma
       ↓
Configura organização
       ↓
Cria unidades
       ↓
Cria administradores
       ↓
Administração passa a ser descentralizada
```

Por exemplo:

```text
Administrador da Plataforma
        │
        ├── Administrador Municipal
        │       ├── UBS A
        │       ├── UBS B
        │       └── UPA A
        │
        └── Administrador Regional
                ├── UBS C
                └── UBS D
```

Depois disso, o Master **não precisa administrar o dia a dia das unidades**.

---

# 4. O ponto central: RBAC

Eu usaria inicialmente **RBAC — Role-Based Access Control**.

Ou seja:

> O usuário recebe um papel, e o papel recebe permissões.

Exemplo:

```text
Papel: ENFERMEIRO

Permissões:
 ├── atendimento.visualizar
 ├── atendimento.criar
 ├── enfermagem.triagem
 ├── enfermagem.evolucao
 ├── enfermagem.procedimento
 └── prontuario.consultar
```

Outro:

```text
Papel: FARMACEUTICO

Permissões:
 ├── farmácia.dispensacao
 ├── farmácia.consultar_estoque
 ├── farmácia.movimentar_estoque
 └── farmácia.inventario
```

---

# 5. Mas RBAC sozinho não resolve

Esse é o ponto mais importante.

Imagine:

```text
João = médico
```

Isso não deveria significar:

> João pode acessar todos os pacientes de todas as unidades do município.

Precisamos de **escopo**.

Então:

```text
Papel + Permissão + Escopo
```

Exemplo:

```text
João
 └── Médico
      └── prontuario.consultar
           └── UBS Central
```

Outro médico:

```text
Carlos
 └── Médico
      └── prontuario.consultar
           └── Hospital Municipal
```

---

# 6. Escopo organizacional

Eu criaria uma hierarquia de escopo baseada na estrutura que já temos:

```text
Município
   ↓
Regional
   ↓
Unidade de Saúde
   ↓
Setor
```

Assim podemos conceder acesso em diferentes níveis.

### Exemplo 1

```text
Administrador Municipal

Escopo:
Município inteiro
```

Pode administrar:

```text
UBS A
UBS B
UBS C
UPA
Hospital
CAPS
```

### Exemplo 2

```text
Administrador da UBS

Escopo:
UBS Central
```

### Exemplo 3

```text
Enfermeiro

Escopo:
UBS Central
Setor: Enfermagem
```

---

# 7. E aqui entra uma coisa muito interessante

O acesso pode ser herdado pela hierarquia.

Por exemplo:

```text
Município
 ├── Regional Norte
 │    ├── UBS A
 │    └── UBS B
 │
 └── Regional Sul
      ├── UBS C
      └── Hospital
```

Um usuário com:

```text
escopo = Regional Norte
```

pode ter acesso às unidades:

```text
UBS A
UBS B
```

sem precisar cadastrar duas permissões.

---

# 8. Papéis que eu criaria inicialmente

Não criaria 50 papéis.

Começaria com papéis funcionais claros.

### Plataforma

```text
ADMINISTRADOR_PLATAFORMA
```

### Gestão

```text
GESTOR_MUNICIPAL
GESTOR_REGIONAL
GESTOR_UNIDADE
```

### Administrativo

```text
ADMINISTRATIVO
```

### Assistencial

```text
MEDICO
ENFERMEIRO
TECNICO_ENFERMAGEM
FARMACEUTICO
DENTISTA
PSICOLOGO
FISIOTERAPEUTA
ASSISTENTE_SOCIAL
```

### Operacional

```text
RECEPCIONISTA
AGENTE_COMUNITARIO
CONDUTOR
```

### Especializados

```text
REGULADOR
AUDITOR
LABORATORISTA
```

Mas tem um detalhe importante:

> **Cargo do RH não deve automaticamente ser igual a papel de segurança.**

---

# 9. Cargo ≠ Papel

Isso é muito importante para o seu domínio RH.

Por exemplo:

```text
Cargo:
Enfermeiro
```

não significa necessariamente:

```text
Papel:
ENFERMEIRO
```

Porque podemos ter:

```text
Enfermeiro
 ├── Papel: ENFERMEIRO
 └── Papel: COORDENADOR_DE_ENFERMAGEM
```

Ou:

```text
Enfermeiro
 └── Papel: GESTOR_UNIDADE
```

Portanto:

```text
RH
 ↓
Cargo / vínculo / lotação
```

e:

```text
Segurança
 ↓
Papel / permissão / escopo
```

podem conversar, mas não devem ser a mesma coisa.

---

# 10. O acesso deve ser baseado em função

Vamos pegar uma UBS.

## Recepção

Pode:

```text
Paciente
 ├── consultar cadastro
 ├── atualizar dados cadastrais
 └── criar atendimento

Agendamento
 ├── consultar
 ├── criar
 ├── cancelar
 └── reagendar
```

Não pode:

```text
Prontuário clínico completo
Prescrição
Diagnóstico
Evolução médica
```

---

# 11. Enfermagem

Pode:

```text
Paciente
 └── consultar

Atendimento
 └── registrar

Enfermagem
 ├── triagem
 ├── sinais vitais
 ├── evolução
 └── procedimentos

Prontuário
 └── consultar informações necessárias
```

E dependendo do papel:

```text
Administração
 └── escala
```

---

# 12. Médico

Pode:

```text
Atendimento
 ├── consultar
 └── registrar

Prontuário
 ├── consultar
 └── registrar

Diagnóstico
Prescrição
Solicitação de exames
Encaminhamento
```

Mas não deveria automaticamente poder:

```text
RH
Folha
Contratos
Compras
Patrimônio
```

---

# 13. Farmacêutico

Pode:

```text
Farmácia
 ├── estoque
 ├── dispensação
 ├── lote
 ├── validade
 └── inventário
```

Pode consultar informações clínicas necessárias à dispensação.

Mas não deveria ter:

```text
RH completo
Folha
Prontuário completo sem necessidade
```

---

# 14. Gestor da unidade

Aqui o acesso fica muito maior:

```text
Unidade
 ├── setores
 ├── profissionais
 ├── agenda
 ├── indicadores
 ├── recursos
 ├── estoque
 └── processos administrativos
```

Mas ainda assim:

> gestor da UBS não deve automaticamente administrar o Hospital Municipal.

O escopo limita isso.

---

# 15. Gestor municipal

Pode ter:

```text
Município
 ├── unidades
 ├── indicadores
 ├── produção
 ├── regulação
 ├── recursos
 └── gestão
```

Porém eu separaria:

```text
visualizar
```

de:

```text
alterar
```

Por exemplo, um gestor pode visualizar indicadores de todas as unidades, mas não necessariamente editar prontuários ou dispensações.

---

# 16. Regulação

Um regulador tem um acesso muito diferente.

Por exemplo:

```text
Regulação
 ├── solicitações
 ├── filas
 ├── prioridades
 ├── vagas
 ├── encaminhamentos
 └── referência/contrarreferência
```

Ele pode precisar visualizar **informações clínicas necessárias para tomar uma decisão regulatória**, mas isso não significa acesso irrestrito ao prontuário.

Esse conceito é muito importante:

> **acesso ao dado deve ser proporcional à função.**

---

# 17. Auditor

Outro exemplo interessante.

Um auditor pode precisar:

```text
Atendimentos
Produção
Procedimentos
Registros
Prontuário
Indicadores
Auditoria
```

Mas predominantemente em:

```text
modo consulta
```

Ou seja:

```text
AUDITOR
 ├── consultar
 ├── auditar
 └── registrar achado
```

e não:

```text
editar atendimento
editar diagnóstico
alterar prontuário
```

---

# 18. Então precisamos de uma matriz

Eu faria uma matriz de segurança.

Por exemplo:

| Papel            | Paciente | Atendimento | Prontuário | Enfermagem | Farmácia | RH       | Administrativo | Regulação |
| ---------------- | -------- | ----------- | ---------- | ---------- | -------- | -------- | -------------- | --------- |
| Recepção         | C/E      | C/E         | limitado   | -          | -        | -        | limitado       | -         |
| Enfermeiro       | C        | C/E         | C/E        | C/E        | limitado | -        | -              | limitado  |
| Médico           | C        | C/E         | C/E        | C          | limitado | -        | -              | C/E       |
| Farmacêutico     | C        | C           | necessário | -          | C/E      | -        | C/E            | -         |
| Gestor Unidade   | C        | C           | limitado   | C          | C        | limitado | C/E            | C         |
| Regulador        | C        | C           | necessário | -          | -        | -        | -              | C/E       |
| Auditor          | C        | C           | C          | C          | C        | C        | C              | C         |
| Admin Plataforma | C/E      | C/E         | C/E*       | C/E*       | C/E*     | C/E      | C/E            | C/E       |

`C = consultar`
`E = executar/alterar`

O `*` é importante: **administrador de plataforma não deveria ganhar automaticamente acesso ao conteúdo clínico só porque administra o sistema**.

Esse é um princípio que eu adotaria desde o início.

---

# 19. Administrador técnico ≠ profissional de saúde

Isso merece virar uma regra arquitetural.

Imagine:

```text
Administrador do sistema
```

Ele pode:

```text
criar usuário
bloquear usuário
configurar papel
configurar permissão
configurar unidade
```

Mas não deveria automaticamente:

```text
ler prontuário
ver diagnóstico
ver prescrição
ver histórico clínico
```

Mesmo tendo poderes administrativos sobre o sistema.

Isso cria uma separação entre:

```text
ADMINISTRAÇÃO DO SISTEMA
```

e:

```text
ACESSO AO DADO DE SAÚDE
```

---

# 20. E eu colocaria uma segunda camada: permissões

Não faria:

```text
MEDICO = tudo que médico pode fazer
```

dentro do código.

Criaria permissões como:

```text
PACIENTE.CONSULTAR
PACIENTE.EDITAR

ATENDIMENTO.CONSULTAR
ATENDIMENTO.CRIAR
ATENDIMENTO.EDITAR
ATENDIMENTO.CANCELAR

PRONTUARIO.CONSULTAR
PRONTUARIO.REGISTRAR
PRONTUARIO.ASSINAR

ENFERMAGEM.TRIAGEM
ENFERMAGEM.EVOLUCAO
ENFERMAGEM.PROCEDIMENTO

FARMACIA.DISPENSAR
FARMACIA.ESTOQUE.CONSULTAR
FARMACIA.ESTOQUE.MOVIMENTAR

REGULACAO.SOLICITACAO.CONSULTAR
REGULACAO.SOLICITACAO.REGULAR

RH.PROFISSIONAL.CONSULTAR
RH.LOTACAO.CONSULTAR

ADMINISTRATIVO.SETOR.CONSULTAR
ADMINISTRATIVO.SETOR.EDITAR
```

Então:

```text
MEDICO
```

é apenas um agrupamento de permissões.

---

# 21. E existe uma terceira dimensão: o contexto

Essa é a parte que eu acho que vai deixar o **Mais Saúde Pública** realmente robusto.

Imagine que o médico João trabalha em:

```text
Hospital Municipal
```

mas atende ocasionalmente na:

```text
UBS Central
```

Ele não deveria ganhar acesso global só porque tem dois vínculos.

Podemos ter:

```text
João
 ├── Médico
 │    └── Hospital Municipal
 │
 └── Médico
      └── UBS Central
```

Cada vínculo de acesso possui:

```text
papel
unidade
setor
permissões
período
```

---

# 22. Isso também resolve profissionais temporários

Por exemplo:

```text
Médico plantonista
```

Acesso:

```text
UPA Norte
Setor: Emergência
Período:
01/10/2026 → 31/10/2026
```

Quando termina:

```text
acesso expirado
```

Isso é muito melhor do que ficar concedendo e removendo permissões manualmente.

---

# 23. E precisamos de auditoria

Em um sistema de saúde, eu considero isso obrigatório desde a arquitetura.

Não basta saber:

```text
Maria acessou o sistema.
```

Precisamos saber:

```text
Maria
acessou
prontuário do paciente X
em 26/09/2026 15:22
a partir da unidade Y
```

E também:

```text
Maria
alterou
evolução clínica
de X
em determinado momento.
```

Portanto:

```text
Auditoria
 ├── usuário
 ├── ação
 ├── recurso
 ├── entidade
 ├── identificador
 ├── data/hora
 ├── unidade
 ├── resultado
 └── contexto
```

E para alterações:

```text
valor anterior
valor posterior
```

quando tecnicamente aplicável.

---

# 24. Minha arquitetura final seria

```text
                  IDENTIDADE
                      │
                    Usuário
                      │
              ┌───────┴────────┐
              │                │
            Papéis          Escopos
              │                │
        Permissões       Organização
              │                │
              └───────┬────────┘
                      │
                 AUTORIZAÇÃO
                      │
        ┌─────────────┼─────────────┐
        │             │             │
       RH       Administrativo   Assistência
        │             │             │
        │             │       ┌─────┼─────┐
        │             │       │     │     │
        │             │      Pront. Farm. Regulação
        │             │
        └─────────────┴──────────────┘
                      │
                  AUDITORIA
```

---

# 25. O mais importante: não começar com 100 papéis

Eu faria a **primeira versão de segurança** muito mais enxuta:

### Plataforma

```text
ADMINISTRADOR_PLATAFORMA
```

### Gestão

```text
GESTOR_MUNICIPAL
GESTOR_REGIONAL
GESTOR_UNIDADE
```

### Administrativo

```text
ADMINISTRATIVO
```

### Assistencial

```text
MEDICO
ENFERMEIRO
TECNICO_ENFERMAGEM
FARMACEUTICO
RECEPCIONISTA
```

### Especializado

```text
REGULADOR
AUDITOR
```

Depois adicionamos papéis conforme os domínios forem realmente implementados.

---

# 26. E eu faria o primeiro fluxo assim

Quando o sistema subir pela primeira vez:

```text
1. Bootstrap
      ↓
2. Administrador da Plataforma
      ↓
3. Configura Município
      ↓
4. Cadastra/estrutura unidades
      ↓
5. Cria Administrador Municipal
      ↓
6. Administrador Municipal cria Gestores
      ↓
7. Gestor cria usuários da unidade
      ↓
8. Usuário recebe papel
      ↓
9. Papel recebe permissões
      ↓
10. Acesso recebe escopo
      ↓
11. Sistema registra tudo em auditoria
```

E eu acrescentaria uma regra:

> **Ninguém deve conseguir conceder uma permissão que esteja acima do próprio nível de autoridade.**

Por exemplo:

```text
Gestor da UBS
```

não pode criar:

```text
ADMINISTRADOR_PLATAFORMA
```

nem conceder:

```text
acesso a todas as unidades do município
```

---

## O desenho que eu colocaria no projeto agora

Antes de implementar o login, eu criaria um domínio transversal:

```text
seguranca/
├── identidade/
│   ├── Usuario
│   └── Credencial
│
├── autorizacao/
│   ├── Papel
│   ├── Permissao
│   ├── EscopoAcesso
│   └── AtribuicaoAcesso
│
├── auditoria/
│   └── EventoAuditoria
│
└── bootstrap/
    └── AdministradorPlataforma
```

E as relações:

```text
Usuario
   │
   ├── AtribuicaoAcesso
   │       │
   │       ├── Papel
   │       │       └── Permissoes
   │       │
   │       └── Escopo
   │
   └── Auditoria
```

**Isso eu considero uma fundação que vale a pena fazer antes de liberar os próximos domínios.**

E tem uma decisão que eu deixaria para a próxima etapa: **se o acesso ao prontuário será puramente por papel/escopo ou se também teremos regras contextuais**, como “o profissional só pode acessar o paciente quando existe atendimento, vínculo assistencial, encaminhamento ou outra justificativa”. Essa segunda camada é onde o modelo de segurança do Mais Saúde Pública começa a ficar realmente interessante.
