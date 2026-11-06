# 0162 — Vigilância em Saúde: controle de vetores (V4a)

## Status

Aceita e implementada. Primeira parte da fatia V4 (ambiental e zoonoses) do domínio 23, na
[ADR-0110](./0110-vigilancia-em-saude.md) e em [`vigilancia/MODELO-VIGILANCIA.md`](../vigilancia/MODELO-VIGILANCIA.md),
seções 5 e 6. Usa o imóvel do Território ([ADR-0127](./0127-cadastro-territorial.md)) e a notificação e o surto da
Vigilância ([ADR-0124](./0124-vigilancia-epidemiologica-fundacao.md), [ADR-0131](./0131-vigilancia-surto-e-medidas.md)). A
V4b (animais e vigilância ambiental) vem depois, com ADR própria.

## Contexto

- **O controle do Aedes é a rotina diária do agente de endemias** e o que mais pesa na dengue, na chikungunya e na zika.
  O Programa Nacional de Controle da Dengue (PNCD) organiza o trabalho em ciclos bimestrais de visita aos imóveis, com os
  depósitos classificados por tipo e os índices de infestação.
- **O modelo pede** `Território → Imóvel → Vistoria → Foco → Ação de controle` (seção 6). O imóvel é o do Território, mas
  o agente de endemias não fica preso à microárea do agente comunitário de saúde.

## Decisão

1. **Papéis e permissões:**
   - `ZOONOSES.VISTORIAR`: registrar a vistoria, os focos e o tratamento;
   - `ZOONOSES.GERENCIAR`: planejar e registrar as ações de controle e ver os casos sem bloqueio;
   - papel novo **Agente de endemias** (Lei 11.350/2006), com `ZOONOSES.VISTORIAR`;
   - papel novo **Vigilância ambiental** (coordenação de zoonoses), com as duas permissões e o registro de evento
     emergencial;
   - o gestor ganha `ZOONOSES.GERENCIAR`; a leitura das vistorias, dos focos, dos índices e das ações é também de quem
     investiga (`VIGILANCIA.INVESTIGAR`) e de quem consulta indicadores.
2. **Vistoria** (`TB_VISTORIA_VETORIAL`, só inclusão):
   - imóvel ativo do Território (422), agente pela matrícula (404), data não futura (400; sem ela, hoje);
   - tipo: rotina, levantamento de índice (LIRAa), bloqueio, ponto estratégico ou denúncia;
   - resultado: inspecionado, fechado ou recusado. **Fechado ou recusado não leva depósito nem foco** (422) e vira
     pendência de revisita: a pendência é o imóvel cuja última vistoria do ciclo não foi inspecionada; a revisita
     inspecionada a resolve;
   - **depósitos inspecionados por tipo do PNCD:** A1 (caixa d'água elevada), A2 (no nível do solo), B (pequeno móvel),
     C (fixo), D1 (pneu e material rodante), D2 (lixo e sucata), E (natural); os eliminados e os tratados não passam dos
     inspecionados (422);
   - tratamento focal: o larvicida e a quantidade (a quantidade pede o larvicida, 400);
   - **o ciclo bimestral** (1 a 6) sai da data e fica guardado com o ano, para os índices;
   - pode apontar a ação de controle que a motivou (cancelada, 422).
3. **Foco** (`TB_FOCO_VETORIAL`), registrado na vistoria inspecionada:
   - tipo do depósito, fase (larva, pupa, as duas), espécie suspeita (*Aedes aegypti*, *Aedes albopictus*, outra, não
     identificada) e se a amostra foi coletada;
   - **os focos de um tipo não passam dos depósitos inspecionados desse tipo** (422);
   - resultado do laboratório de entomologia, só com amostra (422); **negativo não conta nos índices**;
   - ativo até tratado ou eliminado (só do ativo, 422).
4. **Índices do ciclo** (calculados na leitura; sem ciclo, o atual):
   - **IIP** (índice de infestação predial): imóveis com foco ÷ imóveis inspecionados × 100;
   - **IB** (índice de Breteau): depósitos positivos ÷ imóveis inspecionados × 100;
   - **classificação do Ministério da Saúde:** IIP abaixo de 1%, satisfatório; de 1% a 3,9%, alerta; 4% ou mais, risco;
   - por área e no município, com os imóveis visitados, os pendentes e o percentual de pendência.
5. **Ação de controle** (`TB_ACAO_CONTROLE_VETORIAL`, `ZOONOSES.GERENCIAR`):
   - tipo: bloqueio de transmissão, nebulização (UBV), mutirão de limpeza, ação educativa ou outra;
   - microáreas (ao menos uma, 400), data prevista, responsável e descrição;
   - **o caso notificado** de dengue, chikungunya ou zika (outro agravo, 422; já com ação em curso, 409) e o surto, ambos
     opcionais;
   - planejada → realizada (data não futura, 400; imóveis trabalhados, resultado) ou cancelada (com o motivo); só da
     planejada (422).
6. **Casos sem bloqueio** (`GET vetores/bloqueios-pendentes`, leitura auditada): os casos de arbovirose dos últimos 30 dias,
   não descartados e sem ação em curso, **só com o lugar e as datas, sem o paciente**.
7. **O agente não vê família nem morador:** a lista de imóveis para a vistoria traz o endereço, a microárea, o ponto e a
   última vistoria.

### API

| Método | Caminho | Permissão |
|---|---|---|
| GET | `vetores/imoveis`, `vetores/pendencias` | `ZOONOSES.VISTORIAR` ou `ZOONOSES.GERENCIAR` |
| POST | `vetores/vistoria` | `ZOONOSES.VISTORIAR` |
| GET | `vetores/vistorias`, `vetores/focos`, `vetores/indices`, `vetores/acoes` | as de zoonoses, `VIGILANCIA.INVESTIGAR` ou `INDICADOR.CONSULTAR` |
| POST | `vetores/foco/{id}/laboratorio` | `ZOONOSES.VISTORIAR` ou `ZOONOSES.GERENCIAR` |
| POST | `vetores/foco/{id}/situacao` | `ZOONOSES.VISTORIAR` |
| POST | `vetores/acao`, `vetores/acao/{id}/realizacao`, `vetores/acao/{id}/cancelamento` | `ZOONOSES.GERENCIAR` |
| GET | `vetores/bloqueios-pendentes` | `ZOONOSES.GERENCIAR` ou `VIGILANCIA.INVESTIGAR` |

### Tela

- **"Zoonoses e vetores"** no menu Vigilância, com o filtro de área e as abas:
  - **Vistorias:** registrar (o imóvel buscado no Território, os depósitos por tipo, o tratamento e os focos), as
    pendências de revisita com "Revisitar" e as vistorias do ciclo;
  - **Focos:** os ativos no mapa, o resultado do laboratório e o tratamento;
  - **Índices do ciclo:** ano e ciclo, os números do município e a tabela por área com a classificação em cores;
  - **Ações de controle:** os casos sem bloqueio com "Planejar bloqueio", planejar, registrar a realização e cancelar.
- **Sala de Situação:** a área com IIP de risco no ciclo atual aparece como alerta alto.

### Migração

`V49__vigilancia_controle_de_vetores.sql`: as tabelas da vistoria, do foco, da ação de controle e das microáreas da ação.
As permissões e os papéis entram pelo `CatalogoDeAcesso`.

## Consequências

- **O ciclo do PNCD tem registro e índice** sem planilha paralela, e a pendência de revisita fica visível.
- **O caso notificado puxa o bloqueio:** a Vigilância epidemiológica e a de zoonoses trabalham sobre o mesmo caso, sem
  expor o paciente ao agente.
- **Ficam para depois:**
  - a V4b: animais (raiva e leishmaniose) e a vigilância ambiental (água, risco e monitoramento);
  - o quarteirão como unidade de trabalho do agente (hoje, a microárea e o imóvel);
  - o envio ao sistema nacional (domínio 20, adiado).

## Testes

- **JUnit:**
  - `ControleVetorialControllerTest`:
    - vistorias, focos e índices: imóveis da área com a busca (sem área, 400); data futura (400); fechado com depósito
      (422); foco acima dos depósitos do tipo (422); larvicida sem nome (400); agente inexistente (404); os índices (3
      inspecionados, 2 positivos, IIP 66,7, IB 100, 1 pendente, risco); ciclo inválido (400); o laboratório negativo
      deixando de contar e o foco sem amostra (422); tratar e eliminar o foco (ativo de novo, 400; de novo, 422); a
      revisita recuperando a pendência;
    - ações e bloqueios: o caso de dengue sem bloqueio (o de sarampo, não; sem o nome do paciente); ação sem microárea
      (400), de outro agravo (422), repetida (409); a vistoria do bloqueio; realizar (data futura, 400) e cancelar a
      realizada (422); a ação cancelada não recebe vistoria (422);
  - `ControleVetorialAutorizacaoControllerTest`, com segurança ligada: o agente vistoria e não planeja; a vigilância
    ambiental planeja; a vigilância epidemiológica e o gestor leem os índices; a recepção, não; os casos sem bloqueio são
    da coordenação e da investigação.
- **Robot de API:** `test/vigilancia/ControleVetorial.robot`, CT-001 a CT-012.
- **Robot de tela:** `test/ui/vigilancia/vetores/UI_controle_vetorial.robot`, CT-001 a CT-003.
