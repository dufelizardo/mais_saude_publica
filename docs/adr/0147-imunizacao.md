# 0147 — Imunização: domínio próprio e fundação (I1)

## Status

Aceita. **Desenho do domínio 24** (reservado pela [ADR-0110](./0110-vigilancia-em-saude.md)) e **I1 implementada**. As
fatias I2 a I5 têm ADR própria quando entrarem: a I2 está na [ADR-0148](./0148-imunizacao-sala-de-vacina.md), a I3
na [ADR-0149](./0149-imunizacao-cobertura-e-campanhas.md) e a I4 na [ADR-0150](./0150-imunizacao-esavi.md).

## Contexto

- **A ADR-0110 separou a Imunização da Vigilância,** como o domínio 24, a desenhar. Ela atravessa a vigilância, a
  assistência e o estoque.
- **O que já existia em volta:**
  - o programa "Imunização" no catálogo de Programas;
  - a campanha do tipo vacinação (P3, [ADR-0133](./0133-programas-campanhas.md)), com a meta andando pelas ações;
  - a ação programática "Vacina" (P4);
  - a medida "Bloqueio vacinal" da Vigilância;
  - a aba "Vacinação" do paciente e o bloco de cobertura vacinal da Sala de Situação, os dois "Em breve".
- **Faltava o núcleo:** o imunobiológico, o calendário, a dose aplicada e a situação vacinal da pessoa.

## Decisão — o domínio

1. **Imunobiológico é catálogo próprio,** separado do medicamento da Farmácia: vacina, soro ou imunoglobulina, com a
   via, o volume da dose, as doses por frasco, a validade depois de aberto e o código do SI-PNI. Tem carga inicial com
   os imunobiológicos do Calendário Nacional de Vacinação (PNI), editável.
2. **O calendário vacinal é dado, não código.** Cada dose do calendário tem:
   - o imunobiológico e a dose: 1ª, 2ª, 3ª, 4ª, reforço, 2º reforço, única, anual ou adicional;
   - o público: criança, adolescente, adulto, idoso ou gestante;
   - as idades mínima, recomendada e máxima, em meses, e o intervalo mínimo desde a dose anterior do mesmo
     imunobiológico;
   - a repetição, como o dT a cada 10 anos, e a vigência;
   - **se conta na situação:** as doses de idade definida entram nos atrasos. As que dependem da situação (hepatite B e
     tríplice viral no adulto, influenza anual, dTpa na gestação, pneumo 23 no idoso) ficam como recomendação.

   Quando o Ministério muda o calendário, encerra-se a vigência e cadastra-se a versão nova.
3. **A dose aplicada é registro clínico** (ADR-0062): imutável, corrigida por retificação. O registro feito por engano
   é anulado com motivo.
4. **O estoque de vacinas é da Imunização** (I2), como o de medicamentos é da Farmácia. O Estoque geral (#13) é de
   insumos.
5. **A campanha continua sendo de Programas** (ADR-0110). A dose registra a campanha, e a cobertura e o andamento real
   da meta vêm na I3.

### Fatias

| Fatia | Entrega | Depende de |
|---|---|---|
| **I1** · fundação ✅ | imunobiológicos, calendário, registro de dose (aplicada e transcrita), situação vacinal, caderneta do paciente e Sala de Vacina | — |
| **I2** · sala de vacina e cadeia de frio ✅ ([ADR-0148](./0148-imunizacao-sala-de-vacina.md)) | capacidade "sala de vacina" (MO1); lotes por unidade com livro de movimentação (entrada, baixa na aplicação, perdas técnica e física; o remanejamento entre unidades ficou para depois); frasco aberto com validade; temperatura da câmara duas vezes ao dia e lotes sob suspeita na excursão | I1 |
| **I3** · cobertura e campanhas ✅ ([ADR-0149](./0149-imunizacao-cobertura-e-campanhas.md)) | dose da campanha no andamento real da meta; cobertura vacinal, homogeneidade e taxa de abandono por vacina, unidade e território; busca ativa por microárea; vacinação na Sala de Situação | I1, adscrição (#21) |
| **I4** · ESAVI ✅ ([ADR-0150](./0150-imunizacao-esavi.md)) | evento supostamente atribuível à vacinação, notificado pela Vigilância e ligado à dose | I1, V1 |
| **I5** · SI-PNI/RNDS | envio das doses ao Ministério | domínio 20 |

## Decisão — a fatia I1

1. **Registro da dose** (`TB_REGISTRO_VACINACAO`):
   - **o que registra:** paciente, imunobiológico, dose, data e hora, unidade que registra, via, local de aplicação,
     estratégia (rotina, campanha, bloqueio, intensificação ou especial/CRIE), campanha (só de vacinação, 422), se é
     gestante e o atendimento, quando houver;
   - **aplicada:** pede quem aplicou e o lote (400). A via vem do imunobiológico, se não for informada;
   - **transcrita da caderneta:** pede a data (400) e não exige o lote; registra onde a dose foi aplicada;
   - **regras:**
     - nada no futuro nem antes do nascimento (400);
     - a mesma dose vigente não se repete (409); a anual, uma por ano; a adicional e a dose que se repete no calendário
       podem repetir;
     - **fora da idade ou do intervalo do calendário,** só com a justificativa (422 sem ela), como na prática do PNI:
       a dose fora do esquema é possível, mas registrada com o porquê;
     - quando a mesma vacina e dose está em mais de um público (hepatite B ao nascer e no adulto, influenza da criança,
       da gestante e do idoso), vale a dose do calendário em cuja faixa de idade o paciente está.
2. **Retificação e anulação:**
   - **retificação:** nova versão do mesmo paciente, com o motivo, só da vigente, pelo autor ou pela supervisão;
   - **anulação:** para o registro por engano (paciente errado, dose que não houve), com o motivo; sai da caderneta e
     fica na trilha.
3. **Situação vacinal** (`CalendarioVacinal`), para cada dose que conta na situação:
   - **aplicada;**
   - **atrasada:** a data prevista já passou;
   - **próxima:** nos próximos 30 dias;
   - **futura;**
   - **fora da idade:** passou da idade máxima sem a dose.

   A data prevista sai da idade recomendada e do intervalo mínimo desde a dose anterior. A dose que se repete vale pela
   última aplicada. Sem a data de nascimento, a situação não é calculada.
4. **Caderneta do paciente** (`GET paciente/{id}/vacinacao`): as doses vigentes, o calendário com a situação e as
   recomendações do público do paciente. É dado de saúde e segue as regras do prontuário: `PRONTUARIO.CONSULTAR` ou
   `VACINACAO.REGISTRAR`, vínculo assistencial ou acesso justificado ([ADR-0076](./0076-prontuario-por-vinculo-assistencial.md)),
   leitura auditada.
5. **Sala de vacina:**
   - as doses aplicadas na unidade no dia, com a contagem por vacina;
   - **atrasados:** os adscritos às equipes da unidade com dose atrasada, os de atraso mais antigo primeiro, com a
     equipe e a microárea, para a busca ativa.
6. **Acesso:**
   - `VACINACAO.REGISTRAR` (dado de saúde): técnico de enfermagem, enfermeiro e coordenador de enfermagem, no escopo da
     unidade;
   - `IMUNIZACAO.GERENCIAR` (catálogo e calendário): gestor, coordenador de enfermagem e vigilância epidemiológica;
   - o catálogo e o calendário são lidos por qualquer logado.

### API

| Método | Caminho | Permissão |
|---|---|---|
| GET | `imunobiologicos`, `calendario-vacinal` | logado |
| POST / PUT | `imunobiologico`, `imunobiologico/{id}` | `IMUNIZACAO.GERENCIAR` |
| POST / PUT | `esquema-vacinal`, `esquema-vacinal/{id}` | `IMUNIZACAO.GERENCIAR` |
| POST | `vacinacao` | `VACINACAO.REGISTRAR` (escopo) |
| POST | `vacinacao/{id}/retificacao`, `vacinacao/{id}/anulacao` | `VACINACAO.REGISTRAR`; autor ou supervisão |
| GET | `paciente/{id}/vacinacao` | `PRONTUARIO.CONSULTAR` ou `VACINACAO.REGISTRAR`; regras do prontuário |
| GET | `vacinacao/dia?unidadeId&data`, `vacinacao/atrasados?unidadeId` | `VACINACAO.REGISTRAR` (escopo) |

### Tela

- **Aba Vacinação do paciente:**
  - a situação (em dia, ou quantas doses atrasadas e próximas) e a idade;
  - o calendário com a situação de cada dose (as futuras e as fora da idade aparecem com um interruptor);
  - as doses registradas, com retificar e anular;
  - as recomendações conforme a situação;
  - os botões "Registrar vacinação" e "Transcrever da caderneta".
- **Sala de Vacina** (Assistência, para quem tem `VACINACAO.REGISTRAR`):
  - a unidade;
  - "Doses do dia", com a contagem por vacina;
  - "Atrasados", com o link para a caderneta de cada paciente;
  - "Calendário vacinal", agrupado por público.
- **Registro na gaveta:**
  - busca do paciente, aplicada ou transcrita, vacina e dose do calendário;
  - quem aplicou, preenchido pelo profissional logado (ADR-0065), lote, via, local e estratégia;
  - a justificativa aparece quando a API diz que a dose está fora do calendário.

### Migração

`V36__imunizacao_fundacao.sql`:
- as tabelas `tb_imunobiologico`, `tb_esquema_vacinal` e `tb_registro_vacinacao`;
- a carga dos 20 imunobiológicos e do calendário: criança, adolescente, adulto, idoso e gestante, vigente desde
  2025-01-01;
- conferida contra o `ddl-auto=validate`.

## Consequências

- **A situação vacinal depende da data de nascimento** e só conta as doses de idade definida. A da gestante, as de
  campanha e as do adulto não vacinado ficam como recomendação, sem gerar atraso.
- **O lote é texto até a I2.** A I2 ([ADR-0148](./0148-imunizacao-sala-de-vacina.md)) liga a dose ao lote do estoque
  da sala de vacina e dá baixa na aplicação; a unidade sem sala de vacina segue com o lote em texto.
- **A lista de atrasados vem da adscrição** (domínio 21): quem não está adscrito a uma equipe da unidade não aparece
  nela, mas a caderneta de cada paciente mostra os atrasos.
- **A cobertura vacinal e o bloco de vacinação da Sala de Situação** ficam para a I3
  ([ADR-0149](./0149-imunizacao-cobertura-e-campanhas.md)).
- **Também neste PR:** a etiqueta vermelha das telas da Sala de Situação e dos programas usava uma classe que não
  existe (`tag--err`) e aparecia sem cor; passou a usar `tag--alert`.

## Testes

- **JUnit:**
  - `VacinacaoControllerTest`:
    - catálogo e calendário da carga inicial, e a gerência;
    - registro com as regras: lote, quem aplicou, futuro, repetição e fora do calendário com justificativa;
    - a escolha da dose do calendário pela idade (adulto);
    - transcrição;
    - situação vacinal calculada;
    - retificação e anulação;
    - sala de vacina do dia e atrasados adscritos;
  - `VacinacaoAutorizacaoControllerTest`, com segurança ligada:
    - técnico na própria unidade, e 403 em outra unidade e para a recepção;
    - retificação pelo autor ou pela coordenação;
    - catálogo pela gestão.
- **Robot de API:** `test/imunizacao/Vacinacao.robot`.
- **Robot de tela:** `test/ui/imunizacao/UI_sala_de_vacina.robot`.
