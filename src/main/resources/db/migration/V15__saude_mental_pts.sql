-- Saúde Mental, fatia SM2 (ADR-0121): o Projeto Terapêutico Singular versionado (uma versão por linha; a revisão marca
-- a anterior como substituída), com objetivos e intervenções previstas; as intervenções realizadas, com tipo de um
-- catálogo semeado; e a rede de apoio do caso, como histórico. Tabelas conferidas contra o ddl-auto=validate (ADR-0090).

CREATE TABLE public.tb_tipo_intervencao_saude_mental (
    ativo boolean NOT NULL,
    ordem integer NOT NULL,
    uuid uuid NOT NULL,
    codigo character varying(40) NOT NULL,
    nome character varying(120) NOT NULL
);

CREATE TABLE public.tb_projeto_terapeutico_singular (
    pactuado_com_pessoa boolean NOT NULL,
    revisar_em date NOT NULL,
    versao integer NOT NULL,
    elaborado_em timestamp(6) with time zone NOT NULL,
    substituido_em timestamp(6) with time zone,
    acompanhamento_id uuid NOT NULL,
    profissional_id uuid,
    uuid uuid NOT NULL,
    pactuacao_observacao character varying(1000),
    sintese_revisao character varying(2000),
    necessidades character varying(4000) NOT NULL,
    periodicidade_revisao character varying(255) NOT NULL,
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_projeto_terapeutico_singular_periodicidade_revisao_check CHECK (((periodicidade_revisao)::text = ANY ((ARRAY['MENSAL'::character varying, 'BIMESTRAL'::character varying, 'TRIMESTRAL'::character varying, 'QUADRIMESTRAL'::character varying, 'SEMESTRAL'::character varying, 'ANUAL'::character varying, 'SEM_PERIODICIDADE'::character varying])::text[])))
);

CREATE TABLE public.tb_objetivo_terapeutico (
    ordem integer NOT NULL,
    prazo date,
    pts_id uuid NOT NULL,
    responsavel_id uuid,
    uuid uuid NOT NULL,
    descricao character varying(1000) NOT NULL,
    meta character varying(1000),
    situacao character varying(255) NOT NULL,
    CONSTRAINT tb_objetivo_terapeutico_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['EM_ANDAMENTO'::character varying, 'ALCANCADO'::character varying, 'NAO_ALCANCADO'::character varying, 'SUSPENSO'::character varying])::text[])))
);

CREATE TABLE public.tb_intervencao_prevista (
    ordem integer NOT NULL,
    pts_id uuid NOT NULL,
    responsavel_id uuid,
    tipo_id uuid NOT NULL,
    uuid uuid NOT NULL,
    frequencia character varying(100),
    servico character varying(300)
);

CREATE TABLE public.tb_intervencao_saude_mental (
    realizada_em timestamp(6) with time zone NOT NULL,
    acompanhamento_id uuid NOT NULL,
    compartilhado_com_id uuid,
    prevista_id uuid,
    profissional_id uuid,
    tipo_id uuid NOT NULL,
    uuid uuid NOT NULL,
    resumo character varying(2000) NOT NULL,
    registrado_por_cpf character varying(255),
    situacao character varying(255) NOT NULL,
    CONSTRAINT tb_intervencao_saude_mental_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['REALIZADA'::character varying, 'NAO_COMPARECEU'::character varying])::text[])))
);

CREATE TABLE public.tb_pessoa_rede_apoio (
    fim date,
    inicio date NOT NULL,
    acompanhamento_id uuid NOT NULL,
    uuid uuid NOT NULL,
    telefone character varying(30),
    vinculo character varying(100),
    nome character varying(200) NOT NULL,
    motivo_fim character varying(500),
    observacao character varying(500),
    papel character varying(255) NOT NULL,
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_pessoa_rede_apoio_papel_check CHECK (((papel)::text = ANY ((ARRAY['FAMILIAR'::character varying, 'CUIDADOR'::character varying, 'PESSOA_DE_REFERENCIA'::character varying, 'SERVICO'::character varying, 'OUTRO'::character varying])::text[])))
);

ALTER TABLE ONLY public.tb_tipo_intervencao_saude_mental ADD CONSTRAINT tb_tipo_intervencao_saude_mental_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_tipo_intervencao_saude_mental ADD CONSTRAINT uk_tipo_intervencao_sm_codigo UNIQUE (codigo);
ALTER TABLE ONLY public.tb_projeto_terapeutico_singular ADD CONSTRAINT tb_projeto_terapeutico_singular_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_projeto_terapeutico_singular ADD CONSTRAINT uk_pts_versao UNIQUE (acompanhamento_id, versao);
ALTER TABLE ONLY public.tb_objetivo_terapeutico ADD CONSTRAINT tb_objetivo_terapeutico_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_intervencao_prevista ADD CONSTRAINT tb_intervencao_prevista_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_intervencao_saude_mental ADD CONSTRAINT tb_intervencao_saude_mental_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_pessoa_rede_apoio ADD CONSTRAINT tb_pessoa_rede_apoio_pkey PRIMARY KEY (uuid);

CREATE INDEX ix_objetivo_terapeutico_pts ON public.tb_objetivo_terapeutico USING btree (pts_id);
CREATE INDEX ix_intervencao_prevista_pts ON public.tb_intervencao_prevista USING btree (pts_id);
CREATE INDEX ix_intervencao_sm_acomp ON public.tb_intervencao_saude_mental USING btree (acompanhamento_id, realizada_em);
CREATE INDEX ix_pessoa_rede_apoio_acomp ON public.tb_pessoa_rede_apoio USING btree (acompanhamento_id, fim);

ALTER TABLE ONLY public.tb_projeto_terapeutico_singular
    ADD CONSTRAINT fk_pts_acompanhamento FOREIGN KEY (acompanhamento_id) REFERENCES public.tb_acompanhamento_saude_mental(uuid);
ALTER TABLE ONLY public.tb_projeto_terapeutico_singular
    ADD CONSTRAINT fk_pts_profissional FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);
ALTER TABLE ONLY public.tb_objetivo_terapeutico
    ADD CONSTRAINT fk_objetivo_terapeutico_pts FOREIGN KEY (pts_id) REFERENCES public.tb_projeto_terapeutico_singular(uuid);
ALTER TABLE ONLY public.tb_objetivo_terapeutico
    ADD CONSTRAINT fk_objetivo_terapeutico_responsavel FOREIGN KEY (responsavel_id) REFERENCES public.tb_profissional(uuid);
ALTER TABLE ONLY public.tb_intervencao_prevista
    ADD CONSTRAINT fk_intervencao_prevista_pts FOREIGN KEY (pts_id) REFERENCES public.tb_projeto_terapeutico_singular(uuid);
ALTER TABLE ONLY public.tb_intervencao_prevista
    ADD CONSTRAINT fk_intervencao_prevista_tipo FOREIGN KEY (tipo_id) REFERENCES public.tb_tipo_intervencao_saude_mental(uuid);
ALTER TABLE ONLY public.tb_intervencao_prevista
    ADD CONSTRAINT fk_intervencao_prevista_responsavel FOREIGN KEY (responsavel_id) REFERENCES public.tb_profissional(uuid);
ALTER TABLE ONLY public.tb_intervencao_saude_mental
    ADD CONSTRAINT fk_intervencao_sm_acomp FOREIGN KEY (acompanhamento_id) REFERENCES public.tb_acompanhamento_saude_mental(uuid);
ALTER TABLE ONLY public.tb_intervencao_saude_mental
    ADD CONSTRAINT fk_intervencao_sm_tipo FOREIGN KEY (tipo_id) REFERENCES public.tb_tipo_intervencao_saude_mental(uuid);
ALTER TABLE ONLY public.tb_intervencao_saude_mental
    ADD CONSTRAINT fk_intervencao_sm_profissional FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);
ALTER TABLE ONLY public.tb_intervencao_saude_mental
    ADD CONSTRAINT fk_intervencao_sm_compartilhado FOREIGN KEY (compartilhado_com_id) REFERENCES public.tb_profissional(uuid);
ALTER TABLE ONLY public.tb_intervencao_saude_mental
    ADD CONSTRAINT fk_intervencao_sm_prevista FOREIGN KEY (prevista_id) REFERENCES public.tb_intervencao_prevista(uuid);
ALTER TABLE ONLY public.tb_pessoa_rede_apoio
    ADD CONSTRAINT fk_pessoa_rede_apoio_acomp FOREIGN KEY (acompanhamento_id) REFERENCES public.tb_acompanhamento_saude_mental(uuid);

-- Os tipos de intervenção do modelo (MODELO-SAUDE-MENTAL, seção 8). Intervenção em crise entra com a SM3.
INSERT INTO public.tb_tipo_intervencao_saude_mental (uuid, codigo, nome, ordem, ativo) VALUES
    (gen_random_uuid(), 'ATENDIMENTO_INDIVIDUAL', 'Atendimento individual', 1, true),
    (gen_random_uuid(), 'ATENDIMENTO_FAMILIAR', 'Atendimento familiar', 2, true),
    (gen_random_uuid(), 'GRUPO', 'Grupo', 3, true),
    (gen_random_uuid(), 'OFICINA', 'Oficina', 4, true),
    (gen_random_uuid(), 'ATIVIDADE_COMUNITARIA', 'Atividade comunitária', 5, true),
    (gen_random_uuid(), 'VISITA_DOMICILIAR', 'Visita domiciliar', 6, true),
    (gen_random_uuid(), 'ATENDIMENTO_COMPARTILHADO', 'Atendimento compartilhado', 7, true),
    (gen_random_uuid(), 'ORIENTACAO', 'Orientação', 8, true);
