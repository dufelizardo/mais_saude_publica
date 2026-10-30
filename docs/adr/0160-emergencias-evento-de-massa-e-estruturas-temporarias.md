# 0160 — Emergências e Desastres: evento de massa e estruturas temporárias (E6a)

## Status

Aceita e implementada. Primeira parte da fatia E6 (eventos especializados) do domínio 27, na
[ADR-0113](./0113-emergencias-e-desastres.md) e em [`emergencias/MODELO-EMERGENCIAS.md`](../emergencias/MODELO-EMERGENCIAS.md),
seções 5, 8 e 15. Usa as E1 a E3 e a E5 ([ADR-0156](./0156-emergencias-nucleo.md), [ADR-0157](./0157-emergencias-contingencia.md),
[ADR-0158](./0158-emergencias-coordenacao.md), [ADR-0159](./0159-emergencias-desmobilizacao-e-relatorio.md)). A E6b (painel
da epidemia e exposição ambiental) vem em seguida, com ADR própria.

## Contexto

- **O modelo proíbe uma classe por tipo de evento** (seção 5): não existem `PlanoEnchente`, `PlanoEpidemia` nem
  `PlanoEventoMassa`. O que cada tipo tem de próprio precisa ser configurável.
- **O evento de massa é uma emergência planejada** (seção 15): carnaval, show, jogo, festa religiosa. É cadastrado antes
  de acontecer, com público estimado, local, período, postos de atendimento e ambulâncias. Até a E5, toda emergência
  nascia ativa.
- **O hospital de campanha entra como capacidade, não como unidade** (seção 17, "fora do escopo": hospital de campanha
  completo). O mesmo vale para o posto médico do evento e a tenda de hidratação da epidemia de dengue.

## Decisão

1. **Perfil do tipo de emergência** (`TB_TIPO_EMERGENCIA.perfil`): geral, evento de massa, epidemia, desastre ambiental.
   Editável no catálogo; sem o perfil, o tipo nasce geral e, no ajuste, mantém. A carga classifica os tipos existentes:
   evento de massa, epidemia, e desastre ambiental para produto perigoso e incêndio florestal.
2. **Emergência planejada** (situação nova, antes da ativa; só no perfil evento de massa):
   - **planejar:** a conversão do evento recebe `planejada` (`EMERGENCIA.ATIVAR`); em outro perfil, 422;
   - **enquanto planejada:** entram os dados do evento, o território e as estruturas. Ação, mobilização, plano,
     capacidade, avaliação de impacto e nível só depois que a operação começa (422);
   - **iniciar a operação** (`EMERGENCIA.ATIVAR`): pede os dados do evento (422) e passa a ativa, com o início contado
     de agora;
   - **cancelar:** encerrar uma planejada cancela o evento e desativa as estruturas planejadas; o relatório só começa
     depois (como na E5).
3. **Dados do evento de massa** (`TB_EVENTO_MASSA`, um por emergência; `EMERGENCIA.COORDENAR` ou `EMERGENCIA.ATIVAR`):
   natureza (esportivo, show, festa popular ou carnaval, religioso, manifestação, outro), local, início e fim
   previstos (o fim não vem antes do início, 400), público estimado e máximo (o máximo não fica abaixo do estimado, 400),
   organizador e contato, classificação de risco (baixo, médio, alto) e observação. Só no perfil evento de massa (422);
   na emergência planejada ou ativa (422).
4. **Estrutura temporária de atendimento** (`TB_ESTRUTURA_TEMPORARIA`; `EMERGENCIA.COORDENAR` ou `EMERGENCIA.ATIVAR`):
   - **uma entidade para os três casos:** posto médico, tenda de hidratação, hospital de campanha, outra;
   - **campos:** nome, local e ponto no mapa (latitude e longitude juntas, 400), **unidade de referência** existente
     (404), leitos de observação, atendimentos por dia, ambulâncias de prontidão (um número, até existir Transporte) e
     horário;
   - **ciclo:** planejada → em operação (só com a emergência ativa, 422) → desativada (com o motivo, 400). Desativada não
     muda (422). Nova estrutura e ajuste só na emergência planejada ou ativa (422); desativar vale também na
     desmobilização;
   - **é capacidade, não unidade:** a unidade de referência continua dona da escala, do estoque e dos leitos. Mobilizar
     para a estrutura é mobilizar para a unidade de referência (E3).
5. **Boletim da estrutura** (`TB_BOLETIM_ESTRUTURA`, `EMERGENCIA.REGISTRAR`): data, atendimentos, remoções para a rede,
   casos graves, óbitos e observação; datado e repetível (um por turno, se quiser), só inclusão. Só da estrutura que já
   operou (422); a data não é futura (400) nem anterior à abertura (422). Vale também na desmobilização, para o último
   dia.
6. **Desmobilização e encerramento (E5):** as estruturas planejadas e em operação são desativadas junto com o resto, e
   o registro da desmobilização guarda quantas.
7. **Relatório pós-evento (E5):** os números ganham as estruturas que operaram, a soma dos boletins (atendimentos,
   remoções, graves, óbitos) e, no evento de massa, o público estimado. O CSV leva as mesmas linhas.
8. **Sem permissão nova.**

### API

| Método | Caminho | Permissão |
|---|---|---|
| POST | `evento-emergencial/{id}/emergencia` (com `planejada`) | `EMERGENCIA.ATIVAR` |
| PUT | `emergencia/{id}/evento-massa` | `EMERGENCIA.COORDENAR` ou `EMERGENCIA.ATIVAR` |
| POST | `emergencia/{id}/operacao` | `EMERGENCIA.ATIVAR` |
| POST | `emergencia/{id}/estrutura` | `EMERGENCIA.COORDENAR` ou `EMERGENCIA.ATIVAR` |
| PUT | `emergencia/{id}/estrutura/{estrutura}` | `EMERGENCIA.COORDENAR` ou `EMERGENCIA.ATIVAR` |
| POST | `emergencia/{id}/estrutura/{estrutura}/situacao` | `EMERGENCIA.COORDENAR` ou `EMERGENCIA.ATIVAR` |
| POST | `emergencia/{id}/estrutura/{estrutura}/boletim` | `EMERGENCIA.REGISTRAR` |

O tipo ganha `perfil`. A emergência ganha `perfil`, `eventoMassa` (também na lista) e `estruturas` (com os boletins e os
totais); a situação ganha `PLANEJADA`; a desmobilização, `estruturasDesativadas`; os indicadores do relatório, os números
das estruturas e o público estimado.

### Tela

- **Catálogos:** o perfil do tipo.
- **Converter em emergência:** no evento de massa, "Planejar: o evento ainda não começou" (marcado por padrão).
- **Aba "Planejadas"** na tela Emergências, com o início previsto.
- **Detalhe da planejada:** "Território", "Iniciar operação" (só com os dados do evento) e "Cancelar evento"; a
  coordenação aparece depois que a operação começa.
- **Seções novas no detalhe:** "Evento de massa" (os dados e o editor) e "Estruturas temporárias" (nova, editar, abrir,
  boletim, desativar; os totais dos boletins). As estruturas com ponto aparecem no mapa do território.
- **Relatório pós-evento:** os números das estruturas e o público.
- **Sala de Situação:** o evento de massa planejado para os próximos 7 dias aparece como alerta baixo; a emergência
  planejada não conta como ativa.

### Migração

`V47__emergencias_eventos_especializados.sql`: o perfil do tipo (com a carga), o CHECK da situação da emergência, a
coluna da desmobilização e as tabelas do evento de massa, das estruturas e dos boletins.

## Consequências

- **O evento de massa é preparado com antecedência** e a resposta começa na hora, com a estrutura já cadastrada.
- **Posto, tenda e hospital de campanha usam o mesmo cadastro,** sem unidade paralela: a rede continua a mesma, e a
  estrutura temporária some na desmobilização.
- **Ficam para depois:**
  - a E6b: o painel da epidemia (da Vigilância) e a exposição ambiental;
  - as ambulâncias como veículos (dependem de Transporte);
  - o hospital de campanha como unidade completa, com leitos próprios na Internação;
  - a E4 (integração da rede).

## Testes

- **JUnit:**
  - `EventosEspecializadosControllerTest`:
    - o perfil do tipo (carga; nasce geral; o ajuste sem perfil mantém);
    - o evento de massa planejado até o relatório: os bloqueios da planejada (ação, avaliação, operação sem dados),
      os dados do evento (período e público inválidos, 400), a lista com os dados, a estrutura (localização incompleta,
      400; abrir antes da operação, 422; boletim antes de abrir, 422), iniciar a operação (e de novo, 422), abrir, os
      boletins (data futura, 400; antes da abertura, 422; os totais), a desmobilização desativando as estruturas, a
      estrutura nova bloqueada e o boletim do último dia, os números do relatório e o CSV;
    - planejar fora do evento de massa (422), os dados do evento em outro perfil (422), o hospital de campanha no
      desastre (desativar sem motivo, 400; ajustar desativada, 422) e o cancelamento da planejada;
  - `EmergenciaAutorizacaoControllerTest.eventosEspecializadosPorPapel`, com segurança ligada: planejar e iniciar a
    operação são do gestor; o coordenador cuida dos dados do evento e das estruturas; a vigilância registra o boletim; a
    recepção, não.
- **Robot de API:** `test/emergencias/EventosEspecializados.robot`, CT-001 a CT-012.
- **Robot de tela:** `UI_emergencias.robot`, CT-009 (evento de massa planejado até a operação) e CT-010 (estrutura
  temporária e boletim).
