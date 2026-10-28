# 0152 — Programas: linhas de cuidado (P5)

## Status

Aceita e implementada. Última fatia do domínio 22, desenhado na [ADR-0109](./0109-programas-acoes-e-linhas-de-cuidado.md)
(detalhe em [`programas/MODELO-PROGRAMAS.md`](../programas/MODELO-PROGRAMAS.md)). Com ela, o domínio fica completo.

> O número 0151 ficou para o domínio 20, Integrações, desenhado em conversa e adiado como implementação futura.

## Contexto

- **A ADR-0109 separou a linha de cuidado do programa:** o programa organiza o acompanhamento; a linha é o
  **percurso assistencial** que atravessa domínios (da UBS ao exame, à regulação, à maternidade e de volta).
- **Ela deixou a P5 por último,** até Atendimento, Regulação e Prontuário estarem maduros. Hoje estão, e os registros que
  mostram o caminho do paciente já existem:
  - o atendimento concluído, com a especialidade;
  - o exame com o resultado liberado;
  - a solicitação da regulação, até realizada;
  - a dose de vacina;
  - a internação com a alta e o CID.
- **Faltava** juntar isso numa sequência com prazos e mostrar em que etapa cada paciente está e quem está atrasado.

## Decisão

1. **A linha de cuidado é catálogo cadastrável** (`LinhaDeCuidado`), sem lista fixa no código:
   - código único (409), nome, descrição, situação e o programa a que se liga (opcional). A linha de programa sensível é
     sensível;
   - **etapas como dado** (`EtapaLinhaCuidado`), na ordem, cada uma com:
     - **nome** e se é **obrigatória**;
     - **como se cumpre** (o tipo) e a referência do tipo;
     - **quantidade:** quantas vezes precisa acontecer ("seis consultas");
     - **prazo** em dias, contado do **início do percurso** ou do **cumprimento da etapa anterior**. A primeira etapa conta
       do início (400).
   - **ajuste:** a etapa que vem com uuid é ajustada, a nova é criada e a que não vem é **desativada**, não apagada,
     porque os percursos guardam registros dela.
2. **Como cada etapa se cumpre** (nada é preenchido em dobro):

   | Tipo | Cumpre quando o paciente tem, a partir da referência | Pede |
   |---|---|---|
   | Atendimento | atendimento concluído (da especialidade, se informada) | — |
   | Exame | o exame do catálogo com o resultado liberado | o exame (400) |
   | Regulação | a solicitação do procedimento realizada; antes disso, mostra se está solicitada, autorizada ou agendada | o procedimento (400) |
   | Vacina | a dose vigente do imunobiológico | a vacina (400) |
   | Internação | a alta, com o CID começando por um dos informados (O80, O81…) | — |
   | Manual | registro do profissional (teste rápido, teste do pezinho) | — |

3. **O percurso do paciente** (`PercursoLinhaCuidado`):
   - **abertura** por profissional (`PROGRAMA.ACOMPANHAR` na unidade), como a inscrição no programa: paciente, linha
     ativa (422), data de início (a última menstruação, o nascimento, a suspeita; não no futuro, 400), unidade, equipe
     da unidade (400) e o acompanhamento no programa da linha, quando houver (de outro paciente ou programa, 422). Um só
     percurso em curso por paciente e linha (409);
   - **situação de cada etapa calculada na hora,** como a situação vacinal:
     - **cumprida** quando chega à quantidade, com a data e o registro que a cumpriu;
     - **dispensada** pelo profissional, com o motivo;
     - **atrasada** quando passou do prazo;
     - **no prazo** antes disso, mostrando quantas já houve;
     - **aguardando a anterior** quando conta da etapa anterior e ela não foi feita;
   - **só se guardam** os registros manuais e as dispensas (`RegistroEtapaPercurso`). A etapa de domínio não se registra à
     mão (422); dispensar pede o motivo (400) e não vale para a cumprida (422);
   - **atrasado:** alguma obrigatória atrasada; **etapa atual:** a primeira obrigatória por fazer;
   - **encerramento:** concluído, com as obrigatórias cumpridas ou dispensadas (422); interrompido, com o motivo (400).
     Encerrado, não muda (422).
4. **Painel e busca ativa:** os percursos em curso nas unidades de quem acompanha, filtrados por linha, unidade e
   equipe, **atrasados primeiro**; e a **sugestão** de abrir a linha para quem está no programa dela sem percurso em
   curso (a inscrita no Pré-natal sem a linha da gestante).
5. **Carga inicial editável:**
   - **Gestante, do pré-natal ao puerpério** (programa Pré-natal), contada da última menstruação:
     1. 1ª consulta até a 12ª semana (84 dias);
     2. testes rápidos de sífilis, HIV e hepatites (manual, 84 dias);
     3. dTpa até a 36ª semana (252 dias);
     4. seis consultas até a 40ª semana (280 dias);
     5. parto: alta de internação com CID O80 a O84 até a 42ª semana (294 dias);
     6. consulta puerperal até 42 dias do parto (conta da etapa anterior);
   - **Primeira semana de saúde da criança** (programa Saúde da Criança), contada do nascimento: consulta do
     recém-nascido (7 dias), teste do pezinho (manual, 5 dias), BCG e hepatite B (30 dias).

   As etapas de exame e de regulação (a mamografia na suspeita de câncer de mama, o ultrassom obstétrico) dependem do
   catálogo do município, que começa vazio, e entram pelo editor.
6. **Permissões** (sem permissão nova):
   - **catálogo:** a leitura é de quem está logado; cadastrar e ajustar, `PROGRAMA.GERENCIAR`;
   - **percurso:** abrir, registrar, dispensar e encerrar, `PROGRAMA.ACOMPANHAR` na unidade;
   - **leitura do percurso:** dado de saúde, com as regras do prontuário (`PRONTUARIO.CONSULTAR` ou
     `PROGRAMA.ACOMPANHAR`, vínculo assistencial ou acesso justificado, leitura auditada). A linha de programa sensível
     só aparece para quem acompanha na unidade (ADR-0119);
   - **painel:** `PROGRAMA.ACOMPANHAR`, com a leitura auditada.

### API

| Método | Caminho | Permissão |
|---|---|---|
| GET | `linhas-de-cuidado`, `linha-de-cuidado/{id}` | logado |
| POST / PUT | `linha-de-cuidado`, `linha-de-cuidado/{id}` | `PROGRAMA.GERENCIAR` |
| POST | `percurso-linha-cuidado` | `PROGRAMA.ACOMPANHAR` (escopo) |
| GET | `percurso-linha-cuidado/{id}`, `paciente/{id}/linhas-de-cuidado` | `PRONTUARIO.CONSULTAR` ou `PROGRAMA.ACOMPANHAR`, vínculo, auditada |
| POST | `percurso-linha-cuidado/{id}/etapa`, `percurso-linha-cuidado/{id}/encerramento` | `PROGRAMA.ACOMPANHAR` (escopo) |
| GET | `linhas-de-cuidado/painel?linhaId&unidadeId&equipeId&atrasados` | `PROGRAMA.ACOMPANHAR` (escopo), auditada |

### Tela

- **Programas de Saúde, aba nova "Linhas de cuidado":**
  - **percursos em curso:** a tabela com a etapa atual e o atraso, os filtros de linha e "só atrasados", e "Abrir", que
    mostra o percurso;
  - **sugestões:** quem está no programa sem a linha, com "Abrir a linha";
  - **catálogo:** as linhas com as etapas e como cada uma se cumpre; "Nova linha" e "Editar" (gestão), com o editor de
    etapas (tipo, referência, vezes, prazo, contado do início ou da anterior, obrigatória, subir e remover).
- **Percurso:** as etapas na ordem com a situação, o prazo, o que cumpriu e o motivo da dispensa; "Registrar" na manual,
  "Dispensar", "Concluir o percurso" e "Interromper".
- **Pacientes, aba Programas:** as linhas de cuidado do paciente com a próxima etapa e o atraso, "Ver" e "Abrir linha de
  cuidado".

### Migração

`V40__programas_linhas_de_cuidado.sql`: as tabelas `tb_linha_cuidado`, `tb_etapa_linha_cuidado`,
`tb_percurso_linha_cuidado` e `tb_registro_etapa_percurso`, com os CHECKs dos enums, e a carga das duas linhas.

## Consequências

- **O domínio 22 fica completo:** programa, acompanhamento, campanha, ação programática, linha de cuidado, metas e
  indicadores.
- **A situação é calculada na hora,** como a situação vacinal e os indicadores: sem tarefa agendada e sem número que
  envelhece. O painel lê os registros de cada paciente em curso; numa rede grande, se pesar, vira apuração.
- **A etapa de atendimento sem especialidade aceita qualquer atendimento concluído** depois da referência. Para ser
  mais preciso (só a consulta de pré-natal), a gestão informa a especialidade.
- **Ficam para depois:**
  - a linha recorrente (a consulta do hipertenso a cada seis meses), que hoje é a periodicidade do programa (ADR-0119);
  - os indicadores de linha (percursos em dia, tempo até a etapa) no P6 e na Sala de Situação;
  - a abertura automática por regra: a ADR-0109 pede que seja ato de profissional.

## Testes

- **JUnit:**
  - `LinhaCuidadoControllerTest`:
    - a carga inicial;
    - o cadastro com um exemplo de cada tipo, os CIDs normalizados e o critério em palavras;
    - as recusas (código repetido, exame sem exame, primeira etapa contada da anterior) e o ajuste que desativa;
    - o percurso: o atendimento antes do início que não conta, o exame liberado, a regulação agendada (no prazo, com o
      andamento) e realizada, a vacina atrasada, a internação com alta e CID que libera a etapa seguinte, a manual com
      quantidade dois, a etapa de domínio recusada à mão, a dispensa com e sem motivo;
    - o encerramento concluído com pendência (422), interrompido sem motivo (400), concluído e o registro depois (422);
    - os percursos do paciente;
    - o painel com a sugestão, o acompanhamento de outro programa (422) e os atrasados;
  - `LinhaCuidadoAutorizacaoControllerTest`, com segurança ligada: o catálogo é da gestão; a enfermagem abre na própria
    unidade, não em outra; a recepção não abre nem vê o painel.
- **Robot de API:** `test/programas/linha/LinhaDeCuidado.robot`, CT-001 a CT-012.
- **Robot de tela:** `test/ui/assistencia/programas/UI_programas.robot`, CT-019 a CT-021:
  - abrir a linha pela aba Programas do paciente e registrar a etapa manual;
  - o painel com a dispensa;
  - o editor com as seleções.
