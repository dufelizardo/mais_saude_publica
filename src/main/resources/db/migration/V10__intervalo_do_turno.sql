-- Escalas (ADR-0107): intervalo de repouso e alimentação no turno, que não conta na jornada, e o tipo DIURNO (8h + 1h).

ALTER TABLE public.tb_turno_escala ADD COLUMN intervalo_minutos integer DEFAULT 0 NOT NULL;

ALTER TABLE public.tb_turno_escala ALTER COLUMN intervalo_minutos DROP DEFAULT;

ALTER TABLE public.tb_turno_escala DROP CONSTRAINT IF EXISTS tb_turno_escala_tipo_check;
ALTER TABLE public.tb_turno_escala ADD CONSTRAINT tb_turno_escala_tipo_check CHECK (((tipo)::text = ANY ((ARRAY[
    'MANHA'::character varying, 'TARDE'::character varying, 'DIURNO'::character varying, 'NOITE'::character varying,
    'PLANTAO_12H'::character varying, 'PLANTAO_24H'::character varying, 'SOBREAVISO'::character varying,
    'CAPACITACAO'::character varying])::text[])));
