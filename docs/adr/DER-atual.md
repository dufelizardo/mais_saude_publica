# DER (Diagrama Entidade-Relacionamento) — Modelo ATUAL implementado

**Data:** 2026-09-06 (criado) — **corrigido em 2026-09-26** — inventário em 2026-11-07
**Versão:** 1.1 (reflete o código em `src/main/java/com/edufelizardo/maissaudepublica/models/`)

> **Correção 2026-09-26**: a versão 1.0 deste documento descrevia `tipo` como `int` solto (não
> `@Enumerated`) e `TipoUnidadeDeSaude` com apenas 6 valores (`ADMINISTRACAO1..4`, `UBS`,
> `HOSPITAL`). Isso ficou desatualizado a partir da ADR-0013 (5º nível hierárquico e supervisão
> regional) e da ADR-0031 (extensão do enum para os tipos de Unidade de Saúde do Administrativo).
> Corrigido para refletir o código real, como parte do levantamento de
> [`MAPA-DE-EQUIPAMENTOS-DE-SAUDE.md`](../MAPA-DE-EQUIPAMENTOS-DE-SAUDE.md) (ver
> [ADR-0053](./0053-criterio-de-governanca-para-equipamentos-de-saude.md)).

Este documento descreve o schema **realmente implementado** hoje, em contraste com o modelo de domínio clínico futuro/proposto em [`DER.md`](./DER.md). Ver também [`0002-modelar-hierarquia-como-entidade-unica-autorreferenciada.md`](./0002-modelar-hierarquia-como-entidade-unica-autorreferenciada.md) para a decisão arquitetural por trás deste modelo.

---

## Diagrama

```
┌────────────────────────────────────────────────────┐
│                 TB_UNIDADE_DE_SAUDE                 │
├────────────────────────────────────────────────────┤
│ uuid (PK)                    UUID                   │
│ nome                         VARCHAR   UNIQUE, NOT NULL │
│ tipo                         VARCHAR   @Enumerated(STRING) — ver TipoUnidadeDeSaude │
│ unidade_superior_id (FK)     UUID      NULL — autorreferência (mesma tabela), hierarquia de 5 níveis │
│ supervisao_regional_id (FK)  UUID      NULL — vínculo lateral de supervisão técnica (só UBS/HOSPITAL/demais tipos de Unidade de Saúde), sempre para uma unidade REGIONAL │
│ regiao                       VARCHAR   NULL — só preenchido no nível "Regional"  │
│ municipio                    VARCHAR   NULL — só preenchido no nível "Municipal" │
│ estado_administracao         VARCHAR   NULL — só preenchido no nível "Estadual"  │
│ responsavel_cpf              VARCHAR   NULL — CPF do responsável, aceito antes do Profissional existir (ADR-0014) │
│ responsavel_id (FK)          UUID      NULL — vínculo com Profissional, quando reconciliado │
│ email                        VARCHAR                │
│ ativo                        BOOLEAN                │
│ -- colunas embutidas de Endereco (@Embedded) --     │
│ cep, logradouro, numeroLogradouro, complemento,     │
│ bairro, cidade, estado, ddd     (todas VARCHAR)     │
└───────────────┬──────────────────────┬──────────────┘
                │ 1                    │ 1
                │ (unidade_superior_id)│
                ▼ N (auto-relacionamento)
        [outra linha de TB_UNIDADE_DE_SAUDE]
                │
                │ 1 ── N (via @ElementCollection, FK implícita para a uuid da unidade)
   ┌────────────┼──────────────────────────────┐
   ▼                       ▼                    ▼
┌───────────────────┐ ┌──────────────────────────┐ ┌──────────────────────────┐
│ TB_TELEFONES_SAUDE │ │ TB_HORARIO_FUNIONAMENTO  │ │ TB_HORARIO_ATENDIMENTO   │
├────────────────────┤ ├──────────────────────────┤ ├──────────────────────────┤
│ (FK unidade)        │ │ (FK unidade)             │ │ (FK unidade)             │
│ saudeTelefones (str)│ │ DAY_OF_WEEK (map key)    │ │ DAY_OF_WEEK (map key)    │
│                      │ │ HORARIO_FUNCIONAMENTO   │ │ HORARIO_ATENDIMENTO      │
└────────────────────┘ └──────────────────────────┘ └──────────────────────────┘
```

> `TB_HORARIO_FUNIONAMENTO` (sem o "C") é o nome real da tabela no código-fonte (`@CollectionTable(name = "TB_HORARIO_FUNIONAMENTO")`) — mantido aqui de propósito, não é erro de digitação deste documento.

## Descrição das entidades

### 1. `UnidadeDeSaude` (`@Entity`, tabela `TB_UNIDADE_DE_SAUDE`)

| Campo | Tipo Java | Coluna/Anotação | Observação |
|---|---|---|---|
| `uuid` | `UUID` | `@Id @GeneratedValue(AUTO)` | PK |
| `nome` | `String` | `@NotBlank @Column(unique = true)` | Único em toda a tabela, independente do nível |
| `tipo` | `TipoUnidadeDeSaude` | `@Enumerated(EnumType.STRING)` | Discriminador de nível/tipo — ver seção 3 abaixo |
| `unidadeSuperior` | `UnidadeDeSaude` | `@ManyToOne(LAZY)` `@JoinColumn(name="unidade_superior_id", referencedColumnName="uuid")` | Autorreferência — implementa a hierarquia de 5 níveis (ADR-0002, estendida pela ADR-0013); sem `@OneToMany` inverso (não há navegação pai→filhos) |
| `supervisaoRegional` | `UnidadeDeSaude` | `@ManyToOne(LAZY)` `@JoinColumn(name="supervisao_regional_id", referencedColumnName="uuid", nullable=true)` | Vínculo lateral, **não hierárquico**, de supervisão técnica — só preenchido para o 5º nível (Unidade de Saúde), sempre apontando para uma unidade `REGIONAL`; não substitui `unidadeSuperior` (ADR-0009/ADR-0013) |
| `regiao` | `String` | coluna simples, nullable | Só setado pelo construtor do nível `REGIONAL` |
| `municipio` | `String` | coluna simples, nullable | Só setado pelo construtor do nível `MUNICIPAL` |
| `estado` | `String` | `@Column(name="estado_administracao")`, nullable | Só setado pelo construtor do nível `ESTADUAL` |
| `endereco` | `Endereco` | `@Embedded` | Ver detalhamento abaixo — colunas ficam na própria `TB_UNIDADE_DE_SAUDE` |
| `saudeTelefones` | `Set<String>` | `@ElementCollection` `@CollectionTable(name="TB_TELEFONES_SAUDE")` | Tabela filha 1:N |
| `email` | `String` | coluna simples | |
| `horarioFuncionamento` | `Map<DayOfWeek, String>` | `@ElementCollection` `@CollectionTable(name="TB_HORARIO_FUNIONAMENTO")`, chave `DAY_OF_WEEK`, valor `HORARIO_FUNCIONAMENTO` | Tabela filha, mapa dia-da-semana → texto livre |
| `horarioAtendimento` | `Map<DayOfWeek, String>` | `@ElementCollection` `@CollectionTable(name="TB_HORARIO_ATENDIMENTO")`, chave `DAY_OF_WEEK`, valor `HORARIO_ATENDIMENTO` | Idem, para horário de atendimento ao público |
| `ativo` | `boolean` | coluna simples | |
| `responsavelCpf` | `String` | coluna simples, nullable | CPF do responsável pela unidade, aceito mesmo antes do `Profissional` correspondente existir — vínculo fraco resolvido por reconciliação (ADR-0014) |
| `responsavel` | `Profissional` | `@ManyToOne(LAZY)` `@JoinColumn(name="responsavel_id", referencedColumnName="uuid", nullable=true)` | Preenchido quando a reconciliação por CPF encontra o `Profissional` |

**Relacionamentos**: autorreferenciado (`unidadeSuperior` → `UnidadeDeSaude`, hierarquia de 5
níveis); lateral não-hierárquico (`supervisaoRegional` → `UnidadeDeSaude`, ADR-0013); `responsavel`
→ `Profissional` (RH, ADR-0014); 3 tabelas-filhas via `@ElementCollection` (telefones, horário de
funcionamento, horário de atendimento).

### 2. `Endereco` (`@Embeddable`, sem tabela própria)

| Campo | Tipo | Observação |
|---|---|---|
| `cep`, `logradouro`, `numeroLogradouro`, `complemento`, `bairro`, `cidade`, `estado`, `ddd` | `String` | Todos embutidos diretamente nas colunas de `TB_UNIDADE_DE_SAUDE` — não é uma tabela nem entidade separada |

### 3. `TipoUnidadeDeSaude` (enum, `models/enuns/`)

12 valores, `@Enumerated(EnumType.STRING)` (persistido pelo nome, não pelo ordinal):

```java
public enum TipoUnidadeDeSaude {
    FEDERAL, ESTADUAL, MUNICIPAL, REGIONAL, UBS,
    HOSPITAL, UPA, LABORATORIO, CAPS, CENTRO_ESPECIALIDADES, CENTRO_REABILITACAO, POLICLINICA
}
```

Os 4 primeiros (`FEDERAL, ESTADUAL, MUNICIPAL, REGIONAL`) são os 4 níveis hierárquicos originais
das esferas de gestão do SUS (renomeados de Zero/Um/Dois/Três pela ADR-0009). `UBS` foi adicionado
junto com o 5º nível — "Unidade de Saúde" — pela **ADR-0013**. Os 6 seguintes (`UPA, LABORATORIO,
CAPS, CENTRO_ESPECIALIDADES, CENTRO_REABILITACAO, POLICLINICA`) foram adicionados pela **ADR-0031**,
todos sob o mesmo 5º nível que `UBS`/`HOSPITAL`.

Esses 8 valores do 5º nível (`UBS, HOSPITAL, UPA, LABORATORIO, CAPS, CENTRO_ESPECIALIDADES,
CENTRO_REABILITACAO, POLICLINICA`) são exatamente a lista retornada por
`UnidadeSaudeService.getTiposAceitos()` — o mecanismo (criado pela ADR-0013, generalizado pela
ADR-0031) que permite um único controller/service aceitar todos os tipos de "Unidade de Saúde" sem
precisar de um controller por tipo. Ver [ADR-0053](./0053-criterio-de-governanca-para-equipamentos-de-saude.md)
para o critério de quando um novo tipo de equipamento deve (ou não) virar um 9º valor aqui.

## Lacunas de integridade conhecidas

- Nada no schema ou na camada de serviço garante que `unidadeSuperior` aponte para o nível
  imediatamente superior correto (ex.: uma unidade `MUNICIPAL` só deveria poder referenciar uma
  `ESTADUAL` como superior) — ver ADR-0002.
- Nenhuma validação garante que os 8 valores do 5º nível sejam usados exclusivamente por unidades
  cujo `unidadeSuperior` é `MUNICIPAL` — a regra é aplicada pelos controllers (`getTiposAceitos()`),
  não pelo schema.

## Inventário do esquema atual

**Revisto em 2026-11-08.** Os diagramas acima descrevem só o núcleo e param em 2026-09-26. O esquema completo tem **310 tabelas**,
listadas aqui pela migração que criou cada uma (até a V84). A fonte de verdade são as migrações, em
`src/main/resources/db/migration`, e as ADRs de cada fatia.

| Migração | Tabelas |
|---|---|
| V1 | `tb_acesso_justificado`, `tb_acidente_trabalho`, `tb_adesao_beneficio`, `tb_administracao_medicamento`, `tb_afastamento`, `tb_agendamento`, `tb_ajuste_individual`, `tb_atendimento`, `tb_atribuicao_acesso`, `tb_avaliacao`, `tb_calculo_rescisao`, `tb_candidato`, `tb_capacidade_administrativa`, `tb_cargo`, `tb_categoria_salarial`, `tb_ciclo_avaliacao`, `tb_consulta`, `tb_dispensacao`, `tb_epi`, `tb_evento_auditoria`, `tb_evolucao_enfermagem`, `tb_exame_ocupacional`, `tb_folha_pagamento`, `tb_horario_atendimento`, `tb_horario_funionamento`, `tb_licenca`, `tb_lotacao`, `tb_lote`, `tb_medicamento`, `tb_movimentacao_farmacia`, `tb_necessidade_de_pessoal`, `tb_paciente`, `tb_papel`, `tb_papel_permissao`, `tb_participacao_treinamento`, `tb_perfil_administrativo`, `tb_perfil_por_tipo_unidade`, `tb_permissao`, `tb_procedimento`, `tb_processo_administrativo`, `tb_profissional`, `tb_redefinicao_senha`, `tb_registro_ponto`, `tb_regra_anuenio`, `tb_responsabilidade_administrativa`, `tb_setor`, `tb_tabela_salarial`, `tb_telefones_paciente`, `tb_telefones_profissional`, `tb_telefones_saude`, `tb_tipo_beneficio`, `tb_transferencia_farmacia`, `tb_treinamento`, `tb_triagem`, `tb_unidade_de_saude`, `tb_usuario`, `tb_vaga`, `tb_valor_beneficio` |
| V2 | `tb_evento_regulacao`, `tb_procedimento_regulado`, `tb_solicitacao_regulacao` |
| V3 | `tb_bloco_agenda`, `tb_bloqueio_agenda` |
| V4 | `tb_amostra_exame`, `tb_evento_exame`, `tb_exame_laboratorial`, `tb_item_pedido_exame`, `tb_pedido_exame`, `tb_resultado_exame` |
| V5 | `tb_alerta_auditoria` |
| V6 | `tb_evento_leito`, `tb_internacao`, `tb_leito` |
| V7 | `tb_evento_situacao_unidade`, `tb_horario_unidade` |
| V8 | `tb_equipe`, `tb_equipe_apoio`, `tb_membro_equipe` |
| V9 | `tb_turno_escala` |
| V11 | `tb_capacidade_assistencial`, `tb_perfil_operacional`, `tb_unidade_capacidade_assistencial`, `tb_unidade_perfil_operacional` |
| V12 | `tb_categoria_programa`, `tb_participacao_programa`, `tb_programa`, `tb_programa_condicao` |
| V13 | `tb_acompanhamento_programatico`, `tb_evento_acompanhamento` |
| V14 | `tb_acolhimento_saude_mental`, `tb_acompanhamento_saude_mental`, `tb_avaliacao_saude_mental`, `tb_referencia_saude_mental` |
| V15 | `tb_intervencao_prevista`, `tb_intervencao_saude_mental`, `tb_objetivo_terapeutico`, `tb_pessoa_rede_apoio`, `tb_projeto_terapeutico_singular`, `tb_tipo_intervencao_saude_mental` |
| V16 | `tb_compartilhamento_cuidado`, `tb_encaminhamento_saude_mental`, `tb_transferencia_cuidado` |
| V17 | `tb_avaliacao_risco`, `tb_comunicacao_ministerio_publico`, `tb_etapa_crise`, `tb_evento_crise` |
| V18 | `tb_agravo`, `tb_agravo_cid`, `tb_agravo_criterio`, `tb_classificacao_caso`, `tb_ficha_agravo`, `tb_investigacao_epidemiologica`, `tb_investigacao_exame`, `tb_notificacao`, `tb_notificacao_negativa` |
| V19 | `tb_area`, `tb_atribuicao_microarea`, `tb_microarea`, `tb_responsabilidade_territorial`, `tb_segmento_territorial`, `tb_territorio` |
| V20 | `tb_domicilio`, `tb_imovel`, `tb_membro_nucleo`, `tb_nucleo_familiar` |
| V21 | `tb_adscricao` |
| V23 | `tb_contato_epidemiologico` |
| V24 | `tb_medida_controle`, `tb_surto`, `tb_surto_microarea` |
| V25 | `tb_acao_programatica`, `tb_acao_programatica_participante` |
| V26 | `tb_campanha`, `tb_campanha_condicao`, `tb_local_campanha` |
| V27 | `tb_avaliacao_odontologica`, `tb_especialidade` |
| V28 | `tb_condicao_odontologica`, `tb_registro_odontograma` |
| V29 | `tb_complemento_odontologico`, `tb_complemento_odontologico_face`, `tb_procedimento_catalogo` |
| V30 | `tb_item_plano_tratamento`, `tb_item_plano_tratamento_face`, `tb_plano_tratamento` |
| V32 | `tb_etapa_tratamento_protetico`, `tb_tratamento_protetico` |
| V33 | `tb_acao_saude_bucal`, `tb_acao_saude_bucal_atividade`, `tb_avaliacao_bucal_coletiva` |
| V34 | `tb_meta_programatica` |
| V36 | `tb_esquema_vacinal`, `tb_imunobiologico`, `tb_registro_vacinacao` |
| V37 | `tb_camara_refrigeracao`, `tb_frasco_aberto`, `tb_leitura_temperatura`, `tb_lote_imunobiologico`, `tb_movimentacao_imunobiologico` |
| V38 | `tb_campanha_imunobiologico` |
| V39 | `tb_contraindicacao_vacinal`, `tb_notificacao_dose` |
| V40 | `tb_etapa_linha_cuidado`, `tb_linha_cuidado`, `tb_percurso_linha_cuidado`, `tb_registro_etapa_percurso` |
| V41 | `tb_configuracao_acolhimento_noturno`, `tb_permanencia_acolhimento_noturno` |
| V42 | `tb_encaminhamento_intersetorial`, `tb_instituicao_externa` |
| V43 | `tb_avaliacao_impacto`, `tb_avaliacao_impacto_unidade`, `tb_emergencia`, `tb_emergencia_microarea`, `tb_evento_emergencial`, `tb_mudanca_nivel_emergencia`, `tb_nivel_emergencia`, `tb_tipo_emergencia` |
| V44 | `tb_acao_plano`, `tb_ativacao_plano`, `tb_contato_plano`, `tb_criterio_ativacao`, `tb_plano_contingencia`, `tb_plano_contingencia_nivel`, `tb_plano_contingencia_unidade`, `tb_recurso_plano`, `tb_registro_ativacao_plano` |
| V45 | `tb_acao_emergencial`, `tb_ativacao_capacidade`, `tb_capacidade_emergencial`, `tb_mobilizacao` |
| V46 | `tb_desmobilizacao`, `tb_recomendacao_relatorio`, `tb_relatorio_pos_evento` |
| V47 | `tb_boletim_estrutura`, `tb_estrutura_temporaria`, `tb_evento_massa` |
| V48 | `tb_exposicao_ambiental`, `tb_exposicao_ambiental_unidade` |
| V49 | `tb_acao_controle_vetorial`, `tb_acao_controle_vetorial_microarea`, `tb_foco_vetorial`, `tb_vistoria_vetorial` |
| V50 | `tb_animal_zoonoses`, `tb_exame_animal`, `tb_observacao_animal`, `tb_vacinacao_antirrabica_animal` |
| V51 | `tb_amostra_agua`, `tb_ponto_coleta_agua`, `tb_risco_ambiental` |
| V52 | `tb_servico_setor`, `tb_servico_unidade`, `tb_tipo_servico` |
| V53 | `tb_ponto_operacional`, `tb_posicao_unidade_movel`, `tb_rota_dia`, `tb_rota_ponto`, `tb_rota_programada` |
| V54 | `tb_canal_comunicacao`, `tb_comunicado`, `tb_comunicado_canal`, `tb_conteudo_canal`, `tb_conteudo_educativo`, `tb_publicacao` |
| V56 | `tb_limite_leitos` |
| V57 | `tb_documento`, `tb_tipo_documento`, `tb_versao_documento` |
| V58 | `tb_competencia`, `tb_treinamento_competencia`, `tb_treinamento_conteudo` |
| V59 | `tb_campanha_comunicacao`, `tb_campanha_comunicacao_canal`, `tb_campanha_comunicacao_conteudo`, `tb_campanha_comunicacao_tema`, `tb_campanha_comunicacao_territorio`, `tb_material_educativo` |
| V60 | `tb_area_gestao`, `tb_titularidade_area_gestao` |
| V61 | `tb_ocorrencia_urgencia`, `tb_evento_urgencia` |
| V62 | `tb_material_estoque`, `tb_lote_material`, `tb_movimentacao_estoque`, `tb_estoque_minimo` |
| V63 | `tb_veiculo`, `tb_situacao_veiculo`, `tb_plantao_viatura`, `tb_tripulante_plantao` (e `veiculo_id` em `tb_ocorrencia_urgencia`) |
| V64 | `tb_oferta_assistencial`, `tb_cota_ppi` (e a cota e a extracota em `tb_solicitacao_regulacao`) |
| V65 | `tb_faixa_referencia_exame` (e os críticos em `tb_exame_laboratorial` e em `tb_resultado_exame`, com a descrição da faixa usada) |
| V66 | `tb_comunicacao_critico_exame`, `tb_entrega_resultado_exame` (e a ciência de quem pediu em `tb_resultado_exame`) |
| V67 | `tb_realizacao_exame_imagem` (e a categoria e a modalidade no catálogo, o serviço de imagem no pedido e o laudo em `tb_resultado_exame`) |
| V68 | `tb_emissao_laudo_exame`, `tb_emissao_laudo_exame_resultado`, `tb_insumo_exame`, `tb_consumo_sem_saldo` (e a assinatura em `tb_resultado_exame`) |
| V69 | `tb_solicitacao_leito`, `tb_evento_solicitacao_leito` (e a faixa etária e a reserva em `tb_leito`) |
| V70 | `tb_internacao_agendada`, `tb_evento_internacao_agendada`, `tb_censo_diario`, `tb_censo_diario_paciente` (e a reserva da eletiva em `tb_leito`) |
| V71 | `tb_laudo_aih`, `tb_evento_laudo_aih`, `tb_faixa_aih` (e o número da AIH em `tb_internacao`) |
| V72 | `tb_prescricao_hospitalar`, `tb_item_prescricao`, `tb_horario_prescricao`, `tb_checagem_prescricao` |
| V73 | `tb_ficha_aph`, `tb_ficha_aph_procedimento`, `tb_sinais_vitais_aph`, `tb_medicamento_aph`, `tb_adendo_ficha_aph` |
| V74 | `tb_requisicao_estoque`, `tb_item_requisicao_estoque`, `tb_transferencia_estoque`, `tb_item_transferencia_estoque`, `tb_inventario_estoque`, `tb_item_inventario_estoque` (e a capacidade Central de abastecimento) |
| V75 | `tb_solicitacao_transporte`, `tb_viagem_transporte`, `tb_embarque_viagem` |
| V76 | `tb_reserva_estoque`, `tb_cessao_veiculo` (e as ligações da mobilização ao estoque, a emergência na requisição e no consumo) |
| V77 | `tb_paciente_deslocado` (e o domínio de origem `EMERGENCIAS` no encaminhamento intersetorial) |
| V78 | sem tabela nova: o medicamento na mobilização e a emergência na solicitação regulada |
| V79 | `tb_acao_educativa`, `tb_acao_educativa_conteudo`, `tb_acao_educativa_material`, `tb_acao_educativa_microarea` |
| V80 | `tb_avaliacao_campanha` (e as metas em `tb_campanha_comunicacao`) |
| V81 | `tb_componente_rede_item`, `tb_referencia_rede` (e `fora_da_referencia` em `tb_solicitacao_regulacao`) |
| V82 | `tb_estabelecimento_regulado`, `tb_atividade_estabelecimento`, `tb_roteiro_inspecao`, `tb_versao_roteiro_inspecao`, `tb_denuncia_sanitaria`, `tb_inspecao_sanitaria`, `tb_inspecao_sanitaria_fiscal`, `tb_licenca_sanitaria`, `tb_irregularidade_sanitaria` (e o dono do documento sem unidade fora do paciente) |
| V83 | `tb_processo_sanitario`, `tb_processo_sanitario_fiscal`, `tb_penalidade_sanitaria`, `tb_medida_sanitaria`, `tb_termo_sanitario` (e `processo_id` na irregularidade, `suspensa_ate` na licença) |
| V84 | sem tabela nova: `VIGILANCIA` na origem do encaminhamento intersetorial e `CONSELHO_DE_DIREITOS` no tipo da instituição |
