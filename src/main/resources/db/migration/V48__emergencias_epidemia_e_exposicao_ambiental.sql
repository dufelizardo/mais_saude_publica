-- Emergências e Desastres, fatia E6b (ADR-0161, desenho na ADR-0113): o agravo da epidemia na emergência (o painel é
-- lido da Vigilância, sem tabela nova) e a exposição ambiental do desastre, com as unidades na área. Conferido contra o
-- ddl-auto=validate (ADR-0090).

ALTER TABLE public.tb_emergencia ADD COLUMN agravo_id uuid;
ALTER TABLE ONLY public.tb_emergencia ADD CONSTRAINT fk_emergencia_agravo FOREIGN KEY (agravo_id) REFERENCES public.tb_agravo(uuid);

CREATE TABLE public.tb_exposicao_ambiental (
    populacao_exposta integer,
    avaliada_em timestamp(6) with time zone NOT NULL,
    emergencia_id uuid NOT NULL,
    uuid uuid NOT NULL,
    meio character varying(255) NOT NULL,
    agente character varying(200) NOT NULL,
    fonte character varying(300),
    medidas character varying(1000),
    recomendacao character varying(1000),
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_exposicao_ambiental_meio_check CHECK (((meio)::text = ANY ((ARRAY['AGUA'::character varying, 'AR'::character varying, 'SOLO'::character varying, 'PRODUTO_QUIMICO'::character varying, 'OUTRO'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_exposicao_ambiental ADD CONSTRAINT tb_exposicao_ambiental_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_exposicao_ambiental_emergencia ON public.tb_exposicao_ambiental USING btree (emergencia_id, avaliada_em);
ALTER TABLE ONLY public.tb_exposicao_ambiental ADD CONSTRAINT fk_exposicao_ambiental_emergencia FOREIGN KEY (emergencia_id) REFERENCES public.tb_emergencia(uuid);

CREATE TABLE public.tb_exposicao_ambiental_unidade (
    exposicao_id uuid NOT NULL,
    unidade_id uuid NOT NULL
);
ALTER TABLE ONLY public.tb_exposicao_ambiental_unidade ADD CONSTRAINT tb_exposicao_ambiental_unidade_pkey PRIMARY KEY (exposicao_id, unidade_id);
ALTER TABLE ONLY public.tb_exposicao_ambiental_unidade ADD CONSTRAINT fk_exposicao_ambiental_unidade_exposicao FOREIGN KEY (exposicao_id) REFERENCES public.tb_exposicao_ambiental(uuid);
ALTER TABLE ONLY public.tb_exposicao_ambiental_unidade ADD CONSTRAINT fk_exposicao_ambiental_unidade_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);
