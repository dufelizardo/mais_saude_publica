-- Emergências e Desastres, fatia E2 (ADR-0157, desenho na ADR-0113): plano de contingência versionado (níveis,
-- critérios de ativação e ações previstas por nível, recursos, contatos, unidades) e a ativação do plano na emergência,
-- com histórico. Conferido contra o ddl-auto=validate (ADR-0090).

CREATE TABLE public.tb_plano_contingencia (
    versao integer NOT NULL,
    vigencia_fim date,
    vigencia_inicio date NOT NULL,
    criado_em timestamp(6) with time zone NOT NULL,
    publicado_em timestamp(6) with time zone,
    encerrado_em timestamp(6) with time zone,
    tipo_id uuid NOT NULL,
    uuid uuid NOT NULL,
    situacao character varying(255) NOT NULL,
    codigo character varying(40) NOT NULL,
    nome character varying(200) NOT NULL,
    objetivo character varying(2000) NOT NULL,
    motivo_revogacao character varying(500),
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_plano_contingencia_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['RASCUNHO'::character varying, 'VIGENTE'::character varying, 'SUBSTITUIDA'::character varying, 'REVOGADA'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_plano_contingencia ADD CONSTRAINT tb_plano_contingencia_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_plano_contingencia ADD CONSTRAINT uk_plano_contingencia_versao UNIQUE (codigo, versao);
ALTER TABLE ONLY public.tb_plano_contingencia ADD CONSTRAINT fk_plano_contingencia_tipo FOREIGN KEY (tipo_id) REFERENCES public.tb_tipo_emergencia(uuid);

CREATE TABLE public.tb_plano_contingencia_nivel (
    plano_id uuid NOT NULL,
    nivel_id uuid NOT NULL
);
ALTER TABLE ONLY public.tb_plano_contingencia_nivel ADD CONSTRAINT tb_plano_contingencia_nivel_pkey PRIMARY KEY (plano_id, nivel_id);
ALTER TABLE ONLY public.tb_plano_contingencia_nivel ADD CONSTRAINT fk_plano_nivel_plano FOREIGN KEY (plano_id) REFERENCES public.tb_plano_contingencia(uuid);
ALTER TABLE ONLY public.tb_plano_contingencia_nivel ADD CONSTRAINT fk_plano_nivel_nivel FOREIGN KEY (nivel_id) REFERENCES public.tb_nivel_emergencia(uuid);

CREATE TABLE public.tb_plano_contingencia_unidade (
    plano_id uuid NOT NULL,
    unidade_id uuid NOT NULL
);
ALTER TABLE ONLY public.tb_plano_contingencia_unidade ADD CONSTRAINT tb_plano_contingencia_unidade_pkey PRIMARY KEY (plano_id, unidade_id);
ALTER TABLE ONLY public.tb_plano_contingencia_unidade ADD CONSTRAINT fk_plano_unidade_plano FOREIGN KEY (plano_id) REFERENCES public.tb_plano_contingencia(uuid);
ALTER TABLE ONLY public.tb_plano_contingencia_unidade ADD CONSTRAINT fk_plano_unidade_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);

CREATE TABLE public.tb_criterio_ativacao (
    ordem integer NOT NULL,
    nivel_id uuid NOT NULL,
    plano_id uuid NOT NULL,
    uuid uuid NOT NULL,
    descricao character varying(1000) NOT NULL
);
ALTER TABLE ONLY public.tb_criterio_ativacao ADD CONSTRAINT tb_criterio_ativacao_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_criterio_ativacao ADD CONSTRAINT fk_criterio_ativacao_plano FOREIGN KEY (plano_id) REFERENCES public.tb_plano_contingencia(uuid);
ALTER TABLE ONLY public.tb_criterio_ativacao ADD CONSTRAINT fk_criterio_ativacao_nivel FOREIGN KEY (nivel_id) REFERENCES public.tb_nivel_emergencia(uuid);

CREATE TABLE public.tb_acao_plano (
    ordem integer NOT NULL,
    prazo_horas integer,
    nivel_id uuid NOT NULL,
    plano_id uuid NOT NULL,
    unidade_id uuid,
    uuid uuid NOT NULL,
    descricao character varying(1000) NOT NULL,
    responsavel character varying(200) NOT NULL
);
ALTER TABLE ONLY public.tb_acao_plano ADD CONSTRAINT tb_acao_plano_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_acao_plano ADD CONSTRAINT fk_acao_plano_plano FOREIGN KEY (plano_id) REFERENCES public.tb_plano_contingencia(uuid);
ALTER TABLE ONLY public.tb_acao_plano ADD CONSTRAINT fk_acao_plano_nivel FOREIGN KEY (nivel_id) REFERENCES public.tb_nivel_emergencia(uuid);
ALTER TABLE ONLY public.tb_acao_plano ADD CONSTRAINT fk_acao_plano_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);

CREATE TABLE public.tb_recurso_plano (
    ordem integer NOT NULL,
    quantidade integer,
    plano_id uuid NOT NULL,
    uuid uuid NOT NULL,
    descricao character varying(300) NOT NULL
);
ALTER TABLE ONLY public.tb_recurso_plano ADD CONSTRAINT tb_recurso_plano_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_recurso_plano ADD CONSTRAINT fk_recurso_plano_plano FOREIGN KEY (plano_id) REFERENCES public.tb_plano_contingencia(uuid);

CREATE TABLE public.tb_contato_plano (
    ordem integer NOT NULL,
    plano_id uuid NOT NULL,
    uuid uuid NOT NULL,
    telefone character varying(30),
    nome character varying(200) NOT NULL,
    papel character varying(200)
);
ALTER TABLE ONLY public.tb_contato_plano ADD CONSTRAINT tb_contato_plano_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_contato_plano ADD CONSTRAINT fk_contato_plano_plano FOREIGN KEY (plano_id) REFERENCES public.tb_plano_contingencia(uuid);

CREATE TABLE public.tb_ativacao_plano (
    ativada_em timestamp(6) with time zone NOT NULL,
    desativada_em timestamp(6) with time zone,
    emergencia_id uuid NOT NULL,
    plano_id uuid NOT NULL,
    uuid uuid NOT NULL,
    situacao character varying(255) NOT NULL,
    CONSTRAINT tb_ativacao_plano_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['ATIVA'::character varying, 'DESATIVADA'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_ativacao_plano ADD CONSTRAINT tb_ativacao_plano_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_ativacao_plano ADD CONSTRAINT fk_ativacao_plano_emergencia FOREIGN KEY (emergencia_id) REFERENCES public.tb_emergencia(uuid);
ALTER TABLE ONLY public.tb_ativacao_plano ADD CONSTRAINT fk_ativacao_plano_plano FOREIGN KEY (plano_id) REFERENCES public.tb_plano_contingencia(uuid);
CREATE INDEX ix_ativacao_plano_emergencia ON public.tb_ativacao_plano USING btree (emergencia_id, situacao);

CREATE TABLE public.tb_registro_ativacao_plano (
    nivel_coberto boolean NOT NULL,
    registrado_em timestamp(6) with time zone NOT NULL,
    ativacao_id uuid NOT NULL,
    nivel_id uuid NOT NULL,
    uuid uuid NOT NULL,
    tipo character varying(255) NOT NULL,
    autoridade character varying(200),
    justificativa character varying(1000) NOT NULL,
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_registro_ativacao_plano_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['ATIVADO'::character varying, 'NIVEL_ALTERADO'::character varying, 'DESATIVADO'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_registro_ativacao_plano ADD CONSTRAINT tb_registro_ativacao_plano_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_registro_ativacao_plano ADD CONSTRAINT fk_registro_ativacao_ativacao FOREIGN KEY (ativacao_id) REFERENCES public.tb_ativacao_plano(uuid);
ALTER TABLE ONLY public.tb_registro_ativacao_plano ADD CONSTRAINT fk_registro_ativacao_nivel FOREIGN KEY (nivel_id) REFERENCES public.tb_nivel_emergencia(uuid);
CREATE INDEX ix_registro_ativacao_plano ON public.tb_registro_ativacao_plano USING btree (ativacao_id, registrado_em);
