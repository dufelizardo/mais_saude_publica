# 0195 — Comunicação e Educação em Saúde: educação em saúde na ação coletiva (C3)

## Status

Aceita e implementada. Fatia **C3** do domínio 28 ([ADR-0114](./0114-comunicacao-e-educacao-em-saude.md); detalhe em
[`comunicacao/MODELO-COMUNICACAO.md`](../comunicacao/MODELO-COMUNICACAO.md), seção 11).

## Contexto

- **A ADR-0114 decidiu que a ação educativa comunitária não ganha motor próprio:** é a ação programática coletiva de Programas (P4,
  [ADR-0132](./0132-programas-acoes-programaticas.md)). Este domínio fornece o conteúdo, o material e a avaliação.
- **Já existiam:**
  - a ação coletiva, com unidade, equipe, local, participantes e o público alcançado;
  - o conteúdo educativo com revisão técnica e o material educativo (C1, [ADR-0168](./0168-comunicacao-basica.md));
  - a campanha de comunicação (C2, [ADR-0174](./0174-campanhas-de-comunicacao.md));
  - a ligação de conteúdos pelo grupo de versões, na educação permanente (C4, [ADR-0173](./0173-educacao-permanente.md)).
- **O precedente:** a saúde bucal coletiva ([ADR-0140](./0140-saude-bucal-coletiva.md)) estende a mesma ação com um detalhe próprio.

## Decisão

1. **O detalhe educativo da ação coletiva** (`TB_ACAO_EDUCATIVA`, um por ação):
   - **Quais ações:** as coletivas (individual, 422) de grupo, orientação, mobilização ou outra (os demais tipos, 422), previstas ou
     realizadas (não realizada e cancelada, 422).
   - **O que guarda:**
     - o tema, obrigatório (400), que agrupa os relatórios;
     - os conteúdos educativos pelo grupo de versões (`TB_ACAO_EDUCATIVA_CONTEUDO`): só os que passaram pela revisão técnica (422); a
       versão em vigor aparece, e a vencida fica marcada;
     - os materiais distribuídos com a quantidade (`TB_ACAO_EDUCATIVA_MATERIAL`): quantidade acima de zero (400), material repetido
       (400), retirado do catálogo (422);
     - as microáreas alvo (`TB_ACAO_EDUCATIVA_MICROAREA`), para a C6 medir a cobertura;
     - a campanha de comunicação, planejada ou em andamento (encerrada ou cancelada, 422).
   - **Salvar substitui as ligações.**
2. **O público:**
   - na ação educativa, os participantes não precisam ser acompanhados no programa: a lista nominal continua opcional;
   - o público alcançado é a quantidade informada na realização ou, sem ela, os participantes.
3. **A avaliação da equipe:**
   - fica na ação realizada (422) que já tem o detalhe educativo (422);
   - registra a participação (baixa, média ou alta; 400 sem ela) e um texto;
   - não há questionário nem pesquisa de satisfação.
4. **Onde aparece:**
   - **na campanha de comunicação:** as ações educativas ligadas, com a data, o local, o público, os materiais e a participação;
   - **nos conteúdos:** o uso em ações educativas e as pessoas alcançadas, ao lado do uso em treinamentos (C4).
5. **Acesso:** sem permissão nova.
   - **Quem preenche e avalia:** quem acompanha o programa (`PROGRAMA.ACOMPANHAR`), na unidade da ação; o mesmo papel lê.
   - **Conteúdo e material:** são escolhidos entre os revisados, sem precisar de `CONTEUDO.EDITAR`.
6. **Telas:**
   - **Programas,** ações programáticas: "Educação em saúde" na ação coletiva, com o tema, os conteúdos revisados, a quantidade de cada
     material, as microáreas, a campanha e, na realizada, a avaliação.
   - **Comunicação:**
     - no detalhe da campanha, as ações educativas;
     - nos conteúdos, o uso em ações educativas.

### API

| Método | Caminho | Permissão |
|---|---|---|
| GET, PUT | `acao-programatica/{id}/educacao` (`tema`, `conteudoGrupoIds`, `materiais`, `microareaIds`, `campanhaComunicacaoId`) | `PROGRAMA.ACOMPANHAR` |
| POST | `acao-programatica/{id}/educacao/avaliacao` (`participacao`, `avaliacao`) | idem |

Mudam também:
- `GET comunicacao/campanha` passa a trazer `acoesEducativas`;
- `GET educacao-permanente/uso-dos-conteudos` passa a trazer `acoesEducativas` e `pessoasAlcancadas`.

### Migração

`V79__educacao_em_saude.sql`: `tb_acao_educativa` e as três tabelas de ligação.

## Alternativas descartadas

- **Motor próprio de ação educativa:** duplicaria a ação, a equipe e os participantes de Programas (ADR-0114).
- **A avaliação dentro da realização da ação:** a realização é de Programas; a avaliação educativa é deste domínio, num detalhe próprio
  como o da saúde bucal.
- **Questionário de conhecimento antes e depois:** pede instrumento validado e plataforma; fica fora.

## Consequências

- **STATUS-DOS-DOMINIOS:** a C3 sai das pendências do domínio 28; seguem a C6 (avaliação) e a C5 (integrações).
- **Implantação:** cadastrar e revisar os conteúdos e os materiais na Comunicação para as equipes usarem.
- **Fora do escopo:** o envio do material pelos canais (C5, Integrações); a EAD.

## Testes

- **JUnit `EducacaoEmSaudeControllerTest`:**
  - sem tema (400), conteúdo em rascunho (422), material retirado (422), repetido (400) ou com quantidade zero (400);
  - campanha encerrada (422), ação de vacina (422);
  - o detalhe com o conteúdo vencido marcado, o material, a microárea e a campanha;
  - a avaliação antes de realizar (422), sem participação (400) e depois, com o público;
  - a campanha com a ação, o uso dos conteúdos e a ação cancelada (422).
- **JUnit `EducacaoEmSaudeAutorizacaoControllerTest`,** com a segurança ligada: o enfermeiro preenche e lê na sua unidade e não noutra;
  a recepção não preenche nem lê.
- **Robot:** `EducacaoEmSaude.robot` (API, CT-001) e CT-022 de `UI_programas.robot`.
