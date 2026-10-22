-- Adscrição, fatia F3b (ADR-0128): o paciente adscrito a uma equipe, com início e fim, pelo território (o domicílio onde
-- mora, numa área com equipe responsável) ou à mão, com motivo. Uma vigente por paciente. Conferido contra o
-- ddl-auto=validate (ADR-0090).

CREATE TABLE public.tb_adscricao (
    fim date,
    inicio date NOT NULL,
    equipe_id uuid NOT NULL,
    microarea_id uuid,
    paciente_id uuid NOT NULL,
    uuid uuid NOT NULL,
    motivo character varying(500),
    motivo_fim character varying(500),
    origem character varying(255) NOT NULL,
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_adscricao_origem_check CHECK (((origem)::text = ANY ((ARRAY['TERRITORIO'::character varying, 'MANUAL'::character varying])::text[])))
);

ALTER TABLE ONLY public.tb_adscricao ADD CONSTRAINT tb_adscricao_pkey PRIMARY KEY (uuid);

CREATE INDEX ix_adscricao_paciente ON public.tb_adscricao USING btree (paciente_id, fim);
CREATE INDEX ix_adscricao_equipe ON public.tb_adscricao USING btree (equipe_id, fim);

-- Uma adscrição vigente por paciente.
CREATE UNIQUE INDEX uk_adscricao_paciente_vigente ON public.tb_adscricao USING btree (paciente_id) WHERE (fim IS NULL);

ALTER TABLE ONLY public.tb_adscricao
    ADD CONSTRAINT fk_adscricao_paciente FOREIGN KEY (paciente_id) REFERENCES public.tb_paciente(uuid);
ALTER TABLE ONLY public.tb_adscricao
    ADD CONSTRAINT fk_adscricao_equipe FOREIGN KEY (equipe_id) REFERENCES public.tb_equipe(uuid);
ALTER TABLE ONLY public.tb_adscricao
    ADD CONSTRAINT fk_adscricao_microarea FOREIGN KEY (microarea_id) REFERENCES public.tb_microarea(uuid);

-- Quem já mora num domicílio cadastrado (F3a) de área com equipe responsável fica adscrito a ela, desde a entrada na
-- família ou desde a equipe na área, o que for mais recente.
INSERT INTO public.tb_adscricao (uuid, paciente_id, equipe_id, microarea_id, origem, inicio, registrado_por_cpf)
SELECT gen_random_uuid(), m.paciente_id, r.equipe_id, i.microarea_id, 'TERRITORIO', greatest(m.inicio, r.inicio), 'migracao-v21'
FROM public.tb_membro_nucleo m
JOIN public.tb_nucleo_familiar n ON n.uuid = m.nucleo_id
JOIN public.tb_domicilio d ON d.uuid = n.domicilio_id
JOIN public.tb_imovel i ON i.uuid = d.imovel_id
JOIN public.tb_microarea mi ON mi.uuid = i.microarea_id
JOIN public.tb_responsabilidade_territorial r ON r.area_id = mi.area_id AND r.fim IS NULL
WHERE m.fim IS NULL;
