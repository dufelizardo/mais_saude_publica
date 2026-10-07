# 0103 — Equipes de saúde: backend

## Status

Aceita e implementada (parte 1 de 4 de Equipes e Escalas).
- A tela vem na ADR-0104.
- As escalas vêm nas ADRs 0105 e 0106.

## Contexto

- Os protótipos `Equipes.html` e `Escalas.html` não tinham domínio por trás.
- Profissionais (ADR-0072), Equipamentos de Saúde (ADR-0102) e a Agenda (ADR-0092) mostravam equipe como
  "Em breve".
- Na Atenção Primária, a equipe é a unidade de organização do cuidado:
  - a eSF tem composição mínima (PNAB);
  - a eMulti (Portaria GM/MS 635/2023, que substitui o NASF-AB) apoia equipes de Saúde da Família e de
    Atenção Primária;
  - cada equipe tem um INE no CNES.
- Dados que mudam no tempo viram histórico (memória do projeto, ADRs de RH). Quem entra e quem sai da
  equipe não pode ser um campo sobrescrito.

## Decisão

1. **Equipe** (`TB_EQUIPE`):
   - unidade e tipo, que não mudam;
   - nome único na unidade (ex.: "SF-12");
   - INE com 10 dígitos, opcional e único;
   - ativa ou inativa;
   - microáreas (códigos);
   - coordenação, que precisa ser membro vigente;
   - reunião de equipe: dia, início, fim e local.
2. **Tipos e composição mínima**, calculada e nunca guardada:

   | Tipo | Composição mínima |
   |---|---|
   | **eSF** (Saúde da Família) | médico, enfermeiro, técnico ou auxiliar de enfermagem, e agente comunitário |
   | **eAB / eAP** (Atenção Primária) | médico e enfermeiro |
   | **eSB** (Saúde Bucal) | cirurgião-dentista, e técnico ou auxiliar de saúde bucal |
   | **eMulti**, **Consultório na Rua** e **multiprofissional de CAPS** | sem composição mínima no sistema (varia com a modalidade) |

   A equipe responde "completa" ou o que falta (ex.: "ACS", "AUXILIAR_ENFERMAGEM ou TECNICO_ENFERMAGEM").
3. **Membros como histórico** (`TB_MEMBRO_EQUIPE`):
   - profissional, função na equipe, microárea (do ACS), entrada e saída com motivo;
   - **regras:**
     - o membro precisa ter **lotação vigente na unidade** da equipe (422);
     - uma participação vigente por equipe (409, também por índice único parcial no banco);
     - **na eSF e na eAP, o profissional é de uma equipe só** (409, pela carga horária da PNAB);
     - equipe inativa não recebe membro (422);
     - a saída fica entre a entrada e hoje;
     - quem coordena e sai deixa de coordenar;
   - membro antigo continua listado, com a data e o motivo da saída.
4. **eMulti:** guarda as equipes que apoia (`TB_EQUIPE_APOIO`).
   - Só a eMulti apoia, e só eSF e eAP (422).
   - A equipe apoiada mostra quem a apoia.
5. **Leituras:**
   - **lista** do escopo com os indicadores do protótipo: equipes, ativas, por tipo, profissionais vinculados
     (distintos, em equipe ativa) e incompletas;
   - **cartão de cada equipe:** composição, número de membros e de ACS, coordenação e nomes para os
     avatares;
   - **detalhe:** membros vigentes com cargo e jornada da lotação e o **afastamento vigente do RH** (férias,
     licença), os antigos, a reunião, as apoiadas e quem apoia.
6. **Acesso:**
   - `EQUIPE.GERENCIAR` para cadastrar e mexer em membros, com o Gestor e o Coordenador de enfermagem;
   - leitura com `EQUIPE.GERENCIAR`, `RH.CONSULTAR`, `RH.GERENCIAR` ou `ADMINISTRATIVO.CONSULTAR`;
   - sempre no escopo da unidade.
7. **Migração V8.** A matriz de Usuários & Perfis ganha o módulo Equipes, e a Auditoria o recurso.

## Consequências

- Equipamentos de Saúde, Profissionais e as Escalas passam a ter de onde ler a equipe. A tela vem na
  ADR-0104.
- A eSB é a equipe do domínio Saúde Bucal ([ADR-0111](./0111-saude-bucal.md)), que não cria equipe própria.
- As microáreas ficam como texto (`Equipe.microareas`, `MembroEquipe.microarea`) até o domínio Território
  ([ADR-0108](./0108-territorio-e-adscricao.md)), cuja fatia F1 as migra para entidades.
- **Fora do escopo** (em `PENDENCIAS.md`):
  - território (famílias e pacientes adscritos, microáreas georreferenciadas, cadastro domiciliar);
  - indicadores do Previne e do PMAQ;
  - programas vinculados;
  - sincronização das equipes com o CNES.

## Testes

- **JUnit** (`EquipeControllerTest`):
  - nome e INE repetidos e INE inválido;
  - composição da eSF faltando ACS até o agente entrar;
  - profissional sem lotação na unidade;
  - membro repetido;
  - médico em duas eSF;
  - indicadores;
  - coordenação só de membro e tipo que não muda;
  - saída com motivo indo para os antigos, e a saída repetida;
  - eMulti apoiando só eSF e eAP.
- **JUnit com autorização ligada** (`EquipeAutorizacaoControllerTest`):
  - o gestor da unidade cadastra;
  - o enfermeiro recebe 403;
  - o gestor de outra unidade não cadastra, não lista e não lê.
- **Robot de API** (`test/equipes/equipe`): 7 casos.
