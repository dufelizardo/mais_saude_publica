-- Vigilância epidemiológica, fatia V2b (ADR-0131): o surto (casos do mesmo agravo num território ou estabelecimento),
-- as microáreas afetadas, o surto de cada caso e as medidas de controle do surto ou do caso. Conferido contra o
-- ddl-auto=validate (ADR-0090).

CREATE TABLE public.tb_surto (
    inicio date NOT NULL,
    fim date,
    aberto_em timestamp(6) with time zone NOT NULL,
    encerrado_em timestamp(6) with time zone,
    agravo_id uuid NOT NULL,
    unidade_id uuid NOT NULL,
    uuid uuid NOT NULL,
    nome character varying(200) NOT NULL,
    nome_instituicao character varying(200),
    endereco_instituicao character varying(500),
    definicao_caso character varying(2000),
    conclusao character varying(2000),
    registrado_por_cpf character varying(255),
    situacao character varying(255) NOT NULL,
    tipo character varying(255) NOT NULL,
    tipo_instituicao character varying(255),
    CONSTRAINT tb_surto_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['SUSPEITO'::character varying, 'EM_INVESTIGACAO'::character varying, 'CONFIRMADO'::character varying, 'DESCARTADO'::character varying, 'ENCERRADO'::character varying])::text[]))),
    CONSTRAINT tb_surto_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['COMUNITARIO'::character varying, 'INSTITUCIONAL'::character varying])::text[]))),
    CONSTRAINT tb_surto_tipo_instituicao_check CHECK (((tipo_instituicao)::text = ANY ((ARRAY['ESCOLA'::character varying, 'CRECHE'::character varying, 'SERVICO_SAUDE'::character varying, 'ILPI'::character varying, 'PRISIONAL'::character varying, 'ALOJAMENTO'::character varying, 'EMPRESA'::character varying, 'OUTRO'::character varying])::text[])))
);

ALTER TABLE ONLY public.tb_surto ADD CONSTRAINT tb_surto_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_surto_unidade ON public.tb_surto USING btree (unidade_id);
ALTER TABLE ONLY public.tb_surto ADD CONSTRAINT fk_surto_agravo FOREIGN KEY (agravo_id) REFERENCES public.tb_agravo(uuid);
ALTER TABLE ONLY public.tb_surto ADD CONSTRAINT fk_surto_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);

CREATE TABLE public.tb_surto_microarea (
    surto_id uuid NOT NULL,
    microarea_id uuid NOT NULL
);

ALTER TABLE ONLY public.tb_surto_microarea ADD CONSTRAINT tb_surto_microarea_pkey PRIMARY KEY (surto_id, microarea_id);
ALTER TABLE ONLY public.tb_surto_microarea ADD CONSTRAINT fk_surto_microarea_surto FOREIGN KEY (surto_id) REFERENCES public.tb_surto(uuid);
ALTER TABLE ONLY public.tb_surto_microarea ADD CONSTRAINT fk_surto_microarea_microarea FOREIGN KEY (microarea_id) REFERENCES public.tb_microarea(uuid);

ALTER TABLE public.tb_notificacao ADD COLUMN surto_id uuid;
ALTER TABLE public.tb_notificacao ADD COLUMN surto_incluida_em timestamp(6) with time zone;
ALTER TABLE ONLY public.tb_notificacao ADD CONSTRAINT fk_notificacao_surto FOREIGN KEY (surto_id) REFERENCES public.tb_surto(uuid);
CREATE INDEX ix_notificacao_surto ON public.tb_notificacao USING btree (surto_id);

CREATE TABLE public.tb_medida_controle (
    prazo date,
    criada_em timestamp(6) with time zone NOT NULL,
    concluida_em timestamp(6) with time zone,
    notificacao_id uuid,
    responsavel_id uuid,
    surto_id uuid,
    uuid uuid NOT NULL,
    descricao character varying(1000) NOT NULL,
    resultado character varying(2000),
    registrado_por_cpf character varying(255),
    situacao character varying(255) NOT NULL,
    tipo character varying(255) NOT NULL,
    CONSTRAINT tb_medida_controle_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['PLANEJADA'::character varying, 'EM_ANDAMENTO'::character varying, 'CONCLUIDA'::character varying, 'CANCELADA'::character varying])::text[]))),
    CONSTRAINT tb_medida_controle_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['BLOQUEIO_VACINAL'::character varying, 'ISOLAMENTO'::character varying, 'QUIMIOPROFILAXIA'::character varying, 'CONTROLE_VETORIAL'::character varying, 'BUSCA_ATIVA'::character varying, 'EDUCACAO_EM_SAUDE'::character varying, 'INTERDICAO'::character varying, 'OUTRA'::character varying])::text[]))),
    -- Do surto ou do caso, um dos dois.
    CONSTRAINT tb_medida_controle_alvo_check CHECK (((surto_id IS NULL) <> (notificacao_id IS NULL)))
);

ALTER TABLE ONLY public.tb_medida_controle ADD CONSTRAINT tb_medida_controle_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_medida_surto ON public.tb_medida_controle USING btree (surto_id);
CREATE INDEX ix_medida_notificacao ON public.tb_medida_controle USING btree (notificacao_id);
ALTER TABLE ONLY public.tb_medida_controle ADD CONSTRAINT fk_medida_surto FOREIGN KEY (surto_id) REFERENCES public.tb_surto(uuid);
ALTER TABLE ONLY public.tb_medida_controle ADD CONSTRAINT fk_medida_notificacao FOREIGN KEY (notificacao_id) REFERENCES public.tb_notificacao(uuid);
ALTER TABLE ONLY public.tb_medida_controle ADD CONSTRAINT fk_medida_responsavel FOREIGN KEY (responsavel_id) REFERENCES public.tb_profissional(uuid);
