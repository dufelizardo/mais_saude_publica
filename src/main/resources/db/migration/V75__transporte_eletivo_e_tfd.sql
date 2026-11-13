-- Transporte, fatia T2 (ADR-0190): solicitação de transporte eletivo (consulta, exame, hemodiálise, terapia seriada, alta, TFD), a
-- viagem com o veículo, o condutor e os embarques, e a quilometragem. Conferido contra o ddl-auto=validate (ADR-0090).

CREATE TABLE public.tb_solicitacao_transporte (
    cadeira_de_rodas boolean NOT NULL,
    maca boolean NOT NULL,
    oxigenio boolean NOT NULL,
    data_hora timestamp(6) without time zone NOT NULL,
    decidido_em timestamp(6) with time zone,
    solicitado_em timestamp(6) with time zone NOT NULL,
    uuid uuid NOT NULL,
    paciente_id uuid NOT NULL,
    unidade_id uuid NOT NULL,
    agendamento_id uuid,
    solicitacao_regulacao_id uuid,
    internacao_id uuid,
    destino_unidade_id uuid,
    profissional_id uuid,
    recorrencia_id uuid,
    motivo character varying(30) NOT NULL,
    situacao character varying(30) NOT NULL,
    trecho character varying(20) NOT NULL,
    cid character varying(10),
    acompanhante_documento character varying(30),
    destino_municipio character varying(100),
    acompanhante_nome character varying(150),
    endereco_embarque character varying(500) NOT NULL,
    destino character varying(500) NOT NULL,
    motivo_situacao character varying(500),
    observacao character varying(500),
    justificativa character varying(2000),
    solicitado_por_cpf character varying(255),
    decidido_por_cpf character varying(255),
    CONSTRAINT tb_solicitacao_transporte_motivo_check CHECK (motivo IN ('CONSULTA', 'EXAME', 'HEMODIALISE', 'TERAPIA_SERIADA', 'ALTA_HOSPITALAR',
        'TFD', 'OUTRO')),
    CONSTRAINT tb_solicitacao_transporte_situacao_check CHECK (situacao IN ('SOLICITADA', 'AGUARDANDO_AUTORIZACAO', 'AUTORIZADA', 'PROGRAMADA',
        'REALIZADA', 'FALTOU', 'NEGADA', 'CANCELADA')),
    CONSTRAINT tb_solicitacao_transporte_trecho_check CHECK (trecho IN ('IDA', 'VOLTA', 'IDA_E_VOLTA'))
);
ALTER TABLE ONLY public.tb_solicitacao_transporte ADD CONSTRAINT tb_solicitacao_transporte_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_solicitacao_transporte_data ON public.tb_solicitacao_transporte USING btree (data_hora);
CREATE INDEX ix_solicitacao_transporte_paciente ON public.tb_solicitacao_transporte USING btree (paciente_id);
CREATE INDEX ix_solicitacao_transporte_recorrencia ON public.tb_solicitacao_transporte USING btree (recorrencia_id);
ALTER TABLE ONLY public.tb_solicitacao_transporte ADD CONSTRAINT fk_solicitacao_transporte_paciente FOREIGN KEY (paciente_id) REFERENCES public.tb_paciente(uuid);
ALTER TABLE ONLY public.tb_solicitacao_transporte ADD CONSTRAINT fk_solicitacao_transporte_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);
ALTER TABLE ONLY public.tb_solicitacao_transporte ADD CONSTRAINT fk_solicitacao_transporte_agendamento FOREIGN KEY (agendamento_id) REFERENCES public.tb_agendamento(uuid);
ALTER TABLE ONLY public.tb_solicitacao_transporte ADD CONSTRAINT fk_solicitacao_transporte_regulacao FOREIGN KEY (solicitacao_regulacao_id) REFERENCES public.tb_solicitacao_regulacao(uuid);
ALTER TABLE ONLY public.tb_solicitacao_transporte ADD CONSTRAINT fk_solicitacao_transporte_internacao FOREIGN KEY (internacao_id) REFERENCES public.tb_internacao(uuid);
ALTER TABLE ONLY public.tb_solicitacao_transporte ADD CONSTRAINT fk_solicitacao_transporte_destino FOREIGN KEY (destino_unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);
ALTER TABLE ONLY public.tb_solicitacao_transporte ADD CONSTRAINT fk_solicitacao_transporte_profissional FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);

CREATE TABLE public.tb_viagem_transporte (
    km_chegada integer,
    km_saida integer,
    chegada_em timestamp(6) with time zone,
    retorno_previsto timestamp(6) with time zone NOT NULL,
    saida_em timestamp(6) with time zone,
    saida_prevista timestamp(6) with time zone NOT NULL,
    uuid uuid NOT NULL,
    veiculo_id uuid NOT NULL,
    condutor_id uuid NOT NULL,
    situacao character varying(20) NOT NULL,
    destino character varying(500) NOT NULL,
    motivo_cancelamento character varying(500),
    observacao character varying(500),
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_viagem_transporte_situacao_check CHECK (situacao IN ('PROGRAMADA', 'EM_ANDAMENTO', 'CONCLUIDA', 'CANCELADA'))
);
ALTER TABLE ONLY public.tb_viagem_transporte ADD CONSTRAINT tb_viagem_transporte_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_viagem_transporte_saida ON public.tb_viagem_transporte USING btree (saida_prevista);
ALTER TABLE ONLY public.tb_viagem_transporte ADD CONSTRAINT fk_viagem_transporte_veiculo FOREIGN KEY (veiculo_id) REFERENCES public.tb_veiculo(uuid);
ALTER TABLE ONLY public.tb_viagem_transporte ADD CONSTRAINT fk_viagem_transporte_condutor FOREIGN KEY (condutor_id) REFERENCES public.tb_profissional(uuid);

CREATE TABLE public.tb_embarque_viagem (
    uuid uuid NOT NULL,
    viagem_id uuid NOT NULL,
    solicitacao_id uuid NOT NULL,
    situacao character varying(20) NOT NULL,
    CONSTRAINT tb_embarque_viagem_situacao_check CHECK (situacao IN ('PREVISTO', 'EMBARCOU', 'FALTOU'))
);
ALTER TABLE ONLY public.tb_embarque_viagem ADD CONSTRAINT tb_embarque_viagem_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_embarque_viagem ADD CONSTRAINT uk_embarque_viagem UNIQUE (viagem_id, solicitacao_id);
ALTER TABLE ONLY public.tb_embarque_viagem ADD CONSTRAINT fk_embarque_viagem_viagem FOREIGN KEY (viagem_id) REFERENCES public.tb_viagem_transporte(uuid);
ALTER TABLE ONLY public.tb_embarque_viagem ADD CONSTRAINT fk_embarque_viagem_solicitacao FOREIGN KEY (solicitacao_id) REFERENCES public.tb_solicitacao_transporte(uuid);
