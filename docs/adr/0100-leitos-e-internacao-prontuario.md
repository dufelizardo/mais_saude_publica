# 0100 — Leitos e internação: prontuário e vínculo assistencial

## Status

Aceita e implementada (parte 3 de 3). Fecha o primeiro ciclo do domínio #12 e o item 5 da sequência pós-v1.4.0.
Usa as ADRs [0098](./0098-leitos-e-internacao-backend.md) e [0099](./0099-leitos-e-internacao-tela.md) e estende
a regra de vínculo da [ADR-0076](./0076-prontuario-por-vinculo-assistencial.md).

## Contexto

- Depois da ADR-0099, a internação só aparecia na tela Leitos. O prontuário, onde se olha o histórico do
  paciente, não mostrava nem a internação nem o sumário de alta.
- O prontuário por vínculo (ADR-0076) olha atendimento, agendamento e regulação (ADR-0089). O paciente
  internado sem atendimento recente na unidade ficava sem vínculo para a equipe que cuida dele. O médico
  responsável de outra unidade também ficava de fora.

## Decisão

1. **Quinta condição de vínculo:** uma internação em curso, ou com alta dentro da janela do vínculo (30 dias,
   `app.security.prontuario-por-vinculo.janela-dias`), dá acesso a:
   - **quem tem acesso à unidade da internação**, que é a equipe do hospital;
   - **o médico responsável pela internação**, mesmo de outra unidade.

   O acesso registra "Internação na unidade …" ou "Médico responsável pela internação …". Depois da janela, o
   vínculo acaba, como nas outras condições.
2. **Internações no prontuário:**
   - `GET /prontuario/{pacienteId}` ganha `internacoes`, da mais recente à mais antiga;
   - cada uma vem completa: leito, setor, unidade, médico, CID, motivo, permanência, alta (tipo, data e
     quem deu), sumário e movimentos;
   - **sem endpoint novo:** a regra de vínculo e a auditoria do prontuário valem para elas.
3. **Tela de Atendimentos:**
   - a aba **Prontuário** ganha a seção **Internações**, acima dos exames;
   - o detalhe do atendimento mostra a **Internação** que nasceu dele;
   - as duas usam o componente `shared/internacoes-paciente`. Os movimentos ficam recolhidos, e a
     internação em curso tem o link "Ver no mapa de leitos".

## Consequências

- O cuidado do internado não depende mais de "quebra de vidro": a equipe da unidade e o médico responsável têm
  vínculo enquanto o paciente está internado e por 30 dias depois da alta.
- O sumário de alta chega a quem continua o cuidado pelo prontuário.
- O guia de autorização (`docs/acesso/GUIA-LIGAR-AUTORIZACAO.md`) passa a listar a internação entre as condições
  de vínculo.

## Testes

- **JUnit, com login, autorização e vínculo ligados** (`ProntuarioInternacaoVinculoTest`):
  - a equipe da unidade da internação abre o prontuário, com o motivo da internação;
  - o médico responsável, de outra unidade, também abre;
  - a enfermagem de outra unidade recebe 403;
  - com a alta recente, o vínculo continua e o sumário aparece; depois da janela, 403.
- **Robot de API:** CT-011, internação com motivo e movimentos no prontuário.
- **Robot de interface:** CT-008 de `UI_leitos.robot`, internação no prontuário.
