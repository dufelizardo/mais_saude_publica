# Modelo da Gestão da Rede de Atenção (domínio 30)

Desenho na [ADR-0179](../adr/0179-gestao-da-rede-de-atencao.md). A Regulação processa a necessidade individual; este domínio define a estrutura:
quanto cada unidade oferta, como a oferta se reparte e, depois, como as unidades se organizam em redes.

## Fatias

| Fatia | O que é | Estado |
|---|---|---|
| GR1 | Oferta assistencial por executante, procedimento e mês; cotas da PPI; a Regulação consome a cota | ✅ ADR-0179, V64 |
| GR2 | Redes temáticas (Urgência, Cegonha/Alyne, Pessoa com Deficiência, Doenças Crônicas) e referência estrutural | ⬜ só com aprovação |
| GR3 | Cobertura da rede e indicadores de pactuação no BI | ⬜ só com aprovação |

## Entidades (GR1)

```text
ProcedimentoRegulado (Regulação)        UnidadeDeSaude (executante)
              │                                    │
              └──────────────┬─────────────────────┘
                             ▼
OfertaAssistencial: competencia (mês) · quantidade
                             │
                             ▼
CotaPpi: beneficiaria (unidade ou esfera) · quantidade
                             ▲
                             │ consumida na autorização
SolicitacaoRegulacao: cota · extraCota · justificativaExtraCota
```

## Regras

- **Sem oferta cadastrada,** a autorização segue como antes; com oferta, a vaga sai da cota.
- **Cota que vale:** a mais específica que cobre a solicitante (a própria, senão a da esfera mais próxima acima); a esgotada não passa adiante.
- **Saldo:** cota − (autorizada + agendada + realizada + falta); o cancelamento devolve.
- **Sem saldo:** só extracota com justificativa.
- **Limites:** soma das cotas ≤ oferta; oferta ≥ pactuado e ≥ usado; cota ≥ usado; só sai a cota nunca usada.

## Permissões

| Permissão | Para quê | Papéis padrão |
|---|---|---|
| `REDE.CONSULTAR` | Painel da PPI e saldo | Gestor, Médico regulador |
| `REDE.GERENCIAR` | Oferta, cotas e cópia de competência | Gestor |
