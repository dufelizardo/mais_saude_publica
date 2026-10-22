-- Fim do texto livre das microáreas (ADR-0129): Equipe.microareas e MembroEquipe.microarea saem. Antes, o que ainda
-- estiver em texto em equipe SEM área vigente no Território (gravado depois da V19) vai para o Território do mesmo jeito
-- da V19: uma área da equipe no território "Migrado das equipes", uma microárea por código e a atribuição do ACS.
-- Conferido contra o ddl-auto=validate (ADR-0090).

CREATE TEMPORARY TABLE sobra_codigo ON COMMIT DROP AS
SELECT DISTINCT equipe_id, codigo FROM (
    SELECT e.uuid AS equipe_id, upper(left(trim(c), 20)) AS codigo
    FROM public.tb_equipe e, unnest(string_to_array(e.microareas, ',')) AS c
    UNION
    SELECT m.equipe_id, upper(left(trim(m.microarea), 20))
    FROM public.tb_membro_equipe m
    WHERE m.funcao = 'ACS' AND m.microarea IS NOT NULL
) x
WHERE codigo <> ''
  AND NOT EXISTS (SELECT 1 FROM public.tb_responsabilidade_territorial r WHERE r.equipe_id = x.equipe_id AND r.fim IS NULL);

INSERT INTO public.tb_segmento_territorial (uuid, codigo, nome, tipo)
SELECT gen_random_uuid(), 'MIGRACAO', 'Migração das equipes', 'OUTRO'
WHERE EXISTS (SELECT 1 FROM sobra_codigo)
  AND NOT EXISTS (SELECT 1 FROM public.tb_segmento_territorial WHERE codigo = 'MIGRACAO');

INSERT INTO public.tb_territorio (uuid, segmento_id, nome, finalidade, ativo)
SELECT gen_random_uuid(), s.uuid, 'Migrado das equipes', 'ATENCAO_PRIMARIA', true
FROM public.tb_segmento_territorial s
WHERE s.codigo = 'MIGRACAO' AND EXISTS (SELECT 1 FROM sobra_codigo)
  AND NOT EXISTS (SELECT 1 FROM public.tb_territorio t WHERE t.segmento_id = s.uuid AND t.nome = 'Migrado das equipes');

CREATE TEMPORARY TABLE sobra_area ON COMMIT DROP AS
SELECT e.uuid AS equipe_id, gen_random_uuid() AS area_id, t.uuid AS territorio_id,
       CASE WHEN EXISTS (SELECT 1 FROM public.tb_area a WHERE a.territorio_id = t.uuid
                         AND a.codigo = coalesce(e.ine, 'EQ-' || upper(left(replace(e.uuid::text, '-', ''), 8))))
            THEN 'EQ-' || upper(left(md5(e.uuid::text || 'v22'), 8))
            ELSE coalesce(e.ine, 'EQ-' || upper(left(replace(e.uuid::text, '-', ''), 8))) END AS codigo,
       left(e.nome || ' · ' || u.nome, 120) AS nome
FROM public.tb_equipe e
JOIN public.tb_unidade_de_saude u ON u.uuid = e.unidade_id
JOIN public.tb_segmento_territorial s ON s.codigo = 'MIGRACAO'
JOIN public.tb_territorio t ON t.segmento_id = s.uuid AND t.nome = 'Migrado das equipes'
WHERE EXISTS (SELECT 1 FROM sobra_codigo c WHERE c.equipe_id = e.uuid);

INSERT INTO public.tb_area (uuid, territorio_id, codigo, nome)
SELECT area_id, territorio_id, codigo, nome FROM sobra_area;

INSERT INTO public.tb_responsabilidade_territorial (uuid, area_id, equipe_id, inicio, registrado_por_cpf)
SELECT gen_random_uuid(), a.area_id, a.equipe_id,
       coalesce((SELECT min(m.inicio) FROM public.tb_membro_equipe m WHERE m.equipe_id = a.equipe_id), current_date),
       'migracao-v22'
FROM sobra_area a;

INSERT INTO public.tb_microarea (uuid, area_id, codigo)
SELECT gen_random_uuid(), a.area_id, c.codigo
FROM sobra_codigo c
JOIN sobra_area a ON a.equipe_id = c.equipe_id;

INSERT INTO public.tb_atribuicao_microarea (uuid, microarea_id, membro_id, inicio, fim, motivo_fim, registrado_por_cpf)
SELECT gen_random_uuid(), x.microarea_id, x.membro_id, x.inicio, x.fim,
       CASE WHEN x.fim IS NOT NULL THEN left('Saída da equipe: ' || coalesce(x.motivo_saida, 'sem motivo registrado'), 500) END,
       'migracao-v22'
FROM (
    SELECT mi.uuid AS microarea_id, m.uuid AS membro_id, m.inicio, m.fim, m.motivo_saida,
           row_number() OVER (PARTITION BY mi.uuid, (m.fim IS NULL) ORDER BY m.inicio, m.uuid) AS ordem,
           row_number() OVER (PARTITION BY m.uuid, (m.fim IS NULL) ORDER BY mi.codigo) AS ordem_membro
    FROM public.tb_membro_equipe m
    JOIN sobra_area a ON a.equipe_id = m.equipe_id
    JOIN public.tb_microarea mi ON mi.area_id = a.area_id AND mi.codigo = upper(left(trim(m.microarea), 20))
    WHERE m.funcao = 'ACS' AND m.microarea IS NOT NULL AND trim(m.microarea) <> ''
) x
WHERE x.fim IS NOT NULL OR (x.ordem = 1 AND x.ordem_membro = 1);

ALTER TABLE public.tb_equipe DROP COLUMN microareas;
ALTER TABLE public.tb_membro_equipe DROP COLUMN microarea;
