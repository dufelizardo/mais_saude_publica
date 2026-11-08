-- Campanhas de comunicação, fatia C2 (ADR-0174): campanha com público, territórios, temas, canais e conteúdos; material educativo;
-- o comunicado ganha a campanha de origem. Conferido contra o ddl-auto=validate (ADR-0090).

CREATE TABLE public.tb_campanha_comunicacao (
    fim date NOT NULL,
    inicio date NOT NULL,
    encerrada_em timestamp(6) without time zone,
    iniciada_em timestamp(6) without time zone,
    registrada_em timestamp(6) without time zone NOT NULL,
    campanha_operacional_id uuid,
    programa_id uuid,
    unidade_id uuid,
    uuid uuid NOT NULL,
    situacao character varying(20) NOT NULL,
    publico_alvo character varying(30) NOT NULL,
    faixa_etaria character varying(60),
    nome character varying(150) NOT NULL,
    objetivo character varying(500) NOT NULL,
    motivo_cancelamento character varying(500),
    descricao character varying(2000),
    balanco character varying(2000),
    registrada_por_cpf character varying(255),
    CONSTRAINT tb_campanha_comunicacao_situacao_check CHECK (situacao IN ('PLANEJADA', 'EM_ANDAMENTO', 'ENCERRADA', 'CANCELADA')),
    CONSTRAINT tb_campanha_comunicacao_publico_alvo_check CHECK (publico_alvo IN ('POPULACAO_GERAL', 'GESTANTES', 'CRIANCAS', 'ADOLESCENTES', 'IDOSOS', 'PROFISSIONAIS_SAUDE', 'ACS'))
);
ALTER TABLE ONLY public.tb_campanha_comunicacao ADD CONSTRAINT tb_campanha_comunicacao_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_campanha_comunicacao ADD CONSTRAINT fk_campanha_comunicacao_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);
ALTER TABLE ONLY public.tb_campanha_comunicacao ADD CONSTRAINT fk_campanha_comunicacao_programa FOREIGN KEY (programa_id) REFERENCES public.tb_programa(uuid);
ALTER TABLE ONLY public.tb_campanha_comunicacao ADD CONSTRAINT fk_campanha_comunicacao_operacional FOREIGN KEY (campanha_operacional_id) REFERENCES public.tb_campanha(uuid);

CREATE TABLE public.tb_campanha_comunicacao_territorio (
    campanha_id uuid NOT NULL,
    territorio_id uuid NOT NULL
);
ALTER TABLE ONLY public.tb_campanha_comunicacao_territorio ADD CONSTRAINT tb_campanha_comunicacao_territorio_pkey PRIMARY KEY (campanha_id, territorio_id);
ALTER TABLE ONLY public.tb_campanha_comunicacao_territorio ADD CONSTRAINT fk_campanha_territorio_campanha FOREIGN KEY (campanha_id) REFERENCES public.tb_campanha_comunicacao(uuid);
ALTER TABLE ONLY public.tb_campanha_comunicacao_territorio ADD CONSTRAINT fk_campanha_territorio_territorio FOREIGN KEY (territorio_id) REFERENCES public.tb_territorio(uuid);

CREATE TABLE public.tb_campanha_comunicacao_tema (
    campanha_id uuid NOT NULL,
    tema character varying(100) NOT NULL
);
ALTER TABLE ONLY public.tb_campanha_comunicacao_tema ADD CONSTRAINT tb_campanha_comunicacao_tema_pkey PRIMARY KEY (campanha_id, tema);
ALTER TABLE ONLY public.tb_campanha_comunicacao_tema ADD CONSTRAINT fk_campanha_tema_campanha FOREIGN KEY (campanha_id) REFERENCES public.tb_campanha_comunicacao(uuid);

CREATE TABLE public.tb_campanha_comunicacao_canal (
    campanha_id uuid NOT NULL,
    canal_id uuid NOT NULL
);
ALTER TABLE ONLY public.tb_campanha_comunicacao_canal ADD CONSTRAINT tb_campanha_comunicacao_canal_pkey PRIMARY KEY (campanha_id, canal_id);
ALTER TABLE ONLY public.tb_campanha_comunicacao_canal ADD CONSTRAINT fk_campanha_canal_campanha FOREIGN KEY (campanha_id) REFERENCES public.tb_campanha_comunicacao(uuid);
ALTER TABLE ONLY public.tb_campanha_comunicacao_canal ADD CONSTRAINT fk_campanha_canal_canal FOREIGN KEY (canal_id) REFERENCES public.tb_canal_comunicacao(uuid);

-- O conteúdo entra pelo grupo de versões, que não é tabela.
CREATE TABLE public.tb_campanha_comunicacao_conteudo (
    campanha_id uuid NOT NULL,
    conteudo_grupo_id uuid NOT NULL
);
ALTER TABLE ONLY public.tb_campanha_comunicacao_conteudo ADD CONSTRAINT tb_campanha_comunicacao_conteudo_pkey PRIMARY KEY (campanha_id, conteudo_grupo_id);
ALTER TABLE ONLY public.tb_campanha_comunicacao_conteudo ADD CONSTRAINT fk_campanha_conteudo_campanha FOREIGN KEY (campanha_id) REFERENCES public.tb_campanha_comunicacao(uuid);

CREATE TABLE public.tb_material_educativo (
    registrado_em timestamp(6) without time zone NOT NULL,
    retirado_em timestamp(6) without time zone,
    conteudo_grupo_id uuid NOT NULL,
    uuid uuid NOT NULL,
    formato character varying(20) NOT NULL,
    titulo character varying(150) NOT NULL,
    descricao character varying(500),
    link character varying(500),
    motivo_retirada character varying(500),
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_material_educativo_formato_check CHECK (formato IN ('CARTILHA', 'FOLDER', 'CARTAZ', 'INFOGRAFICO', 'VIDEO', 'AUDIO', 'REDE_SOCIAL', 'OUTRO'))
);
ALTER TABLE ONLY public.tb_material_educativo ADD CONSTRAINT tb_material_educativo_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_material_conteudo ON public.tb_material_educativo USING btree (conteudo_grupo_id);

ALTER TABLE public.tb_comunicado ADD COLUMN campanha_comunicacao_id uuid;
ALTER TABLE ONLY public.tb_comunicado ADD CONSTRAINT fk_comunicado_campanha FOREIGN KEY (campanha_comunicacao_id) REFERENCES public.tb_campanha_comunicacao(uuid);
