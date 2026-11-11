-- Emergências, fatia E4a (ADR-0193): os pacientes deslocados (origem, destino, situação e o histórico da realocação) e o
-- encaminhamento intersetorial com origem nas Emergências. Os vulneráveis são calculados, sem tabela. Conferido contra o
-- ddl-auto=validate (ADR-0090).

CREATE TABLE public.tb_paciente_deslocado (
    saida_em timestamp(6) with time zone NOT NULL,
    fim_em timestamp(6) with time zone,
    registrado_em timestamp(6) with time zone NOT NULL,
    uuid uuid NOT NULL,
    emergencia_id uuid NOT NULL,
    paciente_id uuid NOT NULL,
    origem_unidade_id uuid,
    destino_unidade_id uuid,
    destino_instituicao_id uuid,
    anterior_id uuid,
    solicitacao_transporte_id uuid,
    origem_tipo character varying(20) NOT NULL,
    destino_tipo character varying(20) NOT NULL,
    situacao character varying(20) NOT NULL,
    responsavel character varying(200),
    origem_descricao character varying(500),
    destino_descricao character varying(500),
    motivo_fim character varying(500),
    necessidades character varying(1000),
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_paciente_deslocado_origem_tipo_check CHECK (origem_tipo IN ('DOMICILIO', 'UNIDADE_SAUDE', 'ABRIGO', 'CASA_PARENTE', 'OUTRO')),
    CONSTRAINT tb_paciente_deslocado_destino_tipo_check CHECK (destino_tipo IN ('DOMICILIO', 'UNIDADE_SAUDE', 'ABRIGO', 'CASA_PARENTE', 'OUTRO')),
    CONSTRAINT tb_paciente_deslocado_situacao_check CHECK (situacao IN ('DESLOCADO', 'REALOCADO', 'RETORNOU', 'ENCERRADO'))
);
ALTER TABLE ONLY public.tb_paciente_deslocado ADD CONSTRAINT tb_paciente_deslocado_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_paciente_deslocado ADD CONSTRAINT fk_paciente_deslocado_emergencia FOREIGN KEY (emergencia_id) REFERENCES public.tb_emergencia(uuid);
ALTER TABLE ONLY public.tb_paciente_deslocado ADD CONSTRAINT fk_paciente_deslocado_paciente FOREIGN KEY (paciente_id) REFERENCES public.tb_paciente(uuid);
ALTER TABLE ONLY public.tb_paciente_deslocado ADD CONSTRAINT fk_paciente_deslocado_origem FOREIGN KEY (origem_unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);
ALTER TABLE ONLY public.tb_paciente_deslocado ADD CONSTRAINT fk_paciente_deslocado_destino FOREIGN KEY (destino_unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);
ALTER TABLE ONLY public.tb_paciente_deslocado ADD CONSTRAINT fk_paciente_deslocado_instituicao FOREIGN KEY (destino_instituicao_id) REFERENCES public.tb_instituicao_externa(uuid);
ALTER TABLE ONLY public.tb_paciente_deslocado ADD CONSTRAINT fk_paciente_deslocado_anterior FOREIGN KEY (anterior_id) REFERENCES public.tb_paciente_deslocado(uuid);
ALTER TABLE ONLY public.tb_paciente_deslocado ADD CONSTRAINT fk_paciente_deslocado_transporte FOREIGN KEY (solicitacao_transporte_id) REFERENCES public.tb_solicitacao_transporte(uuid);
CREATE INDEX ix_paciente_deslocado_emergencia ON public.tb_paciente_deslocado USING btree (emergencia_id, situacao);
CREATE INDEX ix_paciente_deslocado_paciente ON public.tb_paciente_deslocado USING btree (paciente_id, situacao);

-- O encaminhamento intersetorial passa a aceitar a origem nas Emergências (ADR-0154, item 3).
ALTER TABLE public.tb_encaminhamento_intersetorial DROP CONSTRAINT tb_encaminhamento_intersetorial_dominio_origem_check;
ALTER TABLE public.tb_encaminhamento_intersetorial ADD CONSTRAINT tb_encaminhamento_intersetorial_dominio_origem_check
    CHECK (dominio_origem IN ('SAUDE_MENTAL', 'EMERGENCIAS'));
