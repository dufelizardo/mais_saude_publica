-- Modelo Operacional, fatia MO4 (ADR-0170): os domínios passam a consultar capacidades. As unidades que já têm leitos recebem o
-- perfil e as capacidades correspondentes, para nada deixar de funcionar; a sala de vacina passa a exigir SALA_VACINA, que a
-- ADR-0165 deixou de fora por engano. Sem mudança de esquema.

CREATE TEMPORARY TABLE tmp_capacidade_por_leito ON COMMIT DROP AS
SELECT DISTINCT l.unidade_id, c.uuid AS capacidade_id, c.perfil_operacional_id
FROM public.tb_leito l
JOIN (VALUES
    ('CLINICO', 'INTERNACAO_CLINICA'),
    ('PEDIATRICO', 'INTERNACAO_CLINICA'),
    ('ISOLAMENTO', 'INTERNACAO_CLINICA'),
    ('CIRURGICO', 'CIRURGIA'),
    ('OBSTETRICO', 'OBSTETRICIA'),
    ('UTI_ADULTO', 'UTI'),
    ('UTI_PEDIATRICA', 'UTI'),
    ('UTI_NEONATAL', 'UTI'),
    ('OBSERVACAO', 'OBSERVACAO')
) AS m(tipo, capacidade) ON m.tipo = l.tipo
JOIN public.tb_capacidade_assistencial c ON c.codigo = m.capacidade
WHERE l.ativo;

INSERT INTO public.tb_unidade_perfil_operacional (uuid, unidade_id, perfil_operacional_id, inicio)
SELECT gen_random_uuid(), x.unidade_id, x.perfil_operacional_id, CURRENT_DATE
FROM (SELECT DISTINCT unidade_id, perfil_operacional_id FROM tmp_capacidade_por_leito WHERE perfil_operacional_id IS NOT NULL) x
WHERE NOT EXISTS (SELECT 1 FROM public.tb_unidade_perfil_operacional a
                  WHERE a.unidade_id = x.unidade_id AND a.perfil_operacional_id = x.perfil_operacional_id AND a.fim IS NULL);

INSERT INTO public.tb_unidade_capacidade_assistencial (uuid, unidade_id, capacidade_assistencial_id, inicio, observacao)
SELECT gen_random_uuid(), x.unidade_id, x.capacidade_id, CURRENT_DATE, 'Incluída pela ADR-0170: a unidade já tinha leitos deste tipo.'
FROM tmp_capacidade_por_leito x
WHERE NOT EXISTS (SELECT 1 FROM public.tb_unidade_capacidade_assistencial a
                  WHERE a.unidade_id = x.unidade_id AND a.capacidade_assistencial_id = x.capacidade_id AND a.fim IS NULL);

UPDATE public.tb_tipo_servico t
SET capacidade_id = c.uuid
FROM public.tb_capacidade_assistencial c
WHERE t.codigo = 'SALA_DE_VACINA' AND t.capacidade_id IS NULL AND c.codigo = 'SALA_VACINA';

INSERT INTO public.tb_unidade_capacidade_assistencial (uuid, unidade_id, capacidade_assistencial_id, inicio, observacao)
SELECT DISTINCT ON (s.unidade_id) gen_random_uuid(), s.unidade_id, c.uuid, CURRENT_DATE, 'Incluída pela ADR-0170: a unidade já tinha este serviço.'
FROM public.tb_servico_unidade s
JOIN public.tb_tipo_servico t ON t.uuid = s.tipo_id AND t.codigo = 'SALA_DE_VACINA'
JOIN public.tb_capacidade_assistencial c ON c.codigo = 'SALA_VACINA'
WHERE s.fim IS NULL
  AND NOT EXISTS (SELECT 1 FROM public.tb_unidade_capacidade_assistencial a
                  WHERE a.unidade_id = s.unidade_id AND a.capacidade_assistencial_id = c.uuid AND a.fim IS NULL);
