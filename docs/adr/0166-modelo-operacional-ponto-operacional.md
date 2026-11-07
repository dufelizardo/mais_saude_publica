# 0166 — Modelo Operacional: ponto operacional (MO3)

## Status

Aceita e implementada. Fatia MO3 da camada 29, desenhada na [ADR-0115](./0115-modelo-operacional-dos-equipamentos.md) e em
[`equipamentos/MODELO-OPERACIONAL.md`](../equipamentos/MODELO-OPERACIONAL.md). Segue a MO2
([ADR-0165](./0165-modelo-operacional-servicos-da-unidade.md)).

## Contexto

- **O endereço da unidade não é necessariamente onde o cuidado acontece:** domicílio, rua, escola, local do evento, ponto
  da unidade móvel. Até aqui, o `Atendimento` só tinha a unidade.
- **A unidade continua sendo o vínculo organizacional:** quem responde e onde o registro mora.
- **Unidade móvel é modalidade, não tipo** (ADR-0115): `TipoUnidadeDeSaude` não muda.

## Decisão

1. **Local de realização no Atendimento**, opcional. Sem local, o atendimento foi na unidade; os registros existentes
   seguem válidos.
   - Opções: domicílio, via pública, local do evento, escola, ponto da unidade e outro.
   - **Domicílio:** aponta o `Domicilio` do cadastro territorial (ADR-0127), ou fica só com a descrição, porque nem todo
     paciente está cadastrado. Sem nenhum dos dois, 400.
   - **Ponto da unidade:** exige um ponto ativo da unidade do atendimento (400 sem ponto, 422 de outra unidade ou encerrado).
     O que já estava continua valendo depois de encerrado, para a edição não falhar.
   - **Demais:** descrição obrigatória (400).
   - Latitude e longitude opcionais, juntas (400).
2. **Pontos operacionais** (`TB_PONTO_OPERACIONAL`, `ORGANIZACAO.GERENCIAR`): nome, tipo (base, ponto fixo, ponto
   programado), referência e posição no mapa.
   - Valem para qualquer unidade de atendimento, como a ação na escola ou o ponto do Consultório na Rua. Nível de gestão
     fica de fora (422).
   - **Histórico:** encerramento com data e motivo; nada se apaga. Nome único entre os ativos da unidade (409).
   - Ponto de rota vigente não encerra (422).
3. **Unidade móvel é a unidade com o perfil Atenção móvel vigente.** Base e rota exigem o perfil (422).
   - **Base:** uma vigente por unidade (409).
   - **Rota** (`TB_ROTA_PROGRAMADA`): nome, dias da semana, horário e pontos em ordem, ativos e da própria unidade (422). O
     ponto não se repete na rota (409). Tem histórico como o ponto.
   - **Localização** (`TB_POSICAO_UNIDADE_MOVEL`): cada passagem por um ponto, informada por quem opera, com data e hora. A
     última é a localização atual. **Não há GPS nem rastreamento.** O ponto atual de novo: 409; data futura ou anterior à
     passagem anterior: 400.
4. **Uma regra a mais na MO1:** o perfil Atenção móvel não encerra com base ou rota vigente (422).
5. **Leitura:** a de Equipamentos para os pontos. Quem atende (`ATENDIMENTO.GERENCIAR`) só vê os locais de realização: pontos
   ativos e o domicílio do paciente, em rota própria, sem a ficha da unidade.

### API

- `GET /api/v1/unidade-saude/id/{uuid}/pontos`;
- `GET .../locais-de-realizacao?pacienteId=`;
- `POST .../ponto`, `PATCH .../ponto/{id}`, `POST .../ponto/{id}/encerramento`;
- `POST .../rota`, `PATCH .../rota/{id}`, `POST .../rota/{id}/encerramento`;
- `POST .../posicao`;
- `POST` e `PATCH /api/v1/atendimento/` aceitam `localRealizacao`, `localDescricao`, `localLatitude`, `localLongitude`,
  `domicilioId` e `pontoOperacionalId`. O `PATCH` sem local volta o atendimento para a unidade.

### Telas

- **Equipamentos de Saúde:** aba **Pontos** na ficha da unidade, com o mapa dos pontos. Na unidade móvel, também "Onde está
  agora" e as rotas programadas.
- **Atendimentos:** campo Local de realização na gaveta do atendimento; no domicílio, sugere o do cadastro do paciente. O
  local aparece na ficha do atendimento.
- **Correção de passagem:** concluir o atendimento pela ficha agora preserva a especialidade, que era enviada vazia e se
  perdia quando não havia marcação, além do local novo.

### Migração

`V53__modelo_operacional_ponto_operacional.sql`: as tabelas de ponto, rota, dias, pontos da rota e posição, e as colunas do
local no atendimento, todas opcionais.

## Consequências

- **Atendimento domiciliar e Consultório na Rua** passam a registrar onde o cuidado aconteceu, sem unidade nova.
- **SAMU e ambulância seguem fora:** dependem de Transporte (#16). A base e a central usam o perfil Atenção móvel.
- **Ficam para depois:** agendar pelo ponto ou pela rota; GPS e rastreamento em tempo real; indicadores por local de
  realização.

## Testes

- **JUnit:**
  - `PontoOperacionalControllerTest`: ponto em unidade de atendimento e nível de gestão (422); nome repetido (409) e reuso
    depois de encerrado; coordenada sem par e início futuro (400); ajuste; base sem perfil (422) e segunda base (409); rota
    em ordem; rota sem perfil, de ponto alheio, com ponto repetido, com horário invertido e sem dias; ponto preso à rota
    (422); ajuste da rota e encerrada (422); passagens, ponto repetido (409), data inválida (400) e unidade não móvel (422);
    perfil móvel preso à base (422);
  - `LocalRealizacaoAtendimentoControllerTest`: sem local; descrição e posição; sem descrição ou com só a latitude (400);
    ponto da própria unidade, alheio e encerrado; domicílio do cadastro; domicílio inexistente (404); edição sem local;
  - `PontoOperacionalAutorizacaoControllerTest`, com a segurança ligada: quem gerencia inclui, o gestor só lê, o gestor de
    outra unidade nem lê, e quem atende vê os locais, mas não a ficha.
- **Robot:** `PontosOperacionais.robot` (API, sete casos) e os casos 10 a 12 de `UI_modelo_operacional.robot` (ponto, rota e
  posição da unidade móvel, e local do atendimento).
