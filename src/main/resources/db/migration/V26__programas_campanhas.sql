-- Programas de saúde, fatia P3 (ADR-0133): a campanha (período, público, meta, publicação e cancelamento), os locais
-- da campanha e a ação programática da campanha (do programa ou da campanha, um dos dois), com a quantidade atendida.
-- Conferido contra o ddl-auto=validate (ADR-0090).

CREATE TABLE public.tb_campanha (
    gestante boolean NOT NULL,
    publicada boolean NOT NULL,
    idade_minima integer,
    idade_maxima integer,
    meta_quantidade integer,
    inicio date NOT NULL,
    fim date NOT NULL,
    cancelada_em timestamp(6) with time zone,
    criada_em timestamp(6) with time zone NOT NULL,
    programa_id uuid,
    uuid uuid NOT NULL,
    nome character varying(200) NOT NULL,
    publico_descricao character varying(300),
    meta_descricao character varying(300),
    motivo_cancelamento character varying(500),
    descricao character varying(2000),
    publico_sexo character varying(255),
    registrado_por_cpf character varying(255),
    tipo character varying(255) NOT NULL,
    CONSTRAINT tb_campanha_publico_sexo_check CHECK (((publico_sexo)::text = ANY ((ARRAY['MASCULINO'::character varying, 'FEMININO'::character varying, 'IGNORADO'::character varying])::text[]))),
    CONSTRAINT tb_campanha_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['VACINACAO'::character varying, 'RASTREAMENTO'::character varying, 'CONSCIENTIZACAO'::character varying, 'MUTIRAO'::character varying, 'OUTRA'::character varying])::text[])))
);

ALTER TABLE ONLY public.tb_campanha ADD CONSTRAINT tb_campanha_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_campanha ADD CONSTRAINT fk_campanha_programa FOREIGN KEY (programa_id) REFERENCES public.tb_programa(uuid);

CREATE TABLE public.tb_campanha_condicao (
    posicao integer NOT NULL,
    campanha_id uuid NOT NULL,
    codigo character varying(10) NOT NULL
);

ALTER TABLE ONLY public.tb_campanha_condicao ADD CONSTRAINT tb_campanha_condicao_pkey PRIMARY KEY (campanha_id, posicao);
ALTER TABLE ONLY public.tb_campanha_condicao ADD CONSTRAINT fk_campanha_condicao FOREIGN KEY (campanha_id) REFERENCES public.tb_campanha(uuid);

CREATE TABLE public.tb_local_campanha (
    temporario boolean NOT NULL,
    latitude double precision,
    longitude double precision,
    inicio date,
    fim date,
    campanha_id uuid NOT NULL,
    unidade_id uuid NOT NULL,
    uuid uuid NOT NULL,
    nome character varying(200),
    funcionamento character varying(200),
    endereco character varying(500)
);

ALTER TABLE ONLY public.tb_local_campanha ADD CONSTRAINT tb_local_campanha_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_local_campanha ON public.tb_local_campanha USING btree (campanha_id);
ALTER TABLE ONLY public.tb_local_campanha ADD CONSTRAINT fk_local_campanha FOREIGN KEY (campanha_id) REFERENCES public.tb_campanha(uuid);
ALTER TABLE ONLY public.tb_local_campanha ADD CONSTRAINT fk_local_campanha_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);

-- A ação programática passa a ser do programa ou da campanha (um dos dois), com o local da campanha e o atendido.
ALTER TABLE public.tb_acao_programatica ALTER COLUMN programa_id DROP NOT NULL;
ALTER TABLE public.tb_acao_programatica ADD COLUMN campanha_id uuid;
ALTER TABLE public.tb_acao_programatica ADD COLUMN local_campanha_id uuid;
ALTER TABLE public.tb_acao_programatica ADD COLUMN quantidade_atendida integer;
ALTER TABLE ONLY public.tb_acao_programatica ADD CONSTRAINT fk_acao_campanha FOREIGN KEY (campanha_id) REFERENCES public.tb_campanha(uuid);
ALTER TABLE ONLY public.tb_acao_programatica ADD CONSTRAINT fk_acao_local_campanha FOREIGN KEY (local_campanha_id) REFERENCES public.tb_local_campanha(uuid);
CREATE INDEX ix_acao_campanha ON public.tb_acao_programatica USING btree (campanha_id);
ALTER TABLE public.tb_acao_programatica ADD CONSTRAINT tb_acao_programatica_alvo_check CHECK (((programa_id IS NULL) <> (campanha_id IS NULL)));
ALTER TABLE public.tb_acao_programatica DROP CONSTRAINT tb_acao_programatica_tipo_check;
ALTER TABLE public.tb_acao_programatica ADD CONSTRAINT tb_acao_programatica_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['CONSULTA_PROGRAMADA'::character varying, 'AFERICAO'::character varying, 'EXAME'::character varying, 'VACINA'::character varying, 'VISITA_DOMICILIAR'::character varying, 'BUSCA_ATIVA'::character varying, 'GRUPO'::character varying, 'ORIENTACAO'::character varying, 'MOBILIZACAO'::character varying, 'OUTRA'::character varying])::text[])));
