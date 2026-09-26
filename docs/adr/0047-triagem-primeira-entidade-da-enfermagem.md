# 0047 — Triagem: primeira entidade do domínio Enfermagem

## Status

Aceita e implementada.

## Contexto

Com a onda Operação Assistencial (`Paciente`, `Atendimento`, `Agendamento`, `Consulta`,
`Procedimento`, `Prontuário` — ADR-0040 a 0045) completa e em produção, o `MAPA-DE-DOMINIOS.md`
lista os próximos candidatos "conforme requisito real": #8 Enfermagem, #9 Farmácia, #10
Laboratório, #11 Regulação, #12 Leitos, #16 Transporte. Enfermagem é o primeiro da lista e o mais
diretamente conectado ao que já existe: toda unidade que atende paciente faz algum tipo de
triagem/acolhimento antes ou durante o atendimento médico.

O esboço original do domínio (`DER.md`, apêndice "Enfermagem (#8)", nunca reconciliado com o código
real) descreve:

```text
Paciente → Triagem (sinais vitais) → Classificação de risco → Atendimento médico/enfermagem
```

Esse fluxo antecede o `Atendimento` — mas o modelo já implementado (ADR-0041) trata `Atendimento`
como o registro de entrada do paciente na rede, e `Consulta` (ADR-0043) já se apoia nele como FK
obrigatória. Manter `Triagem` solta, sem vínculo com `Atendimento`, duplicaria o conceito de
"entrada do paciente" e deixaria o Prontuário (ADR-0045) sem como agregá-la de forma consistente
com o resto da árvore Atendimento → Consulta → Procedimento.

## Decisão

- **`Triagem` é filha de `Atendimento`**, não o antecede — mesmo papel estrutural de `Consulta`
  (FK obrigatória a `Atendimento`, resolvida por uuid). Na prática de acolhimento do SUS, a
  triagem acontece logo após o paciente ser registrado no atendimento e antes (ou em paralelo) da
  consulta médica — o comportamento observável é o mesmo, só o desenho original invertia a
  dependência estrutural. Reconciliação registrada aqui, não no DER.md (que permanece só como
  registro histórico do material trazido).
- Nova entidade `Triagem`: `uuid`, `atendimento` (`@ManyToOne Atendimento`, obrigatório),
  `profissional` (`@ManyToOne Profissional`, obrigatório, FK por `matricula` resolvida na
  fronteira da API — mesmo padrão ADR-0034/ADR-0041), `dataHora`, `pressaoArterial` (`String`,
  formato livre "120/80" — é assim que se registra na prática, não dois campos numéricos
  separados), `temperatura`/`saturacaoOxigenio`/`peso` (`Double`, opcionais), `frequenciaCardiaca`
  (`Integer`, opcional), `classificacaoRisco` (novo enum `ClassificacaoRisco`, obrigatório —
  protocolo de Manchester, já consagrado no SUS: `AZUL, VERDE, AMARELO, LARANJA, VERMELHO`),
  `observacoes` (`String`, opcional).
- **`ClassificacaoRisco` fica como campo enum na própria `Triagem`**, não uma entidade própria —
  mesma disciplina YAGNI já aplicada em `Consulta.diagnostico` (ADR-0043): é o resultado da
  triagem, não um conceito com identidade e ciclo de vida próprios.
- CRUD no mesmo formato de `Consulta`: `criar`/`atualizar` substituem os campos editáveis por
  inteiro, `listar`/`buscarPorId` sem filtro.
- **Prontuário (ADR-0045) passa a agregar `Triagem`** junto de `Consulta` dentro de cada
  `ProntuarioAtendimentoDto` (`triagens: List<TriagemResponseDto>`, irmã de `consultas`) — extensão
  direta e de baixo risco da agregação já existente, sem precisar de nova decisão de design.
- Sem `criado_em`/`atualizado_em` — mesma omissão já feita em toda a onda Assistência.

## Trade-offs considerados

**Manter `Triagem` antes do `Atendimento` (como no esboço original do DER.md) — rejeitada**
- ✅ Fiel ao fluxo clínico "puro" descrito pelo usuário na visão original.
- ❌ Exigiria decidir separadamente como uma Triagem "solta" se conecta depois a um Atendimento (e
  o que acontece se nunca se conectar), duplicando a responsabilidade de "registrar a entrada do
  paciente" que `Atendimento` já cobre. Nenhum requisito concreto pede triagem sem atendimento
  associado.

**`ClassificacaoRisco` como entidade própria (catálogo) — rejeitada**
- ✅ Permitiria configurar cores/prioridades sem alterar código.
- ❌ Protocolo de Manchester é um padrão estável e conhecido, não um catálogo administrável pela
  unidade; nenhum requisito concreto pede customização.

## Consequências

**Positivas**: primeira entidade do domínio Enfermagem, conectada de forma consistente ao restante
da onda Assistência; Prontuário passa a mostrar também os dados de triagem sem precisar de uma
segunda agregação.

**Negativas / pendências**: os demais candidatos citados no DER.md para Enfermagem
(`EvolucaoDeEnfermagem`, `AdministracaoDeMedicamento`, `Cuidado`, `Escala`) continuam não
modelados — implementar apenas quando houver requisito real, mesma disciplina do restante do mapa.

## Referências

- [`DER.md`](./DER.md), apêndice "Enfermagem (#8)" — esboço original, não reconciliado até agora.
- [`MAPA-DE-DOMINIOS.md`](../MAPA-DE-DOMINIOS.md) — domínio #8, próximo passo da onda Assistência.
- [ADR-0041](./0041-atendimento-registra-entrada-do-paciente-na-rede.md) — `Atendimento`, referenciado aqui.
- [ADR-0043](./0043-consulta-registrada-durante-o-atendimento.md) — precedente estrutural direto (FK
  obrigatória a `Atendimento`, resultado como campo de texto/enum em vez de entidade própria).
- [ADR-0045](./0045-prontuario-agregacao-de-leitura.md) — agregação do Prontuário, estendida aqui.
- [ADR-0034](./0034-integracao-administrativo-rh-sem-duplicar-profissional.md) — precedente de FK
  por matrícula resolvida na fronteira da API.
