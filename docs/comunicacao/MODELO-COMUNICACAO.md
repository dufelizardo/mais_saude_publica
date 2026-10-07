# Modelo do domínio Comunicação e Educação em Saúde

**Data:** 2026-10-07 (criado)
**Status:** desenho aprovado na [ADR-0114](../adr/0114-comunicacao-e-educacao-em-saude.md). **Domínio futuro, nada
implementado.** A implementação vem nas fatias C1 a C6 (seção 15), cada uma com ADR própria.

O valor do domínio está em **conectar comunicação, educação, campanhas e públicos da saúde**. Não é um "CMS de
notícias": o portal é só um canal.

> **Regra:** Comunicação e Educação em Saúde organiza conhecimento, campanhas, ações educativas e comunicação com os
> públicos da saúde. Não é dona de programas, territórios, profissionais ou eventos assistenciais: usa esses domínios
> para comunicar e educar de forma contextualizada.

## 1. Objetivo e ciclo

O domínio deve permitir:
- criar campanhas educativas e publicar orientações de saúde;
- produzir materiais educativos;
- comunicar campanhas e ações e enviar avisos de saúde pública;
- organizar e registrar ações educativas comunitárias;
- apoiar a educação permanente dos profissionais;
- direcionar conteúdos para públicos, territórios e unidades.

```text
CONTEÚDO → PÚBLICO-ALVO → CANAL → AÇÃO DE COMUNICAÇÃO → ALCANCE / PARTICIPAÇÃO → AVALIAÇÃO
```

## 2. Quatro finalidades diferentes

| Finalidade | Objetivo | Público | Exemplo |
|---|---|---|---|
| Comunicação | **informar** | cidadão | "A vacinação contra influenza estará disponível nas UBS a partir de segunda-feira." |
| Educação em saúde | **conhecimento e mudança de comportamento** | cidadão e comunidade | "Como reconhecer sinais de dengue e quando procurar atendimento." |
| Educação permanente | **qualificar o trabalho** | profissionais e equipes | "Capacitação sobre o novo protocolo de atendimento." |
| Comunicação de emergência | **orientar com urgência** | população afetada | "Por causa das enchentes, a UBS X está fechada. Procure a UBS Y." |

## 3. Quem é dono de quê

| Conceito | Dono | Este domínio |
|---|---|---|
| Programa, campanha operacional, ação programática | Programas (ADR-0109) | divulga, educa, fornece conteúdo |
| Território, população, adscrição | Território (ADR-0108) | usa para dizer **quem** recebe |
| Profissional, treinamento, participação | RH (`Treinamento`, `ParticipacaoTreinamento`) | fornece conteúdo e material didático |
| Emergência, decisão de comunicar | Emergências (ADR-0113) | executa **como** comunicar |
| Arquivos (PDF, vídeo, imagem) | Documentos | referencia |
| Envio por provedor | Integrações (#20) | aciona o adaptador |
| Indicadores consolidados | Indicadores (#19) | produz os dados |
| **Conteúdo, material, comunicado, campanha de comunicação, publicação, canal, público-alvo** | **Comunicação** | — |

## 4. Duas campanhas, dois nomes

Programas (ADR-0109) já tem a `Campanha` **operacional**: vacinação, rastreamento, com período, público, meta (por
exemplo, 90% de cobertura) e locais. Para não haver duas entidades `Campanha` com sentidos diferentes, a deste
domínio é a **`CampanhaComunicacao`**:

```text
CampanhaComunicacao
├── nome, objetivo comunicacional, descrição
├── período e situação
├── público-alvo
├── territórios
├── temas
├── conteúdos
├── canais
└── programa e/ou campanha operacional relacionada (opcionais)
```

Exemplo do Outubro Rosa:

```text
Programa Saúde da Mulher (Programas)
   ↓
Campanha operacional de rastreamento do câncer de mama (Programas: meta, locais)
   ↓
CampanhaComunicacao "Outubro Rosa" (este domínio)
   ├── conteúdos educativos sobre autoexame e mamografia
   ├── ações educativas nas UBS (ação programática de Programas, com o conteúdo daqui)
   └── comunicados no portal, painéis e e-mail
```

Outros exemplos: vacinação, prevenção à dengue, saúde bucal, prevenção ao tabagismo, saúde mental.

## 5. Conteúdo e material

O **conteúdo** é o conhecimento; o **material** é cada formato do mesmo conhecimento:

```text
ConteudoEducativo "Prevenção da dengue"
├── MaterialEducativo: cartilha (PDF)
├── MaterialEducativo: infográfico
├── MaterialEducativo: vídeo
├── MaterialEducativo: cartaz
└── MaterialEducativo: peça para redes sociais
```

- **`ConteudoEducativo`:** título, resumo, texto, tema, público, faixa etária, idioma, situação, versão, autor e
  validade.
- **`MaterialEducativo`:** conteúdo, **formato como atributo**, arquivo (no domínio Documentos), situação. Sem uma
  classe por formato.

### Validação técnica

Conteúdo de saúde não vai ao ar sem revisão técnica, e não fica no ar depois de vencer:

```text
RASCUNHO → REVISÃO TÉCNICA → APROVADO → PUBLICADO → EXPIRADO (ou nova versão)
```

A validade e a versão evitam que fique publicada uma orientação desatualizada (por exemplo, um protocolo que mudou).
A **acessibilidade** faz parte do conteúdo: linguagem simples, outros idiomas e formatos acessíveis (Lei
13.146/2015).

## 6. Comunicado

Aviso operacional, diferente de conteúdo educativo:
- alteração do horário da UBS;
- interrupção temporária de serviço;
- mudança de local de atendimento;
- campanha de vacinação;
- aviso sobre medicamentos;
- orientação epidemiológica.

```text
Comunicado
├── título, mensagem
├── prioridade (normal, alta, emergencial)
├── público-alvo
├── período
├── território e/ou unidade
├── canais
└── situação
```

**Pode nascer de fatos de outros domínios**, como sugestão a quem publica:
- unidade que fica **inoperante ou em obra** (situação operacional, ADR-0101) → comunicado "UBS fechada, procure a
  UBS Y";
- **campanha operacional** que começa (Programas) → comunicado de divulgação;
- **emergência** que pede comunicação (seção 7).

## 7. Comunicação de emergência

Sem um segundo sistema: é um **comunicado com prioridade emergencial**.

```text
Enchente (Emergências) → UBS X interditada → "precisamos comunicar"
   → Comunicado emergencial (este domínio)
   → moradores do território da UBS X (Território)
   → portal + SMS + painel (canais)
```

**Emergências (ADR-0113) sabe que precisa comunicar; este domínio sabe como.** Assim fica coberta a "comunicação de
risco à população", que a ADR-0113 deixou fora do escopo dela.

## 8. Canais e adaptadores

O domínio não fica preso a WhatsApp, SMS ou e-mail:

```text
CanalComunicacao: tipo, nome, descrição, ativo
Tipos: PORTAL · APLICATIVO · EMAIL · SMS · NOTIFICACAO · PAINEL · IMPRESSO · EVENTO · REDE_SOCIAL

Comunicado → Publicacao (por canal) → Adaptador → Provedor externo
```

| Canal | Hoje |
|---|---|
| Portal | a **landing page** pública, que já tem "consulte campanhas e programas" |
| E-mail | o envio que já existe para a recuperação de senha (ADR-0081); falta contratar o SMTP |
| SMS, WhatsApp, push | precisam de provedor, pela camada de Integrações (#20) |
| Painel | painel de TV nas unidades (futuro) |

**O disparo em massa fica fora do domínio:** quem envia é o adaptador. A `Publicacao` registra o que foi publicado,
onde, quando e com que resultado.

## 9. Público-alvo

Simples no início: uma classificação, mais território, unidade, equipe e faixa etária.

```text
GESTANTES · CRIANÇAS · IDOSOS · ADOLESCENTES · PROFISSIONAIS_SAUDE · ACS · POPULAÇÃO_GERAL
```

- **Território:** usa o domínio 21, sem outro modelo. Exemplos: "famílias com crianças do território X", "microáreas
  afetadas pela enchente".
- **Sem motor de segmentação** agora.

## 10. Privacidade na comunicação

Comunicar com pessoas identificadas pede cuidados (LGPD, Lei 13.709/2018):
- **Segmentar por dado de saúde** (por exemplo, pessoas com diabetes ou gestantes do território) usa a base legal da
  tutela da saúde, só para essa finalidade.
- **A mensagem nunca revela a condição de saúde.** SMS e notificação aparecem na tela bloqueada:

| Não | Sim |
|---|---|
| "Seu exame de HIV está pronto." | "Há um resultado disponível. Procure sua UBS." |
| "Lembrete da consulta do pré-natal." | "Você tem um agendamento na UBS Vila Esperança amanhã às 9h." |
| "Paciente diabético, sua insulina chegou." | "Seu medicamento está disponível para retirada." |

- **Consentimento e descadastro:** o envio a pessoa identificada respeita o consentimento e o descadastro do canal.
- **População geral:** campanha para todos não usa dado pessoal.

## 11. Ação educativa

A ação educativa comunitária **não ganha motor próprio**. É a **ação programática ou atividade coletiva** de
Programas (ADR-0109, P4), como já decidido para a saúde bucal coletiva (ADR-0111).

| Parte | De onde vem |
|---|---|
| ação, local (escola, UBS, praça, associação, unidade móvel), data, equipe, território, público | Programas, Território, Equipe |
| **conteúdo e material** usados, tema, avaliação | **este domínio** |

Exemplo: ação "Prevenção da dengue", nas microáreas 12 a 18, da eSF 03, para as famílias do território, usando o
conteúdo "Prevenção da dengue" e a cartilha.

## 12. Educação permanente

```text
Educação em saúde → cidadão e comunidade
Educação permanente → profissionais e equipes
```

A capacitação **já existe no RH**: `Treinamento` (nome, carga horária, validade, obrigatório) e
`ParticipacaoTreinamento` (profissional, conclusão, validade, certificado), com a tela Desenvolvimento (ADR-0073).
- Este domínio acrescenta só o **conteúdo e o material didático** ligados ao treinamento e as **competências**
  abordadas.
- **O histórico funcional continua no RH.** Não se cria uma `Capacitacao` paralela nem uma plataforma de EAD.

## 13. Avaliação

```text
AvaliacaoCampanha: campanha, alcance, participação, cobertura por território, resultados, conclusões
```

Responde:
- quantas pessoas a campanha alcançou e quantas participaram;
- que território teve menor alcance;
- que canal funcionou melhor;
- se o público-alvo foi atingido.

**O domínio produz os dados; o #19 Indicadores e BI consolida e analisa.**

## 14. Permissões e entidades

| Permissão | Para quê |
|---|---|
| `COMUNICACAO.PUBLICAR` | comunicados, publicação, campanhas de comunicação |
| `CONTEUDO.EDITAR` | conteúdos e materiais |
| `CONTEUDO.REVISAR` | validação técnica |
| `EMERGENCIA.COORDENAR` (ADR-0113) | comunicado emergencial |

| Entidade | Atributos previstos | Fatia |
|---|---|---|
| `CanalComunicacao` (catálogo) | tipo, nome, descrição, ativo | C1 |
| `ConteudoEducativo` | título, resumo, texto, tema, público, faixa etária, idioma, situação, versão, autor, revisor, validade | C1 |
| `Comunicado` | título, mensagem, prioridade, público, período, território, unidade, canais, situação, origem (opcional) | C1 |
| `Publicacao` | comunicado ou conteúdo, canal, data, situação, resultado do envio | C1 |
| `CampanhaComunicacao` | nome, objetivo, período, situação, público, territórios, temas, programa, campanha operacional | C2 |
| `PublicoAlvo` | classificação, faixa etária, território, unidade, equipe | C2 |
| `MaterialEducativo` | conteúdo, formato, arquivo (Documentos), situação | C2 |
| ligação conteúdo ↔ ação programática | ação de Programas, conteúdos, materiais, tema | C3 |
| ligação conteúdo ↔ treinamento | `Treinamento` do RH, conteúdos, materiais, competências | C4 |
| consentimento e descadastro por canal | pessoa, canal, consentido em, descadastrado em | C5 |
| `AvaliacaoCampanha` | campanha, alcance, participação, cobertura, resultados, conclusões | C6 |

Organização lógica prevista (não se implementa tudo de início):

```text
comunicacao/
├── campanha/    CampanhaComunicacao, PublicoAlvo
├── conteudo/    ConteudoEducativo, MaterialEducativo
├── comunicado/  Comunicado, Publicacao, CanalComunicacao
├── educacao/    ligação com as ações de Programas e os treinamentos do RH
└── avaliacao/   AvaliacaoCampanha
```

## 15. Relações e fatias

```text
                     PROGRAMAS
                         │
              campanha operacional
                         │
              CAMPANHA DE COMUNICAÇÃO
              ┌──────────┴──────────┐
       EDUCAÇÃO EM SAÚDE      COMUNICAÇÃO
              │                     │
         COMUNIDADE              CIDADÃO
              └──────────┬──────────┘
                     TERRITÓRIO

EMERGÊNCIA → necessidade de comunicar → COMUNICAÇÃO → público + território + canal
RH → profissionais e treinamentos → EDUCAÇÃO PERMANENTE (conteúdo e material)
```

| Fatia | Entrega | Depende de |
|---|---|---|
| **C1** · comunicação básica | comunicado, conteúdo com validação técnica, publicação, canal; portal (landing page) e e-mail | — |
| **C2** · campanhas de comunicação | `CampanhaComunicacao`, público-alvo, material educativo | C1; Programas P3; Documentos para os arquivos |
| **C3** · educação em saúde | conteúdo e material nas ações educativas de Programas | C2; Programas P4; Território |
| **C4** · educação permanente | conteúdo e material ligados ao `Treinamento` do RH; competências | C1 |
| **C5** · multicanal | adaptadores de SMS, notificação, aplicativo e painel; consentimento e descadastro | C1; Integrações (#20) |
| **C6** · avaliação | alcance, participação, efetividade | C2; Indicadores (#19) |

**Prioridade: futuro.** O domínio consome o que Território, Programas, Vigilância, Saúde Mental e Emergências produzem,
e ganha valor quando consegue dizer "existe uma campanha de prevenção da dengue para este território, com estas
equipes, estes programas, estas unidades e este público", e não só "existe um post sobre dengue". A exceção possível é
a **C1**, que já tem valor sozinha.

**Fora do escopo:**
- rede social própria;
- CMS completo;
- editor multimídia;
- marketing;
- CRM de cidadãos;
- disparo em massa dentro do domínio;
- LMS e EAD completos;
- analytics próprio;
- qualquer duplicação de Programas, Território ou RH.

## 16. Referências

- Lei nº 13.709/2018 (LGPD): dado pessoal sensível e tutela da saúde.
- Lei nº 13.146/2015 (Lei Brasileira de Inclusão): acessibilidade na comunicação.
