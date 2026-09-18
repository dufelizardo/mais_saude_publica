# 0017 — Número de matrícula automático e CPF deixa de ser único em Profissional

## Status

Aceita e implementada.

## Contexto

O endpoint de desabilitar Profissional (`DELETE /api/v1/profissional/des-habilitar/{cpf}`) só
alternava `ativo`, sem nunca preencher `dataDesligamento` — campo que já existia na entidade e no
`ProfissionalResponseDto` desde a ADR-0014, mas ficou morto (nada escrevia nele). Ao planejar ligar
esse campo, surgiu uma pergunta de negócio mais profunda: o que acontece quando um profissional
desligado é recontratado depois?

A resposta do usuário: recontratação cria **uma ficha nova, com uma matrícula nova** — não reabre a
ficha antiga. Isso implica que a mesma pessoa (mesmo CPF) pode ter mais de um registro
`Profissional` ao longo do tempo (um por vínculo empregatício), o que **quebra a constraint de CPF
único** que existia desde a ADR-0014.

## Decisão

- **Novo campo `matricula`** em `Profissional`: gerado automaticamente no `create()`, nunca
  informado pelo cliente. Formato: 14 dígitos + hífen + 2 dígitos verificadores (ex.:
  `11111111111111-11`) — os 2 dígitos são a soma dos 14 primeiros, módulo 100, não um algoritmo de
  verificação criptográfico de verdade, só um padrão reconhecível de "número de documento com
  dígito verificador", suficiente pro contexto (não há orgão regulador validando isso de fora).
  `@Column(unique = true)` — **esta** é a chave única de verdade do domínio agora, não mais o CPF.
- **CPF deixa de ser único** (`@Column(unique = true)` removido de `Profissional.cpf`) — pode se
  repetir entre fichas históricas da mesma pessoa.
- **Toda consulta "por CPF" passa a significar "a ficha ATIVA com este CPF"**:
  `ProfissionalRepository.findByCpf` → `findByCpfAndAtivoTrue`. Isso vale pra busca
  (`GET /{cpf}`), atualização de contato, desabilitar, e a reconciliação por CPF da ADR-0014 (uma
  `UnidadeDeSaude` nunca deveria linkar seu responsável a uma ficha de alguém já desligado).
- **`dataDesligamento` agora é escrito de verdade**: `DELETE des-habilitar/{cpf}` ganha um novo
  parâmetro opcional `dataDesligamento` (mesma técnica de binding via query param que o resto do
  des-habilitar já usa, sem `@RequestBody` — ver comentário histórico no `pom` do Robot). Ao
  desabilitar, grava a data informada. Ao reabilitar, **limpa** `dataDesligamento` — decisão
  explícita do usuário: reabilitar não devia carregar uma data de saída antiga junto.
  **Refinamento**: o parâmetro `ativo` foi removido do contrato — `dataDesligamento` sozinha decide
  o status (informada → desabilita e grava a data; ausente → reabilita e limpa a data), já que o
  par `ativo`+`dataDesligamento` era redundante (a intenção já está implícita em ter ou não uma
  data).
- **Colisão de matrícula**: com 14 dígitos aleatórios (10¹⁴ combinações), a chance de colisão é
  desprezível mesmo em milhões de registros, mas `create()` ainda tem um retry limitado (até 5
  tentativas) gerando uma matrícula nova se o `save()` falhar por violação de unicidade — defesa
  barata contra o caso extremo, sem exigir infraestrutura nova (sequence dedicada, etc.).

### Migração de schema — atenção, `ddl-auto=update` não remove a constraint antiga sozinho

Diferente de toda mudança de schema anterior deste projeto (só aditivas — novas colunas/tabelas,
nunca removendo nada), esta decisão **remove** uma constraint (`UNIQUE (cpf)`) que já existe fisicamente
no banco de todo ambiente que já rodou a ADR-0014 (dev/qaa/homologacao/prod). O Hibernate, em modo
`update`, só adiciona o que falta — **não** derruba constraints existentes só porque a anotação Java
mudou. Ou seja: sem ação manual, o banco continuaria rejeitando CPF duplicado mesmo depois deste
deploy, mascarando silenciosamente esta decisão.

**Ação manual necessária em cada ambiente**, antes ou logo depois do deploy:
```sql
ALTER TABLE tb_profissional DROP CONSTRAINT IF EXISTS uk_<nome_gerado_pelo_hibernate_para_cpf>;
```
O nome exato da constraint varia (Hibernate gera algo como `uk6i1u8p... ` baseado num hash) — descobrir
com `\d tb_profissional` no `psql` de cada ambiente antes de rodar o `ALTER TABLE`. Hibernate cria a
constraint nova de `matricula` sozinho no próximo boot (coluna nova, ddl-auto=update cobre isso
normalmente).

## Trade-offs considerados

**Matrícula como chave única + CPF não-único (escolhida)**
- ✅ Reflete a regra de negócio real: uma pessoa pode ter múltiplos vínculos empregatícios ao longo
  do tempo, cada um com sua própria matrícula.
- ✅ Preserva histórico — a ficha antiga (desligada) continua no banco, consultável, em vez de ser
  sobrescrita por uma reabilitação.
- ❌ Exige migração manual de schema (remover a constraint antiga) — a primeira vez que este
  projeto precisa disso, já que até aqui `ddl-auto=update` sempre bastou sozinho.
- ❌ "Buscar por CPF" agora é implicitamente "buscar a ficha ativa" — um pouco menos óbvio pra quem
  lê o código pela primeira vez; documentado no Javadoc do repositório.

**Reabilitar a ficha antiga em vez de criar uma nova (rejeitada)**
- ✅ Não precisaria mexer na unicidade do CPF nem migrar schema.
- ❌ Rejeitada a pedido explícito do usuário: cada vínculo empregatício deveria ter sua própria
  matrícula, não reaproveitar a antiga.

**Matrícula com dígito verificador "de verdade" (algoritmo tipo CPF/CNPJ) (rejeitada por ora)**
- ✅ Mais rigoroso, mais próximo de um padrão de documento oficial.
- ❌ Rejeitada por simplicidade: nenhum órgão externo valida esse número hoje, então o ganho não
  compensa a complexidade extra — soma módulo 100 já dá uma verificação básica de digitação.

## Consequências

**Positivas**
- `dataDesligamento` deixa de ser campo morto — `des-habilitar` agora registra quando e (via
  histórico de fichas) quantas vezes um profissional já foi desligado/recontratado.
- Matrícula gerada automaticamente remove a necessidade de o cliente (frontend ou integração
  externa) inventar/coordenar esse número.

**Negativas / pendências**
- Migração manual pendente em produção (Render) e nos 4 ambientes do home-lab — precisa ser feita
  antes que alguém tente cadastrar um CPF que já existe numa ficha desligada, ou o `ALTER TABLE`
  antigo vai continuar rejeitando com 409 mesmo depois deste deploy.
- Não existe endpoint de "listar todas as fichas históricas de um CPF" — só a ativa é alcançável via
  API hoje; se precisar consultar o histórico completo de alguém no futuro, precisa de um novo
  endpoint (`GET /profissional/historico/{cpf}` ou similar), fora do escopo desta ADR.
