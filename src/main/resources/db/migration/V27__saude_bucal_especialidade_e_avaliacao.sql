-- Saúde Bucal, fatia SB1 (ADR-0134): o catálogo de especialidades (com a carga inicial), a especialidade no atendimento,
-- no bloco da agenda e na marcação, e a avaliação odontológica (registro clínico com retificação, ADR-0062). Conferido
-- contra o ddl-auto=validate (ADR-0090).

CREATE TABLE public.tb_especialidade (
    ativa boolean NOT NULL,
    odontologica boolean NOT NULL,
    uuid uuid NOT NULL,
    codigo character varying(40) NOT NULL,
    nome character varying(100) NOT NULL
);

ALTER TABLE ONLY public.tb_especialidade ADD CONSTRAINT tb_especialidade_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_especialidade ADD CONSTRAINT uk_especialidade_codigo UNIQUE (codigo);

-- Carga inicial editável: as especialidades da atenção básica e as que já aparecem nas equipes.
INSERT INTO public.tb_especialidade (uuid, codigo, nome, odontologica, ativa) VALUES
    (gen_random_uuid(), 'CLINICA_GERAL', 'Clínica geral', false, true),
    (gen_random_uuid(), 'MEDICINA_FAMILIA', 'Medicina de família e comunidade', false, true),
    (gen_random_uuid(), 'ENFERMAGEM', 'Enfermagem', false, true),
    (gen_random_uuid(), 'ODONTOLOGIA', 'Odontologia', true, true),
    (gen_random_uuid(), 'PEDIATRIA', 'Pediatria', false, true),
    (gen_random_uuid(), 'GINECOLOGIA_OBSTETRICIA', 'Ginecologia e obstetrícia', false, true),
    (gen_random_uuid(), 'PSICOLOGIA', 'Psicologia', false, true),
    (gen_random_uuid(), 'SERVICO_SOCIAL', 'Serviço social', false, true);

ALTER TABLE public.tb_atendimento ADD COLUMN especialidade_id uuid;
ALTER TABLE ONLY public.tb_atendimento ADD CONSTRAINT fk_atendimento_especialidade FOREIGN KEY (especialidade_id) REFERENCES public.tb_especialidade(uuid);
ALTER TABLE public.tb_bloco_agenda ADD COLUMN especialidade_id uuid;
ALTER TABLE ONLY public.tb_bloco_agenda ADD CONSTRAINT fk_bloco_agenda_especialidade FOREIGN KEY (especialidade_id) REFERENCES public.tb_especialidade(uuid);
ALTER TABLE public.tb_agendamento ADD COLUMN especialidade_id uuid;
ALTER TABLE ONLY public.tb_agendamento ADD CONSTRAINT fk_agendamento_especialidade FOREIGN KEY (especialidade_id) REFERENCES public.tb_especialidade(uuid);

CREATE TABLE public.tb_avaliacao_odontologica (
    data_hora timestamp(6) without time zone NOT NULL,
    registrado_em timestamp(6) with time zone NOT NULL,
    atendimento_id uuid NOT NULL,
    profissional_id uuid NOT NULL,
    retificacao_de_id uuid,
    uuid uuid NOT NULL,
    motivo_retificacao character varying(500),
    queixa_principal character varying(1000),
    diagnostico character varying(2000),
    observacoes character varying(2000),
    plano_inicial character varying(2000),
    anamnese character varying(4000),
    exame_clinico character varying(4000),
    necessidade_tratamento character varying(255) NOT NULL,
    registrado_por_cpf character varying(255),
    risco character varying(255) NOT NULL,
    CONSTRAINT tb_avaliacao_odontologica_necessidade_tratamento_check CHECK (((necessidade_tratamento)::text = ANY ((ARRAY['SEM_NECESSIDADE'::character varying, 'ELETIVA'::character varying, 'PRIORITARIA'::character varying, 'URGENCIA'::character varying])::text[]))),
    CONSTRAINT tb_avaliacao_odontologica_risco_check CHECK (((risco)::text = ANY ((ARRAY['BAIXO'::character varying, 'MEDIO'::character varying, 'ALTO'::character varying])::text[])))
);

ALTER TABLE ONLY public.tb_avaliacao_odontologica ADD CONSTRAINT tb_avaliacao_odontologica_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_avaliacao_odonto_atendimento ON public.tb_avaliacao_odontologica USING btree (atendimento_id);
ALTER TABLE ONLY public.tb_avaliacao_odontologica ADD CONSTRAINT fk_avaliacao_odonto_atendimento FOREIGN KEY (atendimento_id) REFERENCES public.tb_atendimento(uuid);
ALTER TABLE ONLY public.tb_avaliacao_odontologica ADD CONSTRAINT fk_avaliacao_odonto_profissional FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);
ALTER TABLE ONLY public.tb_avaliacao_odontologica ADD CONSTRAINT fk_avaliacao_odonto_retificacao FOREIGN KEY (retificacao_de_id) REFERENCES public.tb_avaliacao_odontologica(uuid);
