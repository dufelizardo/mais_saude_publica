# 0079 — Telas só oferecem as ações que o perfil permite

## Status

Aceita e implementada. Resolve a pendência "botões visíveis sem a permissão" do guia de ligar a
autorização ([ADR-0068](./0068-tela-usuarios-e-perfis.md)).

## Contexto

As telas refeitas recentemente já escondiam as ações sem permissão, com `pode(...)` sobre
`AuthService.acessoDaInterface()`:
- Setores, Modelo administrativo e Necessidades;
- RH e Profissionais;
- Usuários & Perfis.

Quatro telas ainda mostravam tudo:
- **Atendimentos;**
- **Farmácia;**
- **Pacientes;**
- **Perfil do profissional.**

Com a autorização ligada, um técnico de enfermagem via "+ Triagem", preenchia o formulário e só ao
salvar recebia "Seu acesso não inclui TRIAGEM.REGISTRAR". O dado estava protegido, porque a API recusa,
mas a pessoa perdia tempo e ficava confusa.

## Decisão

1. **Cada ação aparece só com a mesma permissão que a API exige na rota**, conferida nos controllers:

   | Tela | Ação | Permissão |
   |---|---|---|
   | Atendimentos | Acolhimento, Iniciar atendimento, Editar e Concluir atendimento | `ATENDIMENTO.GERENCIAR` |
   | | Agendar, editar agendamento | `AGENDAMENTO.GERENCIAR` |
   | | Triagem (registrar e retificar) | `TRIAGEM.REGISTRAR` |
   | | Consulta | `CONSULTA.REGISTRAR` |
   | | Evolução | `EVOLUCAO.REGISTRAR` |
   | | Procedimento e desfecho | `PROCEDIMENTO.REGISTRAR` |
   | | Checagem de medicação | `MEDICACAO.ADMINISTRAR` |
   | Farmácia | Medicamento, entrada e correção de lote, perda e ajuste | `FARMACIA.GERENCIAR_ESTOQUE` |
   | | Dispensar | `FARMACIA.DISPENSAR` |
   | | Transferir, receber, cancelar | `FARMACIA.TRANSFERIR` |
   | Pacientes | Novo paciente, editar | `PACIENTE.CADASTRAR` |
   | Perfil do profissional | Editar contato, os 13 formulários de registro (título e formulário), aprovar, rejeitar ou solicitar correção de ponto, encerrar adesão | `RH.GERENCIAR` |

2. **A leitura não muda:** listas, detalhes, prontuário e extratos continuam como estão, e quem chega à tela
   sem a permissão de leitura recebe o 403 da API, como antes.
3. **Com a autorização desligada,** como hoje em todos os ambientes, `pode` é sempre verdadeiro e nada muda
   para quem usa.
4. **A retificação aparece com a permissão de registrar.** A regra "só o autor ou a supervisão"
   (ADR-0062) continua decidida pela API, porque a tela não sabe quem é o autor de cada registro.
5. **Esconder nunca substitui a checagem da API** (PADRAO-TELAS-INTERNAS §8).

## Consequências

**Positivas:**
- Com a autorização ligada, cada perfil vê só o que pode fazer em todas as telas.
- Fecha mais um item que bloqueava ligar a autorização num ambiente.

**Negativas:**
- Na abertura da tela, as ações aparecem por um instante, até o acesso carregar, e então somem.
  É o mesmo comportamento das telas que já escondiam.

## Testes

- **Robot de interface (tag `SEGURANCA`), `UI_botoes_por_permissao.robot`:**
  - o técnico de enfermagem não vê "Novo paciente", "Iniciar atendimento" nem "Acolhimento";
  - a recepção vê os três.
- **Sem segurança, os 13 testes de interface continuam passando:** com a autorização desligada, nada muda.
