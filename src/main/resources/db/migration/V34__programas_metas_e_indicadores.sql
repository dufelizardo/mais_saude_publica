-- Programas, fatia P6 (ADR-0141): as metas dos indicadores programáticos. Os indicadores são calculados no código, a
-- partir do que o sistema registra; só as metas são guardadas. Conferido contra o ddl-auto=validate (ADR-0090).

CREATE TABLE public.tb_meta_programatica (
    alvo numeric(10,2) NOT NULL,
    fim date NOT NULL,
    inicio date NOT NULL,
    limite_maximo numeric(12,4),
    limite_minimo numeric(12,4),
    criada_em timestamp(6) with time zone NOT NULL,
    exame_id uuid,
    programa_id uuid NOT NULL,
    unidade_id uuid,
    uuid uuid NOT NULL,
    indicador character varying(255) NOT NULL,
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_meta_programatica_indicador_check CHECK (((indicador)::text = ANY ((ARRAY['ACOMPANHADOS_EM_DIA'::character varying, 'COBERTURA'::character varying, 'ACOES_REALIZADAS'::character varying, 'PA_CONTROLADA'::character varying, 'EXAME_NO_ALVO'::character varying, 'PRE_NATAL_6_CONSULTAS'::character varying, 'TRATAMENTO_ODONTOLOGICO_CONCLUIDO'::character varying, 'PROTESES_ENTREGUES'::character varying, 'ESCOVACAO_SUPERVISIONADA'::character varying])::text[])))
);

ALTER TABLE ONLY public.tb_meta_programatica ADD CONSTRAINT tb_meta_programatica_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_meta_programatica_programa ON public.tb_meta_programatica USING btree (programa_id);
ALTER TABLE ONLY public.tb_meta_programatica ADD CONSTRAINT fk_meta_programatica_programa FOREIGN KEY (programa_id) REFERENCES public.tb_programa(uuid);
ALTER TABLE ONLY public.tb_meta_programatica ADD CONSTRAINT fk_meta_programatica_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);
ALTER TABLE ONLY public.tb_meta_programatica ADD CONSTRAINT fk_meta_programatica_exame FOREIGN KEY (exame_id) REFERENCES public.tb_exame_laboratorial(uuid);
