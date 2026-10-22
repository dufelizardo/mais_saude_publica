-- Saúde Bucal, fatia SB4a (ADR-0138): a capacidade que o item regulado exige da unidade executante (o CEO é uma unidade
-- com as capacidades odontológicas da MO1, sem tipo próprio) e a carga das especialidades odontológicas no catálogo da
-- Regulação. Conferido contra o ddl-auto=validate (ADR-0090).

ALTER TABLE public.tb_procedimento_regulado ADD COLUMN capacidade_exigida_id uuid;
ALTER TABLE ONLY public.tb_procedimento_regulado ADD CONSTRAINT fk_procedimento_regulado_capacidade FOREIGN KEY (capacidade_exigida_id) REFERENCES public.tb_capacidade_assistencial(uuid);

-- Especialidades odontológicas (MODELO-SAUDE-BUCAL, seção 9), cada uma com a capacidade do CEO. O nome é único no
-- catálogo: um item já cadastrado com o mesmo nome não é duplicado.
INSERT INTO public.tb_procedimento_regulado (uuid, nome, tipo, ativo, capacidade_exigida_id)
SELECT gen_random_uuid(), v.nome, 'CONSULTA_ESPECIALIZADA', true, c.uuid
FROM (VALUES
    ('Endodontia (CEO)', 'ODONTO_ENDODONTIA'),
    ('Periodontia (CEO)', 'ODONTO_PERIODONTIA'),
    ('Cirurgia oral menor (CEO)', 'ODONTO_CIRURGIA_ORAL_MENOR'),
    ('Diagnóstico bucal e câncer de boca (CEO)', 'ODONTO_DIAGNOSTICO_BUCAL'),
    ('Odontologia para pacientes com necessidades especiais (CEO)', 'ODONTO_PNE'),
    ('Prótese dentária', 'ODONTO_PROTESE')
) AS v(nome, capacidade)
JOIN public.tb_capacidade_assistencial c ON c.codigo = v.capacidade
WHERE NOT EXISTS (SELECT 1 FROM public.tb_procedimento_regulado r WHERE lower(r.nome) = lower(v.nome));
