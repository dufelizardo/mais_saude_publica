# Modelo do Transporte Sanitário (domínio 16)

Desenho na [ADR-0178](../adr/0178-transporte-sanitario.md).

## Fatias

| Fatia | O que é | Estado |
|---|---|---|
| T1 | Frota com situação em histórico, plantão das viaturas com tripulação mínima, viatura empenhada pelo SAMU | ✅ ADR-0178, V63 |
| T2 | Transporte eletivo: solicitação (consulta, exame, hemodiálise, alta, TFD), agenda de viagens, embarques e quilometragem | ⬜ só com aprovação |
| T3 | Veículos nas Emergências (E4) | ⬜ só com aprovação |

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

## Permissões

| Permissão | Para quê | Papéis padrão |
|---|---|---|
| `TRANSPORTE.CONSULTAR` | Frota, situação e plantões | Coordenador de frota, Gestor, Atendente de regulação, Médico regulador de urgência |
| `TRANSPORTE.GERENCIAR` | Veículos, manutenção e plantões | Coordenador de frota, Gestor |
