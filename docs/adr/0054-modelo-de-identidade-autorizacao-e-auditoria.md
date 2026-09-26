# 0054 — Modelo de Identidade, Autorização e Auditoria

## Status

Proposta. Mesmo status da ADR-0006, que esta ADR substitui: decisão de **forma** tomada,
implementação intencionalmente adiada (ver Contexto).

## Contexto

O usuário trouxe [`docs/pm/sistema_de_acesso.md`](../pm/sistema_de_acesso.md), um desenho detalhado
para o domínio de acesso do Mais Saúde Pública, com a posição explícita "concordo com isso 100%,
criar ADRs e DERs para isso". Recebe o mesmo tratamento de todo material-fonte trazido ao projeto
(`MAPA-DE-DOMINIOS.md` §1): aqui, diferente do levantamento de equipamentos (ADR-0053), **há uma
decisão real de arquitetura a tomar agora** — o documento-fonte propõe abandonar o modelo já
proposto pela ADR-0006 em favor de um mais rico, e o usuário está endossando essa troca.

Estado atual, confirmado por leitura direta do código: nenhuma dependência de segurança no
`pom.xml`, nenhuma entidade `Usuario`/`SecurityConfig` implementada — terreno limpo. A
**ADR-0006** ("Proposto — não implementado") propunha JWT + Spring Security com **4 roles fixas**
(`ADMIN, GESTOR, PROFISSIONAL, CONSULTOR`), sem escopo organizacional e sem catálogo de permissões
granular. A **ADR-0039** (decisão 2) reafirmou conscientemente adiar toda essa segurança, mesmo para
o domínio clínico, com a ressalva explícita de revisitar **antes de qualquer deploy com dado real de
paciente**.

`docs/adr/DER.md` já tem dois esboços relacionados a este domínio:
- Seção principal `1. USUARIO` (⚠️ SUPERSEDIDA) — tabela simples com uma coluna `roles` VARCHAR
  livre, refletindo exatamente as 4 roles fixas da ADR-0006, com FK 1:1 opcional para
  `PROFISSIONAL`/`PACIENTE`.
- Seção principal `15. AUDITORIA` — `usuario_id/acao/entidade/dados_anteriores/dados_novos`, já
  bastante alinhada ao que o novo documento pede.
- Apêndice, domínios `#20 (parte) Identidade e Acesso` e `#18 (parte) Auditoria` — esboço curto
  (`Usuario, Perfil, Papel, Permissao`, escopo por Unidade/Setor), que o novo documento-fonte
  aprofunda diretamente.

O documento-fonte não contradiz nada já decidido — generaliza princípios que o projeto já usa em
outros contextos:
- Separar `Usuario` de `Profissional`/`Paciente` é o mesmo raciocínio de bounded context da
  ADR-0034 (Administrativo↔RH sem duplicar `Profissional`) e da ADR-0039 decisão 5 (sem `Pessoa`
  compartilhada entre `Profissional` e `Paciente`).
- Catálogo de permissões como dado (não enum Java) é o mesmo padrão da `CapacidadeAdministrativa`
  (ADR-0032).
- Escopo por unidade organizacional reaproveita a hierarquia de 5 níveis já implementada
  (`UnidadeDeSaude`, ADR-0002/0009/0013) e `Setor` (ADR-0030) — não propõe nenhuma hierarquia nova.

Por ADR-0039 decisão 8, uma ADR só se justifica quando há decisão real com trade-off — não bastaria
"isso existirá algum dia". Aqui há: o usuário está decidindo trocar 4 roles fixas por
Papel/Permissão/Escopo orientado a dado. Por isso esta é uma ADR que **substitui a ADR-0006**
(preservando seu conteúdo histórico), não apenas mais um esboço no apêndice do DER.

## Decisão

Adota o modelo de 6 pontos do documento-fonte como forma para quando a onda de Identidade/
Autorização/Auditoria (domínios #20 e #18-parte do `MAPA-DE-DOMINIOS.md`) for implementada:

1. **`Usuario` é identidade, separada de `Profissional` (RH) e de `Paciente` (Assistência)**.
   Vínculo fraco opcional por CPF/uuid quando existir (mesmo mecanismo de reconciliação da
   ADR-0014), nunca fusão de entidade. Uma conta técnica pode existir sem representar nenhum
   profissional; um `Profissional` do RH pode nunca ganhar acesso ao sistema.
2. **RBAC em camada de dado**: `Papel` agrupa `Permissao` no formato `RECURSO.ACAO` (ex.:
   `PRONTUARIO.CONSULTAR`, `FARMACIA.DISPENSAR`, `ADMINISTRATIVO.SETOR.EDITAR`) — catálogo em
   tabela, mesmo padrão da ADR-0032: uma permissão nova é uma linha de dado, não uma alteração de
   enum/redeploy. Substitui as 4 roles fixas hardcoded que a ADR-0006 propunha.
3. **Escopo reaproveita a hierarquia organizacional já existente** — `UnidadeDeSaude` (5 níveis) e
   `Setor`, sem hierarquia de escopo nova. `AtribuicaoAcesso` liga `Usuario + Papel + Escopo`, com
   período de validade opcional (`inicio`/`fim` nullable, para acesso temporário — ex.: plantonista
   em um posto por um mês). Escopo em um nível mais alto (ex.: Regional) implica acesso às unidades
   abaixo dele na mesma hierarquia, sem reatribuir uma a uma.
4. **`Cargo` (RH) ≠ `Papel` (Segurança)**, explicitamente — mesma disciplina de bounded context já
   praticada pela ADR-0034. Um cargo de Enfermeiro no RH não implica automaticamente o papel
   `ENFERMEIRO` na autorização; conceder papel é um ato administrativo separado, e um mesmo cargo
   pode acumular mais de um papel (ex.: Enfermeiro-Coordenador ganha `ENFERMEIRO` +
   `COORDENADOR_DE_ENFERMAGEM`).
5. **Bootstrap por `AdministradorPlataforma`**: a primeira identidade do sistema nasce fora do fluxo
   normal de concessão de papel (seed de instalação), e a partir dela são criados administradores
   municipais/regionais/de unidade, descentralizando a administração do dia a dia. A regra "ninguém
   concede um papel/escopo acima do próprio nível de autoridade" fica registrada como governança
   (mesmo tratamento não-mecânico já usado pela ADR-0037 para seu critério de especialização), não
   como validação técnica nesta decisão.
6. **Auditoria** reaproveita e enriquece o esboço já existente em `DER.md` (seção 15): `EventoAuditoria`
   com usuário, ação, recurso/entidade afetada, unidade/contexto organizacional, valores
   antes/depois (quando a ação for uma alteração), data/hora.

`Administrador de plataforma ≠ acesso a dado de saúde`: um administrador técnico (cria usuário,
bloqueia conta, configura papel/permissão/unidade) não ganha automaticamente permissão para ler
prontuário/diagnóstico/prescrição só por administrar o sistema — essas são dimensões independentes
(`ADMINISTRACAO_DO_SISTEMA` vs. `ACESSO_AO_DADO_DE_SAUDE`), e a matriz de permissões deve manter essa
separação explícita quando for desenhada em detalhe.

**Nenhuma implementação começa agora.** Login, JWT, Spring Security, entidades JPA e endpoints
continuam fora de escopo — ADR-0006/ADR-0039 seguem valendo até a onda deste domínio ser priorizada
e revisitada antes de qualquer deploy com dado real de paciente. Esta ADR só fixa a forma para não
redesenhar do zero quando essa onda chegar.

## Trade-offs considerados

**RBAC + Escopo organizacional + catálogo de permissões como dado (escolhida)**
- ✅ Escopo por hierarquia evita o problema concreto que a ADR-0006 sozinha não resolveria ("gestor
  da UBS" não deveria administrar automaticamente o "Hospital Municipal" só por ter o mesmo papel).
- ✅ Catálogo como dado evita redeploy a cada permissão nova — mesmo ganho já validado pela
  ADR-0032.
- ✅ Reaproveita 100% da hierarquia organizacional e do vínculo por CPF já implementados — nenhuma
  estrutura nova de "organização" ou "pessoa" é criada.
- ❌ Mais entidades e mais trabalho de desenho que um RBAC simples — aceitável porque nenhuma delas
  é implementada agora, só desenhada.

**Manter as 4 roles fixas da ADR-0006 (rejeitada agora)**
- ✅ Mais simples de implementar quando chegar a hora.
- ❌ Não resolve escopo organizacional (papel global, sem noção de "só nesta unidade") nem permite
  granularidade por ação/recurso — ambos requisitos que o uso real do projeto já evidencia (múltiplas
  unidades, múltiplos perfis assistenciais com necessidades de acesso bem diferentes).

**Formalizar também um mecanismo técnico de "nível de autoridade" para concessão de papel (rejeitada
por ora)**
- ✅ Resolveria automaticamente "ninguém concede acima do próprio nível".
- ❌ Prematuro sem nenhuma implementação real para validar o modelo — mesmo raciocínio já usado pela
  ADR-0037 ao rejeitar um mecanismo técnico equivalente para especialização administrativa.

## Consequências

**Positivas**: quando a onda de Identidade/Autorização/Auditoria for priorizada, o desenho já está
decidido e reconciliado com tudo que o projeto já implementou (hierarquia organizacional, RH,
`CapacidadeAdministrativa`) — não precisa ser redesenhado sob pressão. `ADR-0006` fica preservada
como histórico, não apagada.

**Negativas / pendências**:
- O documento-fonte deixa uma decisão explicitamente para uma etapa futura, e esta ADR **não a
  resolve agora**: se o acesso a dado clínico (ex.: prontuário) será só por papel/escopo, ou também
  por regra contextual (ex.: só acessar um paciente havendo atendimento/vínculo assistencial ativo).
  Fica registrado como pendência aberta.
- A matriz de permissões por papel (seções 10–18 do documento-fonte) é rica o suficiente para
  orientar a implementação futura, mas não foi transcrita entidade por entidade nesta ADR — vive no
  documento-fonte e no esboço expandido de `DER.md`, a ser refinada quando o domínio for de fato
  desenhado.
- Mecanismo técnico de "nível de autoridade" para concessão de papel/escopo continua em aberto,
  registrado apenas como regra de governança.

## Referências

- [`docs/pm/sistema_de_acesso.md`](../pm/sistema_de_acesso.md) — documento-fonte.
- [ADR-0006](./0006-seguranca-jwt.md) — substituída por esta ADR; mecanismo de autenticação
  (JWT/Spring Security) permanece candidato técnico válido, o que muda é o modelo de autorização.
- [ADR-0002](./0002-modelar-hierarquia-como-entidade-unica-autorreferenciada.md),
  [ADR-0009](./0009-renomear-hierarquia-para-esferas-de-gestao-do-sus.md),
  [ADR-0013](./0013-implementar-5-nivel-unidade-de-saude-e-supervisao-regional.md) — hierarquia
  organizacional reaproveitada como escopo de acesso.
- [ADR-0014](./0014-modulo-profissional-rh-com-vinculo-fraco-por-reconciliacao.md) — padrão de
  vínculo fraco por CPF, reaproveitado para `Usuario ↔ Profissional`.
- [ADR-0030](./0030-setor-administrativo-e-relacao-com-unidade-de-saude.md) — `Setor`, reaproveitado
  como nível de escopo mais granular.
- [ADR-0032](./0032-catalogo-de-capacidades-administrativas.md) — padrão de catálogo-como-dado,
  precedente direto de `Papel`/`Permissao`.
- [ADR-0034](./0034-integracao-administrativo-rh-sem-duplicar-profissional.md) — bounded context
  entre módulos, precedente de `Usuario` separado de `Profissional`/`Paciente` e de `Cargo` ≠
  `Papel`.
- [ADR-0037](./0037-criterio-para-especializacao-administrativa.md) — precedente de regra de
  governança registrada sem mecanismo técnico.
- [ADR-0039](./0039-mapa-de-dominios-e-prioridades-de-arquitetura.md) decisões 2, 5 e 8 — segurança
  adiada conscientemente, sem `Pessoa` compartilhada, domínio ganha esboço antes de ADR própria.
- [`DER.md`](./DER.md) — seções `USUARIO`/`AUDITORIA` e apêndice `Identidade e Acesso`/`Auditoria`,
  expandidos como parte desta mesma entrega.
