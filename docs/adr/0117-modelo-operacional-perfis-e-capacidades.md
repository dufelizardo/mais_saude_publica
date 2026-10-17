# 0117 — Modelo operacional: perfis e capacidades da unidade (MO1)

## Status

Aceita e implementada (fatia MO1 da [ADR-0115](./0115-modelo-operacional-dos-equipamentos.md)). As fatias MO2
(serviços), MO3 (ponto operacional) e MO4 (domínios consultam capacidades) vêm depois.

## Contexto

- A [ADR-0115](./0115-modelo-operacional-dos-equipamentos.md) desenhou o modelo operacional dos equipamentos:
  - **o tipo estrutural da unidade não determina sozinho o comportamento;**
  - a unidade declara **perfis operacionais** (o que opera) e **capacidades assistenciais** (o que consegue fazer).
- Detalhe e catálogos iniciais em [`equipamentos/MODELO-OPERACIONAL.md`](../equipamentos/MODELO-OPERACIONAL.md).
- Hoje as regras dependem do tipo (`TipoUnidadeDeSaude`) ou de nada:
  - não há onde dizer que um CAPS é II ou AD;
  - nem que um hospital tem UTI e maternidade;
  - nem quais especialidades um CEO faz.
- A Saúde Mental (SM5), a Saúde Bucal (SB4) e as Emergências (E3) dependem desse catálogo.
- O precedente é o modelo administrativo: catálogo como dado e associação por tipo (ADRs 0031 e 0032).
  - O modelo operacional segue o padrão do catálogo, mas associa **por unidade**, porque é isso que diferencia um
    hospital geral de uma maternidade.

## Decisão

1. **Dois catálogos editáveis** (tabelas, não enums):
   - **`PerfilOperacional`:**
     - campos: código, nome, descrição e ativo;
     - o código é único e guardado em maiúsculas.
   - **`CapacidadeAssistencial`:**
     - os mesmos campos, mais um **perfil opcional**, que é o grupo da capacidade;
     - sem perfil, a capacidade é **transversal** (acolhimento, demanda espontânea) e vale em qualquer unidade de
       atendimento.
2. **Associação por unidade, como histórico:**
   - `UnidadePerfilOperacional` e `UnidadeCapacidadeAssistencial` guardam:
     - início e fim;
     - motivo do fim;
     - quem registrou;
     - e, na capacidade, uma observação (a portaria de habilitação, por exemplo).
   - Sem fim, o registro está vigente.
   - **Encerrar não apaga:** o registro vai para o histórico. Voltar cria um registro novo.
3. **Regras:**

   | Situação | Resposta |
   |---|---|
   | O mesmo perfil ou capacidade vigente duas vezes na unidade | 409 |
   | Item inativo do catálogo | 422 (não entra em unidade nova; os vigentes ficam) |
   | Nível de gestão (federal, estadual, municipal, regional) | 422: perfis e capacidades são das unidades de atendimento |
   | Capacidade de um grupo sem o perfil do grupo vigente na unidade | 422 (ex.: CAPS AD exige Atenção psicossocial) |
   | Encerrar um perfil com capacidades do grupo vigentes | 422, com a lista delas |
   | Mudar o perfil de uma capacidade vigente em alguma unidade | 422 |
   | Início no futuro; fim antes do início ou no futuro | 400 |
   | Código repetido no catálogo | 409 |

4. **API:**
   - `/api/v1/perfil-operacional/` e `/api/v1/capacidade-assistencial/`:
     - rotas GET, GET por id, POST e PATCH;
     - a lista de capacidades aceita `perfilId`;
     - cada item traz em quantas unidades está vigente; o perfil traz também quantas capacidades agrupa.
   - `GET /api/v1/unidade-saude/id/{uuid}/modelo-operacional`: perfis e capacidades vigentes e os encerrados.
   - `POST .../modelo-operacional/perfil` e `POST .../modelo-operacional/capacidade`.
   - `POST .../modelo-operacional/{perfil|capacidade}/{id}/encerramento`, com motivo.
   - **A rede (`GET /api/v1/unidade-saude/rede`, ADR-0101) traz os códigos dos perfis vigentes** de cada unidade.
5. **Consulta pelos domínios:**
   - `ModeloOperacionalService.unidadeTemCapacidade(unidade, código)` é a pergunta que os domínios passam a fazer
     no lugar do tipo.
   - Ninguém a usa ainda: o uso é a fatia MO4.
6. **Acesso:**
   - igual ao de Equipamentos: ler com `ORGANIZACAO.GERENCIAR` ou `ADMINISTRATIVO.CONSULTAR`, escrever com
     `ORGANIZACAO.GERENCIAR`;
   - sempre no escopo da unidade;
   - as alterações entram na trilha de auditoria, nos recursos `PERFIL_OPERACIONAL`, `CAPACIDADE_ASSISTENCIAL` e
     `MODELO_OPERACIONAL`.
7. **Migração V11** (roda uma vez; depois, tudo é editável pela tela):
   - as quatro tabelas;
   - **10 perfis:** atenção primária, urgência, internação, atenção especializada, atenção psicossocial,
     diagnóstico, assistência farmacêutica, vigilância, reabilitação e atenção móvel;
   - **46 capacidades, 4 transversais.** Entre elas:
     - as modalidades de CAPS (I, II, III, i, AD, AD III) e o acolhimento noturno;
     - as especialidades mínimas de CEO e a prótese;
     - UTI, cirurgia e obstetrícia;
     - as quatro modalidades de reabilitação;
     - dispensação de controlados;
     - inspeção e licenciamento sanitário;
     - atendimento pré-hospitalar e regulação médica de urgência.
   - **As unidades existentes recebem o perfil do tipo:**

     | Tipo | Perfil |
     |---|---|
     | UBS | atenção primária |
     | UPA | urgência |
     | Hospital | internação e urgência |
     | CAPS | atenção psicossocial |
     | Laboratório | diagnóstico |
     | Centro de especialidades | atenção especializada |
     | Policlínica | atenção especializada e diagnóstico |
     | Centro de reabilitação | reabilitação |

     **Capacidades não entram sozinhas:** não dá para adivinhar se um CAPS é II ou AD.
8. **Telas:**
   - **Aba Capacidades** na ficha da unidade em Equipamentos de Saúde, entre Operacional e Equipes:
     - os perfis vigentes, com a data;
     - as capacidades agrupadas por perfil, mais o grupo das transversais;
     - **Adicionar perfil**, **Adicionar capacidade** (o seletor só oferece as transversais e as dos perfis que a
       unidade tem) e **Encerrar**, com motivo;
     - **Mostrar encerrados**, para o histórico.
     - O nível de gestão mostra que não tem perfil.
   - **Filtro "Perfil operacional"** na lista de Equipamentos.
   - **Tela Modelo operacional** em `/administrativo/modelo-operacional`, no grupo Administrativo:
     - abas Perfis operacionais e Capacidades assistenciais, com lista e gaveta;
     - mesmo layout do Modelo administrativo (ADR-0074);
     - do perfil, o botão Capacidades filtra as do grupo.

## Consequências

- **SB4 (CEO), SM5 (CAPS) e E3 (capacidade emergencial) ficam sem o pré-requisito MO1.**
  - As modalidades de CAPS e as especialidades de CEO **já estão no catálogo**.
  - O que falta nessas fatias é o fluxo de cada domínio.
- **O tipo da unidade continua estrutural** (hierarquia, regra de nível) e não cresce por classificação.
  - "Maternidade" é um hospital com a capacidade Parto e obstetrícia.
- **Fora do escopo:**
  - serviços da unidade (MO2);
  - local de realização do atendimento e unidade móvel (MO3);
  - Leitos, Agenda e os outros domínios passarem a consultar as capacidades (MO4);
  - capacidade emergencial ativável (E3);
  - sincronização com as habilitações do CNES (Integrações, #20).

## Testes

- **JUnit:**
  - `ModeloOperacionalControllerTest`:
    - carga inicial;
    - catálogo com código único e perfil inativo;
    - perfil e capacidade como histórico, coerência do grupo e encerramento em ordem;
    - perfil de capacidade vigente não muda;
    - nível de gestão recusado;
    - associação de outra unidade não encerra;
    - perfis na rede;
    - `unidadeTemCapacidade`.
  - `ModeloOperacionalAutorizacaoControllerTest`: quem tem `ORGANIZACAO.GERENCIAR` inclui, o gestor só lê e o
    gestor de outra unidade não alcança.
  - `MigracoesDoEsquemaTest`, `RedeUnidadesControllerTest` e `EquipeControllerTest` continuam passando.
- **Robot de API** (`test/equipamentos/modelo_operacional`): 8 casos. A suíte de Rede continua passando.
- **Robot de interface** (`test/ui/administrativo/modelo_operacional`): 6 casos.
  - Na aba Capacidades: incluir perfil e capacidade, encerrar com motivo e filtrar a rede por perfil.
  - Na tela Modelo operacional: carga inicial, cadastro e validação.
