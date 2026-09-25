# 0030 — Setor Administrativo e relação com Unidade de Saúde

## Status

Aceita e implementada (Fase 1 — [PR #181](https://github.com/dufelizardo/mais_saude_publica/pull/181)).

## Contexto

A "Especificação Preliminar do Setor Administrativo Adaptativo" (documento externo trazido pelo
usuário, curada em [ESCOPO-ADMINISTRATIVO.md](../administrativo/ESCOPO-ADMINISTRATIVO.md)) propõe
um conceito de `Setor` como subdivisão operacional da unidade de saúde, e um `SetorAdministrativo`
como a estrutura responsável pela operação administrativa — usando o mesmo modelo
independentemente do equipamento (UBS, UPA, Hospital, Laboratório etc.).

Hoje não existe nenhum conceito de `Setor` no código (busca exaustiva não encontrou entidade, enum,
controller ou tela). `UnidadeDeSaude` existe como entidade única autorreferenciada para os 5 níveis
hierárquicos (Federal/Estadual/Municipal/Regional/Unidade de Saúde — ADR-0002, ADR-0009, ADR-0013).

O próprio documento-fonte já define, na sua Regra 5, que "tipo de unidade não implica classe
administrativa" (ex.: um hospital não deve exigir `HospitalAdministracaoService`). Ao desenhar o
`Setor`, aplicamos essa mesma regra reflexivamente a ele: se `SetorAdministrativo` virasse uma
classe/entidade própria, seria o mesmo erro que a ADR-0013 já rejeitou ao recusar separar
`Administracao` de `UnidadeDeSaude` numa proposta externa anterior.

## Decisão

- Nova entidade `Setor`: `uuid`, `unidade` (`@ManyToOne UnidadeDeSaude`), `nome`, `codigo`, `tipo`,
  `ativo`. Fase 5 (ver ADR-0034) acrescenta `responsavel` (`@ManyToOne Profissional`, opcional,
  resolvido por matrícula).
- Novo enum `TipoSetor`: `ADMINISTRATIVO, ASSISTENCIAL, APOIO, TECNICO` — lista inicial, ampliável
  por dado/negócio conforme outros domínios (assistencial, apoio) forem modelados.
- **`SetorAdministrativo` não é uma classe ou entidade própria.** É simplesmente um `Setor` com
  `tipo = ADMINISTRATIVO`. As capacidades e processos administrativos (ADR-0031 a ADR-0033) se
  associam à unidade/ao perfil, não a uma subclasse de setor.
- `Setor` segue o mesmo padrão de camadas flat já usado pelo restante do projeto (`models/`,
  `services/version1/`, `controllers/version1/` — ADR-0005 ainda não implementou separação por
  domínio, e este ADR não antecipa essa mudança).
- `Setor` não se confunde com `Lotacao`/`Cargo` do RH: é estrutura organizacional da unidade, o RH
  continua sendo autoridade sobre o vínculo funcional do profissional (ver ADR-0034).

## Trade-offs considerados

**`Setor` genérico com enum de tipo (escolhida)**
- ✅ Reaproveita CRUD comum sem duplicar `Setor`/`SetorAdministrativo`/`SetorAssistencial` como
  entidades separadas.
- ✅ Segue o mesmo precedente de generalização por enum já usado em `TipoUnidadeDeSaude` e
  `getTiposAceitos()` (ADR-0013).
- ❌ `TipoSetor.ASSISTENCIAL/APOIO/TECNICO` entram no enum sem nenhum consumidor até que esses
  domínios existam — aceito como fundação, não como código morto útil hoje.

**`SetorAdministrativo` como entidade/subclasse própria (rejeitada)**
- ✅ Modelaria mais explicitamente o conceito do documento-fonte.
- ❌ Contradiz a própria Regra 5 do documento aplicada ao `Setor`; reproduziria o padrão que a
  ADR-0013 já rejeitou para `UnidadeDeSaude`/`Administracao`.

## Consequências

**Positivas**: abre caminho para as ADRs seguintes (perfil, capacidades) sem exigir modelagem
paralela por tipo de unidade; um novo tipo de setor futuro é só um valor de enum, não uma classe
nova.

**Negativas / pendências**: `TipoSetor.ASSISTENCIAL/APOIO/TECNICO` ficam sem consumidor até que
esses domínios sejam modelados; `Setor` não é obrigatório por unidade enquanto não houver
tela/regra de negócio que exija — isso é decisão de implementação futura, não deste ADR.

## Referências

- [ESCOPO-ADMINISTRATIVO.md](../administrativo/ESCOPO-ADMINISTRATIVO.md) — documento de origem e
  roadmap das fases.
- [ADR-0002](./0002-modelar-hierarquia-como-entidade-unica-autorreferenciada.md) e
  [ADR-0013](./0013-implementar-5-nivel-unidade-de-saude-e-supervisao-regional.md) — precedente de
  entidade única autorreferenciada e de rejeição de separação `Administracao`/`UnidadeDeSaude`.
- [ADR-0031](./0031-perfil-administrativo-por-tipo-de-unidade.md) — como o perfil administrativo se
  relaciona com o tipo de unidade, não com o `Setor`.
