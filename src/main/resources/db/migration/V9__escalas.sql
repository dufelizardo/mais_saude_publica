-- Escalas (ADR-0105): turnos da unidade com data e hora de início e fim; sem profissional, vaga aberta.
-- Gerado com ddl-auto=create num banco descartável e conferido (ADR-0090).

CREATE TABLE public.tb_turno_escala (
    fim_em timestamp(6) without time zone NOT NULL,
    inicio_em timestamp(6) without time zone NOT NULL,
    equipe_id uuid,
    profissional_id uuid,
    unidade_id uuid NOT NULL,
    uuid uuid NOT NULL,
    descricao character varying(120),
    funcao character varying(255),
    registrado_por_cpf character varying(255),
    tipo character varying(255) NOT NULL,
    CONSTRAINT tb_turno_escala_funcao_check CHECK (((funcao)::text = ANY ((ARRAY['MEDICO'::character varying, 'ENFERMEIRO'::character varying, 'TECNICO_ENFERMAGEM'::character varying, 'AUXILIAR_ENFERMAGEM'::character varying, 'ACS'::character varying, 'CIRURGIAO_DENTISTA'::character varying, 'TECNICO_SAUDE_BUCAL'::character varying, 'AUXILIAR_SAUDE_BUCAL'::character varying, 'PSICOLOGO'::character varying, 'ASSISTENTE_SOCIAL'::character varying, 'OUTRO'::character varying])::text[]))),
    CONSTRAINT tb_turno_escala_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['MANHA'::character varying, 'TARDE'::character varying, 'NOITE'::character varying, 'PLANTAO_12H'::character varying, 'PLANTAO_24H'::character varying, 'SOBREAVISO'::character varying, 'CAPACITACAO'::character varying])::text[])))
);

ALTER TABLE ONLY public.tb_turno_escala
    ADD CONSTRAINT tb_turno_escala_pkey PRIMARY KEY (uuid);

CREATE INDEX ix_turno_escala_profissional ON public.tb_turno_escala USING btree (profissional_id, inicio_em);

CREATE INDEX ix_turno_escala_unidade ON public.tb_turno_escala USING btree (unidade_id, inicio_em);

ALTER TABLE ONLY public.tb_turno_escala
    ADD CONSTRAINT fk31wigtym608hj302n2lhdtngj FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);

ALTER TABLE ONLY public.tb_turno_escala
    ADD CONSTRAINT fk46lf3n3avpe9qg56mwhxipwds FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);

ALTER TABLE ONLY public.tb_turno_escala
    ADD CONSTRAINT fkk3ao8c0uqww0q2v6cbacncbok FOREIGN KEY (equipe_id) REFERENCES public.tb_equipe(uuid);
