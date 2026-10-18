-- Acompanhamento programático, fatia P2 (ADR-0119): o paciente no programa, com início, fim, situação e responsáveis,
-- e as mudanças de situação só de inclusão. Tabelas conferidas contra o ddl-auto=validate (ADR-0090). A permissão
-- PROGRAMA.ACOMPANHAR nasce na subida pelo catálogo de acesso, que a acrescenta aos papéis padrão (ADR-0087).

CREATE TABLE public.tb_acompanhamento_programatico (
    fim date,
    inicio date NOT NULL,
    equipe_id uuid,
    paciente_id uuid NOT NULL,
    profissional_id uuid,
    programa_id uuid NOT NULL,
    unidade_id uuid NOT NULL,
    uuid uuid NOT NULL,
    observacao_saida character varying(500),
    motivo_saida character varying(255),
    registrado_por_cpf character varying(255),
    situacao character varying(255) NOT NULL,
    CONSTRAINT tb_acompanhamento_programatico_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['ATIVO'::character varying, 'FALTOSO'::character varying, 'CONCLUIDO'::character varying, 'ENCERRADO'::character varying])::text[]))),
    CONSTRAINT tb_acompanhamento_programatico_motivo_saida_check CHECK (((motivo_saida)::text = ANY ((ARRAY['ALTA'::character varying, 'TRANSFERENCIA'::character varying, 'MUDANCA_DE_MUNICIPIO'::character varying, 'ABANDONO'::character varying, 'OBITO'::character varying, 'OUTRO'::character varying])::text[])))
);

CREATE TABLE public.tb_evento_acompanhamento (
    ocorrido_em timestamp(6) with time zone NOT NULL,
    acompanhamento_id uuid NOT NULL,
    uuid uuid NOT NULL,
    motivo character varying(500),
    registrado_por_cpf character varying(255),
    situacao character varying(255) NOT NULL,
    situacao_anterior character varying(255),
    CONSTRAINT tb_evento_acompanhamento_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['ATIVO'::character varying, 'FALTOSO'::character varying, 'CONCLUIDO'::character varying, 'ENCERRADO'::character varying])::text[]))),
    CONSTRAINT tb_evento_acompanhamento_situacao_anterior_check CHECK (((situacao_anterior)::text = ANY ((ARRAY['ATIVO'::character varying, 'FALTOSO'::character varying, 'CONCLUIDO'::character varying, 'ENCERRADO'::character varying])::text[])))
);

ALTER TABLE ONLY public.tb_acompanhamento_programatico ADD CONSTRAINT tb_acompanhamento_programatico_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_evento_acompanhamento ADD CONSTRAINT tb_evento_acompanhamento_pkey PRIMARY KEY (uuid);

CREATE INDEX ix_acompanhamento_paciente ON public.tb_acompanhamento_programatico USING btree (paciente_id, fim);
CREATE INDEX ix_acompanhamento_programa ON public.tb_acompanhamento_programatico USING btree (programa_id, fim);
CREATE INDEX ix_evento_acompanhamento ON public.tb_evento_acompanhamento USING btree (acompanhamento_id, ocorrido_em);

ALTER TABLE ONLY public.tb_acompanhamento_programatico
    ADD CONSTRAINT fk_acompanhamento_paciente FOREIGN KEY (paciente_id) REFERENCES public.tb_paciente(uuid);
ALTER TABLE ONLY public.tb_acompanhamento_programatico
    ADD CONSTRAINT fk_acompanhamento_programa FOREIGN KEY (programa_id) REFERENCES public.tb_programa(uuid);
ALTER TABLE ONLY public.tb_acompanhamento_programatico
    ADD CONSTRAINT fk_acompanhamento_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);
ALTER TABLE ONLY public.tb_acompanhamento_programatico
    ADD CONSTRAINT fk_acompanhamento_equipe FOREIGN KEY (equipe_id) REFERENCES public.tb_equipe(uuid);
ALTER TABLE ONLY public.tb_acompanhamento_programatico
    ADD CONSTRAINT fk_acompanhamento_profissional FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);
ALTER TABLE ONLY public.tb_evento_acompanhamento
    ADD CONSTRAINT fk_evento_acompanhamento FOREIGN KEY (acompanhamento_id) REFERENCES public.tb_acompanhamento_programatico(uuid);
