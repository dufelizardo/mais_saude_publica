-- Imunização, fatia I1 (ADR-0147): o catálogo de imunobiológicos e o calendário vacinal como dado (com a carga inicial do
-- Calendário Nacional de Vacinação do PNI, editável), e o registro de vacinação (registro clínico com retificação,
-- ADR-0062). Conferido contra o ddl-auto=validate (ADR-0090).

CREATE TABLE public.tb_imunobiologico (
    ativo boolean NOT NULL,
    doses_por_frasco integer NOT NULL,
    validade_apos_aberto_horas integer,
    volume_dose_ml numeric(5,2),
    uuid uuid NOT NULL,
    codigo character varying(40) NOT NULL,
    codigo_sipni character varying(20),
    nome character varying(150) NOT NULL,
    sigla character varying(30) NOT NULL,
    tipo character varying(255) NOT NULL,
    via character varying(255) NOT NULL,
    CONSTRAINT tb_imunobiologico_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['VACINA'::character varying, 'SORO'::character varying, 'IMUNOGLOBULINA'::character varying])::text[]))),
    CONSTRAINT tb_imunobiologico_via_check CHECK (((via)::text = ANY ((ARRAY['INTRAMUSCULAR'::character varying, 'SUBCUTANEA'::character varying, 'INTRADERMICA'::character varying, 'ORAL'::character varying, 'INTRANASAL'::character varying])::text[])))
);

ALTER TABLE ONLY public.tb_imunobiologico ADD CONSTRAINT tb_imunobiologico_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_imunobiologico ADD CONSTRAINT uk_imunobiologico_codigo UNIQUE (codigo);

CREATE TABLE public.tb_esquema_vacinal (
    ativo boolean NOT NULL,
    conta_na_situacao boolean NOT NULL,
    idade_maxima_meses integer,
    idade_minima_meses integer,
    idade_recomendada_meses integer,
    intervalo_minimo_dias integer,
    ordem integer NOT NULL,
    repeticao_anos integer,
    vigente_ate date,
    vigente_desde date NOT NULL,
    imunobiologico_id uuid NOT NULL,
    uuid uuid NOT NULL,
    observacao character varying(300),
    dose character varying(255) NOT NULL,
    publico character varying(255) NOT NULL,
    CONSTRAINT tb_esquema_vacinal_dose_check CHECK (((dose)::text = ANY ((ARRAY['PRIMEIRA'::character varying, 'SEGUNDA'::character varying, 'TERCEIRA'::character varying, 'QUARTA'::character varying, 'REFORCO'::character varying, 'SEGUNDO_REFORCO'::character varying, 'UNICA'::character varying, 'ANUAL'::character varying, 'ADICIONAL'::character varying])::text[]))),
    CONSTRAINT tb_esquema_vacinal_publico_check CHECK (((publico)::text = ANY ((ARRAY['CRIANCA'::character varying, 'ADOLESCENTE'::character varying, 'ADULTO'::character varying, 'IDOSO'::character varying, 'GESTANTE'::character varying])::text[])))
);

ALTER TABLE ONLY public.tb_esquema_vacinal ADD CONSTRAINT tb_esquema_vacinal_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_esquema_vacinal_imunobiologico ON public.tb_esquema_vacinal USING btree (imunobiologico_id);
ALTER TABLE ONLY public.tb_esquema_vacinal ADD CONSTRAINT fk_esquema_vacinal_imunobiologico FOREIGN KEY (imunobiologico_id) REFERENCES public.tb_imunobiologico(uuid);

CREATE TABLE public.tb_registro_vacinacao (
    anulada boolean NOT NULL,
    gestante boolean NOT NULL,
    aplicada_em timestamp(6) without time zone NOT NULL,
    anulada_em timestamp(6) with time zone,
    registrado_em timestamp(6) with time zone NOT NULL,
    atendimento_id uuid,
    campanha_id uuid,
    esquema_id uuid,
    imunobiologico_id uuid NOT NULL,
    paciente_id uuid NOT NULL,
    profissional_id uuid,
    retificacao_de_id uuid,
    unidade_id uuid NOT NULL,
    uuid uuid NOT NULL,
    lote character varying(40),
    fabricante character varying(100),
    local_origem character varying(200),
    justificativa character varying(500),
    motivo_anulacao character varying(500),
    motivo_retificacao character varying(500),
    observacao character varying(500),
    anulada_por_cpf character varying(255),
    dose character varying(255) NOT NULL,
    estrategia character varying(255) NOT NULL,
    local_aplicacao character varying(255),
    origem character varying(255) NOT NULL,
    registrado_por_cpf character varying(255),
    via character varying(255),
    CONSTRAINT tb_registro_vacinacao_dose_check CHECK (((dose)::text = ANY ((ARRAY['PRIMEIRA'::character varying, 'SEGUNDA'::character varying, 'TERCEIRA'::character varying, 'QUARTA'::character varying, 'REFORCO'::character varying, 'SEGUNDO_REFORCO'::character varying, 'UNICA'::character varying, 'ANUAL'::character varying, 'ADICIONAL'::character varying])::text[]))),
    CONSTRAINT tb_registro_vacinacao_estrategia_check CHECK (((estrategia)::text = ANY ((ARRAY['ROTINA'::character varying, 'CAMPANHA'::character varying, 'BLOQUEIO'::character varying, 'INTENSIFICACAO'::character varying, 'ESPECIAL'::character varying])::text[]))),
    CONSTRAINT tb_registro_vacinacao_local_aplicacao_check CHECK (((local_aplicacao)::text = ANY ((ARRAY['DELTOIDE_DIREITO'::character varying, 'DELTOIDE_ESQUERDO'::character varying, 'VASTO_LATERAL_DIREITO'::character varying, 'VASTO_LATERAL_ESQUERDO'::character varying, 'VENTROGLUTEO'::character varying, 'ORAL'::character varying, 'OUTRO'::character varying])::text[]))),
    CONSTRAINT tb_registro_vacinacao_origem_check CHECK (((origem)::text = ANY ((ARRAY['APLICADA'::character varying, 'TRANSCRITA'::character varying])::text[]))),
    CONSTRAINT tb_registro_vacinacao_via_check CHECK (((via)::text = ANY ((ARRAY['INTRAMUSCULAR'::character varying, 'SUBCUTANEA'::character varying, 'INTRADERMICA'::character varying, 'ORAL'::character varying, 'INTRANASAL'::character varying])::text[])))
);

ALTER TABLE ONLY public.tb_registro_vacinacao ADD CONSTRAINT tb_registro_vacinacao_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_registro_vacinacao_paciente ON public.tb_registro_vacinacao USING btree (paciente_id);
CREATE INDEX ix_registro_vacinacao_unidade_data ON public.tb_registro_vacinacao USING btree (unidade_id, aplicada_em);
ALTER TABLE ONLY public.tb_registro_vacinacao ADD CONSTRAINT fk_registro_vacinacao_paciente FOREIGN KEY (paciente_id) REFERENCES public.tb_paciente(uuid);
ALTER TABLE ONLY public.tb_registro_vacinacao ADD CONSTRAINT fk_registro_vacinacao_imunobiologico FOREIGN KEY (imunobiologico_id) REFERENCES public.tb_imunobiologico(uuid);
ALTER TABLE ONLY public.tb_registro_vacinacao ADD CONSTRAINT fk_registro_vacinacao_esquema FOREIGN KEY (esquema_id) REFERENCES public.tb_esquema_vacinal(uuid);
ALTER TABLE ONLY public.tb_registro_vacinacao ADD CONSTRAINT fk_registro_vacinacao_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);
ALTER TABLE ONLY public.tb_registro_vacinacao ADD CONSTRAINT fk_registro_vacinacao_profissional FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);
ALTER TABLE ONLY public.tb_registro_vacinacao ADD CONSTRAINT fk_registro_vacinacao_campanha FOREIGN KEY (campanha_id) REFERENCES public.tb_campanha(uuid);
ALTER TABLE ONLY public.tb_registro_vacinacao ADD CONSTRAINT fk_registro_vacinacao_atendimento FOREIGN KEY (atendimento_id) REFERENCES public.tb_atendimento(uuid);
ALTER TABLE ONLY public.tb_registro_vacinacao ADD CONSTRAINT fk_registro_vacinacao_retificacao FOREIGN KEY (retificacao_de_id) REFERENCES public.tb_registro_vacinacao(uuid);

-- Carga inicial editável: os imunobiológicos do Calendário Nacional de Vacinação (PNI).
INSERT INTO public.tb_imunobiologico (uuid, codigo, nome, sigla, tipo, via, volume_dose_ml, doses_por_frasco, validade_apos_aberto_horas, ativo) VALUES
    (gen_random_uuid(), 'BCG', 'BCG (formas graves de tuberculose)', 'BCG', 'VACINA', 'INTRADERMICA', 0.10, 10, 6, true),
    (gen_random_uuid(), 'HEPATITE_B', 'Hepatite B (recombinante)', 'HepB', 'VACINA', 'INTRAMUSCULAR', 0.50, 10, 360, true),
    (gen_random_uuid(), 'PENTA', 'Pentavalente (DTP + Hib + Hepatite B)', 'Penta', 'VACINA', 'INTRAMUSCULAR', 0.50, 1, NULL, true),
    (gen_random_uuid(), 'VIP', 'Poliomielite inativada (VIP)', 'VIP', 'VACINA', 'INTRAMUSCULAR', 0.50, 10, 672, true),
    (gen_random_uuid(), 'PNEUMO_10', 'Pneumocócica 10-valente (conjugada)', 'Pneumo 10', 'VACINA', 'INTRAMUSCULAR', 0.50, 1, NULL, true),
    (gen_random_uuid(), 'ROTAVIRUS', 'Rotavírus humano (VRH)', 'VRH', 'VACINA', 'ORAL', 1.50, 1, NULL, true),
    (gen_random_uuid(), 'MENINGO_C', 'Meningocócica C (conjugada)', 'MenC', 'VACINA', 'INTRAMUSCULAR', 0.50, 1, NULL, true),
    (gen_random_uuid(), 'FEBRE_AMARELA', 'Febre amarela (atenuada)', 'FA', 'VACINA', 'SUBCUTANEA', 0.50, 5, 6, true),
    (gen_random_uuid(), 'TRIPLICE_VIRAL', 'Tríplice viral (sarampo, caxumba e rubéola)', 'SCR', 'VACINA', 'SUBCUTANEA', 0.50, 10, 8, true),
    (gen_random_uuid(), 'TETRA_VIRAL', 'Tetra viral (sarampo, caxumba, rubéola e varicela)', 'SCRV', 'VACINA', 'SUBCUTANEA', 0.50, 1, NULL, true),
    (gen_random_uuid(), 'HEPATITE_A', 'Hepatite A (pediátrica)', 'HepA', 'VACINA', 'INTRAMUSCULAR', 0.50, 1, NULL, true),
    (gen_random_uuid(), 'DTP', 'Tríplice bacteriana (DTP)', 'DTP', 'VACINA', 'INTRAMUSCULAR', 0.50, 10, 360, true),
    (gen_random_uuid(), 'VARICELA', 'Varicela (atenuada)', 'VZ', 'VACINA', 'SUBCUTANEA', 0.50, 1, NULL, true),
    (gen_random_uuid(), 'HPV4', 'Papilomavírus humano quadrivalente (HPV4)', 'HPV4', 'VACINA', 'INTRAMUSCULAR', 0.50, 1, NULL, true),
    (gen_random_uuid(), 'MENINGO_ACWY', 'Meningocócica ACWY (conjugada)', 'MenACWY', 'VACINA', 'INTRAMUSCULAR', 0.50, 1, NULL, true),
    (gen_random_uuid(), 'DT', 'Dupla adulto (dT)', 'dT', 'VACINA', 'INTRAMUSCULAR', 0.50, 10, 360, true),
    (gen_random_uuid(), 'DTPA', 'Tríplice bacteriana acelular do adulto (dTpa)', 'dTpa', 'VACINA', 'INTRAMUSCULAR', 0.50, 1, NULL, true),
    (gen_random_uuid(), 'INFLUENZA', 'Influenza (trivalente)', 'Influenza', 'VACINA', 'INTRAMUSCULAR', 0.50, 10, 168, true),
    (gen_random_uuid(), 'COVID19', 'Covid-19', 'Covid-19', 'VACINA', 'INTRAMUSCULAR', NULL, 1, NULL, true),
    (gen_random_uuid(), 'PNEUMO_23', 'Pneumocócica 23-valente (polissacarídica)', 'Pneumo 23', 'VACINA', 'INTRAMUSCULAR', 0.50, 1, NULL, true);

-- Carga inicial editável: o calendário. Idades em meses; o intervalo mínimo conta desde a dose anterior do mesmo
-- imunobiológico. "Conta na situação" = entra nas doses atrasadas e próximas; as demais são recomendações conforme a
-- situação vacinal (adulto, idoso, gestante e campanhas anuais).
INSERT INTO public.tb_esquema_vacinal (uuid, imunobiologico_id, dose, publico, idade_minima_meses, idade_recomendada_meses, idade_maxima_meses,
    intervalo_minimo_dias, repeticao_anos, conta_na_situacao, observacao, vigente_desde, ordem, ativo)
SELECT gen_random_uuid(), i.uuid, c.dose, c.publico, c.minimo, c.recomendada, c.maximo, c.intervalo, c.repeticao, c.situacao, c.observacao,
    DATE '2025-01-01', c.ordem, true
FROM (VALUES
    ('BCG', 'UNICA', 'CRIANCA', 0, 0, 59, NULL, NULL, true, 'Ao nascer; até 4 anos, 11 meses e 29 dias.', 10),
    ('HEPATITE_B', 'PRIMEIRA', 'CRIANCA', 0, 0, 1, NULL, NULL, true, 'Ao nascer, de preferência nas primeiras 24 horas; até 30 dias.', 20),
    ('PENTA', 'PRIMEIRA', 'CRIANCA', 1, 2, 83, NULL, NULL, true, 'Aos 2 meses (a partir de 6 semanas).', 30),
    ('PENTA', 'SEGUNDA', 'CRIANCA', 2, 4, 83, 30, NULL, true, 'Aos 4 meses.', 31),
    ('PENTA', 'TERCEIRA', 'CRIANCA', 3, 6, 83, 30, NULL, true, 'Aos 6 meses.', 32),
    ('VIP', 'PRIMEIRA', 'CRIANCA', 1, 2, 59, NULL, NULL, true, 'Aos 2 meses.', 40),
    ('VIP', 'SEGUNDA', 'CRIANCA', 2, 4, 59, 30, NULL, true, 'Aos 4 meses.', 41),
    ('VIP', 'TERCEIRA', 'CRIANCA', 3, 6, 59, 30, NULL, true, 'Aos 6 meses.', 42),
    ('VIP', 'REFORCO', 'CRIANCA', 12, 15, 59, 180, NULL, true, 'Aos 15 meses (substituiu o reforço com a VOP).', 43),
    ('PNEUMO_10', 'PRIMEIRA', 'CRIANCA', 1, 2, 59, NULL, NULL, true, 'Aos 2 meses.', 50),
    ('PNEUMO_10', 'SEGUNDA', 'CRIANCA', 2, 4, 59, 30, NULL, true, 'Aos 4 meses.', 51),
    ('PNEUMO_10', 'REFORCO', 'CRIANCA', 12, 12, 59, 60, NULL, true, 'Aos 12 meses.', 52),
    ('ROTAVIRUS', 'PRIMEIRA', 'CRIANCA', 1, 2, 3, NULL, NULL, true, 'Aos 2 meses; até 3 meses e 15 dias.', 60),
    ('ROTAVIRUS', 'SEGUNDA', 'CRIANCA', 3, 4, 7, 30, NULL, true, 'Aos 4 meses; até 7 meses e 29 dias.', 61),
    ('MENINGO_C', 'PRIMEIRA', 'CRIANCA', 3, 3, 59, NULL, NULL, true, 'Aos 3 meses.', 70),
    ('MENINGO_C', 'SEGUNDA', 'CRIANCA', 4, 5, 59, 30, NULL, true, 'Aos 5 meses.', 71),
    ('MENINGO_C', 'REFORCO', 'CRIANCA', 12, 12, 59, 60, NULL, true, 'Aos 12 meses.', 72),
    ('COVID19', 'PRIMEIRA', 'CRIANCA', 6, 6, 59, NULL, NULL, true, 'Aos 6 meses (esquema conforme a vacina disponível).', 75),
    ('COVID19', 'SEGUNDA', 'CRIANCA', 7, 7, 59, 28, NULL, true, 'Aos 7 meses.', 76),
    ('COVID19', 'TERCEIRA', 'CRIANCA', 9, 9, 59, 56, NULL, true, 'Aos 9 meses.', 77),
    ('FEBRE_AMARELA', 'PRIMEIRA', 'CRIANCA', 9, 9, 59, NULL, NULL, true, 'Aos 9 meses.', 80),
    ('FEBRE_AMARELA', 'REFORCO', 'CRIANCA', 48, 48, 59, 30, NULL, true, 'Aos 4 anos.', 81),
    ('TRIPLICE_VIRAL', 'PRIMEIRA', 'CRIANCA', 12, 12, 59, NULL, NULL, true, 'Aos 12 meses.', 90),
    ('TETRA_VIRAL', 'UNICA', 'CRIANCA', 15, 15, 83, 30, NULL, true, 'Aos 15 meses: segunda dose de tríplice viral e primeira de varicela.', 91),
    ('HEPATITE_A', 'UNICA', 'CRIANCA', 15, 15, 59, NULL, NULL, true, 'Aos 15 meses.', 92),
    ('DTP', 'REFORCO', 'CRIANCA', 15, 15, 83, 180, NULL, true, 'Aos 15 meses.', 93),
    ('DTP', 'SEGUNDO_REFORCO', 'CRIANCA', 48, 48, 83, 180, NULL, true, 'Aos 4 anos.', 94),
    ('VARICELA', 'UNICA', 'CRIANCA', 48, 48, 83, 30, NULL, true, 'Aos 4 anos: segunda dose de varicela.', 95),
    ('INFLUENZA', 'ANUAL', 'CRIANCA', 6, 6, 71, NULL, NULL, false, 'Campanha anual, de 6 meses a menores de 6 anos.', 96),
    ('HPV4', 'UNICA', 'ADOLESCENTE', 108, 108, 179, NULL, NULL, true, 'De 9 a 14 anos, dose única.', 100),
    ('MENINGO_ACWY', 'UNICA', 'ADOLESCENTE', 132, 132, 179, NULL, NULL, true, 'De 11 a 14 anos.', 101),
    ('DT', 'REFORCO', 'ADULTO', 168, 168, NULL, NULL, 10, true, 'Reforço a cada 10 anos (três doses se o esquema estiver incompleto).', 110),
    ('HEPATITE_B', 'PRIMEIRA', 'ADULTO', 240, NULL, NULL, NULL, NULL, false, 'Três doses (0, 1 e 6 meses) para quem não foi vacinado.', 111),
    ('TRIPLICE_VIRAL', 'PRIMEIRA', 'ADULTO', 240, NULL, 719, NULL, NULL, false, 'Duas doses até 29 anos; uma dose de 30 a 59 anos.', 112),
    ('FEBRE_AMARELA', 'UNICA', 'ADULTO', 60, NULL, 719, NULL, NULL, false, 'Dose única para quem nunca foi vacinado, até 59 anos.', 113),
    ('INFLUENZA', 'ANUAL', 'IDOSO', 720, NULL, NULL, NULL, NULL, false, 'Campanha anual, a partir de 60 anos.', 120),
    ('PNEUMO_23', 'UNICA', 'IDOSO', 720, NULL, NULL, NULL, NULL, false, 'Para acamados e institucionalizados.', 121),
    ('DTPA', 'UNICA', 'GESTANTE', 120, NULL, NULL, NULL, NULL, false, 'A cada gestação, a partir da 20ª semana.', 130),
    ('INFLUENZA', 'ANUAL', 'GESTANTE', 120, NULL, NULL, NULL, NULL, false, 'Em qualquer idade gestacional, na campanha anual.', 131)
) AS c(codigo, dose, publico, minimo, recomendada, maximo, intervalo, repeticao, situacao, observacao, ordem)
JOIN public.tb_imunobiologico i ON i.codigo = c.codigo;
