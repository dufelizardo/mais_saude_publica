-- Laboratório, fatia L1 (ADR-0180): faixas de referência por sexo e idade, valores críticos na faixa geral e em cada faixa, a
-- cópia dos críticos e da descrição da faixa usada no resultado, e as interpretações críticas. Conferido contra o
-- ddl-auto=validate (ADR-0090).

CREATE TABLE public.tb_faixa_referencia_exame (
    idade_minima integer NOT NULL,
    idade_maxima integer,
    referencia_minima numeric(14,4),
    referencia_maxima numeric(14,4),
    critico_minimo numeric(14,4),
    critico_maximo numeric(14,4),
    exame_id uuid NOT NULL,
    uuid uuid NOT NULL,
    descricao character varying(100),
    sexo character varying(255),
    unidade_idade_minima character varying(255) NOT NULL,
    unidade_idade_maxima character varying(255),
    CONSTRAINT tb_faixa_referencia_exame_sexo_check CHECK (((sexo)::text = ANY ((ARRAY['MASCULINO'::character varying, 'FEMININO'::character varying, 'IGNORADO'::character varying])::text[]))),
    CONSTRAINT tb_faixa_referencia_exame_unidade_idade_minima_check CHECK (((unidade_idade_minima)::text = ANY ((ARRAY['DIAS'::character varying, 'MESES'::character varying, 'ANOS'::character varying])::text[]))),
    CONSTRAINT tb_faixa_referencia_exame_unidade_idade_maxima_check CHECK (((unidade_idade_maxima)::text = ANY ((ARRAY['DIAS'::character varying, 'MESES'::character varying, 'ANOS'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_faixa_referencia_exame ADD CONSTRAINT tb_faixa_referencia_exame_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_faixa_referencia_exame ADD CONSTRAINT fk_faixa_referencia_exame FOREIGN KEY (exame_id) REFERENCES public.tb_exame_laboratorial(uuid);
CREATE INDEX ix_faixa_referencia_exame ON public.tb_faixa_referencia_exame USING btree (exame_id);

ALTER TABLE public.tb_exame_laboratorial ADD COLUMN critico_minimo numeric(14,4);
ALTER TABLE public.tb_exame_laboratorial ADD COLUMN critico_maximo numeric(14,4);

ALTER TABLE public.tb_resultado_exame ADD COLUMN critico_minimo numeric(14,4);
ALTER TABLE public.tb_resultado_exame ADD COLUMN critico_maximo numeric(14,4);
ALTER TABLE public.tb_resultado_exame ADD COLUMN faixa_descricao character varying(200);

ALTER TABLE public.tb_resultado_exame DROP CONSTRAINT tb_resultado_exame_interpretacao_check;
ALTER TABLE public.tb_resultado_exame ADD CONSTRAINT tb_resultado_exame_interpretacao_check CHECK (((interpretacao)::text = ANY ((ARRAY['NORMAL'::character varying, 'ACIMA'::character varying, 'ABAIXO'::character varying, 'CRITICO_ALTO'::character varying, 'CRITICO_BAIXO'::character varying])::text[])));
