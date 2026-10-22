-- Programas de saúde, fatia P4 (ADR-0132): a ação programática (individual ou coletiva, com os participantes), a busca
-- ativa e o programa na marcação da Agenda. Conferido contra o ddl-auto=validate (ADR-0090).

CREATE TABLE public.tb_acao_programatica (
    data_prevista date NOT NULL,
    realizada_em date,
    criada_em timestamp(6) with time zone NOT NULL,
    acompanhamento_id uuid,
    agendamento_id uuid,
    atendimento_id uuid,
    equipe_id uuid,
    paciente_id uuid,
    programa_id uuid NOT NULL,
    responsavel_id uuid,
    unidade_id uuid NOT NULL,
    uuid uuid NOT NULL,
    local character varying(200),
    motivo character varying(500),
    descricao character varying(1000),
    observacao character varying(1000),
    registrado_por_cpf character varying(255),
    resultado_busca character varying(255),
    situacao character varying(255) NOT NULL,
    tipo character varying(255) NOT NULL,
    CONSTRAINT tb_acao_programatica_resultado_busca_check CHECK (((resultado_busca)::text = ANY ((ARRAY['ENCONTRADO_AGENDADO'::character varying, 'ENCONTRADO_RECUSOU'::character varying, 'NAO_ENCONTRADO'::character varying, 'MUDOU_DE_ENDERECO'::character varying])::text[]))),
    CONSTRAINT tb_acao_programatica_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['PENDENTE'::character varying, 'REALIZADA'::character varying, 'NAO_REALIZADA'::character varying, 'CANCELADA'::character varying])::text[]))),
    CONSTRAINT tb_acao_programatica_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['CONSULTA_PROGRAMADA'::character varying, 'AFERICAO'::character varying, 'EXAME'::character varying, 'VACINA'::character varying, 'VISITA_DOMICILIAR'::character varying, 'BUSCA_ATIVA'::character varying, 'GRUPO'::character varying, 'ORIENTACAO'::character varying, 'OUTRA'::character varying])::text[])))
);

ALTER TABLE ONLY public.tb_acao_programatica ADD CONSTRAINT tb_acao_programatica_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_acao_programa ON public.tb_acao_programatica USING btree (programa_id, data_prevista);
CREATE INDEX ix_acao_unidade ON public.tb_acao_programatica USING btree (unidade_id, data_prevista);
CREATE INDEX ix_acao_paciente ON public.tb_acao_programatica USING btree (paciente_id);
ALTER TABLE ONLY public.tb_acao_programatica ADD CONSTRAINT fk_acao_programa FOREIGN KEY (programa_id) REFERENCES public.tb_programa(uuid);
ALTER TABLE ONLY public.tb_acao_programatica ADD CONSTRAINT fk_acao_acompanhamento FOREIGN KEY (acompanhamento_id) REFERENCES public.tb_acompanhamento_programatico(uuid);
ALTER TABLE ONLY public.tb_acao_programatica ADD CONSTRAINT fk_acao_paciente FOREIGN KEY (paciente_id) REFERENCES public.tb_paciente(uuid);
ALTER TABLE ONLY public.tb_acao_programatica ADD CONSTRAINT fk_acao_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);
ALTER TABLE ONLY public.tb_acao_programatica ADD CONSTRAINT fk_acao_equipe FOREIGN KEY (equipe_id) REFERENCES public.tb_equipe(uuid);
ALTER TABLE ONLY public.tb_acao_programatica ADD CONSTRAINT fk_acao_responsavel FOREIGN KEY (responsavel_id) REFERENCES public.tb_profissional(uuid);
ALTER TABLE ONLY public.tb_acao_programatica ADD CONSTRAINT fk_acao_atendimento FOREIGN KEY (atendimento_id) REFERENCES public.tb_atendimento(uuid);
ALTER TABLE ONLY public.tb_acao_programatica ADD CONSTRAINT fk_acao_agendamento FOREIGN KEY (agendamento_id) REFERENCES public.tb_agendamento(uuid);

CREATE TABLE public.tb_acao_programatica_participante (
    acao_id uuid NOT NULL,
    paciente_id uuid NOT NULL
);

ALTER TABLE ONLY public.tb_acao_programatica_participante ADD CONSTRAINT tb_acao_programatica_participante_pkey PRIMARY KEY (acao_id, paciente_id);
ALTER TABLE ONLY public.tb_acao_programatica_participante ADD CONSTRAINT fk_participante_acao FOREIGN KEY (acao_id) REFERENCES public.tb_acao_programatica(uuid);
ALTER TABLE ONLY public.tb_acao_programatica_participante ADD CONSTRAINT fk_participante_paciente FOREIGN KEY (paciente_id) REFERENCES public.tb_paciente(uuid);

ALTER TABLE public.tb_agendamento ADD COLUMN programa_id uuid;
ALTER TABLE ONLY public.tb_agendamento ADD CONSTRAINT fk_agendamento_programa FOREIGN KEY (programa_id) REFERENCES public.tb_programa(uuid);
