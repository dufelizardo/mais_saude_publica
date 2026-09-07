# 0009 — Renomear a hierarquia genérica para as esferas de gestão do SUS

## Status

Proposta — decisão registrada, implementação ainda não feita.

## Contexto

O domínio atual (`UnidadeDeSaude`, hierarquia autorreferenciada — ver ADR-0002) usa nomes genéricos e posicionais: 4 níveis "Zero/Um/Dois/Três" (enum `TipoUnidadeDeSaude` com `ADMINISTRACAO1..4`), sem relação explícita com a estrutura real do SUS.

Ao planejar o épico [AQUAQE-142](https://edufelizardo.atlassian.net/browse/AQUAQE-142) ("Sistema Integrado de Gestão da Saúde Pública"), confirmou-se o mapeamento real entre os níveis existentes e os conceitos de negócio:

- **Zero → Gestão Federal**
- **Um → Gestão Estadual**
- **Dois → Gestão Municipal**
- **Três → Gestão Regional da Saúde**
- **(inexistente hoje) → Gestão das Unidades de Saúde** — a unidade clínica concreta (UBS/Hospital). Os valores `UBS`(4) e `HOSPITAL`(5) já existem no enum `TipoUnidadeDeSaude`, mas **nenhum controller/service os utiliza hoje** — é código morto, nunca implementado.

Essa renomeação expõe um problema adicional: o DTO do nível **Um** (agora "Estadual") mistura os campos `estados` **e** `municipio` no mesmo objeto — mas `municipio` conceitualmente pertence ao nível **Dois** (Municipal), e o campo `regiao` que hoje está em **Dois** conceitualmente pertence a **Três** (Regional). Ou seja, os campos geográficos extras estão "descolados" um nível do mapeamento correto:

| Nível atual | Campo extra hoje | Nível correto do campo |
|---|---|---|
| Um (→ Estadual) | `estados`, `municipio` | `municipio` deveria estar em Dois (Municipal) |
| Dois (→ Municipal) | `regiao` | `regiao` deveria estar em Três (Regional) |
| Três (→ Regional) | *(nenhum)* | falta `regiao`, hoje em Dois |

## Decisão

Renomear os 4 níveis existentes e formalizar a criação do 5º nível ("Unidade de Saúde"), mantendo o modelo estrutural já decidido na ADR-0002 (entidade única `UnidadeDeSaude` autorreferenciada, discriminada por `tipo`) — esta ADR trata apenas de nomenclatura e distribuição de campos, não do modelo de dados em si.

### Tabela de renomeação

| Nível atual | Vira | Controller | Service | DTOs | Endpoint | Tag Swagger | Enum |
|---|---|---|---|---|---|---|---|
| Zero | **Federal** | `UnidadeDeSaudeFederalController` | `FederalService` | `FederalRequestDto` / `FederalResponseDto` | `/api/v1/federal/` | "Federal" | `ADMINISTRACAO1` → `FEDERAL` |
| Um | **Estadual** | `UnidadeDeSaudeEstadualController` | `EstadualService` | `EstadualRequestDto` / `EstadualResponseDto` | `/api/v1/estadual/` | "Estadual" | `ADMINISTRACAO2` → `ESTADUAL` |
| Dois | **Municipal** | `UnidadeDeSaudeMunicipalController` | `MunicipalService` | `MunicipalRequestDto` / `MunicipalResponseDto` | `/api/v1/municipal/` | "Municipal" | `ADMINISTRACAO3` → `MUNICIPAL` |
| Três | **Regional** | `UnidadeDeSaudeRegionalController` | `RegionalService` | `RegionalRequestDto` / `RegionalResponseDto` | `/api/v1/regional/` | "Regional" | `ADMINISTRACAO4` → `REGIONAL` |
| *(novo)* | **Unidade de Saúde** | `UnidadeDeSaudeUnidadeController` | `UnidadeSaudeService` | `UnidadeSaudeRequestDto` / `UnidadeSaudeResponseDto` | `/api/v1/unidade-saude/` | "Unidade de Saúde" | `UBS`(4) / `HOSPITAL`(5) — já existem, hoje sem uso |

### Redistribuição de campos geográficos

- **Estadual** (ex-Um): renomear `estados` → `estado` (singular); **remover** `municipio` deste nível.
- **Municipal** (ex-Dois): **adicionar** `municipio` (vindo de Estadual); **remover** `regiao` deste nível.
- **Regional** (ex-Três): **adicionar** `regiao` (vindo de Municipal).
- **Unidade de Saúde** (novo): vinculada via `unidadeSuperior` a uma unidade **Municipal** (não a Federal/Estadual/Regional diretamente), refletindo que a esfera municipal tem papel central na execução dos serviços de saúde.

### Sobre a base compartilhada

`AbstractHierarquicoService` e a entidade única `UnidadeDeSaude` (ADR-0002) são mantidos. O 5º nível (Unidade de Saúde) segue o mesmo padrão dos outros 4: um controller/service dedicado, filtrando por `tipo` (`UBS`/`HOSPITAL`), reaproveitando o CRUD comum já existente na classe abstrata.

## Trade-offs considerados

**Renomear direto, sem versionamento (escolhida)**
- ✅ Projeto ainda não tem consumidores em produção — não há custo real de quebrar o contrato agora.
- ✅ Evita manutenção dupla (v1 antigo + v2 novo) por um período de transição sem benefício real neste estágio do projeto.
- ❌ Qualquer cliente/script que já tenha integrado com os paths `/hierarquico-*/` quebra sem aviso prévio.

**Manter `/hierarquico-*/` (v1) e adicionar os novos paths (v2), com depreciação gradual (rejeitada)**
- ✅ Não quebra clientes existentes.
- ❌ Overhead de manter duas implementações (ou uma camada de tradução) por tempo indeterminado, para um projeto que ainda está na fase de fundação e provavelmente não tem consumidores externos reais.

## Consequências

**Positivas**
- Nomes de classes, endpoints e tags Swagger passam a refletir a estrutura real do SUS (Federal/Estadual/Municipal/Regional/Unidade de Saúde), em vez de posições genéricas (Zero/Um/Dois/Três).
- Resolve a ambiguidade de `estados`+`municipio` misturados no mesmo DTO — cada campo geográfico passa a viver no nível correto.
- Formaliza e prepara terreno para implementar de fato a "Gestão das Unidades de Saúde" (hoje código morto — `UBS`/`HOSPITAL` existem no enum mas nunca foram conectados a um controller).
- Alinha o código com as histórias já criadas no Jira ([AQUAQE-143](https://edufelizardo.atlassian.net/browse/AQUAQE-143) a [AQUAQE-147](https://edufelizardo.atlassian.net/browse/AQUAQE-147)) e com a nota de reconciliação já deixada em AQUAQE-147 / [AQUAQE-170](https://edufelizardo.atlassian.net/browse/AQUAQE-170).

**Negativas / pendências**
- É uma mudança de contrato de API (breaking change): todos os paths, exemplos de Swagger (`ExampleConstants`), testes e qualquer documentação/coleção de requisições precisam ser atualizados juntos, na mesma tarefa.
- A implementação de fato (renomear classes/pacotes, migrar os dados dos campos redistribuídos entre níveis, criar o 5º nível do zero) fica para uma tarefa/sprint separada — esta ADR só formaliza a decisão, não a executa.
- `docs/adr/DER-atual.md` (schema real hoje) ficará desatualizado assim que esta ADR for implementada, e precisará de uma revisão nessa ocasião — não nesta rodada.
