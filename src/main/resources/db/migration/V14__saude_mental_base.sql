-- Saúde Mental, fatia SM1 (ADR-0120): o caso em saúde mental ligado ao acompanhamento do programa Saúde Mental, a
-- equipe e o técnico de referência como histórico, o acolhimento e a avaliação (só inclusão). Tabelas conferidas contra
-- o ddl-auto=validate (ADR-0090). As permissões SAUDE_MENTAL.* e os papéis PSICOLOGO e ASSISTENTE_SOCIAL nascem na
-- subida pelo catálogo de acesso (ADR-0087).

CREATE TABLE public.tb_acompanhamento_saude_mental (
    fim date,
    inicio date NOT NULL,
    acompanhamento_programatico_id uuid NOT NULL,
    paciente_id uuid NOT NULL,
    unidade_id uuid NOT NULL,
    uuid uuid NOT NULL,
    observacao_saida character varying(500),
    motivo_saida character varying(255),
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_acompanhamento_saude_mental_motivo_saida_check CHECK (((motivo_saida)::text = ANY ((ARRAY['ALTA'::character varying, 'TRANSFERENCIA'::character varying, 'MUDANCA_DE_MUNICIPIO'::character varying, 'ABANDONO'::character varying, 'OBITO'::character varying, 'OUTRO'::character varying])::text[])))
);

CREATE TABLE public.tb_referencia_saude_mental (
    fim date,
    inicio date NOT NULL,
    acompanhamento_id uuid NOT NULL,
    equipe_id uuid NOT NULL,
    tecnico_id uuid NOT NULL,
    uuid uuid NOT NULL,
    motivo character varying(500),
    registrado_por_cpf character varying(255)
);

CREATE TABLE public.tb_acolhimento_saude_mental (
    realizado_em timestamp(6) with time zone NOT NULL,
    acompanhamento_id uuid,
    atendimento_id uuid,
    paciente_id uuid NOT NULL,
    profissional_id uuid,
    unidade_id uuid NOT NULL,
    uuid uuid NOT NULL,
    motivo_procura character varying(2000) NOT NULL,
    necessidade_imediata character varying(1000),
    vulnerabilidade character varying(1000),
    encaminhamento_destino character varying(500),
    desfecho character varying(255) NOT NULL,
    risco_inicial character varying(255) NOT NULL,
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_acolhimento_saude_mental_desfecho_check CHECK (((desfecho)::text = ANY ((ARRAY['ACOMPANHAMENTO_NA_UNIDADE'::character varying, 'CUIDADO_COMPARTILHADO'::character varying, 'ENCAMINHAMENTO'::character varying, 'CRISE'::character varying, 'ORIENTACAO_SEM_SEGUIMENTO'::character varying])::text[]))),
    CONSTRAINT tb_acolhimento_saude_mental_risco_inicial_check CHECK (((risco_inicial)::text = ANY ((ARRAY['SEM_RISCO_APARENTE'::character varying, 'BAIXO'::character varying, 'MODERADO'::character varying, 'ALTO'::character varying])::text[])))
);

CREATE TABLE public.tb_avaliacao_saude_mental (
    realizada_em timestamp(6) with time zone NOT NULL,
    acompanhamento_id uuid NOT NULL,
    profissional_id uuid,
    uuid uuid NOT NULL,
    cid character varying(10),
    contexto_historia character varying(4000) NOT NULL,
    aspectos_psicossociais character varying(4000),
    fatores_risco character varying(2000),
    fatores_protecao character varying(2000),
    necessidades character varying(2000) NOT NULL,
    registrado_por_cpf character varying(255)
);

ALTER TABLE ONLY public.tb_acompanhamento_saude_mental ADD CONSTRAINT tb_acompanhamento_saude_mental_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_referencia_saude_mental ADD CONSTRAINT tb_referencia_saude_mental_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_acolhimento_saude_mental ADD CONSTRAINT tb_acolhimento_saude_mental_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_avaliacao_saude_mental ADD CONSTRAINT tb_avaliacao_saude_mental_pkey PRIMARY KEY (uuid);

CREATE INDEX ix_acomp_saude_mental_paciente ON public.tb_acompanhamento_saude_mental USING btree (paciente_id, fim);
CREATE INDEX ix_acomp_saude_mental_unidade ON public.tb_acompanhamento_saude_mental USING btree (unidade_id, fim);
CREATE INDEX ix_referencia_saude_mental_acomp ON public.tb_referencia_saude_mental USING btree (acompanhamento_id, fim);
CREATE INDEX ix_referencia_saude_mental_equipe ON public.tb_referencia_saude_mental USING btree (equipe_id, fim);
CREATE INDEX ix_acolhimento_saude_mental_paciente ON public.tb_acolhimento_saude_mental USING btree (paciente_id, realizado_em);
CREATE INDEX ix_avaliacao_saude_mental_acomp ON public.tb_avaliacao_saude_mental USING btree (acompanhamento_id, realizada_em);

ALTER TABLE ONLY public.tb_acompanhamento_saude_mental
    ADD CONSTRAINT fk_acomp_saude_mental_paciente FOREIGN KEY (paciente_id) REFERENCES public.tb_paciente(uuid);
ALTER TABLE ONLY public.tb_acompanhamento_saude_mental
    ADD CONSTRAINT fk_acomp_saude_mental_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);
ALTER TABLE ONLY public.tb_acompanhamento_saude_mental
    ADD CONSTRAINT fk_acomp_saude_mental_programatico FOREIGN KEY (acompanhamento_programatico_id) REFERENCES public.tb_acompanhamento_programatico(uuid);
ALTER TABLE ONLY public.tb_referencia_saude_mental
    ADD CONSTRAINT fk_referencia_saude_mental_acomp FOREIGN KEY (acompanhamento_id) REFERENCES public.tb_acompanhamento_saude_mental(uuid);
ALTER TABLE ONLY public.tb_referencia_saude_mental
    ADD CONSTRAINT fk_referencia_saude_mental_equipe FOREIGN KEY (equipe_id) REFERENCES public.tb_equipe(uuid);
ALTER TABLE ONLY public.tb_referencia_saude_mental
    ADD CONSTRAINT fk_referencia_saude_mental_tecnico FOREIGN KEY (tecnico_id) REFERENCES public.tb_profissional(uuid);
ALTER TABLE ONLY public.tb_acolhimento_saude_mental
    ADD CONSTRAINT fk_acolhimento_saude_mental_paciente FOREIGN KEY (paciente_id) REFERENCES public.tb_paciente(uuid);
ALTER TABLE ONLY public.tb_acolhimento_saude_mental
    ADD CONSTRAINT fk_acolhimento_saude_mental_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);
ALTER TABLE ONLY public.tb_acolhimento_saude_mental
    ADD CONSTRAINT fk_acolhimento_saude_mental_profissional FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);
ALTER TABLE ONLY public.tb_acolhimento_saude_mental
    ADD CONSTRAINT fk_acolhimento_saude_mental_atendimento FOREIGN KEY (atendimento_id) REFERENCES public.tb_atendimento(uuid);
ALTER TABLE ONLY public.tb_acolhimento_saude_mental
    ADD CONSTRAINT fk_acolhimento_saude_mental_acomp FOREIGN KEY (acompanhamento_id) REFERENCES public.tb_acompanhamento_saude_mental(uuid);
ALTER TABLE ONLY public.tb_avaliacao_saude_mental
    ADD CONSTRAINT fk_avaliacao_saude_mental_acomp FOREIGN KEY (acompanhamento_id) REFERENCES public.tb_acompanhamento_saude_mental(uuid);
ALTER TABLE ONLY public.tb_avaliacao_saude_mental
    ADD CONSTRAINT fk_avaliacao_saude_mental_profissional FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);
