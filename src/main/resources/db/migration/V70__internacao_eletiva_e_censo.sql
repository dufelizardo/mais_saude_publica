-- Leitos, fatia RL2 (ADR-0185): a internação eletiva agendada com a reserva do leito, e o censo hospitalar diário fechado com o
-- censo nominal. Conferido contra o ddl-auto=validate (ADR-0090). A permissão CENSO.FECHAR vem do CatalogoDeAcesso.

ALTER TABLE public.tb_leito ADD COLUMN reserva_agendamento_id uuid;

CREATE TABLE public.tb_internacao_agendada (
    data_prevista date NOT NULL,
    previsao_dias integer,
    agendada_em timestamp(6) with time zone NOT NULL,
    reservado_ate timestamp(6) with time zone,
    internacao_id uuid,
    leito_reservado_id uuid,
    medico_id uuid NOT NULL,
    paciente_id uuid NOT NULL,
    solicitacao_regulacao_id uuid,
    unidade_id uuid NOT NULL,
    uuid uuid NOT NULL,
    cid character varying(10) NOT NULL,
    procedimento character varying(500) NOT NULL,
    motivo_encerramento character varying(1000),
    registrado_por_cpf character varying(255),
    status character varying(255) NOT NULL,
    tipo_leito character varying(255) NOT NULL,
    CONSTRAINT tb_internacao_agendada_status_check CHECK (((status)::text = ANY ((ARRAY['AGENDADA'::character varying, 'RESERVADA'::character varying, 'ADMITIDA'::character varying, 'FALTA'::character varying, 'CANCELADA'::character varying])::text[]))),
    CONSTRAINT tb_internacao_agendada_tipo_leito_check CHECK (((tipo_leito)::text = ANY ((ARRAY['CLINICO'::character varying, 'CIRURGICO'::character varying, 'PEDIATRICO'::character varying, 'OBSTETRICO'::character varying, 'UTI_ADULTO'::character varying, 'UTI_PEDIATRICA'::character varying, 'UTI_NEONATAL'::character varying, 'OBSERVACAO'::character varying, 'ISOLAMENTO'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_internacao_agendada ADD CONSTRAINT tb_internacao_agendada_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_internacao_agendada ADD CONSTRAINT fk_internacao_agendada_paciente FOREIGN KEY (paciente_id) REFERENCES public.tb_paciente(uuid);
ALTER TABLE ONLY public.tb_internacao_agendada ADD CONSTRAINT fk_internacao_agendada_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);
ALTER TABLE ONLY public.tb_internacao_agendada ADD CONSTRAINT fk_internacao_agendada_medico FOREIGN KEY (medico_id) REFERENCES public.tb_profissional(uuid);
ALTER TABLE ONLY public.tb_internacao_agendada ADD CONSTRAINT fk_internacao_agendada_leito FOREIGN KEY (leito_reservado_id) REFERENCES public.tb_leito(uuid);
ALTER TABLE ONLY public.tb_internacao_agendada ADD CONSTRAINT fk_internacao_agendada_internacao FOREIGN KEY (internacao_id) REFERENCES public.tb_internacao(uuid);
CREATE INDEX ix_internacao_agendada_data ON public.tb_internacao_agendada USING btree (unidade_id, data_prevista);
CREATE INDEX ix_internacao_agendada_paciente ON public.tb_internacao_agendada USING btree (paciente_id);

CREATE TABLE public.tb_evento_internacao_agendada (
    ocorrido_em timestamp(6) with time zone NOT NULL,
    agendamento_id uuid NOT NULL,
    profissional_id uuid,
    uuid uuid NOT NULL,
    texto character varying(1000),
    registrado_por_cpf character varying(255),
    tipo character varying(255) NOT NULL,
    CONSTRAINT tb_evento_internacao_agendada_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['AGENDAMENTO'::character varying, 'RESERVA'::character varying, 'REMARCACAO'::character varying, 'ADMISSAO'::character varying, 'FALTA'::character varying, 'CANCELAMENTO'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_evento_internacao_agendada ADD CONSTRAINT tb_evento_internacao_agendada_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_evento_internacao_agendada ADD CONSTRAINT fk_evento_internacao_agendada FOREIGN KEY (agendamento_id) REFERENCES public.tb_internacao_agendada(uuid);
ALTER TABLE ONLY public.tb_evento_internacao_agendada ADD CONSTRAINT fk_evento_internacao_agendada_profissional FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);
CREATE INDEX ix_evento_internacao_agendada ON public.tb_evento_internacao_agendada USING btree (agendamento_id, ocorrido_em);

CREATE TABLE public.tb_censo_diario (
    admissoes integer NOT NULL,
    altas_a_pedido integer NOT NULL,
    altas_melhorado integer NOT NULL,
    altas_transferencia integer NOT NULL,
    data date NOT NULL,
    evasoes integer NOT NULL,
    internados_fim integer NOT NULL,
    internados_inicio integer NOT NULL,
    leitos_bloqueados integer NOT NULL,
    leitos_existentes integer NOT NULL,
    leitos_operacionais integer NOT NULL,
    media_permanencia numeric(8,1),
    obitos integer NOT NULL,
    paciente_dia integer NOT NULL,
    reaberto boolean NOT NULL,
    taxa_ocupacao numeric(6,1),
    transferencias_internas integer NOT NULL,
    versao integer NOT NULL,
    fechado_em timestamp(6) with time zone NOT NULL,
    reaberto_em timestamp(6) with time zone,
    unidade_id uuid NOT NULL,
    uuid uuid NOT NULL,
    motivo_reabertura character varying(1000),
    fechado_por_cpf character varying(255),
    reaberto_por_cpf character varying(255)
);
ALTER TABLE ONLY public.tb_censo_diario ADD CONSTRAINT tb_censo_diario_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_censo_diario ADD CONSTRAINT fk_censo_diario_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);
CREATE INDEX ix_censo_diario_unidade_data ON public.tb_censo_diario USING btree (unidade_id, data);
-- Um censo vigente (não reaberto) por unidade e dia.
CREATE UNIQUE INDEX uk_censo_diario_vigente ON public.tb_censo_diario USING btree (unidade_id, data) WHERE (NOT reaberto);

CREATE TABLE public.tb_censo_diario_paciente (
    dias_internado integer NOT NULL,
    censo_id uuid NOT NULL,
    internacao_id uuid NOT NULL,
    uuid uuid NOT NULL,
    leito_identificacao character varying(60) NOT NULL,
    setor_nome character varying(200) NOT NULL,
    paciente_nome character varying(300) NOT NULL
);
ALTER TABLE ONLY public.tb_censo_diario_paciente ADD CONSTRAINT tb_censo_diario_paciente_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_censo_diario_paciente ADD CONSTRAINT fk_censo_paciente_censo FOREIGN KEY (censo_id) REFERENCES public.tb_censo_diario(uuid);
ALTER TABLE ONLY public.tb_censo_diario_paciente ADD CONSTRAINT fk_censo_paciente_internacao FOREIGN KEY (internacao_id) REFERENCES public.tb_internacao(uuid);
CREATE INDEX ix_censo_diario_paciente ON public.tb_censo_diario_paciente USING btree (censo_id);
