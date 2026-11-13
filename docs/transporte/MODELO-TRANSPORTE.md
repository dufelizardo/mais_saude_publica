# Modelo do Transporte Sanitário (domínio 16)

Desenho na [ADR-0178](../adr/0178-transporte-sanitario.md).

## Fatias

| Fatia | O que é | Estado |
|---|---|---|
| T1 | Frota com situação em histórico, plantão das viaturas com tripulação mínima, viatura empenhada pelo SAMU | ✅ ADR-0178, V63 |
| T2 | Transporte eletivo: solicitação (consulta, exame, hemodiálise, alta, TFD), agenda de viagens, embarques e quilometragem | ✅ ADR-0190, V75 |
| T3 | Veículos nas Emergências: pedido, cessão e devolução | ✅ ADR-0192, V76 |

## Entidades (T1)

```text
Veiculo (lotado numa unidade)
  placa, prefixo (únicos) · tipo · modelo · ano · lugares · maca · cadeiraDeRodas · quilometragem · situacao
      │
      ├── SituacaoVeiculoRegistro (histórico, só inclusão): situacao · inicio · motivo
      │
      └── PlantaoViatura: inicio · fim (até 24 h)
              └── TripulantePlantao: profissional · funcao (condutor, técnico, enfermeiro, médico)

OcorrenciaUrgencia ──► Veiculo (a viatura empenhada; ADR-0176)
```

## Entidades (T2)

```text
SolicitacaoTransporte (paciente, unidade que pede)
  motivo · dataHora · enderecoEmbarque · destino (unidade ou texto) · destinoMunicipio · trecho
  maca · cadeiraDeRodas · oxigenio · acompanhante · situacao · recorrenciaId
  laudo do TFD: cid · justificativa · profissional
      ├──► Agendamento | SolicitacaoRegulacao | Internacao (um só, opcional)
      │
ViagemTransporte (veiculo eletivo, condutor)
  saidaPrevista · retornoPrevisto (até 72 h) · destino · situacao · kmSaida · kmChegada
      └── EmbarqueViagem: solicitacao · situacao (previsto, embarcou, faltou)
```

## Regras

- **Lotação:** viatura do SAMU (USA, USB, motolância, aeromédico) só em unidade com o perfil Atenção móvel.
- **Tripulação mínima:**

| Tipo | Mínimo |
|---|---|
| USB | condutor e técnico de enfermagem (ou enfermeiro) |
| USA | condutor, enfermeiro e médico |
| Aeromédico | enfermeiro e médico |
| Motolância | técnico de enfermagem ou enfermeiro |
| Demais | condutor |

- **Conflitos:** a viatura ou um tripulante em dois plantões no mesmo período.
- **SAMU:**
  - a regulação escolhe a viatura disponível, com plantão e do tipo pedido;
  - o envio a põe em serviço;
  - o fim da ocorrência a devolve.
- **Situação manual:** manutenção, disponível e inativo; "em serviço" só pelo SAMU.

## Regras do transporte eletivo (T2)

- **Acompanhante:** obrigatório para menor de 18 anos e para maca; ocupa um lugar.
- **TFD:** laudo (CID, justificativa, quem pede), outro município, autorização da Comissão de TFD antes da viagem.
- **Recorrência:** hemodiálise e terapia seriada, dias da semana até 90 dias, uma solicitação por sessão.
- **Viagem:**
  - veículo eletivo, lugares, maca e cadeira de rodas;
  - o compromisso dentro da janela;
  - sem conflito de veículo ou condutor;
  - a saída põe o veículo em serviço; a chegada atualiza a quilometragem e o devolve.

## Veículos nas emergências (T3)

```text
Mobilizacao (veículo, E3)
  └── CessaoVeiculo: veiculo · cedidaEm · devolvidaEm · kmInicio · kmFim
```

- A coordenação pede; a frota cede (`TRANSPORTE.GERENCIAR` na unidade do veículo), até a quantidade pedida.
- Cedido: em serviço (mecanismo da T1), sem viagem eletiva nova (409); as programadas voltam como aviso.
- Devolvido pela frota com a quilometragem, ou no fim do pedido, na desmobilização e no encerramento.

## Permissões

| Permissão | Para quê | Papéis padrão |
|---|---|---|
| `TRANSPORTE.CONSULTAR` | Frota, situação e plantões | Coordenador de frota, Gestor, Atendente de regulação, Médico regulador de urgência |
| `TRANSPORTE.GERENCIAR` | Veículos, manutenção, plantões e viagens | Coordenador de frota, Gestor |
| `TRANSPORTE.SOLICITAR` | Pedir transporte eletivo para o paciente | Recepção, Coordenador de enfermagem, Assistente social |
| `TFD.AUTORIZAR` | Autorizar e negar o TFD (lê o laudo) | Comissão de TFD, Gestor |
