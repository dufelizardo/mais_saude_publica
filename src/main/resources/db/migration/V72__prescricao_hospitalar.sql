-- Leitos, fatia RL4 (ADR-0187): a prescrição hospitalar diária com medicamentos, dieta e cuidados, o aprazamento dos horários e a
-- checagem pela enfermagem (e pela nutrição, na dieta). Conferido contra o ddl-auto=validate (ADR-0090). As permissões
-- PRESCRICAO.HOSPITALAR, PRESCRICAO.CHECAR e DIETA.CHECAR e o papel Nutricionista vêm do CatalogoDeAcesso.

CREATE TABLE public.tb_prescricao_hospitalar (
    encerrada_em timestamp(6) with time zone,
    fim timestamp(6) with time zone NOT NULL,
    inicio timestamp(6) with time zone NOT NULL,
    registrada_em timestamp(6) with time zone NOT NULL,
    internacao_id uuid NOT NULL,
    medico_id uuid NOT NULL,
    renovada_de uuid,
    uuid uuid NOT NULL,
    observacao character varying(1000),
    registrada_por_cpf character varying(255)
);
ALTER TABLE ONLY public.tb_prescricao_hospitalar ADD CONSTRAINT tb_prescricao_hospitalar_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_prescricao_hospitalar ADD CONSTRAINT fk_prescricao_hospitalar_internacao FOREIGN KEY (internacao_id) REFERENCES public.tb_internacao(uuid);
ALTER TABLE ONLY public.tb_prescricao_hospitalar ADD CONSTRAINT fk_prescricao_hospitalar_medico FOREIGN KEY (medico_id) REFERENCES public.tb_profissional(uuid);
CREATE INDEX ix_prescricao_hospitalar_internacao ON public.tb_prescricao_hospitalar USING btree (internacao_id, inicio);

CREATE TABLE public.tb_item_prescricao (
    intervalo_horas integer,
    ordem integer NOT NULL,
    se_necessario boolean NOT NULL,
    primeiro_horario timestamp(6) with time zone,
    suspenso_em timestamp(6) with time zone,
    medicamento_id uuid,
    prescricao_id uuid NOT NULL,
    suspenso_por_id uuid,
    uuid uuid NOT NULL,
    dose character varying(100),
    condicao character varying(500),
    descricao character varying(1000),
    motivo_suspensao character varying(1000),
    tipo character varying(255) NOT NULL,
    tipo_dieta character varying(255),
    via character varying(255),
    CONSTRAINT tb_item_prescricao_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['MEDICAMENTO'::character varying, 'DIETA'::character varying, 'CUIDADO'::character varying])::text[]))),
    CONSTRAINT tb_item_prescricao_tipo_dieta_check CHECK (((tipo_dieta)::text = ANY ((ARRAY['ZERO'::character varying, 'LIVRE'::character varying, 'BRANDA'::character varying, 'PASTOSA'::character varying, 'LIQUIDA'::character varying, 'LIQUIDA_PASTOSA'::character varying, 'HIPOSSODICA'::character varying, 'PARA_DIABETICO'::character varying, 'ENTERAL'::character varying, 'PARENTERAL'::character varying])::text[]))),
    CONSTRAINT tb_item_prescricao_via_check CHECK (((via)::text = ANY ((ARRAY['ORAL'::character varying, 'SUBLINGUAL'::character varying, 'INTRAMUSCULAR'::character varying, 'INTRAVENOSA'::character varying, 'SUBCUTANEA'::character varying, 'TOPICA'::character varying, 'INALATORIA'::character varying, 'RETAL'::character varying, 'OUTRA'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_item_prescricao ADD CONSTRAINT tb_item_prescricao_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_item_prescricao ADD CONSTRAINT fk_item_prescricao_prescricao FOREIGN KEY (prescricao_id) REFERENCES public.tb_prescricao_hospitalar(uuid);
ALTER TABLE ONLY public.tb_item_prescricao ADD CONSTRAINT fk_item_prescricao_medicamento FOREIGN KEY (medicamento_id) REFERENCES public.tb_medicamento(uuid);
ALTER TABLE ONLY public.tb_item_prescricao ADD CONSTRAINT fk_item_prescricao_suspenso_por FOREIGN KEY (suspenso_por_id) REFERENCES public.tb_profissional(uuid);
CREATE INDEX ix_item_prescricao_prescricao ON public.tb_item_prescricao USING btree (prescricao_id, ordem);

CREATE TABLE public.tb_horario_prescricao (
    aprazado_originalmente timestamp(6) with time zone,
    previsto_para timestamp(6) with time zone NOT NULL,
    item_id uuid NOT NULL,
    uuid uuid NOT NULL,
    motivo_reaprazamento character varying(500),
    situacao character varying(255) NOT NULL,
    CONSTRAINT tb_horario_prescricao_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['PENDENTE'::character varying, 'FEITO'::character varying, 'NAO_FEITO'::character varying, 'CANCELADO'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_horario_prescricao ADD CONSTRAINT tb_horario_prescricao_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_horario_prescricao ADD CONSTRAINT fk_horario_prescricao_item FOREIGN KEY (item_id) REFERENCES public.tb_item_prescricao(uuid);
CREATE INDEX ix_horario_prescricao_item ON public.tb_horario_prescricao USING btree (item_id, previsto_para);
CREATE INDEX ix_horario_prescricao_previsto ON public.tb_horario_prescricao USING btree (previsto_para, situacao);

CREATE TABLE public.tb_checagem_prescricao (
    quantidade integer,
    retificada boolean NOT NULL,
    realizado_em timestamp(6) with time zone NOT NULL,
    registrado_em timestamp(6) with time zone NOT NULL,
    horario_id uuid,
    item_id uuid NOT NULL,
    lote_id uuid,
    profissional_id uuid NOT NULL,
    retificacao_de_id uuid,
    uuid uuid NOT NULL,
    observacao character varying(1000),
    motivo_retificacao character varying(1000),
    registrado_por_cpf character varying(255),
    aceitacao character varying(255),
    motivo_nao_administracao character varying(255),
    resultado character varying(255) NOT NULL,
    CONSTRAINT tb_checagem_prescricao_resultado_check CHECK (((resultado)::text = ANY ((ARRAY['FEITO'::character varying, 'NAO_FEITO'::character varying])::text[]))),
    CONSTRAINT tb_checagem_prescricao_aceitacao_check CHECK (((aceitacao)::text = ANY ((ARRAY['TOTAL'::character varying, 'PARCIAL'::character varying, 'RECUSOU'::character varying])::text[]))),
    CONSTRAINT tb_checagem_prescricao_motivo_nao_administracao_check CHECK (((motivo_nao_administracao)::text = ANY ((ARRAY['RECUSA_DO_PACIENTE'::character varying, 'PACIENTE_AUSENTE'::character varying, 'MEDICAMENTO_EM_FALTA'::character varying, 'SUSPENSO_PELO_MEDICO'::character varying, 'OUTRO'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_checagem_prescricao ADD CONSTRAINT tb_checagem_prescricao_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_checagem_prescricao ADD CONSTRAINT fk_checagem_prescricao_item FOREIGN KEY (item_id) REFERENCES public.tb_item_prescricao(uuid);
ALTER TABLE ONLY public.tb_checagem_prescricao ADD CONSTRAINT fk_checagem_prescricao_horario FOREIGN KEY (horario_id) REFERENCES public.tb_horario_prescricao(uuid);
ALTER TABLE ONLY public.tb_checagem_prescricao ADD CONSTRAINT fk_checagem_prescricao_lote FOREIGN KEY (lote_id) REFERENCES public.tb_lote(uuid);
ALTER TABLE ONLY public.tb_checagem_prescricao ADD CONSTRAINT fk_checagem_prescricao_profissional FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);
ALTER TABLE ONLY public.tb_checagem_prescricao ADD CONSTRAINT fk_checagem_prescricao_retificacao FOREIGN KEY (retificacao_de_id) REFERENCES public.tb_checagem_prescricao(uuid);
CREATE INDEX ix_checagem_prescricao_item ON public.tb_checagem_prescricao USING btree (item_id, realizado_em);
CREATE INDEX ix_checagem_prescricao_horario ON public.tb_checagem_prescricao USING btree (horario_id);
