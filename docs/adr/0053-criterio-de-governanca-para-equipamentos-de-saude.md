# 0053 — Critério de governança para equipamentos de saúde (evitar lista plana em `TipoUnidadeDeSaude`)

## Status

Proposta.

## Contexto

O usuário trouxe [`docs/pm/sistema_de_saude_brasileiro.md`](../pm/sistema_de_saude_brasileiro.md),
um levantamento de ~30 tipos de estabelecimento/serviço de saúde brasileiro com base no CNES (UBS,
Hospital, UPA, CAPS, SAMU, Central de Regulação, LACEN, Hemocentro, Telessaúde, unidades móveis,
estruturas de Saúde Indígena, entre outros), com o pedido explícito de "documentar adequadamente,
criar ADRs e DERs se precisar, seguir isto 100%". Recebe o mesmo tratamento de qualquer material-
fonte trazido ao projeto (`MAPA-DE-DOMINIOS.md` §1): é checklist de vocabulário e estrutura, não
especificação literal a adotar em bloco.

O próprio documento-fonte identifica o risco central: tratar esses ~30 equipamentos como uma lista
plana e equivalente (`TipoUnidadeDeSaude = UBS | UPA | HOSPITAL | LABORATORIO | CAPS | ...`) esconde
diferenças reais de natureza, função e capacidade, e propõe separar `TipoEstabelecimento` /
`TipoServico` / `PerfilAssistencial` / `Capacidade` / `Modalidade`.

Investigação do código confirma que esse risco não é hipotético — já começou a acontecer:
`TipoUnidadeDeSaude` cresceu duas vezes:

- **ADR-0013**: criação do 5º nível hierárquico ("Unidade de Saúde"), com `UBS` e `HOSPITAL` como
  primeiros valores, resolvendo o mecanismo `UnidadeSaudeService.getTiposAceitos()` (lista de tipos
  aceitos por um controller, reaproveitável sem novo controller/service).
- **ADR-0031**: extensão do mesmo 5º nível com `UPA, LABORATORIO, CAPS, CENTRO_ESPECIALIDADES,
  CENTRO_REABILITACAO, POLICLINICA`, para que cada um pudesse ter um `PerfilAdministrativo`.

Hoje `TipoUnidadeDeSaude` (`@Enumerated(EnumType.STRING)`) tem 12 valores: `FEDERAL, ESTADUAL,
MUNICIPAL, REGIONAL, UBS, HOSPITAL, UPA, LABORATORIO, CAPS, CENTRO_ESPECIALIDADES,
CENTRO_REABILITACAO, POLICLINICA`. O levantamento do usuário cobre ~18 equipamentos adicionais
(SAMU, Central de Regulação, LACEN, Hemocentro, Maternidade/CPN, Hospital Especializado, SAD,
DSEI/Polo Base/UBSI/CASAI, Telessaúde, Unidades móveis, Central de Abastecimento, Central de
Transplantes, Serviço de Verificação de Óbito, Academia da Saúde, Consultório na Rua, CEREST, UVZ,
Oficina Ortopédica) — se cada um virasse um novo valor de enum pelo mesmo caminho de ADR-0013/0031,
o projeto chegaria exatamente na "lista plana com 30 subclasses equivalentes" que o próprio
documento-fonte adverte, e que a **ADR-0037** já proíbe em espírito para o núcleo Administrativo
(critério de 4 pontos para decidir "capacidade (dado) vs. especialização (domínio novo)", com a
regra de nunca inserir condicional por tipo dentro do núcleo).

Por **ADR-0039 decisão 8**, um domínio ainda não desenhado ganha esboço descritivo (DER.md), nunca
uma ADR própria — uma ADR registra uma decisão com trade-off real, não "isso existirá algum dia".
Isso decide o formato desta entrega: **uma única ADR de governança**, não uma ADR por equipamento.

## Decisão

Generaliza o critério de 4 pontos da ADR-0037 (hoje restrito ao núcleo Administrativo) para toda a
plataforma, especificamente para decidir o que fazer com um novo equipamento de saúde do
levantamento:

1. **Se a diferença entre equipamentos é "tem esta capacidade/processo ou não" (dado)**, ela não
   vira valor de `TipoUnidadeDeSaude`. Vira entrada em um catálogo de capacidades — reaproveitando
   o padrão já usado por `CapacidadeAdministrativa` (ADR-0032: catálogo como dado + tabela de
   associação por tipo, não enum Java). Quando um domínio assistencial precisar disso de verdade
   (ex.: "realiza pequena cirurgia", "tem leito de UTI"), o caminho é um catálogo próprio no
   contexto certo (ex.: `CapacidadeAssistencial`, respeitando fronteira de bounded-context — mesmo
   raciocínio da ADR-0034 de não acoplar contextos), não a reutilização direta da entidade
   administrativa. **Não criado agora** — nenhum domínio assistencial tem requisito real ainda.
2. **Se a diferença exige comportamento de negócio novo** — novas entidades com ciclo de vida
   próprio, novas regras (ex.: Leito com estados, Internação com admissão/alta, SAMU com
   despacho/regulação médica) — não cabe em configuração; é uma **especialização de domínio**, com
   suas próprias entidades/serviços, habilitada por uma capacidade. Ela ganha sua própria ADR **só
   quando sua onda de implementação chegar** (ADR-0039 decisão 8), nunca antecipada.
3. **`TipoUnidadeDeSaude` só cresce quando o novo valor muda comportamento estrutural que o enum já
   carrega hoje** — associação ao 5º nível hierárquico e/ou à lista de tipos aceitos por um
   controller (exatamente o que ADR-0013 e ADR-0031 resolveram). Não cresce para capturar nuance de
   classificação do CNES que não muda esse comportamento — ex.: `HOSPITAL` continua sendo o valor
   para Hospital Geral **e** Hospital Especializado **e** Maternidade/CPN (todos "hospital" do ponto
   de vista estrutural hoje); a diferença entre eles é `perfil`/especialidades, a decidir via
   critério 1 ou 2 acima quando esse domínio chegar — nunca `HOSPITAL_ESPECIALIZADO` como valor novo
   de enum.
4. **A distinção de "Natureza"** que o documento-fonte propõe (ponto assistencial / serviço de apoio
   à rede / estrutura territorial-especializada / recurso móvel) é real e preservada — mas como
   **taxonomia descritiva na documentação** (`MAPA-DE-EQUIPAMENTOS-DE-SAUDE.md`, `DER.md`) agora,
   não como campo/coluna/enum novo. Isso não é descartar o conceito como "só vocabulário": é adiar a
   decisão de como implementá-lo (campo em `UnidadeDeSaude`? entidade própria? só documentação?) até
   que um domínio precise de fato consultar/filtrar por natureza — mesma disciplina do critério 2.
5. **Nenhuma especialização deve exigir condicional por tipo dentro do núcleo** (`UnidadeDeSaude`/
   `TipoUnidadeDeSaude`) — vive ao lado, referenciando o núcleo, nunca dentro dele. Generalização
   direta do 4º ponto da ADR-0037 para além do contexto Administrativo.
6. O levantamento completo dos ~30 equipamentos, com a correspondência de cada um contra o estado
   real do código, fica registrado em
   [`MAPA-DE-EQUIPAMENTOS-DE-SAUDE.md`](../MAPA-DE-EQUIPAMENTOS-DE-SAUDE.md) — não perdido, apenas
   não implementado.

## Trade-offs considerados

**Critério registrado como ADR de governança, sem mecanismo técnico novo (escolhida)**
- ✅ Resolve "novo equipamento vira enum, capacidade ou especialização?" sem inventar abstração
  nova — reaproveita o que ADR-0032/0037 já validaram no Administrativo.
- ✅ Uma ADR só, coerente com ADR-0039 decisão 8 (não criar ADR por domínio ainda não desenhado).
- ❌ É julgamento humano aplicado caso a caso, não uma validação automática — mesma limitação já
  aceita pela ADR-0037.

**Redesenhar `TipoUnidadeDeSaude` agora em `TipoEstabelecimento`/`TipoServico`/`Modalidade` reais
(rejeitada)**
- ✅ Seguiria o documento-fonte ao pé da letra.
- ❌ Nenhum domínio assistencial que precise dessa distinção foi implementado ainda — seria
  redesenhar um enum usado hoje por 4 controllers de hierarquia (ADR-0002/0009/0013) e por
  `PerfilAdministrativo` (ADR-0031) sem um requisito concreto puxando a mudança. Contraria a mesma
  diretriz "só quando houver requisito real" já aplicada ao Administrativo (ADR-0037) e ao mapa de
  domínios inteiro (ADR-0039 decisão 7).

**Adicionar os ~18 equipamentos novos como valores de `TipoUnidadeDeSaude` agora, "para não deixar
faltando" (rejeitada)**
- ✅ Deixaria o enum "completo" em relação ao documento-fonte.
- ❌ É exatamente o anti-padrão que o documento-fonte e a ADR-0037 advertem — infla o núcleo com
  valores sem comportamento associado (nenhum controller/perfil os usaria), e cada um precisaria ser
  desfeito/ajustado quando o domínio real (SAMU, Regulação, etc.) finalmente chegar e revelar que a
  diferença era capacidade ou especialização, não tipo estrutural.

## Consequências

**Positivas**: critério único e reaproveitável para toda decisão futura de "isso é um
`TipoUnidadeDeSaude` novo, uma capacidade ou uma especialização?" — evita repetir a discussão a cada
domínio (Laboratório, Regulação, Leitos, Transporte, e os novos identificados: SAMU, Hemoterapia,
Saúde Indígena, Telessaúde etc.). A distinção de Natureza do documento-fonte fica preservada e
rastreável, não descartada. `TipoUnidadeDeSaude` para de crescer por classificação, só por mudança
estrutural real.

**Negativas / pendências**: `MAPA-DE-EQUIPAMENTOS-DE-SAUDE.md` precisa ser mantido conforme cada
onda de implementação avança (situação de cada equipamento muda de `Não implementado` para
`Implementado`/`Base existente`). Um futuro catálogo `CapacidadeAssistencial` (critério 1) ainda não
tem contexto/bounded-context definido — só quando o primeiro domínio assistencial precisar dele.
`DER-atual.md` foi corrigido (estava desatualizado, descrevendo só 6 valores do enum) e `DER.md`
ganhou notas/esboços para os equipamentos novos, como parte desta mesma entrega.

## Referências

- [`docs/pm/sistema_de_saude_brasileiro.md`](../pm/sistema_de_saude_brasileiro.md) — documento-fonte
  que motivou esta ADR.
- [`MAPA-DE-EQUIPAMENTOS-DE-SAUDE.md`](../MAPA-DE-EQUIPAMENTOS-DE-SAUDE.md) — levantamento completo
  dos ~30 equipamentos, correspondência contra o código real.
- [ADR-0013](./0013-implementar-5-nivel-unidade-de-saude-e-supervisao-regional.md) e
  [ADR-0031](./0031-perfil-administrativo-por-tipo-de-unidade.md) — as duas extensões reais de
  `TipoUnidadeDeSaude` até hoje, precedente do critério 3.
- [ADR-0032](./0032-catalogo-de-capacidades-administrativas.md) — padrão de catálogo-como-dado,
  precedente do critério 1.
- [ADR-0037](./0037-criterio-para-especializacao-administrativa.md) — critério de 4 pontos original
  (escopo Administrativo), generalizado por esta ADR para toda a plataforma.
- [ADR-0039](./0039-mapa-de-dominios-e-prioridades-de-arquitetura.md) decisão 8 — domínio não
  desenhado ganha esboço em DER.md, nunca ADR própria.
- [`MAPA-DE-DOMINIOS.md`](../MAPA-DE-DOMINIOS.md) §1 — tratamento de material-fonte externo como
  checklist de vocabulário, não spec literal.
