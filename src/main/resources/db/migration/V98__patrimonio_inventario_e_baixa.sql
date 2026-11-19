-- Patrimônio e Manutenção, fatia PM3 (ADR-0217, desenho do domínio na ADR-0215): o inventário anual por unidade com a comissão, a
-- conferência item a item, os achados a mais e o fechamento com as divergências (Lei 4.320/1964, arts. 94 a 96); e a baixa do bem, com a
-- confirmação da gestão acima do valor de alçada. Conferido contra o ddl-auto=validate (ADR-0090).

CREATE TABLE public.tb_inventario_patrimonio (
    aberto_em date NOT NULL,
    fechado_em date,
    registrado_em timestamp(6) with time zone NOT NULL,
    unidade_id uuid NOT NULL,
    uuid uuid NOT NULL,
    numero character varying(20) NOT NULL,
    observacao character varying(500),
    aberto_por_cpf character varying(255),
    fechado_por_cpf character varying(255),
    situacao character varying(255) NOT NULL,
    CONSTRAINT tb_inventario_patrimonio_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['ABERTO'::character varying, 'FECHADO'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_inventario_patrimonio ADD CONSTRAINT tb_inventario_patrimonio_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_inventario_patrimonio ADD CONSTRAINT uk_inventario_patrimonio_numero UNIQUE (numero);
CREATE INDEX ix_inventario_patrimonio_unidade ON public.tb_inventario_patrimonio USING btree (unidade_id, situacao);
ALTER TABLE ONLY public.tb_inventario_patrimonio ADD CONSTRAINT fk_inventario_patrimonio_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);

CREATE TABLE public.tb_membro_inventario (
    inventario_id uuid NOT NULL,
    profissional_id uuid NOT NULL,
    uuid uuid NOT NULL
);
ALTER TABLE ONLY public.tb_membro_inventario ADD CONSTRAINT tb_membro_inventario_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_membro_inventario ON public.tb_membro_inventario USING btree (inventario_id);
ALTER TABLE ONLY public.tb_membro_inventario ADD CONSTRAINT fk_membro_inventario FOREIGN KEY (inventario_id) REFERENCES public.tb_inventario_patrimonio(uuid);
ALTER TABLE ONLY public.tb_membro_inventario ADD CONSTRAINT fk_membro_inventario_profissional FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);

CREATE TABLE public.tb_item_inventario (
    conferido_em date,
    bem_id uuid NOT NULL,
    inventario_id uuid NOT NULL,
    setor_encontrado_id uuid,
    setor_esperado_id uuid,
    unidade_encontrada_id uuid,
    uuid uuid NOT NULL,
    observacao character varying(500),
    acao character varying(255),
    conferido_por_cpf character varying(255),
    conservacao character varying(255),
    resultado character varying(255),
    CONSTRAINT tb_item_inventario_acao_check CHECK (((acao)::text = ANY ((ARRAY['SETOR_CORRIGIDO'::character varying, 'TRANSFERENCIA'::character varying, 'BAIXA_SUGERIDA'::character varying])::text[]))),
    CONSTRAINT tb_item_inventario_conservacao_check CHECK (((conservacao)::text = ANY ((ARRAY['BOM'::character varying, 'REGULAR'::character varying, 'RUIM'::character varying, 'INSERVIVEL'::character varying])::text[]))),
    CONSTRAINT tb_item_inventario_resultado_check CHECK (((resultado)::text = ANY ((ARRAY['ENCONTRADO'::character varying, 'NAO_ENCONTRADO'::character varying, 'OUTRO_LOCAL'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_item_inventario ADD CONSTRAINT tb_item_inventario_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_item_inventario ON public.tb_item_inventario USING btree (inventario_id);
ALTER TABLE ONLY public.tb_item_inventario ADD CONSTRAINT fk_item_inventario FOREIGN KEY (inventario_id) REFERENCES public.tb_inventario_patrimonio(uuid);
ALTER TABLE ONLY public.tb_item_inventario ADD CONSTRAINT fk_item_inventario_bem FOREIGN KEY (bem_id) REFERENCES public.tb_bem_patrimonial(uuid);
ALTER TABLE ONLY public.tb_item_inventario ADD CONSTRAINT fk_item_inventario_setor_esperado FOREIGN KEY (setor_esperado_id) REFERENCES public.tb_setor(uuid);
ALTER TABLE ONLY public.tb_item_inventario ADD CONSTRAINT fk_item_inventario_setor_encontrado FOREIGN KEY (setor_encontrado_id) REFERENCES public.tb_setor(uuid);
ALTER TABLE ONLY public.tb_item_inventario ADD CONSTRAINT fk_item_inventario_unidade_encontrada FOREIGN KEY (unidade_encontrada_id) REFERENCES public.tb_unidade_de_saude(uuid);

CREATE TABLE public.tb_achado_inventario (
    registrado_em timestamp(6) with time zone NOT NULL,
    inventario_id uuid NOT NULL,
    uuid uuid NOT NULL,
    local character varying(200),
    descricao character varying(500) NOT NULL,
    registrado_por_cpf character varying(255)
);
ALTER TABLE ONLY public.tb_achado_inventario ADD CONSTRAINT tb_achado_inventario_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_achado_inventario ON public.tb_achado_inventario USING btree (inventario_id);
ALTER TABLE ONLY public.tb_achado_inventario ADD CONSTRAINT fk_achado_inventario FOREIGN KEY (inventario_id) REFERENCES public.tb_inventario_patrimonio(uuid);

CREATE TABLE public.tb_baixa_bem (
    data_baixa date NOT NULL,
    resolvida_em date,
    valor_contabil numeric(15,2),
    registrado_em timestamp(6) with time zone NOT NULL,
    bem_id uuid NOT NULL,
    uuid uuid NOT NULL,
    boletim_ocorrencia character varying(60),
    processo character varying(60) NOT NULL,
    motivo_recusa character varying(500),
    observacao character varying(500),
    motivo character varying(255) NOT NULL,
    registrado_por_cpf character varying(255),
    resolvida_por_cpf character varying(255),
    situacao character varying(255) NOT NULL,
    CONSTRAINT tb_baixa_bem_motivo_check CHECK (((motivo)::text = ANY ((ARRAY['INSERVIVEL'::character varying, 'EXTRAVIO'::character varying, 'FURTO_ROUBO'::character varying, 'DOACAO'::character varying, 'ALIENACAO'::character varying, 'DEVOLUCAO_CESSAO'::character varying])::text[]))),
    CONSTRAINT tb_baixa_bem_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['PENDENTE'::character varying, 'CONFIRMADA'::character varying, 'RECUSADA'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_baixa_bem ADD CONSTRAINT tb_baixa_bem_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_baixa_bem ON public.tb_baixa_bem USING btree (bem_id, situacao);
ALTER TABLE ONLY public.tb_baixa_bem ADD CONSTRAINT fk_baixa_bem FOREIGN KEY (bem_id) REFERENCES public.tb_bem_patrimonial(uuid);
