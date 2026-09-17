# 0013 — Implementar o 5º nível (Unidade de Saúde) e o vínculo de supervisão regional

## Status

Aceita e implementada.

## Contexto

A ADR-0009 já tinha decidido formalizar um 5º nível hierárquico, "Unidade de Saúde" (UBS/Hospital),
vinculado como filho de **Municipal**, mas só a renomeação dos 4 níveis existentes (Federal/
Estadual/Municipal/Regional) tinha sido executada — o 5º nível ficou registrado como decisão, não
como código.

Ao retomar esse trabalho, um estudo externo sobre o domínio (feito em outra ferramenta, buscando
avaliar separar "estrutura organizacional" de "unidades de saúde" e adicionar um módulo de RH —
ver ADR-0014) propôs uma regra de negócio diferente: a unidade de saúde subordinada à
**Administração Regional**, não à Municipal. Isso contradizia diretamente a ADR-0009 já aceita.
Confrontado o estudo com o código e a ADR real, a decisão do usuário foi:

1. Manter `unidadeSuperior` apontando para **Municipal**, como a ADR-0009 já determinava — não é
   revisitada aqui.
2. Adicionar uma referência **separada e não-hierárquica** para a Regional: uma "supervisão
   regional" — vínculo lateral de apoio técnico, não o pai estrutural da unidade.

Também foi confirmado, nessa retomada, que a estrutura de dados **não** seria dividida em
`Administracao` + `UnidadeDeSaude` separadas (como o estudo externo também sugeria) — o modelo de
entidade única autorreferenciada da ADR-0002 é mantido, reaproveitando 100% do
`AbstractHierarquicoService` já existente.

### Descoberta durante a implementação: `getTipo()` não comporta dois valores

O 5º nível precisa aceitar **dois** valores de `tipo` (`UBS` e `HOSPITAL`) sob um único
controller/service, mas `AbstractHierarquicoService` era construído em cima de
`getTipo(): TipoUnidadeDeSaude` (um valor só) — não previsto na ADR-0009 original. Isso exigiu
generalizar a classe-base para `getTiposAceitos(): List<TipoUnidadeDeSaude>`.

## Decisão

- Generalizar `AbstractHierarquicoService<RES>`: `getTipo()` → `getTiposAceitos(): List<TipoUnidadeDeSaude>`.
  `getAll()`, `buscarUnidadeDeSaudePorNome()` e `salvar()` passam a operar sobre a lista. Os 4
  services existentes (`FederalService`/`EstadualService`/`MunicipalService`/`RegionalService`)
  só trocam `getTipo()` por `getTiposAceitos()` retornando `List.of(TIPO_ÚNICO)` — comportamento
  idêntico para eles.
- `vincularSuperiorSeInformado()` passa a chavear o nível-superior esperado pelo **tipo real da
  entidade sendo salva** (`unidadeDeSaude.getTipo()`), não mais pelo tipo do service — necessário
  porque o service de Unidade de Saúde aceita dois tipos. Novo caso no switch:
  `case UBS, HOSPITAL -> MUNICIPAL`.
- Novo método `vincularSupervisaoRegionalSeInformado(...)`: mesmo padrão de
  not-found/tipo-errado de `vincularSuperiorSeInformado`, mas o tipo esperado é sempre fixo
  (`REGIONAL`), independente do tipo da unidade sendo salva. Persistido em
  `UnidadeDeSaude.supervisaoRegional` (`@ManyToOne` self-reference, nullable,
  `supervisao_regional_id`), separado de `unidadeSuperior`.
- Novo nível completo seguindo o padrão dos outros 4: `UnidadeSaudeService extends
  AbstractHierarquicoService<UnidadeSaudeResponseDto>` (`getTiposAceitos() → List.of(UBS,
  HOSPITAL)`), `UnidadeDeSaudeUnidadeController` em `/api/v1/unidade-saude/` (tag "Unidade de
  Saúde"), com os mesmos 8 endpoints dos demais níveis mais dois exclusivos: `PATCH
  supervisao-regional/{nome}` e `PATCH responsavel/{nome}` (este último documentado na ADR-0014).

## Trade-offs considerados

**Generalizar para `getTiposAceitos()` (escolhida)**
- ✅ Reaproveita 100% do CRUD comum já existente (`AbstractHierarquicoService`), sem duplicar
  lógica de busca/atualização/desabilitação para o 5º nível.
- ✅ Mudança mecânica e de baixo risco para os 4 níveis existentes (troca de uma linha cada,
  comportamento idêntico).
- ❌ Muda a assinatura de um método abstrato usado por toda a hierarquia — qualquer novo nível
  futuro precisa seguir o novo contrato (lista, não valor único).

**Um controller/service por tipo (`UbsService`/`HospitalService` separados) (rejeitada)**
- ✅ Mantém `getTipo()` de valor único, sem generalizar a classe-base.
- ❌ Rejeitada: UBS e HOSPITAL são o mesmo conceito de negócio ("Unidade de Saúde") na ADR-0009 —
  dois controllers/endpoints (`/api/v1/ubs/`, `/api/v1/hospital/`) fragmentaria artificialmente
  algo que o domínio trata como um nível só, e duplicaria toda a lógica de vínculo/validação.

**Supervisão regional como o próprio `unidadeSuperior` (rejeitada)**
- ✅ Não precisaria de campo novo nem de método de vínculo novo.
- ❌ Rejeitada a pedido explícito do usuário: contradiria a ADR-0009 já aceita (Municipal como pai
  estrutural) e colapsaria dois conceitos diferentes — "quem é o pai na hierarquia administrativa"
  (Municipal) e "quem dá apoio técnico/supervisão" (Regional) — em um único campo.

## Consequências

**Positivas**
- Completa a ADR-0009: os 5 níveis da esfera de gestão do SUS (Federal/Estadual/Municipal/
  Regional/Unidade de Saúde) agora existem de fato no código, não só na decisão.
- `UBS`/`HOSPITAL`, que existiam no enum `TipoUnidadeDeSaude` como código morto, agora têm
  controller/service reais.
- A supervisão regional fica registrada como um vínculo de primeira classe (campo próprio,
  validado, atualizável via endpoint dedicado), em vez de forçada dentro de `unidadeSuperior`.

**Negativas / pendências**
- `docs/adr/DER-atual.md` fica desatualizado (não sketcha `supervisao_regional_id`,
  `responsavel_cpf`, `responsavel_id`, nem `TB_PROFISSIONAL`) — pendência conhecida, mesma dívida
  já registrada na ADR-0009 original, não resolvida nesta rodada.
- `vincularSupervisaoRegionalSeInformado` não impede um ciclo (ex.: uma unidade REGIONAL sendo sua
  própria supervisão) além da checagem de tipo — igual ao comportamento já existente (e aceito) de
  `vincularSuperiorSeInformado` para os outros níveis.
