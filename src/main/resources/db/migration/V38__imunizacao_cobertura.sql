-- Imunização, fatia I3 (ADR-0149): cobertura vacinal e campanhas.

-- A dose do calendário que mede a cobertura da vacina, com a meta (dado, não código).
ALTER TABLE public.tb_esquema_vacinal ADD COLUMN mede_cobertura boolean NOT NULL DEFAULT false;
ALTER TABLE public.tb_esquema_vacinal ADD COLUMN meta_cobertura numeric(5,2);

-- Carga inicial editável: as doses de referência do PNI na criança, 90% para BCG e rotavírus, 95% para as demais.
UPDATE public.tb_esquema_vacinal e SET mede_cobertura = true, meta_cobertura = c.meta
FROM (VALUES
    ('BCG', 'UNICA', 90.00),
    ('ROTAVIRUS', 'SEGUNDA', 90.00),
    ('PENTA', 'TERCEIRA', 95.00),
    ('VIP', 'TERCEIRA', 95.00),
    ('PNEUMO_10', 'SEGUNDA', 95.00),
    ('MENINGO_C', 'SEGUNDA', 95.00),
    ('FEBRE_AMARELA', 'PRIMEIRA', 95.00),
    ('TRIPLICE_VIRAL', 'PRIMEIRA', 95.00),
    ('TETRA_VIRAL', 'UNICA', 95.00),
    ('HEPATITE_A', 'UNICA', 95.00),
    ('DTP', 'REFORCO', 95.00)
) AS c(codigo, dose, meta), public.tb_imunobiologico i
WHERE i.codigo = c.codigo AND e.imunobiologico_id = i.uuid AND e.dose = c.dose AND e.publico = 'CRIANCA';

-- As vacinas da campanha de vacinação (opcional): com a lista, a dose de outra vacina não entra na campanha.
CREATE TABLE public.tb_campanha_imunobiologico (
    campanha_id uuid NOT NULL,
    imunobiologico_id uuid NOT NULL
);
ALTER TABLE ONLY public.tb_campanha_imunobiologico ADD CONSTRAINT tb_campanha_imunobiologico_pkey PRIMARY KEY (campanha_id, imunobiologico_id);
ALTER TABLE ONLY public.tb_campanha_imunobiologico ADD CONSTRAINT fk_campanha_imunobiologico_campanha FOREIGN KEY (campanha_id) REFERENCES public.tb_campanha(uuid);
ALTER TABLE ONLY public.tb_campanha_imunobiologico ADD CONSTRAINT fk_campanha_imunobiologico_imunobiologico FOREIGN KEY (imunobiologico_id) REFERENCES public.tb_imunobiologico(uuid);

CREATE INDEX ix_registro_vacinacao_campanha ON public.tb_registro_vacinacao USING btree (campanha_id);
