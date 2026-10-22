-- Território e adscrição, fatia F1 (ADR-0125, desenho na ADR-0108): segmento, território com finalidade, área e
-- microárea com GeoJSON em texto (sem PostGIS); equipe responsável pela área e ACS da microárea como histórico;
-- coordenadas da unidade. No fim, os textos livres Equipe.microareas e MembroEquipe.microarea (ADR-0103) viram
-- microáreas e atribuições. Conferido contra o ddl-auto=validate (ADR-0090). A permissão TERRITORIO.GERENCIAR nasce
-- pelo catálogo de acesso.

CREATE TABLE public.tb_segmento_territorial (
    unidade_gestao_id uuid,
    uuid uuid NOT NULL,
    codigo character varying(30) NOT NULL,
    nome character varying(120) NOT NULL,
    tipo character varying(255) NOT NULL,
    geometria text,
    CONSTRAINT tb_segmento_territorial_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['DISTRITO_SANITARIO'::character varying, 'ZONA_IBGE'::character varying, 'OUTRO'::character varying])::text[])))
);

CREATE TABLE public.tb_territorio (
    ativo boolean NOT NULL,
    segmento_id uuid NOT NULL,
    uuid uuid NOT NULL,
    nome character varying(120) NOT NULL,
    finalidade character varying(255) NOT NULL,
    CONSTRAINT tb_territorio_finalidade_check CHECK (((finalidade)::text = ANY ((ARRAY['ATENCAO_PRIMARIA'::character varying, 'REFERENCIA_ASSISTENCIAL'::character varying, 'VIGILANCIA'::character varying])::text[])))
);

CREATE TABLE public.tb_area (
    territorio_id uuid NOT NULL,
    uuid uuid NOT NULL,
    codigo_cnes character varying(10),
    codigo character varying(20) NOT NULL,
    nome character varying(120) NOT NULL,
    geometria text
);

CREATE TABLE public.tb_microarea (
    populacao_referencia integer,
    area_id uuid NOT NULL,
    uuid uuid NOT NULL,
    codigo character varying(20) NOT NULL,
    nome character varying(120),
    geometria text
);

CREATE TABLE public.tb_responsabilidade_territorial (
    fim date,
    inicio date NOT NULL,
    area_id uuid NOT NULL,
    equipe_id uuid NOT NULL,
    uuid uuid NOT NULL,
    motivo_fim character varying(500),
    registrado_por_cpf character varying(255)
);

CREATE TABLE public.tb_atribuicao_microarea (
    fim date,
    inicio date NOT NULL,
    membro_id uuid NOT NULL,
    microarea_id uuid NOT NULL,
    uuid uuid NOT NULL,
    motivo_fim character varying(500),
    registrado_por_cpf character varying(255)
);

ALTER TABLE public.tb_unidade_de_saude ADD COLUMN latitude double precision;
ALTER TABLE public.tb_unidade_de_saude ADD COLUMN longitude double precision;

ALTER TABLE ONLY public.tb_segmento_territorial ADD CONSTRAINT tb_segmento_territorial_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_segmento_territorial ADD CONSTRAINT uk_segmento_territorial_codigo UNIQUE (codigo);
ALTER TABLE ONLY public.tb_territorio ADD CONSTRAINT tb_territorio_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_territorio ADD CONSTRAINT uk_territorio_nome_por_segmento UNIQUE (segmento_id, nome);
ALTER TABLE ONLY public.tb_area ADD CONSTRAINT tb_area_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_area ADD CONSTRAINT uk_area_codigo_por_territorio UNIQUE (territorio_id, codigo);
ALTER TABLE ONLY public.tb_microarea ADD CONSTRAINT tb_microarea_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_microarea ADD CONSTRAINT uk_microarea_codigo_por_area UNIQUE (area_id, codigo);
ALTER TABLE ONLY public.tb_responsabilidade_territorial ADD CONSTRAINT tb_responsabilidade_territorial_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_atribuicao_microarea ADD CONSTRAINT tb_atribuicao_microarea_pkey PRIMARY KEY (uuid);

CREATE INDEX ix_responsabilidade_area ON public.tb_responsabilidade_territorial USING btree (area_id, fim);
CREATE INDEX ix_responsabilidade_equipe ON public.tb_responsabilidade_territorial USING btree (equipe_id, fim);
CREATE INDEX ix_atribuicao_microarea ON public.tb_atribuicao_microarea USING btree (microarea_id, fim);
CREATE INDEX ix_atribuicao_membro ON public.tb_atribuicao_microarea USING btree (membro_id, fim);

-- Uma equipe vigente por área e uma área vigente por equipe; um ACS vigente por microárea e uma microárea por ACS.
CREATE UNIQUE INDEX uk_responsabilidade_area_vigente ON public.tb_responsabilidade_territorial USING btree (area_id) WHERE (fim IS NULL);
CREATE UNIQUE INDEX uk_responsabilidade_equipe_vigente ON public.tb_responsabilidade_territorial USING btree (equipe_id) WHERE (fim IS NULL);
CREATE UNIQUE INDEX uk_atribuicao_microarea_vigente ON public.tb_atribuicao_microarea USING btree (microarea_id) WHERE (fim IS NULL);
CREATE UNIQUE INDEX uk_atribuicao_membro_vigente ON public.tb_atribuicao_microarea USING btree (membro_id) WHERE (fim IS NULL);

ALTER TABLE ONLY public.tb_segmento_territorial
    ADD CONSTRAINT fk_segmento_unidade_gestao FOREIGN KEY (unidade_gestao_id) REFERENCES public.tb_unidade_de_saude(uuid);
ALTER TABLE ONLY public.tb_territorio
    ADD CONSTRAINT fk_territorio_segmento FOREIGN KEY (segmento_id) REFERENCES public.tb_segmento_territorial(uuid);
ALTER TABLE ONLY public.tb_area
    ADD CONSTRAINT fk_area_territorio FOREIGN KEY (territorio_id) REFERENCES public.tb_territorio(uuid);
ALTER TABLE ONLY public.tb_microarea
    ADD CONSTRAINT fk_microarea_area FOREIGN KEY (area_id) REFERENCES public.tb_area(uuid);
ALTER TABLE ONLY public.tb_responsabilidade_territorial
    ADD CONSTRAINT fk_responsabilidade_area FOREIGN KEY (area_id) REFERENCES public.tb_area(uuid);
ALTER TABLE ONLY public.tb_responsabilidade_territorial
    ADD CONSTRAINT fk_responsabilidade_equipe FOREIGN KEY (equipe_id) REFERENCES public.tb_equipe(uuid);
ALTER TABLE ONLY public.tb_atribuicao_microarea
    ADD CONSTRAINT fk_atribuicao_microarea FOREIGN KEY (microarea_id) REFERENCES public.tb_microarea(uuid);
ALTER TABLE ONLY public.tb_atribuicao_microarea
    ADD CONSTRAINT fk_atribuicao_membro FOREIGN KEY (membro_id) REFERENCES public.tb_membro_equipe(uuid);

-- ── Migração do texto livre (ADR-0103 → ADR-0125) ─────────────────────────────────────────────────────────────
-- Cada equipe com microárea em texto (na equipe ou num ACS dela) ganha uma área própria, sem desenho, no território
-- "Migrado das equipes" (atenção primária) do segmento MIGRACAO, com a equipe responsável. Cada código vira uma
-- microárea; cada participação de ACS com microárea vira atribuição (a vigente, e a encerrada com o mesmo fim). Se
-- dois ACS vigentes tinham o mesmo código, fica o que entrou primeiro; o outro segue sem microárea, para acerto na tela.

CREATE TEMPORARY TABLE migracao_codigo ON COMMIT DROP AS
SELECT DISTINCT equipe_id, codigo FROM (
    SELECT e.uuid AS equipe_id, upper(left(trim(c), 20)) AS codigo
    FROM public.tb_equipe e, unnest(string_to_array(e.microareas, ',')) AS c
    UNION
    SELECT m.equipe_id, upper(left(trim(m.microarea), 20))
    FROM public.tb_membro_equipe m
    WHERE m.funcao = 'ACS' AND m.microarea IS NOT NULL
) x
WHERE codigo <> '';

INSERT INTO public.tb_segmento_territorial (uuid, codigo, nome, tipo)
SELECT gen_random_uuid(), 'MIGRACAO', 'Migração das equipes', 'OUTRO'
WHERE EXISTS (SELECT 1 FROM migracao_codigo);

INSERT INTO public.tb_territorio (uuid, segmento_id, nome, finalidade, ativo)
SELECT gen_random_uuid(), s.uuid, 'Migrado das equipes', 'ATENCAO_PRIMARIA', true
FROM public.tb_segmento_territorial s WHERE s.codigo = 'MIGRACAO';

CREATE TEMPORARY TABLE migracao_area ON COMMIT DROP AS
SELECT e.uuid AS equipe_id, gen_random_uuid() AS area_id,
       coalesce(e.ine, 'EQ-' || upper(left(replace(e.uuid::text, '-', ''), 8))) AS codigo,
       left(e.nome || ' · ' || u.nome, 120) AS nome
FROM public.tb_equipe e
JOIN public.tb_unidade_de_saude u ON u.uuid = e.unidade_id
WHERE EXISTS (SELECT 1 FROM migracao_codigo c WHERE c.equipe_id = e.uuid);

INSERT INTO public.tb_area (uuid, territorio_id, codigo, nome)
SELECT a.area_id, t.uuid, a.codigo, a.nome
FROM migracao_area a
JOIN public.tb_territorio t ON t.nome = 'Migrado das equipes'
JOIN public.tb_segmento_territorial s ON s.uuid = t.segmento_id AND s.codigo = 'MIGRACAO';

INSERT INTO public.tb_responsabilidade_territorial (uuid, area_id, equipe_id, inicio, registrado_por_cpf)
SELECT gen_random_uuid(), a.area_id, a.equipe_id,
       coalesce((SELECT min(m.inicio) FROM public.tb_membro_equipe m WHERE m.equipe_id = a.equipe_id), current_date),
       'migracao-v19'
FROM migracao_area a;

INSERT INTO public.tb_microarea (uuid, area_id, codigo)
SELECT gen_random_uuid(), a.area_id, c.codigo
FROM migracao_codigo c
JOIN migracao_area a ON a.equipe_id = c.equipe_id;

INSERT INTO public.tb_atribuicao_microarea (uuid, microarea_id, membro_id, inicio, fim, motivo_fim, registrado_por_cpf)
SELECT gen_random_uuid(), x.microarea_id, x.membro_id, x.inicio, x.fim,
       CASE WHEN x.fim IS NOT NULL THEN left('Saída da equipe: ' || coalesce(x.motivo_saida, 'sem motivo registrado'), 500) END,
       'migracao-v19'
FROM (
    SELECT mi.uuid AS microarea_id, m.uuid AS membro_id, m.inicio, m.fim, m.motivo_saida,
           row_number() OVER (PARTITION BY mi.uuid, (m.fim IS NULL) ORDER BY m.inicio, m.uuid) AS ordem
    FROM public.tb_membro_equipe m
    JOIN migracao_area a ON a.equipe_id = m.equipe_id
    JOIN public.tb_microarea mi ON mi.area_id = a.area_id AND mi.codigo = upper(left(trim(m.microarea), 20))
    WHERE m.funcao = 'ACS' AND m.microarea IS NOT NULL AND trim(m.microarea) <> ''
) x
WHERE x.fim IS NOT NULL OR x.ordem = 1;
