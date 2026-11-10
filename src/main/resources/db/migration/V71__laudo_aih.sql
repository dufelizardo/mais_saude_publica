-- Leitos, fatia RL3 (ADR-0186): o laudo para solicitação de AIH com a autorização interna, o histórico do laudo e as faixas de
-- numeração de AIH recebidas do gestor. Conferido contra o ddl-auto=validate (ADR-0090). As permissões AIH.SOLICITAR,
-- AIH.AUTORIZAR e AIH.NUMERAR e o papel Médico autorizador vêm do CatalogoDeAcesso.

ALTER TABLE public.tb_internacao ADD COLUMN numero_aih character varying(13);

CREATE TABLE public.tb_laudo_aih (
    decidido_em timestamp(6) with time zone,
    solicitado_em timestamp(6) with time zone NOT NULL,
    agendamento_id uuid,
    autorizador_id uuid,
    internacao_id uuid,
    paciente_id uuid NOT NULL,
    procedimento_id uuid NOT NULL,
    solicitante_id uuid NOT NULL,
    unidade_id uuid NOT NULL,
    uuid uuid NOT NULL,
    cid_principal character varying(10) NOT NULL,
    cid_secundario character varying(10),
    cid_causa_associada character varying(10),
    numero_aih character varying(13),
    diagnostico_inicial character varying(500) NOT NULL,
    motivo_decisao character varying(1000),
    sinais_sintomas character varying(2000) NOT NULL,
    condicoes_justificativas character varying(2000) NOT NULL,
    resultados_exames character varying(2000),
    registrado_por_cpf character varying(255),
    carater character varying(255) NOT NULL,
    clinica character varying(255) NOT NULL,
    causa_externa character varying(255),
    status character varying(255) NOT NULL,
    CONSTRAINT tb_laudo_aih_carater_check CHECK (((carater)::text = ANY ((ARRAY['ELETIVA'::character varying, 'URGENCIA'::character varying])::text[]))),
    CONSTRAINT tb_laudo_aih_clinica_check CHECK (((clinica)::text = ANY ((ARRAY['CIRURGICA'::character varying, 'OBSTETRICA'::character varying, 'CLINICA_MEDICA'::character varying, 'CUIDADOS_PROLONGADOS'::character varying, 'PSIQUIATRIA'::character varying, 'TISIOLOGIA'::character varying, 'PEDIATRIA'::character varying, 'REABILITACAO'::character varying, 'PSIQUIATRIA_HOSPITAL_DIA'::character varying])::text[]))),
    CONSTRAINT tb_laudo_aih_causa_externa_check CHECK (((causa_externa)::text = ANY ((ARRAY['ACIDENTE_DE_TRANSITO'::character varying, 'ACIDENTE_DE_TRABALHO_TIPICO'::character varying, 'ACIDENTE_DE_TRABALHO_TRAJETO'::character varying, 'OUTRA_CAUSA_EXTERNA'::character varying])::text[]))),
    CONSTRAINT tb_laudo_aih_status_check CHECK (((status)::text = ANY ((ARRAY['SOLICITADO'::character varying, 'DEVOLVIDO'::character varying, 'AUTORIZADO'::character varying, 'NEGADO'::character varying, 'CANCELADO'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_laudo_aih ADD CONSTRAINT tb_laudo_aih_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_laudo_aih ADD CONSTRAINT uk_laudo_aih_numero UNIQUE (numero_aih);
ALTER TABLE ONLY public.tb_laudo_aih ADD CONSTRAINT fk_laudo_aih_paciente FOREIGN KEY (paciente_id) REFERENCES public.tb_paciente(uuid);
ALTER TABLE ONLY public.tb_laudo_aih ADD CONSTRAINT fk_laudo_aih_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);
ALTER TABLE ONLY public.tb_laudo_aih ADD CONSTRAINT fk_laudo_aih_internacao FOREIGN KEY (internacao_id) REFERENCES public.tb_internacao(uuid);
ALTER TABLE ONLY public.tb_laudo_aih ADD CONSTRAINT fk_laudo_aih_agendamento FOREIGN KEY (agendamento_id) REFERENCES public.tb_internacao_agendada(uuid);
ALTER TABLE ONLY public.tb_laudo_aih ADD CONSTRAINT fk_laudo_aih_solicitante FOREIGN KEY (solicitante_id) REFERENCES public.tb_profissional(uuid);
ALTER TABLE ONLY public.tb_laudo_aih ADD CONSTRAINT fk_laudo_aih_autorizador FOREIGN KEY (autorizador_id) REFERENCES public.tb_profissional(uuid);
ALTER TABLE ONLY public.tb_laudo_aih ADD CONSTRAINT fk_laudo_aih_procedimento FOREIGN KEY (procedimento_id) REFERENCES public.tb_procedimento_catalogo(uuid);
CREATE INDEX ix_laudo_aih_internacao ON public.tb_laudo_aih USING btree (internacao_id);
CREATE INDEX ix_laudo_aih_agendamento ON public.tb_laudo_aih USING btree (agendamento_id);
CREATE INDEX ix_laudo_aih_status ON public.tb_laudo_aih USING btree (status, solicitado_em);

CREATE TABLE public.tb_evento_laudo_aih (
    ocorrido_em timestamp(6) with time zone NOT NULL,
    laudo_id uuid NOT NULL,
    profissional_id uuid,
    uuid uuid NOT NULL,
    texto character varying(1000),
    registrado_por_cpf character varying(255),
    tipo character varying(255) NOT NULL,
    CONSTRAINT tb_evento_laudo_aih_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['SOLICITACAO'::character varying, 'CORRECAO'::character varying, 'DEVOLUCAO'::character varying, 'AUTORIZACAO'::character varying, 'NEGACAO'::character varying, 'CANCELAMENTO'::character varying, 'VINCULO_INTERNACAO'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_evento_laudo_aih ADD CONSTRAINT tb_evento_laudo_aih_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_evento_laudo_aih ADD CONSTRAINT fk_evento_laudo_aih FOREIGN KEY (laudo_id) REFERENCES public.tb_laudo_aih(uuid);
ALTER TABLE ONLY public.tb_evento_laudo_aih ADD CONSTRAINT fk_evento_laudo_aih_profissional FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);
CREATE INDEX ix_evento_laudo_aih ON public.tb_evento_laudo_aih USING btree (laudo_id, ocorrido_em);

CREATE TABLE public.tb_faixa_aih (
    ano integer NOT NULL,
    ativa boolean NOT NULL,
    fim bigint NOT NULL,
    inicio bigint NOT NULL,
    proximo bigint NOT NULL,
    registrada_em timestamp(6) with time zone NOT NULL,
    uuid uuid NOT NULL,
    registrada_por_cpf character varying(255),
    CONSTRAINT tb_faixa_aih_intervalo_check CHECK (inicio <= fim AND inicio > 0 AND fim < 1000000000000)
);
ALTER TABLE ONLY public.tb_faixa_aih ADD CONSTRAINT tb_faixa_aih_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_faixa_aih_ano ON public.tb_faixa_aih USING btree (ano, inicio);
