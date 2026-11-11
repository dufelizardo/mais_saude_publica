# 0193 — Emergências e Desastres: vulneráveis, pacientes deslocados e abrigos (E4a)

## Status

Aceita e implementada. Primeira metade da fatia **E4** do domínio 27 ([ADR-0113](./0113-emergencias-e-desastres.md); detalhe em
[`emergencias/MODELO-EMERGENCIAS.md`](../emergencias/MODELO-EMERGENCIAS.md), seção 11). A E4b (redistribuição e Regulação) segue na
ADR-0194.

## Contexto

- **A ADR-0113 manda que a lista de vulneráveis venha do Território e dos Programas,** com os deslocados registrados por origem e destino;
  abrigo é local, não domínio.
- **O que o sistema já sabe:**
  - quem mora em cada domicílio, com a microárea e o ACS ([ADR-0127](./0127-cadastro-territorial.md));
  - os acompanhamentos em programas, com o gestante e o sensível ([ADR-0119](./0119-programas-acompanhamento.md));
  - as sessões de diálise e terapia seriada e o oxigênio no transporte ([ADR-0190](./0190-transporte-eletivo-e-tfd.md)).
- **A rede intersetorial** ([ADR-0154](./0154-intersetorialidade.md)) tem o acolhimento institucional no catálogo e deixou o
  encaminhamento aberto às Emergências.
- **Estoque e Transporte** já atendem a mobilização (ADRs 0191 e 0192).

## Decisão

1. **Vulneráveis da área afetada** (calculados, sem tabela):
   - **Quem entra:** os membros vigentes dos núcleos familiares nos domicílios das microáreas afetadas, ou em imóvel com coordenada dentro
     do polígono da emergência.
   - **Marcadores:**
     - idoso (60 anos ou mais) e criança (menos de 5);
     - gestante, pelo programa marcado como gestante;
     - os programas não sensíveis, pelo nome (acamado e atenção domiciliar entram assim, quando o município os cadastra);
     - diálise ou terapia seriada, pelas sessões futuras de transporte;
     - oxigênio, pelo transporte com oxigênio nos últimos 90 dias.
   - **Cada linha:** o nome, a idade, o telefone, o endereço do imóvel, a microárea, o ACS, os marcadores e os programas, e se a pessoa já
     está deslocada nesta emergência (e onde).
   - **Por padrão, só quem tem marcador;** um filtro traz todos; outro filtra por marcador ou microárea. CSV para a busca ativa.
   - **Sigilo:** o programa sensível não aparece nem marca; saúde mental e agravos ficam de fora. O acesso pede `EMERGENCIA.COORDENAR` e
     `PACIENTE.CONSULTAR`; a leitura e o CSV entram na auditoria.
2. **Paciente deslocado** (`TB_PACIENTE_DESLOCADO`):
   - **Origem:** o domicílio (o endereço do cadastro, quando não se informa outro) ou a unidade de saúde evacuada.
   - **Destino:**
     - unidade de saúde (400 sem ela);
     - abrigo: a instituição ativa da rede intersetorial (inativa, 422) ou o nome do local (400 sem nenhum dos dois);
     - casa de parente, com o endereço, ou outro, com a descrição (400 sem texto);
     - o domicílio não é destino: voltar é o retorno (400).
   - **Também:** a saída (no futuro, 400), as necessidades, o responsável e a solicitação de transporte da T2 do mesmo paciente (outro,
     422; atalho "Pedir transporte" na tela).
   - **Situações:** deslocado (fora de casa), realocado, retornou e encerrado (com motivo, 400 sem ele).
   - **Regras:**
     - o mesmo paciente deslocado em aberto na mesma emergência: 409;
     - registrar e realocar só com a emergência ativa (422);
     - **a realocação** fecha o registro e abre outro ligado a ele (a origem do novo é o destino do anterior), para guardar o caminho;
     - retorno, encerramento, transporte e encaminhamento valem também na desmobilização; encerrada, 422;
     - só o deslocado em aberto muda (422).
3. **Encaminhamento intersetorial:** o domínio de origem `EMERGENCIAS` entra no CHECK; o deslocado é o registro de origem; a unidade que
   encaminha é informada. As regras são as da ADR-0154 (409 duplicado, recebimento, retorno, cancelamento).
4. **A emergência mostra a ocupação dos destinos,** sem nomes (por exemplo, "Ginásio Municipal: 23"), e quantos seguem fora de casa.
5. **Desmobilização:** os deslocados ainda fora entram nas pendências, só como aviso.
6. **Relatório pós-evento:** os pacientes deslocados, os que retornaram, os que seguem fora, os abrigos usados e os encaminhamentos.
7. **Prontuário:** o aviso "Deslocado na emergência X", com o destino e a data, enquanto em aberto. Lê quem tem `PACIENTE.CONSULTAR` ou
   `PRONTUARIO.CONSULTAR`.
8. **Telas:**
   - **Emergência**, seção **Pessoas**:
     - a ocupação dos destinos;
     - "Ver vulneráveis", sob demanda: os filtros, o CSV e "Deslocar";
     - "Ver deslocados": registrar, realocar, retornou, encerrar, pedir e ligar transporte, encaminhar.
   - **Prontuário:** o aviso.

### API

| Método | Caminho | Permissão |
|---|---|---|
| GET | `emergencia/{id}/vulneraveis` e `/vulneraveis.csv`, com `todos`, `marcador` e `microareaId` | `EMERGENCIA.COORDENAR` e `PACIENTE.CONSULTAR` |
| GET, POST | `emergencia/{id}/deslocados`, `emergencia/{id}/deslocado` | idem |
| POST | `emergencia/{id}/deslocado/{d}/realocacao`, `/retorno`, `/encerramento` | idem |
| PUT | `emergencia/{id}/deslocado/{d}/transporte` | idem |
| POST | `emergencia/{id}/deslocado/{d}/encaminhamento` e `/{e}/recebimento`, `/retorno`, `/cancelamento` | idem |
| GET | `emergencias/deslocamentos?pacienteId` | `PACIENTE.CONSULTAR` ou `PRONTUARIO.CONSULTAR` |

A emergência ganha `abrigos` e `deslocadosEmAberto`.

### Migração

`V77__emergencias_vulneraveis_e_deslocados.sql`: `tb_paciente_deslocado` e o CHECK do domínio de origem do encaminhamento intersetorial
com `EMERGENCIAS`.

## Alternativas descartadas

- **Guardar a lista de vulneráveis:** envelheceria com o cadastro; calculada, mostra sempre o que os donos sabem.
- **Cadastro próprio de acamado e de pessoa com deficiência:** seria um registro paralelo; o programa do município já cobre, e o
  cadastro individual fica com o Território.
- **Abrigo como entidade:** a ADR-0113 manda que seja local; a rede intersetorial ou um texto bastam, e a ocupação sai dos deslocados.

## Consequências

- **STATUS-DOS-DOMINIOS:** a E4a sai das pendências do domínio 27; segue a E4b.
- **Implantação:** sem permissão nova. A coordenação (Gestor, Coordenador de enfermagem) vê e registra; cadastrar os abrigos na rede
  intersetorial ajuda a ocupação.
- **Fora do escopo:** CadÚnico e Defesa Civil (domínio 20); gestão do abrigo (estoque do abrigo, escala de voluntários).

## Testes

- **JUnit `PessoasEmergenciaControllerTest`:**
  - **vulneráveis:** a microárea marcada e o imóvel dentro do polígono, sem o de fora; idoso, criança, gestante com o programa, o ACS;
    o caso de saúde mental sem marcador nem programa; os filtros, o CSV e a diálise pelo transporte;
  - **deslocados:**
    - os destinos que pedem o que precisam (400), o domicílio como destino (400), duplicado (409) e transporte de outro paciente
      (422);
    - a ocupação na emergência;
    - a realocação com o caminho e o antigo travado (422);
    - o aviso do prontuário, o retorno, o encerramento sem motivo (400);
  - **encaminhamento** ao CRAS (409 duplicado, recebido, concluído);
  - **a desmobilização** com a pendência, o deslocado novo barrado (422), o encerramento ainda aceito e o relatório.
- **JUnit `PessoasEmergenciaAutorizacaoControllerTest`,** com a segurança ligada: a coordenação vê e registra; a recepção não vê a lista
  nem registra, mas lê o aviso do prontuário; o almoxarife, nada.
- **Robot:** `PessoasEmergencia.robot` (API, CT-001 e CT-002) e CT-014 de `UI_emergencias.robot`.
