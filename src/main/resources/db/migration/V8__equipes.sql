-- Equipes de saúde (ADR-0103): equipe da unidade, membros com início e fim, e as equipes apoiadas pela eMulti.
-- Gerado com ddl-auto=create num banco descartável e conferido (ADR-0090).

CREATE TABLE public.tb_equipe (
    ativa boolean NOT NULL,
    reuniao_fim time(0) without time zone,
    reuniao_inicio time(0) without time zone,
    ine character varying(10),
    coordenador_id uuid,
    unidade_id uuid NOT NULL,
    uuid uuid NOT NULL,
    nome character varying(80) NOT NULL,
    reuniao_local character varying(100),
    microareas character varying(300),
    reuniao_dia character varying(255),
    tipo character varying(255) NOT NULL,
    CONSTRAINT tb_equipe_reuniao_dia_check CHECK (((reuniao_dia)::text = ANY ((ARRAY['MONDAY'::character varying, 'TUESDAY'::character varying, 'WEDNESDAY'::character varying, 'THURSDAY'::character varying, 'FRIDAY'::character varying, 'SATURDAY'::character varying, 'SUNDAY'::character varying])::text[]))),
    CONSTRAINT tb_equipe_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['ESF'::character varying, 'EAB'::character varying, 'ESB'::character varying, 'EMULTI'::character varying, 'ECR'::character varying, 'CAPS_MULTI'::character varying])::text[])))
);

CREATE TABLE public.tb_equipe_apoio (
    apoiada_id uuid NOT NULL,
    equipe_id uuid NOT NULL
);

CREATE TABLE public.tb_membro_equipe (
    fim date,
    inicio date NOT NULL,
    equipe_id uuid NOT NULL,
    profissional_id uuid NOT NULL,
    uuid uuid NOT NULL,
    microarea character varying(20),
    motivo_saida character varying(500),
    funcao character varying(255) NOT NULL,
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_membro_equipe_funcao_check CHECK (((funcao)::text = ANY ((ARRAY['MEDICO'::character varying, 'ENFERMEIRO'::character varying, 'TECNICO_ENFERMAGEM'::character varying, 'AUXILIAR_ENFERMAGEM'::character varying, 'ACS'::character varying, 'CIRURGIAO_DENTISTA'::character varying, 'TECNICO_SAUDE_BUCAL'::character varying, 'AUXILIAR_SAUDE_BUCAL'::character varying, 'PSICOLOGO'::character varying, 'ASSISTENTE_SOCIAL'::character varying, 'OUTRO'::character varying])::text[])))
);

ALTER TABLE ONLY public.tb_equipe_apoio
    ADD CONSTRAINT tb_equipe_apoio_pkey PRIMARY KEY (apoiada_id, equipe_id);

ALTER TABLE ONLY public.tb_equipe
    ADD CONSTRAINT tb_equipe_ine_key UNIQUE (ine);

ALTER TABLE ONLY public.tb_equipe
    ADD CONSTRAINT tb_equipe_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_membro_equipe
    ADD CONSTRAINT tb_membro_equipe_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_equipe
    ADD CONSTRAINT uk_equipe_nome_por_unidade UNIQUE (unidade_id, nome);

CREATE INDEX ix_membro_equipe_equipe ON public.tb_membro_equipe USING btree (equipe_id, fim);

CREATE INDEX ix_membro_equipe_profissional ON public.tb_membro_equipe USING btree (profissional_id, fim);

ALTER TABLE ONLY public.tb_equipe_apoio
    ADD CONSTRAINT fka9ds184tvtdn9c6e8fpsx1y8q FOREIGN KEY (equipe_id) REFERENCES public.tb_equipe(uuid);

ALTER TABLE ONLY public.tb_equipe
    ADD CONSTRAINT fkb1h387qjj4xadbbg7t05o8qyo FOREIGN KEY (coordenador_id) REFERENCES public.tb_profissional(uuid);

ALTER TABLE ONLY public.tb_membro_equipe
    ADD CONSTRAINT fkbvhpi7iwnn6jre9yp0v3a7o7y FOREIGN KEY (equipe_id) REFERENCES public.tb_equipe(uuid);

ALTER TABLE ONLY public.tb_equipe
    ADD CONSTRAINT fkmu7j3lukpfafmd5k1ljnv2sop FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);

ALTER TABLE ONLY public.tb_equipe_apoio
    ADD CONSTRAINT fknytwed84nh5hysqjuiv5qxwl0 FOREIGN KEY (apoiada_id) REFERENCES public.tb_equipe(uuid);

ALTER TABLE ONLY public.tb_membro_equipe
    ADD CONSTRAINT fkskcswbokc30ofy1tal6vm23yl FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);

-- Uma participação vigente por profissional e equipe.
CREATE UNIQUE INDEX uk_membro_equipe_vigente ON public.tb_membro_equipe USING btree (equipe_id, profissional_id) WHERE (fim IS NULL);
