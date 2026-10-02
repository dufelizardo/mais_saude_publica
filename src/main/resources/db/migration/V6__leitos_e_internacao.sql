-- Leitos e internação (ADR-0098): leito num setor assistencial, internação com troca de leito e alta, e os movimentos.
-- Gerado com ddl-auto=create num banco descartável e conferido (ADR-0090).

CREATE TABLE public.tb_evento_leito (
    ocorrido_em timestamp(6) with time zone NOT NULL,
    internacao_id uuid,
    leito_id uuid NOT NULL,
    profissional_id uuid,
    uuid uuid NOT NULL,
    texto character varying(1000),
    registrado_por_cpf character varying(255),
    tipo character varying(255) NOT NULL,
    CONSTRAINT tb_evento_leito_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['ADMISSAO'::character varying, 'TROCA_DE_LEITO'::character varying, 'ALTA'::character varying, 'HIGIENIZACAO_CONCLUIDA'::character varying, 'BLOQUEIO'::character varying, 'DESBLOQUEIO'::character varying])::text[])))
);

CREATE TABLE public.tb_internacao (
    previsao_alta date,
    admitida_em timestamp(6) with time zone NOT NULL,
    alta_em timestamp(6) with time zone,
    cid character varying(10) NOT NULL,
    alta_por_id uuid,
    atendimento_id uuid,
    leito_id uuid NOT NULL,
    medico_responsavel_id uuid NOT NULL,
    paciente_id uuid NOT NULL,
    unidade_id uuid NOT NULL,
    uuid uuid NOT NULL,
    motivo character varying(1000) NOT NULL,
    sumario_alta character varying(4000),
    carater character varying(255) NOT NULL,
    registrado_por_cpf character varying(255),
    status character varying(255) NOT NULL,
    tipo_alta character varying(255),
    CONSTRAINT tb_internacao_carater_check CHECK (((carater)::text = ANY ((ARRAY['ELETIVA'::character varying, 'URGENCIA'::character varying])::text[]))),
    CONSTRAINT tb_internacao_status_check CHECK (((status)::text = ANY ((ARRAY['INTERNADO'::character varying, 'ALTA'::character varying])::text[]))),
    CONSTRAINT tb_internacao_tipo_alta_check CHECK (((tipo_alta)::text = ANY ((ARRAY['MELHORADO'::character varying, 'A_PEDIDO'::character varying, 'TRANSFERENCIA'::character varying, 'EVASAO'::character varying, 'OBITO'::character varying])::text[])))
);

CREATE TABLE public.tb_leito (
    ativo boolean NOT NULL,
    setor_id uuid NOT NULL,
    unidade_id uuid NOT NULL,
    uuid uuid NOT NULL,
    identificacao character varying(60) NOT NULL,
    motivo_bloqueio character varying(500),
    sexo character varying(255) NOT NULL,
    situacao character varying(255) NOT NULL,
    tipo character varying(255) NOT NULL,
    CONSTRAINT tb_leito_sexo_check CHECK (((sexo)::text = ANY ((ARRAY['MASCULINO'::character varying, 'FEMININO'::character varying, 'MISTO'::character varying])::text[]))),
    CONSTRAINT tb_leito_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['LIVRE'::character varying, 'OCUPADO'::character varying, 'HIGIENIZACAO'::character varying, 'BLOQUEADO'::character varying])::text[]))),
    CONSTRAINT tb_leito_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['CLINICO'::character varying, 'CIRURGICO'::character varying, 'PEDIATRICO'::character varying, 'OBSTETRICO'::character varying, 'UTI_ADULTO'::character varying, 'UTI_PEDIATRICA'::character varying, 'UTI_NEONATAL'::character varying, 'OBSERVACAO'::character varying, 'ISOLAMENTO'::character varying])::text[])))
);

ALTER TABLE ONLY public.tb_evento_leito
    ADD CONSTRAINT tb_evento_leito_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_internacao
    ADD CONSTRAINT tb_internacao_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_leito
    ADD CONSTRAINT tb_leito_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_leito
    ADD CONSTRAINT uk_leito_identificacao_por_unidade UNIQUE (unidade_id, identificacao);

CREATE INDEX ix_evento_leito_internacao ON public.tb_evento_leito USING btree (internacao_id);

CREATE INDEX ix_evento_leito_leito ON public.tb_evento_leito USING btree (leito_id, ocorrido_em);

CREATE INDEX ix_internacao_paciente ON public.tb_internacao USING btree (paciente_id, status);

CREATE INDEX ix_internacao_unidade_status ON public.tb_internacao USING btree (unidade_id, status);

ALTER TABLE ONLY public.tb_leito
    ADD CONSTRAINT fk1urwu6qkamc02o7jdi7wt1l37 FOREIGN KEY (setor_id) REFERENCES public.tb_setor(uuid);

ALTER TABLE ONLY public.tb_internacao
    ADD CONSTRAINT fk3jbt77s9pc0lm6hsa010mtkar FOREIGN KEY (alta_por_id) REFERENCES public.tb_profissional(uuid);

ALTER TABLE ONLY public.tb_internacao
    ADD CONSTRAINT fk584ido90j0cqhn0wkfirlikq6 FOREIGN KEY (medico_responsavel_id) REFERENCES public.tb_profissional(uuid);

ALTER TABLE ONLY public.tb_evento_leito
    ADD CONSTRAINT fk7hlrwrm608d9y6svjhlwva4os FOREIGN KEY (leito_id) REFERENCES public.tb_leito(uuid);

ALTER TABLE ONLY public.tb_internacao
    ADD CONSTRAINT fka2vj0qxsx4o4nl39ab06400ia FOREIGN KEY (paciente_id) REFERENCES public.tb_paciente(uuid);

ALTER TABLE ONLY public.tb_evento_leito
    ADD CONSTRAINT fkcyhe3r15w9uaf5jwnevoy2j5i FOREIGN KEY (internacao_id) REFERENCES public.tb_internacao(uuid);

ALTER TABLE ONLY public.tb_internacao
    ADD CONSTRAINT fkf4krettpu067mddc3p6oi7ibs FOREIGN KEY (atendimento_id) REFERENCES public.tb_atendimento(uuid);

ALTER TABLE ONLY public.tb_internacao
    ADD CONSTRAINT fkglxmw30sp9urobxda4vuvvny7 FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);

ALTER TABLE ONLY public.tb_internacao
    ADD CONSTRAINT fkpexxj1pk5clbwjldm4ke06t9e FOREIGN KEY (leito_id) REFERENCES public.tb_leito(uuid);

ALTER TABLE ONLY public.tb_leito
    ADD CONSTRAINT fkq11lar7ltunf7o5ychjyccfi FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);

ALTER TABLE ONLY public.tb_evento_leito
    ADD CONSTRAINT fkqfe1n6gy3ek8as7ivdicgsw2e FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);

-- Garantia no banco, além da trava no serviço: uma internação ativa por paciente e por leito.
CREATE UNIQUE INDEX uk_internacao_ativa_por_paciente ON public.tb_internacao USING btree (paciente_id) WHERE ((status)::text = 'INTERNADO'::text);

CREATE UNIQUE INDEX uk_internacao_ativa_por_leito ON public.tb_internacao USING btree (leito_id) WHERE ((status)::text = 'INTERNADO'::text);
