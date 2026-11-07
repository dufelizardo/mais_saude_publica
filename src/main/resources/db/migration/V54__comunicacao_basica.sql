-- Comunicação e Educação em Saúde, fatia C1 (ADR-0168, desenho na ADR-0114): canais, comunicado, conteúdo educativo e
-- publicação. A carga traz Portal e E-mail; as permissões nascem pelo catálogo de acesso.
-- Conferido contra o ddl-auto=validate (ADR-0090).

CREATE TABLE public.tb_canal_comunicacao (
    ativo boolean NOT NULL,
    uuid uuid NOT NULL,
    tipo character varying(20) NOT NULL,
    nome character varying(100) NOT NULL,
    descricao character varying(300),
    CONSTRAINT tb_canal_comunicacao_tipo_check CHECK (tipo IN ('PORTAL', 'APLICATIVO', 'EMAIL', 'SMS', 'NOTIFICACAO', 'PAINEL', 'IMPRESSO', 'EVENTO', 'REDE_SOCIAL'))
);
ALTER TABLE ONLY public.tb_canal_comunicacao ADD CONSTRAINT tb_canal_comunicacao_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_canal_comunicacao ADD CONSTRAINT uk_canal_comunicacao_nome UNIQUE (nome);

CREATE TABLE public.tb_comunicado (
    encerrado_em date,
    fim date NOT NULL,
    inicio date NOT NULL,
    publicado_em timestamp(6) without time zone,
    unidade_id uuid,
    uuid uuid NOT NULL,
    prioridade character varying(20) NOT NULL,
    publico character varying(20) NOT NULL,
    situacao character varying(20) NOT NULL,
    titulo character varying(150) NOT NULL,
    motivo_encerramento character varying(500),
    publicado_por_cpf character varying(255),
    registrado_por_cpf character varying(255),
    mensagem text NOT NULL,
    CONSTRAINT tb_comunicado_prioridade_check CHECK (prioridade IN ('NORMAL', 'ALTA', 'EMERGENCIAL')),
    CONSTRAINT tb_comunicado_publico_check CHECK (publico IN ('POPULACAO', 'PROFISSIONAIS')),
    CONSTRAINT tb_comunicado_situacao_check CHECK (situacao IN ('RASCUNHO', 'PUBLICADO', 'ENCERRADO'))
);
ALTER TABLE ONLY public.tb_comunicado ADD CONSTRAINT tb_comunicado_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_comunicado ADD CONSTRAINT fk_comunicado_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);

CREATE TABLE public.tb_comunicado_canal (
    canal_id uuid NOT NULL,
    comunicado_id uuid NOT NULL
);
ALTER TABLE ONLY public.tb_comunicado_canal ADD CONSTRAINT tb_comunicado_canal_pkey PRIMARY KEY (comunicado_id, canal_id);
ALTER TABLE ONLY public.tb_comunicado_canal ADD CONSTRAINT fk_comunicado_canal_comunicado FOREIGN KEY (comunicado_id) REFERENCES public.tb_comunicado(uuid);
ALTER TABLE ONLY public.tb_comunicado_canal ADD CONSTRAINT fk_comunicado_canal_canal FOREIGN KEY (canal_id) REFERENCES public.tb_canal_comunicacao(uuid);

CREATE TABLE public.tb_conteudo_educativo (
    validade date NOT NULL,
    versao integer NOT NULL,
    publicado_em timestamp(6) without time zone,
    revisado_em timestamp(6) without time zone,
    grupo_id uuid NOT NULL,
    uuid uuid NOT NULL,
    publico character varying(20) NOT NULL,
    situacao character varying(20) NOT NULL,
    faixa_etaria character varying(60),
    tema character varying(100) NOT NULL,
    titulo character varying(150) NOT NULL,
    autor_cpf character varying(255) NOT NULL,
    revisor_cpf character varying(255),
    motivo_devolucao character varying(500),
    resumo character varying(500) NOT NULL,
    texto text NOT NULL,
    CONSTRAINT tb_conteudo_educativo_publico_check CHECK (publico IN ('POPULACAO', 'PROFISSIONAIS')),
    CONSTRAINT tb_conteudo_educativo_situacao_check CHECK (situacao IN ('RASCUNHO', 'EM_REVISAO', 'APROVADO', 'PUBLICADO', 'EXPIRADO', 'SUBSTITUIDO'))
);
ALTER TABLE ONLY public.tb_conteudo_educativo ADD CONSTRAINT tb_conteudo_educativo_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_conteudo_grupo ON public.tb_conteudo_educativo USING btree (grupo_id);

CREATE TABLE public.tb_conteudo_canal (
    canal_id uuid NOT NULL,
    conteudo_id uuid NOT NULL
);
ALTER TABLE ONLY public.tb_conteudo_canal ADD CONSTRAINT tb_conteudo_canal_pkey PRIMARY KEY (conteudo_id, canal_id);
ALTER TABLE ONLY public.tb_conteudo_canal ADD CONSTRAINT fk_conteudo_canal_conteudo FOREIGN KEY (conteudo_id) REFERENCES public.tb_conteudo_educativo(uuid);
ALTER TABLE ONLY public.tb_conteudo_canal ADD CONSTRAINT fk_conteudo_canal_canal FOREIGN KEY (canal_id) REFERENCES public.tb_canal_comunicacao(uuid);

CREATE TABLE public.tb_publicacao (
    enviados integer NOT NULL,
    falhas integer NOT NULL,
    publicada_em timestamp(6) without time zone NOT NULL,
    canal_id uuid NOT NULL,
    comunicado_id uuid,
    conteudo_id uuid,
    uuid uuid NOT NULL,
    situacao character varying(20) NOT NULL,
    detalhe character varying(300),
    publicada_por_cpf character varying(255),
    CONSTRAINT tb_publicacao_situacao_check CHECK (situacao IN ('PUBLICADA', 'ENVIADA', 'PARCIAL', 'NAO_ENVIADA')),
    CONSTRAINT tb_publicacao_origem_check CHECK ((comunicado_id IS NULL) <> (conteudo_id IS NULL))
);
ALTER TABLE ONLY public.tb_publicacao ADD CONSTRAINT tb_publicacao_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_publicacao_comunicado ON public.tb_publicacao USING btree (comunicado_id);
CREATE INDEX ix_publicacao_conteudo ON public.tb_publicacao USING btree (conteudo_id);
ALTER TABLE ONLY public.tb_publicacao ADD CONSTRAINT fk_publicacao_canal FOREIGN KEY (canal_id) REFERENCES public.tb_canal_comunicacao(uuid);
ALTER TABLE ONLY public.tb_publicacao ADD CONSTRAINT fk_publicacao_comunicado FOREIGN KEY (comunicado_id) REFERENCES public.tb_comunicado(uuid);
ALTER TABLE ONLY public.tb_publicacao ADD CONSTRAINT fk_publicacao_conteudo FOREIGN KEY (conteudo_id) REFERENCES public.tb_conteudo_educativo(uuid);

INSERT INTO public.tb_canal_comunicacao (uuid, tipo, nome, descricao, ativo) VALUES
    (gen_random_uuid(), 'PORTAL', 'Portal', 'A página pública do sistema.', true),
    (gen_random_uuid(), 'EMAIL', 'E-mail', 'E-mail do cadastro dos profissionais.', true);
