-- Agenda do profissional (ADR-0091): blocos recorrentes por unidade, bloqueios, e o agendamento com unidade,
-- encaixe e o status de falta.

-- ── Agendamento ─────────────────────────────────────────────────────────────────────────────────────
ALTER TABLE public.tb_agendamento ADD COLUMN IF NOT EXISTS unidade_id uuid;
ALTER TABLE public.tb_agendamento ADD COLUMN IF NOT EXISTS encaixe boolean DEFAULT false NOT NULL;

ALTER TABLE public.tb_agendamento DROP CONSTRAINT IF EXISTS tb_agendamento_status_check;
ALTER TABLE public.tb_agendamento ADD CONSTRAINT tb_agendamento_status_check CHECK (((status)::text = ANY ((ARRAY[
    'AGENDADO'::character varying, 'CONFIRMADO'::character varying, 'REALIZADO'::character varying,
    'CANCELADO'::character varying, 'FALTOU'::character varying])::text[])));

ALTER TABLE ONLY public.tb_agendamento
    ADD CONSTRAINT fkord65ni9iihrdjantklqxsp6u FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);

-- A agenda lê as marcações de um profissional num período.
CREATE INDEX ix_agendamento_profissional_data ON public.tb_agendamento USING btree (profissional_id, data_hora);

-- ── Blocos recorrentes ──────────────────────────────────────────────────────────────────────────────
CREATE TABLE public.tb_bloco_agenda (
    uuid uuid NOT NULL,
    duracao_minutos integer NOT NULL,
    hora_fim time(0) without time zone NOT NULL,
    hora_inicio time(0) without time zone NOT NULL,
    vigente_ate date,
    vigente_desde date NOT NULL,
    profissional_id uuid NOT NULL,
    unidade_id uuid NOT NULL,
    dia_semana character varying(255) NOT NULL,
    tipo character varying(255) NOT NULL,
    CONSTRAINT tb_bloco_agenda_pkey PRIMARY KEY (uuid),
    CONSTRAINT tb_bloco_agenda_dia_semana_check CHECK (((dia_semana)::text = ANY ((ARRAY[
        'MONDAY'::character varying, 'TUESDAY'::character varying, 'WEDNESDAY'::character varying, 'THURSDAY'::character varying,
        'FRIDAY'::character varying, 'SATURDAY'::character varying, 'SUNDAY'::character varying])::text[]))),
    CONSTRAINT tb_bloco_agenda_tipo_check CHECK (((tipo)::text = ANY ((ARRAY[
        'CONSULTA'::character varying, 'PROCEDIMENTO'::character varying, 'RETORNO'::character varying])::text[]))),
    CONSTRAINT fkok2c2n44kkg58jdl1locip6rf FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid),
    CONSTRAINT fkgakpl9vk7y0xqtoyb08pog2yp FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid)
);

CREATE INDEX ix_bloco_agenda_profissional ON public.tb_bloco_agenda USING btree (profissional_id, unidade_id);

-- ── Bloqueios ───────────────────────────────────────────────────────────────────────────────────────
CREATE TABLE public.tb_bloqueio_agenda (
    uuid uuid NOT NULL,
    fim timestamp(6) without time zone NOT NULL,
    inicio timestamp(6) without time zone NOT NULL,
    registrado_em timestamp(6) with time zone NOT NULL,
    profissional_id uuid,
    unidade_id uuid,
    descricao character varying(500),
    motivo character varying(255) NOT NULL,
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_bloqueio_agenda_pkey PRIMARY KEY (uuid),
    CONSTRAINT tb_bloqueio_agenda_motivo_check CHECK (((motivo)::text = ANY ((ARRAY[
        'FOLGA'::character varying, 'REUNIAO'::character varying, 'CAPACITACAO'::character varying,
        'UNIDADE_FECHADA'::character varying, 'OUTRO'::character varying])::text[]))),
    CONSTRAINT fkmhskcgynw1bxcnkmtq4p59wvd FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid),
    CONSTRAINT fkshxn5y2sppslxvbvsddrh2aj0 FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid)
);

CREATE INDEX ix_bloqueio_agenda_periodo ON public.tb_bloqueio_agenda USING btree (inicio, fim);
