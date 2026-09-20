# 0029 — Listagem de profissionais e edição de contato

## Status

Aceita e implementada.

## Contexto

Continuando o levantamento endpoint-a-uso do módulo RH (o mesmo tipo de auditoria que abriu a
fatia 9 — ver [ADR-0028](./0028-fatia-9-beneficios.md)), desta vez focado especificamente em
`Profissional`: de 5 endpoints, 2 não tinham nenhuma tela consumindo — `GET /profissional/`
(listar todos) e `PATCH /profissional/contato/{cpf}` (atualizar e-mail/telefone/endereço). O
usuário classificou isso como gap real ("vai fazer falta no futuro"), não como "endpoint aceitável
sem tela por enquanto".

Diferente da fatia 9, aqui não havia gap de backend — os dois endpoints já suportavam a operação
completa. Faltava só a interface.

## Decisão

### Nova tela `/profissionais` — lista de todos os profissionais

Consome `GET /profissional/`. Como o endpoint não aceita filtro (só devolve a lista inteira),
busca por nome/CPF e filtro por situação (Ativo/Desligado) são client-side — mesmo padrão já usado
em `Cargos` (`cargosFiltrados` via `computed()`). Cada linha tem um link "Ver perfil" que navega
para `/profissionais/perfil?cpf=...`.

### `profissional-perfil.ts` passa a ler `?cpf=` da URL

Para o link "Ver perfil" da lista funcionar sem exigir redigitar o CPF: se a rota tiver
`?cpf=...`, o formulário de busca é pré-preenchido e a busca dispara automaticamente no
`constructor()`. Não muda o fluxo de busca manual existente — é um caminho adicional, não uma
substituição.

### Aba "Dados" ganha "Editar contato"

Botão abre um modal (mesmo componente `Modal` já usado em toda a aplicação) com e-mail, telefone e
endereço completo — incluindo autocomplete de CEP via `CepService`, replicando exatamente o
formulário já usado no cadastro (`profissional-cadastro.ts`). Ao salvar, chama
`PATCH /profissional/contato/{cpf}` e, como a resposta é só `SuccessResponseDto` (não devolve o
profissional atualizado), refaz um `GET /profissional/{cpf}` para atualizar a aba com os dados
novos — mesmo padrão de "recarregar após mutação" usado em todas as outras abas do perfil.

### Telefone: um campo só, não um editor de lista

O backend aceita `Set<String>` de telefones, mas o cadastro (`profissional-cadastro.ts`) já
simplifica isso para um único campo (`telefones: [raw.telefone]`). A edição de contato replica a
mesma simplificação em vez de introduzir um editor de lista de telefones que não existe em nenhum
outro lugar da aplicação — mantém a UI consistente com o que já existe, não resolve um problema que
não foi pedido.

## Trade-offs considerados

**Telefone único vs. editor de múltiplos telefones**
- ✅ Consistente com o cadastro — não introduz um padrão de UI novo e sem precedente.
- ❌ Se um profissional já tiver mais de um telefone (só possível hoje via dado semeado
  diretamente no banco, não via UI), editar o contato substitui por um único telefone. Aceito: a
  UI de cadastro já tem essa mesma limitação, não é uma regressão introduzida aqui.

**Recarregar via novo GET vs. backend devolver o profissional atualizado**
- ✅ Não muda o contrato do endpoint (`PATCH` continua devolvendo `SuccessResponseDto`, igual a
  todo outro endpoint de mutação do projeto).
- ❌ Uma chamada HTTP extra após salvar — aceito, é o mesmo padrão já usado em `registrarAjuste`,
  `registrarTransferencia` etc., que recarregam listas após criar.

## Consequências

**Positivas**: fecha os 2 únicos endpoints de `Profissional` sem consumo — não há mais nenhum
endpoint órfão nesse controller específico.

**Negativas / pendências**: nenhuma pendência de backend. A tela `/profissionais` ainda não entra
no agrupamento de menu planejado na fatia 8d (menu por grupos) — quando 8d for implementada, decidir
seu grupo (provavelmente "Pessoas", junto de Cadastrar/Desligar/Perfil/Folha).

## Referências

- [ADR-0028](./0028-fatia-9-beneficios.md) — mesma prática de auditoria endpoint-a-uso que
  originou esta decisão.
- [ADR-0017](./0017-numero-de-matricula-automatico-e-cpf-nao-unico.md) — contrato de
  `des-habilitar`/matrícula do `Profissional`, para contexto do controller.
