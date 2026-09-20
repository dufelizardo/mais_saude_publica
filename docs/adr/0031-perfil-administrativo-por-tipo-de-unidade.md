# 0031 — Perfil Administrativo por tipo de unidade

## Status

Proposta.

## Contexto

O documento-fonte (ver [ESCOPO-ADMINISTRATIVO.md](../administrativo/ESCOPO-ADMINISTRATIVO.md))
propõe que o tipo da unidade determine um `PerfilAdministrativo`, que por sua vez determina as
capacidades disponíveis — em vez de condicionais espalhadas pelo código (`if tipo == HOSPITAL`).

Hoje `TipoUnidadeDeSaude` tem apenas `FEDERAL, ESTADUAL, MUNICIPAL, REGIONAL, UBS, HOSPITAL`. O
documento-fonte cita equipamentos adicionais — UPA, Laboratório, CAPS, Centro de Especialidades,
Centro de Reabilitação, Policlínica — que ainda não existem como valores desse enum. Sem esses
valores, não há "tipo" para associar a um perfil.

O documento-fonte já concorda (sua seção 12) em não criar um `TipoEquipamentoSaude` paralelo,
reaproveitando `TipoUnidadeDeSaude` — mesma direção que a ADR-0013 já tomou ao generalizar
`UnidadeSaudeService.getTiposAceitos(): List<TipoUnidadeDeSaude>` para aceitar `UBS` e `HOSPITAL`
sob o mesmo 5º nível hierárquico.

## Decisão

- Estender `TipoUnidadeDeSaude` com novos valores sob o 5º nível ("Unidade de Saúde"), ao lado de
  `UBS`/`HOSPITAL`: `UPA, LABORATORIO, CAPS, CENTRO_ESPECIALIDADES, CENTRO_REABILITACAO,
  POLICLINICA`. Esses valores entram na lista aceita por `UnidadeSaudeService.getTiposAceitos()`,
  sem exigir novo controller/service (mesmo padrão da ADR-0013).
- Nova entidade `PerfilAdministrativo`: `uuid`, `codigo` (ex.: `ADMIN_UBS`), `nome`, `descricao`,
  `ativo`.
- Relação `TipoUnidadeDeSaude → PerfilAdministrativo` resolvida por **configuração em tabela** (uma
  linha de associação por tipo), não por campo fixo em `UnidadeDeSaude` nem por condicional no
  código.
- Sem override de perfil por unidade individual nesta fase — cada unidade usa o perfil
  correspondente ao seu tipo. Override por unidade fica para quando houver caso real (Regra 7 do
  documento-fonte).

## Trade-offs considerados

**Perfil resolvido por tipo via tabela de configuração, sem override por unidade (escolhida)**
- ✅ Resolve o caso descrito no documento (UBS com perfil diferente de Hospital) sem complexidade
  extra.
- ❌ Uma unidade "atípica" que precise de capacidades fora do padrão do seu tipo não tem como ser
  configurada individualmente ainda — aceito até que apareça um caso real.

**Perfil por unidade individual desde já (rejeitada)**
- ✅ Mais flexível em tese.
- ❌ Nenhum caso real hoje exige essa granularidade — construir para esse cenário agora seria
  antecipar requisito hipotético.

**Condicionais no código (`if tipo == HOSPITAL`) em vez de configuração (rejeitada)**
- ✅ Não precisa de tabela nova.
- ❌ É exatamente o anti-padrão que a seção 15 e a Regra 6 do documento-fonte identificam como
  problema a evitar; reproduziria o motivo original da proposta.

## Consequências

**Positivas**: um novo equipamento de saúde passa a exigir só uma nova linha de configuração
(tipo + perfil + capacidades), sem alterar service/controller existente.

**Negativas / pendências**: estender `TipoUnidadeDeSaude` é mudança de contrato — qualquer código
que faça switch exaustivo sobre esse enum precisaria ser revisado ao implementar (nesta
investigação não foi encontrado nenhum, mas deve ser reconferido no momento da implementação); o
catálogo inicial de perfis (seção 23 do documento-fonte) precisa de validação de negócio antes de
virar dado de produção — não é assumido como definitivo por este ADR.

## Referências

- [ESCOPO-ADMINISTRATIVO.md](../administrativo/ESCOPO-ADMINISTRATIVO.md)
- [ADR-0030](./0030-setor-administrativo-e-relacao-com-unidade-de-saude.md)
- [ADR-0013](./0013-implementar-5-nivel-unidade-de-saude-e-supervisao-regional.md) — precedente de
  `getTiposAceitos()` para múltiplos tipos sob um único service.
- [ADR-0032](./0032-catalogo-de-capacidades-administrativas.md) — capacidades associadas ao perfil.
