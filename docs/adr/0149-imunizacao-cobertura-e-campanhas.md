# 0149 — Imunização: cobertura vacinal e campanhas (I3)

## Status

Aceita e implementada. Terceira fatia do domínio 24, desenhado na [ADR-0147](./0147-imunizacao.md).

## Contexto

- **Com a I1 e a I2, a rede registra a dose e controla o estoque,** mas não sabe se está vacinando quem precisa:
  - a Sala de Situação tinha o bloco "Cobertura vacinal e doses aplicadas" em "Em breve";
  - a tela de Equipes dizia "cobertura vacinal: em breve";
  - a lista de atrasados da I1 era da unidade inteira, sem a microárea, o agente comunitário nem o endereço.
- **A meta da campanha de vacinação andava pelas ações** (P3, [ADR-0133](./0133-programas-campanhas.md)): alguém somava
  à mão as pessoas atendidas num posto. Desde a I1, a dose registra a campanha, mas o andamento não usava isso.
- **O PNI acompanha três indicadores:**
  - a **cobertura** de cada vacina, pela dose que completa o esquema (a 3ª da penta, a 1ª da tríplice viral), contra a
    meta: 90% para BCG e rotavírus, 95% para as demais;
  - a **homogeneidade**, entre vacinas (quantas atingiram a meta) e entre territórios (quantos atingiram a meta numa
    vacina);
  - a **taxa de abandono** das vacinas de várias doses (quem começou e não terminou): baixa abaixo de 5%, média de 5% a
    10%, alta a partir de 10%.
- **O PNI calcula a cobertura sobre os nascidos vivos (SINASC).** Aqui ainda não há essa integração (domínio 20). O que
  há é o cadastro territorial com a adscrição ([ADR-0128](./0128-adscricao-e-cobertura.md)), que já decidiu que a
  cobertura sai do cadastro, não de estimativa.

## Decisão

1. **A dose que mede a cobertura é dado do calendário,** não código. `TB_ESQUEMA_VACINAL` ganha `mede_cobertura` e
   `meta_cobertura` (em %):
   - **a carga inicial** marca, na criança: BCG e rotavírus (2ª dose) com 90%; penta (3ª), VIP (3ª), pneumo 10 (2ª),
     meningo C (2ª), febre amarela (1ª), tríplice viral (1ª), tetra viral, hepatite A e o reforço da DTP com 95%;
   - **a gestão muda** pela tela do calendário (`IMUNIZACAO.GERENCIAR`);
   - **só mede** a dose da criança com idade recomendada, porque a coorte sai dessa idade (400). Marcar sem a meta é 400;
   - no ajuste da dose do calendário, o campo vazio mantém o que está.
2. **A coorte e a cobertura, pelo cadastro:**
   - **coorte:** as crianças adscritas (adscrição vigente, paciente ativo) com **de 1 a 12 meses a mais que a idade
     recomendada** da dose, na data de referência. O mês de folga dá tempo de a dose ser aplicada; os 12 meses fecham
     uma coorte anual, como a do PNI;
   - **vacinada:** quem tem a dose vigente (aplicada ou transcrita, nem anulada nem retificada) até a data;
   - **cobertura:** vacinadas sobre a coorte, com uma casa decimal; **na meta** quando chega à meta da dose;
   - **a data de referência** é hoje por padrão e pode voltar no tempo; não pode ser no futuro (400). A adscrição é a
     de hoje: o cadastro não guarda a fotografia de cada dia.
3. **Homogeneidade e abandono:**
   - **entre vacinas:** as vacinas na meta sobre as vacinas com crianças na coorte;
   - **entre territórios:** numa vacina, os recortes (unidades, equipes ou microáreas) na meta sobre os recortes com
     crianças na coorte;
   - **abandono:** para a dose de referência que não é a 1ª nem a única, de vacina com 1ª dose no calendário da
     criança. Na mesma coorte, quem tem a 1ª dose e não tem a de referência, sobre quem tem a 1ª dose. O reforço da DTP
     não tem abandono, porque a série começa na penta.
4. **A abrangência e os recortes:**
   - **sem filtro:** as unidades do acesso (a rede inteira para quem tem acesso na rede), por unidade;
   - **unidade:** por equipe;
   - **equipe:** por microárea; quem não tem microárea fica em "Sem microárea";
   - **equipe e microárea:** a microárea, sem recorte. A microárea sozinha é 400, e a equipe de outra unidade também;
   - a microárea sem nome aparece como "Microárea" e o código.
5. **Andamento real da campanha de vacinação:**
   - **o realizado conta as pessoas diferentes** com dose vigente registrada com a campanha;
   - **as ações dessa campanha não somam mais,** para a mesma vacinação não contar duas vezes. Elas continuam no
     calendário de ações, como logística do posto;
   - as campanhas de outros tipos seguem somando as ações. A consulta pública mostra o mesmo andamento;
   - **as vacinas da campanha** (`TB_CAMPANHA_IMUNOBIOLOGICO`), opcionais: com elas, a dose de outra vacina com a
     campanha é 422. Só a campanha de vacinação tem vacinas (400). No ajuste, o campo vazio mantém as vacinas, e a lista
     vazia libera qualquer uma.
6. **Busca ativa por microárea:** a lista de atrasados ganha:
   - os filtros por equipe (a equipe precisa ser da unidade, 400) e por microárea;
   - em cada paciente, a equipe e a microárea, o agente comunitário da microárea e onde mora (o domicílio do cadastro
     territorial ou, sem ele, o endereço do paciente);
   - a **lista para imprimir**, agrupada por microárea com o agente e uma coluna para anotar a visita.
7. **Permissões:**
   - **cobertura** (só números): `VACINACAO.REGISTRAR`, `IMUNIZACAO.GERENCIAR` ou `INDICADOR.CONSULTAR` na unidade;
     sem filtro, as unidades onde a pessoa tem alguma delas;
   - **vacinação na Sala de Situação:** `INDICADOR.CONSULTAR`, com a abrangência dos indicadores
     ([ADR-0142](./0142-indicadores-bi-e-gestao.md));
   - **busca ativa** (tem nome de paciente): `VACINACAO.REGISTRAR` na unidade, como na I1, com a leitura auditada.

   Sem permissão nova.

### API

| Método | Caminho | Permissão |
|---|---|---|
| GET | `vacinacao/cobertura?unidadeId&equipeId&microareaId&data` | `VACINACAO.REGISTRAR`, `IMUNIZACAO.GERENCIAR` ou `INDICADOR.CONSULTAR` (escopo) |
| GET | `vacinacao/atrasados?unidadeId&equipeId&microareaId` | `VACINACAO.REGISTRAR` (escopo), auditada |
| GET | `indicadores-rede/vacinacao?de&ate&unidadeId` | `INDICADOR.CONSULTAR` (escopo) |

Mudam também:
- **a dose do calendário:** ganha `medeCobertura` e `metaCobertura`;
- **a campanha:** ganha `imunobiologicoIds` (e `imunobiologicoSiglas` na resposta);
- **o atrasado:** ganha `equipeId`, `microareaId`, `acsNome` e `endereco`.

### Tela

- **Sala de Vacina:**
  - **aba nova "Cobertura":**
    - os números: homogeneidade entre vacinas, vacinas na meta e vacinas com abandono alto;
    - a tabela por vacina: coorte, vacinadas, cobertura (verde na meta, amarelo até 10 pontos abaixo, vermelho
      abaixo disso), meta, homogeneidade entre territórios e abandono;
    - a tabela por recorte, que desce da unidade para a equipe e da equipe para a microárea;
    - os filtros de equipe, microárea e data de referência. Sem unidade escolhida, a aba mostra as unidades do acesso;
  - **aba "Atrasados":** os filtros de equipe e microárea, o agente comunitário, onde mora e "Imprimir lista";
  - **aba "Calendário vacinal":** a marca "Cobertura · meta" e a gaveta "Meta" das doses da criança.
- **Lista de busca ativa para imprimir** (`/busca-ativa/vacinacao`): página fora do AppShell, como o laudo
  ([ADR-0095](./0095-laboratorio-prontuario-laudo-recoleta.md)).
- **Sala de Situação:** o bloco "Em breve" dá lugar ao bloco "Vacinação". Ele mostra as doses do período (por
  estratégia), a homogeneidade, o abandono alto, a cobertura por vacina e as campanhas de vacinação do período com o
  andamento.
- **Equipes:** o detalhe da equipe ganha a cobertura vacinal (homogeneidade e cobertura por vacina com coorte), no lugar
  do "em breve".
- **Programas de Saúde, campanha:** na de vacinação, a escolha das vacinas da campanha; o detalhe mostra as vacinas.

### Migração

`V38__imunizacao_cobertura.sql`:
- as colunas `mede_cobertura` e `meta_cobertura` em `tb_esquema_vacinal`, com a carga das doses de referência;
- a tabela `tb_campanha_imunobiologico`;
- o índice de `tb_registro_vacinacao` por campanha, para o andamento.

## Consequências

- **A cobertura depende do cadastro.** Sem território, sem adscrição e sem crianças cadastradas, ela não existe. Com o
  cadastro incompleto, ela aparece alta (falta quem não foi cadastrado, que costuma ser quem não vacinou). A cobertura
  do cadastro (pessoas cadastradas sobre a população de referência, ADR-0128) diz o quanto confiar nela.
- **A cobertura é calculada na hora,** como os outros indicadores da Sala de Situação: sem tabela consolidada. Numa
  rede grande, a consulta sem filtro percorre todas as adscrições de crianças; se pesar, vira apuração mensal.
- **A data de referência no passado usa a adscrição de hoje.** Quem mudou de equipe aparece na equipe nova.
- **A campanha de vacinação com ações já confirmadas muda de número:** o realizado passa a ser o das doses. Nas
  campanhas de antes da I1, sem doses ligadas, o realizado cai a zero.
- **Ficam para depois:**
  - a cobertura sobre os nascidos vivos do SINASC (domínio 20);
  - a cobertura de adolescentes, adultos e gestantes (as doses deles seguem como recomendação);
  - a cobertura vacinal como indicador das metas programáticas (P6).

## Testes

- **JUnit:**
  - `CoberturaVacinalControllerTest`:
    - o calendário com a meta;
    - a cobertura da unidade: coorte, vacinadas, cobertura, meta, abandono alto e homogeneidade entre territórios;
    - a criança fora da coorte e a data de referência no passado;
    - a equipe por microárea e a microárea sem recorte;
    - as recusas: microárea sem equipe, data futura, equipe de outra unidade, meta sem valor e meta fora da criança;
    - a busca ativa por equipe e microárea, com o agente;
    - a campanha: vacinas só na de vacinação, a dose de outra vacina (422) e o andamento pela dose;
    - a vacinação na Sala de Situação;
  - `CoberturaVacinalAutorizacaoControllerTest`, com segurança ligada:
    - o técnico vê a cobertura e a busca ativa da própria unidade, não de outra, e sem filtro só as unidades dele;
    - a recepção não vê;
    - a gestão vê a cobertura e a Sala de Situação, mas não a busca ativa;
    - a Sala de Situação é de quem consulta indicadores.
- **Robot de API:** `test/imunizacao/CoberturaVacinal.robot`, CT-001 a CT-011. O teste da P3 de ação e andamento passou
  a usar uma campanha de rastreamento.
- **Robot de tela:**
  - `test/ui/imunizacao/UI_sala_de_vacina.robot`, com a cobertura por equipe e a busca ativa por microárea;
  - `test/ui/indicadores/UI_sala_de_situacao.robot`, com o bloco "Vacinação" no lugar do "Em breve";
  - `test/ui/assistencia/programas`, com a ação da campanha em campanha de rastreamento.
