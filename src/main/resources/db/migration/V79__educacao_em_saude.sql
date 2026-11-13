-- Comunicação e Educação em Saúde, fatia C3 (ADR-0195): o detalhe educativo da ação coletiva de Programas (P4): tema, conteúdos
-- revisados, materiais distribuídos, microáreas alvo, campanha de comunicação e a avaliação da equipe. Conferido contra o
-- ddl-auto=validate (ADR-0090).

CREATE TABLE public.tb_acao_educativa (
    atualizado_em timestamp(6) with time zone NOT NULL,
    avaliado_em timestamp(6) with time zone,
    uuid uuid NOT NULL,
    acao_id uuid NOT NULL,
    campanha_comunicacao_id uuid,
    participacao character varying(10),
    tema character varying(120) NOT NULL,
    avaliacao character varying(1000),
    atualizado_por_cpf character varying(255),
    avaliado_por_cpf character varying(255),
    CONSTRAINT tb_acao_educativa_participacao_check CHECK (participacao IN ('BAIXA', 'MEDIA', 'ALTA'))
);
ALTER TABLE ONLY public.tb_acao_educativa ADD CONSTRAINT tb_acao_educativa_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_acao_educativa ADD CONSTRAINT uk_acao_educativa_acao UNIQUE (acao_id);
ALTER TABLE ONLY public.tb_acao_educativa ADD CONSTRAINT fk_acao_educativa_acao FOREIGN KEY (acao_id) REFERENCES public.tb_acao_programatica(uuid);
ALTER TABLE ONLY public.tb_acao_educativa ADD CONSTRAINT fk_acao_educativa_campanha FOREIGN KEY (campanha_comunicacao_id) REFERENCES public.tb_campanha_comunicacao(uuid);
CREATE INDEX ix_acao_educativa_campanha ON public.tb_acao_educativa USING btree (campanha_comunicacao_id);

CREATE TABLE public.tb_acao_educativa_conteudo (
    acao_educativa_id uuid NOT NULL,
    conteudo_grupo_id uuid NOT NULL
);
ALTER TABLE ONLY public.tb_acao_educativa_conteudo ADD CONSTRAINT tb_acao_educativa_conteudo_pkey PRIMARY KEY (acao_educativa_id, conteudo_grupo_id);
ALTER TABLE ONLY public.tb_acao_educativa_conteudo ADD CONSTRAINT fk_acao_educativa_conteudo FOREIGN KEY (acao_educativa_id) REFERENCES public.tb_acao_educativa(uuid);

CREATE TABLE public.tb_acao_educativa_microarea (
    acao_educativa_id uuid NOT NULL,
    microarea_id uuid NOT NULL
);
ALTER TABLE ONLY public.tb_acao_educativa_microarea ADD CONSTRAINT tb_acao_educativa_microarea_pkey PRIMARY KEY (acao_educativa_id, microarea_id);
ALTER TABLE ONLY public.tb_acao_educativa_microarea ADD CONSTRAINT fk_acao_educativa_microarea_acao FOREIGN KEY (acao_educativa_id) REFERENCES public.tb_acao_educativa(uuid);
ALTER TABLE ONLY public.tb_acao_educativa_microarea ADD CONSTRAINT fk_acao_educativa_microarea FOREIGN KEY (microarea_id) REFERENCES public.tb_microarea(uuid);

CREATE TABLE public.tb_acao_educativa_material (
    quantidade integer NOT NULL,
    uuid uuid NOT NULL,
    acao_educativa_id uuid NOT NULL,
    material_id uuid NOT NULL
);
ALTER TABLE ONLY public.tb_acao_educativa_material ADD CONSTRAINT tb_acao_educativa_material_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_acao_educativa_material ADD CONSTRAINT fk_acao_educativa_material_acao FOREIGN KEY (acao_educativa_id) REFERENCES public.tb_acao_educativa(uuid);
ALTER TABLE ONLY public.tb_acao_educativa_material ADD CONSTRAINT fk_acao_educativa_material FOREIGN KEY (material_id) REFERENCES public.tb_material_educativo(uuid);
