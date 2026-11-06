# 0156 — Emergências e Desastres: núcleo (E1)

## Status

Aceita e implementada. Primeira fatia do domínio 27, desenhado na [ADR-0113](./0113-emergencias-e-desastres.md)
(detalhe em [`emergencias/MODELO-EMERGENCIAS.md`](../emergencias/MODELO-EMERGENCIAS.md)). Usa o Território
([ADR-0125](./0125-territorio-fundacao.md) e [ADR-0126](./0126-territorio-mapa.md)), o surto da Vigilância
([ADR-0131](./0131-vigilancia-surto-e-medidas.md)), a situação operacional das unidades
([ADR-0101](./0101-equipamentos-de-saude-backend.md)) e os leitos ([ADR-0098](./0098-leitos-e-internacao-backend.md)).

## Contexto

- **O domínio é uma camada de coordenação extraordinária,** não dona de recursos (ADR-0113): registra a resposta e
  deixa leitos, escalas e estoques com os donos.
- **Faltava o ponto de partida:** registrar o fenômeno, decidir se ele vira emergência, acompanhar o nível de resposta,
  o território atingido e o impacto na rede.
- **Três coisas diferentes** (ADR-0113): o evento é o fenômeno, a emergência é a resposta coordenada e o plano
  (E2) é a preparação. Nem todo evento vira emergência.

## Decisão

1. **Catálogos:**
   - **tipo de emergência** (`TB_TIPO_EMERGENCIA`): código estável, nome, categoria (desastre ou saúde) e o código
     COBRADE opcional. A carga traz inundação, enxurrada, alagamento, deslizamento, estiagem, incêndio florestal e
     acidente com produto perigoso (com o COBRADE quando conhecido), e os tipos da saúde: acidente com múltiplas
     vítimas, epidemia, evento de massa, desabastecimento e interrupção de unidade. O município revisa;
   - **nível de resposta** (`TB_NIVEL_EMERGENCIA`): ordem estável, nome e descrição; carga de 0 (alerta) a 3 (resposta
     máxima), como no plano de resposta às emergências em saúde pública;
   - o código do tipo e a ordem do nível não mudam (422); o resto, sim, e ambos podem ser inativados.
2. **Evento emergencial** (`TB_EVENTO_EMERGENCIAL`):
   - tipo (ativo, 422), data e hora (não no futuro, 400), local, área opcional em GeoJSON (inválida, 400), fonte
     (Vigilância, unidade, Defesa Civil, gestor, profissional, sistema externo), descrição e o **surto** de origem;
   - **situações:** registrado → monitorado; descartado (com o motivo, 400) ou convertido. Convertido ou descartado não
     muda mais (422).
3. **Emergência** (`TB_EMERGENCIA`):
   - nasce da **conversão do evento**, com nome, nível inicial, justificativa e responsável opcional (pela matrícula).
     Uma por evento: converter de novo dá 409; evento descartado, 422;
   - **nível com histórico** (`TB_MUDANCA_NIVEL_EMERGENCIA`): a conversão grava a primeira mudança (sem nível anterior);
     cada elevação ou redução grava o anterior, o novo, a justificativa, quem e quando. O mesmo nível dá 422;
   - **território afetado:** o polígono (de partida, a área do evento) e as **microáreas** (`TB_EMERGENCIA_MICROAREA`).
     Sem a lista, entram as microáreas que o polígono atinge (sobreposição ou contenção, com a geometria do
     Território); com a lista, ela vale;
   - **referências legais registradas:** a ESPIN (Decreto 7.616/2011) com número e data, e o decreto de situação de
     emergência ou calamidade pública (Lei 12.608/2012) com tipo, número e data. Cada um completo ou vazio (400). **O
     sistema não declara:** é ato do poder público;
   - **situação:** ativa ou encerrada (com o motivo). Encerrada, não muda mais (422). A desmobilização é da E5.
4. **Avaliação de impacto** (`TB_AVALIACAO_IMPACTO`, `TB_AVALIACAO_IMPACTO_UNIDADE`):
   - datada e repetível, só inclusão: população afetada, unidades afetadas, profissionais e leitos indisponíveis,
     medicamentos críticos, equipamentos, transporte, infraestrutura, nível de impacto (baixo, moderado, alto, crítico)
     e observação. Só na emergência ativa (422);
   - **sugestão dos donos** (`GET emergencia/{id}/sugestao-impacto`), editável na tela:
     - **população:** as pessoas cadastradas nas microáreas afetadas (cadastro territorial, ADR-0127); sem cadastro, a
       população de referência da microárea;
     - **unidades:** as que têm a localização dentro do polígono, com a situação operacional;
     - **leitos:** os ativos dessas unidades fora de uso (ocupados, em higienização ou bloqueados).
5. **Permissões novas:**
   - `EMERGENCIA.REGISTRAR`: evento, território e avaliação. Papéis: Gestor, Vigilância epidemiológica e Coordenador de
     enfermagem;
   - `EMERGENCIA.ATIVAR`: converter, mudar o nível, ESPIN e decreto, encerrar. Papel: Gestor (a autoridade sanitária);
   - `CONTINGENCIA.GERENCIAR`: os catálogos de tipo e nível (Gestor); na E2, os planos;
   - **leitura:** quem tem uma delas ou `INDICADOR.CONSULTAR`. Não há dado de paciente na E1, e o escopo é a rede: a
     emergência é municipal.

### API

| Método | Caminho | Permissão |
|---|---|---|
| GET | `emergencias/tipos`, `emergencias/niveis` | leitura |
| POST, PUT | `emergencias/tipo`, `emergencias/nivel` (`/{id}`) | `CONTINGENCIA.GERENCIAR` |
| GET | `eventos-emergenciais?situacao`, `evento-emergencial/{id}` | leitura |
| POST | `evento-emergencial`, `evento-emergencial/{id}/situacao` | `EMERGENCIA.REGISTRAR` |
| POST | `evento-emergencial/{id}/emergencia` | `EMERGENCIA.ATIVAR` |
| GET | `emergencias?situacao`, `emergencia/{id}`, `emergencia/{id}/sugestao-impacto` | leitura |
| POST | `emergencia/{id}/nivel`, `emergencia/{id}/encerramento` | `EMERGENCIA.ATIVAR` |
| PUT | `emergencia/{id}/referencias-legais` | `EMERGENCIA.ATIVAR` |
| PUT | `emergencia/{id}/territorio` | `EMERGENCIA.REGISTRAR` ou `EMERGENCIA.ATIVAR` |
| POST | `emergencia/{id}/avaliacao-impacto` | `EMERGENCIA.REGISTRAR` |

### Tela

- **Emergências** (`/vigilancia/emergencias`, menu Vigilância · Emergências):
  - abas "Emergências ativas", "Eventos" e "Encerradas";
  - **eventos:** registrar (tipo, fonte, local, quando, descrição e a área desenhada no mapa), monitorar, descartar
    com motivo e converter em emergência;
  - **detalhe da emergência:** nível, situação e último impacto; o mapa com a área e as microáreas; ESPIN e decreto; o
    histórico de nível; as avaliações em linha do tempo;
  - **gavetas:** nova avaliação (pré-preenchida pela sugestão, com as unidades da área marcadas), território (desenho
    no mapa), mudar nível, ESPIN e decreto, encerrar.
- **Sala de Situação:** as emergências ativas aparecem como alerta no alto, com o nível e o link; até três, e um
  "e mais N" que leva à tela.
- **Vigilância:** no detalhe do surto, "Registrar evento emergencial" abre a gaveta já ligada ao surto (fonte
  Vigilância, tipo Epidemia).
- **Na tela, as microáreas vêm da interseção com o polígono;** a escolha manual existe na API e fica para depois.

### Migração

`V43__emergencias_nucleo.sql`: as tabelas de tipo, nível, evento, emergência (com as microáreas), mudança de nível e
avaliação de impacto (com as unidades), os CHECK dos enums e a carga dos catálogos. As permissões nascem pelo catálogo
de acesso.

## Consequências

- **A rede passa a ter onde registrar e acompanhar uma emergência,** com o nível e o impacto ao longo do tempo, sem
  mexer nos recursos dos donos.
- **O impacto melhora com os cadastros:** sem geometria nas microáreas e sem localização nas unidades, a sugestão vem
  vazia e o número é informado à mão.
- **Ficam para depois:**
  - E2: plano de contingência e ativação com plano;
  - E3: mobilização, capacidade emergencial e ações;
  - E4: vulneráveis e pacientes deslocados;
  - E5: desmobilização e relatório pós-evento;
  - E6: eventos especializados;
  - a tela dos catálogos de tipo e nível;
  - a comunicação de risco à população, no domínio 28.

## Testes

- **JUnit:**
  - `EmergenciaControllerTest`:
    - catálogos: a carga (COBRADE e níveis 0 a 3), código repetido (409), código que muda (422), tipo inativo sem evento
      novo (422), ordem repetida (409);
    - evento: no futuro e área inválida (400), descarte sem e com motivo, conversão do descartado (422), monitorar,
      converter (nível sem anterior), evento convertido (409 e 422);
    - emergência: microáreas atingidas pela área do evento, sugestão (população de referência, unidade na área com os
      leitos fora de uso), lista explícita de microáreas, elevação com histórico, mesmo nível (422), ESPIN e decreto
      incompletos (400), avaliação (unidades, último impacto na lista, número negativo 400), encerramento sem e com
      motivo, nível e avaliação depois de encerrada (422);
  - `EmergenciaAutorizacaoControllerTest`, com segurança ligada: a vigilância registra e não ativa; o gestor ativa; a
    recepção e a administração da plataforma não leem; a vigilância não mantém o catálogo.
- **Robot de API:** `test/emergencias/Emergencias.robot`, CT-001 a CT-011.
- **Robot de tela:** `test/ui/vigilancia/emergencias/UI_emergencias.robot`, CT-001 (registrar e converter) e CT-002
  (mudar nível e avaliar impacto).
