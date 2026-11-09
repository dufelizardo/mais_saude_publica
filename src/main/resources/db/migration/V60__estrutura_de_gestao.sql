-- Coordenações e gerência, fatia BI5 (ADR-0175): a estrutura de gestão e quem responde por cada área, como histórico.
-- Conferido contra o ddl-auto=validate (ADR-0090).

CREATE TABLE public.tb_area_gestao (
    ativa boolean NOT NULL,
    registrada_em timestamp(6) without time zone NOT NULL,
    regional_id uuid,
    superior_id uuid,
    uuid uuid NOT NULL,
    sigla character varying(20),
    tipo character varying(20) NOT NULL,
    tema character varying(30) NOT NULL,
    nome character varying(150) NOT NULL,
    descricao character varying(500),
    CONSTRAINT tb_area_gestao_tipo_check CHECK (tipo IN ('GERENCIA', 'COORDENACAO', 'SUPERVISAO')),
    CONSTRAINT tb_area_gestao_tema_check CHECK (tema IN ('ATENCAO_BASICA', 'ESPECIALIDADES', 'URGENCIA_EMERGENCIA', 'VIGILANCIA', 'SAUDE_MENTAL', 'SAUDE_BUCAL', 'ASSISTENCIA_FARMACEUTICA', 'QUALIDADE', 'GESTAO'))
);
ALTER TABLE ONLY public.tb_area_gestao ADD CONSTRAINT tb_area_gestao_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_area_gestao ADD CONSTRAINT fk_area_gestao_superior FOREIGN KEY (superior_id) REFERENCES public.tb_area_gestao(uuid);
ALTER TABLE ONLY public.tb_area_gestao ADD CONSTRAINT fk_area_gestao_regional FOREIGN KEY (regional_id) REFERENCES public.tb_unidade_de_saude(uuid);

CREATE TABLE public.tb_titularidade_area_gestao (
    fim date,
    inicio date NOT NULL,
    registrada_em timestamp(6) without time zone NOT NULL,
    area_id uuid NOT NULL,
    profissional_id uuid NOT NULL,
    uuid uuid NOT NULL,
    papel character varying(20) NOT NULL,
    ato character varying(120),
    registrada_por_cpf character varying(255),
    CONSTRAINT tb_titularidade_area_gestao_papel_check CHECK (papel IN ('TITULAR', 'SUBSTITUTO'))
);
ALTER TABLE ONLY public.tb_titularidade_area_gestao ADD CONSTRAINT tb_titularidade_area_gestao_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_titularidade_area ON public.tb_titularidade_area_gestao USING btree (area_id);
ALTER TABLE ONLY public.tb_titularidade_area_gestao ADD CONSTRAINT fk_titularidade_area FOREIGN KEY (area_id) REFERENCES public.tb_area_gestao(uuid);
ALTER TABLE ONLY public.tb_titularidade_area_gestao ADD CONSTRAINT fk_titularidade_profissional FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);
