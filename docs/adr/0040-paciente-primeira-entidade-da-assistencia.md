# 0040 — Paciente: primeira entidade da onda Assistência

## Status

Aceita e implementada (Fase 1 da onda Operação Assistencial).

## Contexto

A ADR-0039 e o [`MAPA-DE-DOMINIOS.md`](../MAPA-DE-DOMINIOS.md) definiram a ordem recomendada da
próxima onda: **Paciente → Atendimento → Agendamento → Prontuário (agregação)**. O modelo de
`Paciente` já tinha sido esboçado campo a campo em [`DER.md`](./DER.md), seção "Modelo revisado
para a próxima onda" — este ADR registra a decisão de implementá-lo como a primeira fatia real de
código dessa onda, e as poucas escolhas de implementação que o esboço do DER.md ainda deixava em
aberto.

Antes desta ADR, nenhum domínio de Assistência existia em nenhuma forma no código — `Paciente` é a
primeira entidade fora de Organização/RH/Administrativo.

## Decisão

- Nova entidade `Paciente`: `uuid`, `nome`, `cpf` (não único, mesmo motivo do `Profissional` —
  ADR-0017), `cartaoSus` (opcional, sem uniqueness — CNS é um identificador externo, não gerado
  pelo sistema), `dataNascimento`, `sexo` (novo enum `Sexo`: `MASCULINO, FEMININO, IGNORADO`),
  `endereco` (`@Embeddable Endereco`, reaproveitando a mesma classe já usada por
  `Profissional`/`UnidadeDeSaude`), `telefones` (`@ElementCollection`, mesmo padrão do
  `Profissional`), `email` (opcional — diferente do `Profissional`, cujo e-mail é contato de
  trabalho obrigatório, nem todo cidadão atendido pela rede pública tem e-mail), `ativo`.
- **Sem geração de matrícula.** Ao contrário do `Profissional` (ADR-0017), o `cartaoSus` já é um
  identificador externo pré-existente (emitido pelo SUS) — não há nada para o sistema gerar. Outros
  domínios ainda referenciarão `Paciente` pelo `uuid`, não pelo CNS (ver DER.md, seção
  `Atendimento`).
- **CRUD no formato Setor, não Profissional.** Create/atualizar substituem os campos editáveis por
  inteiro (`PacienteRequestDto` único, reaproveitado em ambos), incluindo `ativo` — sem endpoint
  dedicado de desabilitar/reabilitar com data de desligamento, porque paciente não tem vínculo
  empregatício (esse padrão é específico do `Profissional`, ver ADR-0017). "Inativar" é só
  `ativo=false` numa atualização normal, mesmo contrato do `Setor` (ADR-0030).
- Busca por `uuid` (chave interna), por `cpf` (lista — não único) e por `cartaoSus` (lista — sem
  uniqueness). Não há busca por matrícula/equivalente, porque `Paciente` ainda não tem essa chave.
- Sem entidade `Pessoa` compartilhada com `Profissional` (reafirma ADR-0039 decisão 5) e sem FK de
  nenhum outro domínio ainda — `Paciente` é standalone até `Atendimento` ser implementado.

## Trade-offs considerados

**`cartaoSus` como identificador único, gerando conflito em duplicidade (rejeitada)**
- ✅ Refletiria a realidade: CNS é teoricamente único por cidadão.
- ❌ Dado real de CNS é frequentemente ausente/duplicado/incorreto em sistemas de saúde pública
  (cadastros feitos sem o cartão em mãos, erro de digitação); marcar como `unique` geraria
  `DataIntegrityViolationException` em cenários legítimos (ex.: recém-nascido sem CNS emitido
  ainda) sem nenhum requisito concreto pedindo essa trava agora.

**Endpoint de desabilitar com data de desligamento, espelhando o `Profissional` (rejeitada)**
- ✅ Manteria simetria total com o padrão mais elaborado já existente.
- ❌ Paciente não tem "vínculo" que se encerra numa data — a analogia não se sustenta; o padrão mais
  simples do `Setor` (ativo como campo comum de update) já resolve sem inventar um conceito de
  "desligamento" que não existe no domínio.

## Consequências

**Positivas**: abre caminho para `Atendimento` referenciar um `Paciente` real por `uuid`; a onda
Assistência deixa de ser "folha em branco" no código.

**Negativas / pendências**: `Paciente` ainda não é referenciado por nenhuma outra entidade — fica
órfão até `Atendimento` (próxima fatia da mesma onda) ser implementado. Sem validação de CPF/CNS
(formato, dígito verificador) — mesma disciplina de "não antecipar validação sem requisito real" já
usada pelo restante do projeto.

## Referências

- [`MAPA-DE-DOMINIOS.md`](../MAPA-DE-DOMINIOS.md) e [ADR-0039](./0039-mapa-de-dominios-e-prioridades-de-arquitetura.md)
  — roadmap e decisões guarda-chuva que esta ADR implementa.
- [`DER.md`](./DER.md), seção "Modelo revisado para a próxima onda" — esboço de campos original.
- [ADR-0017](./0017-numero-de-matricula-automatico-e-cpf-nao-unico.md) — precedente de CPF não-único
  e motivo pelo qual `Paciente` não precisa de um equivalente gerado à matrícula.
- [ADR-0030](./0030-setor-administrativo-e-relacao-com-unidade-de-saude.md) — precedente do formato
  de CRUD (`ativo` como campo comum de update, sem endpoint dedicado).
