-- Vigilância epidemiológica, fatia V1 (ADR-0124): catálogo de agravos com ficha versionada (JSON), notificação,
-- investigação, classificação com histórico e notificação negativa semanal. Conferido contra o ddl-auto=validate
-- (ADR-0090). As permissões VIGILANCIA.* e o papel de vigilância epidemiológica nascem pelo catálogo de acesso.

CREATE TABLE public.tb_agravo (
    ativo boolean NOT NULL,
    compulsorio boolean NOT NULL,
    prazo_encerramento_dias integer NOT NULL,
    sensivel boolean NOT NULL,
    uuid uuid NOT NULL,
    codigo character varying(60) NOT NULL,
    nome character varying(200) NOT NULL,
    descricao character varying(1000),
    periodicidade character varying(255) NOT NULL,
    CONSTRAINT tb_agravo_periodicidade_check CHECK (((periodicidade)::text = ANY ((ARRAY['IMEDIATA'::character varying, 'SEMANAL'::character varying])::text[])))
);

CREATE TABLE public.tb_agravo_cid (
    agravo_id uuid NOT NULL,
    cid character varying(10) NOT NULL
);

CREATE TABLE public.tb_agravo_criterio (
    agravo_id uuid NOT NULL,
    criterio character varying(255) NOT NULL,
    CONSTRAINT tb_agravo_criterio_criterio_check CHECK (((criterio)::text = ANY ((ARRAY['LABORATORIAL'::character varying, 'CLINICO_EPIDEMIOLOGICO'::character varying, 'CLINICO'::character varying])::text[])))
);

CREATE TABLE public.tb_ficha_agravo (
    versao integer NOT NULL,
    criada_em timestamp(6) with time zone NOT NULL,
    agravo_id uuid NOT NULL,
    uuid uuid NOT NULL,
    definicao text NOT NULL,
    registrado_por_cpf character varying(255)
);

CREATE TABLE public.tb_notificacao (
    data_notificacao date NOT NULL,
    data_primeiros_sintomas date,
    ficha_versao integer,
    prazo_encerramento date NOT NULL,
    encerrada_em timestamp(6) with time zone,
    registrada_em timestamp(6) with time zone NOT NULL,
    agravo_id uuid NOT NULL,
    atendimento_id uuid,
    crise_id uuid,
    internacao_id uuid,
    paciente_id uuid NOT NULL,
    pedido_exame_id uuid,
    profissional_id uuid,
    unidade_id uuid NOT NULL,
    uuid uuid NOT NULL,
    local_provavel character varying(500),
    classificacao_final character varying(255),
    evolucao character varying(255),
    origem character varying(255) NOT NULL,
    registrado_por_cpf character varying(255),
    situacao character varying(255) NOT NULL,
    respostas text,
    CONSTRAINT tb_notificacao_classificacao_final_check CHECK (((classificacao_final)::text = ANY ((ARRAY['SUSPEITO'::character varying, 'PROVAVEL'::character varying, 'CONFIRMADO'::character varying, 'DESCARTADO'::character varying, 'INCONCLUSIVO'::character varying])::text[]))),
    CONSTRAINT tb_notificacao_evolucao_check CHECK (((evolucao)::text = ANY ((ARRAY['CURA'::character varying, 'OBITO_PELO_AGRAVO'::character varying, 'OBITO_POR_OUTRA_CAUSA'::character varying, 'IGNORADA'::character varying])::text[]))),
    CONSTRAINT tb_notificacao_origem_check CHECK (((origem)::text = ANY ((ARRAY['ATENDIMENTO'::character varying, 'INTERNACAO'::character varying, 'EXAME'::character varying, 'SAUDE_MENTAL'::character varying, 'BUSCA_ATIVA'::character varying, 'COMUNIDADE'::character varying, 'OUTRA'::character varying])::text[]))),
    CONSTRAINT tb_notificacao_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['NOTIFICADA'::character varying, 'EM_INVESTIGACAO'::character varying, 'ENCERRADA'::character varying])::text[])))
);

CREATE TABLE public.tb_investigacao_epidemiologica (
    iniciada_em timestamp(6) with time zone NOT NULL,
    investigador_id uuid,
    notificacao_id uuid NOT NULL,
    uuid uuid NOT NULL,
    local_provavel_infeccao character varying(500),
    exposicao character varying(2000),
    medidas character varying(2000),
    registrado_por_cpf character varying(255)
);

CREATE TABLE public.tb_investigacao_exame (
    investigacao_id uuid NOT NULL,
    pedido_exame_id uuid NOT NULL
);

CREATE TABLE public.tb_classificacao_caso (
    classificada_em timestamp(6) with time zone NOT NULL,
    notificacao_id uuid NOT NULL,
    responsavel_id uuid,
    uuid uuid NOT NULL,
    justificativa character varying(1000),
    classificacao character varying(255) NOT NULL,
    criterio character varying(255),
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_classificacao_caso_classificacao_check CHECK (((classificacao)::text = ANY ((ARRAY['SUSPEITO'::character varying, 'PROVAVEL'::character varying, 'CONFIRMADO'::character varying, 'DESCARTADO'::character varying, 'INCONCLUSIVO'::character varying])::text[]))),
    CONSTRAINT tb_classificacao_caso_criterio_check CHECK (((criterio)::text = ANY ((ARRAY['LABORATORIAL'::character varying, 'CLINICO_EPIDEMIOLOGICO'::character varying, 'CLINICO'::character varying])::text[])))
);

CREATE TABLE public.tb_notificacao_negativa (
    ano integer NOT NULL,
    semana integer NOT NULL,
    declarada_em timestamp(6) with time zone NOT NULL,
    responsavel_id uuid,
    unidade_id uuid NOT NULL,
    uuid uuid NOT NULL,
    registrado_por_cpf character varying(255)
);

ALTER TABLE ONLY public.tb_agravo ADD CONSTRAINT tb_agravo_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_agravo ADD CONSTRAINT uk_agravo_codigo UNIQUE (codigo);
ALTER TABLE ONLY public.tb_ficha_agravo ADD CONSTRAINT tb_ficha_agravo_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_ficha_agravo ADD CONSTRAINT uk_ficha_agravo_versao UNIQUE (agravo_id, versao);
ALTER TABLE ONLY public.tb_notificacao ADD CONSTRAINT tb_notificacao_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_investigacao_epidemiologica ADD CONSTRAINT tb_investigacao_epidemiologica_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_investigacao_epidemiologica ADD CONSTRAINT uk_investigacao_notificacao UNIQUE (notificacao_id);
ALTER TABLE ONLY public.tb_classificacao_caso ADD CONSTRAINT tb_classificacao_caso_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_notificacao_negativa ADD CONSTRAINT tb_notificacao_negativa_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_notificacao_negativa ADD CONSTRAINT uk_notificacao_negativa UNIQUE (unidade_id, ano, semana);

CREATE INDEX ix_notificacao_paciente ON public.tb_notificacao USING btree (paciente_id, agravo_id);
CREATE INDEX ix_notificacao_unidade ON public.tb_notificacao USING btree (unidade_id, data_notificacao);
CREATE INDEX ix_classificacao_caso ON public.tb_classificacao_caso USING btree (notificacao_id, classificada_em);

ALTER TABLE ONLY public.tb_agravo_cid
    ADD CONSTRAINT fk_agravo_cid FOREIGN KEY (agravo_id) REFERENCES public.tb_agravo(uuid);
ALTER TABLE ONLY public.tb_agravo_criterio
    ADD CONSTRAINT fk_agravo_criterio FOREIGN KEY (agravo_id) REFERENCES public.tb_agravo(uuid);
ALTER TABLE ONLY public.tb_ficha_agravo
    ADD CONSTRAINT fk_ficha_agravo FOREIGN KEY (agravo_id) REFERENCES public.tb_agravo(uuid);
ALTER TABLE ONLY public.tb_notificacao
    ADD CONSTRAINT fk_notificacao_paciente FOREIGN KEY (paciente_id) REFERENCES public.tb_paciente(uuid);
ALTER TABLE ONLY public.tb_notificacao
    ADD CONSTRAINT fk_notificacao_agravo FOREIGN KEY (agravo_id) REFERENCES public.tb_agravo(uuid);
ALTER TABLE ONLY public.tb_notificacao
    ADD CONSTRAINT fk_notificacao_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);
ALTER TABLE ONLY public.tb_notificacao
    ADD CONSTRAINT fk_notificacao_profissional FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);
ALTER TABLE ONLY public.tb_notificacao
    ADD CONSTRAINT fk_notificacao_atendimento FOREIGN KEY (atendimento_id) REFERENCES public.tb_atendimento(uuid);
ALTER TABLE ONLY public.tb_notificacao
    ADD CONSTRAINT fk_notificacao_internacao FOREIGN KEY (internacao_id) REFERENCES public.tb_internacao(uuid);
ALTER TABLE ONLY public.tb_notificacao
    ADD CONSTRAINT fk_notificacao_pedido_exame FOREIGN KEY (pedido_exame_id) REFERENCES public.tb_pedido_exame(uuid);
ALTER TABLE ONLY public.tb_notificacao
    ADD CONSTRAINT fk_notificacao_crise FOREIGN KEY (crise_id) REFERENCES public.tb_evento_crise(uuid);
ALTER TABLE ONLY public.tb_investigacao_epidemiologica
    ADD CONSTRAINT fk_investigacao_notificacao FOREIGN KEY (notificacao_id) REFERENCES public.tb_notificacao(uuid);
ALTER TABLE ONLY public.tb_investigacao_epidemiologica
    ADD CONSTRAINT fk_investigacao_investigador FOREIGN KEY (investigador_id) REFERENCES public.tb_profissional(uuid);
ALTER TABLE ONLY public.tb_investigacao_exame
    ADD CONSTRAINT fk_investigacao_exame FOREIGN KEY (investigacao_id) REFERENCES public.tb_investigacao_epidemiologica(uuid);
ALTER TABLE ONLY public.tb_classificacao_caso
    ADD CONSTRAINT fk_classificacao_notificacao FOREIGN KEY (notificacao_id) REFERENCES public.tb_notificacao(uuid);
ALTER TABLE ONLY public.tb_classificacao_caso
    ADD CONSTRAINT fk_classificacao_responsavel FOREIGN KEY (responsavel_id) REFERENCES public.tb_profissional(uuid);
ALTER TABLE ONLY public.tb_notificacao_negativa
    ADD CONSTRAINT fk_notificacao_negativa_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);
ALTER TABLE ONLY public.tb_notificacao_negativa
    ADD CONSTRAINT fk_notificacao_negativa_responsavel FOREIGN KEY (responsavel_id) REFERENCES public.tb_profissional(uuid);

-- Carga inicial: agravos da Lista Nacional de Notificação Compulsória (Portaria de Consolidação GM/MS nº 4/2017,
-- anexo V). O município acrescenta os de interesse local.
INSERT INTO public.tb_agravo (uuid, codigo, nome, compulsorio, periodicidade, prazo_encerramento_dias, sensivel, ativo) VALUES
    (gen_random_uuid(), 'DENGUE', 'Dengue', true, 'SEMANAL', 60, false, true),
    (gen_random_uuid(), 'CHIKUNGUNYA', 'Febre de chikungunya', true, 'SEMANAL', 60, false, true),
    (gen_random_uuid(), 'ZIKA', 'Doença aguda pelo vírus zika', true, 'SEMANAL', 60, false, true),
    (gen_random_uuid(), 'FEBRE_AMARELA', 'Febre amarela', true, 'IMEDIATA', 60, false, true),
    (gen_random_uuid(), 'MALARIA', 'Malária', true, 'IMEDIATA', 60, false, true),
    (gen_random_uuid(), 'TUBERCULOSE', 'Tuberculose', true, 'SEMANAL', 270, false, true),
    (gen_random_uuid(), 'HANSENIASE', 'Hanseníase', true, 'SEMANAL', 365, false, true),
    (gen_random_uuid(), 'SIFILIS_ADQUIRIDA', 'Sífilis adquirida', true, 'SEMANAL', 60, true, true),
    (gen_random_uuid(), 'SIFILIS_GESTANTE', 'Sífilis em gestante', true, 'SEMANAL', 60, true, true),
    (gen_random_uuid(), 'SIFILIS_CONGENITA', 'Sífilis congênita', true, 'SEMANAL', 60, true, true),
    (gen_random_uuid(), 'HIV_AIDS', 'HIV/aids', true, 'SEMANAL', 180, true, true),
    (gen_random_uuid(), 'HEPATITES_VIRAIS', 'Hepatites virais', true, 'SEMANAL', 180, false, true),
    (gen_random_uuid(), 'LEPTOSPIROSE', 'Leptospirose', true, 'IMEDIATA', 60, false, true),
    (gen_random_uuid(), 'MENINGITE', 'Meningite', true, 'IMEDIATA', 60, false, true),
    (gen_random_uuid(), 'SARAMPO', 'Sarampo', true, 'IMEDIATA', 30, false, true),
    (gen_random_uuid(), 'COQUELUCHE', 'Coqueluche', true, 'IMEDIATA', 60, false, true),
    (gen_random_uuid(), 'SRAG_HOSPITALIZADO', 'Síndrome respiratória aguda grave hospitalizada', true, 'IMEDIATA', 60, false, true),
    (gen_random_uuid(), 'ACIDENTE_ANIMAL_PECONHENTO', 'Acidente por animal peçonhento', true, 'SEMANAL', 60, false, true),
    (gen_random_uuid(), 'ATENDIMENTO_ANTIRRABICO', 'Atendimento antirrábico humano', true, 'SEMANAL', 60, false, true),
    (gen_random_uuid(), 'INTOXICACAO_EXOGENA', 'Intoxicação exógena', true, 'SEMANAL', 60, false, true),
    (gen_random_uuid(), 'ACIDENTE_TRABALHO_GRAVE', 'Acidente de trabalho grave, fatal ou com crianças e adolescentes', true, 'SEMANAL', 60, false, true),
    (gen_random_uuid(), 'VIOLENCIA_INTERPESSOAL_AUTOPROVOCADA', 'Violência interpessoal e autoprovocada', true, 'SEMANAL', 60, true, true);
INSERT INTO public.tb_agravo_cid (agravo_id, cid)
SELECT a.uuid, v.cid FROM (VALUES ('DENGUE', 'A90'), ('DENGUE', 'A91'), ('DENGUE', 'A97'), ('CHIKUNGUNYA', 'A920'), ('ZIKA', 'A928'), ('FEBRE_AMARELA', 'A95'), ('MALARIA', 'B50'), ('MALARIA', 'B51'), ('MALARIA', 'B52'), ('MALARIA', 'B53'), ('MALARIA', 'B54'), ('TUBERCULOSE', 'A15'), ('TUBERCULOSE', 'A16'), ('TUBERCULOSE', 'A17'), ('TUBERCULOSE', 'A18'), ('TUBERCULOSE', 'A19'), ('HANSENIASE', 'A30'), ('SIFILIS_ADQUIRIDA', 'A51'), ('SIFILIS_ADQUIRIDA', 'A52'), ('SIFILIS_ADQUIRIDA', 'A53'), ('SIFILIS_GESTANTE', 'O981'), ('SIFILIS_CONGENITA', 'A50'), ('HIV_AIDS', 'B20'), ('HIV_AIDS', 'B21'), ('HIV_AIDS', 'B22'), ('HIV_AIDS', 'B23'), ('HIV_AIDS', 'B24'), ('HIV_AIDS', 'Z21'), ('HEPATITES_VIRAIS', 'B15'), ('HEPATITES_VIRAIS', 'B16'), ('HEPATITES_VIRAIS', 'B17'), ('HEPATITES_VIRAIS', 'B18'), ('HEPATITES_VIRAIS', 'B19'), ('LEPTOSPIROSE', 'A27'), ('MENINGITE', 'A39'), ('MENINGITE', 'A87'), ('MENINGITE', 'G00'), ('MENINGITE', 'G01'), ('MENINGITE', 'G02'), ('MENINGITE', 'G03'), ('SARAMPO', 'B05'), ('COQUELUCHE', 'A37'), ('SRAG_HOSPITALIZADO', 'J09'), ('SRAG_HOSPITALIZADO', 'J10'), ('SRAG_HOSPITALIZADO', 'J11'), ('SRAG_HOSPITALIZADO', 'U07'), ('ACIDENTE_ANIMAL_PECONHENTO', 'X20'), ('ACIDENTE_ANIMAL_PECONHENTO', 'X21'), ('ACIDENTE_ANIMAL_PECONHENTO', 'X22'), ('ACIDENTE_ANIMAL_PECONHENTO', 'X23'), ('ACIDENTE_ANIMAL_PECONHENTO', 'X25'), ('ACIDENTE_ANIMAL_PECONHENTO', 'X29'), ('ATENDIMENTO_ANTIRRABICO', 'W54'), ('ATENDIMENTO_ANTIRRABICO', 'Z203'), ('INTOXICACAO_EXOGENA', 'T36'), ('INTOXICACAO_EXOGENA', 'T37'), ('INTOXICACAO_EXOGENA', 'T38'), ('INTOXICACAO_EXOGENA', 'T39'), ('INTOXICACAO_EXOGENA', 'T40'), ('INTOXICACAO_EXOGENA', 'T41'), ('INTOXICACAO_EXOGENA', 'T42'), ('INTOXICACAO_EXOGENA', 'T43'), ('INTOXICACAO_EXOGENA', 'T44'), ('INTOXICACAO_EXOGENA', 'T45'), ('INTOXICACAO_EXOGENA', 'T46'), ('INTOXICACAO_EXOGENA', 'T47'), ('INTOXICACAO_EXOGENA', 'T48'), ('INTOXICACAO_EXOGENA', 'T49'), ('INTOXICACAO_EXOGENA', 'T50'), ('INTOXICACAO_EXOGENA', 'T51'), ('INTOXICACAO_EXOGENA', 'T52'), ('INTOXICACAO_EXOGENA', 'T53'), ('INTOXICACAO_EXOGENA', 'T54'), ('INTOXICACAO_EXOGENA', 'T55'), ('INTOXICACAO_EXOGENA', 'T56'), ('INTOXICACAO_EXOGENA', 'T57'), ('INTOXICACAO_EXOGENA', 'T58'), ('INTOXICACAO_EXOGENA', 'T59'), ('INTOXICACAO_EXOGENA', 'T60'), ('INTOXICACAO_EXOGENA', 'T61'), ('INTOXICACAO_EXOGENA', 'T62'), ('INTOXICACAO_EXOGENA', 'T63'), ('INTOXICACAO_EXOGENA', 'T64'), ('INTOXICACAO_EXOGENA', 'T65'), ('VIOLENCIA_INTERPESSOAL_AUTOPROVOCADA', 'X60'), ('VIOLENCIA_INTERPESSOAL_AUTOPROVOCADA', 'X61'), ('VIOLENCIA_INTERPESSOAL_AUTOPROVOCADA', 'X62'), ('VIOLENCIA_INTERPESSOAL_AUTOPROVOCADA', 'X63'), ('VIOLENCIA_INTERPESSOAL_AUTOPROVOCADA', 'X64'), ('VIOLENCIA_INTERPESSOAL_AUTOPROVOCADA', 'X65'), ('VIOLENCIA_INTERPESSOAL_AUTOPROVOCADA', 'X66'), ('VIOLENCIA_INTERPESSOAL_AUTOPROVOCADA', 'X67'), ('VIOLENCIA_INTERPESSOAL_AUTOPROVOCADA', 'X68'), ('VIOLENCIA_INTERPESSOAL_AUTOPROVOCADA', 'X69'), ('VIOLENCIA_INTERPESSOAL_AUTOPROVOCADA', 'X70'), ('VIOLENCIA_INTERPESSOAL_AUTOPROVOCADA', 'X71'), ('VIOLENCIA_INTERPESSOAL_AUTOPROVOCADA', 'X72'), ('VIOLENCIA_INTERPESSOAL_AUTOPROVOCADA', 'X73'), ('VIOLENCIA_INTERPESSOAL_AUTOPROVOCADA', 'X74'), ('VIOLENCIA_INTERPESSOAL_AUTOPROVOCADA', 'X75'), ('VIOLENCIA_INTERPESSOAL_AUTOPROVOCADA', 'X76'), ('VIOLENCIA_INTERPESSOAL_AUTOPROVOCADA', 'X77'), ('VIOLENCIA_INTERPESSOAL_AUTOPROVOCADA', 'X78'), ('VIOLENCIA_INTERPESSOAL_AUTOPROVOCADA', 'X79'), ('VIOLENCIA_INTERPESSOAL_AUTOPROVOCADA', 'X80'), ('VIOLENCIA_INTERPESSOAL_AUTOPROVOCADA', 'X81'), ('VIOLENCIA_INTERPESSOAL_AUTOPROVOCADA', 'X82'), ('VIOLENCIA_INTERPESSOAL_AUTOPROVOCADA', 'X83'), ('VIOLENCIA_INTERPESSOAL_AUTOPROVOCADA', 'X84'), ('VIOLENCIA_INTERPESSOAL_AUTOPROVOCADA', 'Y05'), ('VIOLENCIA_INTERPESSOAL_AUTOPROVOCADA', 'Y06'), ('VIOLENCIA_INTERPESSOAL_AUTOPROVOCADA', 'Y07'), ('VIOLENCIA_INTERPESSOAL_AUTOPROVOCADA', 'Y08'), ('VIOLENCIA_INTERPESSOAL_AUTOPROVOCADA', 'Y09')) AS v(codigo, cid) JOIN public.tb_agravo a ON a.codigo = v.codigo;
INSERT INTO public.tb_agravo_criterio (agravo_id, criterio)
SELECT a.uuid, v.criterio FROM (VALUES ('DENGUE', 'LABORATORIAL'), ('DENGUE', 'CLINICO_EPIDEMIOLOGICO'), ('CHIKUNGUNYA', 'LABORATORIAL'), ('CHIKUNGUNYA', 'CLINICO_EPIDEMIOLOGICO'), ('ZIKA', 'LABORATORIAL'), ('ZIKA', 'CLINICO_EPIDEMIOLOGICO'), ('FEBRE_AMARELA', 'LABORATORIAL'), ('FEBRE_AMARELA', 'CLINICO_EPIDEMIOLOGICO'), ('MALARIA', 'LABORATORIAL'), ('TUBERCULOSE', 'LABORATORIAL'), ('TUBERCULOSE', 'CLINICO_EPIDEMIOLOGICO'), ('TUBERCULOSE', 'CLINICO'), ('HANSENIASE', 'LABORATORIAL'), ('HANSENIASE', 'CLINICO'), ('SIFILIS_ADQUIRIDA', 'LABORATORIAL'), ('SIFILIS_GESTANTE', 'LABORATORIAL'), ('SIFILIS_CONGENITA', 'LABORATORIAL'), ('SIFILIS_CONGENITA', 'CLINICO_EPIDEMIOLOGICO'), ('HIV_AIDS', 'LABORATORIAL'), ('HEPATITES_VIRAIS', 'LABORATORIAL'), ('HEPATITES_VIRAIS', 'CLINICO_EPIDEMIOLOGICO'), ('LEPTOSPIROSE', 'LABORATORIAL'), ('LEPTOSPIROSE', 'CLINICO_EPIDEMIOLOGICO'), ('MENINGITE', 'LABORATORIAL'), ('MENINGITE', 'CLINICO_EPIDEMIOLOGICO'), ('MENINGITE', 'CLINICO'), ('SARAMPO', 'LABORATORIAL'), ('SARAMPO', 'CLINICO_EPIDEMIOLOGICO'), ('COQUELUCHE', 'LABORATORIAL'), ('COQUELUCHE', 'CLINICO_EPIDEMIOLOGICO'), ('COQUELUCHE', 'CLINICO'), ('SRAG_HOSPITALIZADO', 'LABORATORIAL'), ('SRAG_HOSPITALIZADO', 'CLINICO_EPIDEMIOLOGICO'), ('SRAG_HOSPITALIZADO', 'CLINICO'), ('ACIDENTE_ANIMAL_PECONHENTO', 'CLINICO'), ('ATENDIMENTO_ANTIRRABICO', 'CLINICO'), ('INTOXICACAO_EXOGENA', 'LABORATORIAL'), ('INTOXICACAO_EXOGENA', 'CLINICO'), ('ACIDENTE_TRABALHO_GRAVE', 'CLINICO'), ('VIOLENCIA_INTERPESSOAL_AUTOPROVOCADA', 'CLINICO')) AS v(codigo, criterio) JOIN public.tb_agravo a ON a.codigo = v.codigo;

-- Fichas específicas iniciais (versão 1).
INSERT INTO public.tb_ficha_agravo (uuid, agravo_id, versao, definicao, criada_em)
SELECT gen_random_uuid(), a.uuid, 1, '{"campos": [{"chave": "febre", "rotulo": "Febre", "tipo": "SIM_NAO", "obrigatorio": true, "opcoes": null}, {"chave": "sinaisAlarme", "rotulo": "Sinais de alarme", "tipo": "MULTIPLA", "obrigatorio": false, "opcoes": ["dor abdominal", "vômitos persistentes", "sangramento de mucosa", "letargia", "hipotensão postural"]}, {"chave": "hospitalizado", "rotulo": "Hospitalizado", "tipo": "SIM_NAO", "obrigatorio": false, "opcoes": null}]}', now() FROM public.tb_agravo a WHERE a.codigo = 'DENGUE';
INSERT INTO public.tb_ficha_agravo (uuid, agravo_id, versao, definicao, criada_em)
SELECT gen_random_uuid(), a.uuid, 1, '{"campos": [{"chave": "tipoViolencia", "rotulo": "Tipo de violência", "tipo": "OPCAO", "obrigatorio": true, "opcoes": ["Física", "Psicológica ou moral", "Sexual", "Autoprovocada", "Negligência ou abandono", "Outra"]}, {"chave": "lesaoAutoprovocada", "rotulo": "Lesão autoprovocada", "tipo": "SIM_NAO", "obrigatorio": true, "opcoes": null}, {"chave": "localOcorrencia", "rotulo": "Local de ocorrência", "tipo": "OPCAO", "obrigatorio": false, "opcoes": ["Residência", "Via pública", "Escola", "Local de trabalho", "Outro"]}, {"chave": "violenciaRepetida", "rotulo": "Ocorreu outras vezes", "tipo": "SIM_NAO", "obrigatorio": false, "opcoes": null}]}', now() FROM public.tb_agravo a WHERE a.codigo = 'VIOLENCIA_INTERPESSOAL_AUTOPROVOCADA';
