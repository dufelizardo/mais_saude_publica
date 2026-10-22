-- Saúde Bucal, fatia SB5 (ADR-0140): o detalhe odontológico da ação coletiva, a avaliação coletiva por participante e
-- o programa Saúde na Escola (PSE) no catálogo. Conferido contra o ddl-auto=validate (ADR-0090).

INSERT INTO public.tb_programa (uuid, codigo, nome, descricao, categoria_id, abrangencia, situacao, periodicidade, sensivel,
                                idade_minima, idade_maxima, publico_sexo, gestante)
SELECT gen_random_uuid(), 'SAUDE_NA_ESCOLA', 'Saúde na Escola (PSE)',
       'Ações de promoção, prevenção e avaliação de saúde nas escolas públicas, pelas equipes da atenção básica.',
       c.uuid, 'NACIONAL', 'ATIVO', 'ANUAL', false, NULL, NULL, NULL, false
FROM public.tb_categoria_programa c
WHERE c.nome = 'Atenção Básica' AND NOT EXISTS (SELECT 1 FROM public.tb_programa p WHERE p.codigo = 'SAUDE_NA_ESCOLA');

CREATE TABLE public.tb_acao_saude_bucal (
    kits_distribuidos integer,
    atualizado_em timestamp(6) with time zone NOT NULL,
    acao_id uuid NOT NULL,
    uuid uuid NOT NULL,
    instituicao character varying(200),
    publico_alvo character varying(300),
    atualizado_por_cpf character varying(255),
    local_tipo character varying(255) NOT NULL,
    CONSTRAINT tb_acao_saude_bucal_local_tipo_check CHECK (((local_tipo)::text = ANY ((ARRAY['ESCOLA'::character varying, 'CRECHE'::character varying, 'UBS'::character varying, 'COMUNIDADE'::character varying, 'EMPRESA'::character varying, 'INSTITUICAO_LONGA_PERMANENCIA'::character varying, 'OUTRO'::character varying])::text[])))
);

ALTER TABLE ONLY public.tb_acao_saude_bucal ADD CONSTRAINT tb_acao_saude_bucal_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_acao_saude_bucal ADD CONSTRAINT uk_acao_saude_bucal_acao UNIQUE (acao_id);
ALTER TABLE ONLY public.tb_acao_saude_bucal ADD CONSTRAINT fk_acao_saude_bucal_acao FOREIGN KEY (acao_id) REFERENCES public.tb_acao_programatica(uuid);

CREATE TABLE public.tb_acao_saude_bucal_atividade (
    detalhe_id uuid NOT NULL,
    atividade character varying(255) NOT NULL,
    CONSTRAINT tb_acao_saude_bucal_atividade_atividade_check CHECK (((atividade)::text = ANY ((ARRAY['ESCOVACAO_SUPERVISIONADA'::character varying, 'APLICACAO_TOPICA_FLUOR'::character varying, 'EDUCACAO_EM_SAUDE'::character varying, 'AVALIACAO_COLETIVA'::character varying, 'LEVANTAMENTO_EPIDEMIOLOGICO'::character varying])::text[])))
);

ALTER TABLE ONLY public.tb_acao_saude_bucal_atividade ADD CONSTRAINT tb_acao_saude_bucal_atividade_pkey PRIMARY KEY (detalhe_id, atividade);
ALTER TABLE ONLY public.tb_acao_saude_bucal_atividade ADD CONSTRAINT fk_acao_saude_bucal_atividade FOREIGN KEY (detalhe_id) REFERENCES public.tb_acao_saude_bucal(uuid);

CREATE TABLE public.tb_avaliacao_bucal_coletiva (
    registrado_em timestamp(6) with time zone NOT NULL,
    acao_id uuid NOT NULL,
    paciente_id uuid NOT NULL,
    uuid uuid NOT NULL,
    observacao character varying(500),
    necessidade_tratamento character varying(255) NOT NULL,
    registrado_por_cpf character varying(255),
    risco character varying(255) NOT NULL,
    CONSTRAINT tb_avaliacao_bucal_coletiva_necessidade_tratamento_check CHECK (((necessidade_tratamento)::text = ANY ((ARRAY['SEM_NECESSIDADE'::character varying, 'ELETIVA'::character varying, 'PRIORITARIA'::character varying, 'URGENCIA'::character varying])::text[]))),
    CONSTRAINT tb_avaliacao_bucal_coletiva_risco_check CHECK (((risco)::text = ANY ((ARRAY['BAIXO'::character varying, 'MEDIO'::character varying, 'ALTO'::character varying])::text[])))
);

ALTER TABLE ONLY public.tb_avaliacao_bucal_coletiva ADD CONSTRAINT tb_avaliacao_bucal_coletiva_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_avaliacao_bucal_coletiva ADD CONSTRAINT uk_avaliacao_bucal_coletiva UNIQUE (acao_id, paciente_id);
CREATE INDEX ix_avaliacao_bucal_coletiva_paciente ON public.tb_avaliacao_bucal_coletiva USING btree (paciente_id);
ALTER TABLE ONLY public.tb_avaliacao_bucal_coletiva ADD CONSTRAINT fk_avaliacao_bucal_coletiva_acao FOREIGN KEY (acao_id) REFERENCES public.tb_acao_programatica(uuid);
ALTER TABLE ONLY public.tb_avaliacao_bucal_coletiva ADD CONSTRAINT fk_avaliacao_bucal_coletiva_paciente FOREIGN KEY (paciente_id) REFERENCES public.tb_paciente(uuid);
