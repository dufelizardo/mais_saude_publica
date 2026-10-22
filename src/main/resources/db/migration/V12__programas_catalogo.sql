-- Programas de saúde, fatia P1 (ADR-0118): categorias, programas com o público-alvo e as unidades e equipes que os
-- oferecem, com início e fim. Tabelas conferidas contra o ddl-auto=validate (ADR-0090). A carga inicial roda uma vez só;
-- depois, tudo é editado pela tela. Participações não entram na carga: não dá para adivinhar quem oferece o quê.

CREATE TABLE public.tb_categoria_programa (
    ativa boolean NOT NULL,
    ordem integer NOT NULL,
    uuid uuid NOT NULL,
    nome character varying(80) NOT NULL
);

CREATE TABLE public.tb_programa (
    fim date,
    gestante boolean NOT NULL,
    idade_maxima integer,
    idade_minima integer,
    inicio date,
    sensivel boolean NOT NULL,
    categoria_id uuid NOT NULL,
    coordenador_id uuid,
    uuid uuid NOT NULL,
    codigo character varying(40) NOT NULL,
    nome character varying(120) NOT NULL,
    abrangencia character varying(255) NOT NULL,
    periodicidade character varying(255) NOT NULL,
    publico_sexo character varying(255),
    situacao character varying(255) NOT NULL,
    descricao text,
    CONSTRAINT tb_programa_abrangencia_check CHECK (((abrangencia)::text = ANY ((ARRAY['NACIONAL'::character varying, 'ESTADUAL'::character varying, 'MUNICIPAL'::character varying])::text[]))),
    CONSTRAINT tb_programa_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['ATIVO'::character varying, 'SUSPENSO'::character varying, 'ENCERRADO'::character varying])::text[]))),
    CONSTRAINT tb_programa_periodicidade_check CHECK (((periodicidade)::text = ANY ((ARRAY['MENSAL'::character varying, 'BIMESTRAL'::character varying, 'TRIMESTRAL'::character varying, 'QUADRIMESTRAL'::character varying, 'SEMESTRAL'::character varying, 'ANUAL'::character varying, 'SEM_PERIODICIDADE'::character varying])::text[]))),
    CONSTRAINT tb_programa_publico_sexo_check CHECK (((publico_sexo)::text = ANY ((ARRAY['MASCULINO'::character varying, 'FEMININO'::character varying, 'IGNORADO'::character varying])::text[])))
);

CREATE TABLE public.tb_programa_condicao (
    posicao integer NOT NULL,
    programa_id uuid NOT NULL,
    codigo character varying(10) NOT NULL
);

CREATE TABLE public.tb_participacao_programa (
    fim date,
    inicio date NOT NULL,
    equipe_id uuid,
    programa_id uuid NOT NULL,
    unidade_id uuid NOT NULL,
    uuid uuid NOT NULL,
    motivo_fim character varying(500),
    registrado_por_cpf character varying(255)
);

ALTER TABLE ONLY public.tb_categoria_programa ADD CONSTRAINT tb_categoria_programa_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_categoria_programa ADD CONSTRAINT tb_categoria_programa_nome_key UNIQUE (nome);
ALTER TABLE ONLY public.tb_programa ADD CONSTRAINT tb_programa_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_programa ADD CONSTRAINT tb_programa_codigo_key UNIQUE (codigo);
ALTER TABLE ONLY public.tb_programa_condicao ADD CONSTRAINT tb_programa_condicao_pkey PRIMARY KEY (programa_id, posicao);
ALTER TABLE ONLY public.tb_participacao_programa ADD CONSTRAINT tb_participacao_programa_pkey PRIMARY KEY (uuid);

CREATE INDEX ix_participacao_programa ON public.tb_participacao_programa USING btree (programa_id, fim);
CREATE INDEX ix_participacao_programa_unidade ON public.tb_participacao_programa USING btree (unidade_id, fim);

ALTER TABLE ONLY public.tb_programa
    ADD CONSTRAINT fk_programa_categoria FOREIGN KEY (categoria_id) REFERENCES public.tb_categoria_programa(uuid);
ALTER TABLE ONLY public.tb_programa
    ADD CONSTRAINT fk_programa_coordenador FOREIGN KEY (coordenador_id) REFERENCES public.tb_profissional(uuid);
ALTER TABLE ONLY public.tb_programa_condicao
    ADD CONSTRAINT fk_programa_condicao_programa FOREIGN KEY (programa_id) REFERENCES public.tb_programa(uuid);
ALTER TABLE ONLY public.tb_participacao_programa
    ADD CONSTRAINT fk_participacao_programa_programa FOREIGN KEY (programa_id) REFERENCES public.tb_programa(uuid);
ALTER TABLE ONLY public.tb_participacao_programa
    ADD CONSTRAINT fk_participacao_programa_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);
ALTER TABLE ONLY public.tb_participacao_programa
    ADD CONSTRAINT fk_participacao_programa_equipe FOREIGN KEY (equipe_id) REFERENCES public.tb_equipe(uuid);

-- ── Carga inicial: categorias e programas (MODELO-PROGRAMAS.md, seção 10) ─────────────────────────────────────────────

INSERT INTO public.tb_categoria_programa (uuid, nome, ordem, ativa) VALUES
    (gen_random_uuid(), 'Atenção Básica', 1, true),
    (gen_random_uuid(), 'Crônicos', 2, true),
    (gen_random_uuid(), 'Ciclos de vida', 3, true),
    (gen_random_uuid(), 'Saúde Mental', 4, true);

INSERT INTO public.tb_programa (uuid, codigo, nome, descricao, categoria_id, abrangencia, situacao, periodicidade, sensivel,
                                idade_minima, idade_maxima, publico_sexo, gestante)
SELECT gen_random_uuid(), p.codigo, p.nome, p.descricao, c.uuid, 'NACIONAL', 'ATIVO', p.periodicidade, p.sensivel,
       p.idade_minima, p.idade_maxima, p.sexo, p.gestante
FROM (VALUES
    ('SAUDE_DA_FAMILIA', 'Saúde da Família', 'Cuidado longitudinal da família no território, pelas equipes de Saúde da Família.', 'Atenção Básica', 'SEM_PERIODICIDADE', false, NULL::int, NULL::int, NULL, false),
    ('SAUDE_BUCAL', 'Saúde Bucal', 'Promoção, prevenção e tratamento odontológico na atenção básica.', 'Atenção Básica', 'ANUAL', false, NULL, NULL, NULL, false),
    ('IMUNIZACAO', 'Imunização', 'Vacinação de rotina pelo calendário nacional.', 'Atenção Básica', 'SEM_PERIODICIDADE', false, NULL, NULL, NULL, false),
    ('PLANEJAMENTO_REPRODUTIVO', 'Planejamento Reprodutivo', 'Orientação e acesso a métodos contraceptivos e à concepção planejada.', 'Atenção Básica', 'SEMESTRAL', false, 10, 49, NULL, false),
    ('HIPERDIA', 'HiperDia', 'Acompanhamento contínuo de pessoas com hipertensão e diabetes na atenção básica.', 'Crônicos', 'MENSAL', false, NULL, NULL, NULL, false),
    ('TUBERCULOSE', 'Tuberculose', 'Diagnóstico, tratamento supervisionado e acompanhamento dos casos e contatos.', 'Crônicos', 'MENSAL', false, NULL, NULL, NULL, false),
    ('HANSENIASE', 'Hanseníase', 'Diagnóstico, tratamento e prevenção de incapacidades.', 'Crônicos', 'MENSAL', false, NULL, NULL, NULL, false),
    ('TABAGISMO', 'Tabagismo', 'Cessação do tabagismo com abordagem cognitivo-comportamental e apoio medicamentoso.', 'Crônicos', 'MENSAL', false, NULL, NULL, NULL, false),
    ('PRE_NATAL', 'Pré-natal', 'Acompanhamento da gestação, do pré-natal ao puerpério.', 'Ciclos de vida', 'MENSAL', false, NULL, NULL, 'FEMININO', true),
    ('SAUDE_DA_MULHER', 'Saúde da Mulher', 'Atenção integral à saúde da mulher, com rastreamento dos cânceres de colo do útero e de mama.', 'Ciclos de vida', 'ANUAL', false, NULL, NULL, 'FEMININO', false),
    ('SAUDE_DA_CRIANCA', 'Saúde da Criança', 'Puericultura: crescimento, desenvolvimento e vacinação.', 'Ciclos de vida', 'TRIMESTRAL', false, 0, 9, NULL, false),
    ('SAUDE_DO_IDOSO', 'Saúde do Idoso', 'Atenção à pessoa idosa, com avaliação multidimensional e prevenção de quedas.', 'Ciclos de vida', 'SEMESTRAL', false, 60, NULL, NULL, false),
    ('SAUDE_MENTAL', 'Saúde Mental', 'Acolhimento e cuidado em saúde mental na rede de atenção psicossocial.', 'Saúde Mental', 'MENSAL', true, NULL, NULL, NULL, false)
) AS p(codigo, nome, descricao, categoria, periodicidade, sensivel, idade_minima, idade_maxima, sexo, gestante)
JOIN public.tb_categoria_programa c ON c.nome = p.categoria;

-- Condições acompanhadas (CID-10 e CIAP-2), na ordem.
INSERT INTO public.tb_programa_condicao (programa_id, posicao, codigo)
SELECT p.uuid, x.posicao, x.codigo
FROM (VALUES
    ('HIPERDIA', 0, 'I10'), ('HIPERDIA', 1, 'E10'), ('HIPERDIA', 2, 'E11'),
    ('HIPERDIA', 3, 'K86'), ('HIPERDIA', 4, 'K87'), ('HIPERDIA', 5, 'T89'), ('HIPERDIA', 6, 'T90'),
    ('TUBERCULOSE', 0, 'A15'), ('TUBERCULOSE', 1, 'A16'), ('TUBERCULOSE', 2, 'A17'), ('TUBERCULOSE', 3, 'A18'), ('TUBERCULOSE', 4, 'A19'),
    ('HANSENIASE', 0, 'A30'),
    ('TABAGISMO', 0, 'F17'), ('TABAGISMO', 1, 'P17')
) AS x(programa, posicao, codigo)
JOIN public.tb_programa p ON p.codigo = x.programa;
