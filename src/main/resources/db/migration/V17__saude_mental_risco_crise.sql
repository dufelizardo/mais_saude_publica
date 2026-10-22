-- Saúde Mental, fatia SM3 (ADR-0123): risco com histórico no caso, evento de crise com etapas e, na Internação, a
-- modalidade psiquiátrica e a comunicação da involuntária ao Ministério Público (Lei 10.216/2001, arts. 6º e 8º).
-- Conferido contra o ddl-auto=validate (ADR-0090). A permissão SAUDE_MENTAL.CRISE nasce pelo catálogo de acesso.

CREATE TABLE public.tb_avaliacao_risco (
    plano_seguranca boolean NOT NULL,
    reavaliar_em date,
    avaliada_em timestamp(6) with time zone NOT NULL,
    acompanhamento_id uuid NOT NULL,
    profissional_id uuid,
    uuid uuid NOT NULL,
    frequencia_contato character varying(100),
    fatores character varying(2000),
    medidas character varying(2000),
    nivel character varying(255) NOT NULL,
    registrado_por_cpf character varying(255),
    tipo character varying(255) NOT NULL,
    CONSTRAINT tb_avaliacao_risco_nivel_check CHECK (((nivel)::text = ANY ((ARRAY['SEM_RISCO_ATUAL'::character varying, 'BAIXO'::character varying, 'MODERADO'::character varying, 'ALTO'::character varying])::text[]))),
    CONSTRAINT tb_avaliacao_risco_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['AUTOAGRESSAO'::character varying, 'HETEROAGRESSAO'::character varying, 'USO_DE_SUBSTANCIAS'::character varying, 'VULNERABILIDADE'::character varying, 'OUTRO'::character varying])::text[])))
);

CREATE TABLE public.tb_evento_crise (
    notificacao_sugerida boolean NOT NULL,
    notificada_em date,
    encerrada_em timestamp(6) with time zone,
    identificada_em timestamp(6) with time zone NOT NULL,
    acompanhamento_id uuid,
    paciente_id uuid NOT NULL,
    profissional_id uuid,
    unidade_id uuid NOT NULL,
    uuid uuid NOT NULL,
    notificacao_numero character varying(40),
    local_ocorrencia character varying(200),
    descricao character varying(2000) NOT NULL,
    desfecho character varying(255),
    notificacao_registrada_por_cpf character varying(255),
    registrado_por_cpf character varying(255),
    situacao character varying(255) NOT NULL,
    tipo character varying(255) NOT NULL,
    CONSTRAINT tb_evento_crise_desfecho_check CHECK (((desfecho)::text = ANY ((ARRAY['PERMANECEU_NA_UNIDADE'::character varying, 'RETORNO_AO_DOMICILIO'::character varying, 'ENCAMINHADO_A_URGENCIA'::character varying, 'INTERNACAO'::character varying, 'OUTRO'::character varying])::text[]))),
    CONSTRAINT tb_evento_crise_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['EM_CURSO'::character varying, 'ENCERRADA'::character varying])::text[]))),
    CONSTRAINT tb_evento_crise_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['AUTOAGRESSAO'::character varying, 'TENTATIVA_DE_SUICIDIO'::character varying, 'HETEROAGRESSAO'::character varying, 'AGITACAO_PSICOMOTORA'::character varying, 'SURTO_PSICOTICO'::character varying, 'INTOXICACAO'::character varying, 'OUTRO'::character varying])::text[])))
);

CREATE TABLE public.tb_etapa_crise (
    ocorrida_em timestamp(6) with time zone NOT NULL,
    crise_id uuid NOT NULL,
    profissional_id uuid,
    uuid uuid NOT NULL,
    descricao character varying(2000) NOT NULL,
    etapa character varying(255) NOT NULL,
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_etapa_crise_etapa_check CHECK (((etapa)::text = ANY ((ARRAY['AVALIACAO'::character varying, 'INTERVENCAO'::character varying, 'ACIONAMENTO_DA_REDE'::character varying, 'ENCAMINHAMENTO'::character varying, 'POS_CRISE'::character varying])::text[])))
);

CREATE TABLE public.tb_comunicacao_ministerio_publico (
    comunicada_em timestamp(6) with time zone NOT NULL,
    internacao_id uuid NOT NULL,
    responsavel_id uuid,
    uuid uuid NOT NULL,
    protocolo character varying(60),
    observacao character varying(500),
    momento character varying(255) NOT NULL,
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_comunicacao_ministerio_publico_momento_check CHECK (((momento)::text = ANY ((ARRAY['ADMISSAO'::character varying, 'ALTA'::character varying])::text[])))
);

ALTER TABLE public.tb_internacao ADD COLUMN modalidade character varying(255);
ALTER TABLE public.tb_internacao ADD COLUMN ordem_judicial character varying(60);
ALTER TABLE public.tb_internacao ADD CONSTRAINT tb_internacao_modalidade_check CHECK (((modalidade)::text = ANY ((ARRAY['VOLUNTARIA'::character varying, 'INVOLUNTARIA'::character varying, 'COMPULSORIA'::character varying])::text[])));

ALTER TABLE ONLY public.tb_avaliacao_risco ADD CONSTRAINT tb_avaliacao_risco_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_evento_crise ADD CONSTRAINT tb_evento_crise_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_etapa_crise ADD CONSTRAINT tb_etapa_crise_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_comunicacao_ministerio_publico ADD CONSTRAINT tb_comunicacao_ministerio_publico_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_comunicacao_ministerio_publico ADD CONSTRAINT uk_comunicacao_mp_momento UNIQUE (internacao_id, momento);

CREATE INDEX ix_avaliacao_risco_acomp ON public.tb_avaliacao_risco USING btree (acompanhamento_id, avaliada_em);
CREATE INDEX ix_evento_crise_paciente ON public.tb_evento_crise USING btree (paciente_id, identificada_em);
CREATE INDEX ix_evento_crise_unidade ON public.tb_evento_crise USING btree (unidade_id, situacao);
CREATE INDEX ix_etapa_crise ON public.tb_etapa_crise USING btree (crise_id, ocorrida_em);

ALTER TABLE ONLY public.tb_avaliacao_risco
    ADD CONSTRAINT fk_avaliacao_risco_acomp FOREIGN KEY (acompanhamento_id) REFERENCES public.tb_acompanhamento_saude_mental(uuid);
ALTER TABLE ONLY public.tb_avaliacao_risco
    ADD CONSTRAINT fk_avaliacao_risco_profissional FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);
ALTER TABLE ONLY public.tb_evento_crise
    ADD CONSTRAINT fk_evento_crise_paciente FOREIGN KEY (paciente_id) REFERENCES public.tb_paciente(uuid);
ALTER TABLE ONLY public.tb_evento_crise
    ADD CONSTRAINT fk_evento_crise_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);
ALTER TABLE ONLY public.tb_evento_crise
    ADD CONSTRAINT fk_evento_crise_acomp FOREIGN KEY (acompanhamento_id) REFERENCES public.tb_acompanhamento_saude_mental(uuid);
ALTER TABLE ONLY public.tb_evento_crise
    ADD CONSTRAINT fk_evento_crise_profissional FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);
ALTER TABLE ONLY public.tb_etapa_crise
    ADD CONSTRAINT fk_etapa_crise FOREIGN KEY (crise_id) REFERENCES public.tb_evento_crise(uuid);
ALTER TABLE ONLY public.tb_etapa_crise
    ADD CONSTRAINT fk_etapa_crise_profissional FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);
ALTER TABLE ONLY public.tb_comunicacao_ministerio_publico
    ADD CONSTRAINT fk_comunicacao_mp_internacao FOREIGN KEY (internacao_id) REFERENCES public.tb_internacao(uuid);
ALTER TABLE ONLY public.tb_comunicacao_ministerio_publico
    ADD CONSTRAINT fk_comunicacao_mp_responsavel FOREIGN KEY (responsavel_id) REFERENCES public.tb_profissional(uuid);
