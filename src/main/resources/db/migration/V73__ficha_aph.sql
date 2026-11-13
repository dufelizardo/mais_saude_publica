-- Regulação, fatia RL5 (ADR-0188): a ficha clínica do atendimento pré-hospitalar da ocorrência de urgência, com os procedimentos, os
-- sinais vitais em série, os medicamentos e os adendos. Conferido contra o ddl-auto=validate (ADR-0090). A permissão APH.REGISTRAR e o
-- papel Equipe de intervenção do SAMU vêm do CatalogoDeAcesso.

CREATE TABLE public.tb_ficha_aph (
    glasgow_motor integer,
    glasgow_ocular integer,
    glasgow_verbal integer,
    hemorragia_exsanguinante boolean,
    aberta_em timestamp(6) with time zone NOT NULL,
    atualizada_em timestamp(6) with time zone,
    entregue_em timestamp(6) with time zone,
    finalizada_em timestamp(6) with time zone,
    obito_em timestamp(6) with time zone,
    finalizada_por_id uuid,
    obito_constatado_por_id uuid,
    ocorrencia_id uuid NOT NULL,
    uuid uuid NOT NULL,
    cid character varying(10),
    pupilas character varying(200),
    recebido_por character varying(200),
    recusa_nome character varying(200),
    circulacao character varying(500),
    exposicao character varying(500),
    procedimento_outro character varying(500),
    respiracao character varying(500),
    via_aerea character varying(500),
    impressao_diagnostica character varying(1000),
    situacao_encontrada character varying(2000),
    aberta_por_cpf character varying(255),
    desfecho character varying(255),
    mecanismo_trauma character varying(255),
    natureza character varying(255),
    CONSTRAINT tb_ficha_aph_desfecho_check CHECK (((desfecho)::text = ANY ((ARRAY['REMOVIDO_AO_DESTINO'::character varying, 'LIBERADO_NO_LOCAL'::character varying, 'RECUSA_DE_REMOCAO'::character varying, 'OBITO_NO_LOCAL'::character varying, 'EVASAO'::character varying])::text[]))),
    CONSTRAINT tb_ficha_aph_mecanismo_trauma_check CHECK (((mecanismo_trauma)::text = ANY ((ARRAY['QUEDA'::character varying, 'COLISAO_AUTOMOVEL'::character varying, 'ACIDENTE_MOTO'::character varying, 'ATROPELAMENTO'::character varying, 'ARMA_DE_FOGO'::character varying, 'ARMA_BRANCA'::character varying, 'AGRESSAO'::character varying, 'QUEIMADURA'::character varying, 'AFOGAMENTO'::character varying, 'OUTRO'::character varying])::text[]))),
    CONSTRAINT tb_ficha_aph_natureza_check CHECK (((natureza)::text = ANY ((ARRAY['CLINICA'::character varying, 'TRAUMA'::character varying, 'OBSTETRICA'::character varying, 'PSIQUIATRICA'::character varying, 'PEDIATRICA'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_ficha_aph ADD CONSTRAINT tb_ficha_aph_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_ficha_aph ADD CONSTRAINT uk_ficha_aph_ocorrencia UNIQUE (ocorrencia_id);
ALTER TABLE ONLY public.tb_ficha_aph ADD CONSTRAINT fk_ficha_aph_ocorrencia FOREIGN KEY (ocorrencia_id) REFERENCES public.tb_ocorrencia_urgencia(uuid);
ALTER TABLE ONLY public.tb_ficha_aph ADD CONSTRAINT fk_ficha_aph_obito FOREIGN KEY (obito_constatado_por_id) REFERENCES public.tb_profissional(uuid);
ALTER TABLE ONLY public.tb_ficha_aph ADD CONSTRAINT fk_ficha_aph_finalizada_por FOREIGN KEY (finalizada_por_id) REFERENCES public.tb_profissional(uuid);

CREATE TABLE public.tb_ficha_aph_procedimento (
    ficha_id uuid NOT NULL,
    procedimento character varying(255) NOT NULL,
    CONSTRAINT tb_ficha_aph_procedimento_procedimento_check CHECK (((procedimento)::text = ANY ((ARRAY['OXIGENIO'::character varying, 'INTUBACAO'::character varying, 'ACESSO_VENOSO'::character varying, 'COLAR_CERVICAL'::character varying, 'PRANCHA_IMOBILIZACAO'::character varying, 'TALA'::character varying, 'CURATIVO_COMPRESSAO'::character varying, 'TORNIQUETE'::character varying, 'RCP'::character varying, 'DESFIBRILACAO'::character varying, 'MONITORIZACAO'::character varying, 'PARTO'::character varying, 'OUTRO'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_ficha_aph_procedimento ADD CONSTRAINT tb_ficha_aph_procedimento_pkey PRIMARY KEY (ficha_id, procedimento);
ALTER TABLE ONLY public.tb_ficha_aph_procedimento ADD CONSTRAINT fk_ficha_aph_procedimento FOREIGN KEY (ficha_id) REFERENCES public.tb_ficha_aph(uuid);

CREATE TABLE public.tb_sinais_vitais_aph (
    dor integer,
    frequencia_cardiaca integer,
    frequencia_respiratoria integer,
    glasgow integer,
    glicemia integer,
    pa_diastolica integer,
    pa_sistolica integer,
    saturacao integer,
    temperatura numeric(4,1),
    medido_em timestamp(6) with time zone NOT NULL,
    registrado_em timestamp(6) with time zone NOT NULL,
    ficha_id uuid NOT NULL,
    profissional_id uuid NOT NULL,
    uuid uuid NOT NULL,
    registrado_por_cpf character varying(255)
);
ALTER TABLE ONLY public.tb_sinais_vitais_aph ADD CONSTRAINT tb_sinais_vitais_aph_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_sinais_vitais_aph ADD CONSTRAINT fk_sinais_vitais_aph_ficha FOREIGN KEY (ficha_id) REFERENCES public.tb_ficha_aph(uuid);
ALTER TABLE ONLY public.tb_sinais_vitais_aph ADD CONSTRAINT fk_sinais_vitais_aph_profissional FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);
CREATE INDEX ix_sinais_vitais_aph_ficha ON public.tb_sinais_vitais_aph USING btree (ficha_id, medido_em);

CREATE TABLE public.tb_medicamento_aph (
    administrado_em timestamp(6) with time zone NOT NULL,
    registrado_em timestamp(6) with time zone NOT NULL,
    ficha_id uuid NOT NULL,
    profissional_id uuid NOT NULL,
    uuid uuid NOT NULL,
    dose character varying(100) NOT NULL,
    nome character varying(200) NOT NULL,
    registrado_por_cpf character varying(255),
    via character varying(255) NOT NULL,
    CONSTRAINT tb_medicamento_aph_via_check CHECK (((via)::text = ANY ((ARRAY['ORAL'::character varying, 'SUBLINGUAL'::character varying, 'INTRAMUSCULAR'::character varying, 'INTRAVENOSA'::character varying, 'SUBCUTANEA'::character varying, 'TOPICA'::character varying, 'INALATORIA'::character varying, 'RETAL'::character varying, 'OUTRA'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_medicamento_aph ADD CONSTRAINT tb_medicamento_aph_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_medicamento_aph ADD CONSTRAINT fk_medicamento_aph_ficha FOREIGN KEY (ficha_id) REFERENCES public.tb_ficha_aph(uuid);
ALTER TABLE ONLY public.tb_medicamento_aph ADD CONSTRAINT fk_medicamento_aph_profissional FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);
CREATE INDEX ix_medicamento_aph_ficha ON public.tb_medicamento_aph USING btree (ficha_id, administrado_em);

CREATE TABLE public.tb_adendo_ficha_aph (
    registrado_em timestamp(6) with time zone NOT NULL,
    ficha_id uuid NOT NULL,
    profissional_id uuid NOT NULL,
    uuid uuid NOT NULL,
    texto character varying(2000) NOT NULL,
    registrado_por_cpf character varying(255)
);
ALTER TABLE ONLY public.tb_adendo_ficha_aph ADD CONSTRAINT tb_adendo_ficha_aph_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_adendo_ficha_aph ADD CONSTRAINT fk_adendo_ficha_aph_ficha FOREIGN KEY (ficha_id) REFERENCES public.tb_ficha_aph(uuid);
ALTER TABLE ONLY public.tb_adendo_ficha_aph ADD CONSTRAINT fk_adendo_ficha_aph_profissional FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);
CREATE INDEX ix_adendo_ficha_aph_ficha ON public.tb_adendo_ficha_aph USING btree (ficha_id, registrado_em);
