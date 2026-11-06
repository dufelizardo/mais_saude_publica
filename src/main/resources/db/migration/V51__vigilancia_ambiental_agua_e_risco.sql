-- Vigilância em Saúde, fatia V4c (ADR-0164, desenho na ADR-0110): a vigilância ambiental. Os pontos de coleta e as
-- amostras da qualidade da água, com a conformidade pela Portaria GM/MS 888/2021, e o risco ambiental permanente.
-- Conferido contra o ddl-auto=validate (ADR-0090).

CREATE TABLE public.tb_ponto_coleta_agua (
    ativo boolean NOT NULL,
    latitude double precision,
    longitude double precision,
    registrado_em timestamp(6) with time zone NOT NULL,
    microarea_id uuid,
    uuid uuid NOT NULL,
    nome character varying(200) NOT NULL,
    tipo character varying(255) NOT NULL,
    local character varying(300) NOT NULL,
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_ponto_coleta_agua_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['REDE'::character varying, 'SOLUCAO_ALTERNATIVA_COLETIVA'::character varying, 'SOLUCAO_ALTERNATIVA_INDIVIDUAL'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_ponto_coleta_agua ADD CONSTRAINT tb_ponto_coleta_agua_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_ponto_coleta_agua ADD CONSTRAINT fk_ponto_coleta_agua_microarea FOREIGN KEY (microarea_id) REFERENCES public.tb_microarea(uuid);

CREATE TABLE public.tb_amostra_agua (
    coliformes_totais boolean,
    conforme boolean NOT NULL,
    ecoli boolean,
    cloro_residual double precision,
    fluor double precision,
    turbidez double precision,
    data_coleta date NOT NULL,
    medida_em timestamp(6) with time zone,
    registrado_em timestamp(6) with time zone NOT NULL,
    ponto_id uuid NOT NULL,
    uuid uuid NOT NULL,
    nao_conformidades character varying(500),
    medida character varying(1000),
    registrado_por_cpf character varying(255)
);
ALTER TABLE ONLY public.tb_amostra_agua ADD CONSTRAINT tb_amostra_agua_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_amostra_agua_data ON public.tb_amostra_agua USING btree (data_coleta);
ALTER TABLE ONLY public.tb_amostra_agua ADD CONSTRAINT fk_amostra_agua_ponto FOREIGN KEY (ponto_id) REFERENCES public.tb_ponto_coleta_agua(uuid);

CREATE TABLE public.tb_risco_ambiental (
    latitude double precision,
    longitude double precision,
    populacao_exposta integer,
    controlado_em timestamp(6) with time zone,
    registrado_em timestamp(6) with time zone NOT NULL,
    microarea_id uuid,
    uuid uuid NOT NULL,
    tipo character varying(255) NOT NULL,
    situacao character varying(255) NOT NULL,
    local character varying(300) NOT NULL,
    descricao character varying(1000) NOT NULL,
    motivo_controle character varying(1000),
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_risco_ambiental_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['AREA_CONTAMINADA'::character varying, 'DESCARTE_IRREGULAR'::character varying, 'FONTE_POLUIDORA'::character varying, 'OUTRO'::character varying])::text[]))),
    CONSTRAINT tb_risco_ambiental_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['ATIVO'::character varying, 'CONTROLADO'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_risco_ambiental ADD CONSTRAINT tb_risco_ambiental_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_risco_ambiental ADD CONSTRAINT fk_risco_ambiental_microarea FOREIGN KEY (microarea_id) REFERENCES public.tb_microarea(uuid);
