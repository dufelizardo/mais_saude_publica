# 0170 — Modelo Operacional: domínios consultam capacidades (MO4)

## Status

Aceita e implementada. Última fatia da camada 29, desenhada na [ADR-0115](./0115-modelo-operacional-dos-equipamentos.md). Com ela, a camada
fica completa. Corrige a [ADR-0165](./0165-modelo-operacional-servicos-da-unidade.md) quanto à sala de vacina.

## Contexto

- **A ADR-0115 pede que os domínios perguntem "a unidade tem a capacidade X?",** e não olhem o tipo da unidade.
- **Boa parte já faz isso:** Regulação (ADR-0138), prótese (LRPD), acolhimento noturno do CAPS, rede psicossocial e Sala de Vacina.
- **Leitos era a lacuna:** qualquer unidade podia cadastrar leito de qualquer tipo.
- **A ADR-0165 errou:** disse que o catálogo não tinha capacidade de vacinação, mas `SALA_VACINA` existe desde a Imunização (ADR-0147).

## Decisão

1. **Cada tipo de leito pede uma capacidade** vigente, ao criar e ao trocar o tipo (422):

   | Tipo de leito | Capacidade |
   |---|---|
   | Clínico, pediátrico, isolamento | `INTERNACAO_CLINICA` |
   | Cirúrgico | `CIRURGIA` |
   | Obstétrico | `OBSTETRICIA` |
   | UTI adulto, pediátrica, neonatal | `UTI` |
   | Observação | `OBSERVACAO` |

   Editar um leito sem trocar o tipo não confere de novo, para a unidade que perdeu a capacidade poder corrigir o leito.
2. **A capacidade não encerra com leito ativo que dependa dela** (422, com a quantidade), como já vale para os serviços (ADR-0165).
3. **Capacidade emergencial de leitos** pede capacidade de internação ou de observação na unidade, ao cadastrar (422). As já cadastradas
   continuam.
4. **O tipo de serviço Sala de vacina exige `SALA_VACINA`.**
5. **Nada do que existe quebra:** a migração dá às unidades com leitos ativos o perfil e as capacidades correspondentes, e às unidades com sala
   de vacina vigente, a capacidade. A observação da associação registra a origem.

### O que não muda, de propósito

- **Saúde Mental:** o caso abre em qualquer unidade. A atenção primária faz parte da rede psicossocial; exigir CAPS impediria o cuidado na UBS.
- **Saúde Bucal:** a UBS com equipe de saúde bucal atende. O que é do CEO já é controlado na Regulação.
- **Agenda:** consulta com especialista em UBS é legítima, e nenhuma capacidade representa isso sem criar bloqueio falso.
- **"Funciona 24 horas" no cadastro:** é só o valor inicial sugerido pelo tipo, porque a unidade nova ainda não tem capacidades.

### Migração

`V55__modelo_operacional_capacidades_nos_dominios.sql`: só dados, sem mudança de esquema.

## Consequências

- **A camada 29 fica completa:** perfis, capacidades, serviços, pontos operacionais e os domínios consultando.
- **Unidade nova precisa da capacidade antes do leito.** A mensagem diz qual e onde incluir.
- **Leito psiquiátrico segue como leito clínico:** a capacidade `LEITO_SAUDE_MENTAL` conta para o leito emergencial, mas não há tipo de leito próprio.

## Testes

- **JUnit:** `CapacidadesNosDominiosControllerTest`: os nove tipos de leito, a troca de tipo, a edição sem troca, a capacidade presa ao leito, o leito
  emergencial, a sala de vacina e a carga. Os testes de leito, internação e emergência passam a habilitar a capacidade.
- **Migração:** executada sobre um banco com leitos sem capacidade, conferindo a inclusão e que rodar de novo não duplica nada.
- **Robot:** caso 12 de `LeitoInternacao.robot`; as sementes de leito e de capacidade emergencial habilitam a capacidade antes.
