# 0153 — Saúde Mental: CAPS e RAPS (SM5)

## Status

Aceita e implementada. Quinta fatia do domínio 26, desenhado na [ADR-0112](./0112-saude-mental.md) (detalhe em
[`saude-mental/MODELO-SAUDE-MENTAL.md`](../saude-mental/MODELO-SAUDE-MENTAL.md)). Usa o modelo operacional da
[ADR-0117](./0117-modelo-operacional-perfis-e-capacidades.md).

## Contexto

- **A ADR-0112 decidiu que CAPS não é Saúde Mental:** CAPS é equipamento, e as modalidades (I, II, III, infantojuvenil,
  AD, AD III) são capacidades da unidade, sem `Caps.java`. A MO1 (ADR-0117) já trouxe essas capacidades e o acolhimento
  noturno.
- **Faltava a rede:** os componentes da Rede de Atenção Psicossocial (Portaria de Consolidação GM/MS nº 3/2017, anexo V)
  classificam os pontos da rede, e a gestão não tinha onde ver quais pontos existem, o que oferecem e o que falta.
- **Faltava o que é próprio do CAPS 24 horas:** o acolhimento noturno, com vagas e permanência limitadas.

## Decisão

1. **O componente da RAPS é dado do catálogo do modelo operacional,** e não uma entidade:
   - o perfil e a capacidade ganham `componente_raps` (opcional): atenção básica, atenção psicossocial especializada,
     urgência e emergência, residencial de caráter transitório, hospitalar, estratégias de desinstitucionalização e
     reabilitação psicossocial;
   - **a carga liga:**
     - os perfis Atenção primária (atenção básica), Urgência e Atenção móvel (urgência e emergência);
     - as capacidades de CAPS e o acolhimento noturno (especializada) e o leito de saúde mental em hospital geral
       (hospitalar);
   - **capacidades novas:** Consultório na Rua (atenção primária) e Centro de convivência (atenção básica); Unidade de
     acolhimento adulto e infantojuvenil (residencial transitório); Serviço residencial terapêutico
     (desinstitucionalização); Geração de trabalho e renda (reabilitação psicossocial). As que não são cuidado de um
     perfil são transversais;
   - **o cadastro do catálogo** (perfil e capacidade) aceita o componente; no ajuste, o campo vazio mantém o que está;
   - **uma unidade pode estar em mais de um componente:** os dela são a soma dos perfis e capacidades vigentes. O hospital
     com leito de saúde mental entra no hospitalar sem virar "unidade de saúde mental".
2. **Painel da RAPS** (`GET saude-mental/raps`):
   - por componente, as unidades com as capacidades nele, os casos de saúde mental em acompanhamento e **o que a unidade
     oferece de fato**: as intervenções realizadas nos últimos 90 dias, por tipo (oficina, grupo, atendimento familiar).
     Não há cadastro paralelo de serviços: a oferta sai dos registros da SM2;
   - **lacunas:** o componente sem nenhum ponto e a modalidade de CAPS que a rede não tem (para adultos, infantojuvenil,
     álcool e drogas);
   - **abrangência:** as unidades do acesso (a rede inteira para quem tem acesso na rede); só números.
3. **Acolhimento noturno** (CAPS III e AD III, com a capacidade "Acolhimento noturno"):
   - **configuração da unidade** (`TB_CONFIGURACAO_ACOLHIMENTO_NOTURNO`): vagas e permanência máxima em dias, pela
     administração da rede (a mesma permissão que liga as capacidades). Sem a capacidade, 422; vagas abaixo da ocupação,
     422. A tela sugere 14 dias, a referência da portaria do CAPS AD III; o município confere e ajusta;
   - **permanência** (`TB_PERMANENCIA_ACOLHIMENTO_NOTURNO`): paciente, caso de saúde mental, entrada, saída prevista,
     justificativa do prazo, saída e motivo;
   - **regras da entrada:**
     - unidade com a capacidade e configurada (422);
     - paciente com caso de saúde mental vigente **na unidade** (422);
     - uma permanência aberta por paciente (409);
     - com vaga (422);
     - saída prevista não antes da entrada (400) e, além da permanência máxima, com a justificativa (422);
   - **prorrogação:** a nova saída prevista, com as mesmas regras de prazo; **saída:** com o motivo (400 sem ele), entre a
     entrada e hoje (400). Encerrada, não muda (422);
   - **leitura:** a ocupação (vagas, acolhidos, há quantos dias, quem passou da saída prevista e quem está além do
     máximo) e as saídas dos últimos 30 dias; as permanências do paciente, com as regras do prontuário.
4. **Permissões** (sem permissão nova):
   - **painel da RAPS:** `INDICADOR.CONSULTAR` ou `SAUDE_MENTAL.ACOMPANHAR`;
   - **acolhimento noturno:** ler, entrar, prorrogar e sair, `SAUDE_MENTAL.ACOMPANHAR` na unidade, com a leitura
     auditada;
   - **vagas e permanência máxima:** `ORGANIZACAO.GERENCIAR`. A resposta da configuração não traz os nomes dos acolhidos,
     porque a administração da rede não tem acesso clínico.

### API

| Método | Caminho | Permissão |
|---|---|---|
| GET | `saude-mental/raps` | `INDICADOR.CONSULTAR` ou `SAUDE_MENTAL.ACOMPANHAR` (escopo) |
| GET | `saude-mental/acolhimento-noturno?unidadeId` | `SAUDE_MENTAL.ACOMPANHAR` (escopo), auditada |
| PUT | `saude-mental/acolhimento-noturno/configuracao?unidadeId` | `ORGANIZACAO.GERENCIAR` (escopo) |
| POST | `saude-mental/acolhimento-noturno`, `…/{id}/prorrogacao`, `…/{id}/saida` | `SAUDE_MENTAL.ACOMPANHAR` (escopo) |
| GET | `saude-mental/paciente/{id}/acolhimento-noturno` | `SAUDE_MENTAL.ACOMPANHAR`, vínculo, auditada |

O perfil e a capacidade do modelo operacional ganham `componenteRaps` no cadastro e na resposta.

### Tela

- **Saúde Mental:** os botões "Rede (RAPS)" e "Acolhimento noturno" no alto da tela.
- **Rede de Atenção Psicossocial** (`/assistencia/saude-mental/raps`): as lacunas no alto e, por componente, a tabela
  das unidades com as modalidades e capacidades, os casos e as intervenções dos últimos 90 dias.
- **Acolhimento noturno** (`/assistencia/saude-mental/acolhimento-noturno`): a unidade; os números (ocupação,
  permanência máxima, vencidas); os acolhidos com a entrada, a saída prevista (vencida, além do máximo) e os dias;
  "Registrar entrada" (com os casos da unidade), "Prorrogar", "Saída" e "Vagas e permanência"; as saídas dos últimos 30
  dias.

### Migração

`V41__saude_mental_caps_e_raps.sql`: a coluna `componente_raps` (com o CHECK) em `tb_perfil_operacional` e
`tb_capacidade_assistencial`, a carga dos componentes e das capacidades novas, e as tabelas
`tb_configuracao_acolhimento_noturno` e `tb_permanencia_acolhimento_noturno`.

## Consequências

- **A RAPS aparece sem cadastro novo:** basta o modelo operacional das unidades estar preenchido. Unidade sem perfil nem
  capacidade não aparece; a lacuna mostra o que falta cadastrar.
- **A oferta do CAPS é o que ele fez,** não o que declarou. Uma unidade nova aparece sem intervenções até registrar as
  primeiras.
- **Ficam para depois:**
  - o CAPS de referência por território (qual CAPS atende cada UBS ou equipe), que melhora a referência da SM4;
  - a produção do CAPS para o faturamento (RAAS), que depende das Integrações (domínio 20, adiado);
  - os indicadores e a intersetorialidade, na SM6.

## Testes

- **JUnit:**
  - `RapsControllerTest`:
    - a RAPS pelas capacidades e pelo perfil (o CAPS III com o acolhimento noturno, os casos e as intervenções dos
      últimos 90 dias; a UPA pelo perfil, sem capacidade a listar; os sete componentes);
    - o componente no catálogo das capacidades;
    - o acolhimento noturno: sem configuração e sem a capacidade (422), o caso de outra unidade (422), além do máximo sem
      e com justificativa, saída prevista antes da entrada (400), já acolhido (409), sem vaga (422), as vagas, a
      prorrogação, a saída com e sem motivo, encerrada (422), as saídas recentes e as permanências do paciente;
  - `RapsAutorizacaoControllerTest`, com segurança ligada: a RAPS para quem acompanha e para a gestão, não para a
    recepção; o acolhimento só na unidade de quem acompanha; as vagas só pela administração da rede.
- **Robot de API:** `test/saude_mental/SaudeMentalRaps.robot`, CT-001 a CT-008.
- **Robot de tela:** `test/ui/assistencia/saude_mental/UI_saude_mental.robot`, CT-016 (a RAPS) e CT-017 (a entrada e a
  saída do acolhimento noturno).
