-- Esquema da v1.4.0 (ADR-0090), gerado pelo Hibernate num banco vazio e extraído com pg_dump --schema-only.
-- Bancos que já existiam antes do Flyway são marcados nesta versão sem rodar este script
-- (spring.flyway.baseline-on-migrate, baseline-version=1); banco novo roda daqui.
-- Não editar: mudança de esquema entra numa migração nova (V3, V4...).

CREATE TABLE public.tb_acesso_justificado (
    uuid uuid NOT NULL,
    concedido_em timestamp(6) with time zone NOT NULL,
    expira_em timestamp(6) with time zone NOT NULL,
    justificativa character varying(1000) NOT NULL,
    motivo character varying(255) NOT NULL,
    usuario_cpf character varying(255) NOT NULL,
    paciente_uuid uuid NOT NULL,
    CONSTRAINT tb_acesso_justificado_motivo_check CHECK (((motivo)::text = ANY ((ARRAY['EMERGENCIA'::character varying, 'CONTINUIDADE_DO_CUIDADO'::character varying, 'REGULACAO_OU_ENCAMINHAMENTO'::character varying, 'OUTRO'::character varying])::text[])))
);

CREATE TABLE public.tb_acidente_trabalho (
    uuid uuid NOT NULL,
    cat_emitida boolean NOT NULL,
    cat_url character varying(255),
    data_hora timestamp(6) without time zone NOT NULL,
    descricao character varying(255),
    dias_afastamento integer,
    profissional_id uuid NOT NULL
);

CREATE TABLE public.tb_adesao_beneficio (
    uuid uuid NOT NULL,
    data_fim date,
    data_inicio date NOT NULL,
    quantidade_dependentes integer,
    profissional_id uuid NOT NULL,
    tipo_beneficio_id uuid NOT NULL
);

CREATE TABLE public.tb_administracao_medicamento (
    uuid uuid NOT NULL,
    data_hora timestamp(6) without time zone NOT NULL,
    dose character varying(255),
    motivo_nao_administracao character varying(255),
    motivo_retificacao character varying(1000),
    observacao character varying(1000),
    quantidade integer,
    registrado_em timestamp(6) with time zone,
    registrado_por_cpf character varying(255),
    situacao character varying(255) NOT NULL,
    via character varying(255),
    atendimento_id uuid NOT NULL,
    consulta_id uuid NOT NULL,
    lote_id uuid,
    medicamento_id uuid NOT NULL,
    profissional_id uuid NOT NULL,
    retificacao_de_id uuid,
    CONSTRAINT tb_administracao_medicamento_motivo_nao_administracao_check CHECK (((motivo_nao_administracao)::text = ANY ((ARRAY['RECUSA_DO_PACIENTE'::character varying, 'PACIENTE_AUSENTE'::character varying, 'MEDICAMENTO_EM_FALTA'::character varying, 'SUSPENSO_PELO_MEDICO'::character varying, 'OUTRO'::character varying])::text[]))),
    CONSTRAINT tb_administracao_medicamento_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['ADMINISTRADO'::character varying, 'NAO_ADMINISTRADO'::character varying])::text[]))),
    CONSTRAINT tb_administracao_medicamento_via_check CHECK (((via)::text = ANY ((ARRAY['ORAL'::character varying, 'SUBLINGUAL'::character varying, 'INTRAMUSCULAR'::character varying, 'INTRAVENOSA'::character varying, 'SUBCUTANEA'::character varying, 'TOPICA'::character varying, 'INALATORIA'::character varying, 'RETAL'::character varying, 'OUTRA'::character varying])::text[])))
);

CREATE TABLE public.tb_afastamento (
    uuid uuid NOT NULL,
    data_fim date NOT NULL,
    data_inicio date NOT NULL,
    observacao character varying(255),
    status character varying(255) NOT NULL,
    tipo character varying(255) NOT NULL,
    profissional_id uuid NOT NULL,
    CONSTRAINT tb_afastamento_status_check CHECK (((status)::text = ANY ((ARRAY['SOLICITADO'::character varying, 'APROVADO'::character varying, 'EM_ANDAMENTO'::character varying, 'CONCLUIDO'::character varying, 'CANCELADO'::character varying])::text[]))),
    CONSTRAINT tb_afastamento_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['FERIAS'::character varying, 'LICENCA_MEDICA'::character varying, 'LICENCA_PESSOAL'::character varying, 'OUTROS'::character varying])::text[])))
);

CREATE TABLE public.tb_agendamento (
    uuid uuid NOT NULL,
    data_hora timestamp(6) without time zone NOT NULL,
    observacao character varying(255),
    status character varying(255),
    tipo character varying(255),
    paciente_id uuid NOT NULL,
    profissional_id uuid NOT NULL,
    CONSTRAINT tb_agendamento_status_check CHECK (((status)::text = ANY ((ARRAY['AGENDADO'::character varying, 'CONFIRMADO'::character varying, 'REALIZADO'::character varying, 'CANCELADO'::character varying])::text[]))),
    CONSTRAINT tb_agendamento_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['CONSULTA'::character varying, 'PROCEDIMENTO'::character varying, 'RETORNO'::character varying])::text[])))
);

CREATE TABLE public.tb_ajuste_individual (
    uuid uuid NOT NULL,
    data_fim date,
    data_inicio date NOT NULL,
    motivo character varying(255) NOT NULL,
    referencia character varying(255),
    valor numeric(38,2) NOT NULL,
    profissional_id uuid NOT NULL,
    CONSTRAINT tb_ajuste_individual_motivo_check CHECK (((motivo)::text = ANY ((ARRAY['GRATIFICACAO_PESSOAL'::character varying, 'EQUIPARACAO_JUDICIAL'::character varying])::text[])))
);

CREATE TABLE public.tb_atendimento (
    uuid uuid NOT NULL,
    data_hora timestamp(6) without time zone NOT NULL,
    status character varying(255),
    tipo character varying(255),
    agendamento_id uuid,
    paciente_id uuid NOT NULL,
    profissional_id uuid NOT NULL,
    setor_id uuid,
    unidade_id uuid NOT NULL,
    CONSTRAINT tb_atendimento_status_check CHECK (((status)::text = ANY ((ARRAY['AGENDADO'::character varying, 'EM_ANDAMENTO'::character varying, 'CONCLUIDO'::character varying])::text[]))),
    CONSTRAINT tb_atendimento_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['CONSULTA'::character varying, 'URGENCIA'::character varying, 'INTERNACAO'::character varying])::text[])))
);

CREATE TABLE public.tb_atribuicao_acesso (
    uuid uuid NOT NULL,
    concedido_em timestamp(6) with time zone NOT NULL,
    concedido_por_cpf character varying(255),
    fim date,
    inicio date,
    motivo_revogacao character varying(1000),
    revogado_em timestamp(6) with time zone,
    revogado_por_cpf character varying(255),
    papel_id uuid NOT NULL,
    unidade_id uuid,
    usuario_id uuid NOT NULL
);

CREATE TABLE public.tb_avaliacao (
    uuid uuid NOT NULL,
    avaliador character varying(255) NOT NULL,
    nota numeric(38,2),
    observacao character varying(255),
    ciclo_avaliacao_id uuid NOT NULL,
    profissional_id uuid NOT NULL
);

CREATE TABLE public.tb_calculo_rescisao (
    uuid uuid NOT NULL,
    aviso_previo numeric(38,2),
    decimo_terceiro_proporcional numeric(38,2),
    documento_trct_url character varying(255),
    ferias_proporcionais numeric(38,2),
    ferias_vencidas numeric(38,2),
    multa_fgts numeric(38,2),
    tipo_desligamento character varying(255) NOT NULL,
    total numeric(38,2) NOT NULL,
    profissional_id uuid NOT NULL,
    CONSTRAINT tb_calculo_rescisao_tipo_desligamento_check CHECK (((tipo_desligamento)::text = ANY ((ARRAY['SEM_JUSTA_CAUSA'::character varying, 'COM_JUSTA_CAUSA'::character varying, 'PEDIDO_DEMISSAO'::character varying, 'TERMINO_CONTRATO'::character varying, 'APOSENTADORIA'::character varying, 'FALECIMENTO'::character varying])::text[])))
);

CREATE TABLE public.tb_candidato (
    uuid uuid NOT NULL,
    cpf character varying(255) NOT NULL,
    curriculo_url character varying(255),
    nome character varying(255) NOT NULL,
    status character varying(255) NOT NULL,
    vaga_id uuid NOT NULL,
    CONSTRAINT tb_candidato_status_check CHECK (((status)::text = ANY ((ARRAY['INSCRITO'::character varying, 'TRIAGEM'::character varying, 'ENTREVISTA'::character varying, 'APROVADO'::character varying, 'REPROVADO'::character varying])::text[])))
);

CREATE TABLE public.tb_capacidade_administrativa (
    uuid uuid NOT NULL,
    ativo boolean NOT NULL,
    codigo character varying(255) NOT NULL,
    descricao text,
    nome character varying(255) NOT NULL
);

CREATE TABLE public.tb_cargo (
    uuid uuid NOT NULL,
    descricao text,
    nome character varying(255) NOT NULL,
    categoria_salarial_id uuid NOT NULL
);

CREATE TABLE public.tb_categoria_salarial (
    uuid uuid NOT NULL,
    convencao_coletiva character varying(255),
    nome character varying(255) NOT NULL
);

CREATE TABLE public.tb_ciclo_avaliacao (
    uuid uuid NOT NULL,
    data_fim date NOT NULL,
    data_inicio date NOT NULL,
    nome character varying(255) NOT NULL
);

CREATE TABLE public.tb_consulta (
    uuid uuid NOT NULL,
    data_hora timestamp(6) without time zone NOT NULL,
    diagnostico character varying(255),
    exames_solicitados character varying(255),
    motivo_retificacao character varying(1000),
    queixa_principal character varying(255),
    receituario character varying(255),
    registrado_em timestamp(6) with time zone,
    registrado_por_cpf character varying(255),
    retorno date,
    tipo_consulta character varying(255),
    atendimento_id uuid NOT NULL,
    profissional_id uuid NOT NULL,
    retificacao_de_id uuid,
    CONSTRAINT tb_consulta_tipo_consulta_check CHECK (((tipo_consulta)::text = ANY ((ARRAY['PRIMEIRA'::character varying, 'RETORNO'::character varying, 'URGENCIA'::character varying])::text[])))
);

CREATE TABLE public.tb_dispensacao (
    uuid uuid NOT NULL,
    data_hora timestamp(6) without time zone NOT NULL,
    quantidade integer NOT NULL,
    consulta_id uuid,
    lote_id uuid NOT NULL,
    paciente_id uuid NOT NULL,
    profissional_id uuid NOT NULL
);

CREATE TABLE public.tb_epi (
    uuid uuid NOT NULL,
    data_devolucao date,
    data_entrega date NOT NULL,
    numeroca character varying(255),
    tipo character varying(255) NOT NULL,
    profissional_id uuid NOT NULL
);

CREATE TABLE public.tb_evento_auditoria (
    uuid uuid NOT NULL,
    acao character varying(255) NOT NULL,
    detalhe character varying(500),
    metodo character varying(255) NOT NULL,
    ocorrido_em timestamp(6) with time zone NOT NULL,
    origem_ip character varying(255),
    paciente_id uuid,
    recurso character varying(255) NOT NULL,
    registro_id uuid,
    resultado character varying(255) NOT NULL,
    rota character varying(255) NOT NULL,
    status_http integer,
    unidade_id uuid,
    usuario_cpf character varying(255),
    CONSTRAINT tb_evento_auditoria_acao_check CHECK (((acao)::text = ANY ((ARRAY['LOGIN'::character varying, 'TROCA_DE_SENHA'::character varying, 'RECUPERACAO_DE_SENHA'::character varying, 'LEITURA'::character varying, 'CRIACAO'::character varying, 'ALTERACAO'::character varying, 'RETIFICACAO'::character varying, 'REVOGACAO'::character varying, 'ACESSO_JUSTIFICADO'::character varying, 'EXCLUSAO'::character varying])::text[]))),
    CONSTRAINT tb_evento_auditoria_resultado_check CHECK (((resultado)::text = ANY ((ARRAY['PERMITIDO'::character varying, 'NEGADO'::character varying])::text[])))
);

CREATE TABLE public.tb_evolucao_enfermagem (
    uuid uuid NOT NULL,
    data_hora timestamp(6) without time zone NOT NULL,
    descricao character varying(255) NOT NULL,
    motivo_retificacao character varying(1000),
    registrado_em timestamp(6) with time zone,
    registrado_por_cpf character varying(255),
    atendimento_id uuid NOT NULL,
    profissional_id uuid NOT NULL,
    retificacao_de_id uuid
);

CREATE TABLE public.tb_exame_ocupacional (
    uuid uuid NOT NULL,
    aso_url character varying(255),
    data_realizacao date NOT NULL,
    data_validade date,
    resultado character varying(255) NOT NULL,
    tipo character varying(255) NOT NULL,
    profissional_id uuid NOT NULL,
    CONSTRAINT tb_exame_ocupacional_resultado_check CHECK (((resultado)::text = ANY ((ARRAY['APTO'::character varying, 'INAPTO'::character varying, 'APTO_COM_RESTRICAO'::character varying])::text[]))),
    CONSTRAINT tb_exame_ocupacional_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['ADMISSIONAL'::character varying, 'PERIODICO'::character varying, 'DEMISSIONAL'::character varying, 'RETORNO'::character varying, 'MUDANCA_FUNCAO'::character varying])::text[])))
);

CREATE TABLE public.tb_folha_pagamento (
    uuid uuid NOT NULL,
    competencia character varying(255) NOT NULL,
    descontos numeric(38,2) NOT NULL,
    encargos numeric(38,2) NOT NULL,
    proventos numeric(38,2) NOT NULL,
    total numeric(38,2) NOT NULL,
    profissional_id uuid NOT NULL
);

CREATE TABLE public.tb_horario_atendimento (
    unidade_de_saude_uuid uuid NOT NULL,
    horario_atendimento character varying(255),
    day_of_week smallint NOT NULL,
    CONSTRAINT tb_horario_atendimento_day_of_week_check CHECK (((day_of_week >= 0) AND (day_of_week <= 6)))
);

CREATE TABLE public.tb_horario_funionamento (
    unidade_de_saude_uuid uuid NOT NULL,
    horario_funcionamento character varying(255),
    day_of_week smallint NOT NULL,
    CONSTRAINT tb_horario_funionamento_day_of_week_check CHECK (((day_of_week >= 0) AND (day_of_week <= 6)))
);

CREATE TABLE public.tb_licenca (
    uuid uuid NOT NULL,
    documento_url character varying(255),
    responsavel_pagamento character varying(255) NOT NULL,
    tipo_legal character varying(255) NOT NULL,
    afastamento_id uuid NOT NULL,
    CONSTRAINT tb_licenca_responsavel_pagamento_check CHECK (((responsavel_pagamento)::text = ANY ((ARRAY['EMPRESA'::character varying, 'INSS'::character varying, 'MISTO'::character varying])::text[]))),
    CONSTRAINT tb_licenca_tipo_legal_check CHECK (((tipo_legal)::text = ANY ((ARRAY['MATERNIDADE'::character varying, 'PATERNIDADE'::character varying, 'DOENCA'::character varying, 'ACIDENTE_DE_TRABALHO'::character varying, 'FALECIMENTO'::character varying, 'CASAMENTO'::character varying, 'OUTROS'::character varying])::text[])))
);

CREATE TABLE public.tb_lotacao (
    uuid uuid NOT NULL,
    data_fim date,
    data_inicio date NOT NULL,
    jornada_semanal_horas integer,
    motivo character varying(255),
    cargo_id uuid NOT NULL,
    profissional_id uuid NOT NULL,
    unidade_id uuid NOT NULL
);

CREATE TABLE public.tb_lote (
    uuid uuid NOT NULL,
    incorporado_em timestamp(6) with time zone,
    numero_lote character varying(255) NOT NULL,
    quantidade integer NOT NULL,
    validade date NOT NULL,
    lote_incorporador_id uuid,
    medicamento_id uuid NOT NULL,
    unidade_id uuid NOT NULL
);

CREATE TABLE public.tb_medicamento (
    uuid uuid NOT NULL,
    apresentacao character varying(255),
    ativo boolean NOT NULL,
    codigo character varying(255),
    nome character varying(255) NOT NULL,
    principio_ativo character varying(255)
);

CREATE TABLE public.tb_movimentacao_farmacia (
    uuid uuid NOT NULL,
    justificativa character varying(1000),
    motivo_perda character varying(255),
    quantidade integer NOT NULL,
    registrado_em timestamp(6) with time zone NOT NULL,
    registrado_por_cpf character varying(255),
    saldo_apos integer NOT NULL,
    tipo character varying(255) NOT NULL,
    administracao_id uuid,
    dispensacao_id uuid,
    lote_id uuid NOT NULL,
    profissional_id uuid,
    transferencia_id uuid,
    CONSTRAINT tb_movimentacao_farmacia_motivo_perda_check CHECK (((motivo_perda)::text = ANY ((ARRAY['VENCIMENTO'::character varying, 'AVARIA'::character varying, 'EXTRAVIO'::character varying, 'OUTRO'::character varying])::text[]))),
    CONSTRAINT tb_movimentacao_farmacia_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['SALDO_INICIAL'::character varying, 'ENTRADA'::character varying, 'DISPENSACAO'::character varying, 'PERDA'::character varying, 'AJUSTE_INVENTARIO'::character varying, 'TRANSFERENCIA_SAIDA'::character varying, 'TRANSFERENCIA_ENTRADA'::character varying, 'TRANSFERENCIA_ESTORNO'::character varying, 'INCORPORACAO_SAIDA'::character varying, 'INCORPORACAO_ENTRADA'::character varying, 'ADMINISTRACAO'::character varying, 'ADMINISTRACAO_ESTORNO'::character varying])::text[])))
);

CREATE TABLE public.tb_necessidade_de_pessoal (
    uuid uuid NOT NULL,
    competencias_necessarias character varying(255),
    data_registro date NOT NULL,
    jornada_semanal_horas integer NOT NULL,
    justificativa character varying(255),
    quantidade integer NOT NULL,
    cargo_id uuid NOT NULL,
    setor_id uuid,
    unidade_id uuid NOT NULL,
    vaga_associada_id uuid
);

CREATE TABLE public.tb_paciente (
    uuid uuid NOT NULL,
    ativo boolean NOT NULL,
    cartao_sus character varying(255),
    cpf character varying(255),
    data_cadastro timestamp(6) with time zone,
    data_nascimento date,
    email character varying(255),
    bairro character varying(255),
    cep character varying(255),
    cidade character varying(255),
    complemento character varying(255),
    ddd character varying(255),
    estado character varying(255),
    logradouro character varying(255),
    numero_logradouro character varying(255),
    nome character varying(255) NOT NULL,
    sexo character varying(255),
    CONSTRAINT tb_paciente_sexo_check CHECK (((sexo)::text = ANY ((ARRAY['MASCULINO'::character varying, 'FEMININO'::character varying, 'IGNORADO'::character varying])::text[])))
);

CREATE TABLE public.tb_papel (
    uuid uuid NOT NULL,
    ativo boolean NOT NULL,
    codigo character varying(255) NOT NULL,
    descricao character varying(1000),
    nome character varying(255) NOT NULL
);

CREATE TABLE public.tb_papel_permissao (
    papel_id uuid NOT NULL,
    permissao_id uuid NOT NULL
);

CREATE TABLE public.tb_participacao_treinamento (
    uuid uuid NOT NULL,
    certificado_url character varying(255),
    data_conclusao date NOT NULL,
    data_validade date,
    profissional_id uuid NOT NULL,
    treinamento_id uuid NOT NULL
);

CREATE TABLE public.tb_perfil_administrativo (
    uuid uuid NOT NULL,
    ativo boolean NOT NULL,
    codigo character varying(255) NOT NULL,
    descricao text,
    nome character varying(255) NOT NULL
);

CREATE TABLE public.tb_perfil_por_tipo_unidade (
    uuid uuid NOT NULL,
    tipo character varying(255) NOT NULL,
    perfil_administrativo_id uuid NOT NULL,
    CONSTRAINT tb_perfil_por_tipo_unidade_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['FEDERAL'::character varying, 'ESTADUAL'::character varying, 'MUNICIPAL'::character varying, 'REGIONAL'::character varying, 'UBS'::character varying, 'HOSPITAL'::character varying, 'UPA'::character varying, 'LABORATORIO'::character varying, 'CAPS'::character varying, 'CENTRO_ESPECIALIDADES'::character varying, 'CENTRO_REABILITACAO'::character varying, 'POLICLINICA'::character varying])::text[])))
);

CREATE TABLE public.tb_permissao (
    uuid uuid NOT NULL,
    codigo character varying(255) NOT NULL,
    descricao character varying(255) NOT NULL,
    dimensao character varying(255) NOT NULL,
    CONSTRAINT tb_permissao_dimensao_check CHECK (((dimensao)::text = ANY ((ARRAY['ADMINISTRACAO_DO_SISTEMA'::character varying, 'OPERACAO'::character varying, 'ACESSO_AO_DADO_DE_SAUDE'::character varying])::text[])))
);

CREATE TABLE public.tb_procedimento (
    uuid uuid NOT NULL,
    data_prevista timestamp(6) without time zone,
    data_realizacao timestamp(6) without time zone NOT NULL,
    descricao character varying(255),
    justificativa_status character varying(1000),
    motivo_retificacao character varying(1000),
    registrado_em timestamp(6) with time zone,
    registrado_por_cpf character varying(255),
    status character varying(255),
    status_alterado_em timestamp(6) with time zone,
    status_alterado_por_cpf character varying(255),
    tipo character varying(255) NOT NULL,
    consulta_id uuid NOT NULL,
    profissional_id uuid NOT NULL,
    profissional_status_id uuid,
    retificacao_de_id uuid,
    CONSTRAINT tb_procedimento_status_check CHECK (((status)::text = ANY ((ARRAY['AGENDADO'::character varying, 'REALIZADO'::character varying, 'CANCELADO'::character varying])::text[])))
);

CREATE TABLE public.tb_processo_administrativo (
    uuid uuid NOT NULL,
    ativo boolean NOT NULL,
    codigo character varying(255) NOT NULL,
    descricao text,
    nome character varying(255) NOT NULL,
    capacidade_administrativa_id uuid NOT NULL
);

CREATE TABLE public.tb_profissional (
    uuid uuid NOT NULL,
    ativo boolean NOT NULL,
    conselho_classe character varying(255),
    cpf character varying(255) NOT NULL,
    data_admissao date,
    data_desligamento date,
    email character varying(255),
    bairro character varying(255),
    cep character varying(255),
    cidade character varying(255),
    complemento character varying(255),
    ddd character varying(255),
    estado character varying(255),
    logradouro character varying(255),
    numero_logradouro character varying(255),
    matricula character varying(255),
    nome character varying(255),
    numero_conselho character varying(255)
);

CREATE TABLE public.tb_redefinicao_senha (
    uuid uuid NOT NULL,
    criada_em timestamp(6) with time zone NOT NULL,
    expira_em timestamp(6) with time zone NOT NULL,
    token_hash character varying(64) NOT NULL,
    usada_em timestamp(6) with time zone,
    usuario_uuid uuid NOT NULL
);

CREATE TABLE public.tb_registro_ponto (
    uuid uuid NOT NULL,
    data_hora timestamp(6) without time zone NOT NULL,
    data_hora_proposta timestamp(6) without time zone,
    justificativa_correcao character varying(255),
    origem character varying(255),
    tipo character varying(255) NOT NULL,
    tipo_proposto character varying(255),
    profissional_id uuid NOT NULL,
    CONSTRAINT tb_registro_ponto_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['ENTRADA'::character varying, 'SAIDA'::character varying, 'INICIO_INTERVALO'::character varying, 'FIM_INTERVALO'::character varying])::text[]))),
    CONSTRAINT tb_registro_ponto_tipo_proposto_check CHECK (((tipo_proposto)::text = ANY ((ARRAY['ENTRADA'::character varying, 'SAIDA'::character varying, 'INICIO_INTERVALO'::character varying, 'FIM_INTERVALO'::character varying])::text[])))
);

CREATE TABLE public.tb_regra_anuenio (
    uuid uuid NOT NULL,
    percentual_por_ano numeric(38,2) NOT NULL,
    teto_anos integer,
    categoria_salarial_id uuid NOT NULL
);

CREATE TABLE public.tb_responsabilidade_administrativa (
    uuid uuid NOT NULL,
    data_fim date,
    data_inicio date NOT NULL,
    descricao character varying(255),
    tipo character varying(255) NOT NULL,
    profissional_id uuid NOT NULL,
    setor_id uuid NOT NULL
);

CREATE TABLE public.tb_setor (
    uuid uuid NOT NULL,
    ativo boolean NOT NULL,
    codigo character varying(255) NOT NULL,
    nome character varying(255) NOT NULL,
    tipo character varying(255) NOT NULL,
    responsavel_id uuid,
    unidade_id uuid NOT NULL,
    CONSTRAINT tb_setor_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['ADMINISTRATIVO'::character varying, 'ASSISTENCIAL'::character varying, 'APOIO'::character varying, 'TECNICO'::character varying])::text[])))
);

CREATE TABLE public.tb_tabela_salarial (
    uuid uuid NOT NULL,
    data_vigencia date NOT NULL,
    motivo character varying(255) NOT NULL,
    valor_base numeric(38,2) NOT NULL,
    cargo_id uuid NOT NULL,
    CONSTRAINT tb_tabela_salarial_motivo_check CHECK (((motivo)::text = ANY ((ARRAY['DISSIDIO'::character varying, 'REVISAO_PLANO_CARGOS_SALARIOS'::character varying])::text[])))
);

CREATE TABLE public.tb_telefones_paciente (
    paciente_uuid uuid NOT NULL,
    telefones character varying(255)
);

CREATE TABLE public.tb_telefones_profissional (
    profissional_uuid uuid NOT NULL,
    telefones character varying(255)
);

CREATE TABLE public.tb_telefones_saude (
    unidade_de_saude_uuid uuid NOT NULL,
    saude_telefones character varying(255)
);

CREATE TABLE public.tb_tipo_beneficio (
    uuid uuid NOT NULL,
    custeio character varying(255) NOT NULL,
    nome character varying(255) NOT NULL,
    CONSTRAINT tb_tipo_beneficio_custeio_check CHECK (((custeio)::text = ANY ((ARRAY['EMPRESA'::character varying, 'COMPARTILHADO'::character varying, 'PROFISSIONAL'::character varying])::text[])))
);

CREATE TABLE public.tb_transferencia_farmacia (
    uuid uuid NOT NULL,
    cancelado_em timestamp(6) with time zone,
    cancelado_por_cpf character varying(255),
    justificativa_divergencia character varying(1000),
    motivo_cancelamento character varying(1000),
    motivo_divergencia character varying(255),
    observacao character varying(1000),
    quantidade integer NOT NULL,
    quantidade_recebida integer,
    recebido_em timestamp(6) with time zone,
    recebido_por_cpf character varying(255),
    registrado_em timestamp(6) with time zone NOT NULL,
    registrado_por_cpf character varying(255),
    status character varying(255),
    lote_destino_id uuid,
    lote_origem_id uuid NOT NULL,
    profissional_id uuid NOT NULL,
    profissional_cancelamento_id uuid,
    profissional_recebimento_id uuid,
    unidade_destino_id uuid,
    CONSTRAINT tb_transferencia_farmacia_motivo_divergencia_check CHECK (((motivo_divergencia)::text = ANY ((ARRAY['AVARIA'::character varying, 'EXTRAVIO'::character varying, 'OUTRO'::character varying])::text[]))),
    CONSTRAINT tb_transferencia_farmacia_status_check CHECK (((status)::text = ANY ((ARRAY['EM_TRANSITO'::character varying, 'RECEBIDA'::character varying, 'RECEBIDA_COM_DIVERGENCIA'::character varying, 'CANCELADA'::character varying])::text[])))
);

CREATE TABLE public.tb_treinamento (
    uuid uuid NOT NULL,
    carga_horaria integer,
    nome character varying(255) NOT NULL,
    obrigatorio boolean NOT NULL,
    validade_meses integer
);

CREATE TABLE public.tb_triagem (
    uuid uuid NOT NULL,
    classificacao_risco character varying(255) NOT NULL,
    data_hora timestamp(6) without time zone NOT NULL,
    frequencia_cardiaca integer,
    motivo_retificacao character varying(1000),
    observacoes character varying(255),
    peso double precision,
    pressao_arterial character varying(255),
    registrado_em timestamp(6) with time zone,
    registrado_por_cpf character varying(255),
    saturacao_oxigenio double precision,
    temperatura double precision,
    atendimento_id uuid NOT NULL,
    profissional_id uuid NOT NULL,
    retificacao_de_id uuid,
    CONSTRAINT tb_triagem_classificacao_risco_check CHECK (((classificacao_risco)::text = ANY ((ARRAY['AZUL'::character varying, 'VERDE'::character varying, 'AMARELO'::character varying, 'LARANJA'::character varying, 'VERMELHO'::character varying])::text[])))
);

CREATE TABLE public.tb_unidade_de_saude (
    uuid uuid NOT NULL,
    ativo boolean NOT NULL,
    email character varying(255),
    bairro character varying(255),
    cep character varying(255),
    cidade character varying(255),
    complemento character varying(255),
    ddd character varying(255),
    estado character varying(255),
    logradouro character varying(255),
    numero_logradouro character varying(255),
    estado_administracao character varying(255),
    municipio character varying(255),
    nome character varying(255) NOT NULL,
    regiao character varying(255),
    responsavel_cpf character varying(255),
    tipo character varying(255),
    responsavel_id uuid,
    supervisao_regional_id uuid,
    unidade_superior_id uuid,
    CONSTRAINT tb_unidade_de_saude_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['FEDERAL'::character varying, 'ESTADUAL'::character varying, 'MUNICIPAL'::character varying, 'REGIONAL'::character varying, 'UBS'::character varying, 'HOSPITAL'::character varying, 'UPA'::character varying, 'LABORATORIO'::character varying, 'CAPS'::character varying, 'CENTRO_ESPECIALIDADES'::character varying, 'CENTRO_REABILITACAO'::character varying, 'POLICLINICA'::character varying])::text[])))
);

CREATE TABLE public.tb_usuario (
    uuid uuid NOT NULL,
    ativo boolean NOT NULL,
    bloqueado_ate timestamp(6) with time zone,
    cpf character varying(255) NOT NULL,
    nome character varying(255) NOT NULL,
    senha_alterada_em timestamp(6) with time zone,
    senha_hash character varying(255) NOT NULL,
    tentativas_falhas integer NOT NULL,
    trocar_senha boolean DEFAULT false NOT NULL,
    ultimo_acesso_em timestamp(6) with time zone,
    versao_sessao integer DEFAULT 0 NOT NULL
);

CREATE TABLE public.tb_vaga (
    uuid uuid NOT NULL,
    quantidade integer NOT NULL,
    status character varying(255) NOT NULL,
    cargo_id uuid NOT NULL,
    unidade_id uuid NOT NULL,
    CONSTRAINT tb_vaga_status_check CHECK (((status)::text = ANY ((ARRAY['ABERTA'::character varying, 'EM_ANDAMENTO'::character varying, 'FECHADA'::character varying, 'CANCELADA'::character varying])::text[])))
);

CREATE TABLE public.tb_valor_beneficio (
    uuid uuid NOT NULL,
    data_vigencia date NOT NULL,
    motivo character varying(255),
    valor numeric(38,2) NOT NULL,
    tipo_beneficio_id uuid NOT NULL
);

ALTER TABLE ONLY public.tb_redefinicao_senha
    ADD CONSTRAINT ix_redefinicao_senha_token UNIQUE (token_hash);

ALTER TABLE ONLY public.tb_acesso_justificado
    ADD CONSTRAINT tb_acesso_justificado_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_acidente_trabalho
    ADD CONSTRAINT tb_acidente_trabalho_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_adesao_beneficio
    ADD CONSTRAINT tb_adesao_beneficio_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_administracao_medicamento
    ADD CONSTRAINT tb_administracao_medicamento_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_afastamento
    ADD CONSTRAINT tb_afastamento_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_agendamento
    ADD CONSTRAINT tb_agendamento_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_ajuste_individual
    ADD CONSTRAINT tb_ajuste_individual_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_atendimento
    ADD CONSTRAINT tb_atendimento_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_atribuicao_acesso
    ADD CONSTRAINT tb_atribuicao_acesso_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_avaliacao
    ADD CONSTRAINT tb_avaliacao_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_calculo_rescisao
    ADD CONSTRAINT tb_calculo_rescisao_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_candidato
    ADD CONSTRAINT tb_candidato_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_capacidade_administrativa
    ADD CONSTRAINT tb_capacidade_administrativa_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_cargo
    ADD CONSTRAINT tb_cargo_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_categoria_salarial
    ADD CONSTRAINT tb_categoria_salarial_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_ciclo_avaliacao
    ADD CONSTRAINT tb_ciclo_avaliacao_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_consulta
    ADD CONSTRAINT tb_consulta_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_dispensacao
    ADD CONSTRAINT tb_dispensacao_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_epi
    ADD CONSTRAINT tb_epi_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_evento_auditoria
    ADD CONSTRAINT tb_evento_auditoria_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_evolucao_enfermagem
    ADD CONSTRAINT tb_evolucao_enfermagem_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_exame_ocupacional
    ADD CONSTRAINT tb_exame_ocupacional_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_folha_pagamento
    ADD CONSTRAINT tb_folha_pagamento_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_horario_atendimento
    ADD CONSTRAINT tb_horario_atendimento_pkey PRIMARY KEY (unidade_de_saude_uuid, day_of_week);

ALTER TABLE ONLY public.tb_horario_funionamento
    ADD CONSTRAINT tb_horario_funionamento_pkey PRIMARY KEY (unidade_de_saude_uuid, day_of_week);

ALTER TABLE ONLY public.tb_licenca
    ADD CONSTRAINT tb_licenca_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_lotacao
    ADD CONSTRAINT tb_lotacao_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_lote
    ADD CONSTRAINT tb_lote_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_medicamento
    ADD CONSTRAINT tb_medicamento_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_movimentacao_farmacia
    ADD CONSTRAINT tb_movimentacao_farmacia_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_necessidade_de_pessoal
    ADD CONSTRAINT tb_necessidade_de_pessoal_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_paciente
    ADD CONSTRAINT tb_paciente_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_papel_permissao
    ADD CONSTRAINT tb_papel_permissao_pkey PRIMARY KEY (papel_id, permissao_id);

ALTER TABLE ONLY public.tb_papel
    ADD CONSTRAINT tb_papel_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_participacao_treinamento
    ADD CONSTRAINT tb_participacao_treinamento_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_perfil_administrativo
    ADD CONSTRAINT tb_perfil_administrativo_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_perfil_por_tipo_unidade
    ADD CONSTRAINT tb_perfil_por_tipo_unidade_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_permissao
    ADD CONSTRAINT tb_permissao_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_procedimento
    ADD CONSTRAINT tb_procedimento_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_processo_administrativo
    ADD CONSTRAINT tb_processo_administrativo_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_profissional
    ADD CONSTRAINT tb_profissional_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_redefinicao_senha
    ADD CONSTRAINT tb_redefinicao_senha_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_registro_ponto
    ADD CONSTRAINT tb_registro_ponto_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_regra_anuenio
    ADD CONSTRAINT tb_regra_anuenio_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_responsabilidade_administrativa
    ADD CONSTRAINT tb_responsabilidade_administrativa_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_setor
    ADD CONSTRAINT tb_setor_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_tabela_salarial
    ADD CONSTRAINT tb_tabela_salarial_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_tipo_beneficio
    ADD CONSTRAINT tb_tipo_beneficio_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_transferencia_farmacia
    ADD CONSTRAINT tb_transferencia_farmacia_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_treinamento
    ADD CONSTRAINT tb_treinamento_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_triagem
    ADD CONSTRAINT tb_triagem_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_unidade_de_saude
    ADD CONSTRAINT tb_unidade_de_saude_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_usuario
    ADD CONSTRAINT tb_usuario_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_vaga
    ADD CONSTRAINT tb_vaga_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_valor_beneficio
    ADD CONSTRAINT tb_valor_beneficio_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_folha_pagamento
    ADD CONSTRAINT uk1510ipp37veon7oght9wb99go UNIQUE (profissional_id, competencia);

ALTER TABLE ONLY public.tb_permissao
    ADD CONSTRAINT uk1uq3cg2rbrpk3ykinkkju2gaw UNIQUE (codigo);

ALTER TABLE ONLY public.tb_perfil_por_tipo_unidade
    ADD CONSTRAINT uk3fdds2fpt1lvdol9m9maif8lg UNIQUE (tipo);

ALTER TABLE ONLY public.tb_usuario
    ADD CONSTRAINT uk594wib8ansybtilla48x7vdld UNIQUE (cpf);

ALTER TABLE ONLY public.tb_licenca
    ADD CONSTRAINT uk77spsp32vxksmh78541b45j UNIQUE (afastamento_id);

ALTER TABLE ONLY public.tb_unidade_de_saude
    ADD CONSTRAINT uk9oww460f4jj842qpkujqbpq9l UNIQUE (nome);

ALTER TABLE ONLY public.tb_capacidade_administrativa
    ADD CONSTRAINT ukbtq30x5d67s1oom1fcdfnd8vq UNIQUE (codigo);

ALTER TABLE ONLY public.tb_perfil_administrativo
    ADD CONSTRAINT ukdcodsltmigsxqnrlab7hn7gpb UNIQUE (codigo);

ALTER TABLE ONLY public.tb_treinamento
    ADD CONSTRAINT ukejcrho9df5kjdch0kkox2dxsv UNIQUE (nome);

ALTER TABLE ONLY public.tb_profissional
    ADD CONSTRAINT ukhxx9tym09dyrh09tp4ahb0fct UNIQUE (matricula);

ALTER TABLE ONLY public.tb_tipo_beneficio
    ADD CONSTRAINT ukmptcgao1w6ca5luqlillwdm36 UNIQUE (nome);

ALTER TABLE ONLY public.tb_categoria_salarial
    ADD CONSTRAINT ukoingkrsawy2d945ylkl228fka UNIQUE (nome);

ALTER TABLE ONLY public.tb_regra_anuenio
    ADD CONSTRAINT ukreonsanoseagymjp1sev2j6wf UNIQUE (categoria_salarial_id);

ALTER TABLE ONLY public.tb_papel
    ADD CONSTRAINT uktj0hypg4k4b35ktfshc8ugt8y UNIQUE (codigo);

ALTER TABLE ONLY public.tb_ciclo_avaliacao
    ADD CONSTRAINT uktn5hk9kh7wfd6ab9xlyahtdkp UNIQUE (nome);

CREATE INDEX ix_acesso_justificado_usuario_paciente ON public.tb_acesso_justificado USING btree (usuario_cpf, paciente_uuid, expira_em);

CREATE INDEX ix_auditoria_paciente ON public.tb_evento_auditoria USING btree (paciente_id, ocorrido_em);

CREATE INDEX ix_auditoria_registro ON public.tb_evento_auditoria USING btree (registro_id);

CREATE INDEX ix_auditoria_usuario ON public.tb_evento_auditoria USING btree (usuario_cpf, ocorrido_em);

CREATE INDEX ix_redefinicao_senha_usuario ON public.tb_redefinicao_senha USING btree (usuario_uuid, criada_em);

CREATE UNIQUE INDEX uk_lote_remessa_ativa_por_unidade ON public.tb_lote USING btree (medicamento_id, unidade_id, numero_lote, validade) WHERE (lote_incorporador_id IS NULL);

ALTER TABLE ONLY public.tb_lotacao
    ADD CONSTRAINT fk14q9e037t37kfv37ysan4v1xi FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);

ALTER TABLE ONLY public.tb_movimentacao_farmacia
    ADD CONSTRAINT fk1rmmmevblk232qs3wnullg903 FOREIGN KEY (administracao_id) REFERENCES public.tb_administracao_medicamento(uuid);

ALTER TABLE ONLY public.tb_dispensacao
    ADD CONSTRAINT fk1vbgyr10q8m1r45pww2470uqg FOREIGN KEY (lote_id) REFERENCES public.tb_lote(uuid);

ALTER TABLE ONLY public.tb_consulta
    ADD CONSTRAINT fk265gsmx8m6u2arf6qk6gc8efk FOREIGN KEY (atendimento_id) REFERENCES public.tb_atendimento(uuid);

ALTER TABLE ONLY public.tb_valor_beneficio
    ADD CONSTRAINT fk2byp41eai3tg1gacxqorh65y5 FOREIGN KEY (tipo_beneficio_id) REFERENCES public.tb_tipo_beneficio(uuid);

ALTER TABLE ONLY public.tb_telefones_paciente
    ADD CONSTRAINT fk2q3yrx3g0qbc1dth1bosf53fu FOREIGN KEY (paciente_uuid) REFERENCES public.tb_paciente(uuid);

ALTER TABLE ONLY public.tb_vaga
    ADD CONSTRAINT fk3d61fotrhdtju4313ffg3c9oq FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);

ALTER TABLE ONLY public.tb_vaga
    ADD CONSTRAINT fk3leun966ch0b5vp8adoammqp3 FOREIGN KEY (cargo_id) REFERENCES public.tb_cargo(uuid);

ALTER TABLE ONLY public.tb_tabela_salarial
    ADD CONSTRAINT fk3nv8lhxjxky0oqy6518h488q FOREIGN KEY (cargo_id) REFERENCES public.tb_cargo(uuid);

ALTER TABLE ONLY public.tb_perfil_por_tipo_unidade
    ADD CONSTRAINT fk4fdumdyoy9o5avv04aotw9vcb FOREIGN KEY (perfil_administrativo_id) REFERENCES public.tb_perfil_administrativo(uuid);

ALTER TABLE ONLY public.tb_papel_permissao
    ADD CONSTRAINT fk5bpd0qnmhs6x7gbvjoda8jyd0 FOREIGN KEY (permissao_id) REFERENCES public.tb_permissao(uuid);

ALTER TABLE ONLY public.tb_responsabilidade_administrativa
    ADD CONSTRAINT fk5luwp2xbdt743qmi2kvciwg9w FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);

ALTER TABLE ONLY public.tb_telefones_profissional
    ADD CONSTRAINT fk5pw7kftrnukybmcbvsig1demj FOREIGN KEY (profissional_uuid) REFERENCES public.tb_profissional(uuid);

ALTER TABLE ONLY public.tb_acesso_justificado
    ADD CONSTRAINT fk5q2v9p69y0x96ae4xqc9k05oy FOREIGN KEY (paciente_uuid) REFERENCES public.tb_paciente(uuid);

ALTER TABLE ONLY public.tb_necessidade_de_pessoal
    ADD CONSTRAINT fk6oyqv9o71uw0i5wdfh9ej76h7 FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);

ALTER TABLE ONLY public.tb_registro_ponto
    ADD CONSTRAINT fk6pmgahb3ylt7baxk6im8uad4c FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);

ALTER TABLE ONLY public.tb_dispensacao
    ADD CONSTRAINT fk6qjdo9ngw9fvo0ue8mqu1s5ym FOREIGN KEY (consulta_id) REFERENCES public.tb_consulta(uuid);

ALTER TABLE ONLY public.tb_transferencia_farmacia
    ADD CONSTRAINT fk70gb54mht38c0qdi0he2qp1p7 FOREIGN KEY (profissional_recebimento_id) REFERENCES public.tb_profissional(uuid);

ALTER TABLE ONLY public.tb_avaliacao
    ADD CONSTRAINT fk7881kpqrd1t1u73rimoahwwvq FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);

ALTER TABLE ONLY public.tb_epi
    ADD CONSTRAINT fk7efw07pdgspttkjfpa099c8tw FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);

ALTER TABLE ONLY public.tb_redefinicao_senha
    ADD CONSTRAINT fk7ewxqleh2tsphqirt17yy3v1 FOREIGN KEY (usuario_uuid) REFERENCES public.tb_usuario(uuid);

ALTER TABLE ONLY public.tb_atendimento
    ADD CONSTRAINT fk7f9wj1cnovw7ksxrtt0dgukuw FOREIGN KEY (paciente_id) REFERENCES public.tb_paciente(uuid);

ALTER TABLE ONLY public.tb_participacao_treinamento
    ADD CONSTRAINT fk7mgg2tj1x9a45ux7opo73rbbx FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);

ALTER TABLE ONLY public.tb_movimentacao_farmacia
    ADD CONSTRAINT fk7ucpl9llshj90wrmhyvfeh8h9 FOREIGN KEY (transferencia_id) REFERENCES public.tb_transferencia_farmacia(uuid);

ALTER TABLE ONLY public.tb_acidente_trabalho
    ADD CONSTRAINT fk7vdvihovtiqot0s8st6sxw0i6 FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);

ALTER TABLE ONLY public.tb_unidade_de_saude
    ADD CONSTRAINT fk8akaqbedq5rf9f64nfempr8tr FOREIGN KEY (unidade_superior_id) REFERENCES public.tb_unidade_de_saude(uuid);

ALTER TABLE ONLY public.tb_setor
    ADD CONSTRAINT fk8e7l2v3sjpcrbewf9q1aeemm FOREIGN KEY (responsavel_id) REFERENCES public.tb_profissional(uuid);

ALTER TABLE ONLY public.tb_evolucao_enfermagem
    ADD CONSTRAINT fk8fpjisg7wx2c36beglp2prl9c FOREIGN KEY (retificacao_de_id) REFERENCES public.tb_evolucao_enfermagem(uuid);

ALTER TABLE ONLY public.tb_atribuicao_acesso
    ADD CONSTRAINT fk937oitmhufguwbsrt0kfgrhr6 FOREIGN KEY (papel_id) REFERENCES public.tb_papel(uuid);

ALTER TABLE ONLY public.tb_licenca
    ADD CONSTRAINT fk9f6o8i7nd70j9s4rp4etk681a FOREIGN KEY (afastamento_id) REFERENCES public.tb_afastamento(uuid);

ALTER TABLE ONLY public.tb_atribuicao_acesso
    ADD CONSTRAINT fk9pg2mgcovk9mofive9o9ukvyg FOREIGN KEY (usuario_id) REFERENCES public.tb_usuario(uuid);

ALTER TABLE ONLY public.tb_dispensacao
    ADD CONSTRAINT fk9vsujcp9j1t84g22ippa7ymg9 FOREIGN KEY (paciente_id) REFERENCES public.tb_paciente(uuid);

ALTER TABLE ONLY public.tb_papel_permissao
    ADD CONSTRAINT fka83o0pjchxuohkybtu4hnqw0c FOREIGN KEY (papel_id) REFERENCES public.tb_papel(uuid);

ALTER TABLE ONLY public.tb_procedimento
    ADD CONSTRAINT fkadi235xmf1o45dlv0enmameng FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);

ALTER TABLE ONLY public.tb_folha_pagamento
    ADD CONSTRAINT fkayrq1y28mx87gduiv7cceaxur FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);

ALTER TABLE ONLY public.tb_candidato
    ADD CONSTRAINT fkb9mtk6qu0gwk510yhvgm06tar FOREIGN KEY (vaga_id) REFERENCES public.tb_vaga(uuid);

ALTER TABLE ONLY public.tb_movimentacao_farmacia
    ADD CONSTRAINT fkbc3shtxogqpbagwamyjipyf1g FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);

ALTER TABLE ONLY public.tb_lote
    ADD CONSTRAINT fkbej9dsj7u58426d18urreswcr FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);

ALTER TABLE ONLY public.tb_lote
    ADD CONSTRAINT fkbxn32yvjf19c8ysnv6oa9wmoa FOREIGN KEY (medicamento_id) REFERENCES public.tb_medicamento(uuid);

ALTER TABLE ONLY public.tb_agendamento
    ADD CONSTRAINT fkc6hfnsiuinxlwmnhbphlqkroi FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);

ALTER TABLE ONLY public.tb_evolucao_enfermagem
    ADD CONSTRAINT fkcefv06rte2ye4tfv8iyfnhagw FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);

ALTER TABLE ONLY public.tb_administracao_medicamento
    ADD CONSTRAINT fkcformxs5yh8r5rgiffob6nwym FOREIGN KEY (lote_id) REFERENCES public.tb_lote(uuid);

ALTER TABLE ONLY public.tb_consulta
    ADD CONSTRAINT fkcl2h5eprxhp0aaxncu3u52d26 FOREIGN KEY (retificacao_de_id) REFERENCES public.tb_consulta(uuid);

ALTER TABLE ONLY public.tb_regra_anuenio
    ADD CONSTRAINT fkd506rd1uglcenxvdopdag0g19 FOREIGN KEY (categoria_salarial_id) REFERENCES public.tb_categoria_salarial(uuid);

ALTER TABLE ONLY public.tb_consulta
    ADD CONSTRAINT fkdbcxhuv7a6ioitthtxvcl494u FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);

ALTER TABLE ONLY public.tb_lote
    ADD CONSTRAINT fkdey5sil5n4qg4jqjwc08164bd FOREIGN KEY (lote_incorporador_id) REFERENCES public.tb_lote(uuid);

ALTER TABLE ONLY public.tb_unidade_de_saude
    ADD CONSTRAINT fkdt13ev9vh2mk9ve0umqat3opa FOREIGN KEY (supervisao_regional_id) REFERENCES public.tb_unidade_de_saude(uuid);

ALTER TABLE ONLY public.tb_procedimento
    ADD CONSTRAINT fkdtrshi2lkpjisxki5lvcqbme4 FOREIGN KEY (consulta_id) REFERENCES public.tb_consulta(uuid);

ALTER TABLE ONLY public.tb_transferencia_farmacia
    ADD CONSTRAINT fkdwwq5wyoyv767c7i9ddj6segs FOREIGN KEY (lote_origem_id) REFERENCES public.tb_lote(uuid);

ALTER TABLE ONLY public.tb_agendamento
    ADD CONSTRAINT fke57morfrv75qtbdk9ndm38hlt FOREIGN KEY (paciente_id) REFERENCES public.tb_paciente(uuid);

ALTER TABLE ONLY public.tb_exame_ocupacional
    ADD CONSTRAINT fke6mxr2fcniisvtc5j29ik9la8 FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);

ALTER TABLE ONLY public.tb_atendimento
    ADD CONSTRAINT fkedhjm40yki9c3b1vcbvxs59lm FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);

ALTER TABLE ONLY public.tb_lotacao
    ADD CONSTRAINT fkf9kqxyax6lb4x0yid62a2paj5 FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);

ALTER TABLE ONLY public.tb_afastamento
    ADD CONSTRAINT fkfl5u9omplxaft1ql64ok78b1e FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);

ALTER TABLE ONLY public.tb_transferencia_farmacia
    ADD CONSTRAINT fkfo6h0titwkwovm91whi56ulb4 FOREIGN KEY (unidade_destino_id) REFERENCES public.tb_unidade_de_saude(uuid);

ALTER TABLE ONLY public.tb_dispensacao
    ADD CONSTRAINT fkfrj3tb0qfhg26r7tca3whcs32 FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);

ALTER TABLE ONLY public.tb_administracao_medicamento
    ADD CONSTRAINT fkfw9jt7ig01fybxi8ftgc6c5gu FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);

ALTER TABLE ONLY public.tb_horario_atendimento
    ADD CONSTRAINT fkfxnpt0i71ovneaps32jq3lay2 FOREIGN KEY (unidade_de_saude_uuid) REFERENCES public.tb_unidade_de_saude(uuid);

ALTER TABLE ONLY public.tb_transferencia_farmacia
    ADD CONSTRAINT fkg8p69du02s802rjyfhdo76brv FOREIGN KEY (lote_destino_id) REFERENCES public.tb_lote(uuid);

ALTER TABLE ONLY public.tb_administracao_medicamento
    ADD CONSTRAINT fkhf3jwplexg2mapgfiw5o6fbmr FOREIGN KEY (atendimento_id) REFERENCES public.tb_atendimento(uuid);

ALTER TABLE ONLY public.tb_processo_administrativo
    ADD CONSTRAINT fki7iejqijwm8h74w1361f8gpe3 FOREIGN KEY (capacidade_administrativa_id) REFERENCES public.tb_capacidade_administrativa(uuid);

ALTER TABLE ONLY public.tb_participacao_treinamento
    ADD CONSTRAINT fkj7gnaip9xqylto4vk3qu2cagf FOREIGN KEY (treinamento_id) REFERENCES public.tb_treinamento(uuid);

ALTER TABLE ONLY public.tb_administracao_medicamento
    ADD CONSTRAINT fkjnj83er81538goln82t8n2sbn FOREIGN KEY (retificacao_de_id) REFERENCES public.tb_administracao_medicamento(uuid);

ALTER TABLE ONLY public.tb_procedimento
    ADD CONSTRAINT fkjpgdlsouaax2891kdr5ihm6s FOREIGN KEY (profissional_status_id) REFERENCES public.tb_profissional(uuid);

ALTER TABLE ONLY public.tb_transferencia_farmacia
    ADD CONSTRAINT fkjpjiuau8kqs8npywg3ukmtmdu FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);

ALTER TABLE ONLY public.tb_atendimento
    ADD CONSTRAINT fkkrwn4p1ki0iw5m43if9xhubpb FOREIGN KEY (setor_id) REFERENCES public.tb_setor(uuid);

ALTER TABLE ONLY public.tb_triagem
    ADD CONSTRAINT fkl8khtwgfof15he3e1umq31x46 FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);

ALTER TABLE ONLY public.tb_necessidade_de_pessoal
    ADD CONSTRAINT fkm48cnh2m20u3ehhry8pkxjgog FOREIGN KEY (cargo_id) REFERENCES public.tb_cargo(uuid);

ALTER TABLE ONLY public.tb_horario_funionamento
    ADD CONSTRAINT fkm5233ahh9itw2dohaa2np3fqh FOREIGN KEY (unidade_de_saude_uuid) REFERENCES public.tb_unidade_de_saude(uuid);

ALTER TABLE ONLY public.tb_cargo
    ADD CONSTRAINT fkm87kva05xtj6c07ie9tridhji FOREIGN KEY (categoria_salarial_id) REFERENCES public.tb_categoria_salarial(uuid);

ALTER TABLE ONLY public.tb_evolucao_enfermagem
    ADD CONSTRAINT fkm8p784mnfxy5qcyg25qxgdryj FOREIGN KEY (atendimento_id) REFERENCES public.tb_atendimento(uuid);

ALTER TABLE ONLY public.tb_necessidade_de_pessoal
    ADD CONSTRAINT fkmaa2q30i13g1b0kuqkptc4kch FOREIGN KEY (vaga_associada_id) REFERENCES public.tb_vaga(uuid);

ALTER TABLE ONLY public.tb_adesao_beneficio
    ADD CONSTRAINT fkn5mppi9fnprk96mea0afw4bvy FOREIGN KEY (tipo_beneficio_id) REFERENCES public.tb_tipo_beneficio(uuid);

ALTER TABLE ONLY public.tb_setor
    ADD CONSTRAINT fkn87j51eegrlno0k0pxxxl85g5 FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);

ALTER TABLE ONLY public.tb_necessidade_de_pessoal
    ADD CONSTRAINT fknajyba6a3xvi49cevtbu43kf2 FOREIGN KEY (setor_id) REFERENCES public.tb_setor(uuid);

ALTER TABLE ONLY public.tb_triagem
    ADD CONSTRAINT fknc8n8kjsd34jd4up21cpkbajv FOREIGN KEY (retificacao_de_id) REFERENCES public.tb_triagem(uuid);

ALTER TABLE ONLY public.tb_procedimento
    ADD CONSTRAINT fkndft3ovj3kxnil9e95v1h49ba FOREIGN KEY (retificacao_de_id) REFERENCES public.tb_procedimento(uuid);

ALTER TABLE ONLY public.tb_adesao_beneficio
    ADD CONSTRAINT fkndqy048g0hnto7fe9bu3tdnrj FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);

ALTER TABLE ONLY public.tb_movimentacao_farmacia
    ADD CONSTRAINT fknfbljjbymdr5l1o50l94ag0co FOREIGN KEY (lote_id) REFERENCES public.tb_lote(uuid);

ALTER TABLE ONLY public.tb_responsabilidade_administrativa
    ADD CONSTRAINT fknqgsukw2t2xnte5t84ys3dgyo FOREIGN KEY (setor_id) REFERENCES public.tb_setor(uuid);

ALTER TABLE ONLY public.tb_transferencia_farmacia
    ADD CONSTRAINT fknyxwka8lglwo9ndaaxwc8ixm6 FOREIGN KEY (profissional_cancelamento_id) REFERENCES public.tb_profissional(uuid);

ALTER TABLE ONLY public.tb_telefones_saude
    ADD CONSTRAINT fkoare3qma2k2jx1e4n26ls0gr FOREIGN KEY (unidade_de_saude_uuid) REFERENCES public.tb_unidade_de_saude(uuid);

ALTER TABLE ONLY public.tb_avaliacao
    ADD CONSTRAINT fkoft61iw8nxn1w2nhkgwxyoyjc FOREIGN KEY (ciclo_avaliacao_id) REFERENCES public.tb_ciclo_avaliacao(uuid);

ALTER TABLE ONLY public.tb_movimentacao_farmacia
    ADD CONSTRAINT fkojmapcuvkk99b32c9yb6khgou FOREIGN KEY (dispensacao_id) REFERENCES public.tb_dispensacao(uuid);

ALTER TABLE ONLY public.tb_atendimento
    ADD CONSTRAINT fkopl4nw0rtu25mtb5kyk0d4lna FOREIGN KEY (agendamento_id) REFERENCES public.tb_agendamento(uuid);

ALTER TABLE ONLY public.tb_administracao_medicamento
    ADD CONSTRAINT fkoymgfki564xm9enxb70ibsrh7 FOREIGN KEY (consulta_id) REFERENCES public.tb_consulta(uuid);

ALTER TABLE ONLY public.tb_ajuste_individual
    ADD CONSTRAINT fkpw7stq9hka5tqu2c2bxslngw3 FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);

ALTER TABLE ONLY public.tb_calculo_rescisao
    ADD CONSTRAINT fkpwnk9c2hcawuoll0j7193iv1g FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);

ALTER TABLE ONLY public.tb_atribuicao_acesso
    ADD CONSTRAINT fkpykskftut4797phqhyhcmye67 FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);

ALTER TABLE ONLY public.tb_administracao_medicamento
    ADD CONSTRAINT fkqbom6dh0yocwh9ay6eq05irgf FOREIGN KEY (medicamento_id) REFERENCES public.tb_medicamento(uuid);

ALTER TABLE ONLY public.tb_atendimento
    ADD CONSTRAINT fkrbk10we6njskfvv8j0yb8o9vo FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);

ALTER TABLE ONLY public.tb_triagem
    ADD CONSTRAINT fkrseqgi8rnp4a995p4784966co FOREIGN KEY (atendimento_id) REFERENCES public.tb_atendimento(uuid);

ALTER TABLE ONLY public.tb_unidade_de_saude
    ADD CONSTRAINT fkt9l3bw895np2po80pjnvx9f5d FOREIGN KEY (responsavel_id) REFERENCES public.tb_profissional(uuid);

ALTER TABLE ONLY public.tb_lotacao
    ADD CONSTRAINT fktglsj0wpxm505duq8kbsv4fcw FOREIGN KEY (cargo_id) REFERENCES public.tb_cargo(uuid);
